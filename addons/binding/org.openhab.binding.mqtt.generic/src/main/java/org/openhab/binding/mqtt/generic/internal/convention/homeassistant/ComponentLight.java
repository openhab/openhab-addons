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
import org.openhab.binding.mqtt.generic.internal.values.ColorValue;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT light, following the https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * This class condenses the three state/command topics (for ON/OFF, Brightness, Color) to one
 * color channel.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentLight extends AbstractComponent<ComponentLight.Config> {
    public static final String lightChannelID = "light"; // Randomly chosen channel "ID"
    public static final String brightnessChannelID = "brightness"; // Randomly chosen channel "ID"
    public static final String rgbChannelID = "color"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Light");
        }

        @SerializedName(value = "optimistic", alternate = "opt")
        protected boolean optimistic = false;

        // Defines when on the payload_on is sent. Using last (the default) will send any style (brightness, color, etc)
        // topics first and then a payload_on to the command_topic. Using first will send the payload_on and then any
        // style topics. Using brightness will only send brightness commands instead of the payload_on to turn the light
        // on.
        @SerializedName(value = "on_command_type", alternate = "on_cmd_type")
        protected String on_command_type = "last";

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected @Nullable String state_topic;
        @SerializedName(value = "command_topic", alternate = "cmd_t")
        protected @Nullable String command_topic;
        @SerializedName(value = "state_value_template", alternate = "stat_val_tpl")
        protected @Nullable String state_value_template;

        @SerializedName(value = "brightness_scale", alternate = "bri_scl")
        protected int brightness_scale = 255;
        @SerializedName(value = "brightness_state_topic", alternate = "bri_stat_t")
        protected @Nullable String brightness_state_topic;
        @SerializedName(value = "brightness_command_topic", alternate = "bri_cmd_t")
        protected @Nullable String brightness_command_topic;
        @SerializedName(value = "brightness_value_template", alternate = "bri_val_tpl")
        protected @Nullable String brightness_value_template;

        @SerializedName(value = "color_temp_state_topic", alternate = "clr_temp_stat_t")
        protected @Nullable String color_temp_state_topic;
        @SerializedName(value = "color_temp_command_topic", alternate = "clr_temp_cmd_t")
        protected @Nullable String color_temp_command_topic;
        @SerializedName(value = "color_temp_value_template", alternate = "clr_temp_val_tpl")
        protected @Nullable String color_temp_value_template;

        @SerializedName(value = "effect_command_topic", alternate = "fx_cmd_t")
        protected @Nullable String effect_command_topic;
        @SerializedName(value = "effect_state_topic", alternate = "fx_stat_t")
        protected @Nullable String effect_state_topic;
        @SerializedName(value = "effect_value_template", alternate = "fx_val_tpl")
        protected @Nullable String effect_value_template;
        @SerializedName(value = "effect_list", alternate = "fx_list")
        protected @Nullable List<String> effect_list;

        @SerializedName(value = "hs_command_topic", alternate = "hs_cmd_t")
        protected @Nullable String hs_command_topic;
        @SerializedName(value = "hs_state_topic", alternate = "hs_stat_t")
        protected @Nullable String hs_state_topic;
        @SerializedName(value = "hs_value_template", alternate = "hs_val_tpl")
        protected @Nullable String hs_value_template;

        @SerializedName(value = "rgb_command_topic", alternate = "rgb_cmd_t")
        protected @Nullable String rgb_command_topic;
        @SerializedName(value = "rgb_state_topic", alternate = "rgb_stat_t")
        protected @Nullable String rgb_state_topic;
        @SerializedName(value = "rgb_value_template", alternate = "rgb_val_tpl")
        protected @Nullable String rgb_value_template;
        @SerializedName(value = "rgb_command_template", alternate = "rgb_cmd_tpl")
        protected @Nullable String rgb_command_template;

        @SerializedName(value = "white_value_command_topic", alternate = "whit_val_cmd_t")
        protected @Nullable String white_value_command_topic;
        @SerializedName(value = "white_value_state_topic", alternate = "whit_val_stat_t")
        protected @Nullable String white_value_state_topic;
        @SerializedName(value = "white_value_template", alternate = "whit_val_tpl")
        protected @Nullable String white_value_template;

        @SerializedName(value = "xy_command_topic", alternate = "xy_cmd_t")
        protected @Nullable String xy_command_topic;
        @SerializedName(value = "xy_state_topic", alternate = "xy_stat_t")
        protected @Nullable String xy_state_topic;
        @SerializedName(value = "xy_value_template", alternate = "xy_val_tpl")
        protected @Nullable String xy_value_template;

        @SerializedName(value = "payload_on", alternate = "pl_on")
        protected String payload_on = "ON";
        @SerializedName(value = "payload_off", alternate = "pl_off")
        protected String payload_off = "OFF";
    };

    public ComponentLight(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson,
            TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        ColorValue value = new ColorValue(true, config.payload_on, config.payload_off, 100);

        if (config.state_topic != null || config.command_topic != null) {
            CChannel lightChannel = new CChannel(this, lightChannelID, value, //
                    config.expand(config.state_topic), config.expand(config.command_topic), config.retain, "Light", "",
                    channelStateUpdateListener);
            lightChannel.addTemplateIn(provider, config.state_value_template);
            addChannel(lightChannel);
        }

        if (config.rgb_state_topic != null || config.rgb_command_topic != null) {
            CChannel rgbChannel = new CChannel(this, rgbChannelID, value, //
                    config.expand(config.rgb_state_topic), config.expand(config.rgb_command_topic), config.retain,
                    "RGB", "", channelStateUpdateListener);
            rgbChannel.addTemplateIn(provider, config.rgb_value_template);
            addChannel(rgbChannel);
        }

        if (config.brightness_state_topic != null || config.brightness_command_topic != null) {
            CChannel brightnessChannel = new CChannel(this, brightnessChannelID, value, //
                    config.expand(config.brightness_state_topic), config.expand(config.brightness_command_topic),
                    config.retain, "Brightness", "", channelStateUpdateListener);

            brightnessChannel.addTemplateIn(provider, config.brightness_value_template);
            addChannel(brightnessChannel);
        }

        // TODO
    }
}
