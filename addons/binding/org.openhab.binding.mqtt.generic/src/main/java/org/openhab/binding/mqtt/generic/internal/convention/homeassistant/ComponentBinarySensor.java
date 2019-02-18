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
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

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
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Binary Sensor");
        }

        @SerializedName(value = "device_class", alternate = "dev_cla")
        protected @Nullable String device_class;
        @SerializedName(value = "force_update", alternate = "frc_upd")
        protected boolean force_update = false;
        @SerializedName(value = "expire_after", alternate = "exp_aft")
        protected int expire_after = 0;

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected String state_topic = "";
        @SerializedName(value = "payload_on", alternate = "pl_on")
        protected String payload_on = "ON";
        @SerializedName(value = "payload_off", alternate = "pl_off")
        protected String payload_off = "OFF";
    };

    public ComponentBinarySensor(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson,
            TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        if (config.force_update) {
            throw new UnsupportedOperationException("Component:Sensor does not support forced updates");
        }

        CChannel sensorChannel = new CChannel(this, sensorChannelID,
                new OnOffValue(config.payload_on, config.payload_off), config.expand(config.state_topic), "State", "",
                channelStateUpdateListener);
        sensorChannel.addTemplateIn(provider, config.value_template);

        addChannel(sensorChannel);
    }
}
