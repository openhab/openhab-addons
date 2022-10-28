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
package org.openhab.binding.hue.internal.handler.sensors;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;
import static org.openhab.binding.hue.internal.dto.FullSensor.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.FullSensor;
import org.openhab.binding.hue.internal.dto.PresenceConfigUpdate;
import org.openhab.binding.hue.internal.dto.SensorConfigUpdate;
import org.openhab.binding.hue.internal.handler.HueClient;
import org.openhab.binding.hue.internal.handler.HueSensorHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presence Sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PresenceHandler extends HueSensorHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_PRESENCE_SENSOR);

    private final Logger logger = LoggerFactory.getLogger(PresenceHandler.class);

    public PresenceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(String channel, Command command) {
        HueClient hueBridge = getHueClient();
        if (hueBridge == null) {
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

        SensorConfigUpdate configUpdate = new SensorConfigUpdate();
        switch (channel) {
            case CHANNEL_ENABLED:
                configUpdate.setOn(OnOffType.ON.equals(command));
                break;
        }

        hueBridge.updateSensorConfig(sensor, configUpdate);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        PresenceConfigUpdate configUpdate = new PresenceConfigUpdate();
        if (configurationParameters.containsKey(CONFIG_LED_INDICATION)) {
            configUpdate.setLedIndication(Boolean.TRUE.equals(configurationParameters.get(CONFIG_LED_INDICATION)));
        }
        if (configurationParameters.containsKey(CONFIG_PRESENCE_SENSITIVITY)) {
            configUpdate.setSensitivity(
                    Integer.parseInt(String.valueOf(configurationParameters.get(CONFIG_PRESENCE_SENSITIVITY))));
        }
        return configUpdate;
    }

    @Override
    protected void doSensorStateChanged(FullSensor sensor, Configuration config) {
        Object presence = sensor.getState().get(STATE_PRESENCE);
        if (presence != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(presence));
            updateState(CHANNEL_PRESENCE, value ? OnOffType.ON : OnOffType.OFF);
        }

        if (sensor.getConfig().containsKey(CONFIG_LED_INDICATION)) {
            config.put(CONFIG_LED_INDICATION, sensor.getConfig().get(CONFIG_LED_INDICATION));
        }
        if (sensor.getConfig().containsKey(CONFIG_PRESENCE_SENSITIVITY)) {
            config.put(CONFIG_PRESENCE_SENSITIVITY, sensor.getConfig().get(CONFIG_PRESENCE_SENSITIVITY));
        }
        if (sensor.getConfig().containsKey(CONFIG_PRESENCE_SENSITIVITY_MAX)) {
            config.put(CONFIG_PRESENCE_SENSITIVITY_MAX, sensor.getConfig().get(CONFIG_PRESENCE_SENSITIVITY_MAX));
        }
    }
}
