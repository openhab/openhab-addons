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
import org.openhab.binding.mqtt.generic.values.ImageValue;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;

/**
 * A MQTT camera, following the https://www.home-assistant.io/components/camera.mqtt/ specification.
 *
 * At the moment this only notifies the user that this feature is not yet supported.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Camera extends AbstractComponent<Camera.ChannelConfiguration> {
    public static final String CAMERA_CHANNEL_ID = "camera"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Camera");
        }

        protected String topic = "";
    }

    public Camera(ComponentFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        ImageValue value = new ImageValue();

        buildChannel(CAMERA_CHANNEL_ID, value, getName(), componentConfiguration.getUpdateListener())
                .stateTopic(channelConfiguration.topic).build();
    }
}
