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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT alarm control panel, following the https://www.home-assistant.io/components/alarm_control_panel.mqtt/
 * specification.
 *
 * The implemented provides three state-less switches (For disarming, arming@home, arming@away) and one alarm state
 * text.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class AlarmControlPanel extends AbstractComponent<AlarmControlPanel.ChannelConfiguration> {
    public static final String STATE_CHANNEL_ID = "state";
    public static final String STATE_CHANNEL_ID_DEPRECATED = "alarm";
    public static final String SWITCH_DISARM_CHANNEL_ID = "disarm";
    public static final String SWITCH_ARM_HOME_CHANNEL_ID = "armhome";
    public static final String SWITCH_ARM_AWAY_CHANNEL_ID = "armaway";

    public static final String FEATURE_ARM_HOME = "arm_home";
    public static final String FEATURE_ARM_AWAY = "arm_away";
    public static final String FEATURE_ARM_NIGHT = "arm_night";
    public static final String FEATURE_ARM_VACATION = "arm_vacation";
    public static final String FEATURE_ARM_CUSTOM_BYPASS = "arm_custom_bypass";
    public static final String FEATURE_TRIGGER = "trigger";

    public static final String STATE_ARMED_AWAY = "armed_away";
    public static final String STATE_ARMED_CUSTOM_BYPASS = "armed_custom_bypass";
    public static final String STATE_ARMED_HOME = "armed_home";
    public static final String STATE_ARMED_NIGHT = "armed_night";
    public static final String STATE_ARMED_VACATION = "armed_vacation";
    public static final String STATE_ARMING = "arming";
    public static final String STATE_DISARMED = "disarmed";
    public static final String STATE_DISARMING = "disarming";
    public static final String STATE_PENDING = "pending";
    public static final String STATE_TRIGGERED = "triggered";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Alarm");
        }

        protected @Nullable String code;

        @SerializedName("state_topic")
        protected String stateTopic = "";

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("payload_arm_away")
        protected String payloadArmAway = "ARM_AWAY";
        @SerializedName("payload_arm_home")
        protected String payloadArmHome = "ARM_HOME";
        @SerializedName("payload_arm_night")
        protected String payloadArmNight = "ARM_NIGHT";
        @SerializedName("payload_arm_vacation")
        protected String payloadArmVacation = "ARM_VACATION";
        @SerializedName("payload_arm_custom_bypass")
        protected String payloadArmCustomBypass = "ARM_CUSTOM_BYPASS";
        @SerializedName("payload_disarm")
        protected String payloadDisarm = "DISARM";
        @SerializedName("payload_trigger")
        protected String payloadTrigger = "TRIGGER";

        @SerializedName("supported_features")
        protected List<String> supportedFeatures = List.of(FEATURE_ARM_HOME, FEATURE_ARM_AWAY, FEATURE_ARM_NIGHT,
                FEATURE_ARM_VACATION, FEATURE_ARM_CUSTOM_BYPASS, FEATURE_TRIGGER);
    }

    public AlarmControlPanel(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        List<String> stateEnum = new ArrayList(List.of(STATE_DISARMED, STATE_TRIGGERED, STATE_ARMING, STATE_DISARMING,
                STATE_PENDING, STATE_TRIGGERED));
        List<String> commandEnum = new ArrayList(List.of(channelConfiguration.payloadDisarm));
        if (channelConfiguration.supportedFeatures.contains(FEATURE_ARM_HOME)) {
            stateEnum.add(STATE_ARMED_HOME);
            commandEnum.add(channelConfiguration.payloadArmHome);
        }
        if (channelConfiguration.supportedFeatures.contains(FEATURE_ARM_AWAY)) {
            stateEnum.add(STATE_ARMED_AWAY);
            commandEnum.add(channelConfiguration.payloadArmAway);
        }
        if (channelConfiguration.supportedFeatures.contains(FEATURE_ARM_NIGHT)) {
            stateEnum.add(STATE_ARMED_NIGHT);
            commandEnum.add(channelConfiguration.payloadArmNight);
        }
        if (channelConfiguration.supportedFeatures.contains(FEATURE_ARM_VACATION)) {
            stateEnum.add(STATE_ARMED_VACATION);
            commandEnum.add(channelConfiguration.payloadArmVacation);
        }
        if (channelConfiguration.supportedFeatures.contains(FEATURE_ARM_CUSTOM_BYPASS)) {
            stateEnum.add(STATE_ARMED_CUSTOM_BYPASS);
            commandEnum.add(channelConfiguration.payloadArmCustomBypass);
        }
        if (channelConfiguration.supportedFeatures.contains(FEATURE_TRIGGER)) {
            commandEnum.add(channelConfiguration.payloadTrigger);
        }

        String commandTopic = channelConfiguration.commandTopic;
        TextValue value = (newStyleChannels && commandTopic != null)
                ? new TextValue(stateEnum.toArray(new String[0]), commandEnum.toArray(new String[0]))
                : new TextValue(stateEnum.toArray(new String[0]));
        var builder = buildChannel(newStyleChannels ? STATE_CHANNEL_ID : STATE_CHANNEL_ID_DEPRECATED,
                ComponentChannelType.STRING, value, getName(), componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate());

        if (newStyleChannels && commandTopic != null) {
            builder.commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos());
        }
        builder.build();

        if (!newStyleChannels && commandTopic != null) {
            buildChannel(SWITCH_DISARM_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(new String[] { channelConfiguration.payloadDisarm }), getName(),
                    componentConfiguration.getUpdateListener())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos()).build();

            buildChannel(SWITCH_ARM_HOME_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(new String[] { channelConfiguration.payloadArmHome }), getName(),
                    componentConfiguration.getUpdateListener())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos()).build();

            buildChannel(SWITCH_ARM_AWAY_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(new String[] { channelConfiguration.payloadArmAway }), getName(),
                    componentConfiguration.getUpdateListener())
                    .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos()).build();
        }

        finalizeChannels();
    }
}
