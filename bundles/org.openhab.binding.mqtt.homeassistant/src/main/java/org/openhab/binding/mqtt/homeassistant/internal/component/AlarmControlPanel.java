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
    public static final String stateChannelID = "alarm"; // Randomly chosen channel "ID"
    public static final String switchDisarmChannelID = "disarm"; // Randomly chosen channel "ID"
    public static final String switchArmHomeChannelID = "armhome"; // Randomly chosen channel "ID"
    public static final String switchArmAwayChannelID = "armaway"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Alarm");
        }

        protected @Nullable String code;

        @SerializedName("state_topic")
        protected String state_topic = "";
        @SerializedName("state_disarmed")
        protected String state_disarmed = "disarmed";
        @SerializedName("state_armed_home")
        protected String state_armed_home = "armed_home";
        @SerializedName("state_armed_away")
        protected String state_armed_away = "armed_away";
        @SerializedName("state_pending")
        protected String state_pending = "pending";
        @SerializedName("state_triggered")
        protected String state_triggered = "triggered";

        @SerializedName("command_topic")
        protected @Nullable String command_topic;
        @SerializedName("payload_disarm")
        protected String payload_disarm = "DISARM";
        @SerializedName("payload_arm_home")
        protected String payload_arm_home = "ARM_HOME";
        @SerializedName("payload_arm_away")
        protected String payload_arm_away = "ARM_AWAY";
    }

    public AlarmControlPanel(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        final String[] state_enum = { channelConfiguration.state_disarmed, channelConfiguration.state_armed_home,
                channelConfiguration.state_armed_away, channelConfiguration.state_pending,
                channelConfiguration.state_triggered };
        buildChannel(stateChannelID, new TextValue(state_enum), channelConfiguration.getName(),
                componentConfiguration.getUpdateListener())
                        .stateTopic(channelConfiguration.state_topic, channelConfiguration.getValueTemplate())//
                        .build();

        String command_topic = channelConfiguration.command_topic;
        if (command_topic != null) {
            buildChannel(switchDisarmChannelID, new TextValue(new String[] { channelConfiguration.payload_disarm }),
                    channelConfiguration.getName(), componentConfiguration.getUpdateListener())
                            .commandTopic(command_topic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                            .build();

            buildChannel(switchArmHomeChannelID, new TextValue(new String[] { channelConfiguration.payload_arm_home }),
                    channelConfiguration.getName(), componentConfiguration.getUpdateListener())
                            .commandTopic(command_topic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                            .build();

            buildChannel(switchArmAwayChannelID, new TextValue(new String[] { channelConfiguration.payload_arm_away }),
                    channelConfiguration.getName(), componentConfiguration.getUpdateListener())
                            .commandTopic(command_topic, channelConfiguration.isRetain(), channelConfiguration.getQos())
                            .build();
        }
    }
}
