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
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;
import org.openhab.core.library.unit.Units;

/**
 * A MQTT climate component, following the https://www.home-assistant.io/components/climate.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 * @author Anton Kharuzhy - Implementation
 * @author Vaclav Cejka - added support for humidity and preset_modes
 */
@NonNullByDefault
public class Climate extends AbstractComponent<Climate.Configuration> {
    public static final String ACTION_CH_ID = "action";
    public static final String CURRENT_HUMIDITY_CH_ID = "current-humidity";
    public static final String CURRENT_TEMPERATURE_CH_ID = "current-temperature";
    public static final String FAN_MODE_CH_ID = "fan-mode";
    public static final String MODE_CH_ID = "mode";
    public static final String PRESET_MODE_CH_ID = "preset-mode";
    public static final String SWING_CH_ID = "swing";
    public static final String TARGET_HUMIDITY_CH_ID = "target-humidity";
    public static final String TEMPERATURE_CH_ID = "temperature";
    public static final String TEMPERATURE_HIGH_CH_ID = "temperature-high";
    public static final String TEMPERATURE_LOW_CH_ID = "temperature-low";
    public static final String POWER_CH_ID = "power";

    private static final String ACTION_OFF = "off";
    private static final List<String> ACTION_MODES = List.of(ACTION_OFF, "heating", "cooling", "drying", "idle", "fan");

    private static final String FAN_MODE_AUTO = "auto";
    private static final String FAN_MODE_LOW = "low";
    private static final String FAN_MODE_MEDIUM = "medium";
    private static final String FAN_MODE_HIGH = "high";

    private static final Map<String, String> FAN_MODE_LABELS = Map.of(FAN_MODE_AUTO,
            "@text/state.climate.fan-mode.auto", FAN_MODE_LOW, "@text/state.climate.fan-mode.low", FAN_MODE_MEDIUM,
            "@text/state.climate.fan-mode.medium", FAN_MODE_HIGH, "@text/state.climate.fan-mode.high");

    private static final String MODE_AUTO = "auto";
    private static final String MODE_OFF = "off";
    private static final String MODE_COOL = "cool";
    private static final String MODE_HEAT = "heat";
    private static final String MODE_DRY = "dry";
    private static final String MODE_FAN_ONLY = "fan_only";

    private static final Map<String, String> MODE_LABELS = Map.of(MODE_AUTO, "@text/state.climate.mode.auto", MODE_OFF,
            "@text/state.climate.mode.off", MODE_COOL, "@text/state.climate.mode.cool", MODE_HEAT,
            "@text/state.climate.mode.heat", MODE_DRY, "@text/state.climate.mode.dry", MODE_FAN_ONLY,
            "@text/state.climate.mode.fan-only");

    private static final String SWING_MODE_ON = "on";
    private static final String SWING_MODE_OFF = "off";

    private static final Map<String, String> SWING_MODE_LABELS = Map.of(SWING_MODE_ON,
            "@text/state.climate.swing-mode.on", SWING_MODE_OFF, "@text/state.climate.swing-mode.off");

    private static final String PRESET_MODE_NONE = "none";

    public static class Configuration extends EntityConfiguration {
        private final boolean retain, optimistic;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT HVAC");
            retain = getBoolean("retain");
            optimistic = getBoolean("optimistic");
        }

        @Nullable
        Value getCurrentHumidityTemplate() {
            return getOptionalValue("current_humidity_template");
        }

