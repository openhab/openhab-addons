/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.discovery;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.BridgeFullState;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.handler.DeconzBridgeHandler;
import org.openhab.binding.deconz.internal.handler.LightThingHandler;
import org.openhab.binding.deconz.internal.handler.SensorThermostatThingHandler;
import org.openhab.binding.deconz.internal.handler.SensorThingHandler;
import org.openhab.binding.deconz.internal.types.LightType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every bridge will add its discovered sensors to this discovery service to make them
 * available to the framework.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ThingDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(LightThingHandler.SUPPORTED_THING_TYPE_UIDS, SensorThingHandler.SUPPORTED_THING_TYPES,
                    SensorThermostatThingHandler.SUPPORTED_THING_TYPES)
            .flatMap(Set::stream).collect(Collectors.toSet());
    private final Logger logger = LoggerFactory.getLogger(ThingDiscoveryService.class);

    private @Nullable DeconzBridgeHandler handler;
    private @Nullable ScheduledFuture<?> scanningJob;
    private @Nullable ThingUID bridgeUID;

    public ThingDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30);
    }

    @Override
    protected void startScan() {
        final DeconzBridgeHandler handler = this.handler;
        if (handler != null) {
            handler.requestFullState();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        final ScheduledFuture<?> scanningJob = this.scanningJob;
        if (scanningJob == null || scanningJob.isCancelled()) {
            this.scanningJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        final ScheduledFuture<?> scanningJob = this.scanningJob;
        if (scanningJob != null) {
            scanningJob.cancel(true);
            this.scanningJob = null;
        }
    }

    /**
     * Add a sensor device to the discovery inbox.
     *
     * @param lightID The id of the light
     * @param light The sensor description
     */
    private void addLight(String lightID, LightMessage light) {
        final ThingUID bridgeUID = this.bridgeUID;
        if (bridgeUID == null) {
            logger.warn("Received a message from non-existent bridge. This most likely is a bug.");
            return;
        }

        ThingTypeUID thingTypeUID;
        LightType lightType = light.type;

        if (lightType == null) {
            logger.warn("No light type reported for light {} ({})", light.modelid, light.name);
            return;
        }

        Map<String, Object> properties = new HashMap<>();
        properties.put("id", lightID);
        properties.put(UNIQUE_ID, light.uniqueid);
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, light.swversion);
        properties.put(Thing.PROPERTY_VENDOR, light.manufacturername);
        properties.put(Thing.PROPERTY_MODEL_ID, light.modelid);

        if (light.ctmax != null && light.ctmin != null) {
            properties.put(PROPERTY_CT_MAX,
                    Integer.toString(Util.constrainToRange(light.ctmax, ZCL_CT_MIN, ZCL_CT_MAX)));
            properties.put(PROPERTY_CT_MIN,
                    Integer.toString(Util.constrainToRange(light.ctmin, ZCL_CT_MIN, ZCL_CT_MAX)));
        }

        switch (lightType) {
            case ON_OFF_LIGHT:
            case ON_OFF_PLUGIN_UNIT:
                thingTypeUID = THING_TYPE_ONOFF_LIGHT;
                break;
            case DIMMABLE_LIGHT:
            case DIMMABLE_PLUGIN_UNIT:
                thingTypeUID = THING_TYPE_DIMMABLE_LIGHT;
                break;
            case COLOR_TEMPERATURE_LIGHT:
                thingTypeUID = THING_TYPE_COLOR_TEMPERATURE_LIGHT;
                break;
            case COLOR_DIMMABLE_LIGHT:
            case COLOR_LIGHT:
                thingTypeUID = THING_TYPE_COLOR_LIGHT;
                break;
            case EXTENDED_COLOR_LIGHT:
                thingTypeUID = THING_TYPE_EXTENDED_COLOR_LIGHT;
                break;
            case WINDOW_COVERING_DEVICE:
                thingTypeUID = THING_TYPE_WINDOW_COVERING;
                break;
            case CONFIGURATION_TOOL:
                // ignore configuration tool device
                return;
            default:
                logger.debug(
                        "Found light: {} ({}), type {} but no thing type defined for that type. This should be reported.",
                        light.modelid, light.name, lightType);
                return;
        }

        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, light.uniqueid.replaceAll("[^a-z0-9\\[\\]]", ""));
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(light.name + " (" + light.manufacturername + ")").withProperties(properties)
                .withRepresentationProperty(UNIQUE_ID).build();
        thingDiscovered(discoveryResult);
    }

    /**
     * Add a sensor device to the discovery inbox.
     *
     * @param sensorID The id of the sensor
     * @param sensor The sensor description
     */
    private void addSensor(String sensorID, SensorMessage sensor) {
        final ThingUID bridgeUID = this.bridgeUID;
        if (bridgeUID == null) {
            logger.warn("Received a message from non-existent bridge. This most likely is a bug.");
            return;
        }
        ThingTypeUID thingTypeUID;
        if (sensor.type.contains("Daylight")) { // deCONZ specific: Software simulated daylight sensor
            thingTypeUID = THING_TYPE_DAYLIGHT_SENSOR;
        } else if (sensor.type.contains("Power")) { // ZHAPower, CLIPPower
            thingTypeUID = THING_TYPE_POWER_SENSOR;
        } else if (sensor.type.contains("ZHAConsumption")) { // ZHAConsumption
            thingTypeUID = THING_TYPE_CONSUMPTION_SENSOR;
        } else if (sensor.type.contains("Presence")) { // ZHAPresence, CLIPPrensence
            thingTypeUID = THING_TYPE_PRESENCE_SENSOR;
        } else if (sensor.type.contains("Switch")) { // ZHASwitch
            thingTypeUID = THING_TYPE_SWITCH;
        } else if (sensor.type.contains("LightLevel")) { // ZHALightLevel
            thingTypeUID = THING_TYPE_LIGHT_SENSOR;
        } else if (sensor.type.contains("ZHATemperature")) { // ZHATemperature
            thingTypeUID = THING_TYPE_TEMPERATURE_SENSOR;
        } else if (sensor.type.contains("ZHAHumidity")) { // ZHAHumidity
            thingTypeUID = THING_TYPE_HUMIDITY_SENSOR;
        } else if (sensor.type.contains("ZHAPressure")) { // ZHAPressure
            thingTypeUID = THING_TYPE_PRESSURE_SENSOR;
        } else if (sensor.type.contains("ZHAOpenClose")) { // ZHAOpenClose
            thingTypeUID = THING_TYPE_OPENCLOSE_SENSOR;
        } else if (sensor.type.contains("ZHAWater")) { // ZHAWater
            thingTypeUID = THING_TYPE_WATERLEAKAGE_SENSOR;
        } else if (sensor.type.contains("ZHAFire")) {
            thingTypeUID = THING_TYPE_FIRE_SENSOR; // ZHAFire
        } else if (sensor.type.contains("ZHAAlarm")) {
            thingTypeUID = THING_TYPE_ALARM_SENSOR; // ZHAAlarm
        } else if (sensor.type.contains("ZHAVibration")) {
            thingTypeUID = THING_TYPE_VIBRATION_SENSOR; // ZHAVibration
        } else if (sensor.type.contains("ZHABattery")) {
            thingTypeUID = THING_TYPE_BATTERY_SENSOR; // ZHABattery
        } else if (sensor.type.contains("ZHAThermostat")) {
            thingTypeUID = THING_TYPE_THERMOSTAT; // ZHAThermostat
        } else {
            logger.debug("Unknown type {}", sensor.type);
            return;
        }

        ThingUID uid = new ThingUID(thingTypeUID, bridgeUID, sensor.uniqueid.replaceAll("[^a-z0-9\\[\\]]", ""));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID)
                .withLabel(sensor.name + " (" + sensor.manufacturername + ")").withProperty("id", sensorID)
                .withProperty(UNIQUE_ID, sensor.uniqueid).withRepresentationProperty(UNIQUE_ID).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DeconzBridgeHandler) {
            this.handler = (DeconzBridgeHandler) handler;
            ((DeconzBridgeHandler) handler).setDiscoveryService(this);
            this.bridgeUID = handler.getThing().getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @Override
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * Call this method when a full bridge state request has been performed and either the fullState
     * are known or a failure happened.
     *
     * @param fullState The fullState or null.
     */
    public void stateRequestFinished(final @Nullable BridgeFullState fullState) {
        stopScan();
        removeOlderResults(getTimestampOfLastScan());
        if (fullState != null) {
            fullState.sensors.forEach(this::addSensor);
            fullState.lights.forEach(this::addLight);
        }
    }
}
