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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_RUN_ZONES;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_DEFAULT_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_AVAILABLE_WATER;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_DEPTH_OF_WATER;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_MOISTURE_LEVEL;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_MOISTURE_PERCENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_ROOT_ZONE_DEPTH;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_SATURATED_DEPTH_OF_WATER;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_YARD_AREA_SQUARE_FEET;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

import java.lang.reflect.Field;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests command compatibility for channels that now expose typed Quantity item types.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioQuantityCommandHandlerTest {
    @Test
    void zoneRuntimeAcceptsQuantityTypeSeconds() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        RachioZoneHandler handler = new RachioZoneHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(1));
        handler.cloudHandler = bridgeHandler;
        setField(handler, "dev", device);
        setField(handler, "zone", zone);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_RUNTIME), QuantityType.valueOf("2 min"));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_RUN), OnOffType.ON);

        verify(bridgeHandler).startZone("zone-id", 120);
    }

    @Test
    void zoneRuntimeStillAcceptsPlainNumericSeconds() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        RachioZoneHandler handler = new RachioZoneHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(1));
        handler.cloudHandler = bridgeHandler;
        setField(handler, "dev", device);
        setField(handler, "zone", zone);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_RUNTIME), new DecimalType(30));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_RUN), OnOffType.ON);

        verify(bridgeHandler).startZone("zone-id", 30);
    }

    @Test
    void controllerRuntimeAcceptsQuantityTypeSecondsForMultiZonePayload() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, "bridge", "device");
        RachioDeviceHandler handler = new RachioDeviceHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        handler.cloudHandler = bridgeHandler;
        handler.dev = device;
        when(bridgeHandler.getDefaultRuntime()).thenReturn(300);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_DEVICE_RUN_ZONES), new StringType("1"));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_DEVICE_RUNTIME), QuantityType.valueOf("2 min"));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_DEVICE_RUN), OnOffType.ON);

        assertThat(device.getRunTime(), is(120));
        verify(bridgeHandler).runMultipleZones(contains("\"duration\" : 120"));
    }

    @Test
    void controllerRuntimeStillAcceptsPlainNumericSecondsForMultiZonePayload() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, "bridge", "device");
        RachioDeviceHandler handler = new RachioDeviceHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        handler.cloudHandler = bridgeHandler;
        handler.dev = device;
        when(bridgeHandler.getDefaultRuntime()).thenReturn(300);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_DEVICE_RUN_ZONES), new StringType("1"));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_DEVICE_RUNTIME), new DecimalType(30));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_DEVICE_RUN), OnOffType.ON);

        assertThat(device.getRunTime(), is(30));
        verify(bridgeHandler).runMultipleZones(contains("\"duration\" : 30"));
    }

    @Test
    void valveRuntimeAcceptsQuantityTypeSeconds() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_VALVE, "bridge", "valve");
        RachioValveHandler handler = new RachioValveHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "valve", valve("valve-id"));

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_VALVE_RUNTIME), QuantityType.valueOf("2 min"));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_VALVE_RUN), OnOffType.ON);

        verify(bridgeHandler).startValveWatering("valve-id", 120);
    }

    @Test
    void valveRuntimeStillAcceptsPlainNumericSeconds() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_VALVE, "bridge", "valve");
        RachioValveHandler handler = new RachioValveHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "valve", valve("valve-id"));

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_VALVE_RUNTIME), new DecimalType(30));
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_VALVE_RUN), OnOffType.ON);

        verify(bridgeHandler).startValveWatering("valve-id", 30);
    }

    @Test
    void valveDefaultRuntimeAcceptsQuantityTypeSeconds() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_VALVE, "bridge", "valve");
        RachioValveHandler handler = new RachioValveHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "valve", valve("valve-id"));

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_VALVE_DEFAULT_RUNTIME), QuantityType.valueOf("15 min"));

        verify(bridgeHandler).setValveDefaultRuntime(eq("valve-id"), eq(900));
    }

    @Test
    void valveDefaultRuntimeStillAcceptsPlainNumericSeconds() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_VALVE, "bridge", "valve");
        RachioValveHandler handler = new RachioValveHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "valve", valve("valve-id"));

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_VALVE_DEFAULT_RUNTIME), new DecimalType(900));

        verify(bridgeHandler).setValveDefaultRuntime(eq("valve-id"), eq(900));
    }

    @Test
    void normalZoneRefreshDoesNotForceCommandOnlyMoistureChannelsToUndef() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        TestZoneHandler handler = new TestZoneHandler(thing(thingUID));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        handler.setCallback(callback);
        handler.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        setField(handler, "dev", device);
        setField(handler, "zone", Objects.requireNonNull(device.getZoneByNumber(1)));

        handler.publicPostChannelData();

        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_LEVEL)), any());
        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_PERCENT)), any());
    }

    @Test
    void zoneWaterDepthTelemetryRemainsLengthQuantitiesAndDoesNotPopulateMoisture() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        TestZoneHandler handler = new TestZoneHandler(thing(thingUID));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        RachioCloudZone cloudZone = zone("zone-id", 1);
        cloudZone.availableWater = 0.07;
        cloudZone.depthOfWater = 1.07;
        cloudZone.saturatedDepthOfWater = 1.18;
        cloudZone.rootZoneDepth = 30.48;
        cloudZone.yardAreaSquareFeet = 46;
        RachioDevice device = deviceWithZones(cloudZone);
        handler.setCallback(callback);
        handler.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        setField(handler, "dev", device);
        setField(handler, "zone", Objects.requireNonNull(device.getZoneByNumber(1)));

        handler.publicPostChannelData();

        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_AVAILABLE_WATER),
                RachioQuantityTypes.inchesOrNull(0.07));
        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_DEPTH_OF_WATER),
                RachioQuantityTypes.inchesOrNull(1.07));
        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_SATURATED_DEPTH_OF_WATER),
                RachioQuantityTypes.inchesOrNull(1.18));
        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_ROOT_ZONE_DEPTH),
                RachioQuantityTypes.inchesOrNull(30.48));
        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_YARD_AREA_SQUARE_FEET),
                RachioQuantityTypes.squareFeet(46));
        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_LEVEL)), any());
        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_PERCENT)), any());
    }

    @Test
    void moistureLevelCommandCallsApiAndOptimisticallyUpdatesChannel() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        RachioZoneHandler handler = new RachioZoneHandler(thing(thingUID));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(1));
        handler.setCallback(callback);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "dev", device);
        setField(handler, "zone", zone);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_LEVEL), new DecimalType(42));

        verify(bridgeHandler).setZoneMoistureLevel("zone-id", 42.0);
        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_LEVEL),
                RachioQuantityTypes.millimetersOrUndef(42.0));
    }

    @Test
    void moisturePercentCommandCallsApiAndOptimisticallyUpdatesChannel() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        RachioZoneHandler handler = new RachioZoneHandler(thing(thingUID));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(1));
        handler.setCallback(callback);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "dev", device);
        setField(handler, "zone", zone);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_PERCENT), new DecimalType("0.4"));

        verify(bridgeHandler).setZoneMoisturePercent("zone-id", 0.4);
        verify(callback).stateUpdated(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_PERCENT),
                RachioQuantityTypes.fractionOrUndef(0.4));
    }

    @Test
    void failedMoistureCommandDoesNotUpdateChannelState() throws Exception {
        ThingUID thingUID = new ThingUID(THING_TYPE_ZONE, "bridge", "zone");
        RachioZoneHandler handler = new RachioZoneHandler(thing(thingUID));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioDevice device = deviceWithZones(zone("zone-id", 1));
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(1));
        doThrow(new RachioApiException("failed")).when(bridgeHandler).setZoneMoistureLevel("zone-id", 42.0);
        handler.setCallback(callback);
        handler.cloudHandler = bridgeHandler;
        setField(handler, "dev", device);
        setField(handler, "zone", zone);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_LEVEL), new DecimalType(42));

        verify(callback, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_ZONE_MOISTURE_LEVEL)), any());
    }

    private Thing thing(ThingUID thingUID) {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(thingUID);
        return thing;
    }

    private RachioDevice deviceWithZones(RachioCloudZone... zones) {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "device-id";
        cloudDevice.name = "Controller";
        cloudDevice.macAddress = "ABCDEF123456";
        cloudDevice.zones.addAll(java.util.List.of(zones));
        return new RachioDevice(cloudDevice);
    }

    private RachioCloudZone zone(String id, int zoneNumber) {
        RachioCloudZone zone = new RachioCloudZone();
        zone.id = id;
        zone.name = "Zone " + zoneNumber;
        zone.zoneNumber = zoneNumber;
        zone.enabled = true;
        return zone;
    }

    private RachioValve valve(String id) {
        RachioValve valve = new RachioValve();
        valve.id = id;
        valve.name = "Valve";
        valve.defaultRuntimeSeconds = 300;
        return valve;
    }

    private void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> currentType = type;
        while (currentType != null) {
            try {
                return currentType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentType = currentType.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static class TestZoneHandler extends RachioZoneHandler {
        TestZoneHandler(Thing thing) {
            super(thing);
        }

        void publicPostChannelData() {
            postChannelData();
        }
    }
}
