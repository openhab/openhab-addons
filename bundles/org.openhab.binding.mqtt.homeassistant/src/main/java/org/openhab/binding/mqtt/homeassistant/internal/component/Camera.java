/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.values.ImageValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.EntityConfiguration;

/**
 * A MQTT camera, following the https://www.home-assistant.io/components/camera.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class Camera extends AbstractComponent<Camera.Configuration> {
    public static final String CAMERA_CHANNEL_ID = "camera";

    public static class Configuration extends EntityConfiguration {
        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT Camera");
        }

        String getTopic() {
            return getString("topic");
        }
    }

    public Camera(ComponentFactory.ComponentContext componentContext) {
        super(componentContext, Configuration.class);

        ImageValue value = new ImageValue();

        buildChannel(CAMERA_CHANNEL_ID, ComponentChannelType.IMAGE, value, "Camera",
                componentContext.getUpdateListener()).stateTopic(config.getTopic()).build();

        finalizeChannels();
    }
}
