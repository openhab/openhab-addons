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
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT select, following the https://www.home-assistant.io/integrations/text.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Text extends AbstractComponent<Text.ChannelConfiguration> {
    public static final String TEXT_CHANNEL_ID = "text";

    public static final String MODE_TEXT = "text";
    public static final String MODE_PASSWORD = "password";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Text");
        }

        @SerializedName("command_template")
        protected @Nullable String commandTemplate;
        @SerializedName("command_topic")
        protected String commandTopic = "";
        @SerializedName("state_topic")
        protected @Nullable String stateTopic;

        // openHAB has no way to handle these restrictions in its UI
        protected int min = 0; // Minimum and maximum length of the display we're controlling
        protected int max = 255;
        protected @Nullable String pattern; // Regular expression
        protected String mode = MODE_TEXT; // Presumably for a password, it should mask any controls in the UI
    }

    public Text(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        TextValue value = new TextValue();

        buildChannel(TEXT_CHANNEL_ID, ComponentChannelType.STRING, value, getName(),
                componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos(), channelConfiguration.commandTemplate)
                .inferOptimistic(false).build();
        finalizeChannels();
    }
}
