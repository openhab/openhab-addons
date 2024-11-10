/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
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

/**
 * A MQTT light, following the https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * Specifically, the default schema. This class will present a single channel for color, brightness,
 * or on/off as appropriate. Additional attributes are still exposed as dedicated channels.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DefaultSchemaLight extends Light {
    protected static final String HS_CHANNEL_ID = "hs";
    protected static final String RGB_CHANNEL_ID = "rgb";
    protected static final String RGBW_CHANNEL_ID = "rgbw";
    protected static final String RGBWW_CHANNEL_ID = "rgbww";
    protected static final String XY_CHANNEL_ID = "xy";
    protected static final String WHITE_CHANNEL_ID = "white";

    protected @Nullable ComponentChannel hsChannel;
    protected @Nullable ComponentChannel rgbChannel;
    protected @Nullable ComponentChannel xyChannel;

    public DefaultSchemaLight(ComponentFactory.ComponentConfiguration builder, boolean newStyleChannels) {
        super(builder, newStyleChannels);
    }

    @Override
    protected void buildChannels() {
        AutoUpdatePolicy autoUpdatePolicy = optimistic ? AutoUpdatePolicy.RECOMMEND : null;
        ComponentChannel localOnOffChannel;
        localOnOffChannel = onOffChannel = buildChannel(
                newStyleChannels ? SWITCH_CHANNEL_ID : SWITCH_CHANNEL_ID_DEPRECATED, ComponentChannelType.SWITCH,
                onOffValue, "On/Off State", this)
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.stateValueTemplate)
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .withAutoUpdatePolicy(autoUpdatePolicy).commandFilter(this::handleRawOnOffCommand).build(false);

        @Nullable
        ComponentChannel localBrightnessChannel = null;
        if (channelConfiguration.brightnessStateTopic != null || channelConfiguration.brightnessCommandTopic != null) {
            localBrightnessChannel = brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID,
                    ComponentChannelType.DIMMER, brightnessValue, "Brightness", this)
                    .stateTopic(channelConfiguration.brightnessStateTopic, channelConfiguration.brightnessValueTemplate)
                    .commandTopic(channelConfiguration.brightnessCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .withAutoUpdatePolicy(autoUpdatePolicy).withFormat("%.0f")
                    .commandFilter(this::handleBrightnessCommand).build(false);
        }

        if (channelConfiguration.whiteCommandTopic != null) {
            buildChannel(WHITE_CHANNEL_ID, ComponentChannelType.DIMMER, brightnessValue,
                    "Go directly to white of a specific brightness", this)
                    .commandTopic(channelConfiguration.whiteCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .withAutoUpdatePolicy(autoUpdatePolicy).isAdvanced(true).build();
        }

        if (channelConfiguration.colorModeStateTopic != null) {
            buildChannel(newStyleChannels ? COLOR_MODE_CHANNEL_ID : COLOR_MODE_CHANNEL_ID_DEPRECATED,
                    ComponentChannelType.STRING, new TextValue(), "Current color mode", this)
                    .stateTopic(channelConfiguration.colorModeStateTopic, channelConfiguration.colorModeValueTemplate)
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (channelConfiguration.colorTempStateTopic != null || channelConfiguration.colorTempCommandTopic != null) {
            buildChannel(newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED,
                    ComponentChannelType.NUMBER, colorTempValue, "Color Temperature", this)
                    .stateTopic(channelConfiguration.colorTempStateTopic, channelConfiguration.colorTempValueTemplate)
                    .commandTopic(channelConfiguration.colorTempCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        if (effectValue != null
                && (channelConfiguration.effectStateTopic != null || channelConfiguration.effectCommandTopic != null)) {
            buildChannel(EFFECT_CHANNEL_ID, ComponentChannelType.STRING, Objects.requireNonNull(effectValue),
                    "Lighting Effect", this)
                    .stateTopic(channelConfiguration.effectStateTopic, channelConfiguration.effectValueTemplate)
                    .commandTopic(channelConfiguration.effectCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .inferOptimistic(channelConfiguration.optimistic).build();
        }

        boolean hasColorChannel = false;
        if (channelConfiguration.rgbStateTopic != null || channelConfiguration.rgbCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(rgbChannel = buildChannel(RGB_CHANNEL_ID, ComponentChannelType.COLOR,
                    new ColorValue(ColorMode.RGB, null, null, 100), "RGB state", this)
                    .stateTopic(channelConfiguration.rgbStateTopic, channelConfiguration.rgbValueTemplate)
                    .commandTopic(channelConfiguration.rgbCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build(false));
        }

        if (channelConfiguration.rgbwStateTopic != null || channelConfiguration.rgbwCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels
                    .add(buildChannel(RGBW_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "RGBW state", this)
                            .stateTopic(channelConfiguration.rgbwStateTopic, channelConfiguration.rgbwValueTemplate)
                            .commandTopic(channelConfiguration.rgbwCommandTopic, channelConfiguration.isRetain(),
                                    channelConfiguration.getQos())
                            .build(false));
        }

        if (channelConfiguration.rgbwwStateTopic != null || channelConfiguration.rgbwwCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(
                    buildChannel(RGBWW_CHANNEL_ID, ComponentChannelType.STRING, new TextValue(), "RGBWW state", this)
                            .stateTopic(channelConfiguration.rgbwwStateTopic, channelConfiguration.rgbwwValueTemplate)
                            .commandTopic(channelConfiguration.rgbwwCommandTopic, channelConfiguration.isRetain(),
                                    channelConfiguration.getQos())
                            .build(false));
        }

        if (channelConfiguration.xyStateTopic != null || channelConfiguration.xyCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(xyChannel = buildChannel(XY_CHANNEL_ID, ComponentChannelType.COLOR,
                    new ColorValue(ColorMode.XYY, null, null, 100), "XY State", this)
                    .stateTopic(channelConfiguration.xyStateTopic, channelConfiguration.xyValueTemplate)
                    .commandTopic(channelConfiguration.xyCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build(false));
        }

        if (channelConfiguration.hsStateTopic != null || channelConfiguration.hsCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(this.hsChannel = buildChannel(HS_CHANNEL_ID, ComponentChannelType.STRING,
                    new TextValue(), "Hue and Saturation", this)
                    .stateTopic(channelConfiguration.hsStateTopic, channelConfiguration.hsValueTemplate)
                    .commandTopic(channelConfiguration.hsCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build(false));
        }

        if (hasColorChannel) {
            hiddenChannels.add(localOnOffChannel);
            if (localBrightnessChannel != null) {
                hiddenChannels.add(localBrightnessChannel);
            }
            colorChannel = buildChannel(COLOR_CHANNEL_ID, ComponentChannelType.COLOR, colorValue, "Color", this)
                    .commandTopic(DUMMY_TOPIC, channelConfiguration.isRetain(), channelConfiguration.getQos())
                    .commandFilter(this::handleColorCommand).withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else if (localBrightnessChannel != null) {
            hiddenChannels.add(localOnOffChannel);
            channels.put(BRIGHTNESS_CHANNEL_ID, localBrightnessChannel);
        } else {
            channels.put(newStyleChannels ? SWITCH_CHANNEL_ID : SWITCH_CHANNEL_ID_DEPRECATED, localOnOffChannel);
        }
    }

    // all handle*Command methods return false if they've been handled,
    // or true if default handling should continue

    // The commandFilter for onOffChannel
    private boolean handleRawOnOffCommand(Command command) {
        // on_command_type of brightness is not allowed to send an actual on command
        if (command.equals(OnOffType.ON) && channelConfiguration.onCommandType.equals(ON_COMMAND_TYPE_BRIGHTNESS)) {
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
            onOffChannel.getState().publishValue(command);
            return false;
        }

        boolean needsOn = !onOffValue.getChannelState().equals(OnOffType.ON);
        if (command.equals(PercentType.ZERO) || command.equals(HSBType.BLACK)) {
            needsOn = false;
        }
        if (needsOn) {
            if (channelConfiguration.onCommandType.equals(ON_COMMAND_TYPE_FIRST)) {
                onOffChannel.getState().publishValue(OnOffType.ON);
            } else if (channelConfiguration.onCommandType.equals(ON_COMMAND_TYPE_LAST)) {
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
            if (channelConfiguration.hsCommandTopic != null) {
                // If we don't have a brightness channel, something is probably busted
                // but don't choke
                if (channelConfiguration.brightnessCommandTopic != null) {
                    brightnessChannel.getState().publishValue(color.getBrightness());
                }
                String hs = String.format("%d,%d", color.getHue().intValue(), color.getSaturation().intValue());
                hsChannel.getState().publishValue(new StringType(hs));
            } else if (channelConfiguration.rgbCommandTopic != null) {
                rgbChannel.getState().publishValue(command);
                // } else if (channelConfiguration.rgbwCommandTopic != null) {
                // TODO
                // } else if (channelConfiguration.rgbwwCommandTopic != null) {
                // TODO
            } else if (channelConfiguration.xyCommandTopic != null) {
                PercentType[] xy = color.toXY();
                // If we don't have a brightness channel, something is probably busted
                // but don't choke
                if (channelConfiguration.brightnessCommandTopic != null) {
                    brightnessChannel.getState().publishValue(color.getBrightness());
                }
                String xyString = String.format("%f,%f", xy[0].doubleValue(), xy[1].doubleValue());
                xyChannel.getState().publishValue(new StringType(xyString));
            }
        } else if (command instanceof PercentType brightness) {
            if (channelConfiguration.brightnessCommandTopic != null) {
                brightnessChannel.getState().publishValue(command);
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
        ComponentChannel localBrightnessChannel = brightnessChannel;
        ComponentChannel localColorChannel = colorChannel;
        ChannelUID primaryChannelUID;
        if (localColorChannel != null) {
            primaryChannelUID = localColorChannel.getChannel().getUID();
        } else if (localBrightnessChannel != null) {
            primaryChannelUID = localBrightnessChannel.getChannel().getUID();
        } else {
            primaryChannelUID = onOffChannel.getChannel().getUID();
        }
        // on_off, brightness, and color might exist as a sole channel, which means
        // they got renamed. they need to be compared against the actual UID of the
        // channel. all the rest we can just check against the basic ID
        if (channel.equals(onOffChannel.getChannel().getUID())) {
            if (localColorChannel != null) {
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
        } else if (localBrightnessChannel != null && localBrightnessChannel.getChannel().getUID().equals(channel)) {
            onOffValue.update(Objects.requireNonNull(state.as(OnOffType.class)));
            if (localColorChannel != null) {
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
        } else if (id.equals(newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED)
                || channel.getIdWithoutGroup().equals(EFFECT_CHANNEL_ID)) {
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
                HSBType xyColor = HSBType.fromXY(x, y);
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
