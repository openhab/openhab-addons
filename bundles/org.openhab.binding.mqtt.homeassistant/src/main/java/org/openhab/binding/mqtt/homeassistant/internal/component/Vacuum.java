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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
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
    public static final String SCHEMA_LEGACY = "legacy";
    public static final String SCHEMA_STATE = "state";

    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String OFF = "off";

    public static final String FEATURE_TURN_ON = "turn_on"; // Begin cleaning
    public static final String FEATURE_TURN_OFF = "turn_off"; // Turn the Vacuum off
    public static final String FEATURE_RETURN_HOME = "return_home"; // Return to base/dock
    public static final String FEATURE_START = "start";
    public static final String FEATURE_STOP = "stop"; // Stop the Vacuum
    public static final String FEATURE_CLEAN_SPOT = "clean_spot"; // Initialize a spot cleaning cycle
    public static final String FEATURE_LOCATE = "locate"; // Locate the vacuum (typically by playing a song)
    public static final String FEATURE_PAUSE = "pause"; // Pause the vacuum
    public static final String FEATURE_BATTERY = "battery";
    public static final String FEATURE_STATUS = "status";
    public static final String FEATURE_FAN_SPEED = "fan_speed";
    public static final String FEATURE_SEND_COMMAND = "send_command";

    // State Schema only
    public static final String STATE_CLEANING = "cleaning";
    public static final String STATE_DOCKED = "docked";
    public static final String STATE_PAUSED = "paused";
    public static final String STATE_IDLE = "idle";
    public static final String STATE_RETURNING = "returning";
    public static final String STATE_ERROR = "error";

    public static final String COMMAND_CH_ID = "command";
    public static final String FAN_SPEED_CH_ID = "fanSpeed";
    public static final String CUSTOM_COMMAND_CH_ID = "customCommand";
    public static final String BATTERY_LEVEL_CH_ID = "batteryLevel";
    public static final String CHARGING_CH_ID = "charging";
    public static final String CLEANING_CH_ID = "cleaning";
    public static final String DOCKED_CH_ID = "docked";
    public static final String ERROR_CH_ID = "error";
    public static final String JSON_ATTRIBUTES_CH_ID = "jsonAttributes";
    public static final String STATE_CH_ID = "state";

    public static final List<String> LEGACY_DEFAULT_FEATURES = List.of(FEATURE_TURN_ON, FEATURE_TURN_OFF, FEATURE_STOP,
            FEATURE_RETURN_HOME, FEATURE_BATTERY, FEATURE_STATUS, FEATURE_CLEAN_SPOT);
    public static final List<String> LEGACY_SUPPORTED_FEATURES = List.of(FEATURE_TURN_ON, FEATURE_TURN_OFF,
            FEATURE_PAUSE, FEATURE_STOP, FEATURE_RETURN_HOME, FEATURE_BATTERY, FEATURE_STATUS, FEATURE_LOCATE,
            FEATURE_CLEAN_SPOT, FEATURE_FAN_SPEED, FEATURE_SEND_COMMAND);

    public static final List<String> STATE_DEFAULT_FEATURES = List.of(FEATURE_START, FEATURE_STOP, FEATURE_RETURN_HOME,
            FEATURE_STATUS, FEATURE_BATTERY, FEATURE_CLEAN_SPOT);
    public static final List<String> STATE_SUPPORTED_FEATURES = List.of(FEATURE_START, FEATURE_STOP, FEATURE_PAUSE,
            FEATURE_RETURN_HOME, FEATURE_BATTERY, FEATURE_STATUS, FEATURE_LOCATE, FEATURE_CLEAN_SPOT, FEATURE_FAN_SPEED,
            FEATURE_SEND_COMMAND);

    private static final Logger LOGGER = LoggerFactory.getLogger(Vacuum.class);

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Vacuum");
        }

        // Legacy and Common MQTT vacuum configuration section.

        @SerializedName("battery_level_template")
        protected @Nullable String batteryLevelTemplate;
        @SerializedName("battery_level_topic")
        protected @Nullable String batteryLevelTopic;

        @SerializedName("charging_template")
        protected @Nullable String chargingTemplate;
        @SerializedName("charging_topic")
        protected @Nullable String chargingTopic;

        @SerializedName("cleaning_template")
        protected @Nullable String cleaningTemplate;
        @SerializedName("cleaning_topic")
        protected @Nullable String cleaningTopic;

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;

        @SerializedName("docked_template")
        protected @Nullable String dockedTemplate;
        @SerializedName("docked_topic")
        protected @Nullable String dockedTopic;

        @SerializedName("error_template")
        protected @Nullable String errorTemplate;
        @SerializedName("error_topic")
        protected @Nullable String errorTopic;

        @SerializedName("fan_speed_list")
        protected @Nullable List<String> fanSpeedList;
        @SerializedName("fan_speed_template")
        protected @Nullable String fanSpeedTemplate;
        @SerializedName("fan_speed_topic")
        protected @Nullable String fanSpeedTopic;

        @SerializedName("payload_clean_spot")
        protected @Nullable String payloadCleanSpot = "clean_spot";
        @SerializedName("payload_locate")
        protected @Nullable String payloadLocate = "locate";
        @SerializedName("payload_return_to_base")
        protected @Nullable String payloadReturnToBase = "return_to_base";
        @SerializedName("payload_start_pause")
        protected @Nullable String payloadStartPause = "start_pause"; // Legacy only
        @SerializedName("payload_stop")
        protected @Nullable String payloadStop = "stop";
        @SerializedName("payload_turn_off")
        protected @Nullable String payloadTurnOff = "turn_off";
        @SerializedName("payload_turn_on")
        protected @Nullable String payloadTurnOn = "turn_on";

        @SerializedName("schema")
        protected Schema schema = Schema.LEGACY;

        @SerializedName("send_command_topic")
        protected @Nullable String sendCommandTopic;

        @SerializedName("set_fan_speed_topic")
        protected @Nullable String setFanSpeedTopic;

        @SerializedName("supported_features")
        protected @Nullable List<String> supportedFeatures;

        // State MQTT vacuum configuration section.

        // Start/Pause replaced by 2 payloads
        @SerializedName("payload_pause")
        protected @Nullable String payloadPause = "pause";
        @SerializedName("payload_start")
        protected @Nullable String payloadStart = "start";

        @SerializedName("state_topic")
        protected @Nullable String stateTopic;

        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
    }

    /**
     * Creates component based on generic configuration and component configuration type.
     *
     * @param componentConfiguration generic componentConfiguration with not parsed JSON config
     */
    public Vacuum(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);
        final ChannelStateUpdateListener updateListener = componentConfiguration.getUpdateListener();

        final var allowedSupportedFeatures = channelConfiguration.schema == Schema.LEGACY ? LEGACY_SUPPORTED_FEATURES
                : STATE_SUPPORTED_FEATURES;
        final var supportedFeatures = channelConfiguration.supportedFeatures;
        final var configSupportedFeatures = supportedFeatures == null
                ? channelConfiguration.schema == Schema.LEGACY ? LEGACY_DEFAULT_FEATURES : STATE_DEFAULT_FEATURES
                : supportedFeatures;
        List<String> deviceSupportedFeatures = Collections.emptyList();

        if (!configSupportedFeatures.isEmpty()) {
            deviceSupportedFeatures = allowedSupportedFeatures.stream().filter(configSupportedFeatures::contains)
                    .collect(Collectors.toList());
        }
        if (deviceSupportedFeatures.size() != configSupportedFeatures.size()) {
            LOGGER.warn("Vacuum discovery config has unsupported or duplicated features. Supported: {}, provided: {}",
                    Arrays.toString(allowedSupportedFeatures.toArray()),
                    Arrays.toString(configSupportedFeatures.toArray()));
        }

        final List<String> commands = new ArrayList<>();
        addPayloadToList(deviceSupportedFeatures, FEATURE_CLEAN_SPOT, channelConfiguration.payloadCleanSpot, commands);
        addPayloadToList(deviceSupportedFeatures, FEATURE_LOCATE, channelConfiguration.payloadLocate, commands);
        addPayloadToList(deviceSupportedFeatures, FEATURE_RETURN_HOME, channelConfiguration.payloadReturnToBase,
                commands);
        addPayloadToList(deviceSupportedFeatures, FEATURE_STOP, channelConfiguration.payloadStop, commands);
        addPayloadToList(deviceSupportedFeatures, FEATURE_TURN_OFF, channelConfiguration.payloadTurnOff, commands);
        addPayloadToList(deviceSupportedFeatures, FEATURE_TURN_ON, channelConfiguration.payloadTurnOn, commands);

        if (channelConfiguration.schema == Schema.LEGACY) {
            addPayloadToList(deviceSupportedFeatures, FEATURE_PAUSE, channelConfiguration.payloadStartPause, commands);
        } else {
            addPayloadToList(deviceSupportedFeatures, FEATURE_PAUSE, channelConfiguration.payloadPause, commands);
            addPayloadToList(deviceSupportedFeatures, FEATURE_START, channelConfiguration.payloadStart, commands);
        }

        buildOptionalChannel(COMMAND_CH_ID, new TextValue(commands.toArray(new String[0])), updateListener, null,
                channelConfiguration.commandTopic, null, null);

        final var fanSpeedList = channelConfiguration.fanSpeedList;
        if (deviceSupportedFeatures.contains(FEATURE_FAN_SPEED) && fanSpeedList != null && !fanSpeedList.isEmpty()) {
            if (!fanSpeedList.contains(OFF)) {
                fanSpeedList.add(OFF); // Off value is used when cleaning if OFF
            }
            var fanSpeedValue = new TextValue(fanSpeedList.toArray(new String[0]));
            if (channelConfiguration.schema == Schema.LEGACY) {
                buildOptionalChannel(FAN_SPEED_CH_ID, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, channelConfiguration.fanSpeedTemplate,
                        channelConfiguration.fanSpeedTopic);
            } else if (deviceSupportedFeatures.contains(FEATURE_STATUS)) {
                buildOptionalChannel(FAN_SPEED_CH_ID, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, "{{ value_json.fan_speed }}",
                        channelConfiguration.stateTopic);
            } else {
                LOGGER.info("Status feature is disabled, unable to get fan speed.");
                buildOptionalChannel(FAN_SPEED_CH_ID, fanSpeedValue, updateListener, null,
                        channelConfiguration.setFanSpeedTopic, null, null);
            }
        }

        if (deviceSupportedFeatures.contains(FEATURE_SEND_COMMAND)) {
            buildOptionalChannel(CUSTOM_COMMAND_CH_ID, new TextValue(), updateListener, null,
                    channelConfiguration.sendCommandTopic, null, null);
        }

        if (channelConfiguration.schema == Schema.LEGACY) {
            // I assume, that if these topics defined in config, then we don't need to check features
            buildOptionalChannel(BATTERY_LEVEL_CH_ID,
                    new PercentageValue(BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.ONE, null, null),
                    updateListener, null, null, channelConfiguration.batteryLevelTemplate,
                    channelConfiguration.batteryLevelTopic);
            buildOptionalChannel(CHARGING_CH_ID, new OnOffValue(TRUE, FALSE), updateListener, null, null,
                    channelConfiguration.chargingTemplate, channelConfiguration.chargingTopic);
            buildOptionalChannel(CLEANING_CH_ID, new OnOffValue(TRUE, FALSE), updateListener, null, null,
                    channelConfiguration.cleaningTemplate, channelConfiguration.cleaningTopic);
            buildOptionalChannel(DOCKED_CH_ID, new OnOffValue(TRUE, FALSE), updateListener, null, null,
                    channelConfiguration.dockedTemplate, channelConfiguration.dockedTopic);
            buildOptionalChannel(ERROR_CH_ID, new TextValue(), updateListener, null, null,
                    channelConfiguration.errorTemplate, channelConfiguration.errorTopic);
        } else {
            if (deviceSupportedFeatures.contains(FEATURE_STATUS)) {
                // state key is mandatory
                buildOptionalChannel(STATE_CH_ID,
                        new TextValue(new String[] { STATE_CLEANING, STATE_DOCKED, STATE_PAUSED, STATE_IDLE,
                                STATE_RETURNING, STATE_ERROR }),
                        updateListener, null, null, "{{ value_json.state }}", channelConfiguration.stateTopic);
                if (deviceSupportedFeatures.contains(FEATURE_BATTERY)) {
                    buildOptionalChannel(BATTERY_LEVEL_CH_ID,
                            new PercentageValue(BigDecimal.ZERO, BigDecimal.valueOf(100), BigDecimal.ONE, null, null),
                            updateListener, null, null, "{{ value_json.battery_level }}",
                            channelConfiguration.stateTopic);
                }
            }
        }

        buildOptionalChannel(JSON_ATTRIBUTES_CH_ID, new TextValue(), updateListener, null, null,
                channelConfiguration.jsonAttributesTemplate, channelConfiguration.jsonAttributesTopic);
    }

    @Nullable
    private ComponentChannel buildOptionalChannel(String channelId, Value valueState,
            ChannelStateUpdateListener channelStateUpdateListener, @Nullable String commandTemplate,
            @Nullable String commandTopic, @Nullable String stateTemplate, @Nullable String stateTopic) {
        if ((commandTopic != null && !commandTopic.isBlank()) || (stateTopic != null && !stateTopic.isBlank())) {
            return buildChannel(channelId, valueState, getName(), channelStateUpdateListener)
                    .stateTopic(stateTopic, stateTemplate, channelConfiguration.getValueTemplate())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos(),
                            commandTemplate)
                    .build();
        }
        return null;
    }

    private void addPayloadToList(List<String> supportedFeatures, String feature, @Nullable String payload,
            List<String> list) {
        if (supportedFeatures.contains(feature) && payload != null && !payload.isEmpty()) {
            list.add(payload);
        }
    }

    public enum Schema {
        @SerializedName("legacy")
        LEGACY,
        @SerializedName("state")
        STATE
    }
}
