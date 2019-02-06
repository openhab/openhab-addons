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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.FullSensor.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hue.internal.FullHueObject;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.SensorConfigUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Sensor Handler
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class HueSensorHandler extends BaseThingHandler implements SensorStatusListener {

    private @NonNullByDefault({}) String sensorId;

    private final Logger logger = LoggerFactory.getLogger(HueSensorHandler.class);

    private boolean propertiesInitializedSuccessfully;

    private @Nullable HueClient hueClient;

    public HueSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue sensor handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configSensorId = (String) getConfig().get(SENSOR_ID);
        if (configSensorId != null) {
            sensorId = configSensorId;
            // note: this call implicitly registers our handler as a listener on the bridge
            if (getHueClient() != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    initializeProperties();
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-sensor-id");
        }
    }

    private synchronized void initializeProperties() {
        if (!propertiesInitializedSuccessfully) {
            FullHueObject fullSensor = getSensor();
            if (fullSensor != null) {
                String softwareVersion = fullSensor.getSoftwareVersion();
                if (softwareVersion != null) {
                    updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, softwareVersion);
                }
                String modelId = fullSensor.getNormalizedModelID();
                if (modelId != null) {
                    updateProperty(Thing.PROPERTY_MODEL_ID, modelId);
                }
                updateProperty(Thing.PROPERTY_VENDOR, fullSensor.getManufacturerName());
                updateProperty(PRODUCT_NAME, fullSensor.getProductName());
                String uniqueID = fullSensor.getUniqueID();
                if (uniqueID != null) {
                    updateProperty(UNIQUE_ID, uniqueID);
                }
                propertiesInitializedSuccessfully = true;
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Hue sensor handler disposes. Unregistering listener.");
        if (sensorId != null) {
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                bridgeHandler.unregisterSensorStatusListener(this);
                hueClient = null;
            }
            sensorId = null;
        }
    }

    private @Nullable FullSensor getSensor() {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler != null) {
            return bridgeHandler.getSensorById(sensorId);
        }
        return null;
    }

    protected synchronized @Nullable HueClient getHueClient() {
        if (hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueClient) {
                hueClient = (HueClient) handler;
                hueClient.registerSensorStatusListener(this);
            } else {
                return null;
            }
        }
        return hueClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        SensorConfigUpdate configUpdate = doConfigurationUpdate(configurationParameters);
        if (configurationParameters.containsKey(CONFIG_ON)) {
            configUpdate.setOn(Boolean.TRUE.equals(configurationParameters.get(CONFIG_ON)));
        }

        if (!configUpdate.isEmpty()) {
            HueClient hueBridge = getHueClient();
            if (hueBridge == null) {
                logger.warn("hue bridge handler not found. Cannot handle configuration update without bridge.");
                return;
            }

            FullSensor sensor = getSensor();
            if (sensor == null) {
                logger.debug("hue sensor not known on bridge. Cannot handle command.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-wrong-sensor-id");
                return;
            }

            hueBridge.updateSensorConfig(sensor, configUpdate);
        }

        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public void onSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor) {
        logger.trace("onSensorStateChanged() was called");

        if (!sensor.getId().equals(sensorId)) {
            logger.trace("Received state change for another handler's sensor ({}). Will be ignored.", sensor.getId());
            return;
        }

        initializeProperties();

        if (Boolean.TRUE.equals(sensor.getConfig().get(CONFIG_REACHABLE))) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
        }

        // update generic sensor config
        Configuration config = editConfiguration();
        if (sensor.getConfig().containsKey(CONFIG_ON)) {
            config.put(CONFIG_ON, sensor.getConfig().get(CONFIG_ON));
        }

        // update specific sensor config
        doSensorStateChanged(bridge, sensor, config);

        Object lastUpdated = sensor.getState().get(STATE_LAST_UPDATED);
        if (lastUpdated != null) {
            try {
                updateState(CHANNEL_LAST_UPDATED,
                        new DateTimeType(ZonedDateTime.ofInstant(
                                LocalDateTime.parse(String.valueOf(lastUpdated), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                                ZoneOffset.UTC, ZoneId.systemDefault())));
            } catch (DateTimeParseException e) {
                // do nothing
            }
        }

        Object battery = sensor.getConfig().get(CONFIG_BATTERY);
        if (battery != null) {
            DecimalType batteryLevel = DecimalType.valueOf(String.valueOf(battery));
            updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            updateState(CHANNEL_BATTERY_LOW, batteryLevel.intValue() <= 10 ? OnOffType.ON : OnOffType.OFF);
        }

        updateConfiguration(config);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        HueClient handler = getHueClient();
        if (handler != null) {
            FullSensor sensor = handler.getSensorById(sensorId);
            if (sensor != null) {
                onSensorStateChanged(null, sensor);
            }
        }
    }

    /**
     * Handles the sensors configuration change.
     *
     * @param configurationParameters
     * @return
     */
    protected abstract SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters);

    /**
     * Handles the sensor change. Implementation should also update sensor-specific configuration that changed since the
     * last update.
     *
     * @param bridge the bridge
     * @param sensor the sensor
     * @param config the configuration in which to update the config states of the sensor
     */
    protected abstract void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config);

    @Override
    public void onSensorRemoved(@Nullable HueBridge bridge, FullSensor sensor) {
        if (sensor.getId().equals(sensorId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
        }
    }

    @Override
    public void onSensorGone(@Nullable HueBridge bridge, FullSensor sensor) {
        if (sensor.getId().equals(sensorId)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/offline.conf-error-wrong-sensor-id");
        }
    }

    @Override
    public void onSensorAdded(@Nullable HueBridge bridge, FullSensor sensor) {
        if (sensor.getId().equals(sensorId)) {
            onSensorStateChanged(bridge, sensor);
        }
    }
}
