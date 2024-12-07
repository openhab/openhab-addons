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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.library.unit.ImperialUnits;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Humidifier, following the https://www.home-assistant.io/integrations/water_heater.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class WaterHeater extends AbstractComponent<WaterHeater.ChannelConfiguration> {
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

    public static final String TEMPERATURE_UNIT_C = "C";
    public static final String TEMPERATURE_UNIT_F = "F";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Humidifier");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("power_command_topic")
        protected @Nullable String powerCommandTopic;
        @SerializedName("power_command_template")
        protected @Nullable String powerCommandTemplate;
        @SerializedName("current_temperature_topic")
        protected @Nullable String currentTemperatureTopic;
        @SerializedName("current_temperature_template")
        protected @Nullable String currentTemperatureTemplate;
        @SerializedName("temperature_command_topic")
        protected @Nullable String temperatureCommandTopic;
        @SerializedName("temperature_command_template")
        protected @Nullable String temperatureCommandTemplate;
        @SerializedName("temperature_state_topic")
        protected @Nullable String temperatureStateTopic;
        @SerializedName("temperature_state_template")
        protected @Nullable String temperatureStateTemplate;
        @SerializedName("mode_command_topic")
        protected @Nullable String modeCommandTopic;
        @SerializedName("mode_command_template")
        protected @Nullable String modeCommandTemplate;
        @SerializedName("mode_state_topic")
        protected @Nullable String modeStateTopic;
        @SerializedName("mode_state_template")
        protected @Nullable String modeStateTemplate;

        @SerializedName("device_class")
        protected @Nullable String deviceClass;
        protected String platform = "";

        protected @Nullable Integer initial;
        @SerializedName("min_temp")
        protected @Nullable BigDecimal minTemp;
        @SerializedName("max_temp")
        protected @Nullable BigDecimal maxTemp;
        protected @Nullable BigDecimal precision;
        @SerializedName("temperature_unit")
        protected @Nullable TemperatureUnit temperatureUnit;

        @SerializedName("payload_on")
        protected String payloadOn = "ON";
        @SerializedName("payload_off")
        protected String payloadOff = "OFF";
        protected List<String> modes = DEFAULT_MODES;
    }

    public WaterHeater(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        if (!PLATFORM_WATER_HEATER.equals(channelConfiguration.platform)) {
            throw new ConfigurationException("platform must be " + PLATFORM_WATER_HEATER);
        }

        TemperatureUnit temperatureUnit = channelConfiguration.temperatureUnit;
        if (channelConfiguration.temperatureUnit == null) {
            if (ImperialUnits.FAHRENHEIT.equals(componentConfiguration.getUnitProvider().getUnit(Temperature.class))) {
                temperatureUnit = TemperatureUnit.FAHRENHEIT;
            } else {
                temperatureUnit = TemperatureUnit.CELSIUS;
            }
        }
        BigDecimal precision = channelConfiguration.precision != null ? channelConfiguration.precision
                : temperatureUnit.getDefaultPrecision();

        List<String> onStates = new ArrayList<>(channelConfiguration.modes);
        onStates.remove(MODE_OFF);

        List<String> unsupportedModes = onStates.stream().filter(mode -> !DEFAULT_MODES.contains(mode))
                .collect(Collectors.toList());
        if (!unsupportedModes.isEmpty()) {
            throw new ConfigurationException("unsupported modes: " + unsupportedModes.toString());
        }

        if (channelConfiguration.powerCommandTopic != null) {
            buildChannel(STATE_CHANNEL_ID, ComponentChannelType.SWITCH,
                    new OnOffValue(onStates.toArray(new String[0]), new String[] { MODE_OFF },
                            channelConfiguration.payloadOn, channelConfiguration.payloadOff),
                    "State", componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.modeStateTopic, channelConfiguration.modeStateTemplate,
                            channelConfiguration.getValueTemplate())
                    .commandTopic(channelConfiguration.powerCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.powerCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (channelConfiguration.modeCommandTopic != null | channelConfiguration.modeStateTopic != null) {
            buildChannel(MODE_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(channelConfiguration.modes.toArray(new String[0])), "Mode",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.modeStateTopic, channelConfiguration.modeStateTemplate,
                            channelConfiguration.getValueTemplate())
                    .commandTopic(channelConfiguration.modeCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.modeCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (channelConfiguration.currentTemperatureTopic != null) {
            buildChannel(CURRENT_TEMPERATURE_CHANNEL_ID, ComponentChannelType.TEMPERATURE,
                    new NumberValue(null, null, null, temperatureUnit.getUnit()), "Current Temperature",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.currentTemperatureTopic,
                            channelConfiguration.currentTemperatureTemplate, channelConfiguration.getValueTemplate())
                    .build();
        }

        if (channelConfiguration.temperatureStateTopic != null
                || channelConfiguration.temperatureCommandTopic != null) {
            buildChannel(TARGET_TEMPERATURE_CHANNEL_ID, ComponentChannelType.TEMPERATURE,
                    new NumberValue(channelConfiguration.minTemp, channelConfiguration.maxTemp, precision,
                            temperatureUnit.getUnit()),
                    "Target Temperature", componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.temperatureStateTopic,
                            channelConfiguration.temperatureStateTemplate, channelConfiguration.getValueTemplate())
                    .commandTopic(channelConfiguration.temperatureCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.temperatureCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        finalizeChannels();
    }
}
