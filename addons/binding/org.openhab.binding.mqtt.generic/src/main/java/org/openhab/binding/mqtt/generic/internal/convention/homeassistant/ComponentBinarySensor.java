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
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;

import com.google.gson.Gson;

/**
 * A MQTT BinarySensor, following the https://www.home-assistant.io/components/binary_sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentBinarySensor extends AbstractComponent<ComponentBinarySensor.Config> {
    public static final String sensorChannelID = "sensor"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends HAConfiguration {
        Config() {
            super("MQTT Binary Sensor");
        }

        protected String unit_of_measurement = "";
        protected @Nullable String device_class;
        protected boolean force_update = false;
        protected int expire_after = 0;

        protected String state_topic = "";
        protected String payload_on = "ON";
        protected String payload_off = "OFF";
    };

    public ComponentBinarySensor(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson) {
        super(thing, haID, configJSON, gson, Config.class);

        if (config.force_update) {
            throw new UnsupportedOperationException("Component:Sensor does not support forced updates");
        }

        channels.put(sensorChannelID,
                new CChannel(this, sensorChannelID, new OnOffValue(config.payload_on, config.payload_off),
                        config.state_topic, null, config.name, config.unit_of_measurement, channelStateUpdateListener));
    }

}
