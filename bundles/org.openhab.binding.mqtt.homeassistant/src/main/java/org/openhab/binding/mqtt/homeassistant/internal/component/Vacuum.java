/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.DateTimeValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT vacuum, following the https://www.home-assistant.io/components/vacuum.mqtt/ specification.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
public class Vacuum extends AbstractComponent<Vacuum.ChannelConfiguration> {
    public static final String VACUUM_STATE_CHANNEL_ID = "state";
    public static final String VACUUM_COMMAND_CHANNEL_ID = "command";
    public static final String VACUUM_BATTERY_CHANNEL_ID = "batteryLevel";
    public static final String VACUUM_FAN_SPEED_CHANNEL_ID = "fanSpeed";

    // sensor stats
    public static final String VACUUM_MAIN_BRUSH_CHANNEL_ID = "mainBrushUsage";
    public static final String VACUUM_SIDE_BRUSH_CHANNEL_ID = "sideBrushUsage";
    public static final String VACUUM_FILTER_CHANNEL_ID = "filter";
    public static final String VACUUM_SENSOR_CHANNEL_ID = "sensor";
    public static final String VACUUM_CURRENT_CLEAN_TIME_CHANNEL_ID = "currentCleanTime";
    public static final String VACUUM_CURRENT_CLEAN_AREA_CHANNEL_ID = "currentCleanArea";
    public static final String VACUUM_CLEAN_TIME_CHANNEL_ID = "cleanTime";
    public static final String VACUUM_CLEAN_AREA_CHANNEL_ID = "cleanArea";
    public static final String VACUUM_CLEAN_COUNT_CHANNEL_ID = "cleanCount";

    public static final String VACUUM_LAST_RUN_START_CHANNEL_ID = "lastRunStart";
    public static final String VACUUM_LAST_RUN_END_CHANNEL_ID = "lastRunEnd";
    public static final String VACUUM_LAST_RUN_DURATION_CHANNEL_ID = "lastRunDuration";
    public static final String VACUUM_LAST_RUN_AREA_CHANNEL_ID = "lastRunArea";
    public static final String VACUUM_LAST_RUN_ERROR_CODE_CHANNEL_ID = "lastRunErrorCode";
    public static final String VACUUM_LAST_RUN_ERROR_DESCRIPTION_CHANNEL_ID = "lastRunErrorDescription";
    public static final String VACUUM_LAST_RUN_FINISHED_FLAG_CHANNEL_ID = "lastRunFinishedFlag";

    public static final String VACUUM_BIN_IN_TIME_CHANNEL_ID = "binInTime";
    public static final String VACUUM_LAST_BIN_OUT_TIME_CHANNEL_ID = "lastBinOutTime";
    public static final String VACUUM_LAST_BIN_FULL_TIME_CHANNEL_ID = "lastBinFullTime";

