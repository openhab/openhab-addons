/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.hue.internal.HueBindingConstants.*;
import static org.openhab.binding.hue.internal.api.dto.clip1.FullSensor.*;
import static org.openhab.core.thing.Thing.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip1.FullSensor;
import org.openhab.binding.hue.internal.api.dto.clip1.SensorConfigUpdate;
import org.openhab.binding.hue.internal.api.dto.clip1.StateUpdate;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
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

    private final Logger logger = LoggerFactory.getLogger(HueSensorHandler.class);

    private @NonNullByDefault({}) String sensorId;

    private boolean configInitializedSuccessfully;
    private boolean propertiesInitializedSuccessfully;

    private @Nullable HueClient hueClient;

    protected @Nullable FullSensor lastFullSensor;

    public HueSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Hue sensor handler.");
        Bridge bridge = getBridge();
        initializeThing((bridge == null) ? null : bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(@Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);
        final String configSensorId = (String) getConfig().get(SENSOR_ID);
        if (configSensorId != null) {
            sensorId = configSensorId;
            // note: this call implicitly registers our handler as a listener on the bridge
            HueClient bridgeHandler = getHueClient();
            if (bridgeHandler != null) {
                if (bridgeStatus == ThingStatus.ONLINE) {
                    initializeProperties(bridgeHandler.getSensorById(sensorId));
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-sensor-id");
        }
    }

    private synchronized void initializeProperties(@Nullable FullSensor fullSensor) {
        if (!propertiesInitializedSuccessfully && fullSensor != null) {
            Map<String, String> properties = editProperties();
            String softwareVersion = fullSensor.getSoftwareVersion();
            if (softwareVersion != null) {
                properties.put(PROPERTY_FIRMWARE_VERSION, softwareVersion);
            }
            String modelId = fullSensor.getNormalizedModelID();
            if (modelId != null) {
                properties.put(PROPERTY_MODEL_ID, modelId);
            }
            properties.put(PROPERTY_VENDOR, fullSensor.getManufacturerName());
            properties.put(PROPERTY_PRODUCT_NAME, fullSensor.getProductName());
            String uniqueID = fullSensor.getUniqueID();
            if (uniqueID != null) {
                properties.put(UNIQUE_ID, uniqueID);
            }
            updateProperties(properties);
            propertiesInitializedSuccessfully = true;
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

    protected synchronized @Nullable HueClient getHueClient() {
        if (hueClient == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HueBridgeHandler) {
                HueClient bridgeHandler = (HueClient) handler;
                hueClient = bridgeHandler;
                bridgeHandler.registerSensorStatusListener(this);
            } else {
                return null;
            }
        }
        return hueClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        handleCommand(channelUID.getId(), command);
    }

    protected void handleCommand(String channel, Command command) {
        HueClient bridgeHandler = getHueClient();
        if (bridgeHandler == null) {
            logger.warn("Hue Bridge handler not found. Cannot handle command without bridge.");
            return;
        }

        final FullSensor sensor = lastFullSensor;
        if (sensor == null) {
            logger.debug("Hue sensor not known on bridge. Cannot handle command.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-wrong-sensor-id");
            return;
        }

        StateUpdate sensorState = new StateUpdate();
        switch (channel) {
            case STATE_STATUS:
                sensorState = sensorState.setStatus(((DecimalType) command).intValue());
                break;
            case STATE_FLAG:
                sensorState = sensorState.setFlag(OnOffType.ON.equals(command));
                break;
        }

        if (sensorState != null) {
            bridgeHandler.updateSensorState(sensor, sensorState);
        } else {
            logger.warn("Command sent to an unknown channel id: {}:{}", getThing().getUID(), channel);
        }
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
                logger.warn("Hue Bridge handler not found. Cannot handle configuration update without bridge.");
                return;
            }

            final FullSensor sensor = lastFullSensor;
            if (sensor == null) {
                logger.debug("Hue sensor not known on bridge. Cannot handle configuration update.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-wrong-sensor-id");
                return;
            }

            hueBridge.updateSensorConfig(sensor, configUpdate);
        }

        super.handleConfigurationUpdate(configurationParameters);
    }

    @Override
    public boolean onSensorStateChanged(FullSensor sensor) {
        logger.trace("onSensorStateChanged() was called");

        final FullSensor lastSensor = lastFullSensor;
        if (lastSensor == null || !Objects.equals(lastSensor.getState(), sensor.getState())) {
            lastFullSensor = sensor;
        } else {
            return true;
        }

        logger.trace("New state for sensor {}", sensorId);

        initializeProperties(sensor);

        if (Boolean.TRUE.equals(sensor.getConfig().get(CONFIG_REACHABLE))) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
        }

        // update generic sensor config
        final Configuration config = !configInitializedSuccessfully ? editConfiguration() : getConfig();
        if (sensor.getConfig().containsKey(CONFIG_ON)) {
            config.put(CONFIG_ON, sensor.getConfig().get(CONFIG_ON));
        }

        // update specific sensor config
        doSensorStateChanged(sensor, config);

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

        Object status = sensor.getState().get(STATE_STATUS);
        if (status != null) {
            try {
                DecimalType value = new DecimalType(String.valueOf(status));
                updateState(STATE_STATUS, value);
            } catch (DateTimeParseException e) {
                // do nothing
            }
        }
        Object flag = sensor.getState().get(STATE_FLAG);
        if (flag != null) {
            try {
                boolean value = Boolean.parseBoolean(String.valueOf(flag));
                updateState(CHANNEL_FLAG, OnOffType.from(value));
            } catch (DateTimeParseException e) {
                // do nothing
            }
        }

        Object battery = sensor.getConfig().get(CONFIG_BATTERY);
        if (battery != null) {
            DecimalType batteryLevel = DecimalType.valueOf(String.valueOf(battery));
            updateState(CHANNEL_BATTERY_LEVEL, batteryLevel);
            updateState(CHANNEL_BATTERY_LOW, OnOffType.from(batteryLevel.intValue() <= 10));
        }

        if (!configInitializedSuccessfully) {
            updateConfiguration(config);
            configInitializedSuccessfully = true;
        }

        return true;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        final FullSensor sensor = lastFullSensor;
        if (sensor != null) {
            onSensorStateChanged(sensor);
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
     * @param sensor the sensor
     * @param config the configuration in which to update the config states of the sensor
     */
    protected abstract void doSensorStateChanged(FullSensor sensor, Configuration config);

    @Override
    public void onSensorRemoved() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.sensor-not-reachable");
    }

    @Override
    public void onSensorGone() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "@text/offline.conf-error-wrong-sensor-id");
    }

    @Override
    public void onSensorAdded(FullSensor sensor) {
        onSensorStateChanged(sensor);
    }

    @Override
    public String getSensorId() {
        return sensorId;
    }
}
