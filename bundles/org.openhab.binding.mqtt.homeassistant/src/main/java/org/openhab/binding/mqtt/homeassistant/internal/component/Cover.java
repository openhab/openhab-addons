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
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Cover component, following the https://www.home-assistant.io/components/cover.mqtt/ specification.
 *
 * Only Open/Close/Stop works so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Cover extends AbstractComponent<Cover.ChannelConfiguration> {
    public static final String SWITCH_CHANNEL_ID = "cover"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Cover");
        }

        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("payload_open")
        protected String payloadOpen = "OPEN";
        @SerializedName("payload_close")
        protected String payloadClose = "CLOSE";
        @SerializedName("payload_stop")
        protected String payloadStop = "STOP";
    }

    public Cover(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        RollershutterValue value = new RollershutterValue(channelConfiguration.payloadOpen,
                channelConfiguration.payloadClose, channelConfiguration.payloadStop);

        buildChannel(SWITCH_CHANNEL_ID, value, channelConfiguration.getName(),
                componentConfiguration.getUpdateListener())
                        .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                        .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                                channelConfiguration.getQos())
                        .build();
    }
}
