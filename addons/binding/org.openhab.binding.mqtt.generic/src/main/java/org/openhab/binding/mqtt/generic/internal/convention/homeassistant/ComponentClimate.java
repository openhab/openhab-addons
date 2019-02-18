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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.values.NumberValue;
import org.openhab.binding.mqtt.generic.internal.values.OnOffValue;
import org.openhab.binding.mqtt.generic.internal.values.TextValue;
import org.openhab.binding.mqtt.generic.internal.values.Value;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT climate component, following the https://www.home-assistant.io/components/climate.mqtt/ specification.
 *
 * At the moment this only notifies the user that this feature is not yet supported.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentClimate extends AbstractComponent<ComponentClimate.Config> {
    public static final String currentChannelID = "current"; // Randomly chosen channel "ID"
    public static final String powerChannelID = "power"; // Randomly chosen channel "ID"
    public static final String modeChannelID = "mode"; // Randomly chosen channel "ID"
    public static final String temperatueChannelID = "tempertature"; // Randomly chosen channel "ID"
    public static final String fanChannelID = "fan"; // Randomly chosen channel "ID"
    public static final String swingChannelID = "swing"; // Randomly chosen channel "ID"
    public static final String awayChannelID = "away"; // Randomly chosen channel "ID"
    public static final String holdChannelID = "hold"; // Randomly chosen channel "ID"
    public static final String auxChannelID = "aux"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT HVAC");
        }

        protected boolean send_if_off = true;

        @SerializedName(value = "initial", alternate = "init")
        protected BigDecimal initial = BigDecimal.valueOf(21);

        @SerializedName(value = "current_temperature_topic", alternate = "curr_temp_t")
        protected @Nullable String current_temperature_topic;
        @SerializedName(value = "current_temperature_template", alternate = "curr_temp_tpl")
        protected @Nullable String current_temperature_template;

        @SerializedName(value = "power_command_topic", alternate = "pow_cmd_t")
        protected @Nullable String power_command_topic;
        @SerializedName(value = "payload_on", alternate = "pl_on")
        protected String payload_on = "ON";
        @SerializedName(value = "payload_off", alternate = "pl_off")
        protected String payload_off = "OFF";

        @SerializedName(value = "mode_command_topic", alternate = "mode_cmd_t")
        protected @Nullable String mode_command_topic;
        @SerializedName(value = "mode_state_topic", alternate = "mode_stat_t")
        protected @Nullable String mode_state_topic;
        @SerializedName(value = "mode_state_template", alternate = "mode_stat_tpl")
        protected @Nullable String mode_state_template;

        protected List<String> modes = Stream.of("auto", "off", "cool", "heat", "dry", "fan_only")
                .collect(Collectors.toList());

        @SerializedName(value = "temperature_command_topic", alternate = "temp_cmd_t")
        protected @Nullable String temperature_command_topic;
        @SerializedName(value = "temperature_state_topic", alternate = "temp_stat_t")
        protected @Nullable String temperature_state_topic;
        @SerializedName(value = "temperature_state_template", alternate = "temp_stat_tpl")
        protected @Nullable String temperature_state_template;
        protected @Nullable BigDecimal min_temp;
        protected @Nullable BigDecimal max_temp;
        protected BigDecimal temp_step = BigDecimal.ONE;

        @SerializedName(value = "fan_mode_command_topic", alternate = "fan_mode_cmd_t")
        protected @Nullable String fan_mode_command_topic;
        @SerializedName(value = "fan_mode_state_topic", alternate = "fan_mode_stat_t")
        protected @Nullable String fan_mode_state_topic;
        @SerializedName(value = "fan_mode_state_template", alternate = "fan_mode_stat_tpl")
        protected @Nullable String fan_mode_state_template;

        protected List<String> fan_modes = Stream.of("auto", "low", "medium", "high").collect(Collectors.toList());

        @SerializedName(value = "swing_mode_command_topic", alternate = "swing_mode_cmd_t")
        protected @Nullable String swing_mode_command_topic;
        @SerializedName(value = "swing_mode_state_topic", alternate = "swing_mode_stat_t")
        protected @Nullable String swing_mode_state_topic;
        @SerializedName(value = "swing_mode_state_template", alternate = "swing_mode_stat_tpl")
        protected @Nullable String swing_mode_state_template;

        protected List<String> swing_modes = Stream.of("on", "off").collect(Collectors.toList());

        @SerializedName(value = "away_mode_command_topic", alternate = "away_mode_cmd_t")
        protected @Nullable String away_mode_command_topic;
        @SerializedName(value = "away_mode_state_topic", alternate = "away_mode_stat_t")
        protected @Nullable String away_mode_state_topic;
        @SerializedName(value = "away_mode_state_template", alternate = "away_mode_stat_tpl")
        protected @Nullable String away_mode_state_template;

        @SerializedName(value = "hold_command_topic", alternate = "hold_cmd_t")
        protected @Nullable String hold_command_topic;
        @SerializedName(value = "hold_state_topic", alternate = "hold_stat_t")
        protected @Nullable String hold_state_topic;
        @SerializedName(value = "hold_state_template", alternate = "hold_stat_tpl")
        protected @Nullable String hold_state_template;

        @SerializedName(value = "aux_command_topic", alternate = "aux_cmd_t")
        protected @Nullable String aux_command_topic;
        @SerializedName(value = "aux_state_topic", alternate = "aux_stat_t")
        protected @Nullable String aux_state_topic;
        @SerializedName(value = "aux_state_template", alternate = "aux_stat_tpl")
        protected @Nullable String aux_state_template;
    }

    public ComponentClimate(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson,
            TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        if (config.current_temperature_topic != null) {
            Value value = new NumberValue(null, null, null);

            // There is no current temperature unit defined
            CChannel currentChannel = new CChannel(this, currentChannelID, value, //
                    config.expand(config.current_temperature_topic), "Current Temperature", "",
                    channelStateUpdateListener);

            currentChannel.addTemplateIn(provider, config.current_temperature_template, config.value_template);

            addChannel(currentChannel);
        }

        if (config.power_command_topic != null) {
            Value value = new OnOffValue(config.payload_on, config.payload_off);
            CChannel powerChannel = new CChannel(this, powerChannelID, value, //
                    config.expand(config.power_command_topic), config.retain, "Power", "", channelStateUpdateListener);

            addChannel(powerChannel);
        }

        if (config.mode_command_topic != null || config.mode_state_topic != null) {
            Value value = new TextValue(config.modes.toArray(new String[config.modes.size()]));

            CChannel modeChannel = new CChannel(this, modeChannelID, value, //
                    config.expand(config.mode_state_topic), config.expand(config.mode_command_topic), config.retain,
                    "Mode", "", channelStateUpdateListener);

            modeChannel.addTemplateIn(provider, config.mode_state_template, config.value_template);

            addChannel(modeChannel);
        }

        if (config.temperature_command_topic != null || config.temperature_state_topic != null) {
            Value value = new NumberValue(config.min_temp, config.max_temp, config.temp_step);

            // There is no temperature unit defined

            CChannel temperatueChannel = new CChannel(this, temperatueChannelID, value, //
                    config.expand(config.temperature_state_topic), config.expand(config.temperature_command_topic),
                    config.retain, "Temperature", "", channelStateUpdateListener);

            temperatueChannel.addTemplateIn(provider, config.temperature_state_template, config.value_template);

            addChannel(temperatueChannel);
        }

        if (config.fan_mode_command_topic != null || config.fan_mode_state_topic != null) {
            Value value = new TextValue(config.fan_modes.toArray(new String[config.fan_modes.size()]));

            CChannel fanChannel = new CChannel(this, fanChannelID, value, //
                    config.expand(config.fan_mode_state_topic), config.expand(config.fan_mode_command_topic),
                    config.retain, "Fan mode", "", channelStateUpdateListener);

            fanChannel.addTemplateIn(provider, config.mode_state_template, config.value_template);

            addChannel(fanChannel);
        }

        if (config.swing_mode_command_topic != null || config.swing_mode_state_topic != null) {
            Value value = new TextValue(config.swing_modes.toArray(new String[config.swing_modes.size()]));

            CChannel swingChannel = new CChannel(this, swingChannelID, value, //
                    config.expand(config.swing_mode_state_topic), config.expand(config.swing_mode_command_topic),
                    config.retain, "Swing mode", "", channelStateUpdateListener);

            swingChannel.addTemplateIn(provider, config.mode_state_template, config.value_template);

            addChannel(swingChannel);
        }

        if (config.away_mode_command_topic != null || config.away_mode_state_topic != null) {
            Value value = new TextValue(config.modes.toArray(new String[config.modes.size()]));

            CChannel awayChannel = new CChannel(this, awayChannelID, value, //
                    config.expand(config.away_mode_state_topic), config.expand(config.away_mode_command_topic),
                    config.retain, "Away mode", "", channelStateUpdateListener);

            awayChannel.addTemplateIn(provider, config.mode_state_template, config.value_template);

            addChannel(awayChannel);
        }

        if (config.away_mode_command_topic != null || config.away_mode_state_topic != null) {
            Value value = new TextValue(new String[] { config.payload_on, config.payload_off });

            CChannel awayChannel = new CChannel(this, awayChannelID, value, //
                    config.expand(config.away_mode_state_topic), config.expand(config.away_mode_command_topic),
                    config.retain, "Away mode", "", channelStateUpdateListener);

            awayChannel.addTemplateIn(provider, config.away_mode_state_template, config.value_template);

            addChannel(awayChannel);
        }

        if (config.hold_command_topic != null || config.hold_state_topic != null) {
            Value value = new TextValue(new String[] { config.payload_on, config.payload_off });

            CChannel holdChannel = new CChannel(this, holdChannelID, value, //
                    config.expand(config.hold_state_topic), config.expand(config.hold_command_topic), config.retain,
                    "Hold", "", channelStateUpdateListener);

            holdChannel.addTemplateIn(provider, config.hold_state_template, config.value_template);

            addChannel(holdChannel);
        }

        if (config.aux_command_topic != null || config.aux_state_topic != null) {
            Value value = new TextValue(new String[] { config.payload_on, config.payload_off });

            CChannel auxChannel = new CChannel(this, auxChannelID, value, //
                    config.expand(config.aux_state_topic), config.expand(config.aux_command_topic), config.retain,
                    "Aux", "", channelStateUpdateListener);

            auxChannel.addTemplateIn(provider, config.aux_state_template, config.value_template);

            addChannel(auxChannel);
        }
    }
}
