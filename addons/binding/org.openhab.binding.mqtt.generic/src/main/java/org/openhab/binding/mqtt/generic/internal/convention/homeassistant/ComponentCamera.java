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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.values.ImageValue;

import com.google.gson.Gson;

/**
 * A MQTT camera, following the https://www.home-assistant.io/components/camera.mqtt/ specification.
 *
 * At the moment this only notifies the user that this feature is not yet supported.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentCamera extends AbstractComponent<ComponentCamera.Config> {
    public static final String cameraChannelID = "camera"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends HAConfiguration {
        Config() {
            super("MQTT Camera");
        }

        protected String topic = "";
    };

    public ComponentCamera(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson) {
        super(thing, haID, configJSON, gson, Config.class);

        ImageValue value = new ImageValue();
        channels.put(cameraChannelID, new CChannel(this, cameraChannelID, value, //
                config.topic, null, config.name, "", channelStateUpdateListener));
    }

}
