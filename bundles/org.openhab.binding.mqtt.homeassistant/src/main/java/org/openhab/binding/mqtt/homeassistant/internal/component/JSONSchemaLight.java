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
import java.math.MathContext;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
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
public class JSONSchemaLight extends AbstractRawSchemaLight<JSONSchemaLight.Configuration> {
    private final Logger logger = LoggerFactory.getLogger(JSONSchemaLight.class);

    private @Nullable ComponentChannel colorTempChannel;
    protected @Nullable TextValue colorModeValue;

    private static class JSONState {
        static class Color {
            @Nullable
            Integer r, g, b; // , c, w; TODO: add RGBW and RGBWW support
            @Nullable
            Double x, y, h, s;
        }

        @Nullable
        String state;
        @Nullable
        Integer brightness;
        @SerializedName("color_mode")
        @Nullable
        String colorMode;
        @SerializedName("color_temp")
        @Nullable
        Integer colorTemp;
        @Nullable
        Color color;
        @Nullable
        String effect;
        // @Nullable Integer transition; TODO: add transition support
    }

    public static class Configuration extends Light.LightConfiguration {
        private final boolean brightness;
        private final int brightnessScale;
        private final @Nullable Set<String> supportedColorModes;

        public Configuration(Map<String, @Nullable Object> config) {
            super(config, "MQTT JSON Light");
            brightness = getBoolean("brightness");
            brightnessScale = getInt("brightness_scale");
            supportedColorModes = getOptionalStringSet("supported_color_modes");
        }

        boolean hasBrightness() {
            return brightness;
        }

        int getBrightnessScale() {
            return brightnessScale;
        }

        boolean hasEffect() {
            return getBoolean("effect");
        }

        boolean hasFlash() {
            return getBoolean("flash");
        }

        int getFlashTimeLong() {
            return getInt("flash_time_long");
        }

        int getFlashTimeShort() {
            return getInt("flash_time_short");
        }

        @Nullable
        Set<String> getSupportedColorModes() {
            return supportedColorModes;
        }

        boolean hasTransition() {
            return getBoolean("transition");
        }

        int getWhiteScale() {
            return getInt("white_scale");
        }
    }

    public JSONSchemaLight(ComponentFactory.ComponentContext builder, Map<String, @Nullable Object> config) {
        super(builder, new Configuration(config));
    }

    @Override
    protected void buildChannels() {
        OnOffValue onOffValue = this.onOffValue = new OnOffValue("ON", "OFF");
        brightnessValue = new PercentageValue(null, new BigDecimal(this.config.getBrightnessScale()), null, null, null,
                FORMAT_INTEGER);

        boolean hasColorChannel = false;
        AutoUpdatePolicy autoUpdatePolicy = optimistic ? AutoUpdatePolicy.RECOMMEND : null;
        Set<String> supportedColorModes = config.getSupportedColorModes();
        if (supportedColorModes != null) {
            if (LightColorMode.hasColorChannel(supportedColorModes)) {
                hasColorChannel = true;
            }

            if (supportedColorModes.contains(LightColorMode.COLOR_MODE_COLOR_TEMP)) {
                colorTempChannel = buildChannel(COLOR_TEMP_CHANNEL_ID, ComponentChannelType.NUMBER, colorTempValue,
                        "Color Temperature", this).commandTopic(DUMMY_TOPIC, true, 1)
                        .commandFilter(command -> handleColorTempCommand(command))
                        .withAutoUpdatePolicy(autoUpdatePolicy).build();
            }
        }
        if (hasColorChannel && supportedColorModes.size() > 1) {
            TextValue colorModeValue = this.colorModeValue = new TextValue(supportedColorModes.toArray(String[]::new));
            buildChannel(COLOR_MODE_CHANNEL_ID, ComponentChannelType.STRING, colorModeValue, "Color Mode", this)
                    .withAutoUpdatePolicy(autoUpdatePolicy).isAdvanced(true).build();
        }

        if (hasColorChannel) {
            colorChannel = buildChannel(COLOR_CHANNEL_ID, ComponentChannelType.COLOR, colorValue, "Color", this)
                    .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else if (config.hasBrightness() || (supportedColorModes != null
                && supportedColorModes.contains(LightColorMode.COLOR_MODE_BRIGHTNESS))) {
            brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID, ComponentChannelType.DIMMER, brightnessValue,
                    "Brightness", this).commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
        } else {
            onOffChannel = buildChannel(SWITCH_CHANNEL_ID, ComponentChannelType.SWITCH, onOffValue, "On/Off State",
                    this).commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand)
                    .withAutoUpdatePolicy(autoUpdatePolicy).build();
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

        if (state.getBrightness().equals(PercentType.ZERO)) {
            json.state = "OFF";
        } else {
            json.state = "ON";
            Set<String> supportedColorModes = config.getSupportedColorModes();
            if (config.hasBrightness()
                    || (supportedColorModes != null && (supportedColorModes.contains(LightColorMode.COLOR_MODE_HS)
                            || supportedColorModes.contains(LightColorMode.COLOR_MODE_XY)))) {
                json.brightness = state.getBrightness().toBigDecimal()
                        .multiply(new BigDecimal(config.getBrightnessScale()))
                        .divide(new BigDecimal(100), MathContext.DECIMAL128).intValue();
            }

            if (colorChannel != null) {
                supportedColorModes = Objects.requireNonNull(supportedColorModes);
                JSONState.Color color = json.color = new JSONState.Color();
                if (supportedColorModes.contains(LightColorMode.COLOR_MODE_HS)) {
                    color.h = state.getHue().doubleValue();
                    color.s = state.getSaturation().doubleValue() / 100d;
                } else if (LightColorMode.hasRGB(supportedColorModes)) {
                    var rgb = ColorUtil.hsbToRgb(state);
                    color.r = rgb[0];
                    color.g = rgb[1];
                    color.b = rgb[2];
                } else { // if (config.supportedColorModes.contains(COLOR_MODE_XY))
                    var xy = ColorUtil.hsbToXY(state);
                    color.x = xy[0];
                    color.y = xy[1];
                }
            }
        }

        publishState(json);
    }

