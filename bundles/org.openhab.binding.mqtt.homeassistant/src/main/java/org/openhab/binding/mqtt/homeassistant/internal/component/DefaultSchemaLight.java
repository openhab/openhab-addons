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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.mapping.ColorMode;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
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

    public DefaultSchemaLight(ComponentFactory.ComponentConfiguration builder) {
        super(builder);
    }

    @Override
    protected void buildChannels() {
        ComponentChannel localOnOffChannel;
        localOnOffChannel = onOffChannel = buildChannel(ON_OFF_CHANNEL_ID, onOffValue, "On/Off State", this)
                .stateTopic(channelConfiguration.stateTopic, channelConfiguration.stateValueTemplate)
                .commandTopic(channelConfiguration.commandTopic, channelConfiguration.isRetain(),
                        channelConfiguration.getQos())
                .commandFilter(this::handleRawOnOffCommand).build(false);

        @Nullable
        ComponentChannel localBrightnessChannel = null;
        if (channelConfiguration.brightnessStateTopic != null || channelConfiguration.brightnessCommandTopic != null) {
            localBrightnessChannel = brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID, brightnessValue,
                    "Brightness", this)
                            .stateTopic(channelConfiguration.brightnessStateTopic,
                                    channelConfiguration.brightnessValueTemplate)
                            .commandTopic(channelConfiguration.brightnessCommandTopic, channelConfiguration.isRetain(),
                                    channelConfiguration.getQos())
                            .withFormat("%.0f").commandFilter(this::handleBrightnessCommand).build(false);
        }

        if (channelConfiguration.whiteCommandTopic != null) {
            buildChannel(WHITE_CHANNEL_ID, brightnessValue, "Go directly to white of a specific brightness", this)
                    .commandTopic(channelConfiguration.whiteCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .isAdvanced(true).build();
        }

        if (channelConfiguration.colorModeStateTopic != null) {
            buildChannel(COLOR_MODE_CHANNEL_ID, new TextValue(), "Current color mode", this)
                    .stateTopic(channelConfiguration.colorModeStateTopic, channelConfiguration.colorModeValueTemplate)
                    .build();
        }

        if (channelConfiguration.colorTempStateTopic != null || channelConfiguration.colorTempCommandTopic != null) {
            buildChannel(COLOR_TEMP_CHANNEL_ID, colorTempValue, "Color Temperature", this)
                    .stateTopic(channelConfiguration.colorTempStateTopic, channelConfiguration.colorTempValueTemplate)
                    .commandTopic(channelConfiguration.colorTempCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build();
        }

        if (channelConfiguration.effectStateTopic != null || channelConfiguration.effectCommandTopic != null) {
            buildChannel(EFFECT_CHANNEL_ID, effectValue, "Lighting effect", this)
                    .stateTopic(channelConfiguration.effectStateTopic, channelConfiguration.effectValueTemplate)
                    .commandTopic(channelConfiguration.effectCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build();
        }

        if (channelConfiguration.rgbStateTopic != null || channelConfiguration.rgbCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(rgbChannel = buildChannel(RGB_CHANNEL_ID, new ColorValue(ColorMode.RGB, null, null, 100),
                    "RGB state", this)
                            .stateTopic(channelConfiguration.rgbStateTopic, channelConfiguration.rgbValueTemplate)
                            .commandTopic(channelConfiguration.rgbCommandTopic, channelConfiguration.isRetain(),
                                    channelConfiguration.getQos())
                            .build(false));
        }

        if (channelConfiguration.rgbwStateTopic != null || channelConfiguration.rgbwCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(buildChannel(RGBW_CHANNEL_ID, new TextValue(), "RGBW state", this)
                    .stateTopic(channelConfiguration.rgbwStateTopic, channelConfiguration.rgbwValueTemplate)
                    .commandTopic(channelConfiguration.rgbwCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build(false));
        }

        if (channelConfiguration.rgbwwStateTopic != null || channelConfiguration.rgbwwCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(buildChannel(RGBWW_CHANNEL_ID, new TextValue(), "RGBWW state", this)
                    .stateTopic(channelConfiguration.rgbwwStateTopic, channelConfiguration.rgbwwValueTemplate)
                    .commandTopic(channelConfiguration.rgbwwCommandTopic, channelConfiguration.isRetain(),
                            channelConfiguration.getQos())
                    .build(false));
        }

        if (channelConfiguration.xyStateTopic != null || channelConfiguration.xyCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(
                    xyChannel = buildChannel(XY_CHANNEL_ID, new ColorValue(ColorMode.XYY, null, null, 100), "XY State",
                            this).stateTopic(channelConfiguration.xyStateTopic, channelConfiguration.xyValueTemplate)
                                    .commandTopic(channelConfiguration.xyCommandTopic, channelConfiguration.isRetain(),
                                            channelConfiguration.getQos())
                                    .build(false));
        }

        if (channelConfiguration.hsStateTopic != null || channelConfiguration.hsCommandTopic != null) {
            hasColorChannel = true;
            hiddenChannels.add(this.hsChannel = buildChannel(HS_CHANNEL_ID, new TextValue(), "Hue and Saturation", this)
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
            buildChannel(COLOR_CHANNEL_ID, colorValue, "Color", this)
                    .commandTopic(DUMMY_TOPIC, channelConfiguration.isRetain(), channelConfiguration.getQos())
                    .commandFilter(this::handleColorCommand).build();
        } else if (localBrightnessChannel != null) {
            hiddenChannels.add(localOnOffChannel);
            channels.put(BRIGHTNESS_CHANNEL_ID, localBrightnessChannel);
        } else {
            channels.put(ON_OFF_CHANNEL_ID, localOnOffChannel);
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
        } else if (command instanceof HSBType) {
            HSBType color = (HSBType) command;
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
        } else if (command instanceof PercentType) {
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
                HSBType newCommand = new HSBType(existingColor.getHue(), existingColor.getSaturation(),
                        (PercentType) command);
                // re-process
                handleColorCommand(newCommand);
            }
        }
        return false;
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        ChannelStateUpdateListener listener = this.channelStateUpdateListener;
        switch (channel.getIdWithoutGroup()) {
            case ON_OFF_CHANNEL_ID:
                if (hasColorChannel) {
                    HSBType newOnState = colorValue.getChannelState() instanceof HSBType
                            ? (HSBType) colorValue.getChannelState()
                            : HSBType.WHITE;
                    if (state.equals(OnOffType.ON)) {
                        colorValue.update(newOnState);
                    }

                    listener.updateChannelState(new ChannelUID(getGroupUID(), COLOR_CHANNEL_ID),
                            state.equals(OnOffType.ON) ? newOnState : HSBType.BLACK);
                } else if (brightnessChannel != null) {
                    listener.updateChannelState(new ChannelUID(channel.getThingUID(), BRIGHTNESS_CHANNEL_ID),
                            state.equals(OnOffType.ON) ? brightnessValue.getChannelState() : PercentType.ZERO);
                } else {
                    listener.updateChannelState(channel, state);
                }
                return;
            case BRIGHTNESS_CHANNEL_ID:
                onOffValue.update(Objects.requireNonNull(state.as(OnOffType.class)));
                if (hasColorChannel) {
                    if (colorValue.getChannelState() instanceof HSBType) {
                        HSBType hsb = (HSBType) (colorValue.getChannelState());
                        colorValue.update(new HSBType(hsb.getHue(), hsb.getSaturation(),
                                (PercentType) brightnessValue.getChannelState()));
                    } else {
                        colorValue.update(new HSBType(DecimalType.ZERO, PercentType.ZERO,
                                (PercentType) brightnessValue.getChannelState()));
                    }
                    listener.updateChannelState(new ChannelUID(getGroupUID(), COLOR_CHANNEL_ID),
                            colorValue.getChannelState());
                } else {
                    listener.updateChannelState(channel, state);
                }
                return;
            case COLOR_TEMP_CHANNEL_ID:
            case EFFECT_CHANNEL_ID:
                // Real channels; pass through
                listener.updateChannelState(channel, state);
                return;
            case HS_CHANNEL_ID:
            case XY_CHANNEL_ID:
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
                listener.updateChannelState(new ChannelUID(getGroupUID(), COLOR_CHANNEL_ID),
                        colorValue.getChannelState());
                return;
            case RGB_CHANNEL_ID:
                colorValue.update((HSBType) state);
                listener.updateChannelState(new ChannelUID(getGroupUID(), COLOR_CHANNEL_ID),
                        colorValue.getChannelState());
                break;
            case RGBW_CHANNEL_ID:
            case RGBWW_CHANNEL_ID:
                // TODO: update color value
                break;
        }
    }
}
