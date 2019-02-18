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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.values.PercentageValue;
import org.openhab.binding.mqtt.generic.internal.values.RollershutterValue;
import org.openhab.binding.mqtt.generic.internal.values.Value;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT Cover component, following the https://www.home-assistant.io/components/cover.mqtt/ specification.
 *
 * Only Open/Close/Stop works so far.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentCover extends AbstractComponent<ComponentCover.Config> {
    public static final String coverChannelID = "cover"; // Randomly chosen channel "ID"
    public static final String posChannelID = "position"; // Randomly chosen channel "ID"
    public static final String tiltChannelID = "tilt"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config extends AbstractConfiguration {
        public Config() {
            super("MQTT Cover");
        }

        @SerializedName(value = "optimistic", alternate = "opt")
        protected boolean optimistic = false;

        @SerializedName(value = "state_topic", alternate = "stat_t")
        protected @Nullable String state_topic;
        @SerializedName(value = "payload_open", alternate = "pl_open")
        protected String payload_open = "OPEN";
        @SerializedName(value = "payload_close", alternate = "pl_cls")
        protected String payload_close = "CLOSE";
        @SerializedName(value = "payload_stop", alternate = "pl_stop")
        protected String payload_stop = "STOP";

        @SerializedName(value = "command_topic", alternate = "cmd_t")
        protected @Nullable String command_topic;

        @SerializedName(value = "position_topic", alternate = "pos_t")
        protected @Nullable String position_topic;
        @SerializedName(value = "position_open", alternate = "pos_open")
        protected int position_open = 100;
        @SerializedName(value = "payload_close", alternate = "pos_cls")
        protected int position_close = 0;

        @SerializedName(value = "set_position_topic", alternate = "set_pos_t")
        protected @Nullable String set_position_topic;
        @SerializedName(value = "set_position_template", alternate = "set_pos_tpl")
        protected @Nullable String set_position_template;

        @SerializedName(value = "tilt_command_topic", alternate = "tilt_cmd_t")
        protected @Nullable String tilt_command_topic;
        @SerializedName(value = "tilt_status_topic", alternate = "tilt_status_t")
        protected @Nullable String tilt_status_topic;
        protected int tilt_min = 0;
        protected int tilt_max = 100;
        @SerializedName(value = "tilt_closed_value", alternate = "tilt_clsd_val")
        protected int tilt_closed_value = 0;
        @SerializedName(value = "tilt_opened_value", alternate = "tilt_opnd_val")
        protected int tilt_opened_value = 100;
        @SerializedName(value = "tilt_status_optimistic", alternate = "tilt_status_opt")
        protected boolean tilt_status_optimistic = false;
        @SerializedName(value = "tilt_invert_state", alternate = "tilt_inv_stat")
        protected boolean tilt_invert_state = false;
    };

    public ComponentCover(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener updateListener, Gson gson, TransformationServiceProvider provider) {
        super(thing, haID, configJSON, Config.class, gson);

        // how to handle position_open, position_close?

        RollershutterValue value = new RollershutterValue(config.payload_open, config.payload_close,
                config.payload_stop);

        if (config.command_topic != null || config.state_topic != null || config.position_topic != null) {
            @Nullable
            String state_topic = config.position_topic;
            if (state_topic == null) {
                state_topic = config.state_topic;
            }

            CChannel coverChannel = new CChannel(this, coverChannelID, value, //
                    config.expand(state_topic), config.expand(config.command_topic), config.retain, "State", "",
                    updateListener);
            coverChannel.addTemplateIn(provider, config.value_template);
            addChannel(coverChannel);
        }

        if (config.position_topic != null) {
            CChannel posChannel = new CChannel(this, posChannelID, value, //
                    config.expand(config.set_position_topic), config.retain, "Position", "%", updateListener);

            posChannel.addTemplateOut(provider, config.set_position_template);

            addChannel(posChannel);
        }

        if (config.tilt_command_topic != null || config.tilt_status_topic != null) {
            Value tiltValue;

            if (config.tilt_invert_state) {
                tiltValue = new PercentageValue(BigDecimal.valueOf(config.tilt_max),
                        BigDecimal.valueOf(config.tilt_min), null, "" + config.tilt_opened_value,
                        "" + config.tilt_closed_value);
            } else {
                tiltValue = new PercentageValue(BigDecimal.valueOf(config.tilt_min),
                        BigDecimal.valueOf(config.tilt_max), null, "" + config.tilt_opened_value,
                        "" + config.tilt_closed_value);
            }

            CChannel tiltChannel = new CChannel(this, tiltChannelID, tiltValue, //
                    config.expand(config.tilt_status_topic), config.expand(config.tilt_command_topic), config.retain,
                    "Tilt", "", updateListener);
            tiltChannel.addTemplateIn(provider, config.value_template);
            addChannel(tiltChannel);
        }
    }
}
