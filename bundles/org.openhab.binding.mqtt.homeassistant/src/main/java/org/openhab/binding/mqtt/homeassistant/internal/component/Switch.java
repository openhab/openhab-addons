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
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

/**
 * A MQTT switch, following the https://www.home-assistant.io/components/switch.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Switch extends AbstractComponent<Switch.ChannelConfiguration> {
    public static final String switchChannelID = "switch"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Switch");
        }

        protected @Nullable Boolean optimistic;

        protected @Nullable String command_topic;
        protected String state_topic = "";

        protected @Nullable String state_on;
        protected @Nullable String state_off;
        protected String payload_on = "ON";
        protected String payload_off = "OFF";

        protected @Nullable String json_attributes_topic;
        protected @Nullable String json_attributes_template;
    }

    public Switch(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        boolean optimistic = channelConfiguration.optimistic != null ? channelConfiguration.optimistic
                : channelConfiguration.state_topic.isBlank();

        if (optimistic && !channelConfiguration.state_topic.isBlank()) {
            throw new UnsupportedOperationException("Component:Switch does not support forced optimistic mode");
        }

        String state_on = channelConfiguration.state_on != null ? channelConfiguration.state_on
                : channelConfiguration.payload_on;
        String state_off = channelConfiguration.state_off != null ? channelConfiguration.state_off
                : channelConfiguration.payload_off;

        OnOffValue value = new OnOffValue(state_on, state_off, channelConfiguration.payload_on,
                channelConfiguration.payload_off);

        buildChannel(switchChannelID, value, "state", componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.command_topic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .build();
    }
}
