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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT vacuum, following the https://www.home-assistant.io/components/vacuum.mqtt/ specification.
 *
 * @author Stefan Triller - Initial contribution
 * @author Anton Kharuzhyi - Make it compilant with the Specification
 */
@NonNullByDefault
public class Vacuum extends AbstractComponent<Vacuum.ChannelConfiguration> {
    public static final String FEATURE_START = "start";
    public static final String FEATURE_STOP = "stop";
    public static final String FEATURE_PAUSE = "pause";
    public static final String FEATURE_RETURN_HOME = "return_home"; // Return to base/dock
    public static final String FEATURE_BATTERY = "battery";
    public static final String FEATURE_STATUS = "status";
    public static final String FEATURE_LOCATE = "locate"; // Locate the vacuum (typically by playing a song)
    public static final String FEATURE_CLEAN_SPOT = "clean_spot"; // Initialize a spot cleaning cycle
    public static final String FEATURE_FAN_SPEED = "fan_speed";
    public static final String FEATURE_SEND_COMMAND = "send_command";

    public static final String STATE_CLEANING = "cleaning";
    public static final String STATE_DOCKED = "docked";
    public static final String STATE_PAUSED = "paused";
    public static final String STATE_IDLE = "idle";
    public static final String STATE_RETURNING = "returning";
    public static final String STATE_ERROR = "error";

    public static final String COMMAND_CH_ID = "command";
    public static final String FAN_SPEED_CH_ID = "fan-speed";
    public static final String FAN_SPEED_CH_ID_DEPRECATED = "fanSpeed";
    public static final String CUSTOM_COMMAND_CH_ID = "custom-command";
    public static final String CUSTOM_COMMAND_CH_ID_DEPRECATED = "customCommand";
    public static final String BATTERY_LEVEL_CH_ID = "battery-level";
    public static final String BATTERY_LEVEL_CH_ID_DEPRECATED = "batteryLevel";
    public static final String JSON_ATTRIBUTES_CH_ID = "json-attributes";
    public static final String JSON_ATTRIBUTES_CH_ID_DEPRECATED = "jsonAttributes";
    public static final String STATE_CH_ID = "state";

    private static final String STATE_TEMPLATE = "{{ value_json.state }}";
    private static final String OFF = "off";

