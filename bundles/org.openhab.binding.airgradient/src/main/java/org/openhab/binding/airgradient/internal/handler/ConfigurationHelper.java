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
package org.openhab.binding.airgradient.internal.handler;

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airgradient.internal.model.LocalConfiguration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Helper class to reduce code duplication across things.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ConfigurationHelper {

    public static Map<String, String> createProperties(LocalConfiguration configuration) {
        Map<String, String> properties = new HashMap<>(4);

        String model = configuration.model;
        if (model != null) {
            properties.put(Thing.PROPERTY_MODEL_ID, model);
        }

        return properties;
    }

    public static final String CHANNEL_POST_TO_CLOUD = "post-to-cloud";

    public static Map<String, State> createStates(LocalConfiguration configuration) {
        Map<String, State> states = new HashMap<>(11);

        states.put(CHANNEL_COUNTRY_CODE, toStringType(configuration.country));
        states.put(CHANNEL_PM_STANDARD, toStringType(configuration.pmStandard));
        states.put(CHANNEL_ABC_DAYS, toQuantityType(configuration.abcDays, Units.DAY));
        states.put(CHANNEL_TVOC_LEARNING_OFFSET, toQuantityType(configuration.tvocLearningOffset, Units.ONE));
        states.put(CHANNEL_NOX_LEARNING_OFFSET, toQuantityType(configuration.noxLearningOffset, Units.ONE));
        states.put(CHANNEL_MQTT_BROKER_URL, toStringType(configuration.mqttBrokerUrl));
        states.put(CHANNEL_TEMPERATURE_UNIT, toStringType(configuration.temperatureUnit));
        states.put(CHANNEL_CONFIGURATION_CONTROL, toStringType(configuration.configurationControl));
        states.put(CHANNEL_LED_BAR_BRIGHTNESS, toQuantityType(configuration.ledBarBrightness, Units.ONE));
        states.put(CHANNEL_DISPLAY_BRIGHTNESS, toQuantityType(configuration.displayBrightness, Units.ONE));
        states.put(CHANNEL_POST_TO_CLOUD, toOnOffType(configuration.postDataToAirGradient));
        states.put(CHANNEL_MODEL, toStringType(configuration.model));

        return states;
    }

    private static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    private static State toStringType(@Nullable String value) {
        return value == null ? UnDefType.NULL : StringType.valueOf(value);
    }

    private static State toOnOffType(@Nullable Boolean value) {
        return value == null ? UnDefType.NULL : OnOffType.from(value);
    }
}
