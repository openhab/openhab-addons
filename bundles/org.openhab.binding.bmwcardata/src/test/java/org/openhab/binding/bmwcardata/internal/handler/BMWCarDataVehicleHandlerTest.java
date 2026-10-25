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
package org.openhab.binding.bmwcardata.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.handler.VehicleChannel.Drive;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

/**
 * Tests the processing of telematic data by the {@link BMWCarDataVehicleHandler}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class BMWCarDataVehicleHandlerTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_VEHICLE, "car");
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_ACCOUNT, "account");
    private static final String VIN = "WBY11CF020CM00000";
    private static final String MILEAGE = "vehicle.vehicle.travelledDistance";
    private static final String LATITUDE = "vehicle.cabin.infotainment.navigation.currentLocation.latitude";
    private static final String LONGITUDE = "vehicle.cabin.infotainment.navigation.currentLocation.longitude";

    private @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) BMWCarDataVehicleHandler handler;

    @BeforeEach
    void setUp() {
        Thing thing = ThingBuilder.create(THING_TYPE_VEHICLE, THING_UID).build();
        callback = mock(ThingHandlerCallback.class);
        handler = new BMWCarDataVehicleHandler(thing);
        handler.setCallback(callback);
    }

    @Test
    void publishesKnownDescriptorsAndLastUpdate() {
        handler.updateTelematicData(Map.of(MILEAGE, value(12345, "km", "2026-10-01T10:00:00Z"),
                "vehicle.powertrain.tractionBattery.charging.port.anyPosition.isPlugged",
                value(true, null, "2026-10-01T10:05:00Z"), "vehicle.unknown.descriptor",
                value(1, null, "2026-10-01T11:00:00Z")));

        verifyState("range#mileage", QuantityType.valueOf("12345 km"));
        verifyState("charge#plugged", OnOffType.ON);
        // descriptors without predefined channel are published by dynamic channels
        verifyState("other#unknown-descriptor", new DecimalType(1));
        verifyState(CHANNEL_LAST_UPDATE, new DateTimeType(Instant.parse("2026-10-01T11:00:00Z")));
    }

    @Test
    void ignoresDescriptorsWithoutValue() {
        TelematicValue empty = new TelematicValue();
        empty.value = JsonNull.INSTANCE;
        empty.timestamp = "2026-10-01T12:00:00Z";
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z"),
                "vehicle.cabin.door.lock.status", empty, "vehicle.unknown.descriptor", empty));

        verifyState(CHANNEL_LAST_UPDATE, new DateTimeType(Instant.parse("2026-10-01T10:00:00Z")));
        verify(callback, never()).stateUpdated(eq(channel("status#door-lock")), any());
        verify(callback, never()).thingUpdated(any());

        // the empty value must not block a later value with an older timestamp
        handler.updateTelematicData(Map.of("vehicle.cabin.door.lock.status",
                stringValue("SELECTIVE-LOCKED", null, "2026-10-01T11:00:00Z")));
        verifyState("status#door-lock", new StringType("SELECTIVELOCKED"));
    }

    @Test
    void ignoresDataAndRefreshResultsAfterDispose() {
        handler.dispose();

        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));
        handler.refreshSucceeded();
        handler.refreshFailed("late failure");

        verifyNoInteractions(callback);
    }

    @Test
    void derivesPluggedFromChargingPortUntilVehicleReportsIt() {
        String port = "vehicle.body.chargingPort.status";
        handler.updateTelematicData(Map.of(port, stringValue("Connected", null, "2026-10-01T10:00:00Z")));
        verifyState("charge#plugged", OnOffType.ON);

        handler.updateTelematicData(Map.of(port, stringValue("DISCONNECTED", null, "2026-10-01T10:01:00Z")));
        verifyState("charge#plugged", OnOffType.OFF);

        // other values leave the plug state as it is
        handler.updateTelematicData(Map.of(port, stringValue("INVALID", null, "2026-10-01T10:02:00Z")));
        verify(callback, never()).stateUpdated(channel("charge#plugged"), UnDefType.UNDEF);

        handler.updateTelematicData(Map.of("vehicle.powertrain.tractionBattery.charging.port.anyPosition.isPlugged",
                value(false, null, "2026-10-01T10:03:00Z")));
        handler.updateTelematicData(Map.of(port, stringValue("CONNECTED", null, "2026-10-01T10:04:00Z")));
        verify(callback, times(1)).stateUpdated(channel("charge#plugged"), OnOffType.ON);
    }

    @Test
    void ignoresValuesOlderThanPublishedOnes() {
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));
        handler.updateTelematicData(Map.of(MILEAGE, value(100, "km", "2026-10-01T09:00:00Z")));

        verifyState("range#mileage", QuantityType.valueOf("200 km"));
        verify(callback, never()).stateUpdated(channel("range#mileage"), QuantityType.valueOf("100 km"));
    }

    @Test
    void publishesCopiesOnce() {
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));
        // the stream delivers many messages twice
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));
        verify(callback, times(1)).stateUpdated(channel("range#mileage"), QuantityType.valueOf("200 km"));

        // a new reading with the same value, and another value with the same timestamp, are published
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:05:00Z")));
        handler.updateTelematicData(Map.of(MILEAGE, value(201, "km", "2026-10-01T10:05:00Z")));
        verify(callback, times(2)).stateUpdated(channel("range#mileage"), QuantityType.valueOf("200 km"));
        verifyState("range#mileage", QuantityType.valueOf("201 km"));
    }

    @Test
    void publishesCopiesFromStreamAndApiOnce() {
        // the stream sends numbers as JSON numbers, the REST API as strings
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));
        handler.updateTelematicData(Map.of(MILEAGE, stringValue("200", "km", "2026-10-01T10:00:00Z")));
        handler.updateTelematicData(Map.of(MILEAGE, stringValue("200.0", "km", "2026-10-01T10:00:00Z")));
        verify(callback, times(1)).stateUpdated(channel("range#mileage"), QuantityType.valueOf("200 km"));

        String plugged = "vehicle.powertrain.tractionBattery.charging.port.anyPosition.isPlugged";
        handler.updateTelematicData(Map.of(plugged, value(true, null, "2026-10-01T10:00:00Z")));
        handler.updateTelematicData(Map.of(plugged, stringValue("true", null, "2026-10-01T10:00:00Z")));
        verify(callback, times(1)).stateUpdated(channel("charge#plugged"), OnOffType.ON);

        // another unit is another value
        handler.updateTelematicData(Map.of(MILEAGE, stringValue("200", "mi", "2026-10-01T10:00:00Z")));
        verifyState("range#mileage", QuantityType.valueOf("200 mi"));
    }

    @Test
    void combinesLatitudeAndLongitudeOfSeparateMessages() {
        handler.updateTelematicData(Map.of(LATITUDE, value(48.17, null, "2026-10-01T10:00:00Z")));
        verify(callback, never()).stateUpdated(eq(channel(CHANNEL_GPS)), any());

        handler.updateTelematicData(Map.of(LONGITUDE, value(11.56, null, "2026-10-01T10:00:00Z")));
        verifyState(CHANNEL_GPS, PointType.valueOf("48.17,11.56"));

        handler.updateTelematicData(Map.of(LATITUDE, value(48.18, null, "2026-10-01T10:05:00Z"),
                "vehicle.cabin.infotainment.navigation.currentLocation.altitude",
                value(520, "m", "2026-10-01T10:05:00Z")));
        verifyState("navigation#altitude", QuantityType.valueOf("520 m"));
        verifyState(CHANNEL_GPS, PointType.valueOf("48.18,11.56,520"));
    }

    @Test
    void olderRefreshDoesNotOverwriteNewerStreamDataPublishedMeanwhile() throws Exception {
        // the refresh is held while publishing its mileage: it has accepted its position, but not published it yet
        CountDownLatch refreshPublishing = new CountDownLatch(1);
        CountDownLatch releaseRefresh = new CountDownLatch(1);
        doAnswer(invocation -> {
            refreshPublishing.countDown();
            releaseRefresh.await(10, TimeUnit.SECONDS);
            return null;
        }).when(callback).stateUpdated(channel("range#mileage"), QuantityType.valueOf("100 km"));
        Map<String, TelematicValue> refresh = new LinkedHashMap<>();
        refresh.put(LATITUDE, value(48.17, null, "2026-10-01T10:00:00Z"));
        refresh.put(LONGITUDE, value(11.56, null, "2026-10-01T10:00:00Z"));
        refresh.put(MILEAGE, value(100, "km", "2026-10-01T10:00:00Z"));
        Thread refreshThread = new Thread(() -> handler.updateTelematicData(refresh));
        Thread streamThread = new Thread(() -> handler.updateTelematicData(Map.of(LATITUDE,
                value(48.18, null, "2026-10-01T10:05:00Z"), LONGITUDE, value(11.57, null, "2026-10-01T10:05:00Z"))));
        refreshThread.setDaemon(true);
        streamThread.setDaemon(true);
        try {
            refreshThread.start();
            assertTrue(refreshPublishing.await(10, TimeUnit.SECONDS));
            streamThread.start();
            // the newer stream data has to wait for the refresh; unserialized, it would be published right away
            waitUntil(() -> streamThread.getState() == Thread.State.BLOCKED || !streamThread.isAlive());
        } finally {
            releaseRefresh.countDown();
        }
        refreshThread.join(10_000);
        streamThread.join(10_000);
        assertFalse(refreshThread.isAlive());
        assertFalse(streamThread.isAlive());

        ArgumentCaptor<State> location = ArgumentCaptor.forClass(State.class);
        verify(callback, atLeastOnce()).stateUpdated(eq(channel(CHANNEL_GPS)), location.capture());
        assertEquals(PointType.valueOf("48.18,11.57"), location.getValue());
        // the REFRESH cache holds the newer position as well
        handler.handleCommand(channel(CHANNEL_GPS), RefreshType.REFRESH);
        verify(callback, times(2)).stateUpdated(channel(CHANNEL_GPS), PointType.valueOf("48.18,11.57"));
    }

    private static void waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (!condition.getAsBoolean()) {
            assertTrue(System.nanoTime() < deadline, "condition not reached within 10 seconds");
            Thread.sleep(5);
        }
    }

    @Test
    void publishesStateOfOpenings() {
        handler.updateTelematicData(Map.of("vehicle.cabin.window.row1.driver.status",
                stringValue("INTERMEDIATE", null, "2026-10-01T10:00:00Z")));

        verifyState("status#window-front-driver", OpenClosedType.OPEN);
        verifyState("status#window-front-driver-state", new StringType("INTERMEDIATE"));
    }

    @Test
    void refreshRepublishesCachedState() {
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));

        handler.handleCommand(channel("range#mileage"), RefreshType.REFRESH);
        handler.handleCommand(channel("range#electric"), RefreshType.REFRESH);

        verify(callback, times(2)).stateUpdated(channel("range#mileage"), QuantityType.valueOf("200 km"));
        verify(callback, never()).stateUpdated(eq(channel("range#electric")), any());
    }

    @Test
    void createsDynamicChannelsOnce() {
        handler.updateTelematicData(Map.of("vehicle.vehicle.avgAuxPower",
                stringValue("250", "W", "2026-10-01T10:00:00Z"), "vehicle.cabin.sunroof.tiltStatus",
                stringValue("CLOSED", null, "2026-10-01T10:00:00Z"), "vehicle.body.lights.isRunningOn",
                stringValue("true", null, "2026-10-01T10:00:00Z"), "vehicle.cabin.door.row1.driver.position",
                stringValue("INVALID", null, "2026-10-01T10:00:00Z")));

        ArgumentCaptor<Thing> captor = ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(captor.capture());
        Thing updated = captor.getValue();
        assertEquals("String", channelOf(updated, "other#cabin-sunroof-tilt-status").getAcceptedItemType());
        assertEquals("Switch", channelOf(updated, "other#body-lights-is-running-on").getAcceptedItemType());
        assertEquals("Number:Power", channelOf(updated, "other#vehicle-avg-aux-power").getAcceptedItemType());
        // the item type cannot be derived from a placeholder value
        assertNull(updated.getChannel("other#cabin-door-row1-driver-position"));
        verifyState("other#cabin-sunroof-tilt-status", new StringType("CLOSED"));
        verifyState("other#body-lights-is-running-on", OnOffType.ON);

        handler.updateTelematicData(
                Map.of("vehicle.cabin.sunroof.tiltStatus", stringValue("INVALID", null, "2026-10-01T11:00:00Z")));

        verify(callback, times(1)).thingUpdated(any());
        verifyState("other#cabin-sunroof-tilt-status", UnDefType.UNDEF);
    }

    @Test
    void dynamicChannelWithUnitGetsQuantityItemType() {
        handler.updateTelematicData(
                Map.of("vehicle.drivetrain.avgSpeed", stringValue("42", "km", "2026-10-01T10:00:00Z")));

        ArgumentCaptor<Thing> captor = ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(captor.capture());
        assertEquals("Number:Length", channelOf(captor.getValue(), "other#drivetrain-avg-speed").getAcceptedItemType());
        verifyState("other#drivetrain-avg-speed", QuantityType.valueOf("42 km"));
    }

    private static Channel channelOf(Thing thing, String channelId) {
        Channel channel = thing.getChannel(channelId);
        assertNotNull(channel);
        return Objects.requireNonNull(channel);
    }

    private static TelematicValue stringValue(String value, @Nullable String unit, String timestamp) {
        TelematicValue telematicValue = new TelematicValue();
        telematicValue.value = new JsonPrimitive(value);
        telematicValue.unit = unit;
        telematicValue.timestamp = timestamp;
        return telematicValue;
    }

    @Test
    void electricVehicleLosesCombustionChannels() {
        BasicData basicData = basicData("BEV", null, true, true);
        assertEquals(EnumSet.of(Drive.ELECTRIC), BMWCarDataVehicleHandler.drives(basicData));
        List<String> ids = BMWCarDataVehicleHandler.irrelevantChannelIds(basicData);
        assertTrue(ids.contains(GROUP_RANGE + "#fuel-level"));
        assertTrue(ids.contains(GROUP_RANGE + "#total"));
        assertFalse(ids.contains(GROUP_CHARGE + "#soc"));
        assertFalse(ids.contains(GROUP_STATUS + "#sunroof"));
        assertFalse(ids.contains(CHANNEL_GPS));
    }

    @Test
    void combustionVehicleLosesElectricChannels() {
        BasicData basicData = basicData("CONV", "BE", true, true);
        assertEquals(EnumSet.of(Drive.COMBUSTION), BMWCarDataVehicleHandler.drives(basicData));
        List<String> ids = BMWCarDataVehicleHandler.irrelevantChannelIds(basicData);
        assertTrue(ids.contains(GROUP_CHARGE + "#soc"));
        assertTrue(ids.contains(GROUP_RANGE + "#electric"));
        assertFalse(ids.contains(GROUP_RANGE + "#fuel-level"));
        assertFalse(ids.contains(GROUP_RANGE + "#mileage"));
    }

    @Test
    void hybridAndUnknownDrivesKeepAllDriveChannels() {
        assertEquals(List.of(), BMWCarDataVehicleHandler.irrelevantChannelIds(basicData("PHEV", null, true, true)));
        assertEquals(List.of(), BMWCarDataVehicleHandler.irrelevantChannelIds(basicData("XYZ", null, null, null)));
        assertEquals(List.of(), BMWCarDataVehicleHandler.irrelevantChannelIds(new BasicData()));
        // values not seen on real vehicles are not guessed
        for (String driveTrain : List.of("MHEV", "HEV", "CE", "ICE")) {
            assertEquals(EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION),
                    BMWCarDataVehicleHandler.drives(basicData(driveTrain, null, null, null)));
        }
        // the propulsion type of a combustion vehicle is its fuel
        assertEquals(EnumSet.of(Drive.ELECTRIC, Drive.COMBUSTION),
                BMWCarDataVehicleHandler.drives(basicData(null, "BE", null, null)));
        assertEquals(EnumSet.of(Drive.ELECTRIC), BMWCarDataVehicleHandler.drives(basicData(null, "EL", null, null)));
    }

    @Test
    void missingEquipmentLosesItsChannels() {
        List<String> ids = BMWCarDataVehicleHandler.irrelevantChannelIds(basicData("PHEV", null, false, false));
        assertTrue(ids.contains(GROUP_STATUS + "#sunroof"));
        assertTrue(ids.contains(GROUP_STATUS + "#sunroof-state"));
        assertTrue(ids.contains(GROUP_NAVIGATION + "#destination-distance"));
        assertTrue(ids.contains(GROUP_NAVIGATION + "#arrival-time"));
        // the location comes from the telematics unit
        assertFalse(ids.contains(CHANNEL_GPS));
        assertFalse(ids.contains(GROUP_NAVIGATION + "#heading"));
        assertFalse(ids.contains(GROUP_STATUS + "#door-lock"));
    }

    @Test
    void retrievingThePropertiesRemovesTheChannels() throws Exception {
        BMWCarDataAccountHandler account = mock(BMWCarDataAccountHandler.class);
        when(account.fetchBasicData(VIN)).thenReturn(basicData("CONV", "BE", true, true));
        ThingHandlerCallback vehicleCallback = callbackWithBridge(account);
        BMWCarDataVehicleHandler vehicle = new BMWCarDataVehicleHandler(vehicleThing(Map.of()));
        vehicle.setCallback(vehicleCallback);
        vehicle.initialize();

        // the properties, then the channels
        ArgumentCaptor<Thing> updated = ArgumentCaptor.forClass(Thing.class);
        verify(vehicleCallback, timeout(5000).times(2)).thingUpdated(updated.capture());
        Thing thing = updated.getValue();
        assertEquals("CONV", thing.getProperties().get(PROPERTY_DRIVE_TRAIN));
        assertNull(thing.getChannel(GROUP_CHARGE + "#soc"));
        assertNotNull(thing.getChannel(GROUP_RANGE + "#fuel-level"));
        vehicle.dispose();
    }

    @Test
    void thingWithRetrievedPropertiesIsLeftAlone() throws Exception {
        BMWCarDataAccountHandler account = mock(BMWCarDataAccountHandler.class);
        ThingHandlerCallback vehicleCallback = callbackWithBridge(account);
        BMWCarDataVehicleHandler vehicle = new BMWCarDataVehicleHandler(
                vehicleThing(Map.of(PROPERTY_MODEL, "JCW Countryman ALL4", PROPERTY_DRIVE_TRAIN, "CONV")));
        vehicle.setCallback(vehicleCallback);
        vehicle.initialize();

        verify(account, after(500).never()).fetchBasicData(anyString());
        verify(vehicleCallback, never()).thingUpdated(any());
        vehicle.dispose();
    }

    @Test
    void disposedHandlerDoesNotUpdateTheThing() throws Exception {
        BMWCarDataAccountHandler account = mock(BMWCarDataAccountHandler.class);
        ThingHandlerCallback vehicleCallback = callbackWithBridge(account);
        BMWCarDataVehicleHandler vehicle = new BMWCarDataVehicleHandler(vehicleThing(Map.of()));
        vehicle.setCallback(vehicleCallback);
        // the Thing is disposed while the basic data is retrieved
        when(account.fetchBasicData(VIN)).thenAnswer(invocation -> {
            vehicle.dispose();
            return basicData("CONV", "BE", true, true);
        });
        vehicle.initialize();

        verify(account, timeout(5000)).fetchBasicData(VIN);
        verify(vehicleCallback, after(500).never()).thingUpdated(any());
    }

    private static Thing vehicleThing(Map<String, String> properties) {
        ThingUID uid = new ThingUID(THING_TYPE_VEHICLE, BRIDGE_UID, "car");
        return ThingBuilder.create(THING_TYPE_VEHICLE, uid).withBridge(BRIDGE_UID)
                .withConfiguration(new Configuration(Map.of(CONFIG_VIN, VIN)))
                .withChannels(ChannelBuilder.create(new ChannelUID(uid, GROUP_CHARGE + "#soc")).build(),
                        ChannelBuilder.create(new ChannelUID(uid, GROUP_RANGE + "#fuel-level")).build())
                .withProperties(properties).build();
    }

    private static ThingHandlerCallback callbackWithBridge(BMWCarDataAccountHandler account) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getHandler()).thenReturn(account);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        return callback;
    }

    private static BasicData basicData(@Nullable String driveTrain, @Nullable String propulsionType,
            @Nullable Boolean hasSunRoof, @Nullable Boolean hasNavi) {
        BasicData basicData = new BasicData();
        basicData.driveTrain = driveTrain;
        basicData.propulsionType = propulsionType;
        basicData.hasSunRoof = hasSunRoof;
        basicData.hasNavi = hasNavi;
        return basicData;
    }

    private void verifyState(String channelId, State state) {
        verify(callback).stateUpdated(channel(channelId), state);
    }

    private static ChannelUID channel(String channelId) {
        return new ChannelUID(THING_UID, channelId);
    }

    private static TelematicValue value(Object value, @Nullable String unit, String timestamp) {
        TelematicValue telematicValue = new TelematicValue();
        telematicValue.value = value instanceof Boolean bool ? new JsonPrimitive(bool)
                : new JsonPrimitive((Number) value);
        telematicValue.unit = unit;
        telematicValue.timestamp = timestamp;
        return telematicValue;
    }
}
