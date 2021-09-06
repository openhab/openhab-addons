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
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

/**
 * A MQTT Cover component, following the https://www.home-assistant.io/components/cover.mqtt/ specification.
 *
 * Only Open/Close/Stop works so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Cover extends AbstractComponent<Cover.ChannelConfiguration> {
    public static final String switchChannelID = "cover"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Cover");
        }

        protected @Nullable String state_topic;
        protected @Nullable String command_topic;
        protected String payload_open = "OPEN";
        protected String payload_close = "CLOSE";
        protected String payload_stop = "STOP";
    }

    public Cover(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        RollershutterValue value = new RollershutterValue(channelConfiguration.payload_open,
                channelConfiguration.payload_close, channelConfiguration.payload_stop);

        buildChannel(switchChannelID, value, channelConfiguration.getName(), componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.state_topic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.command_topic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .build();
    }
}
