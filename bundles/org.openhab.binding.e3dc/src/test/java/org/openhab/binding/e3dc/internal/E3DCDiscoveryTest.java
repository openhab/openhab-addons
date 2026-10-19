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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests what the discovery reads from the answers of the device and which things it proposes.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCDiscoveryTest {

    private static final ThingUID BRIDGE = new ThingUID(new ThingTypeUID("e3dc", "bridge"), "device");

    private static RscpValue availableAsNumber(int count) {
        return RscpValue.container(0, List.of(RscpValue.uchar8(RscpTag.BAT_AVAILABLE_BATTERIES, count)));
    }

    private static RscpValue availableAsContainer(int... indexes) {
        List<RscpValue> children = new ArrayList<>();
        for (int index : indexes) {
            children.add(RscpValue.uchar8(RscpTag.BAT_INDEX, index));
        }
        return RscpValue.container(0, List.of(RscpValue.container(RscpTag.BAT_AVAILABLE_BATTERIES, children)));
    }

    /** The answer of a battery with the given modules, as the device sends it. */
    private static RscpValue batteryAnswer(int... modules) {
        List<RscpValue> data = new ArrayList<>();
        data.add(RscpValue.uchar8(RscpTag.BAT_INDEX, 0));
        for (int module : modules) {
            data.add(RscpValue.container(RscpTag.BAT_DCB_INFO,
                    List.of(RscpValue.uchar8(RscpTag.BAT_DCB_INDEX, module))));
        }
        return RscpValue.container(0, List.of(RscpValue.container(RscpTag.BAT_DATA, data)));
    }

    @Test
    void aNumberIsTakenAsTheNumberOfBatteries() {
        assertEquals(List.of(0), E3DCDiscovery.batteryCandidates(availableAsNumber(1)));
        assertEquals(List.of(0, 1), E3DCDiscovery.batteryCandidates(availableAsNumber(2)));
        assertEquals(List.of(), E3DCDiscovery.batteryCandidates(availableAsNumber(0)));
    }

    @Test
    void aListOfIndexesIsTakenAsItIs() {
        assertEquals(List.of(0, 2), E3DCDiscovery.batteryCandidates(availableAsContainer(2, 0)));
        assertEquals(List.of(0), E3DCDiscovery.batteryCandidates(availableAsContainer()),
                "an empty list falls back to the first battery");
    }

    @Test
    void batteryZeroIsTriedWhenTheDeviceSaysNothing() {
        assertEquals(List.of(0), E3DCDiscovery.batteryCandidates(null));
        assertEquals(List.of(0), E3DCDiscovery.batteryCandidates(RscpValue.container(0, List.of())));
    }

    @Test
    void theNumberOfBatteriesIsLimited() {
        assertEquals(E3DCDiscovery.MAX_BATTERIES, E3DCDiscovery.batteryCandidates(availableAsNumber(200)).size());
    }

    @Test
    void aBatteryIsOnlyProposedWhenTheDeviceAnswersWithValues() {
        assertTrue(E3DCDiscovery.isBatteryAnswered(batteryAnswer(0, 1)));

        RscpValue errors = RscpValue.container(0, List.of(RscpValue.container(RscpTag.BAT_DATA,
                List.of(RscpValue.of(RscpTag.BAT_REQ_RSOC, RscpType.ERROR, 6L)))));
        assertFalse(E3DCDiscovery.isBatteryAnswered(errors), "only errors means there is no such battery");
        assertFalse(E3DCDiscovery.isBatteryAnswered(RscpValue.container(0, List.of())),
                "no data container means there is no such battery");
    }

    @Test
    void theModulesAreListedOnceAndInOrder() {
        assertEquals(List.of(0, 1, 2), E3DCDiscovery.moduleIndexes(batteryAnswer(2, 0, 1, 1)));
        assertEquals(List.of(), E3DCDiscovery.moduleIndexes(batteryAnswer()));
    }

    @Test
    void aModuleWithoutIndexIsIgnored() {
        RscpValue answer = RscpValue.container(0, List.of(RscpValue.container(RscpTag.BAT_DATA, List.of(
                RscpValue.container(RscpTag.BAT_DCB_INFO, List.of(RscpValue.uchar8(RscpTag.BAT_DCB_COUNT, 4))),
                RscpValue.container(RscpTag.BAT_DCB_INFO, List.of(RscpValue.uchar8(RscpTag.BAT_DCB_INDEX, 1)))))));
        assertEquals(List.of(1), E3DCDiscovery.moduleIndexes(answer));
    }

    @Test
    void aBatteryIsProposedWithItsIndex() {
        DiscoveryResult result = E3DCDiscovery.battery(BRIDGE, 1);

        assertEquals("e3dc:battery:device:battery-1", result.getThingUID().getAsString());
        assertEquals(BRIDGE, result.getBridgeUID());
        assertEquals(1, result.getProperties().get("batteryIndex"));
        assertEquals("batteryIndex", result.getRepresentationProperty());
        assertEquals("E3/DC Battery 2", result.getLabel());
    }

    @Test
    void aModuleIsProposedWithItsIndex() {
        DiscoveryResult result = E3DCDiscovery.module(BRIDGE, 0);

        assertEquals("e3dc:battery-module:device:module-0", result.getThingUID().getAsString());
        assertEquals(BRIDGE, result.getBridgeUID());
        assertEquals(0, result.getProperties().get("moduleIndex"));
        assertEquals("moduleIndex", result.getRepresentationProperty());
        assertEquals("E3/DC Battery Module 1", result.getLabel());
    }

    /** The properties must carry the names of the configuration parameters, or the thing is created empty. */
    @Test
    void thePropertiesMatchTheConfigurationParameters() {
        assertNotNull(E3DCBatteryConfiguration.class.getFields()[0]);
        assertEquals("batteryIndex", E3DCBatteryConfiguration.class.getFields()[0].getName());
        assertEquals("moduleIndex", E3DCBatteryModuleConfiguration.class.getFields()[0].getName());
        assertEquals("batteryIndex", E3DCDiscovery.PROPERTY_BATTERY_INDEX);
        assertEquals("moduleIndex", E3DCDiscovery.PROPERTY_MODULE_INDEX);
    }

    private static RscpValue answer(int dataTag, int indexTag, RscpValue... values) {
        List<RscpValue> children = new ArrayList<>();
        children.add(RscpValue.uchar8(indexTag, 0));
        children.addAll(List.of(values));
        return RscpValue.container(0, List.of(RscpValue.container(dataTag, children)));
    }

    private static RscpValue error(int tag) {
        return RscpValue.of(tag, RscpType.ERROR, 6L);
    }

    @Test
    void theRepeatedIndexAloneIsNoData() {
        RscpValue indexOnly = answer(RscpTag.BAT_DATA, RscpTag.BAT_INDEX, error(RscpTag.BAT_RSOC));
        assertFalse(E3DCDiscovery.isBatteryAnswered(indexOnly));
        assertFalse(E3DCDiscovery.isBatteryAnswered(answer(RscpTag.BAT_DATA, RscpTag.BAT_INDEX)));
    }

    @Test
    void aMeterNeedsValuesAndAnActivePhase() {
        assertTrue(E3DCDiscovery.isMeterAnswered(answer(RscpTag.PM_DATA, RscpTag.PM_INDEX,
                RscpValue.int32(RscpTag.PM_POWER_L1, 120), RscpValue.uchar8(RscpTag.PM_ACTIVE_PHASES, 7))));
        assertTrue(
                E3DCDiscovery.isMeterAnswered(
                        answer(RscpTag.PM_DATA, RscpTag.PM_INDEX, RscpValue.int32(RscpTag.PM_POWER_L1, 120))),
                "without a report of the phases the values decide");
        assertFalse(E3DCDiscovery.isMeterAnswered(answer(RscpTag.PM_DATA, RscpTag.PM_INDEX,
                RscpValue.int32(RscpTag.PM_POWER_L1, 0), RscpValue.uchar8(RscpTag.PM_ACTIVE_PHASES, 0))));
        assertFalse(
                E3DCDiscovery.isMeterAnswered(answer(RscpTag.PM_DATA, RscpTag.PM_INDEX, error(RscpTag.PM_POWER_L1))));
        assertFalse(E3DCDiscovery.isMeterAnswered(RscpValue.container(0, List.of())));
    }

    @Test
    void aWallboxNeedsValues() {
        assertTrue(E3DCDiscovery.isWallboxAnswered(
                answer(RscpTag.WB_DATA, RscpTag.WB_INDEX, RscpValue.uchar8(RscpTag.WB_PM_ACTIVE_PHASES, 1))));
        assertFalse(E3DCDiscovery
                .isWallboxAnswered(answer(RscpTag.WB_DATA, RscpTag.WB_INDEX, error(RscpTag.WB_PM_ACTIVE_PHASES))));
        assertFalse(E3DCDiscovery.isWallboxAnswered(RscpValue.container(0, List.of())));
    }

    @Test
    void thePowerPlantMeterAndWallboxAreProposed() {
        DiscoveryResult plant = E3DCDiscovery.powerPlant(BRIDGE);
        assertEquals("e3dc:power-plant:device:power-plant", plant.getThingUID().getAsString());
        assertEquals(BRIDGE, plant.getBridgeUID());

        DiscoveryResult meter = E3DCDiscovery.meter(BRIDGE, 2);
        assertEquals("e3dc:power-meter:device:meter-2", meter.getThingUID().getAsString());
        assertEquals(2, meter.getProperties().get(E3DCDiscovery.PROPERTY_METER_INDEX));
        assertEquals("E3/DC Power Meter 3", meter.getLabel());

        DiscoveryResult wallbox = E3DCDiscovery.wallbox(BRIDGE, 0);
        assertEquals("e3dc:wallbox:device:wallbox-0", wallbox.getThingUID().getAsString());
        assertEquals(0, wallbox.getProperties().get(E3DCDiscovery.PROPERTY_WALLBOX_INDEX));
        assertEquals("E3/DC Wallbox 1", wallbox.getLabel());
    }

    @Test
    void theNewPropertiesMatchTheConfigurationParameters() {
        assertEquals("meterIndex", E3DCPowerMeterConfiguration.class.getFields()[0].getName());
        assertEquals("wallboxIndex", E3DCWallboxConfiguration.class.getFields()[0].getName());
    }
}
