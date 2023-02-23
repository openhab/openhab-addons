/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT switch, following the https://www.home-assistant.io/components/switch.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Switch extends AbstractComponent<Switch.ChannelConfiguration> {
    public static final String SWITCH_CHANNEL_ID = "switch"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Switch");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("state_topic")
        protected String stateTopic = "";

        @SerializedName("state_on")
        protected @Nullable String stateOn;
        @SerializedName("state_off")
        protected @Nullable String stateOff;
        @SerializedName("payload_on")
        protected String payloadOn = "ON";
        @SerializedName("payload_off")
        protected String payloadOff = "OFF";

        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
    }

    public Switch(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        boolean optimistic = channelConfiguration.optimistic != null ? channelConfiguration.optimistic
                : channelConfiguration.stateTopic.isBlank();

        if (optimistic && !channelConfiguration.stateTopic.isBlank()) {
            throw new ConfigurationException("Component:Switch does not support forced optimistic mode");
        }

        String stateOn = channelConfiguration.stateOn != null ? channelConfiguration.stateOn
                : channelConfiguration.payloadOn;
        String stateOff = channelConfiguration.stateOff != null ? channelConfiguration.stateOff
                : channelConfiguration.payloadOff;

        OnOffValue value = new OnOffValue(stateOn, stateOff, channelConfiguration.payloadOn,
                channelConfiguration.payloadOff);

        buildChannel(SWITCH_CHANNEL_ID, value, "state", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .build();
    }
}
