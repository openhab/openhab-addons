/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.OnOffValue;

/**
 * A MQTT switch, following the https://www.home-assistant.io/components/switch.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentSwitch extends AbstractComponent<ComponentSwitch.ChannelConfiguration> {
    public static final String switchChannelID = "switch"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Switch");
        }

        protected @Nullable Boolean optimistic;

        protected String state_topic = "";
        protected String state_on = "ON";
        protected String state_off = "OFF";
        protected @Nullable String command_topic;
        protected String payload_on = "ON";
        protected String payload_off = "OFF";
    }

    public ComponentSwitch(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        boolean optimistic = channelConfiguration.optimistic != null ? channelConfiguration.optimistic
                : StringUtils.isBlank(channelConfiguration.state_topic);

        if (optimistic && StringUtils.isNotBlank(channelConfiguration.state_topic)) {
            throw new UnsupportedOperationException("Component:Switch does not support forced optimistic mode");
        }

        String state_on = channelConfiguration.state_on != null ? channelConfiguration.state_on
                : channelConfiguration.payload_on;
        String state_off = channelConfiguration.state_off != null ? channelConfiguration.state_off
                : channelConfiguration.payload_off;

        OnOffValue value = new OnOffValue(state_on, state_off, channelConfiguration.payload_on,
                channelConfiguration.payload_off);

        buildChannel(switchChannelID, value, "state", componentConfiguration.getUpdateListener())//
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template)//
                .commandTopic(channelConfiguration.command_topic, channelConfiguration.retain, channelConfiguration.qos)//
                .build();
    }
}