        @Nullable
        String getCurrentHumidityTopic() {
            return getOptionalString("current_humidity_topic");
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
        Value getFanModeCommandTemplate() {
            return getOptionalValue("fan_mode_command_template");
        }

        @Nullable
        String getFanModeCommandTopic() {
            return getOptionalString("fan_mode_command_topic");
        }

        List<String> getFanModes() {
            return getStringList("fan_modes");
        }

        @Nullable
        Value getFanModeStateTemplate() {
            return getOptionalValue("fan_mode_state_template");
        }

        @Nullable
        String getFanModeStateTopic() {
            return getOptionalString("fan_mode_state_topic");
        }

        @Nullable
        Value getTargetHumidityCommandTemplate() {
            return getOptionalValue("target_humidity_command_template");
        }

        @Nullable
        String getTargetHumidityCommandTopic() {
            return getOptionalString("target_humidity_command_topic");
        }

        double getMinHumidity() {
            return getDouble("min_humidity");
        }

        double getMaxHumidity() {
            return getDouble("max_humidity");
        }

        @Nullable
        Value getTargetHumidityStateTemplate() {
            return getOptionalValue("target_humidity_state_template");
        }

        @Nullable
        String getTargetHumidityStateTopic() {
            return getOptionalString("target_humidity_state_topic");
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
            return optimistic;
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
            return retain;
        }

        @Nullable
        Value getActionTemplate() {
            return getOptionalValue("action_template");
        }

        @Nullable
        String getActionTopic() {
            return getOptionalString("action_topic");
        }

        @Nullable
        String getPresetModeCommandTopic() {
            return getOptionalString("preset_mode_command_topic");
        }

        List<String> getPresetModes() {
            return getStringList("preset_modes");
        }

        @Nullable
        Value getPresetModeCommandTemplate() {
            return getOptionalValue("preset_mode_command_template");
        }

        @Nullable
        String getPresetModeStateTopic() {
            return getOptionalString("preset_mode_state_topic");
        }

        @Nullable
        Value getPresetModeValueTemplate() {
            return getOptionalValue("preset_mode_value_template");
        }

        @Nullable
        Value getSwingHorizontalModeCommandTemplate() {
            return getOptionalValue("swing_horizontal_mode_command_template");
        }

        @Nullable
        String getSwingHorizontalModeCommandTopic() {
            return getOptionalString("swing_horizontal_mode_command_topic");
        }

        List<String> getSwingHorizontalModes() {
            return getStringList("swing_horizontal_modes");
        }

        @Nullable
        Value getSwingHorizontalModeStateTemplate() {
            return getOptionalValue("swing_horizontal_mode_state_template");
        }

        @Nullable
        String getSwingHorizontalModeStateTopic() {
            return getOptionalString("swing_horizontal_mode_state_topic");
        }

        @Nullable
        Value getSwingModeCommandTemplate() {
            return getOptionalValue("swing_mode_command_template");
        }

        @Nullable
        String getSwingModeCommandTopic() {
            return getOptionalString("swing_mode_command_topic");
        }

        List<String> getSwingModes() {
            return getStringList("swing_modes");
        }

        @Nullable
        Value getSwingModeStateTemplate() {
            return getOptionalValue("swing_mode_state_template");
        }

        @Nullable
        String getSwingModeStateTopic() {
            return getOptionalString("swing_mode_state_topic");
        }

        @Nullable
        Double getTempInitial() {
            return getOptionalDouble("initial");
        }

        @Nullable
        Double getMinTemp() {
            return getOptionalDouble("min_temp");
        }

        @Nullable
        Double getMaxTemp() {
            return getOptionalDouble("max_temp");
        }

        double getTempStep() {
            return getDouble("temp_step");
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
        Value getTemperatureHighCommandTemplate() {
            return getOptionalValue("temperature_high_command_template");
        }

        @Nullable
        String getTemperatureHighCommandTopic() {
            return getOptionalString("temperature_high_command_topic");
        }

        @Nullable
        Value getTemperatureHighStateTemplate() {
            return getOptionalValue("temperature_high_state_template");
        }

        @Nullable
        String getTemperatureHighStateTopic() {
            return getOptionalString("temperature_high_state_topic");
        }

        @Nullable
        Value getTemperatureLowCommandTemplate() {
            return getOptionalValue("temperature_low_command_template");
        }

        @Nullable
        String getTemperatureLowCommandTopic() {
            return getOptionalString("temperature_low_command_topic");
        }

        @Nullable
        Value getTemperatureLowStateTemplate() {
            return getOptionalValue("temperature_low_state_template");
        }

        @Nullable
        String getTemperatureLowStateTopic() {
            return getOptionalString("temperature_low_state_topic");
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

    public Climate(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        TemperatureUnit temperatureUnit = getTemperatureUnit(config.getTemperatureUnit());

        Double configPrecision = config.getPrecision();
        BigDecimal precision = configPrecision != null ? BigDecimal.valueOf(configPrecision)
                : temperatureUnit.getDefaultPrecision();
        final ChannelStateUpdateListener updateListener = componentContext.getUpdateListener();

        buildOptionalChannel(ACTION_CH_ID, ComponentChannelType.STRING,
                new TextValue(ACTION_MODES.toArray(new String[0])), "Action", updateListener, null, null,
                config.getActionTemplate(), config.getActionTopic());

        buildOptionalChannel(CURRENT_HUMIDITY_CH_ID, ComponentChannelType.HUMIDITY,
                new NumberValue(new BigDecimal(0), new BigDecimal(100), null, Units.PERCENT), "Current Humidity",
                updateListener, null, null, config.getCurrentHumidityTemplate(), config.getCurrentHumidityTopic());

        buildOptionalChannel(CURRENT_TEMPERATURE_CH_ID, ComponentChannelType.TEMPERATURE,
                new NumberValue(null, null, precision, temperatureUnit.getUnit()), "Current Temperature",
                updateListener, null, null, config.getCurrentTemperatureTemplate(),
                config.getCurrentTemperatureTopic());

        Map<String, String> modes = config.getFanModes().stream()
                .collect(Collectors.toMap(m -> m, m -> m, (a, b) -> a, LinkedHashMap::new));
        buildOptionalChannel(FAN_MODE_CH_ID, ComponentChannelType.STRING,
                new TextValue(modes, modes, FAN_MODE_LABELS, FAN_MODE_LABELS), "Fan Mode", updateListener,
                config.getFanModeCommandTemplate(), config.getFanModeCommandTopic(), config.getFanModeStateTemplate(),
                config.getFanModeStateTopic());

        modes = config.getModes().stream().collect(Collectors.toMap(m -> m, m -> m, (a, b) -> a, LinkedHashMap::new));
        buildOptionalChannel(MODE_CH_ID, ComponentChannelType.STRING,
                new TextValue(modes, modes, MODE_LABELS, MODE_LABELS), "Mode", updateListener,
                config.getModeCommandTemplate(), config.getModeCommandTopic(), config.getModeStateTemplate(),
                config.getModeStateTopic());

        List<String> presetModes = new ArrayList<>(config.getPresetModes());
        if (!presetModes.isEmpty()) {
            presetModes.add(0, PRESET_MODE_NONE);
        }
        buildOptionalChannel(PRESET_MODE_CH_ID, ComponentChannelType.STRING,
                new TextValue(presetModes.toArray(new String[0])), "Preset", updateListener,
                config.getPresetModeCommandTemplate(), config.getPresetModeCommandTopic(),
                config.getPresetModeValueTemplate(), config.getPresetModeStateTopic());

        modes = config.getSwingModes().stream()
                .collect(Collectors.toMap(m -> m, m -> m, (a, b) -> a, LinkedHashMap::new));
        buildOptionalChannel(SWING_CH_ID, ComponentChannelType.STRING,
                new TextValue(modes, modes, SWING_MODE_LABELS, SWING_MODE_LABELS), "Swing Mode", updateListener,
                config.getSwingModeCommandTemplate(), config.getSwingModeCommandTopic(),
                config.getSwingModeStateTemplate(), config.getSwingModeStateTopic());

        buildOptionalChannel(TARGET_HUMIDITY_CH_ID, ComponentChannelType.HUMIDITY,
                new NumberValue(BigDecimal.valueOf(config.getMinHumidity()),
                        BigDecimal.valueOf(config.getMaxHumidity()), null, Units.PERCENT),
                "Target Humidity", updateListener, config.getTargetHumidityCommandTemplate(),
                config.getTargetHumidityCommandTopic(), config.getTargetHumidityStateTemplate(),
                config.getTargetHumidityStateTopic());

        Double configMinTemp = config.getMinTemp(), configMaxTemp = config.getMaxTemp(),
                configTempStep = config.getTempStep();
        BigDecimal minTemp = configMinTemp != null ? BigDecimal.valueOf(configMinTemp) : null;
        BigDecimal maxTemp = configMaxTemp != null ? BigDecimal.valueOf(configMaxTemp) : null;
        BigDecimal tempStep = BigDecimal.valueOf(configTempStep);
        buildOptionalChannel(TEMPERATURE_CH_ID, ComponentChannelType.TEMPERATURE,
                new NumberValue(minTemp, maxTemp, tempStep, temperatureUnit.getUnit()), "Target Temperature",
                updateListener, config.getTemperatureCommandTemplate(), config.getTemperatureCommandTopic(),
                config.getTemperatureStateTemplate(), config.getTemperatureStateTopic());

        buildOptionalChannel(TEMPERATURE_HIGH_CH_ID, ComponentChannelType.TEMPERATURE,
                new NumberValue(minTemp, maxTemp, tempStep, temperatureUnit.getUnit()), "Highest Allowed Temperature",
                updateListener, config.getTemperatureHighCommandTemplate(), config.getTemperatureHighCommandTopic(),
                config.getTemperatureHighStateTemplate(), config.getTemperatureHighStateTopic());

        buildOptionalChannel(TEMPERATURE_LOW_CH_ID, ComponentChannelType.TEMPERATURE,
                new NumberValue(minTemp, maxTemp, tempStep, temperatureUnit.getUnit()), "Lowest Allowed Temperature",
                updateListener, config.getTemperatureLowCommandTemplate(), config.getTemperatureLowCommandTopic(),
                config.getTemperatureLowStateTemplate(), config.getTemperatureLowStateTopic());

        buildOptionalChannel(POWER_CH_ID, ComponentChannelType.SWITCH,
                new OnOffValue(config.getPayloadOn(), config.getPayloadOff()), "Power", updateListener,
                config.getPowerCommandTemplate(), config.getPowerCommandTopic(), null, null);

        finalizeChannels();
    }

    @Nullable
    private ComponentChannel buildOptionalChannel(String channelId, ComponentChannelType channelType,
            org.openhab.binding.mqtt.generic.values.Value valueState, String name,
            ChannelStateUpdateListener channelStateUpdateListener, @Nullable Value commandTemplate,
            @Nullable String commandTopic, @Nullable Value stateTemplate, @Nullable String stateTopic) {
        if ((commandTopic != null && !commandTopic.isBlank()) || (stateTopic != null && !stateTopic.isBlank())) {
            return buildChannel(channelId, channelType, valueState, name, channelStateUpdateListener)
                    .stateTopic(stateTopic, stateTemplate, config.getValueTemplate())
                    .commandTopic(commandTopic, config.isRetain(), config.getQos(), commandTemplate)
                    .inferOptimistic(config.isOptimistic()).build();
        }
        return null;
    }
}
