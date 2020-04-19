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
package org.openhab.binding.hue.internal.handler.sensors;

import static org.openhab.binding.hue.internal.FullSensor.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.SensorConfigUpdate;
import org.openhab.binding.hue.internal.TemperatureConfigUpdate;
import org.openhab.binding.hue.internal.handler.HueSensorHandler;

/**
 * Temperature Sensor
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class HumidityHandler extends HueSensorHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_HUMIDITY_SENSOR);

    public HumidityHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        TemperatureConfigUpdate configUpdate = new TemperatureConfigUpdate();
        if (configurationParameters.containsKey(CONFIG_LED_INDICATION)) {
            configUpdate.setLedIndication(Boolean.TRUE.equals(configurationParameters.get(CONFIG_LED_INDICATION)));
        }
        return configUpdate;
    }

    @Override
    protected void doSensorStateChanged(@Nullable HueBridge bridge, FullSensor sensor, Configuration config) {
        Object humidity = sensor.getState().get(STATE_HUMIDITY);
        if (humidity != null) {
            BigDecimal value = new BigDecimal(String.valueOf(humidity));
            updateState(CHANNEL_HUMIDITY, new DecimalType(value.divide(new BigDecimal(100))));
        }

        if (sensor.getConfig().containsKey(CONFIG_LED_INDICATION)) {
            config.put(CONFIG_LED_INDICATION, sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK));
        }
    }
}
