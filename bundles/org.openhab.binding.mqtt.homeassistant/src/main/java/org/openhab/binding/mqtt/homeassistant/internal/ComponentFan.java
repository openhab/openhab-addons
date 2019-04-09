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
import org.openhab.binding.mqtt.generic.values.OnOffValue;

/**
 * A MQTT Fan component, following the https://www.home-assistant.io/components/fan.mqtt/ specification.
 *
 * Only ON/OFF is supported so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentFan extends AbstractComponent<ComponentFan.ChannelConfiguration> {
    public static final String switchChannelID = "fan"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Fan");
        }

        protected @Nullable String state_topic;
        protected String command_topic = "";
        protected String payload_on = "ON";
        protected String payload_off = "OFF";
    };

    public ComponentFan(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        OnOffValue value = new OnOffValue(channelConfiguration.payload_on, channelConfiguration.payload_off);
        buildChannel(switchChannelID, value, channelConfiguration.name)
                .listener(componentConfiguration.getUpdateListener())//
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.value_template)//
                .commandTopic(channelConfiguration.command_topic, channelConfiguration.retain)//
                .build();
    }
}
