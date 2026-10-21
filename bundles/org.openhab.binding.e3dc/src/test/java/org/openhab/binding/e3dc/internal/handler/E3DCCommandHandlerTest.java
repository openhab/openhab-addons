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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCCommandRegistry;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

/**
 * Tests that commands become the requests the device interface expects.
 *
 * <p>
 * The power settings are the delicate part: switching the power limits flag off resets the enclosed
 * limits, so a write has to carry every parameter of that container. These tests pin that behaviour.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCCommandHandlerTest {

    private static final int SETTINGS_REQUEST = RscpTag.EMS_REQ_SET_POWER_SETTINGS;

    /** Builds a settings response the way the device reports it after a poll. */
    private static RscpValue settingsResponse(int powerLimits, int maxCharge, int maxDischarge, int startPower,
            int weather) {
        return settingsResponse(RscpTag.EMS_GET_POWER_SETTINGS, powerLimits, maxCharge, maxDischarge, startPower,
                weather);
    }

    /** Builds a settings response with the given container, which the device uses for a poll and for a write. */
    private static RscpValue settingsResponse(int container, int powerLimits, int maxCharge, int maxDischarge,
            int startPower, int weather) {
        return RscpValue.container(0, List.of(RscpValue.container(container,
                List.of(RscpValue.of(RscpTag.EMS_POWER_LIMITS_USED, RscpType.UCHAR8, powerLimits),
                        RscpValue.of(RscpTag.EMS_MAX_CHARGE_POWER, RscpType.UINT32, maxCharge),
                        RscpValue.of(RscpTag.EMS_MAX_DISCHARGE_POWER, RscpType.UINT32, maxDischarge),
                        RscpValue.of(RscpTag.EMS_DISCHARGE_START_POWER, RscpType.UINT32, startPower),
                        RscpValue.of(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED, RscpType.UCHAR8, weather)))));
    }

    private static E3DCCommandHandler handlerWithSettings() {
        E3DCCommandHandler handler = new E3DCCommandHandler();
        handler.updateFromResponse(settingsResponse(1, 9000, 8000, 100, 1));
        return handler;
    }

    private static E3DCCommandSpec spec(String group, String channel) {
        E3DCCommandSpec spec = E3DCCommandSpec.powerPlant().spec(group, channel);
        assertNotNull(spec, "the channel " + group + "#" + channel + " must be writable");
        return spec;
    }

    /**
     * Builds a request that must be constructible, turning nullness into a test failure.
     *
     * <p>
     * A command is one top level element of the frame, so the list it comes in has to hold exactly one.
     */
    private static RscpValue request(E3DCCommandHandler handler, E3DCCommandSpec spec,
            org.openhab.core.types.Command command) {
        List<RscpValue> built = handler.buildRequest(spec, command, 0);
        assertNotNull(built, "the command must be representable: " + command);
        assertEquals(1, built.size(), "a command is sent as one element");
        return built.get(0);
    }

    /** The settings container of a built request. */
    private static RscpValue settingsOf(RscpValue built) {
        RscpValue settings = built.find(SETTINGS_REQUEST);
        assertNotNull(settings, "the write must target the settings container");
        return settings;
    }

    /** Reads a number that must be present. */
    private static int numberAt(RscpValue tree, int tag) {
        Number number = tree.findNumber(tag);
        assertNotNull(number, "no numeric value for " + RscpTag.nameOf(tag));
        return number.intValue();
    }

    /** Reads a number that must be present, without narrowing it to an int. */
    private static long longAt(RscpValue tree, int tag) {
        Number number = tree.findNumber(tag);
        assertNotNull(number, "no numeric value for " + RscpTag.nameOf(tag));
        return number.longValue();
    }

    // ------------------------------------------------------------------ request shapes

    @Test
    void aPowerLimitWriteCarriesEveryParameterOfTheSettingsContainer() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power");

        RscpValue request = request(handler, spec, new DecimalType(5000));
        assertNotNull(request);

        RscpValue settings = request.find(SETTINGS_REQUEST);
        assertNotNull(settings, "the write must target the settings container");
        for (int tag : E3DCCommandSpec.POWER_SETTINGS_PARAMETERS) {
            assertNotNull(settings.find(tag),
                    "the write must carry " + RscpTag.nameOf(tag) + " so the device does not reset it");
        }
    }

    @Test
    void aPowerLimitWriteAppliesTheNewValueAndKeepsTheOthers() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power");

        RscpValue settings = settingsOf(request(handler, spec, new DecimalType(5000)));

        assertEquals(5000, numberAt(settings, RscpTag.EMS_MAX_CHARGE_POWER), "the written value");
        assertEquals(8000, numberAt(settings, RscpTag.EMS_MAX_DISCHARGE_POWER), "kept from the poll");
        assertEquals(100, numberAt(settings, RscpTag.EMS_DISCHARGE_START_POWER), "kept from the poll");
        assertEquals(1, numberAt(settings, RscpTag.EMS_POWER_LIMITS_USED), "kept from the poll");
        assertEquals(1, numberAt(settings, RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED), "kept from the poll");
    }

    @Test
    void parametersThatWereNeverReadAreNotWrittenAsZero() {
        // a device that has not answered the settings request yet
        E3DCCommandHandler handler = new E3DCCommandHandler();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "discharge-start-power");

        RscpValue settings = settingsOf(request(handler, spec, new DecimalType(250)));

        assertEquals(List.of(RscpTag.EMS_DISCHARGE_START_POWER),
                settings.getChildren().stream().map(RscpValue::getTag).toList(),
                "only the written parameter may be sent, never an unknown limit as zero");
    }

    @Test
    void switchingThePowerLimitsOffIsSentAsABoolean() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL, "power-limits-used");

        RscpValue settings = settingsOf(request(handler, spec, OnOffType.OFF));

        RscpValue limits = settings.find(RscpTag.EMS_POWER_LIMITS_USED);
        assertNotNull(limits);
        assertEquals(RscpType.BOOL, limits.getType());
        assertEquals(Boolean.FALSE, limits.getValue());
    }

    @Test
    void theWeatherFlagIsSentInsideTheSettingsContainer() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL, "weather-regulated-charge");

        RscpValue settings = settingsOf(request(handler, spec, OnOffType.ON));

        RscpValue flag = settings.find(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED);
        assertNotNull(flag);
        assertEquals(RscpType.BOOL, flag.getType(), "the device reports this parameter as a boolean");
        assertEquals(Boolean.TRUE, flag.getValue());
    }

    // ------------------------------------------------------------------ rejected commands

    @Test
    void aTextCommandForANumericChannelIsRejected() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power");

        assertNull(handler.buildRequest(spec, new StringType("lots"), 0));
    }

    @Test
    void aTextCommandForASwitchChannelIsAcceptedWhenItIsRecognisable() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL, "weather-regulated-charge");

        assertNotNull(handler.buildRequest(spec, new StringType("on"), 0));
        assertNotNull(handler.buildRequest(spec, new StringType("false"), 0));
        assertNull(handler.buildRequest(spec, new StringType("maybe"), 0));
    }

    @Test
    void aNegativeValueIsRejectedBecauseTheDeviceExpectsUnsignedValues() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power");

        assertNull(handler.buildRequest(spec, new DecimalType(-1), 0));
    }

    @Test
    void aFractionalValueIsRoundedToTheNearestWholeWatt() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "max-discharge-power");

        RscpValue settings = settingsOf(request(handler, spec, new DecimalType(4999.6)));
        assertEquals(5000, numberAt(settings, RscpTag.EMS_MAX_DISCHARGE_POWER));
    }

    @Test
    void aValueBeyondTheUnsignedRangeIsRejected() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec spec = spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power");

        assertNull(handler.buildRequest(spec, new DecimalType(4294967296L), 0),
                "four bytes cannot carry a larger value, so it must not be sent as a truncated one");
        RscpValue settings = settingsOf(request(handler, spec, new DecimalType(4294967295L)));
        assertEquals(4294967295L, longAt(settings, RscpTag.EMS_MAX_CHARGE_POWER));
    }

    /**
     * The share channels are written in percent, and a dimensionless quantity carries the same share as a
     * percentage: 0.5 of one is 50 %.
     */
    @Test
    void aDimensionlessQuantityIsWrittenInPercent() {
        E3DCCommandHandler handler = handlerWithSettings();
        E3DCCommandSpec percentage = spec(E3DCBindingConstants.GROUP_EMERGENCY, "percent");

        assertEquals(50, numberAt(request(handler, percentage, new QuantityType<>(0.5, Units.ONE)),
                RscpTag.SE_PARAM_EP_RESERVE));
        assertEquals(50, numberAt(request(handler, percentage, new QuantityType<>(50, Units.PERCENT)),
                RscpTag.SE_PARAM_EP_RESERVE));
        assertEquals(50,
                numberAt(request(handler, spec(E3DCBindingConstants.GROUP_BATTERY, "wallbox-discharge-until"),
                        new QuantityType<>(0.5, Units.ONE)), RscpTag.EMS_REQ_SET_WB_DISCHARGE_BAT_UNTIL),
                "the byte channel rounds the same share to the whole percent");
    }

    // ------------------------------------------------------------------ acknowledged writes

    /** The device answers a write with its own settings container, which holds the acknowledged values. */
    @Test
    void theSettingsOfAWriteAcknowledgementAreTakenOver() {
        E3DCCommandHandler handler = handlerWithSettings();
        handler.updateFromResponse(settingsResponse(RscpTag.EMS_SET_POWER_SETTINGS, 1, 5000, 6000, 100, 1));

        RscpValue settings = settingsOf(request(handler,
                spec(E3DCBindingConstants.GROUP_BATTERY, "discharge-start-power"), new DecimalType(250)));

        assertEquals(5000, numberAt(settings, RscpTag.EMS_MAX_CHARGE_POWER),
                "a following write must not send the limit that the acknowledged write replaced");
        assertEquals(6000, numberAt(settings, RscpTag.EMS_MAX_DISCHARGE_POWER));
    }

    /** An answer that carries no settings container must not leave the snapshot of the write behind. */
    @Test
    void anAcknowledgedWriteIsKeptForTheNextCommand() {
        E3DCCommandHandler handler = handlerWithSettings();
        List<RscpValue> written = handler.buildRequest(spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power"),
                new DecimalType(5000), 0);
        assertNotNull(written);
        handler.rememberAcknowledged(written);

        RscpValue settings = settingsOf(request(handler,
                spec(E3DCBindingConstants.GROUP_BATTERY, "max-discharge-power"), new DecimalType(6000)));

        assertEquals(5000, numberAt(settings, RscpTag.EMS_MAX_CHARGE_POWER));
        assertEquals(6000, numberAt(settings, RscpTag.EMS_MAX_DISCHARGE_POWER));
    }

    @Test
    void anErrorOfOneParameterIsNoSettingForTheNextWrite() {
        E3DCCommandHandler handler = new E3DCCommandHandler();
        handler.updateFromResponse(
                RscpValue.container(0,
                        List.of(RscpValue.container(RscpTag.EMS_GET_POWER_SETTINGS, List.of(
                                RscpValue.of(RscpTag.EMS_MAX_CHARGE_POWER, RscpType.UINT32, 9000),
                                RscpValue.of(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED, RscpType.ERROR, 6L))))));

        RscpValue settings = settingsOf(request(handler,
                spec(E3DCBindingConstants.GROUP_BATTERY, "max-discharge-power"), new DecimalType(6000)));

        assertNull(settings.find(RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED),
                "an error item cannot be encoded, so it must not be written back");
        assertEquals(9000, numberAt(settings, RscpTag.EMS_MAX_CHARGE_POWER));
    }

    // ------------------------------------------------------------------ registry

    @Test
    void everyWritableChannelTargetsARequestTag() {
        for (E3DCCommandSpec spec : E3DCCommandSpec.powerPlant().all()) {
            String name = RscpTag.nameOf(spec.container() != 0 ? spec.container() : spec.tag());
            assertFalse(name.startsWith("UNKNOWN_"), "a writable channel points at an unknown tag: " + name);
        }
    }

    @Test
    void theWritableChannelsCoverTheDocumentedControls() {
        E3DCCommandRegistry registry = E3DCCommandSpec.powerPlant();

        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_BATTERY, "max-charge-power"));
        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_BATTERY, "max-discharge-power"));
        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_BATTERY, "discharge-start-power"));
        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL, "power-limits-used"));
        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL, "weather-regulated-charge"));
        assertNull(registry.spec(E3DCBindingConstants.GROUP_SYSTEM, "mode"),
                "the mode is read only, see set-power-mode");
        assertNotNull(registry.spec(E3DCBindingConstants.GROUP_BATTERY_CONTROL, "set-manual-charge"));
    }

    @Test
    void aReadOnlyChannelIsNotDescribedAsWritable() {
        assertNull(E3DCCommandSpec.powerPlant().spec(E3DCBindingConstants.GROUP_POWER, "solar-power"));
        assertNull(E3DCCommandSpec.powerPlant().spec("no-such-group", "no-such-channel"));
    }

    @Test
    void theSettingsParameterOrderMatchesTheDeviceInterface() {
        assertArrayEquals(new int[] { RscpTag.EMS_POWER_LIMITS_USED, RscpTag.EMS_MAX_CHARGE_POWER,
                RscpTag.EMS_MAX_DISCHARGE_POWER, RscpTag.EMS_DISCHARGE_START_POWER,
                RscpTag.EMS_WEATHER_REGULATED_CHARGE_ENABLED }, E3DCCommandSpec.POWER_SETTINGS_PARAMETERS);
    }
}
