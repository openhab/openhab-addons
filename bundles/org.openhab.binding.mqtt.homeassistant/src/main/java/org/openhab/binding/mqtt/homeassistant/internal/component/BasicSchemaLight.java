/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.Value;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;

/**
 * A MQTT light, following the https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * Specifically, the basic schema. This class will present a single channel for color, brightness,
 * or on/off as appropriate. Additional attributes are still exposed as dedicated channels.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class BasicSchemaLight extends Light<BasicSchemaLight.Configuration> {
    private static final String HS_CHANNEL_ID = "hs";
    private static final String RGB_CHANNEL_ID = "rgb";
    private static final String RGBW_CHANNEL_ID = "rgbw";
    private static final String RGBWW_CHANNEL_ID = "rgbww";
    private static final String XY_CHANNEL_ID = "xy";
    private static final String WHITE_CHANNEL_ID = "white";

    private static final String ON_COMMAND_TYPE_FIRST = "first";
    private static final String ON_COMMAND_TYPE_BRIGHTNESS = "brightness";
    private static final String ON_COMMAND_TYPE_LAST = "last";

    private @Nullable OnOffValue onOffValue;
    private @Nullable ComponentChannel hsChannel;
    private @Nullable ComponentChannel rgbChannel;
    private @Nullable ComponentChannel xyChannel;

    public static class Configuration extends Light.LightConfiguration {
        private final @Nullable String brightnessCommandTopic, rgbCommandTopic, rgbwCommandTopic, rgbwwCommandTopic,
                xyCommandTopic, hsCommandTopic;
        private final String onCommandType;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT LightEntity");
            brightnessCommandTopic = getOptionalString("brightness_command_topic");
            rgbCommandTopic = getOptionalString("rgb_command_topic");
            rgbwCommandTopic = getOptionalString("rgbw_command_topic");
            rgbwwCommandTopic = getOptionalString("rgbww_command_topic");
            xyCommandTopic = getOptionalString("xy_command_topic");
            hsCommandTopic = getOptionalString("hs_command_topic");
            onCommandType = getString("on_command_type");
        }

        @Nullable
        Value getBrightnessCommandTemplate() {
            return getOptionalValue("brightness_command_template");
        }

        @Nullable
        String getBrightnessCommandTopic() {
            return brightnessCommandTopic;
        }

        int getBrightnessScale() {
            return getInt("brightness_scale");
        }

        @Nullable
        String getBrightnessStateTopic() {
            return getOptionalString("brightness_state_topic");
        }

        @Nullable
        Value getBrightnessValueTemplate() {
            return getOptionalValue("brightness_value_template");
        }

        @Nullable
        String getColorModeStateTopic() {
            return getOptionalString("color_mode_state_topic");
        }

        @Nullable
        Value getColorModeValueTemplate() {
            return getOptionalValue("color_mode_value_template");
        }

        @Nullable
        Value getColorTempCommandTemplate() {
            return getOptionalValue("color_temp_command_template");
        }

        @Nullable
        String getColorTempCommandTopic() {
            return getOptionalString("color_temp_command_topic");
        }

        @Nullable
        String getColorTempStateTopic() {
            return getOptionalString("color_temp_state_topic");
        }

        @Nullable
        Value getColorTempValueTemplate() {
            return getOptionalValue("color_temp_value_template");
        }

        @Nullable
        Value getEffectCommandTemplate() {
            return getOptionalValue("effect_command_template");
        }

        @Nullable
        String getEffectCommandTopic() {
            return getOptionalString("effect_command_topic");
        }

        @Nullable
        String getEffectStateTopic() {
            return getOptionalString("effect_state_topic");
        }

        @Nullable
        Value getEffectValueTemplate() {
            return getOptionalValue("effect_value_template");
        }

        @Nullable
        Value getHsCommandTemplate() {
            return getOptionalValue("hs_command_template");
        }

        @Nullable
        String getHsCommandTopic() {
            return hsCommandTopic;
        }

        @Nullable
        String getHsStateTopic() {
            return getOptionalString("hs_state_topic");
        }

        @Nullable
        Value getHsValueTemplate() {
            return getOptionalValue("hs_value_template");
        }

        String getOnCommandType() {
            return onCommandType;
        }

        String getPayloadOff() {
            return getString("payload_off");
        }

        String getPayloadOn() {
            return getString("payload_on");
        }

        @Nullable
        Value getRgbCommandTemplate() {
            return getOptionalValue("rgb_command_template");
        }

        @Nullable
        String getRgbCommandTopic() {
            return rgbCommandTopic;
        }

        @Nullable
        String getRgbStateTopic() {
            return getOptionalString("rgb_state_topic");
        }

        @Nullable
        Value getRgbValueTemplate() {
            return getOptionalValue("rgb_value_template");
        }

        @Nullable
        Value getRgbwCommandTemplate() {
            return getOptionalValue("rgbw_command_template");
        }

        @Nullable
        String getRgbwCommandTopic() {
            return rgbwCommandTopic;
        }

        @Nullable
        String getRgbwStateTopic() {
            return getOptionalString("rgbw_state_topic");
        }

        @Nullable
        Value getRgbwValueTemplate() {
            return getOptionalValue("rgbw_value_template");
        }

        @Nullable
        Value getRgbwwCommandTemplate() {
            return getOptionalValue("rgbww_command_template");
        }

        @Nullable
        String getRgbwwCommandTopic() {
            return rgbwwCommandTopic;
        }

        @Nullable
        String getRgbwwStateTopic() {
            return getOptionalString("rgbww_state_topic");
        }

        @Nullable
        Value getRgbwwValueTemplate() {
            return getOptionalValue("rgbww_value_template");
        }

        @Nullable
        Value getStateValueTemplate() {
            return getOptionalValue("state_value_template");
        }

        @Nullable
        String getWhiteCommandTopic() {
            return getOptionalString("white_command_topic");
        }

        int getWhiteScale() {
            return getInt("white_scale");
        }

        @Nullable
        Value getXyCommandTemplate() {
            return getOptionalValue("xy_command_template");
        }

        @Nullable
        String getXyCommandTopic() {
            return xyCommandTopic;
        }

        @Nullable
        String getXyStateTopic() {
            return getOptionalString("xy_state_topic");
        }

        @Nullable
        Value getXyValueTemplate() {
            return getOptionalValue("xy_value_template");
        }
    }

    public BasicSchemaLight(ComponentFactory.ComponentContext builder, Map<String, @Nullable Object> config) {
        super(builder, new Configuration(config));
    }

    @Override
    protected void buildChannels() {
        OnOffValue onOffValue = this.onOffValue = new OnOffValue(this.config.getPayloadOn(),
                this.config.getPayloadOff());
        brightnessValue = new PercentageValue(null, new BigDecimal(this.config.getBrightnessScale()), null, null, null,
                FORMAT_INTEGER);

        AutoUpdatePolicy autoUpdatePolicy = optimistic ? AutoUpdatePolicy.RECOMMEND : null;
        ComponentChannel onOffChannel = this.onOffChannel = buildChannel(SWITCH_CHANNEL_ID, ComponentChannelType.SWITCH,
                onOffValue, "On/Off State", this).stateTopic(config.getStateTopic(), config.getStateValueTemplate())
                .commandTopic(config.getCommandTopic(), config.isRetain(), config.getQos())
                .withAutoUpdatePolicy(autoUpdatePolicy).commandFilter(this::handleRawOnOffCommand).build(false);

        ComponentChannel brightnessChannel = null;
        String brightnessStateTopic = config.getBrightnessStateTopic(),
                brightnessCommandTopic = config.getBrightnessCommandTopic();
        if (brightnessStateTopic != null || brightnessCommandTopic != null) {
            brightnessChannel = this.brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID,
                    ComponentChannelType.DIMMER, brightnessValue, "Brightness", this)
                    .stateTopic(brightnessStateTopic, config.getBrightnessValueTemplate())
                    .commandTopic(brightnessCommandTopic, config.isRetain(), config.getQos(),
                            config.getBrightnessCommandTemplate())
                    .withAutoUpdatePolicy(autoUpdatePolicy).withFormat("%.0f")
                    .commandFilter(this::handleBrightnessCommand).build(false);
        }

        String whiteCommandTopic = config.getWhiteCommandTopic();
        if (whiteCommandTopic != null) {
            buildChannel(WHITE_CHANNEL_ID, ComponentChannelType.DIMMER, brightnessValue,
                    "Go directly to white of a specific brightness", this)
                    .commandTopic(whiteCommandTopic, config.isRetain(), config.getQos())
                    .withAutoUpdatePolicy(autoUpdatePolicy).isAdvanced(true).build();
        }

        String colorModeStateTopic = config.getColorModeStateTopic();
        if (colorModeStateTopic != null) {
            buildChannel(COLOR_MODE_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "Current color mode",
                    this).stateTopic(colorModeStateTopic, config.getColorModeValueTemplate())
                    .inferOptimistic(config.isOptimistic()).build();
        }

        String colorTempStateTopic = config.getColorTempStateTopic();
        String colorTempCommandTopic = config.getColorTempCommandTopic();
        if (colorTempStateTopic != null || colorTempCommandTopic != null) {
            buildChannel(COLOR_TEMP_CHANNEL_ID, ComponentChannelType.NUMBER, colorTempValue, "Color Temperature", this)
                    .stateTopic(colorTempStateTopic, config.getColorTempValueTemplate())
                    .commandTopic(colorTempCommandTopic, config.isRetain(), config.getQos(),
                            config.getColorTempCommandTemplate())
                    .inferOptimistic(config.isOptimistic()).build();
        }

        String effectStateTopic = config.getEffectStateTopic();
        String effectCommandTopic = config.getEffectCommandTopic();
        TextValue effectValue = this.effectValue;
        if (effectValue != null && (effectStateTopic != null || effectCommandTopic != null)) {
            buildChannel(EFFECT_CHANNEL_ID, ComponentChannelType.STRING, effectValue, "Lighting Effect", this)
                    .stateTopic(effectStateTopic, config.getEffectValueTemplate()).commandTopic(effectCommandTopic,
                            config.isRetain(), config.getQos(), config.getEffectCommandTemplate())
                    .inferOptimistic(config.isOptimistic()).build();
        }

        boolean hasColorChannel = false;
        String rgbStateTopic = config.getRgbStateTopic();
        String rgbCommandTopic = config.getRgbCommandTopic();
        if (rgbStateTopic != null || rgbCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(rgbChannel = buildChannel(RGB_CHANNEL_ID, ComponentChannelType.COLOR,
                    new ColorValue(ColorMode.RGB, null, null, 100), "RGB state", this)
                    .stateTopic(rgbStateTopic, config.getRgbValueTemplate())
                    .commandTopic(rgbCommandTopic, config.isRetain(), config.getQos(), config.getRgbCommandTemplate())
                    .build(false));
        }

        String rgbwStateTopic = config.getRgbwStateTopic();
        String rgbwCommandTopic = config.getRgbwCommandTopic();
        if (rgbwStateTopic != null || rgbwCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels
                    .add(buildChannel(RGBW_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "RGBW state", this)
                            .stateTopic(rgbwStateTopic, config.getRgbwValueTemplate()).commandTopic(rgbwCommandTopic,
                                    config.isRetain(), config.getQos(), config.getRgbwCommandTemplate())
                            .build(false));
        }

        String rgbwwStateTopic = config.getRgbwwStateTopic();
        String rgbwwCommandTopic = config.getRgbwwCommandTopic();
        if (rgbwwStateTopic != null || rgbwwCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(
                    buildChannel(RGBWW_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "RGBWW state", this)
                            .stateTopic(rgbwwStateTopic, config.getRgbwwValueTemplate()).commandTopic(rgbwwCommandTopic,
                                    config.isRetain(), config.getQos(), config.getRgbwwCommandTemplate())
                            .build(false));
        }

        String xyStateTopic = config.getXyStateTopic();
        String xyCommandTopic = config.getXyCommandTopic();
        if (xyStateTopic != null || xyCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(xyChannel = buildChannel(XY_CHANNEL_ID, ComponentChannelType.COLOR,
                    new ColorValue(ColorMode.XYY, null, null, 100), "XY State", this)
                    .stateTopic(xyStateTopic, config.getXyValueTemplate())
                    .commandTopic(xyCommandTopic, config.isRetain(), config.getQos(), config.getXyCommandTemplate())
                    .build(false));
        }

        String hsStateTopic = config.getHsStateTopic();
        String hsCommandTopic = config.getHsCommandTopic();
        if (hsStateTopic != null || hsCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(this.hsChannel = buildChannel(HS_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(), "Hue and Saturation", this).stateTopic(hsStateTopic, config.getHsValueTemplate())
                    .commandTopic(hsCommandTopic, config.isRetain(), config.getQos(), config.getHsCommandTemplate())
                    .build(false));
        }

        if (hasColorChannel) {
            hiddenChannels.add(onOffChannel);
            if (brightnessChannel != null) {
                hiddenChannels.add(brightnessChannel);
            }
            colorChannel = buildChannel(COLOR_CHANNEL_ID, ComponentChannelType.COLOR, colorValue, "Color", this)
                    .commandTopic(DUMMY_TOPIC, config.isRetain(), config.getQos())
                    .commandFilter(this::handleColorCommand).withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else if (brightnessChannel != null) {
            hiddenChannels.add(onOffChannel);
            channels.put(BRIGHTNESS_CHANNEL_ID, brightnessChannel);
        } else {
            channels.put(SWITCH_CHANNEL_ID, onOffChannel);
        }
    }

    // all handle*Command methods return false if they've been handled,
    // or true if default handling should continue

    // The commandFilter for onOffChannel
    private boolean handleRawOnOffCommand(Command command) {
        // on_command_type of brightness is not allowed to send an actual on command
        ComponentChannel brightnessChannel = this.brightnessChannel;
        if (command.equals(OnOffType.ON) && brightnessChannel != null
                && config.getOnCommandType().equals(ON_COMMAND_TYPE_BRIGHTNESS)) {
            // No prior state (or explicit off); set to 100%
            if (brightnessValue.getChannelState() instanceof UnDefType
                    || brightnessValue.getChannelState().equals(PercentType.ZERO)) {
                brightnessChannel.getState().publishValue(PercentType.HUNDRED);
            } else {
                brightnessChannel.getState().publishValue((Command) brightnessValue.getChannelState());
            }
            return false;
        }

        return true;
    }

    // The helper method the other commandFilters call
    private boolean handleOnOffCommand(Command command) {
        if (!handleRawOnOffCommand(command)) {
            return false;
        }

        // OnOffType commands to go the regular command topic
        if (command instanceof OnOffType) {
            Objects.requireNonNull(onOffChannel).getState().publishValue(command);
            return false;
        }

        boolean needsOn = !Objects.requireNonNull(onOffValue).getChannelState().equals(OnOffType.ON);
        if (command.equals(PercentType.ZERO) || command.equals(HSBType.BLACK)) {
            needsOn = false;
        }
        if (needsOn) {
            if (config.getOnCommandType().equals(ON_COMMAND_TYPE_FIRST)) {
                Objects.requireNonNull(onOffChannel).getState().publishValue(OnOffType.ON);
            } else if (config.getOnCommandType().equals(ON_COMMAND_TYPE_LAST)) {
                // TODO: schedule the ON publish for after this is sent
            }
        }
        return true;
    }

    private boolean handleBrightnessCommand(Command command) {
        // if it's OnOffType, it'll get handled by this; otherwise it'll return
        // true and PercentType will be handled as normal
        return handleOnOffCommand(command);
    }

    private boolean handleColorCommand(Command command) {
        if (!handleOnOffCommand(command)) {
            return false;
        } else if (command instanceof HSBType color) {
            if (config.getHsCommandTopic() != null) {
                // If we don't have a brightness channel, something is probably busted
                // but don't choke
                if (config.getBrightnessCommandTopic() != null) {
                    Objects.requireNonNull(brightnessChannel).getState().publishValue(color.getBrightness());
                }
                String hs = String.format("%d,%d", color.getHue().intValue(), color.getSaturation().intValue());
                Objects.requireNonNull(hsChannel).getState().publishValue(new StringType(hs));
            } else if (config.getRgbCommandTopic() != null) {
                Objects.requireNonNull(rgbChannel).getState().publishValue(command);
                // } else if (config.getRgbwCommandTopic() != null) {
                // TODO
                // } else if (config.getRgbwwCommandTopic() != null) {
                // TODO
            } else if (config.getXyCommandTopic() != null) {
                PercentType[] xy = color.toXY();
                // If we don't have a brightness channel, something is probably busted
                // but don't choke
                if (config.getBrightnessCommandTopic() != null) {
                    Objects.requireNonNull(brightnessChannel).getState().publishValue(color.getBrightness());
                }
                String xyString = String.format("%f,%f", xy[0].doubleValue(), xy[1].doubleValue());
                Objects.requireNonNull(xyChannel).getState().publishValue(new StringType(xyString));
            }
        } else if (command instanceof PercentType brightness) {
            if (config.getBrightnessCommandTopic() != null) {
                Objects.requireNonNull(brightnessChannel).getState().publishValue(command);
            } else {
                // No brightness command topic?! must be RGB only
                // so re-calculatate
                State color = colorValue.getChannelState();
                if (color instanceof UnDefType) {
                    color = HSBType.WHITE;
                }
                HSBType existingColor = (HSBType) color;
                HSBType newCommand = new HSBType(existingColor.getHue(), existingColor.getSaturation(), brightness);
                // re-process
                handleColorCommand(newCommand);
            }
        }
        return false;
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        ChannelStateUpdateListener listener = this.channelStateUpdateListener;
        String id = channel.getIdWithoutGroup();
        ComponentChannel brightnessChannel = this.brightnessChannel;
        ComponentChannel colorChannel = this.colorChannel;
        ComponentChannel onOffChannel = Objects.requireNonNull(this.onOffChannel);
        ChannelUID primaryChannelUID;
        if (colorChannel != null) {
            primaryChannelUID = colorChannel.getChannel().getUID();
        } else if (brightnessChannel != null) {
            primaryChannelUID = brightnessChannel.getChannel().getUID();
        } else {
            primaryChannelUID = onOffChannel.getChannel().getUID();
        }
        // on_off, brightness, and color might exist as a sole channel, which means
        // they got renamed. they need to be compared against the actual UID of the
        // channel. all the rest we can just check against the basic ID
        if (channel.equals(onOffChannel.getChannel().getUID())) {
            if (colorChannel != null) {
                HSBType newOnState = colorValue.getChannelState() instanceof HSBType newOnStateTmp ? newOnStateTmp
                        : HSBType.WHITE;
                if (state.equals(OnOffType.ON)) {
                    colorValue.update(newOnState);
                }

                listener.updateChannelState(primaryChannelUID, state.equals(OnOffType.ON) ? newOnState : HSBType.BLACK);
            } else if (brightnessChannel != null) {
                listener.updateChannelState(primaryChannelUID,
                        state.equals(OnOffType.ON) ? brightnessValue.getChannelState() : PercentType.ZERO);
            } else {
                listener.updateChannelState(primaryChannelUID, state);
            }
        } else if (brightnessChannel != null && brightnessChannel.getChannel().getUID().equals(channel)) {
            Objects.requireNonNull(onOffValue).update(Objects.requireNonNull(state.as(OnOffType.class)));
            if (colorChannel != null) {
                if (colorValue.getChannelState() instanceof HSBType hsb) {
                    colorValue.update(new HSBType(hsb.getHue(), hsb.getSaturation(),
                            (PercentType) brightnessValue.getChannelState()));
                } else {
                    colorValue.update(new HSBType(DecimalType.ZERO, PercentType.ZERO,
                            (PercentType) brightnessValue.getChannelState()));
                }
                listener.updateChannelState(primaryChannelUID, colorValue.getChannelState());
            } else {
                listener.updateChannelState(primaryChannelUID, state);
            }
        } else if (id.equals(COLOR_TEMP_CHANNEL_ID) || channel.getIdWithoutGroup().equals(EFFECT_CHANNEL_ID)) {
            // Real channels; pass through
            listener.updateChannelState(channel, state);
        } else if (id.equals(HS_CHANNEL_ID) || id.equals(XY_CHANNEL_ID)) {
            if (brightnessValue.getChannelState() instanceof UnDefType) {
                brightnessValue.update(PercentType.HUNDRED);
            }
            String[] split = state.toString().split(",");
            if (split.length != 2) {
                throw new IllegalArgumentException(state.toString() + " is not a valid string syntax");
            }
            float x = Float.parseFloat(split[0]);
            float y = Float.parseFloat(split[1]);
            PercentType brightness = (PercentType) brightnessValue.getChannelState();
            if (channel.getIdWithoutGroup().equals(HS_CHANNEL_ID)) {
                colorValue.update(new HSBType(new DecimalType(x), new PercentType(new BigDecimal(y)), brightness));
            } else {
                HSBType xyColor = ColorUtil.xyToHsb(new double[] { x, y });
                colorValue.update(new HSBType(xyColor.getHue(), xyColor.getSaturation(), brightness));
            }
            listener.updateChannelState(primaryChannelUID, colorValue.getChannelState());
        } else if (id.equals(RGB_CHANNEL_ID)) {
            colorValue.update((HSBType) state);
            listener.updateChannelState(primaryChannelUID, colorValue.getChannelState());
        }
        // else rgbw channel, rgbww channel
    }
}
