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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.values.TextValue;

import com.google.gson.Gson;
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
public class ComponentAlarmControlPanel extends AbstractComponent<ComponentAlarmControlPanel.Config> {
    public static final String stateChannelID = "alarm"; // Randomly chosen channel "ID"
    public static final String switchDisarmChannelID = "disarm"; // Randomly chosen channel "ID"
    public static final String switchArmHomeChannelID = "armhome"; // Randomly chosen channel "ID"
    public static final String switchArmAwayChannelID = "armaway"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Alarm");
        }

        protected @Nullable String code;

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected String state_topic = "";
        protected String state_disarmed = "disarmed";
        protected String state_armed_home = "armed_home";
        protected String state_armed_away = "armed_away";
        protected String state_pending = "pending";
        protected String state_triggered = "triggered";

        @SerializedName(value = "command_topic", alternate = "cmd_t")
        protected String command_topic = "";
        @SerializedName(value = "payload_disarm", alternate = "pl_disarm")
        protected String payload_disarm = "DISARM";
        @SerializedName(value = "payload_arm_home", alternate = "pl_arm_home")
        protected String payload_arm_home = "ARM_HOME";
        @SerializedName(value = "payload_arm_away", alternate = "pl_arm_away")
        protected String payload_arm_away = "ARM_AWAY";

    };

    public ComponentAlarmControlPanel(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson,
            TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        final String[] state_enum = { config.state_disarmed, config.state_armed_home, config.state_armed_away,
                config.state_pending, config.state_triggered };

        CChannel stateChannel = new CChannel(this, stateChannelID, new TextValue(state_enum),
                config.expand(config.state_topic), "State", "", channelStateUpdateListener);
        stateChannel.addTemplateIn(provider, config.value_template);
        addChannel(stateChannel);

        // TODO: How do we check for the code in the front-end?
        // It could be dangerous to allow disarming the alarm thru the web without the code....
        addChannel(new CChannel(this, switchDisarmChannelID, new TextValue(new String[] { config.payload_disarm }),
                config.expand(config.command_topic), false, "Disarm", "", channelStateUpdateListener));

        addChannel(new CChannel(this, switchArmHomeChannelID, new TextValue(new String[] { config.payload_arm_home }),
                config.expand(config.command_topic), false, "Arm", "", channelStateUpdateListener));

        addChannel(new CChannel(this, switchArmAwayChannelID, new TextValue(new String[] { config.payload_arm_away }),
                config.expand(config.command_topic), false, "Away", "", channelStateUpdateListener));
    }

    @Override
    public String name() {
        return config.name;
    }
}