    public static final String VACUUM_CUSMTOM_COMMAND_CHANNEL_ID = "customCommand";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Vacuum");
        }

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("state_topic")
        protected String stateTopic = "";
        @SerializedName("send_command_topic")
        protected @Nullable String sendCommandTopic; // for custom_command

        // [start, pause, stop, return_home, battery, status, locate, clean_spot, fan_speed, send_command]
        @SerializedName("supported_features")
        protected String[] supportedFeatures = new String[] {};
        @SerializedName("set_fan_speed_topic")
        protected @Nullable String setFanSpeedTopic;
        @SerializedName("fan_speed_list")
        protected String[] fanSpeedList = new String[] {};

        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
    }

    public Vacuum(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        List<String> features = Arrays.asList(channelConfiguration.supportedFeatures);

        // features = [start, pause, stop, return_home, status, locate, clean_spot, fan_speed, send_command]
        ArrayList<String> possibleCommands = new ArrayList<String>();
        if (features.contains("start")) {
            possibleCommands.add("start");
        }

        if (features.contains("stop")) {
            possibleCommands.add("stop");
        }

        if (features.contains("pause")) {
            possibleCommands.add("pause");
        }

        if (features.contains("return_home")) {
            possibleCommands.add("return_to_base");
        }

        if (features.contains("locate")) {
            possibleCommands.add("locate");
        }

        TextValue value = new TextValue(possibleCommands.toArray(new String[0]));
        buildChannel(VACUUM_COMMAND_CHANNEL_ID, value, "Command", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.commandTopic).commandTopic(channelConfiguration.commandTopic, false, 1)
                .build();

        List<String> vacuumStates = List.of("docked", "cleaning", "returning", "paused", "idle", "error");
        TextValue valueState = new TextValue(vacuumStates.toArray(new String[0]));
        buildChannel(VACUUM_STATE_CHANNEL_ID, valueState, "State", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, "{{value_json.state}}").build();

        if (features.contains("battery")) {
            // build battery level channel (0-100)
            NumberValue batValue = new NumberValue(BigDecimal.ZERO, new BigDecimal(100), new BigDecimal(1), "%");
            buildChannel(VACUUM_BATTERY_CHANNEL_ID, batValue, "Battery Level",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.stateTopic, "{{value_json.battery_level}}").build();
        }

        if (features.contains("fan_speed")) {
            // build fan speed channel with values from channelConfiguration.fan_speed_list
            TextValue fanValue = new TextValue(channelConfiguration.fanSpeedList);
            buildChannel(VACUUM_FAN_SPEED_CHANNEL_ID, fanValue, "Fan speed", componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.stateTopic, "{{value_json.fan_speed}}")
                    .commandTopic(channelConfiguration.setFanSpeedTopic, false, 1).build();
        }

        // {"mainBrush":"220.6","sideBrush":"120.6","filter":"70.6","sensor":"0.0","currentCleanTime":"0.0","currentCleanArea":"0.0","cleanTime":"79.3","cleanArea":"4439.9","cleanCount":183,"last_run_stats":{"startTime":1613503117000,"endTime":1613503136000,"duration":0,"area":"0.0","errorCode":0,"errorDescription":"No
        // error","finishedFlag":false},"bin_in_time":1000,"last_bin_out":-1,"last_bin_full":-1,"last_loaded_map":null,"state":"docked","valetudo_state":{"id":8,"name":"Charging"}}
        if (features.contains("status")) {
            NumberValue currentCleanTimeValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_CURRENT_CLEAN_TIME_CHANNEL_ID, currentCleanTimeValue, "Current Cleaning Time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.currentCleanTime}}")
                            .build();

            NumberValue currentCleanAreaValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_CURRENT_CLEAN_AREA_CHANNEL_ID, currentCleanAreaValue, "Current Cleaning Area",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.currentCleanArea}}")
                            .build();

            NumberValue cleanTimeValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_CLEAN_TIME_CHANNEL_ID, cleanTimeValue, "Cleaning Time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.cleanTime}}").build();

            NumberValue cleanAreaValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_CLEAN_AREA_CHANNEL_ID, cleanAreaValue, "Cleaned Area",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.cleanArea}}").build();

            NumberValue cleaCountValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_CLEAN_COUNT_CHANNEL_ID, cleaCountValue, "Cleaning Counter",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.cleanCount}}").build();

            DateTimeValue lastStartTime = new DateTimeValue();
            buildChannel(VACUUM_LAST_RUN_START_CHANNEL_ID, lastStartTime, "Last run start time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic,
                                    "{{value_json.last_run_stats.startTime}}")
                            .build();

            DateTimeValue lastEndTime = new DateTimeValue();
            buildChannel(VACUUM_LAST_RUN_END_CHANNEL_ID, lastEndTime, "Last run end time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic,
                                    "{{value_json.last_run_stats.endTime}}")
                            .build();

            NumberValue lastRunDurationValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_LAST_RUN_DURATION_CHANNEL_ID, lastRunDurationValue, "Last run duration",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic,
                                    "{{value_json.last_run_stats.duration}}")
                            .build();

            NumberValue lastRunAreaValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_LAST_RUN_AREA_CHANNEL_ID, lastRunAreaValue, "Last run area",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.last_run_stats.area}}")
                            .build();

            NumberValue lastRunErrorCodeValue = new NumberValue(null, null, null, null);
            buildChannel(VACUUM_LAST_RUN_ERROR_CODE_CHANNEL_ID, lastRunErrorCodeValue, "Last run error code",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic,
                                    "{{value_json.last_run_stats.errorCode}}")
                            .build();

            TextValue lastRunErrorDescriptionValue = new TextValue();
            buildChannel(VACUUM_LAST_RUN_ERROR_DESCRIPTION_CHANNEL_ID, lastRunErrorDescriptionValue,
                    "Last run error description", componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic,
                                    "{{value_json.last_run_stats.errorDescription}}")
                            .build();

            // true/false doesnt map to ON/OFF => use TextValue instead of OnOffValue
            TextValue lastRunFinishedFlagValue = new TextValue();
            buildChannel(VACUUM_LAST_RUN_FINISHED_FLAG_CHANNEL_ID, lastRunFinishedFlagValue, "Last run finished flag",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic,
                                    "{{value_json.last_run_stats.finishedFlag}}")
                            .build();

            // only for valetudo re => advanced channels
            DateTimeValue binInValue = new DateTimeValue();
            buildChannel(VACUUM_BIN_IN_TIME_CHANNEL_ID, binInValue, "Bin In Time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.bin_in_time}}")
                            .isAdvanced(true).build();

            DateTimeValue lastBinOutValue = new DateTimeValue();
            buildChannel(VACUUM_LAST_BIN_OUT_TIME_CHANNEL_ID, lastBinOutValue, "Last Bin Out Time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.last_bin_out}}")
                            .isAdvanced(true).build();

            DateTimeValue lastBinFullValue = new DateTimeValue();
            buildChannel(VACUUM_LAST_BIN_FULL_TIME_CHANNEL_ID, lastBinFullValue, "Last Bin Full Time",
                    componentConfiguration.getUpdateListener())
                            .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.last_bin_full}}")
                            .isAdvanced(true).build();
        }

        NumberValue mainBrush = new NumberValue(null, null, null, null);
        buildChannel(VACUUM_MAIN_BRUSH_CHANNEL_ID, mainBrush, "Main brush usage",
                componentConfiguration.getUpdateListener())
                        .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.mainBrush}}").build();

        NumberValue sideBrush = new NumberValue(null, null, null, null);
        buildChannel(VACUUM_SIDE_BRUSH_CHANNEL_ID, sideBrush, "Side brush usage",
                componentConfiguration.getUpdateListener())
                        .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.sideBrush}}").build();

        NumberValue filterValue = new NumberValue(null, null, null, null);
        buildChannel(VACUUM_FILTER_CHANNEL_ID, filterValue, "Filter time", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.filter}}").build();

        NumberValue sensorValue = new NumberValue(null, null, null, null);
        buildChannel(VACUUM_SENSOR_CHANNEL_ID, sensorValue, "Sensor", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.jsonAttributesTopic, "{{value_json.sensor}}").build();

        // if we have a custom command channel for zone cleanup, etc => create text channel
        if (channelConfiguration.sendCommandTopic != null) {
            TextValue customCommandValue = new TextValue();
            buildChannel(VACUUM_CUSMTOM_COMMAND_CHANNEL_ID, customCommandValue, "Custom Command",
                    componentConfiguration.getUpdateListener())
                            .commandTopic(channelConfiguration.sendCommandTopic, false, 1)
                            .stateTopic(channelConfiguration.sendCommandTopic).build();
        }
    }
}
