/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.TextValue;

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
public class ComponentAlarmControlPanel extends AbstractComponent<ComponentAlarmControlPanel.ChannelConfiguration> {
    public static final String stateChannelID = "alarm"; // Randomly chosen channel "ID"
    public static final String switchDisarmChannelID = "disarm"; // Randomly chosen channel "ID"
    public static final String switchArmHomeChannelID = "armhome"; // Randomly chosen channel "ID"
    public static final String switchArmAwayChannelID = "armaway"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Alarm");
        }

        protected @Nullable String code;

        protected String state_topic = "";
        protected String state_disarmed = "disarmed";
        protected String state_armed_home = "armed_home";
        protected String state_armed_away = "armed_away";
        protected String state_pending = "pending";
        protected String state_triggered = "triggered";

        protected @Nullable String command_topic;
        protected String payload_disarm = "DISARM";
        protected String payload_arm_home = "ARM_HOME";
        protected String payload_arm_away = "ARM_AWAY";
    };

    public ComponentAlarmControlPanel(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        final String[] state_enum = { channelConfiguration.state_disarmed, channelConfiguration.state_armed_home,
                channelConfiguration.state_armed_away, channelConfiguration.state_pending,
                channelConfiguration.state_triggered };
        buildChannel(stateChannelID, new TextValue(state_enum), channelConfiguration.name)
                .listener(componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template)//
                .build();

        String command_topic = channelConfiguration.command_topic;
        if (command_topic != null) {
            buildChannel(switchDisarmChannelID, new TextValue(new String[] { channelConfiguration.payload_disarm }),
                    channelConfiguration.name).listener(componentConfiguration.getUpdateListener())//
                            .commandTopic(command_topic, channelConfiguration.retain)//
                            .build();

            buildChannel(switchArmHomeChannelID, new TextValue(new String[] { channelConfiguration.payload_arm_home }),
                    channelConfiguration.name).listener(componentConfiguration.getUpdateListener())//
                            .commandTopic(command_topic, channelConfiguration.retain)//
                            .build();

            buildChannel(switchArmAwayChannelID, new TextValue(new String[] { channelConfiguration.payload_arm_away }),
                    channelConfiguration.name).listener(componentConfiguration.getUpdateListener())//
                            .commandTopic(command_topic, channelConfiguration.retain)//
                            .build();
        }
    }
}
