/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Guards the channel model.
 *
 * <p>
 * These tests catch the two failure modes that are otherwise invisible: a channel that points at a tag
 * the catalogue does not know, and a channel definition that cannot be published (a nonsense divisor
 * or a bit mask that is not a single bit).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCChannelSpecTest {

    private static List<E3DCChannelRegistry> allRegistries() {
        return List.of(E3DCChannelSpec.powerPlant(), E3DCChannelSpec.wallbox(), E3DCChannelSpec.powerMeter());
    }

    private static List<Map.Entry<String, Map<String, E3DCChannelSpec>>> allGroups() {
        List<Map.Entry<String, Map<String, E3DCChannelSpec>>> groups = new ArrayList<>();
        for (E3DCChannelRegistry registry : allRegistries()) {
            groups.addAll(registry.groups().entrySet());
        }
        return groups;
    }

    @Test
    void everyChannelPointsAtATagTheCatalogueKnows() {
        List<String> unknown = new ArrayList<>();
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                if (RscpTag.nameOf(channel.getValue().tag()).startsWith("UNKNOWN_")) {
                    unknown.add(group.getKey() + "#" + channel.getKey() + " -> "
                            + String.format("0x%08X", channel.getValue().tag()));
                }
            }
        }
        assertTrue(unknown.isEmpty(), "channels reference tags missing from RscpTag: " + unknown);
    }

    @Test
    void everyContainerIsAKnownTagToo() {
        List<String> unknown = new ArrayList<>();
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                int container = channel.getValue().container();
                if (container != 0 && RscpTag.nameOf(container).startsWith("UNKNOWN_")) {
                    unknown.add(group.getKey() + "#" + channel.getKey());
                }
            }
        }
        assertTrue(unknown.isEmpty(), "channels reference unknown containers: " + unknown);
    }

    @Test
    void everyScalingFactorIsAPositiveNumber() {
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                assertTrue(channel.getValue().divisor() > 0,
                        group.getKey() + "#" + channel.getKey() + " has divisor " + channel.getValue().divisor());
            }
        }
    }

    @Test
    void everyBitMaskIsExactlyOneBit() {
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                int mask = channel.getValue().bitMask();
                if (mask != 0) {
                    assertTrue(Integer.bitCount(mask) == 1, group.getKey() + "#" + channel.getKey() + " uses mask 0x"
                            + Integer.toHexString(mask) + " which selects more than one bit");
                }
            }
        }
    }

    @Test
    void channelIdsAreUniqueWithinAGroup() {
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            // a Map cannot hold duplicates, so verify the ids are also non blank and lower case kebab
            for (String channelId : group.getValue().keySet()) {
                assertFalse(channelId.isBlank());
                assertTrue(channelId.equals(channelId.toLowerCase(java.util.Locale.ROOT)),
                        "channel id is not lower case: " + channelId);
            }
        }
    }

    @Test
    void scalingConvertsWhToKWh() {
        E3DCChannelSpec energy = E3DCChannelSpec.scaledNested(RscpTag.DB_HISTORY_DATA_DAY, RscpTag.DB_SUM_CONTAINER,
                RscpTag.DB_DC_POWER, 1000);
        assertEquals(12.5, energy.scale(12500), 0.0001);
    }

    @Test
    void scalingLeavesUnscaledValuesAlone() {
        E3DCChannelSpec power = E3DCChannelSpec.of(RscpTag.EMS_POWER_PV);
        assertEquals(4711, power.scale(4711), 0.0001);
    }

    @Test
    void aBitFieldReportsItsBitAndNotTheWholeWord() {
        E3DCChannelSpec spec = E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 4);

        assertTrue(spec.isBitField());
        assertEquals(4, spec.bitMask());
        // the mask must select the bit, the caller compares against zero
        assertEquals(4, 0x04 & spec.bitMask());
        assertEquals(0, 0x02 & spec.bitMask());
    }

    @Test
    void aByteFieldRecordsItsIndexAndAPlainValueDoesNot() {
        assertEquals(2, E3DCChannelSpec.bitAt(RscpTag.WB_EXTERN_DATA, 2, 8).byteIndex());
        assertEquals(-1, E3DCChannelSpec.of(RscpTag.EMS_POWER_PV).byteIndex(),
                "a plain value must not pretend to be a byte array element");
        assertEquals(-1, E3DCChannelSpec.bit(RscpTag.EMS_STATUS, 1).byteIndex());
    }

    @Test
    void containersAreCollectedExactlyOnceForThePollCycle() {
        E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();
        List<Integer> containers = registry.containers();

        assertEquals(new HashSet<>(containers).size(), containers.size(), "containers must be distinct");
        assertTrue(containers.contains(RscpTag.EMS_GET_POWER_SETTINGS),
                "the power settings container must be polled for the nested channels");
    }

    @Test
    void theWallboxRegistryPollsTheWallboxContainer() {
        List<Integer> containers = E3DCChannelSpec.wallbox().containers();

        assertTrue(containers.contains(RscpTag.WB_DATA), "wallbox values live in the WB_DATA container");
    }

    /**
     * The charging mode is a value of its own on this interface, which is why the channel must not be derived
     * from a status bit: that byte carries another meaning and reported the opposite of the configured mode.
     */
    @Test
    void theSunModeChannelReadsTheValueTheWallboxReports() {
        E3DCChannelSpec spec = E3DCChannelSpec.wallbox().spec(E3DCBindingConstants.GROUP_WALLBOX_STATUS_CONTROL,
                "sun-mode");

        assertNotNull(spec);
        assertEquals(RscpTag.WB_DATA, spec.container());
        assertEquals(RscpTag.WB_SUN_MODE_ACTIVE, spec.tag());
        assertFalse(spec.isBitField(), "the charging mode is not a bit of a status byte");
        assertEquals(RscpTag.WB_REQ_SUN_MODE_ACTIVE, RscpTag.pollTagFor(RscpTag.WB_SUN_MODE_ACTIVE));
    }

    @Test
    void thePowerMeterRegistryPollsTheMeterContainer() {
        List<Integer> containers = E3DCChannelSpec.powerMeter().containers();

        assertTrue(containers.contains(RscpTag.PM_DATA), "meter values live in the PM_DATA container");
    }

    @Test
    void anUnknownGroupYieldsNoChannelsRatherThanFailing() {
        E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();

        assertTrue(registry.channels("no-such-group").isEmpty());
        assertNull(registry.spec("no-such-group", "no-such-channel"));
    }

    @Test
    void thePowerPlantExposesTheDocumentedGroups() {
        Set<String> groups = E3DCChannelSpec.powerPlant().groups().keySet();

        assertEquals(Set.of(E3DCBindingConstants.GROUP_POWER, E3DCBindingConstants.GROUP_BATTERY,
                E3DCBindingConstants.GROUP_GRID, E3DCBindingConstants.GROUP_INVERTER,
                E3DCBindingConstants.GROUP_HISTORY, E3DCBindingConstants.GROUP_EMERGENCY,
                E3DCBindingConstants.GROUP_SYSTEM, E3DCBindingConstants.GROUP_BATTERY_CONTROL), groups);
    }

    /**
     * The reserve values arrive in the container the device answers with, which is not the container the
     * request is sent with. The request names the data points to read, so its container is not the one the
     * channels look their values up in.
     */
    @Test
    void theReserveChannelsReadTheReserveContainer() {
        E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();
        Map<String, Integer> values = Map.of("percent", RscpTag.SE_PARAM_EP_RESERVE, "energy",
                RscpTag.SE_PARAM_EP_RESERVE_W, "energy-max", RscpTag.SE_PARAM_EP_RESERVE_MAX_W, "last-soc",
                RscpTag.SE_PARAM_LAST_SOC);
        for (Map.Entry<String, Integer> value : values.entrySet()) {
            E3DCChannelSpec spec = registry.spec(E3DCBindingConstants.GROUP_EMERGENCY, value.getKey());
            assertNotNull(spec, value.getKey() + " is missing");
            assertEquals(RscpTag.SE_EP_RESERVE, spec.container(), value.getKey() + " reads the wrong container");
            assertEquals(value.getValue(), spec.tag());
            assertEquals(0, spec.nestedTag(), "the reserve values are not double nested");
        }
        assertNotEquals(RscpTag.SE_REQ_EP_RESERVE, RscpTag.SE_EP_RESERVE, "request and answer are two tags");
    }

    /**
     * The reserve can be written as a share or as an amount of energy, and the interface declares both
     * as floating point values.
     */
    @Test
    void theReserveIsWritableAsAShareAndAsEnergy() {
        E3DCCommandRegistry commands = E3DCCommandSpec.powerPlant();
        E3DCCommandSpec percent = commands.spec(E3DCBindingConstants.GROUP_EMERGENCY, "percent");
        E3DCCommandSpec energy = commands.spec(E3DCBindingConstants.GROUP_EMERGENCY, "energy");

        assertNotNull(percent);
        assertNotNull(energy);
        assertEquals(RscpTag.SE_REQ_SET_EP_RESERVE, percent.container());
        assertEquals(RscpTag.SE_PARAM_EP_RESERVE, percent.tag());
        assertEquals(RscpType.FLOAT32, percent.type(), "the reserve percentage is a float in the interface");
        assertEquals(RscpTag.SE_PARAM_EP_RESERVE_W, energy.tag());
        assertEquals(RscpType.FLOAT32, energy.type());
    }

    /**
     * Every history period is requested by its own container and reports its values in a sum container,
     * so every history channel has to name both and the energy counters have to be scaled to kWh.
     */
    @Test
    void theHistoryGroupCoversEveryPeriod() {
        E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();
        Map<String, Integer> periods = Map.of("day", RscpTag.DB_HISTORY_DATA_DAY, "week", RscpTag.DB_HISTORY_DATA_WEEK,
                "month", RscpTag.DB_HISTORY_DATA_MONTH, "year", RscpTag.DB_HISTORY_DATA_YEAR);
        List<String> wrong = new ArrayList<>();
        for (Map.Entry<String, Integer> period : periods.entrySet()) {
            for (String value : List.of("solar", "house", "grid-in", "grid-out", "battery-in", "battery-out", "autarky",
                    "consumed-production")) {
                E3DCChannelSpec spec = registry.spec(E3DCBindingConstants.GROUP_HISTORY, period.getKey() + "-" + value);
                if (spec == null) {
                    wrong.add(period.getKey() + "-" + value + " is missing");
                    continue;
                }
                if (spec.container() != period.getValue()) {
                    wrong.add(period.getKey() + "-" + value + " reads the wrong period container");
                }
                if (spec.nestedTag() != RscpTag.DB_SUM_CONTAINER) {
                    wrong.add(period.getKey() + "-" + value + " does not read the sum container");
                }
                boolean isShare = "autarky".equals(value) || "consumed-production".equals(value);
                if (isShare && spec.divisor() != 1) {
                    wrong.add(period.getKey() + "-" + value + " is a share and must not be scaled");
                }
                if (!isShare && spec.divisor() != 1000) {
                    wrong.add(period.getKey() + "-" + value + " is a counter and must be scaled to kWh");
                }
            }
        }
        assertTrue(wrong.isEmpty(), "the history group is incomplete or inconsistent: " + wrong);
        assertEquals(32, registry.channels(E3DCBindingConstants.GROUP_HISTORY).size(),
                "four periods with eight values each");
    }

    @Test
    void everyThingTypeHasAUniqueIdentifierInTheBindingNamespace() {
        Set<String> ids = new HashSet<>();
        for (ThingTypeUID uid : E3DCBindingConstants.SUPPORTED_THING_TYPES) {
            assertEquals(E3DCBindingConstants.BINDING_ID, uid.getBindingId());
            assertTrue(ids.add(uid.getId()), "duplicate thing type " + uid.getId());
        }
        assertEquals(6, ids.size(),
                "the binding defines a bridge, power plant, wallbox, power meter, battery and battery module");
    }

    @Test
    void theNestedBatteryLimitsAreReadFromTheSettingsContainer() {
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_BATTERY,
                "max-charge-power");

        assertNotNull(spec);
        assertEquals(RscpTag.EMS_GET_POWER_SETTINGS, spec.container());
        assertEquals(RscpTag.EMS_MAX_CHARGE_POWER, spec.tag(), "the settings container carries the res tag");
    }

    /**
     * The state of charge the channel reports comes from the battery itself, which is a data point of its
     * own. The value the history response carries under a similar name is payload of a container and
     * cannot be asked for, so it must not be used here.
     */
    @Test
    void theRsocChannelReadsTheBatteryValue() {
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_BATTERY, "rsoc");

        assertNotNull(spec);
        assertEquals(RscpTag.BAT_RSOC, spec.tag());
        assertEquals(0, spec.container(), "the battery value is a root data point");
        assertEquals(RscpTag.BAT_REQ_RSOC, RscpTag.pollTagFor(RscpTag.BAT_RSOC));
        assertNotEquals(RscpTag.DB_BAT_CHARGE_LEVEL, spec.tag(),
                "DB_BAT_CHARGE_LEVEL is payload of the history container, not a data point");
    }

    /**
     * The energy of the manual charge sits inside the container the device answers with; the container itself
     * carries no value, so naming it as the tag would leave the channel without a state.
     */
    @Test
    void theEnergyCountersOfTheMeterAndTheWallboxAreScaledFromWhToKWh() {
        for (String channel : List.of("energy-l1", "energy-l2", "energy-l3")) {
            E3DCChannelSpec spec = E3DCChannelSpec.powerMeter().spec(E3DCBindingConstants.GROUP_METER, channel);
            assertNotNull(spec);
            assertEquals(1000, spec.divisor(), "the device reports " + channel + " in Wh, the channel shows kWh");
        }
        for (String channel : List.of("total", "solar")) {
            E3DCChannelSpec spec = E3DCChannelSpec.wallbox().spec(E3DCBindingConstants.GROUP_WALLBOX_COUNTERS, channel);
            assertNotNull(spec);
            assertEquals(1000, spec.divisor(), "the device reports " + channel + " in Wh, the channel shows kWh");
        }
        for (String channel : List.of("energy-l1", "energy-l2", "energy-l3")) {
            E3DCChannelSpec spec = E3DCChannelSpec.wallbox().spec(E3DCBindingConstants.GROUP_WALLBOX_SESSION, channel);
            assertNotNull(spec);
            assertEquals(1000, spec.divisor(), "the device reports the last charge " + channel + " in Wh");
        }
    }

    @Test
    void theManualChargeChannelReadsTheEnergyCounterInsideItsContainer() {
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL,
                "manual-charge-energy");

        assertNotNull(spec);
        assertEquals(RscpTag.EMS_GET_MANUAL_CHARGE, spec.container());
        assertEquals(RscpTag.EMS_MANUAL_CHARGE_ENERGY_COUNTER, spec.tag());
        assertEquals(1000, spec.divisor(), "the device reports this counter in Wh");
    }

    @Test
    void requestTagsAreDerivedForThePowersTheChannelsName() {
        assertEquals(RscpTag.EMS_REQ_POWER_PV, RscpTag.requestFor(RscpTag.EMS_POWER_PV));
        assertEquals(RscpTag.EMS_REQ_POWER_BAT, RscpTag.requestFor(RscpTag.EMS_POWER_BAT));
        assertEquals(RscpTag.EMS_REQ_POWER_HOME, RscpTag.requestFor(RscpTag.EMS_POWER_HOME));
        assertEquals(RscpTag.EMS_REQ_POWER_GRID, RscpTag.requestFor(RscpTag.EMS_POWER_GRID));
        assertEquals(RscpTag.EMS_REQ_BAT_SOC, RscpTag.requestFor(RscpTag.EMS_BAT_SOC));
    }

    @Test
    void requestTagsAreDerivedForContainers() {
        // a container is requested by its request tag, the enclosed values arrive with the container
        assertEquals(RscpTag.EMS_REQ_GET_POWER_SETTINGS, RscpTag.requestFor(RscpTag.EMS_GET_POWER_SETTINGS));
        assertEquals(RscpTag.PM_REQ_DATA, RscpTag.requestFor(RscpTag.PM_DATA));
        assertEquals(RscpTag.WB_REQ_DATA, RscpTag.requestFor(RscpTag.WB_DATA));
        assertEquals(RscpTag.PVI_REQ_DATA, RscpTag.requestFor(RscpTag.PVI_DATA));
    }

    @Test
    void aRequestTagIsRecognisedAsNotBeingAResponseTag() {
        // a request tag does not carry the response marker, so there is no request tag to derive
        assertEquals(0, RscpTag.requestFor(RscpTag.EMS_REQ_POWER_PV));
        assertEquals(0, RscpTag.requestFor(RscpTag.EMS_REQ_GET_POWER_SETTINGS));
    }

    /**
     * Guards the defect where channel specs named response tags but the request carried them verbatim,
     * so the device answered every data point with an unknown tag error.
     *
     * <p>
     * Only root tags and containers appear in a request: a value inside a container is not asked for
     * individually, because requesting the container returns its members.
     */
    @Test
    void everyPolledTagCanBeAskedFor() {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                E3DCChannelSpec spec = channel.getValue();
                if (spec.container() == 0 && !isPollable(spec.tag())) {
                    missing.add(group.getKey() + "#" + channel.getKey() + " -> " + RscpTag.nameOf(spec.tag()));
                }
                if (spec.container() != 0 && !isPollable(spec.container())) {
                    missing.add(group.getKey() + "#" + channel.getKey() + " container -> "
                            + RscpTag.nameOf(spec.container()));
                }
            }
        }
        assertTrue(missing.isEmpty(), "these tags cannot be asked for: " + missing);
    }

    /**
     * A tag can be asked for when it is the request variant itself, as the emergency power reserve is,
     * or when the request variant can be derived from the response tag.
     */
    private static boolean isPollable(int tag) {
        return RscpTag.requestFor(tag) != 0 || RscpTag.isRequestTag(tag);
    }

    /**
     * Guards the defect where a channel named a value that only exists inside a history response as if it
     * were a data point of its own.
     *
     * <p>
     * The interface declares an id for every data point a device can be asked for, and a value without one
     * is payload of a container. Asking for it anyway, because the derivation rule happily invents an id,
     * makes the device answer with an unknown tag error. That is indistinguishable from a device that does
     * not implement the data point, so the channel goes unnoticed and stays empty.
     *
     * <p>
     * The extracted tag table is the reference for which ids exist, so the poll set is checked against it
     * rather than against the derivation rule.
     */
    @Test
    void everyPolledIdIsDeclaredByTheInterface() throws IOException {
        Set<Integer> declared = declaredTagIds();
        List<String> fabricated = new ArrayList<>();
        for (Map.Entry<String, Map<String, E3DCChannelSpec>> group : allGroups()) {
            for (Map.Entry<String, E3DCChannelSpec> channel : group.getValue().entrySet()) {
                E3DCChannelSpec spec = channel.getValue();
                // a value inside a container is not asked for on its own, its container is
                int requested = spec.container() != 0 ? RscpTag.pollTagFor(spec.container())
                        : RscpTag.pollTagFor(spec.tag());
                if (!declared.contains(requested)) {
                    fabricated
                            .add(group.getKey() + "#" + channel.getKey() + " -> " + String.format("0x%08X", requested));
                }
            }
        }
        assertTrue(fabricated.isEmpty(),
                "these channels are polled with an id the interface does not declare: " + fabricated);
    }

    /** The ids of the extracted RSCP tag table kept in the test resources as the interface reference. */
    private static Set<Integer> declaredTagIds() throws IOException {
        Set<Integer> ids = new HashSet<>();
        try (InputStream in = E3DCChannelSpecTest.class.getResourceAsStream("/rscp_tags.tsv")) {
            assertNotNull(in, "test resource rscp_tags.tsv not found");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split("\t");
                    if (columns.length == 2 && columns[0].startsWith("TAG_")) {
                        String hex = columns[1].trim();
                        ids.add((int) Long.parseLong(hex.startsWith("0x") ? hex.substring(2) : hex, 16));
                    }
                }
            }
        }
        assertFalse(ids.isEmpty(), "no tags were read from rscp_tags.tsv");
        return ids;
    }

    @Test
    void aTagThatIsAlreadyARequestIsSentAsItIs() {
        // the reserve uses one id for the request and the response, so no variant is derived
        assertEquals(RscpTag.SE_REQ_EP_RESERVE, RscpTag.pollTagFor(RscpTag.SE_REQ_EP_RESERVE));
        assertTrue(RscpTag.isRequestTag(RscpTag.SE_REQ_EP_RESERVE));
        // a response tag is still translated
        assertEquals(RscpTag.EMS_REQ_BAT_SOC, RscpTag.pollTagFor(RscpTag.EMS_BAT_SOC));
        assertFalse(RscpTag.isRequestTag(RscpTag.EMS_BAT_SOC));
    }

    /**
     * Where the catalogue declares both variants of a data point, the derived request tag must be the
     * declared one. This ties the arithmetic rule to the tag table instead of trusting either alone.
     */
    @Test
    void everyDeclaredResponseTagDerivesItsDeclaredRequestTag() {
        int[][] pairs = { //
                { RscpTag.EMS_POWER_PV, RscpTag.EMS_REQ_POWER_PV }, //
                { RscpTag.EMS_POWER_BAT, RscpTag.EMS_REQ_POWER_BAT }, //
                { RscpTag.EMS_POWER_HOME, RscpTag.EMS_REQ_POWER_HOME }, //
                { RscpTag.EMS_POWER_GRID, RscpTag.EMS_REQ_POWER_GRID }, //
                { RscpTag.EMS_POWER_ADD, RscpTag.EMS_REQ_POWER_ADD }, //
                { RscpTag.EMS_BAT_SOC, RscpTag.EMS_REQ_BAT_SOC }, //
                { RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_REQ_GET_POWER_SETTINGS }, //
                { RscpTag.EMS_MODE, RscpTag.EMS_REQ_MODE }, //
                { RscpTag.EMS_STATUS, RscpTag.EMS_REQ_STATUS }, //
                { RscpTag.EMS_INSTALLED_PEAK_POWER, RscpTag.EMS_REQ_INSTALLED_PEAK_POWER }, //
                { RscpTag.EMS_EMERGENCY_POWER_STATUS, RscpTag.EMS_REQ_EMERGENCY_POWER_STATUS }, //
                { RscpTag.EMS_REMAINING_BAT_CHARGE_POWER, RscpTag.EMS_REQ_REMAINING_BAT_CHARGE_POWER }, //
                { RscpTag.EMS_REMAINING_BAT_DISCHARGE_POWER, RscpTag.EMS_REQ_REMAINING_BAT_DISCHARGE_POWER }, //
                { RscpTag.INFO_SERIAL_NUMBER, RscpTag.INFO_REQ_SERIAL_NUMBER }, //
                { RscpTag.INFO_PRODUCTION_DATE, RscpTag.INFO_REQ_PRODUCTION_DATE }, //
                { RscpTag.INFO_TIME_ZONE, RscpTag.INFO_REQ_TIME_ZONE }, //
                { RscpTag.INFO_SW_RELEASE, RscpTag.INFO_REQ_SW_RELEASE }, //
                { RscpTag.PVI_DATA, RscpTag.PVI_REQ_DATA }, //
                { RscpTag.PVI_INVERTER_COUNT, RscpTag.PVI_REQ_INVERTER_COUNT }, //
                { RscpTag.PVI_ON_GRID, RscpTag.PVI_REQ_ON_GRID }, //
                { RscpTag.PVI_USED_STRING_COUNT, RscpTag.PVI_REQ_USED_STRING_COUNT }, //
                { RscpTag.PM_DATA, RscpTag.PM_REQ_DATA }, //
                { RscpTag.WB_DATA, RscpTag.WB_REQ_DATA } };
        for (int[] pair : pairs) {
            assertEquals(pair[1], RscpTag.requestFor(pair[0]),
                    "wrong request tag derived for " + RscpTag.nameOf(pair[0]));
        }
    }

    @Test
    void theResponseMarkerIsTheDifferenceBetweenTheTwoTagVariants() {
        assertEquals(RscpTag.RESPONSE_MARKER, RscpTag.EMS_POWER_PV & ~RscpTag.EMS_REQ_POWER_PV);
        assertEquals(RscpTag.RESPONSE_MARKER, RscpTag.PM_DATA & ~RscpTag.PM_REQ_DATA);
        assertEquals(RscpTag.RESPONSE_MARKER, RscpTag.EMS_GET_POWER_SETTINGS & ~RscpTag.EMS_REQ_GET_POWER_SETTINGS);
    }

    /**
     * Most pairs follow the marker rule, but the number of available batteries is declared with two ids
     * that do not differ by the marker alone. Sending the derived id would ask for a tag the device does
     * not know, so the declared request tag has to win.
     */
    @Test
    void aPairThatDoesNotFollowTheMarkerRuleUsesItsDeclaredRequestTag() {
        assertNotEquals(RscpTag.RESPONSE_MARKER, RscpTag.BAT_AVAILABLE_BATTERIES & ~RscpTag.BAT_REQ_AVAILABLE_BATTERIES,
                "this pair is the documented exception");
        assertEquals(RscpTag.BAT_REQ_AVAILABLE_BATTERIES, RscpTag.requestFor(RscpTag.BAT_AVAILABLE_BATTERIES));
        assertNotEquals(RscpTag.BAT_AVAILABLE_BATTERIES & ~RscpTag.RESPONSE_MARKER,
                RscpTag.requestFor(RscpTag.BAT_AVAILABLE_BATTERIES));
    }

    @Test
    void autarkyComesFromTheHistorySumContainer() {
        // DB_AUTARKY is not a root value: the history response wraps it in a sum container
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_POWER, "autarky");

        assertNotNull(spec);
        assertEquals(RscpTag.DB_AUTARKY, spec.tag(), "autarky is the history value, not the solar counter");
        assertEquals(RscpTag.DB_HISTORY_DATA_DAY, spec.container(), "autarky arrives in the daily history response");
        assertEquals(RscpTag.DB_SUM_CONTAINER, spec.nestedTag(), "the value sits in the sum container");
        assertTrue(spec.divisor() == 1, "autarky is reported in percent and must not be scaled");
    }

    @Test
    void theHistoryPathIsContainerThenSumContainerThenValue() {
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_POWER, "autarky");

        assertNotNull(spec);
        assertEquals(List.of(RscpTag.DB_HISTORY_DATA_DAY, RscpTag.DB_SUM_CONTAINER, RscpTag.DB_AUTARKY),
                spec.valuePath(), "the value is three levels deep");
    }

    @Test
    void aRootValueHasASingleElementPath() {
        E3DCChannelSpec spec = E3DCChannelSpec.of(RscpTag.EMS_POWER_PV);

        assertEquals(List.of(RscpTag.EMS_POWER_PV), spec.valuePath());
    }

    @Test
    void aContainerValueHasNoSumContainerInItsPath() {
        E3DCChannelSpec spec = E3DCChannelSpec.in(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_MAX_CHARGE_POWER);

        assertEquals(List.of(RscpTag.EMS_GET_POWER_SETTINGS, RscpTag.EMS_MAX_CHARGE_POWER), spec.valuePath());
        assertEquals(0, spec.nestedTag());
    }

    /**
     * The history container is answered only when the request states the period, so it must be asked
     * for with its own time parameters and not as a bare container.
     */
    @Test
    void historyContainersFollowTheRequestMarkerRule() {
        // the container is requested by the request variant, which the rule derives
        assertEquals(RscpTag.DB_REQ_HISTORY_DATA_DAY, RscpTag.requestFor(RscpTag.DB_HISTORY_DATA_DAY));
        assertEquals(RscpTag.DB_SUM_CONTAINER & ~RscpTag.RESPONSE_MARKER, RscpTag.requestFor(RscpTag.DB_SUM_CONTAINER),
                "the sum container follows the marker rule as well");
        assertNotEquals(RscpTag.DB_REQ_HISTORY_DATA_DAY, RscpTag.DB_HISTORY_DATA_DAY);
        assertEquals(RscpTag.RESPONSE_MARKER, RscpTag.DB_HISTORY_DATA_DAY & ~RscpTag.DB_REQ_HISTORY_DATA_DAY);
    }

    @Test
    void selfConsumptionIsNotTheHouseConsumptionCounter() {
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_POWER, "self-consumption");

        assertNotNull(spec);
        assertEquals(RscpTag.DB_CONSUMED_PRODUCTION, spec.tag());
        assertTrue(spec.divisor() == 1, "consumed production is reported in percent and must not be scaled");
        // DB_DC_POWER and DB_CONSUMPTION are the solar and house energy counters of the same container
        assertNotEquals(RscpTag.DB_DC_POWER, spec.tag());
        assertNotEquals(RscpTag.DB_CONSUMPTION, spec.tag());
    }

    /**
     * A battery cell module reports its values in its own container, which identifies itself by index.
     * Every module channel reads that container, so the handler can pick the one belonging to the
     * configured module.
     */
    @Test
    void theBatteryModuleChannelsReadTheModuleContainer() {
        E3DCChannelRegistry registry = E3DCChannelSpec.batteryModule();

        assertEquals(Set.of(E3DCBindingConstants.GROUP_MODULE), registry.groups().keySet());
        Map<String, E3DCChannelSpec> channels = registry.channels(E3DCBindingConstants.GROUP_MODULE);
        assertEquals(32, channels.size(), "the module reports 32 values");
        for (Map.Entry<String, E3DCChannelSpec> channel : channels.entrySet()) {
            E3DCChannelSpec spec = channel.getValue();
            assertEquals(RscpTag.BAT_DCB_INFO, spec.container(), channel.getKey() + " must read the module container");
            assertTrue(spec.tag() != 0, channel.getKey() + " has no tag");
            assertEquals(0, spec.nestedTag(), channel.getKey() + " is not double nested");
        }
        // the values have to be reachable inside the container the battery data carries
        assertTrue(channels.containsKey("soc"));
        assertTrue(channels.containsKey("voltage"));
        assertTrue(channels.containsKey("cycles"));
    }

    /**
     * The photovoltaic trackers and temperature sensors are created at runtime, so the registry of the thing
     * description holds none of them; E3DCInverterChannelsTest covers the channels that are added.
     */
    @Test
    void theTrackerChannelsAreNotPartOfTheRegistry() {
        E3DCChannelRegistry registry = E3DCChannelSpec.powerPlant();

        assertNull(registry.spec(E3DCBindingConstants.GROUP_INVERTER, "tracker-1-power"));
        assertNull(registry.spec(E3DCBindingConstants.GROUP_INVERTER, "temperature-1"));
    }

    /**
     * A channel that reads the first occurrence must not pretend to be indexed, otherwise a value that
     * occurs once would stay without a state.
     */
    @Test
    void aChannelWithoutRepeatedValuesIsNotIndexed() {
        E3DCChannelSpec spec = E3DCChannelSpec.powerPlant().spec(E3DCBindingConstants.GROUP_INVERTER, "ac-frequency");

        assertNotNull(spec);
        assertEquals(E3DCChannelSpec.NO_INDEX, spec.valueIndex());
    }

    /**
     * The module container is addressed by its own index tag. The battery container is named as a request
     * already, so it is sent unchanged, while the module values are response tags and follow the marker
     * rule.
     */
    @Test
    void theModuleContainerIsIndexedAndTheBatteryContainerIsRequestable() {
        assertEquals(0x03800100, RscpTag.BAT_DCB_INDEX, "the module container identifies itself by this tag");
        assertTrue(RscpTag.isRequestTag(RscpTag.BAT_REQ_DATA), "the battery container is named as a request");
        assertEquals(RscpTag.BAT_REQ_DATA, RscpTag.pollTagFor(RscpTag.BAT_REQ_DATA));
        // the module values are response tags inside the container, so they follow the marker rule
        assertEquals(RscpTag.BAT_DCB_SOC & ~RscpTag.RESPONSE_MARKER, RscpTag.requestFor(RscpTag.BAT_DCB_SOC));
        assertEquals(RscpTag.BAT_DCB_INFO & ~RscpTag.RESPONSE_MARKER, RscpTag.requestFor(RscpTag.BAT_DCB_INFO));
    }

    /** The phases in use arrive as a bit mask, but the channel reports how many phases that is. */
    @Test
    void theActivePhasesAreReportedAsTheirCount() {
        E3DCChannelSpec spec = E3DCChannelSpec.bitCountIn(RscpTag.PM_DATA, RscpTag.PM_ACTIVE_PHASES);

        assertEquals(3, spec.scale(7));
        assertEquals(2, spec.scale(5));
        assertEquals(1, spec.scale(4));
        assertEquals(0, spec.scale(0));
    }
}
