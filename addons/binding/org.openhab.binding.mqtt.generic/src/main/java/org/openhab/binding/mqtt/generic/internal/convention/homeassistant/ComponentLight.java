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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.internal.values.ColorValue;

import com.google.gson.Gson;

/**
 * A MQTT light, following the https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * This class condenses the three state/command topics (for ON/OFF, Brightness, Color) to one
 * color channel.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ComponentLight extends AbstractComponent implements ChannelStateUpdateListener {
    public static final String switchChannelID = "light"; // Randomly chosen channel "ID"
    public static final String brightnessChannelID = "brightness"; // Randomly chosen channel "ID"
    public static final String colorChannelID = "color"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class Config {
        protected String name = "MQTT Light";
        protected String icon = "";
        protected int qos = 1;
        protected boolean retain = true;
        protected @Nullable String unique_id;

        protected int brightness_scale = 255;
        protected boolean optimistic = false;
        protected @Nullable List<String> effect_list;

        // Defines when on the payload_on is sent. Using last (the default) will send any style (brightness, color, etc)
        // topics first and then a payload_on to the command_topic. Using first will send the payload_on and then any
        // style topics. Using brightness will only send brightness commands instead of the payload_on to turn the light
        // on.
        protected String on_command_type = "last";

        protected @Nullable String state_topic;
        protected @Nullable String command_topic;
        protected @Nullable String state_value_template;

        protected @Nullable String brightness_state_topic;
        protected @Nullable String brightness_command_topic;
        protected @Nullable String brightness_value_template;

        protected @Nullable String color_temp_state_topic;
        protected @Nullable String color_temp_command_topic;
        protected @Nullable String color_temp_value_template;

        protected @Nullable String effect_command_topic;
        protected @Nullable String effect_state_topic;
        protected @Nullable String effect_value_template;

        protected @Nullable String rgb_command_topic;
        protected @Nullable String rgb_state_topic;
        protected @Nullable String rgb_value_template;
        protected @Nullable String rgb_command_template;

        protected @Nullable String white_value_command_topic;
        protected @Nullable String white_value_state_topic;
        protected @Nullable String white_value_template;

        protected @Nullable String xy_command_topic;
        protected @Nullable String xy_state_topic;
        protected @Nullable String xy_value_template;

        protected String payload_on = "ON";
        protected String payload_off = "OFF";

        protected @Nullable String availability_topic;
        protected String payload_available = "online";
        protected String payload_not_available = "offline";
    };

    protected Config config = new Config();
    protected CChannel colorChannel;
    protected CChannel switchChannel;
    protected CChannel brightnessChannel;
    private final @Nullable ChannelStateUpdateListener channelStateUpdateListener;

    public ComponentLight(ThingUID thing, HaID haID, String configJSON,
            @Nullable ChannelStateUpdateListener channelStateUpdateListener, Gson gson) {
        super(thing, haID, configJSON, gson);
        this.channelStateUpdateListener = channelStateUpdateListener;
        config = gson.fromJson(configJSON, Config.class);

        ColorValue value = new ColorValue(true, config.payload_on, config.payload_off, 100);

        // Create three MQTT subscriptions and use this class object as update listener
        switchChannel = new CChannel(this, switchChannelID, value, //
                config.state_topic, config.command_topic, config.name, "", this);
        colorChannel = new CChannel(this, colorChannelID, value, //
                config.rgb_state_topic, config.rgb_command_topic, config.name, "", this);
        brightnessChannel = new CChannel(this, brightnessChannelID, value, //
                config.brightness_state_topic, config.brightness_command_topic, config.name, "", this);

        channels.put(switchChannelID, colorChannel);
    }

    @Override
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return Stream.of(switchChannel, brightnessChannel, colorChannel) //
                .map(v -> v.channelState.start(connection, scheduler, timeout)) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    @Override
    public CompletableFuture<@Nullable Void> stop() {
        return Stream.of(switchChannel, brightnessChannel, colorChannel) //
                .map(v -> v.channelState.stop()) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    @Override
    public String name() {
        return config.name;
    }

    /**
     * Proxy method to condense all three MQTT subscriptions to one channel
     */
    @Override
    public void updateChannelState(ChannelUID channelUID, State value) {
        ChannelStateUpdateListener listener = channelStateUpdateListener;
        if (listener != null) {
            listener.updateChannelState(colorChannel.channelUID, value);
        }
    }

    /**
     * Proxy method to condense all three MQTT subscriptions to one channel
     */
    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        ChannelStateUpdateListener listener = channelStateUpdateListener;
        if (listener != null) {
            listener.postChannelCommand(colorChannel.channelUID, value);
        }
    }

    /**
     * Proxy method to condense all three MQTT subscriptions to one channel
     */
    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        ChannelStateUpdateListener listener = channelStateUpdateListener;
        if (listener != null) {
            listener.triggerChannel(colorChannel.channelUID, eventPayload);
        }
    }
}
