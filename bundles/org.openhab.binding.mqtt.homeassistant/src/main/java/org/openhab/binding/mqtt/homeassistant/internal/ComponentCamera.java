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
import org.openhab.binding.mqtt.generic.values.ImageValue;

/**
 * A MQTT camera, following the https://www.home-assistant.io/components/camera.mqtt/ specification.
 *
 * At the moment this only notifies the user that this feature is not yet supported.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentCamera extends AbstractComponent<ComponentCamera.ChannelConfiguration> {
    public static final String cameraChannelID = "camera"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends BaseChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Camera");
        }

        protected String topic = "";
    }

    public ComponentCamera(CFactory.ComponentConfiguration componentConfiguration) {
        super(componentConfiguration, ChannelConfiguration.class);

        ImageValue value = new ImageValue();

        buildChannel(cameraChannelID, value, channelConfiguration.name, componentConfiguration.getUpdateListener())//
                .stateTopic(channelConfiguration.topic)//
                .build();
    }
}
