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
import org.openhab.core.thing.type.AutoUpdatePolicy;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT scene, following the https://www.home-assistant.io/integrations/scene.mqtt/ specification.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class Scene extends AbstractComponent<Scene.ChannelConfiguration> {
    public static final String SCENE_CHANNEL_ID = "scene";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Scene");
        }

        @SerializedName("command_topic")
        protected @Nullable String commandTopic;

        @SerializedName("payload_on")
        protected String payloadOn = "ON";
    }

    public Scene(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        TextValue value = new TextValue(new String[] { channelConfiguration.payloadOn });

        buildChannel(SCENE_CHANNEL_ID, ComponentChannelType.STRING, value, getName(),
                componentConfiguration.getUpdateListener())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();

        finalizeChannels();
    }
}
