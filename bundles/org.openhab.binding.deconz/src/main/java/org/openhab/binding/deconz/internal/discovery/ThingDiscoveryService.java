/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.Collections;
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.handler.DeconzBridgeHandler;

/**
 * Every bridge will add its discovered sensors to this discovery service to make them
 * available to the framework.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ThingDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_PRESENCE_SENSOR, THING_TYPE_DAYLIGHT_SENSOR, THING_TYPE_POWER_SENSOR,
                    THING_TYPE_CONSUMPTION_SENSOR, THING_TYPE_LIGHT_SENSOR, THING_TYPE_TEMPERATURE_SENSOR,
                    THING_TYPE_HUMIDITY_SENSOR, THING_TYPE_PRESSURE_SENSOR, THING_TYPE_SWITCH,
                    THING_TYPE_OPENCLOSE_SENSOR, THING_TYPE_WATERLEAKAGE_SENSOR, THING_TYPE_ALARM_SENSOR,
                    THING_TYPE_VIBRATION_SENSOR).collect(Collectors.toSet()));

    private @Nullable DeconzBridgeHandler handler;
    private @Nullable ScheduledFuture<?> scanningJob;

    public ThingDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 30);
    }

    @Override
    protected void startScan() {
        handler.requestFullState();
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (scanningJob == null || scanningJob.isCancelled()) {
            scanningJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, 5, TimeUnit.MINUTES);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        if (scanningJob != null && !scanningJob.isCancelled()) {
            scanningJob.cancel(true);
            scanningJob = null;
        }
    }

    /**
     * Add a sensor device to the discovery inbox.
     *
     * @param sensor The sensor description
     * @param bridgeUID The bridge UID
     */
    private void addDevice(String sensorID, SensorMessage sensor) {
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
        } else if (sensor.type.contains("ZHAAlarm")) {
            thingTypeUID = THING_TYPE_ALARM_SENSOR; // ZHAAlarm
        } else if (sensor.type.contains("ZHAVibration")) {
            thingTypeUID = THING_TYPE_VIBRATION_SENSOR; // ZHAVibration
        } else {
            return;
        }

        ThingUID uid = new ThingUID(thingTypeUID, handler.getThing().getUID(),
                sensor.uniqueid.replaceAll("[^a-z0-9\\[\\]]", ""));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(uid).withBridge(handler.getThing().getUID())
                .withLabel(sensor.name + " (" + sensor.manufacturername + ")").withProperty("id", sensorID)
                .withProperty(UNIQUE_ID, sensor.uniqueid).withRepresentationProperty(UNIQUE_ID).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof DeconzBridgeHandler) {
            this.handler = (DeconzBridgeHandler) handler;
            this.handler.setDiscoveryService(this);
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
     * Call this method when a full bridge state request has been performed and either the sensors
     * are known or a failure happened.
     *
     * @param sensors The sensors or null.
     */
    public void stateRequestFinished(@Nullable Map<String, SensorMessage> sensors) {
        stopScan();
        removeOlderResults(getTimestampOfLastScan());
        if (sensors != null) {
            sensors.forEach(this::addDevice);
        }
    }
}
