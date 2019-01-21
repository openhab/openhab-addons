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
package org.openhab.binding.hue.internal.handler.sensors;

import static org.openhab.binding.hue.internal.FullSensor.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.PresenceConfigUpdate;
import org.openhab.binding.hue.internal.SensorConfigUpdate;
import org.openhab.binding.hue.internal.handler.HueSensorHandler;

/**
 * Presence Sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class PresenceHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_PRESENCE_SENSOR);

    public PresenceHandler(Thing thing) {
        super(thing);
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
    protected void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config) {
        Object presence = sensor.getState().get(STATE_PRESENCE);
        if (presence != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(presence));
            updateState(CHANNEL_PRESENCE, value ? OnOffType.ON : OnOffType.OFF);
        }

        if (sensor.getConfig().containsKey(CONFIG_LED_INDICATION)) {
            config.put(CONFIG_LED_INDICATION, sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK));
        }
        if (sensor.getConfig().containsKey(CONFIG_PRESENCE_SENSITIVITY)) {
            config.put(CONFIG_PRESENCE_SENSITIVITY, sensor.getConfig().get(CONFIG_PRESENCE_SENSITIVITY));
        }
        if (sensor.getConfig().containsKey(CONFIG_PRESENCE_SENSITIVITY_MAX)) {
            config.put(CONFIG_PRESENCE_SENSITIVITY_MAX, sensor.getConfig().get(CONFIG_PRESENCE_SENSITIVITY_MAX));
        }
    }
}
