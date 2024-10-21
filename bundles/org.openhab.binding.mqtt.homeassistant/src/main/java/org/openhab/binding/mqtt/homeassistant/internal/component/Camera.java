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
import org.openhab.binding.mqtt.generic.values.ImageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.thing.type.AutoUpdatePolicy;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT camera, following the https://www.home-assistant.io/components/camera.mqtt/ specification.
 *
 * At the moment this only notifies the user that this feature is not yet supported.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Camera extends AbstractComponent<Camera.ChannelConfiguration> {
    public static final String CAMERA_CHANNEL_ID = "camera";
    public static final String JSON_ATTRIBUTES_CHANNEL_ID = "json-attributes";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Camera");
        }

        protected String topic = "";

        @SerializedName("json_attributes_template")
        protected @Nullable String jsonAttributesTemplate;
        @SerializedName("json_attributes_topic")
        protected @Nullable String jsonAttributesTopic;
    }

    public Camera(ComponentFactory.ComponentConfiguration componentConfiguration, boolean newStyleChannels) {
        super(componentConfiguration, ChannelConfiguration.class, newStyleChannels);

        ImageValue value = new ImageValue();

        buildChannel(CAMERA_CHANNEL_ID, ComponentChannelType.IMAGE, value, getName(),
                componentConfiguration.getUpdateListener()).stateTopic(channelConfiguration.topic).build();

        if (channelConfiguration.jsonAttributesTopic != null) {
            buildChannel(JSON_ATTRIBUTES_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "JSON Attributes",
                    componentConfiguration.getUpdateListener())
                    .stateTopic(channelConfiguration.jsonAttributesTopic, channelConfiguration.jsonAttributesTemplate)
                    .withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build();
        }

        finalizeChannels();
    }
}