    private void publishState(JSONState json) {
        String command = componentContext.getGson().toJson(json);
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
                if (config.hasBrightness()) {
                    json.brightness = ((PercentType) command).toBigDecimal()
                            .multiply(new BigDecimal(config.brightnessScale))
                            .divide(new BigDecimal(100), MathContext.DECIMAL128).intValue();
                }
            }
        } else {
            return super.handleCommand(command);
        }

        String jsonCommand = componentContext.getGson().toJson(json);
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

        String jsonCommand = componentContext.getGson().toJson(json);
        rawChannel.getState().publishValue(new StringType(jsonCommand));
        return false;
    }

    @Override
    public void updateChannelState(ChannelUID channel, State state) {
        ChannelStateUpdateListener listener = this.channelStateUpdateListener;
        ComponentChannel brightnessChannel = this.brightnessChannel;
        ComponentChannel colorChannel = this.colorChannel;
        OnOffValue onOffValue = Objects.requireNonNull(this.onOffValue);

        @Nullable
        JSONState jsonState;
        try {
            jsonState = componentContext.getGson().fromJson(state.toString(), JSONState.class);

            if (jsonState == null) {
                logger.warn("JSON light state for '{}' is empty.", getHaID());
                return;
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Cannot parse JSON light state '{}' for '{}'.", state, getHaID());
            return;
        }

        TextValue effectValue = this.effectValue;
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

        Integer jsonBrightness = jsonState.brightness;
        if (jsonBrightness != null) {
            if (!off) {
                brightness = (PercentType) brightnessValue.parseMessage(new DecimalType(jsonBrightness));
            }
            brightnessValue.update(brightness);
            if (colorValue.getChannelState() instanceof HSBType) {
                HSBType color = (HSBType) colorValue.getChannelState();
                colorValue.update(new HSBType(color.getHue(), color.getSaturation(), brightness));
            } else {
                colorValue.update(new HSBType(DecimalType.ZERO, PercentType.ZERO, brightness));
            }
        }

        TextValue colorModeValue = this.colorModeValue;
        try {
            String colorMode = jsonState.colorMode;
            JSONState.Color color = jsonState.color;
            if (colorMode != null) {
                if (colorModeValue != null) {
                    colorModeValue.update(new StringType(colorMode));
                }

                switch (colorMode) {
                    case LightColorMode.COLOR_MODE_COLOR_TEMP:
                        Integer colorTemp = jsonState.colorTemp;
                        if (colorTemp == null) {
                            logger.warn("Incomplete color_temp received for {}", getHaID());
                        } else {
                            colorTempValue.update(
                                    QuantityType.valueOf(Objects.requireNonNull(jsonState.colorTemp), Units.MIRED));
                            listener.updateChannelState(buildChannelUID(COLOR_TEMP_CHANNEL_ID),
                                    colorTempValue.getChannelState());

                            // Populate the color channel (if there is one) to match the color temperature.
                            // First convert color temp to XY, then to HSB, then add in the brightness
                            try {
                                final double[] xy = ColorUtil.kelvinToXY(1000000d / colorTemp);
                                HSBType newColor = ColorUtil.xyToHsb(xy);
                                newColor = new HSBType(newColor.getHue(), newColor.getSaturation(), brightness);
                                colorValue.update(newColor);
                            } catch (IndexOutOfBoundsException e) {
                                logger.warn("Color temperature {} cannot be converted to a color for {}", colorTemp,
                                        getHaID());
                            }
                        }
                        break;
                    case LightColorMode.COLOR_MODE_XY:
                        if (color == null || color.x == null || color.y == null) {
                            logger.warn("Incomplete xy color received for {}", getHaID());
                        } else {
                            final double[] xy = new double[] { Objects.requireNonNull(color.x).doubleValue(),
                                    Objects.requireNonNull(color.y).doubleValue() };
                            HSBType newColor = ColorUtil.xyToHsb(xy);
                            colorValue.update(new HSBType(newColor.getHue(), newColor.getSaturation(), brightness));
                            if (colorTempChannel != null) {
                                double kelvin = ColorUtil.xyToKelvin(xy);
                                colorTempValue.update(QuantityType.valueOf(kelvin, Units.KELVIN));
                                listener.updateChannelState(buildChannelUID(COLOR_TEMP_CHANNEL_ID),
                                        colorTempValue.getChannelState());
                            }
                        }
                        break;
                    case LightColorMode.COLOR_MODE_HS:
                        if (color == null || color.h == null || color.s == null) {
                            logger.warn("Incomplete hs color received for {}", getHaID());
                        } else {
                            colorValue.update(new HSBType(new DecimalType(Objects.requireNonNull(color.h)),
                                    new PercentType(BigDecimal.valueOf(Objects.requireNonNull(color.s))), brightness));
                        }
                        break;
                    case LightColorMode.COLOR_MODE_RGB:
                    case LightColorMode.COLOR_MODE_RGBW:
                    case LightColorMode.COLOR_MODE_RGBWW:
                        if (color == null || color.r == null || color.g == null || color.b == null) {
                            logger.warn("Incomplete rgb color received for {}", getHaID());
                        } else {
                            colorValue.update(ColorUtil.rgbToHsb(new int[] { color.r, color.g, color.b }));
                        }
                        break;
                    default:
                        break;
                }

                // calculate the CCT of the color (xy was special cased above, to do a more direct calculation)
                if (!colorMode.equals(LightColorMode.COLOR_MODE_COLOR_TEMP)
                        && !colorMode.equals(LightColorMode.COLOR_MODE_XY) && colorChannel != null
                        && colorTempChannel != null && colorValue.getChannelState() instanceof HSBType colorState) {
                    final double[] xy = ColorUtil.hsbToXY(colorState);
                    double kelvin = ColorUtil.xyToKelvin(new double[] { xy[0], xy[1] });
                    colorTempValue.update(QuantityType.valueOf(kelvin, Units.KELVIN));
                    listener.updateChannelState(buildChannelUID(COLOR_TEMP_CHANNEL_ID),
                            colorTempValue.getChannelState());
                }

            } else {
                // "deprecated" color mode handling - color mode not specified, so we just accept what we can. See
                // https://github.com/home-assistant/core/blob/4f965f0eca09f0d12ae1c98c6786054063a36b44/homeassistant/components/mqtt/light/schema_json.py#L258
                Integer colorTemp = jsonState.colorTemp;
                if (colorTemp != null) {
                    colorTempValue.update(QuantityType.valueOf(colorTemp, Units.MIRED));
                    listener.updateChannelState(buildChannelUID(COLOR_TEMP_CHANNEL_ID),
                            colorTempValue.getChannelState());

                    if (colorModeValue != null) {
                        colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_COLOR_TEMP));
                    }
                }

                if (color != null) {
                    if (color.h != null && color.s != null) {
                        colorValue.update(new HSBType(new DecimalType(Objects.requireNonNull(color.h)),
                                new PercentType(BigDecimal.valueOf(Objects.requireNonNull(color.s))), brightness));
                        if (colorModeValue != null) {
                            colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_HS));
                        }
                    } else if (color.x != null && color.y != null) {
                        HSBType newColor = ColorUtil
                                .xyToHsb(new double[] { Objects.requireNonNull(color.x).doubleValue(),
                                        Objects.requireNonNull(color.y).doubleValue() });
                        colorValue.update(new HSBType(newColor.getHue(), newColor.getSaturation(), brightness));
                        if (colorModeValue != null) {
                            colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_XY));
                        }
                    } else if (color.r != null && color.g != null && color.b != null) {
                        colorValue.update(ColorUtil.rgbToHsb(new int[] { color.r, color.g, color.b }));
                        if (colorModeValue != null) {
                            colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_RGB));
                        }
                    }

                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid color value for {}", getHaID());
        }

        if (colorModeValue != null) {
            listener.updateChannelState(buildChannelUID(COLOR_MODE_CHANNEL_ID), colorModeValue.getChannelState());
        }

        if (colorChannel != null) {
            listener.updateChannelState(colorChannel.getChannel().getUID(), colorValue.getChannelState());
        } else if (brightnessChannel != null) {
            listener.updateChannelState(brightnessChannel.getChannel().getUID(), brightnessValue.getChannelState());
        } else {
            listener.updateChannelState(Objects.requireNonNull(onOffChannel).getChannel().getUID(),
                    onOffValue.getChannelState());
        }
    }
}
