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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.bmwcardata.internal.api.dto.BasicData;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.binding.bmwcardata.internal.handler.VehicleChannel.Drive;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
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
    void ignoresValuesOlderThanPublishedOnes() {
        handler.updateTelematicData(Map.of(MILEAGE, value(200, "km", "2026-10-01T10:00:00Z")));
        handler.updateTelematicData(Map.of(MILEAGE, value(100, "km", "2026-10-01T09:00:00Z")));

        verifyState("range#mileage", QuantityType.valueOf("200 km"));
        verify(callback, never()).stateUpdated(channel("range#mileage"), QuantityType.valueOf("100 km"));
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
        BasicData basicData = basicData(null, "CONV", true, true);
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
        assertEquals(EnumSet.of(Drive.COMBUSTION),
                BMWCarDataVehicleHandler.drives(basicData("MILD_HYBRID", null, null, null)));
    }

    @Test
    void missingEquipmentLosesItsChannels() {
        List<String> ids = BMWCarDataVehicleHandler.irrelevantChannelIds(basicData("PHEV", null, false, false));
        assertTrue(ids.contains(GROUP_STATUS + "#sunroof"));
        assertTrue(ids.contains(GROUP_STATUS + "#sunroof-state"));
        assertTrue(ids.contains(CHANNEL_GPS));
        assertTrue(ids.contains(GROUP_NAVIGATION + "#heading"));
        assertTrue(ids.contains(GROUP_NAVIGATION + "#destination-distance"));
        assertFalse(ids.contains(GROUP_STATUS + "#door-lock"));
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
