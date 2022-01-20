/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

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
public class Light extends AbstractComponent<Light.ChannelConfiguration> implements ChannelStateUpdateListener {
    public static final String SWITCH_CHANNEL_ID = "light"; // Randomly chosen channel "ID"
    public static final String BRIGHTNESS_CHANNEL_ID = "brightness"; // Randomly chosen channel "ID"
    public static final String COLOR_CHANNEL_ID = "color"; // Randomly chosen channel "ID"

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Light");
        }

        @SerializedName("brightness_scale")
        protected int brightnessScale = 255;
        protected boolean optimistic = false;
        @SerializedName("effect_list")
        protected @Nullable List<String> effectList;

        // Defines when on the payload_on is sent. Using last (the default) will send any style (brightness, color, etc)
        // topics first and then a payload_on to the command_topic. Using first will send the payload_on and then any
        // style topics. Using brightness will only send brightness commands instead of the payload_on to turn the light
        // on.
        @SerializedName("on_command_type")
        protected String onCommandType = "last";

        @SerializedName("state_topic")
        protected @Nullable String stateTopic;
        @SerializedName("command_topic")
        protected @Nullable String commandTopic;
        @SerializedName("state_value_template")
        protected @Nullable String stateValueTemplate;

        @SerializedName("brightness_state_topic")
        protected @Nullable String brightnessStateTopic;
        @SerializedName("brightness_command_topic")
        protected @Nullable String brightnessCommandTopic;
        @SerializedName("brightness_value_template")
        protected @Nullable String brightnessValueTemplate;

        @SerializedName("color_temp_state_topic")
        protected @Nullable String colorTempStateTopic;
        @SerializedName("color_temp_command_topic")
        protected @Nullable String colorTempCommandTopic;
        @SerializedName("color_temp_value_template")
        protected @Nullable String colorTempValueTemplate;

        @SerializedName("effect_command_topic")
        protected @Nullable String effectCommandTopic;
        @SerializedName("effect_state_topic")
        protected @Nullable String effectStateTopic;
        @SerializedName("effect_value_template")
        protected @Nullable String effectValueTemplate;

        @SerializedName("rgb_command_topic")
        protected @Nullable String rgbCommandTopic;
        @SerializedName("rgb_state_topic")
        protected @Nullable String rgbStateTopic;
        @SerializedName("rgb_value_template")
        protected @Nullable String rgbValueTemplate;
        @SerializedName("rgb_command_template")
        protected @Nullable String rgbCommandTemplate;

        @SerializedName("white_value_command_topic")
        protected @Nullable String whiteValueCommandTopic;
        @SerializedName("white_value_state_topic")
        protected @Nullable String whiteValueStateTopic;
        @SerializedName("white_value_template")
        protected @Nullable String whiteValueTemplate;

        @SerializedName("xy_command_topic")
        protected @Nullable String xyCommandTopic;
        @SerializedName("xy_state_topic")
        protected @Nullable String xyStateTopic;
        @SerializedName("xy_value_template")
        protected @Nullable String xyValueTemplate;

        @SerializedName("payload_on")
        protected String payloadOn = "ON";
        @SerializedName("payload_off")
        protected String payloadOff = "OFF";
    }

    protected ComponentChannel colorChannel;
    protected ComponentChannel switchChannel;
    protected ComponentChannel brightnessChannel;
    private final @Nullable ChannelStateUpdateListener channelStateUpdateListener;

    public Light(ComponentFactory.ComponentConfiguration builder) {
        super(builder, ChannelConfiguration.class);
        this.channelStateUpdateListener = builder.getUpdateListener();
        ColorValue value = new ColorValue(ColorMode.RGB, channelConfiguration.payloadOn,
                channelConfiguration.payloadOff, 100);

        // Create three MQTT subscriptions and use this class object as update listener
        switchChannel = buildChannel(SWITCH_CHANNEL_ID, value, channelConfiguration.getName(), this)
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.stateValueTemplate,
                        channelConfiguration.getValueTemplate())
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .build(false);

        colorChannel = buildChannel(COLOR_CHANNEL_ID, value, channelConfiguration.getName(), this)
                .stateTopic(channelConfiguration.rgbStateTopic, channelConfiguration.rgbValueTemplate)
                .commandTopic(channelConfiguration.rgbCommandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .build(false);

        brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID, value, channelConfiguration.getName(), this)
                .stateTopic(channelConfiguration.brightnessStateTopic, channelConfiguration.brightnessValueTemplate)
                .commandTopic(channelConfiguration.brightnessCommandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .build(false);

        channels.put(COLOR_CHANNEL_ID, colorChannel);
    }

    @Override
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return Stream.of(switchChannel, brightnessChannel, colorChannel) //
                .map(v -> v.start(connection, scheduler, timeout)) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    @Override
    public CompletableFuture<@Nullable Void> stop() {
        return Stream.of(switchChannel, brightnessChannel, colorChannel) //
                .map(v -> v.stop()) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    /**
     * Proxy method to condense all three MQTT subscriptions to one channel
     */
    @Override
    public void updateChannelState(ChannelUID channelUID, State value) {
        ChannelStateUpdateListener listener = channelStateUpdateListener;
        if (listener != null) {
            listener.updateChannelState(colorChannel.getChannelUID(), value);
        }
    }

    /**
     * Proxy method to condense all three MQTT subscriptions to one channel
     */
    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        ChannelStateUpdateListener listener = channelStateUpdateListener;
        if (listener != null) {
            listener.postChannelCommand(colorChannel.getChannelUID(), value);
        }
    }

    /**
     * Proxy method to condense all three MQTT subscriptions to one channel
     */
    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        ChannelStateUpdateListener listener = channelStateUpdateListener;
        if (listener != null) {
            listener.triggerChannel(colorChannel.getChannelUID(), eventPayload);
        }
    }
}
