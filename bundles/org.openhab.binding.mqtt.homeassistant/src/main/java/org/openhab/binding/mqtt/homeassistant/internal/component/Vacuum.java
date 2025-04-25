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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static final String PAYLOAD_CLEAN_SPOT = "clean_spot";
    public static final String PAYLOAD_LOCATE = "locate";
    public static final String PAYLOAD_PAUSE = "pause";
    public static final String PAYLOAD_RETURN_TO_BASE = "return_to_base";
    public static final String PAYLOAD_START = "start";
    public static final String PAYLOAD_STOP = "stop";

    private static final Map<String, String> COMMAND_LABELS = Map.of(PAYLOAD_CLEAN_SPOT,
            "@text/command.vacuum.clean-spot", PAYLOAD_LOCATE, "@text/command.vacuum.locate", PAYLOAD_PAUSE,
            "@text/command.vacuum.pause", PAYLOAD_RETURN_TO_BASE, "@text/command.vacuum.return-to-base", PAYLOAD_START,
            "@text/command.vacuum.start", PAYLOAD_STOP, "@text/command.vacuum.stop");

    public static final String STATE_CLEANING = "cleaning";
    public static final String STATE_DOCKED = "docked";
    public static final String STATE_PAUSED = "paused";
    public static final String STATE_IDLE = "idle";
    public static final String STATE_RETURNING = "returning";
    public static final String STATE_ERROR = "error";

    private static final Map<String, String> STATE_LABELS = Map.of(STATE_CLEANING, "@text/state.vacuum.cleaning",
            STATE_DOCKED, "@text/state.vacuum.docked", STATE_PAUSED, "@text/state.vacuum.paused", STATE_IDLE,
            "@text/state.vacuum.idle", STATE_RETURNING, "@text/state.vacuum.returning", STATE_ERROR,
            "@text/state.vacuum.error");

    public static final String COMMAND_CH_ID = "command";
    public static final String FAN_SPEED_CH_ID = "fan-speed";
    public static final String CUSTOM_COMMAND_CH_ID = "custom-command";
    public static final String BATTERY_LEVEL_CH_ID = "battery-level";
    public static final String JSON_ATTRIBUTES_CH_ID = "json-attributes";
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
        protected String payloadCleanSpot = PAYLOAD_CLEAN_SPOT;
        @SerializedName("payload_locate")
        protected String payloadLocate = PAYLOAD_LOCATE;
        @SerializedName("payload_pause")
        protected String payloadPause = PAYLOAD_PAUSE;
        @SerializedName("payload_return_to_base")
        protected String payloadReturnToBase = PAYLOAD_RETURN_TO_BASE;
        @SerializedName("payload_start")
        protected String payloadStart = PAYLOAD_START;
        @SerializedName("payload_stop")
        protected String payloadStop = PAYLOAD_STOP;

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
    public Vacuum(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);
        final ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        final var supportedFeatures = channelConfiguration.supportedFeatures;

        final Map<String, String> commands = new LinkedHashMap<>();
        addPayloadToList(supportedFeatures, FEATURE_CLEAN_SPOT, PAYLOAD_CLEAN_SPOT,
                channelConfiguration.payloadCleanSpot, commands);
        addPayloadToList(supportedFeatures, FEATURE_LOCATE, PAYLOAD_LOCATE, channelConfiguration.payloadLocate,
                commands);
        addPayloadToList(supportedFeatures, FEATURE_RETURN_HOME, PAYLOAD_RETURN_TO_BASE,
                channelConfiguration.payloadReturnToBase, commands);
        addPayloadToList(supportedFeatures, FEATURE_START, PAYLOAD_START, channelConfiguration.payloadStart, commands);
        addPayloadToList(supportedFeatures, FEATURE_STOP, PAYLOAD_STOP, channelConfiguration.payloadStop, commands);
        addPayloadToList(supportedFeatures, FEATURE_PAUSE, PAYLOAD_PAUSE, channelConfiguration.payloadPause, commands);

        buildOptionalChannel(COMMAND_CH_ID, ComponentChannelType.STRING,
                new TextValue(Map.of(), commands, Map.of(), COMMAND_LABELS), updateListener, null,
                channelConfiguration.commandTopic, null, null, "Command");

        final var fanSpeedList = channelConfiguration.fanSpeedList;
        if (supportedFeatures.contains(FEATURE_FAN_SPEED) && fanSpeedList != null && !fanSpeedList.isEmpty()) {
            var fanSpeedCommandList = fanSpeedList.toArray(new String[0]);
            if (!fanSpeedList.contains(OFF)) {
                fanSpeedList.add(OFF); // Off value is used when cleaning if OFF
            }
            var fanSpeedValue = new TextValue(fanSpeedList.toArray(new String[0]), fanSpeedCommandList);
            if (supportedFeatures.contains(FEATURE_STATUS)) {
                buildOptionalChannel(FAN_SPEED_CH_ID, ComponentChannelType.STRING, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, "{{ value_json.fan_speed }}",
                        channelConfiguration.stateTopic, "Fan Speed");
            } else {
                buildOptionalChannel(FAN_SPEED_CH_ID, ComponentChannelType.STRING, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, null, null, "Fan Speed");
            }
        }

        if (supportedFeatures.contains(FEATURE_SEND_COMMAND)) {
            buildOptionalChannel(CUSTOM_COMMAND_CH_ID, ComponentChannelType.STRING, new TextValue(), updateListener,
                    null, channelConfiguration.sendCommandTopic, null, null, "Custom Command");
        }

        if (supportedFeatures.contains(FEATURE_STATUS)) {
            // state key is mandatory
            buildOptionalChannel(STATE_CH_ID, ComponentChannelType.STRING, new TextValue(
                    Map.of(STATE_CLEANING, STATE_CLEANING, STATE_DOCKED, STATE_DOCKED, STATE_PAUSED, STATE_PAUSED,
                            STATE_IDLE, STATE_IDLE, STATE_RETURNING, STATE_RETURNING, STATE_ERROR, STATE_ERROR),
                    Map.of(), STATE_LABELS, Map.of()), updateListener, null, null, STATE_TEMPLATE,
                    channelConfiguration.stateTopic, "State");
            if (supportedFeatures.contains(FEATURE_BATTERY)) {
                buildOptionalChannel(BATTERY_LEVEL_CH_ID, ComponentChannelType.DIMMER,
                        new PercentageValue(BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.ONE, null, null, null),
                        updateListener, null, null, "{{ value_json.battery_level }}", channelConfiguration.stateTopic,
                        "Battery Level");
            }
        }

        finalizeChannels();
    }

    @Nullable
    private ComponentChannel buildOptionalChannel(String channelId, ComponentChannelType channelType, Value valueState,
            ChannelStateUpdateListener channelStateUpdateListener, @Nullable String commandTemplate,
            @Nullable String commandTopic, @Nullable String stateTemplate, @Nullable String stateTopic, String label) {
        if ((commandTopic != null && !commandTopic.isBlank()) || (stateTopic != null && !stateTopic.isBlank())) {
            return buildChannel(channelId, channelType, valueState, label, channelStateUpdateListener)
                    .stateTopic(stateTopic, stateTemplate, channelConfiguration.getValueTemplate())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos(),
                            commandTemplate)
                    .build();
        }
        return null;
    }

    private void addPayloadToList(List<String> supportedFeatures, String feature, String command, String payload,
            Map<String, String> commands) {
        if (supportedFeatures.contains(feature) && !payload.isEmpty()) {
            commands.put(command, payload);
        }
    }
}
