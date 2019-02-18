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
import org.openhab.binding.mqtt.generic.internal.values.NumberValue;
import org.openhab.binding.mqtt.generic.internal.values.TextValue;
import org.openhab.binding.mqtt.generic.internal.values.Value;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT sensor, following the https://www.home-assistant.io/components/sensor.mqtt/ specification.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentSensor extends AbstractComponent<ComponentSensor.Config> {
    public static final String sensorChannelID = "sensor"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Sensor");
        }

        @SerializedName(value = "unit_of_measurement", alternate = "unit_of_meas")
        protected @Nullable String unit_of_measurement;
        @SerializedName(value = "device_class", alternate = "dev_cla")
        protected @Nullable String device_class;
        @SerializedName(value = "force_update", alternate = "frc_upd")
        protected boolean force_update = false;
        @SerializedName(value = "expire_after", alternate = "exp_aft")
        protected int expire_after = 0;

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected String state_topic = "";

        @SerializedName(value = "json_attributes_topic", alternate = "json_attr_t")
        protected @Nullable String json_attributes_topic;
    };

    public ComponentSensor(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson,
            TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        if (config.force_update) {
            throw new UnsupportedOperationException("Component:Sensor does not support forced updates");
        }

        Value value;
        if (config.unit_of_measurement != null) {
            value = new NumberValue(null, null, null);
        } else {
            value = new TextValue();
        }

        CChannel sensorChannel = new CChannel(this, sensorChannelID, value, //
                config.expand(config.state_topic), "Value", defString(config.unit_of_measurement, ""),
                channelStateUpdateListener);

        sensorChannel.addTemplateIn(provider, config.value_template);

        addChannel(sensorChannel);

        // json_attributes_topic: these would need to be written to thing properties....
    }
}
