/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.homeassistant.internal.exception.ConfigurationException;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT select, following the https://www.home-assistant.io/components/select.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Select extends AbstractComponent<Select.ChannelConfiguration> {
    public static final String SELECT_CHANNEL_ID = "select"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Select");
        }

        protected @Nullable Boolean optimistic;

        @SerializedName("command_template")
        protected @Nullable String commandTemplate;
        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("state_topic")
        protected String stateTopic = "";

        protected String[] options = new String[0];

        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
    }

    public Select(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        boolean optimistic = channelConfiguration.optimistic != null ? channelConfiguration.optimistic
                : channelConfiguration.stateTopic.isBlank();

        if (optimistic && !channelConfiguration.stateTopic.isBlank()) {
            throw new ConfigurationException("Component:Select does not support forced optimistic mode");
        }

        TextValue value = new TextValue(channelConfiguration.options);

        buildChannel(SELECT_CHANNEL_ID, value, getName(), componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                .build();
    }
}
