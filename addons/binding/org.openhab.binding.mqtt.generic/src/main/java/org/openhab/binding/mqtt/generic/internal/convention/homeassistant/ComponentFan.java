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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;
import org.openhab.binding.mqtt.generic.internal.values.TextValue;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Fan component, following the https://www.home-assistant.io/components/fan.mqtt/ specification.
 *
 * Only ON/OFF is supported so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentFan extends AbstractComponent<ComponentFan.Config> {
    public static final String fanChannelID = "fan"; // Randomly chosen channel "ID"
    public static final String oscChannelID = "osc"; // Randomly chosen channel "ID"
    public static final String speedChannelID = "speed"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Fan");
        }

        @SerializedName(value = "optimistic", alternate = "opt")
        protected boolean optimistic = false;

        @SerializedName(value = "state_value_template", alternate = "stat_val_tpl")
        protected @Nullable String state_value_template;

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected @Nullable String state_topic;
        @SerializedName(value = "payload_on", alternate = "pl_on")
        protected String payload_on = "ON";
        @SerializedName(value = "payload_off", alternate = "pl_off")
        protected String payload_off = "OFF";

        @SerializedName(value = "command_topic", alternate = "cmd_t")
        protected String command_topic = "";

        @SerializedName(value = "oscillation_state_topic", alternate = "osc_stat_t")
        protected @Nullable String oscillation_state_topic;
        @SerializedName(value = "", alternate = "osc_cmd_t")
        protected @Nullable String oscillation_command_topic;
        @SerializedName(value = "payload_oscillation_on", alternate = "pl_osc_on")
        protected String payload_oscillation_on = "oscillate_on";
        @SerializedName(value = "payload_oscillation_off", alternate = "pl_osc_off")
        protected String payload_oscillation_off = "oscillate_off";
        @SerializedName(value = "oscillation_value_template", alternate = "osc_val_tpl")
        protected @Nullable String oscillation_value_template;

        @SerializedName(value = "speed_state_topic", alternate = "spd_stat_t")
        protected @Nullable String speed_state_topic;
        @SerializedName(value = "speed_command_topic", alternate = "spd_cmd_t")
        protected @Nullable String speed_command_topic;
        @SerializedName(value = "payload_low_speed", alternate = "pl_lo_spd")
        protected String payload_low_speed = "low";
        @SerializedName(value = "payload_medium_speed", alternate = "pl_med_spd")
        protected String payload_medium_speed = "medium";
        @SerializedName(value = "payload_high_speed", alternate = "pl_hi_spd")
        protected String payload_high_speed = "high";
        @SerializedName(value = "speed_value_template", alternate = "spd_val_tpl")
        protected @Nullable String speed_value_template;

        @SerializedName(value = "speeds", alternate = "spds")
        protected @Nullable List<String> speeds;
    };

    public ComponentFan(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener updateListener, Gson gson, TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        // command_topic is required
        OnOffValue value = new OnOffValue(config.payload_on, config.payload_off);
        CChannel fanChannel = new CChannel(this, fanChannelID, value, //
                config.expand(config.state_topic), config.expand(config.command_topic), config.retain, "Power", "",
                updateListener);
        fanChannel.addTemplateIn(provider, config.state_value_template);
        addChannel(fanChannel);

        if (config.oscillation_state_topic != null || config.oscillation_command_topic != null) {
            TextValue oscillation_value = new TextValue(
                    new String[] { config.payload_oscillation_on, config.payload_oscillation_off });
            CChannel oscChannel = new CChannel(this, oscChannelID, oscillation_value, //
                    config.expand(config.oscillation_state_topic), config.expand(config.oscillation_command_topic),
                    config.retain, "Oscillation", "", updateListener);
            oscChannel.addTemplateIn(provider, config.oscillation_value_template);
            addChannel(oscChannel);
        }
        if (config.speed_state_topic != null || config.speed_command_topic != null) {
            TextValue speed_value;

            if (config.speeds != null) {
                speed_value = new TextValue(config.speeds.toArray(new String[0]));
            } else {
                speed_value = new TextValue(new String[] { config.payload_off, config.payload_low_speed,
                        config.payload_medium_speed, config.payload_high_speed });
            }
            CChannel speedChannel = new CChannel(this, speedChannelID, speed_value, //
                    config.expand(config.speed_state_topic), config.expand(config.speed_command_topic), config.retain,
                    "Speed", "", updateListener);
            speedChannel.addTemplateIn(provider, config.speed_value_template);
            addChannel(speedChannel);
        }
    }
}