    private static final Logger LOGGER = LoggerFactory.getLogger(Vacuum.class);

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Vacuum");
        }

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;

        @SerializedName("fan_speed_list")
        protected @Nullable List<String> fanSpeedList;
        @SerializedName("fan_speed_template")
        protected @Nullable String fanSpeedTemplate;
        @SerializedName("fan_speed_topic")
        protected @Nullable String fanSpeedTopic;

        @SerializedName("payload_clean_spot")
        protected String payloadCleanSpot = "clean_spot";
        @SerializedName("payload_locate")
        protected String payloadLocate = "locate";
        @SerializedName("payload_pause")
        protected String payloadPause = "pause";
        @SerializedName("payload_return_to_base")
        protected String payloadReturnToBase = "return_to_base";
        @SerializedName("payload_start")
        protected String payloadStart = "start";
        @SerializedName("payload_stop")
        protected String payloadStop = "stop";

        @SerializedName("send_command_topic")
        protected @Nullable String sendCommandTopic;

        @SerializedName("set_fan_speed_topic")
        protected @Nullable String setFanSpeedTopic;

        @SerializedName("supported_features")
        protected List<String> supportedFeatures = List.of(FEATURE_START, FEATURE_STOP, FEATURE_RETURN_HOME,
                FEATURE_STATUS, FEATURE_BATTERY, FEATURE_CLEAN_SPOT);

        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
    }

    /**
     * Creates component based on generic configuration and component configuration type.
     *
     * @param componentConfiguration generic componentConfiguration with not parsed JSON config
     */
    public Vacuum(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);
        final ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        final var supportedFeatures = channelConfiguration.supportedFeatures;

        final List<String> commands = new ArrayList<>();
        addPayloadToList(supportedFeatures, FEATURE_CLEAN_SPOT, channelConfiguration.payloadCleanSpot, commands);
        addPayloadToList(supportedFeatures, FEATURE_LOCATE, channelConfiguration.payloadLocate, commands);
        addPayloadToList(supportedFeatures, FEATURE_RETURN_HOME, channelConfiguration.payloadReturnToBase, commands);
        addPayloadToList(supportedFeatures, FEATURE_START, channelConfiguration.payloadStart, commands);
        addPayloadToList(supportedFeatures, FEATURE_STOP, channelConfiguration.payloadStop, commands);
        addPayloadToList(supportedFeatures, FEATURE_PAUSE, channelConfiguration.payloadPause, commands);

        buildOptionalChannel(COMMAND_CH_ID, ComponentChannelType.STRING, new TextValue(commands.toArray(new String[0])),
                updateListener, null, channelConfiguration.commandTopic, null, null);

        final var fanSpeedList = channelConfiguration.fanSpeedList;
        if (supportedFeatures.contains(FEATURE_FAN_SPEED) && fanSpeedList != null && !fanSpeedList.isEmpty()) {
            var fanSpeedCommandList = fanSpeedList.toArray(new String[0]);
            if (!fanSpeedList.contains(OFF)) {
                fanSpeedList.add(OFF); // Off value is used when cleaning if OFF
            }
            var fanSpeedValue = new TextValue(fanSpeedList.toArray(new String[0]), fanSpeedCommandList);
            if (supportedFeatures.contains(FEATURE_STATUS)) {
                buildOptionalChannel(newStyleChannels ? FAN_SPEED_CH_ID : FAN_SPEED_CH_ID_DEPRECATED,
                        ComponentChannelType.STRING, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, "{{ value_json.fan_speed }}",
                        channelConfiguration.stateTopic);
            } else {
                buildOptionalChannel(newStyleChannels ? FAN_SPEED_CH_ID : FAN_SPEED_CH_ID_DEPRECATED,
                        ComponentChannelType.STRING, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, null, null);
            }
        }

        if (supportedFeatures.contains(FEATURE_SEND_COMMAND)) {
            buildOptionalChannel(newStyleChannels ? CUSTOM_COMMAND_CH_ID : CUSTOM_COMMAND_CH_ID_DEPRECATED,
                    ComponentChannelType.STRING, new TextValue(), updateListener, null,
                    channelConfiguration.sendCommandTopic, null, null);
        }

        if (supportedFeatures.contains(FEATURE_STATUS)) {
            // state key is mandatory
            buildOptionalChannel(STATE_CH_ID, ComponentChannelType.STRING,
                    new TextValue(new String[] { STATE_CLEANING, STATE_DOCKED, STATE_PAUSED, STATE_IDLE,
                            STATE_RETURNING, STATE_ERROR }),
                    updateListener, null, null, STATE_TEMPLATE, channelConfiguration.stateTopic);
            if (supportedFeatures.contains(FEATURE_BATTERY)) {
                buildOptionalChannel(newStyleChannels ? BATTERY_LEVEL_CH_ID : BATTERY_LEVEL_CH_ID_DEPRECATED,
                        ComponentChannelType.DIMMER,
                        new PercentageValue(BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.ONE, null, null, null),
                        updateListener, null, null, "{{ value_json.battery_level }}", channelConfiguration.stateTopic);
            }
        }

        finalizeChannels();
    }

    // Overridden to use deprecated channel ID
    @Override
    protected void addJsonAttributesChannel() {
        buildOptionalChannel(newStyleChannels ? JSON_ATTRIBUTES_CH_ID : JSON_ATTRIBUTES_CH_ID_DEPRECATED,
                ComponentChannelType.STRING, new TextValue(), componentConfiguration.getUpdateListener(), null, null,
                channelConfiguration.getJsonAttributesTemplate(), channelConfiguration.getJsonAttributesTopic());
    }

    @Nullable
    private ComponentChannel buildOptionalChannel(String channelId, ComponentChannelType channelType, Value valueState,
            ChannelStateUpdateListener channelStateUpdateListener, @Nullable String commandTemplate,
            @Nullable String commandTopic, @Nullable String stateTemplate, @Nullable String stateTopic) {
        if ((commandTopic != null && !commandTopic.isBlank()) || (stateTopic != null && !stateTopic.isBlank())) {
            return buildChannel(channelId, channelType, valueState, getName(), channelStateUpdateListener)
                    .stateTopic(stateTopic, stateTemplate, channelConfiguration.getValueTemplate())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos(),
                            commandTemplate)
                    .build();
        }
        return null;
    }

    private void addPayloadToList(List<String> supportedFeatures, String feature, String payload, List<String> list) {
        if (supportedFeatures.contains(feature) && !payload.isEmpty()) {
            list.add(payload);
        }
    }
}
