/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;

/**
 * A MQTT Humidifier, following the https://www.home-assistant.io/integrations/water_heater.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class WaterHeater extends AbstractComponent<WaterHeater.Configuration> {
    public static final String CURRENT_TEMPERATURE_CHANNEL_ID = "current-temperature";
    public static final String MODE_CHANNEL_ID = "mode";
    public static final String STATE_CHANNEL_ID = "state";
    public static final String TARGET_TEMPERATURE_CHANNEL_ID = "target-temperature";

    public static final String PLATFORM_WATER_HEATER = "water_heater";

    public static final String MODE_OFF = "off";
    public static final String MODE_ECO = "eco";
    public static final String MODE_ELECTRIC = "electric";
    public static final String MODE_GAS = "gas";
    public static final String MODE_HEAT_PUMP = "heat_pump";
    public static final String MODE_HIGH_DEMAND = "high_demand";
    public static final String MODE_PERFORMANCE = "performance";
    public static final List<String> DEFAULT_MODES = List.of(MODE_OFF, MODE_ECO, MODE_ELECTRIC, MODE_GAS,
            MODE_HEAT_PUMP, MODE_HIGH_DEMAND, MODE_PERFORMANCE);

    private static final Map<String, String> MODE_LABELS = Map.of(MODE_OFF, "@text/state.water-heater.mode.off",
            MODE_ECO, "@text/state.water-heater.mode.eco", MODE_ELECTRIC, "@text/state.water-heater.mode.electric",
            MODE_GAS, "@text/state.water-heater.mode.gas", MODE_HEAT_PUMP, "@text/state.water-heater.mode.heat-pump",
            MODE_HIGH_DEMAND, "@text/state.water-heater.mode.high-demand", MODE_PERFORMANCE,
            "@text/state.water-heater.mode.performance");

    public static final String TEMPERATURE_UNIT_C = "C";
    public static final String TEMPERATURE_UNIT_F = "F";

    public static class Configuration extends EntityConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Water Heater");
        }

        @Nullable
        Value getCurrentTemperatureTemplate() {
            return getOptionalValue("current_temperature_template");
        }

        @Nullable
        String getCurrentTemperatureTopic() {
            return getOptionalString("current_temperature_topic");
        }

        @Nullable
        Value getModeCommandTemplate() {
            return getOptionalValue("mode_command_template");
        }

        @Nullable
        String getModeCommandTopic() {
            return getOptionalString("mode_command_topic");
        }

        List<String> getModes() {
            return getStringList("modes");
        }

        @Nullable
        Value getModeStateTemplate() {
            return getOptionalValue("mode_state_template");
        }

        @Nullable
        String getModeStateTopic() {
            return getOptionalString("mode_state_topic");
        }

        boolean isOptimistic() {
            return getBoolean("optimistic");
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        String getPayloadOff() {
            return getString("payload_off");
        }

        @Nullable
        String getPowerCommandTopic() {
            return getOptionalString("power_command_topic");
        }

        @Nullable
        Value getPowerCommandTemplate() {
            return getOptionalValue("power_command_template");
        }

        @Nullable
        Double getPrecision() {
            // Precision can be an int or a float
            Object precision = config.get("precision");
            if (precision instanceof Integer intValue) {
                return intValue.doubleValue();
            }
            return (Double) precision;
        }

        boolean isRetain() {
            return getBoolean("retain");
        }

        @Nullable
        Integer getInitial() {
            return getOptionalInt("initial");
        }

        @Nullable
        Double getMaxTemp() {
            return getOptionalDouble("max_temp");
        }

        @Nullable
        Double getMinTemp() {
            return getOptionalDouble("min_temp");
        }

        @Nullable
        Value getTemperatureCommandTemplate() {
            return getOptionalValue("temperature_command_template");
        }

        @Nullable
        String getTemperatureCommandTopic() {
            return getOptionalString("temperature_command_topic");
        }

        @Nullable
        Value getTemperatureStateTemplate() {
            return getOptionalValue("temperature_state_template");
        }

        @Nullable
        String getTemperatureStateTopic() {
            return getOptionalString("temperature_state_topic");
        }

        @Nullable
        String getTemperatureUnit() {
            return getOptionalString("temperature_unit");
        }

        @Nullable
        Value getValueTemplate() {
            return getOptionalValue("value_template");
        }
    }

    public WaterHeater(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        TemperatureUnit temperatureUnit = getTemperatureUnit(config.getTemperatureUnit());
        Double configPrecision = config.getPrecision();
        BigDecimal precision = configPrecision != null ? BigDecimal.valueOf(configPrecision)
                : temperatureUnit.getDefaultPrecision();

        List<String> modes = config.getModes();
        List<String> onStates = new ArrayList<>(modes);
        onStates.remove(MODE_OFF);

        List<String> unsupportedModes = onStates.stream().filter(mode -> !DEFAULT_MODES.contains(mode))
                .collect(Collectors.toList());
        if (!unsupportedModes.isEmpty()) {
            throw new ConfigurationException("unsupported modes: " + unsupportedModes.toString());
        }

        Value valueTemplate = config.getValueTemplate();

        boolean optimistic = config.isOptimistic();
        String powerCommandTopic = config.getPowerCommandTopic();
        if (powerCommandTopic != null) {
            buildChannel(STATE_CHANNEL_ID, ComponentChannelType.SWITCH,
                    new OnOffValue(onStates.toArray(new String[0]), new String[] { MODE_OFF }, config.getPayloadOn(),
                            config.getPayloadOff()),
                    "State", componentContext.getUpdateListener())
                    .stateTopic(config.getModeStateTopic(), config.getModeStateTemplate(), valueTemplate)
                    .commandTopic(config.getPowerCommandTopic(), config.isRetain(), config.getQos(),
                            config.getPowerCommandTemplate())
                    .inferOptimistic(optimistic).build();
        }

        String modeCommandTopic = config.getModeCommandTopic();
        String modeStateTopic = config.getModeStateTopic();
        if (modeCommandTopic != null || modeStateTopic != null) {
            Map<String, String> modesMapping = modes.stream()
                    .collect(Collectors.toMap(m -> m, m -> m, (a, b) -> a, LinkedHashMap::new));
            buildChannel(MODE_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(modesMapping, modesMapping, MODE_LABELS, MODE_LABELS), "Mode",
                    componentContext.getUpdateListener())
                    .stateTopic(modeStateTopic, config.getModeStateTemplate(), valueTemplate)
                    .commandTopic(modeCommandTopic, config.isRetain(), config.getQos(), config.getModeCommandTemplate())
                    .inferOptimistic(optimistic).build();
        }

        String currentTemperatureTopic = config.getCurrentTemperatureTopic();
        if (currentTemperatureTopic != null) {
            buildChannel(CURRENT_TEMPERATURE_CHANNEL_ID, ComponentChannelType.TEMPERATURE,
                    new NumberValue(null, null, null, temperatureUnit.getUnit()), "Current Temperature",
                    componentContext.getUpdateListener())
                    .stateTopic(currentTemperatureTopic, config.getCurrentTemperatureTemplate(), valueTemplate).build();
        }

        String temperatureStateTopic = config.getTemperatureStateTopic();
        String temperatureCommandTopic = config.getTemperatureCommandTopic();
        if (temperatureStateTopic != null || temperatureCommandTopic != null) {
            Double configMinTemp = config.getMinTemp(), configMaxTemp = config.getMaxTemp();
            BigDecimal minTemp = configMinTemp != null ? BigDecimal.valueOf(configMinTemp) : null;
            BigDecimal maxTemp = configMaxTemp != null ? BigDecimal.valueOf(configMaxTemp) : null;
            buildChannel(TARGET_TEMPERATURE_CHANNEL_ID, ComponentChannelType.TEMPERATURE,
                    new NumberValue(minTemp, maxTemp, precision, temperatureUnit.getUnit()), "Target Temperature",
                    componentContext.getUpdateListener())
                    .stateTopic(temperatureStateTopic, config.getTemperatureStateTemplate(), valueTemplate)
                    .commandTopic(temperatureCommandTopic, config.isRetain(), config.getQos(),
                            config.getTemperatureCommandTemplate())
                    .inferOptimistic(optimistic).build();
        }

        finalizeChannels();
    }
}
