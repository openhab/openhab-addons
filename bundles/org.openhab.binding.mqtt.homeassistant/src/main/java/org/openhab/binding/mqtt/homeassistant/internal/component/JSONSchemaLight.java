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
import java.math.MathContext;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannelType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * A MQTT light, following the https://www.home-assistant.io/components/light.mqtt/ specification.
 *
 * Specifically, the JSON schema. All channels are synthetic, and wrap the single internal raw
 * state.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class JSONSchemaLight extends AbstractRawSchemaLight {
    private static final BigDecimal SCALE_FACTOR = new BigDecimal("2.55"); // string to not lose precision
    private static final BigDecimal BIG_DECIMAL_HUNDRED = new BigDecimal(100);

    private final Logger logger = LoggerFactory.getLogger(JSONSchemaLight.class);

    private @Nullable ComponentChannel colorTempChannel;

    private static class JSONState {
        protected static class Color {
            protected @Nullable Integer r, g, b, c, w;
            protected @Nullable BigDecimal x, y, h, s;
        }

        protected @Nullable String state;
        protected @Nullable Integer brightness;
        @SerializedName("color_mode")
        protected @Nullable LightColorMode colorMode;
        @SerializedName("color_temp")
        protected @Nullable Integer colorTemp;
        protected @Nullable Color color;
        protected @Nullable String effect;
        protected @Nullable Integer transition;
    }

    public JSONSchemaLight(ComponentFactory.ComponentConfiguration builder, boolean newStyleChannels) {
        super(builder, newStyleChannels);
    }

    @Override
    protected void buildChannels() {
        boolean hasColorChannel = false;
        AutoUpdatePolicy autoUpdatePolicy = optimistic ? AutoUpdatePolicy.RECOMMEND : null;
        List<LightColorMode> supportedColorModes = channelConfiguration.supportedColorModes;
        if (supportedColorModes != null) {
            if (LightColorMode.hasColorChannel(supportedColorModes)) {
                hasColorChannel = true;
            }

            if (supportedColorModes.contains(LightColorMode.COLOR_MODE_COLOR_TEMP)) {
                colorTempChannel = buildChannel(
                        newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED,
                        ComponentChannelType.NUMBER, colorTempValue, "Color Temperature", this)
                        .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(command -> handleColorTempCommand(command))
                        .withAutoUpdatePolicy(autoUpdatePolicy).build();

                if (hasColorChannel) {
                    colorModeValue = new TextValue(
                            supportedColorModes.stream().map(LightColorMode::serializedName).toArray(String[]::new));
                    buildChannel(newStyleChannels ? COLOR_MODE_CHANNEL_ID : COLOR_MODE_CHANNEL_ID_DEPRECATED,
                            ComponentChannelType.STRING, colorModeValue, "Color Mode", this)
                            .withAutoUpdatePolicy(autoUpdatePolicy).isAdvanced(true).build();

                }
            }
        }

        if (hasColorChannel) {
            colorChannel = buildChannel(COLOR_CHANNEL_ID, ComponentChannelType.COLOR, colorValue, "Color", this)
                    .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else if (channelConfiguration.brightness) {
            brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID, ComponentChannelType.DIMMER, brightnessValue,
                    "Brightness", this).commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else {
            onOffChannel = buildChannel(newStyleChannels ? SWITCH_CHANNEL_ID : SWITCH_CHANNEL_ID_DEPRECATED,
                    ComponentChannelType.SWITCH, onOffValue, "On/Off State", this).commandTopic(DUMMY_TOPIC, true, 1)
                    .commandFilter(this::handleCommand).withAutoUpdatePolicy(autoUpdatePolicy).build();
        }

        if (effectValue != null) {
            buildChannel(EFFECT_CHANNEL_ID, ComponentChannelType.STRING, Objects.requireNonNull(effectValue),
                    "Lighting Effect", this).commandTopic(DUMMY_TOPIC, true, 1)
                    .commandFilter(command -> handleEffectCommand(command)).withAutoUpdatePolicy(autoUpdatePolicy)
                    .build();

        }
    }

    private boolean handleEffectCommand(Command command) {
        if (command instanceof StringType) {
            JSONState json = new JSONState();
            json.state = "ON";
            json.effect = command.toString();
            publishState(json);
        }
        return false;
    }

    @Override
    protected void publishState(HSBType state) {
        JSONState json = new JSONState();

        logger.trace("Publishing new state {} of light {} to MQTT.", state, getName());
        if (state.getBrightness().equals(PercentType.ZERO)) {
            json.state = "OFF";
        } else {
            json.state = "ON";
            if (channelConfiguration.brightness || (channelConfiguration.supportedColorModes != null
                    && (channelConfiguration.supportedColorModes.contains(LightColorMode.COLOR_MODE_HS)
                            || channelConfiguration.supportedColorModes.contains(LightColorMode.COLOR_MODE_XY)))) {
                json.brightness = state.getBrightness().toBigDecimal()
                        .multiply(new BigDecimal(channelConfiguration.brightnessScale))
                        .divide(new BigDecimal(100), MathContext.DECIMAL128).intValue();
            }

            if (colorChannel != null) {
                json.color = new JSONState.Color();
                if (channelConfiguration.supportedColorModes.contains(LightColorMode.COLOR_MODE_HS)) {
                    json.color.h = state.getHue().toBigDecimal();
                    json.color.s = state.getSaturation().toBigDecimal().divide(BIG_DECIMAL_HUNDRED);
                } else if (LightColorMode.hasRGB(Objects.requireNonNull(channelConfiguration.supportedColorModes))) {
                    var rgb = state.toRGB();
                    json.color.r = rgb[0].toBigDecimal().multiply(SCALE_FACTOR).intValue();
                    json.color.g = rgb[1].toBigDecimal().multiply(SCALE_FACTOR).intValue();
                    json.color.b = rgb[2].toBigDecimal().multiply(SCALE_FACTOR).intValue();
                } else { // if (channelConfiguration.supportedColorModes.contains(COLOR_MODE_XY))
                    var xy = state.toXY();
                    json.color.x = xy[0].toBigDecimal().divide(BIG_DECIMAL_HUNDRED);
                    json.color.y = xy[1].toBigDecimal().divide(BIG_DECIMAL_HUNDRED);
                }
            }
        }

        publishState(json);
    }

    private void publishState(JSONState json) {
        String command = getGson().toJson(json);
        logger.debug("Publishing new state '{}' of light {} to MQTT.", command, getName());
        rawChannel.getState().publishValue(new StringType(command));
    }

    @Override
    protected boolean handleCommand(Command command) {
        JSONState json = new JSONState();
        if (command.getClass().equals(OnOffType.class)) {
            json.state = command.toString();
        } else if (command.getClass().equals(PercentType.class)) {
            if (command.equals(PercentType.ZERO)) {
                json.state = "OFF";
            } else {
                json.state = "ON";
                if (channelConfiguration.brightness) {
                    json.brightness = ((PercentType) command).toBigDecimal()
                            .multiply(new BigDecimal(channelConfiguration.brightnessScale))
                            .divide(new BigDecimal(100), MathContext.DECIMAL128).intValue();
                }
            }
        } else {
            return super.handleCommand(command);
        }

        String jsonCommand = getGson().toJson(json);
        logger.debug("Publishing new state '{}' of light {} to MQTT.", jsonCommand, getName());
        rawChannel.getState().publishValue(new StringType(jsonCommand));
        return false;
    }

    private boolean handleColorTempCommand(Command command) {
        JSONState json = new JSONState();

        if (command instanceof DecimalType) {
            command = new QuantityType<>(((DecimalType) command).toBigDecimal(), Units.MIRED);
        }
        if (command instanceof QuantityType) {
            QuantityType<?> mireds = ((QuantityType<?>) command).toInvertibleUnit(Units.MIRED);
            if (mireds == null) {
                logger.warn("Unable to convert {} to mireds", command);
                return false;
            }
            json.state = "ON";
            json.colorTemp = mireds.toBigDecimal().intValue();
        } else {
            return false;
        }

        String jsonCommand = getGson().toJson(json);
        logger.debug("Publishing new state '{}' of light {} to MQTT.", jsonCommand, getName());
        rawChannel.getState().publishValue(new StringType(jsonCommand));
        return false;
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        ChannelStateUpdateListener listener = this.channelStateUpdateListener;
        ComponentChannel localBrightnessChannel = brightnessChannel;
        ComponentChannel localColorChannel = colorChannel;

        @Nullable
        JSONState jsonState;
        try {
            jsonState = getGson().fromJson(state.toString(), JSONState.class);

            if (jsonState == null) {
                logger.warn("JSON light state for '{}' is empty.", getHaID());
                return;
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Cannot parse JSON light state '{}' for '{}'.", state, getHaID());
            return;
        }

        if (effectValue != null) {
            if (jsonState.effect != null) {
                effectValue.update(new StringType(jsonState.effect));
                listener.updateChannelState(buildChannelUID(EFFECT_CHANNEL_ID), effectValue.getChannelState());
            } else {
                listener.updateChannelState(buildChannelUID(EFFECT_CHANNEL_ID), UnDefType.NULL);
            }
        }

        boolean off = false;
        if (jsonState.state != null) {
            onOffValue.update((State) onOffValue.parseMessage(new StringType(jsonState.state)));
            off = onOffValue.getChannelState().equals(OnOffType.OFF);
            if (onOffValue.getChannelState() instanceof OnOffType onOffState) {
                if (brightnessValue.getChannelState() instanceof UnDefType) {
                    brightnessValue.update(Objects.requireNonNull(onOffState.as(PercentType.class)));
                }
                if (colorValue.getChannelState() instanceof UnDefType) {
                    colorValue.update(Objects.requireNonNull(onOffState.as(PercentType.class)));
                }
            }
        }

        PercentType brightness;
        if (off) {
            brightness = PercentType.ZERO;
        } else if (brightnessValue.getChannelState() instanceof PercentType percentValue) {
            brightness = percentValue;
        } else {
            brightness = PercentType.HUNDRED;
        }

        if (jsonState.brightness != null) {
            if (!off) {
                brightness = (PercentType) brightnessValue
                        .parseMessage(new DecimalType(Objects.requireNonNull(jsonState.brightness)));
            }
            brightnessValue.update(brightness);
            if (colorValue.getChannelState() instanceof HSBType) {
                HSBType color = (HSBType) colorValue.getChannelState();
                colorValue.update(new HSBType(color.getHue(), color.getSaturation(), brightness));
            } else {
                colorValue.update(new HSBType(DecimalType.ZERO, PercentType.ZERO, brightness));
            }
        }

        try {
            LightColorMode localColorMode = jsonState.colorMode;
            if (localColorMode != null) {
                colorModeValue.update(new StringType(localColorMode.serializedName()));

                switch (localColorMode) {
                    case COLOR_MODE_COLOR_TEMP:
                        Integer localColorTemp = jsonState.colorTemp;
                        if (localColorTemp == null) {
                            logger.warn("Incomplete color_temp received for {}", getHaID());
                        } else {
                            colorTempValue
                                    .update(new QuantityType(Objects.requireNonNull(jsonState.colorTemp), Units.MIRED));
                            listener.updateChannelState(buildChannelUID(
                                    newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED),
                                    colorTempValue.getChannelState());

                            // Populate the color channel (if there is one) to match the color temperature.
                            // First convert color temp to XY, then to HSB, then add in the brightness
                            try {
                                final double[] xy = ColorUtil.kelvinToXY(1000000d / localColorTemp);
                                HSBType color = ColorUtil.xyToHsb(xy);
                                color = new HSBType(color.getHue(), color.getSaturation(), brightness);
                                colorValue.update(color);
                            } catch (IndexOutOfBoundsException e) {
                                logger.warn("Color temperature {} cannot be converted to a color for {}",
                                        localColorTemp, getHaID());
                            }
                        }
                        break;
                    case COLOR_MODE_XY:
                        if (jsonState.color == null || jsonState.color.x == null || jsonState.color.y == null) {
                            logger.warn("Incomplete xy color received for {}", getHaID());
                        } else {
                            final double[] xy = new double[] { jsonState.color.x.doubleValue(),
                                    jsonState.color.y.doubleValue() };
                            HSBType newColor = ColorUtil.xyToHsb(xy);
                            colorValue.update(new HSBType(newColor.getHue(), newColor.getSaturation(), brightness));
                            if (colorTempChannel != null) {
                                double kelvin = ColorUtil.xyToKelvin(xy);
                                colorTempValue.update(new QuantityType(kelvin, Units.KELVIN));
                                listener.updateChannelState(buildChannelUID(
                                        newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED),
                                        colorTempValue.getChannelState());
                            }
                        }
                        break;
                    case COLOR_MODE_HS:
                        if (jsonState.color == null || jsonState.color.h == null || jsonState.color.s == null) {
                            logger.warn("Incomplete hs color received for {}", getHaID());
                        } else {
                            colorValue.update(new HSBType(new DecimalType(Objects.requireNonNull(jsonState.color.h)),
                                    new PercentType(Objects.requireNonNull(jsonState.color.s)), brightness));
                        }
                        break;
                    case COLOR_MODE_RGB:
                    case COLOR_MODE_RGBW:
                    case COLOR_MODE_RGBWW:
                        if (jsonState.color == null || jsonState.color.r == null || jsonState.color.g == null
                                || jsonState.color.b == null) {
                            logger.warn("Incomplete rgb color received for {}", getHaID());
                        } else {
                            colorValue.update(ColorUtil
                                    .rgbToHsb(new int[] { jsonState.color.r, jsonState.color.g, jsonState.color.b }));
                        }
                        break;
                    default:
                        break;
                }

                // calculate the CCT of the color (xy was special cased above, to do a more direct calculation)
                if (!localColorMode.equals(LightColorMode.COLOR_MODE_COLOR_TEMP)
                        && !localColorMode.equals(LightColorMode.COLOR_MODE_XY) && localColorChannel != null
                        && colorTempChannel != null && colorValue.getChannelState() instanceof HSBType colorState) {
                    final double[] xy = ColorUtil.hsbToXY(colorState);
                    double kelvin = ColorUtil.xyToKelvin(new double[] { xy[0], xy[1] });
                    colorTempValue.update(new QuantityType(kelvin, Units.KELVIN));
                    listener.updateChannelState(
                            buildChannelUID(
                                    newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED),
                            colorTempValue.getChannelState());
                }

            } else {
                // "deprecated" color mode handling - color mode not specified, so we just accept what we can. See
                // https://github.com/home-assistant/core/blob/4f965f0eca09f0d12ae1c98c6786054063a36b44/homeassistant/components/mqtt/light/schema_json.py#L258
                if (jsonState.colorTemp != null) {
                    colorTempValue.update(new QuantityType(Objects.requireNonNull(jsonState.colorTemp), Units.MIRED));
                    listener.updateChannelState(
                            buildChannelUID(
                                    newStyleChannels ? COLOR_TEMP_CHANNEL_ID : COLOR_TEMP_CHANNEL_ID_DEPRECATED),
                            colorTempValue.getChannelState());

                    colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_COLOR_TEMP.serializedName()));
                }

                if (jsonState.color != null) {
                    if (jsonState.color.h != null && jsonState.color.s != null) {
                        colorValue.update(new HSBType(new DecimalType(Objects.requireNonNull(jsonState.color.h)),
                                new PercentType(Objects.requireNonNull(jsonState.color.s)), brightness));
                        colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_HS.serializedName()));
                    } else if (jsonState.color.x != null && jsonState.color.y != null) {
                        HSBType newColor = ColorUtil.xyToHsb(
                                new double[] { jsonState.color.x.doubleValue(), jsonState.color.y.doubleValue() });
                        colorValue.update(new HSBType(newColor.getHue(), newColor.getSaturation(), brightness));
                        colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_XY.serializedName()));
                    } else if (jsonState.color.r != null && jsonState.color.g != null && jsonState.color.b != null) {
                        colorValue.update(ColorUtil
                                .rgbToHsb(new int[] { jsonState.color.r, jsonState.color.g, jsonState.color.b }));
                        colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_RGB.serializedName()));
                    }

                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid color value for {}", getHaID());
        }

        listener.updateChannelState(
                buildChannelUID(newStyleChannels ? COLOR_MODE_CHANNEL_ID : COLOR_MODE_CHANNEL_ID_DEPRECATED),
                colorModeValue.getChannelState());

        if (localColorChannel != null) {
            listener.updateChannelState(localColorChannel.getChannel().getUID(), colorValue.getChannelState());
        } else if (localBrightnessChannel != null) {
            listener.updateChannelState(localBrightnessChannel.getChannel().getUID(),
                    brightnessValue.getChannelState());
        } else {
            listener.updateChannelState(onOffChannel.getChannel().getUID(), onOffValue.getChannelState());
        }
    }
}
