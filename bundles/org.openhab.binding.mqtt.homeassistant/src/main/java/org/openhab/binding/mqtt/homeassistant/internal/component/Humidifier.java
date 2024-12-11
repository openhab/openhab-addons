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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Humidifier, following the https://www.home-assistant.io/integrations/humidifier.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Humidifier extends AbstractComponent<Humidifier.ChannelConfiguration> {
    public static final String ACTION_CHANNEL_ID = "action";
    public static final String CURRENT_HUMIDITY_CHANNEL_ID = "current-humidity";
    public static final String DEVICE_CLASS_CHANNEL_ID = "device-class";
    public static final String MODE_CHANNEL_ID = "mode";
    public static final String STATE_CHANNEL_ID = "state";
    public static final String TARGET_HUMIDITY_CHANNEL_ID = "target-humidity";

    public static final String PLATFORM_HUMIDIFIER = "humidifier";
    public static final String[] ACTIONS = new String[] { "off", "humidifying", "drying", "idle" };

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Humidifier");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("action_topic")
        protected @Nullable String actionTopic;
        @SerializedName("action_template")
        protected @Nullable String actionTemplate;
        @SerializedName("command_topic")
        protected String commandTopic = "";
        @SerializedName("command_template")
        protected @Nullable String commandTemplate;
        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("state_value_template")
        protected @Nullable String stateValueTemplate;
        @SerializedName("current_humidity_topic")
        protected @Nullable String currentHumidityTopic;
        @SerializedName("current_humidity_template")
        protected @Nullable String currentHumidityTemplate;
        @SerializedName("target_humidity_command_topic")
        protected @Nullable String targetHumidityCommandTopic;
        @SerializedName("target_humidity_command_template")
        protected @Nullable String targetHumidityCommandTemplate;
        @SerializedName("target_humidity_state_topic")
        protected @Nullable String targetHumidityStateTopic;
        @SerializedName("target_humidity_state_template")
        protected @Nullable String targetHumidityStateTemplate;
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

        @SerializedName("min_humidity")
        protected BigDecimal minHumidity = BigDecimal.ZERO;
        @SerializedName("max_humidity")
        protected BigDecimal maxHumidity = new BigDecimal(100);

        @SerializedName("payload_on")
        protected String payloadOn = "ON";
        @SerializedName("payload_off")
        protected String payloadOff = "OFF";
        @SerializedName("payload_reset_humidity")
        protected String payloadResetHumidity = "None";
        @SerializedName("payload_reset_mode")
        protected String payloadResetMode = "None";
        protected @Nullable List<String> modes;
    }

    public Humidifier(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        if (!PLATFORM_HUMIDIFIER.equals(channelConfiguration.platform)) {
            throw new ConfigurationException("platform must be " + PLATFORM_HUMIDIFIER);
        }

        buildChannel(STATE_CHANNEL_ID, ComponentChannelType.SWITCH,
                new OnOffValue(channelConfiguration.payloadOn, channelConfiguration.payloadOff), "State",
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.stateValueTemplate)
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                .inferOptimistic(channelConfiguration.optimistic).build();

        buildChannel(TARGET_HUMIDITY_CHANNEL_ID, ComponentChannelType.HUMIDITY,
                new NumberValue(channelConfiguration.minHumidity, channelConfiguration.maxHumidity, null,
                        Units.PERCENT),
                "Target Humidity", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.targetHumidityStateTopic,
                        channelConfiguration.targetHumidityStateTemplate)
                .commandTopic(channelConfiguration.targetHumidityCommandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.targetHumidityCommandTemplate)
                .inferOptimistic(channelConfiguration.optimistic).build();

        if (channelConfiguration.actionTopic != null) {
            buildChannel(ACTION_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(ACTIONS), "Action",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.actionTopic, channelConfiguration.actionTemplate).build();
        }

        if (channelConfiguration.modeCommandTopic != null) {
            List<String> modes = channelConfiguration.modes;
            if (modes == null) {
                throw new ConfigurationException("modes cannot be null if mode_command_topic is specified");
            }
            TextValue modeValue = new TextValue(modes.toArray(new String[0]));
            modeValue.setNullValue(channelConfiguration.payloadResetMode);
            buildChannel(MODE_CHANNEL_ID, ComponentChannelType.STRING, modeValue, "Mode",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.modeStateTopic, channelConfiguration.modeStateTemplate)
                    .commandTopic(channelConfiguration.modeCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos(), channelConfiguration.modeCommandTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (channelConfiguration.currentHumidityTopic != null) {
            buildChannel(CURRENT_HUMIDITY_CHANNEL_ID, ComponentChannelType.HUMIDITY,
                    new NumberValue(null, null, null, Units.PERCENT), "Current Humidity",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.currentHumidityTopic, channelConfiguration.currentHumidityTemplate)
                    .build();
        }

        if (channelConfiguration.deviceClass != null) {
            TextValue deviceClassValue = new TextValue();
            deviceClassValue.update(new StringType(channelConfiguration.deviceClass));
            buildChannel(DEVICE_CLASS_CHANNEL_ID, ComponentChannelType.STRING, deviceClassValue, "Device Class",
                    componentConfiguration.getUpdateListener()).build();
        }

        finalizeChannels();
    }
}
