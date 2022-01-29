/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.TextValue;
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
    public static final String STATE_CHANNEL_ID = "alarm"; // Randomly chosen channel "ID"
    public static final String SWITCH_DISARM_CHANNEL_ID = "disarm"; // Randomly chosen channel "ID"
    public static final String SWITCH_ARM_HOME_CHANNEL_ID = "armhome"; // Randomly chosen channel "ID"
    public static final String SWITCH_ARM_AWAY_CHANNEL_ID = "armaway"; // Randomly chosen channel "ID"

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
        @SerializedName("state_disarmed")
        protected String stateDisarmed = "disarmed";
        @SerializedName("state_armed_home")
        protected String stateArmedHome = "armed_home";
        @SerializedName("state_armed_away")
        protected String stateArmedAway = "armed_away";
        @SerializedName("state_pending")
        protected String statePending = "pending";
        @SerializedName("state_triggered")
        protected String stateTriggered = "triggered";

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("payload_disarm")
        protected String payloadDisarm = "DISARM";
        @SerializedName("payload_arm_home")
        protected String payloadArmHome = "ARM_HOME";
        @SerializedName("payload_arm_away")
        protected String payloadArmAway = "ARM_AWAY";
    }

    public AlarmControlPanel(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        final String[] stateEnum = { channelConfiguration.stateDisarmed, channelConfiguration.stateArmedHome,
                channelConfiguration.stateArmedAway, channelConfiguration.statePending,
                channelConfiguration.stateTriggered };
        buildChannel(STATE_CHANNEL_ID, new TextValue(stateEnum), channelConfiguration.getName(),
                componentConfiguration.getUpdateListener())
                        .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())//
                        .build();

        String commandTopic = channelConfiguration.commandTopic;
        if (commandTopic != null) {
            buildChannel(SWITCH_DISARM_CHANNEL_ID, new TextValue(new String[] { channelConfiguration.payloadDisarm }),
                    channelConfiguration.getName(), componentConfiguration.getUpdateListener())
                            .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                            .build();

            buildChannel(SWITCH_ARM_HOME_CHANNEL_ID,
                    new TextValue(new String[] { channelConfiguration.payloadArmHome }), channelConfiguration.getName(),
                    componentConfiguration.getUpdateListener())
                            .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                            .build();

            buildChannel(SWITCH_ARM_AWAY_CHANNEL_ID,
                    new TextValue(new String[] { channelConfiguration.payloadArmAway }), channelConfiguration.getName(),
                    componentConfiguration.getUpdateListener())
                            .commandTopic(commandTopic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                            .build();
        }
    }
}
