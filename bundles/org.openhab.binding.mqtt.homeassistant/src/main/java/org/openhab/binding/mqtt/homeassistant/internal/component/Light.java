/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.exception.UnsupportedComponentException;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

import com.google.gson.annotations.SerializedName;

/**
 * A MQTT light, following the
 * https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * Individual concrete classes implement the differing semantics of the
 * three different schemas.
 *
 * As of now, only on/off, brightness, and RGB are fully implemented and tested.
 * HS and XY are implemented, but not tested. Color temp and effect are only
 * implemented (but not tested) for the default schema.
 *
 * @author David Graeff - Initial contribution
 * @author Cody Cutrer - Re-write for (nearly) full support
 */
@NonNullByDefault
public abstract class Light extends AbstractComponent<Light.ChannelConfiguration>
        implements ChannelStateUpdateListener {
    protected static final String DEFAULT_SCHEMA = "default";
    protected static final String JSON_SCHEMA = "json";
    protected static final String TEMPLATE_SCHEMA = "template";

    protected static final String STATE_CHANNEL_ID = "state";
    protected static final String ON_OFF_CHANNEL_ID = "on_off";
    protected static final String BRIGHTNESS_CHANNEL_ID = "brightness";
    protected static final String COLOR_MODE_CHANNEL_ID = "color_mode";
    protected static final String COLOR_TEMP_CHANNEL_ID = "color_temp";
    protected static final String EFFECT_CHANNEL_ID = "effect";
    // This channel is a synthetic channel that may send to other channels
    // underneath
    protected static final String COLOR_CHANNEL_ID = "color";

    protected static final String DUMMY_TOPIC = "dummy";

    protected static final String ON_COMMAND_TYPE_FIRST = "first";
    protected static final String ON_COMMAND_TYPE_BRIGHTNESS = "brightness";
    protected static final String ON_COMMAND_TYPE_LAST = "last";

    /**
     * Configuration class for MQTT component
     */
    static class ChannelConfiguration extends AbstractChannelConfiguration {
        ChannelConfiguration() {
            super("MQTT Light");
        }

        /* Attributes that control the basic structure of the light */

        protected String schema = DEFAULT_SCHEMA;
        protected @Nullable Boolean optimistic; // All schemas
        protected boolean brightness = false; // JSON schema only
        @SerializedName("color_mode")
        protected boolean colorMode = false; // JSON schema only
        @SerializedName("supported_color_modes")
        protected @Nullable List<String> supportedColorModes; // JSON schema only
        // Defines when on the payload_on is sent. Using last (the default) will send
        // any style (brightness, color, etc)
        // topics first and then a payload_on to the command_topic. Using first will
        // send the payload_on and then any
        // style topics. Using brightness will only send brightness commands instead of
        // the payload_on to turn the light
        // on.
        @SerializedName("on_command_type")
        protected String onCommandType = ON_COMMAND_TYPE_LAST; // Default schema only

        /* Basic control attributes */

        @SerializedName("state_topic")
        protected @Nullable String stateTopic; // All Schemas
        @SerializedName("state_value_template")
        protected @Nullable String stateValueTemplate; // Default schema only
        @SerializedName("state_template")
        protected @Nullable String stateTemplate; // Template schema only
        @SerializedName("payload_on")
        protected String payloadOn = "ON"; // Default schema only
        @SerializedName("payload_off")
        protected String payloadOff = "OFF"; // Default schema only
        @SerializedName("command_topic")
        protected @Nullable String commandTopic; // All schemas
        @SerializedName("command_on_template")
        protected @Nullable String commandOnTemplate; // Template schema only; required
        @SerializedName("command_off_template")
        protected @Nullable String commandOffTemplate; // Template schema only; required

        /* Brightness attributes */

        @SerializedName("brightness_scale")
        protected int brightnessScale = 255; // Default, JSON schemas only
        @SerializedName("brightness_state_topic")
        protected @Nullable String brightnessStateTopic; // Default schema only
        @SerializedName("brightness_value_template")
        protected @Nullable String brightnessValueTemplate; // Default schema only
        @SerializedName("brightness_template")
        protected @Nullable String brightnessTemplate; // Template schema only
        @SerializedName("brightness_command_topic")
        protected @Nullable String brightnessCommandTopic; // Default schema only
        @SerializedName("brightness_command_template")
        protected @Nullable String brightnessCommandTemplate; // Default schema only

        /* White value attributes */

        @SerializedName("white_scale")
        protected int whiteScale = 255; // Default, JSON schemas only
        @SerializedName("white_command_topic")
        protected @Nullable String whiteCommandTopic; // Default schema only

        /* Color mode attributes */

        @SerializedName("color_mode_state_topic")
        protected @Nullable String colorModeStateTopic; // Default schema only
        @SerializedName("color_mode_value_template")
        protected @Nullable String colorModeValueTemplate; // Default schema only

        /* Color temp attributes */

        @SerializedName("min_mireds")
        protected @Nullable Integer minMireds; // All schemas
        @SerializedName("max_mireds")
        protected @Nullable Integer maxMireds; // All schemas
        @SerializedName("color_temp_state_topic")
        protected @Nullable String colorTempStateTopic; // Default schema only
        @SerializedName("color_temp_value_template")
        protected @Nullable String colorTempValueTemplate; // Default schema only
        @SerializedName("color_temp_template")
        protected @Nullable String colorTempTemplate; // Template schema only
        @SerializedName("color_temp_command_topic")
        protected @Nullable String colorTempCommandTopic; // Default schema only
        @SerializedName("color_temp_command_template")
        protected @Nullable String colorTempCommandTemplate; // Default schema only

        /* Effect attributes */
        @SerializedName("effect_list")
        protected @Nullable List<String> effectList; // All schemas
        @SerializedName("effect_state_topic")
        protected @Nullable String effectStateTopic; // Default schema only
        @SerializedName("effect_value_template")
        protected @Nullable String effectValueTemplate; // Default schema only
        @SerializedName("effect_template")
        protected @Nullable String effectTemplate; // Template schema only
        @SerializedName("effect_command_topic")
        protected @Nullable String effectCommandTopic; // Default schema only
        @SerializedName("effect_command_template")
        protected @Nullable String effectCommandTemplate; // Default schema only

        /* HS attributes */
        @SerializedName("hs_state_topic")
        protected @Nullable String hsStateTopic; // Default schema only
        @SerializedName("hs_value_template")
        protected @Nullable String hsValueTemplate; // Default schema only
        @SerializedName("hs_command_topic")
        protected @Nullable String hsCommandTopic; // Default schema only

        /* RGB attributes */
        @SerializedName("rgb_state_topic")
        protected @Nullable String rgbStateTopic; // Default schema only
        @SerializedName("rgb_value_template")
        protected @Nullable String rgbValueTemplate; // Default schema only
        @SerializedName("red_template")
        protected @Nullable String redTemplate; // Template schema only
        @SerializedName("green_template")
        protected @Nullable String greenTemplate; // Template schema only
        @SerializedName("blue_template")
        protected @Nullable String blueTemplate; // Template schema only
        @SerializedName("rgb_command_topic")
        protected @Nullable String rgbCommandTopic; // Default schema only
        @SerializedName("rgb_command_template")
        protected @Nullable String rgbCommandTemplate; // Default schema only

        /* RGBW attributes */
        @SerializedName("rgbw_state_topic")
        protected @Nullable String rgbwStateTopic; // Default schema only
        @SerializedName("rgbw_value_template")
        protected @Nullable String rgbwValueTemplate; // Default schema only
        @SerializedName("rgbw_command_topic")
        protected @Nullable String rgbwCommandTopic; // Default schema only
        @SerializedName("rgbw_command_template")
        protected @Nullable String rgbwCommandTemplate; // Default schema only

        /* RGBWW attributes */
        @SerializedName("rgbww_state_topic")
        protected @Nullable String rgbwwStateTopic; // Default schema only
        @SerializedName("rgbww_value_template")
        protected @Nullable String rgbwwValueTemplate; // Default schema only
        @SerializedName("rgbww_command_topic")
        protected @Nullable String rgbwwCommandTopic; // Default schema only
        @SerializedName("rgbww_command_template")
        protected @Nullable String rgbwwCommandTemplate; // Default schema only

        /* XY attributes */
        @SerializedName("xy_command_topic")
        protected @Nullable String xyCommandTopic; // Default schema only
        @SerializedName("xy_state_topic")
        protected @Nullable String xyStateTopic; // Default schema only
        @SerializedName("xy_value_template")
        protected @Nullable String xyValueTemplate; // Default schema only
    }

    protected final boolean optimistic;
    protected boolean hasColorChannel = false;

    protected @Nullable ComponentChannel onOffChannel;
    protected @Nullable ComponentChannel brightnessChannel;

    // State has to be stored here, in order to mux multiple
    // MQTT sources into single OpenHAB channels
    protected OnOffValue onOffValue;
    protected PercentageValue brightnessValue;
    protected final NumberValue colorTempValue;
    protected final TextValue effectValue = new TextValue();
    protected final ColorValue colorValue = new ColorValue(ColorMode.HSB, null, null, 100);

    protected final List<ComponentChannel> hiddenChannels = new ArrayList<>();
    protected final ChannelStateUpdateListener channelStateUpdateListener;

    public static Light create(ComponentFactory.ComponentConfiguration builder) throws UnsupportedComponentException {
        String schema = builder.getConfig(ChannelConfiguration.class).schema;
        switch (schema) {
            case DEFAULT_SCHEMA:
                return new DefaultSchemaLight(builder);
            default:
                throw new UnsupportedComponentException(
                        "Component '" + builder.getHaID() + "' of schema '" + schema + "' is not supported!");
        }
    }

    protected Light(ComponentFactory.ComponentConfiguration builder) {
        super(builder, ChannelConfiguration.class);
        this.channelStateUpdateListener = builder.getUpdateListener();

        @Nullable
        Boolean optimistic = channelConfiguration.optimistic;
        if (optimistic != null) {
            this.optimistic = optimistic;
        } else {
            this.optimistic = (channelConfiguration.stateTopic == null);
        }

        onOffValue = new OnOffValue(channelConfiguration.payloadOn, channelConfiguration.payloadOff);
        brightnessValue = new PercentageValue(null, new BigDecimal(channelConfiguration.brightnessScale), null, null,
                null);
        @Nullable
        BigDecimal min = null, max = null;
        if (channelConfiguration.minMireds != null) {
            min = new BigDecimal(channelConfiguration.minMireds);
        }
        if (channelConfiguration.maxMireds != null) {
            max = new BigDecimal(channelConfiguration.maxMireds);
        }
        colorTempValue = new NumberValue(min, max, BigDecimal.ONE, Units.MIRED);

        buildChannels();
    }

    protected abstract void buildChannels();

    @Override
    public CompletableFuture<@Nullable Void> start(MqttBrokerConnection connection, ScheduledExecutorService scheduler,
            int timeout) {
        return Stream.concat(channels.values().stream(), hiddenChannels.stream()) //
                .map(v -> v.start(connection, scheduler, timeout)) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    @Override
    public CompletableFuture<@Nullable Void> stop() {
        return Stream.concat(channels.values().stream(), hiddenChannels.stream()) //
                .filter(Objects::nonNull) //
                .map(ComponentChannel::stop) //
                .reduce(CompletableFuture.completedFuture(null), (f, v) -> f.thenCompose(b -> v));
    }

    @Override
    public void postChannelCommand(ChannelUID channelUID, Command value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void triggerChannel(ChannelUID channelUID, String eventPayload) {
        throw new UnsupportedOperationException();
    }
}
