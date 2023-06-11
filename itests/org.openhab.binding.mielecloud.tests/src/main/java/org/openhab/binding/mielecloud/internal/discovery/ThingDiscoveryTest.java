/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.OpenHabOsgiTest;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceIdentLabel;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceType;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Ident;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Type;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.inbox.InboxPredicates;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ThingDiscoveryTest extends OpenHabOsgiTest {
    private static final String DEVICE_TYPE_NAME_COFFEE_SYSTEM = "Coffee System";
    private static final String DEVICE_TYPE_NAME_DISHWASHER = "Dishwasher";
    private static final String DEVICE_TYPE_NAME_DISH_WARMER = "Dish Warmer";
    private static final String DEVICE_TYPE_NAME_DRYER = "Dryer";
    private static final String DEVICE_TYPE_NAME_FRIDGE_FREEZER = "Fridge Freezer";
    private static final String DEVICE_TYPE_NAME_HOB = "Hob";
    private static final String DEVICE_TYPE_NAME_HOOD = "Hood";
    private static final String DEVICE_TYPE_NAME_OVEN = "Oven";
    private static final String DEVICE_TYPE_NAME_ROBOTIC_VACUUM_CLEANER = "Robotic Vacuum Cleaner";
    private static final String DEVICE_TYPE_NAME_WASHING_MACHINE = "Washing Machine";
    private static final String DEVICE_TYPE_NAME_WINE_STORAGE = "Wine Storage";

    private static final String TECH_TYPE = "WM1234";
    private static final String TECH_TYPE_2 = "CM1234";
    private static final String DEVICE_NAME = "My Device";
    private static final String DEVICE_NAME_2 = "My Other Device";
    private static final String SERIAL_NUMBER_2 = "900124430017";

    private static final ThingUID DISHWASHER_DEVICE_THING_UID_WITH_SERIAL_NUMBER_2 = new ThingUID(
            new ThingTypeUID(MieleCloudBindingConstants.BINDING_ID, "dishwasher"), BRIDGE_THING_UID, SERIAL_NUMBER_2);

    @Nullable
    private ThingDiscoveryService discoveryService;

    private ThingDiscoveryService getDiscoveryService() {
        assertNotNull(discoveryService);
        return Objects.requireNonNull(discoveryService);
    }

    @BeforeEach
    public void setUp()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        setUpBridge();
        setUpDiscoveryService();
    }

    private void setUpDiscoveryService()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        waitForAssert(() -> {
            discoveryService = getService(DiscoveryService.class, ThingDiscoveryService.class);
            assertNotNull(discoveryService);
        });

        getDiscoveryService().activate();
    }

    private DeviceState createDeviceState(String fabNumber, String techType, String deviceName, DeviceType deviceType,
            String deviceTypeText) {
        // given:
        DeviceIdentLabel deviceIdentLabel = mock(DeviceIdentLabel.class);
        when(deviceIdentLabel.getFabNumber()).thenReturn(Optional.of(fabNumber));
        when(deviceIdentLabel.getTechType()).thenReturn(Optional.of(techType));

        Type type = mock(Type.class);
        when(type.getValueRaw()).thenReturn(deviceType);
        when(type.getValueLocalized()).thenReturn(Optional.of(deviceTypeText));

        Ident ident = mock(Ident.class);
        when(ident.getDeviceIdentLabel()).thenReturn(Optional.of(deviceIdentLabel));
        when(ident.getType()).thenReturn(Optional.of(type));
        when(ident.getDeviceName()).thenReturn(Optional.of(deviceName));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));

        return new DeviceState(fabNumber, device);
    }

    private void assertValidDiscoveryResult(ThingUID expectedThingUID, String expectedSerialNumber,
            String expectedDeviceIdentifier, String expectedLabel, String expectedModelId) {
        List<DiscoveryResult> results = getInbox().stream().filter(InboxPredicates.forThingUID(expectedThingUID))
                .collect(Collectors.toList());
        assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

        DiscoveryResult result = results.get(0);
        assertEquals(MieleCloudBindingConstants.BINDING_ID, result.getBindingId(), "Invalid binding ID");
        assertEquals(MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID, result.getBridgeUID(),
                "Invalid bridge UID");
        assertEquals(Thing.PROPERTY_SERIAL_NUMBER, result.getRepresentationProperty(),
                "Invalid representation property");
        assertEquals(expectedModelId, result.getProperties().get(Thing.PROPERTY_MODEL_ID), "Invalid model ID");
        assertEquals(expectedLabel, result.getLabel(), "Invalid label");
        assertEquals(expectedSerialNumber, result.getProperties().get(Thing.PROPERTY_SERIAL_NUMBER),
                "Invalid serial number");
        assertEquals(expectedDeviceIdentifier,
                result.getProperties().get(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER),
                "Invalid serial number");
    }

    private void testMieleDeviceInboxDiscoveryResult(DeviceType deviceType, ThingUID expectedThingUid,
            String deviceTypeName) {
        // given:
        DeviceState deviceState = createDeviceState(SERIAL_NUMBER, TECH_TYPE, DEVICE_NAME, deviceType, deviceTypeName);

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        assertValidDiscoveryResult(expectedThingUid, SERIAL_NUMBER, SERIAL_NUMBER, DEVICE_NAME,
                deviceTypeName + " " + TECH_TYPE);
    }

    @Test
    public void testWashingDeviceInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.WASHING_MACHINE, WASHING_MACHINE_THING_UID,
                DEVICE_TYPE_NAME_WASHING_MACHINE);
    }

    @Test
    public void testOvenInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.OVEN, OVEN_DEVICE_THING_UID, DEVICE_TYPE_NAME_OVEN);
    }

    @Test
    public void testHobInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.HOB_HIGHLIGHT, HOB_DEVICE_THING_UID, DEVICE_TYPE_NAME_HOB);
    }

    @Test
    public void testCoolingDeviceInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.FRIDGE_FREEZER_COMBINATION, FRIDGE_FREEZER_DEVICE_THING_UID,
                DEVICE_TYPE_NAME_FRIDGE_FREEZER);
    }

    @Test
    public void testHoodInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.HOOD, HOOD_DEVICE_THING_UID, DEVICE_TYPE_NAME_HOOD);
    }

    @Test
    public void testCoffeeDeviceInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.COFFEE_SYSTEM, COFFEE_SYSTEM_THING_UID,
                DEVICE_TYPE_NAME_COFFEE_SYSTEM);
    }

    @Test
    public void testWineStorageDeviceInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.WINE_CABINET, WINE_STORAGE_DEVICE_THING_UID,
                DEVICE_TYPE_NAME_WINE_STORAGE);
    }

    @Test
    public void testDryerInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.TUMBLE_DRYER, DRYER_DEVICE_THING_UID, DEVICE_TYPE_NAME_DRYER);
    }

    @Test
    public void testDishwasherInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.DISHWASHER, DISHWASHER_DEVICE_THING_UID,
                DEVICE_TYPE_NAME_DISHWASHER);
    }

    @Test
    public void testDishWarmerInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.DISH_WARMER, DISH_WARMER_DEVICE_THING_UID,
                DEVICE_TYPE_NAME_DISH_WARMER);
    }

    @Test
    public void testRoboticVacuumCleanerInboxDiscoveryResult() {
        testMieleDeviceInboxDiscoveryResult(DeviceType.VACUUM_CLEANER, ROBOTIC_VACUUM_CLEANER_THING_UID,
                DEVICE_TYPE_NAME_ROBOTIC_VACUUM_CLEANER);
    }

    @Test
    public void testUnknownDeviceCreatesNoInboxDiscoveryResult() {
        // given:
        DeviceState deviceState = createDeviceState(SERIAL_NUMBER, TECH_TYPE, DEVICE_NAME, DeviceType.VACUUM_DRAWER,
                "Vacuum Drawer");

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(0, results.size(), "Amount of things in inbox does not match expected number");
        });
    }

    @Test
    public void testDeviceDiscoveryResultOfDeviceRemovedInTheCloudIsRemovedFromTheInbox() throws InterruptedException {
        // given:
        testMieleDeviceInboxDiscoveryResult(DeviceType.HOOD, HOOD_DEVICE_THING_UID, DEVICE_TYPE_NAME_HOOD);

        Thread.sleep(10);

        // when:
        getDiscoveryService().onDeviceRemoved(SERIAL_NUMBER);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(0, results.size(), "Amount of things in inbox does not match expected number");
        });
    }

    @Test
    public void testDiscoveryResultsForTwoDevices() {
        // given:
        DeviceState hoodDevice = createDeviceState(SERIAL_NUMBER, TECH_TYPE, DEVICE_NAME, DeviceType.HOOD,
                DEVICE_TYPE_NAME_HOOD);
        DeviceState dishwasherDevice = createDeviceState(SERIAL_NUMBER_2, TECH_TYPE_2, DEVICE_NAME_2,
                DeviceType.DISHWASHER, DEVICE_TYPE_NAME_DISHWASHER);

        // when:
        getDiscoveryService().onDeviceStateUpdated(hoodDevice);
        getDiscoveryService().onDeviceStateUpdated(dishwasherDevice);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(2, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(HOOD_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER, DEVICE_NAME,
                    "Hood " + TECH_TYPE);
            assertValidDiscoveryResult(DISHWASHER_DEVICE_THING_UID_WITH_SERIAL_NUMBER_2, SERIAL_NUMBER_2,
                    SERIAL_NUMBER_2, DEVICE_NAME_2, DEVICE_TYPE_NAME_DISHWASHER + " " + TECH_TYPE_2);
        });
    }

    @Test
    public void testOnlyDeviceDiscoveryResultsOfDevicesRemovedInTheCloudAreRemovedFromTheInbox()
            throws InterruptedException {
        // given:
        DeviceState hoodDevice = createDeviceState(SERIAL_NUMBER, TECH_TYPE, DEVICE_NAME, DeviceType.HOOD,
                DEVICE_TYPE_NAME_HOOD);
        DeviceState dishwasherDevice = createDeviceState(SERIAL_NUMBER_2, TECH_TYPE_2, DEVICE_NAME_2,
                DeviceType.DISHWASHER, DEVICE_TYPE_NAME_DISHWASHER);
        getDiscoveryService().onDeviceStateUpdated(hoodDevice);
        getDiscoveryService().onDeviceStateUpdated(dishwasherDevice);

        Thread.sleep(10);

        // when:
        // This order of invocation is enforced by the webservice implementation.
        getDiscoveryService().onDeviceRemoved(SERIAL_NUMBER_2);
        getDiscoveryService().onDeviceStateUpdated(hoodDevice);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(HOOD_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER, DEVICE_NAME,
                    DEVICE_TYPE_NAME_HOOD + " " + TECH_TYPE);
        });
    }

    @Test
    public void testIfNoDeviceNameIsSetThenTheDiscoveryLabelIsTheDeviceTypePlusTheTechType() {
        // given:
        DeviceState deviceState = createDeviceState(SERIAL_NUMBER, TECH_TYPE, "", DeviceType.FRIDGE_FREEZER_COMBINATION,
                DEVICE_TYPE_NAME_FRIDGE_FREEZER);

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(FRIDGE_FREEZER_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER,
                    "Fridge Freezer " + TECH_TYPE, DEVICE_TYPE_NAME_FRIDGE_FREEZER + " " + TECH_TYPE);
        });
    }

    @Test
    public void testIfNeitherDeviceTypeNorDeviceNameAreSetThenTheDiscoveryModelIdAndTheLabelAreTheTechType() {
        // given:
        DeviceState deviceState = createDeviceState(SERIAL_NUMBER, TECH_TYPE, "", DeviceType.FRIDGE_FREEZER_COMBINATION,
                "");

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(FRIDGE_FREEZER_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER, TECH_TYPE,
                    TECH_TYPE);
        });
    }

    @Test
    public void testIfNeitherTechTypeNorDeviceNameAreSetThenTheDiscoveryModelIdAndTheLabelAreTheDeviceType() {
        // given:
        DeviceState deviceState = createDeviceState(SERIAL_NUMBER, "", "", DeviceType.FRIDGE_FREEZER_COMBINATION,
                DEVICE_TYPE_NAME_FRIDGE_FREEZER);

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(FRIDGE_FREEZER_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER,
                    DEVICE_TYPE_NAME_FRIDGE_FREEZER, DEVICE_TYPE_NAME_FRIDGE_FREEZER);
        });
    }

    @Test
    public void testIfNeitherTechTypeNorDeviceTypeNorDeviceNameAreSetThenTheDiscoveryModelIdIsUnknownAndTheLabelIsMieleDevice() {
        // given:
        DeviceState deviceState = createDeviceState(SERIAL_NUMBER, "", "", DeviceType.FRIDGE_FREEZER_COMBINATION, "");

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(FRIDGE_FREEZER_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER, "Miele Device",
                    "Unknown");
        });
    }

    @Test
    public void testIfNoSerialNumberIsSetThenTheDeviceIdentifierIsUsedAsReplacement() {
        // given:
        DeviceIdentLabel deviceIdentLabel = mock(DeviceIdentLabel.class);
        when(deviceIdentLabel.getFabNumber()).thenReturn(Optional.of(""));
        when(deviceIdentLabel.getTechType()).thenReturn(Optional.of(TECH_TYPE));

        Type type = mock(Type.class);
        when(type.getValueRaw()).thenReturn(DeviceType.FRIDGE_FREEZER_COMBINATION);
        when(type.getValueLocalized()).thenReturn(Optional.of(DEVICE_TYPE_NAME_FRIDGE_FREEZER));

        Ident ident = mock(Ident.class);
        when(ident.getDeviceIdentLabel()).thenReturn(Optional.of(deviceIdentLabel));
        when(ident.getType()).thenReturn(Optional.of(type));
        when(ident.getDeviceName()).thenReturn(Optional.of(""));

        Device device = mock(Device.class);
        when(device.getIdent()).thenReturn(Optional.of(ident));
        DeviceState deviceState = new DeviceState(SERIAL_NUMBER, device);

        // when:
        getDiscoveryService().onDeviceStateUpdated(deviceState);

        // then:
        waitForAssert(() -> {
            List<DiscoveryResult> results = getInbox().stream().collect(Collectors.toList());
            assertEquals(1, results.size(), "Amount of things in inbox does not match expected number");

            assertValidDiscoveryResult(FRIDGE_FREEZER_DEVICE_THING_UID, SERIAL_NUMBER, SERIAL_NUMBER,
                    DEVICE_TYPE_NAME_FRIDGE_FREEZER + " " + TECH_TYPE,
                    DEVICE_TYPE_NAME_FRIDGE_FREEZER + " " + TECH_TYPE);
        });
    }
}
