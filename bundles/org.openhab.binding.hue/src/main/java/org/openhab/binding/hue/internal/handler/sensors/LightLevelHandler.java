/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.FullSensor;
import org.openhab.binding.hue.internal.dto.LightLevelConfigUpdate;
import org.openhab.binding.hue.internal.dto.SensorConfigUpdate;
import org.openhab.binding.hue.internal.handler.HueSensorHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Light Level Sensor
 *
 * @author Samuel Leisering - Initial contribution
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class LightLevelHandler extends HueSensorHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_LIGHT_LEVEL_SENSOR);

    public LightLevelHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected SensorConfigUpdate doConfigurationUpdate(Map<String, Object> configurationParameters) {
        LightLevelConfigUpdate configUpdate = new LightLevelConfigUpdate();
        if (configurationParameters.containsKey(CONFIG_LED_INDICATION)) {
            configUpdate.setLedIndication(Boolean.TRUE.equals(configurationParameters.get(CONFIG_LED_INDICATION)));
        }
        if (configurationParameters.containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK)) {
            configUpdate.setThresholdDark(
                    Integer.parseInt(String.valueOf(configurationParameters.get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK))));
        }
        if (configurationParameters.containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET)) {
            configUpdate.setThresholdOffset(
                    Integer.parseInt(String.valueOf(configurationParameters.get(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET))));
        }
        return configUpdate;
    }

    @Override
    protected void doSensorStateChanged(FullSensor sensor, Configuration config) {
        Object lightLevel = sensor.getState().get(STATE_LIGHT_LEVEL);
        if (lightLevel != null) {
            BigDecimal value = new BigDecimal(String.valueOf(lightLevel));
            updateState(CHANNEL_LIGHT_LEVEL, new DecimalType(value));

            // calculate lux, according to
            // https://developers.meethue.com/documentation/supported-sensors#clip_zll_lightlevel
            double lux = Math.pow(10, (value.subtract(BigDecimal.ONE).divide(new BigDecimal(10000))).doubleValue());
            updateState(CHANNEL_ILLUMINANCE, new QuantityType<>(lux, Units.LUX));
        }

        Object dark = sensor.getState().get(STATE_DARK);
        if (dark != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(dark));
            updateState(CHANNEL_DARK, value ? OnOffType.ON : OnOffType.OFF);
        }

        Object daylight = sensor.getState().get(STATE_DAYLIGHT);
        if (daylight != null) {
            boolean value = Boolean.parseBoolean(String.valueOf(daylight));
            updateState(CHANNEL_DAYLIGHT, value ? OnOffType.ON : OnOffType.OFF);
        }

        if (sensor.getConfig().containsKey(CONFIG_LED_INDICATION)) {
            config.put(CONFIG_LED_INDICATION, sensor.getConfig().get(CONFIG_LED_INDICATION));
        }
        if (sensor.getConfig().containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK)) {
            config.put(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK, sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_DARK));
        }
        if (sensor.getConfig().containsKey(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET)) {
            config.put(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET,
                    sensor.getConfig().get(CONFIG_LIGHT_LEVEL_THRESHOLD_OFFSET));
        }
    }
}
