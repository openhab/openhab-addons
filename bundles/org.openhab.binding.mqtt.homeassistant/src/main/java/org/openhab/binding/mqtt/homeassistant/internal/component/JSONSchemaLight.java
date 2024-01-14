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
import org.openhab.binding.mqtt.homeassistant.internal.exception.UnsupportedComponentException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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

    TextValue colorModeValue;

    public JSONSchemaLight(ComponentFactory.ComponentConfiguration builder) {
        super(builder);
        colorModeValue = new TextValue();
    }

    @Override
    protected void buildChannels() {
        List<LightColorMode> supportedColorModes = channelConfiguration.supportedColorModes;
        if (supportedColorModes != null && supportedColorModes.contains(LightColorMode.COLOR_MODE_COLOR_TEMP)) {
            colorModeValue = new TextValue(
                    supportedColorModes.stream().map(LightColorMode::serializedName).toArray(String[]::new));
            buildChannel(COLOR_MODE_CHANNEL_ID, colorModeValue, "Color Mode", this).isAdvanced(true).build();
        }

        if (channelConfiguration.colorMode) {
            if (supportedColorModes == null || channelConfiguration.supportedColorModes.isEmpty()) {
                throw new UnsupportedComponentException("JSON schema light with color modes '" + getHaID()
                        + "' does not define supported_color_modes!");
            }

            if (LightColorMode.hasColorChannel(supportedColorModes)) {
                hasColorChannel = true;
            }

            if (supportedColorModes.contains(LightColorMode.COLOR_MODE_COLOR_TEMP)) {
                buildChannel(COLOR_TEMP_CHANNEL_ID, colorTempValue, "Color Temperature", this)
                        .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(command -> handleColorTempCommand(command))
                        .build();
            }
        }

        if (hasColorChannel) {
            buildChannel(COLOR_CHANNEL_ID, colorValue, "Color", this).commandTopic(DUMMY_TOPIC, true, 1)
                    .commandFilter(this::handleCommand).build();
        } else if (channelConfiguration.brightness) {
            brightnessChannel = buildChannel(BRIGHTNESS_CHANNEL_ID, brightnessValue, "Brightness", this)
                    .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand).build();
        } else {
            onOffChannel = buildChannel(ON_OFF_CHANNEL_ID, onOffValue, "On/Off State", this)
                    .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(this::handleCommand).build();
        }

        if (effectValue != null) {
            buildChannel(EFFECT_CHANNEL_ID, Objects.requireNonNull(effectValue), "Lighting Effect", this)
                    .commandTopic(DUMMY_TOPIC, true, 1).commandFilter(command -> handleEffectCommand(command)).build();

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

            if (hasColorChannel) {
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
            onOffValue.update(onOffValue.parseCommand(new StringType(jsonState.state)));
            off = onOffValue.getChannelState().equals(OnOffType.OFF);
            if (brightnessValue.getChannelState() instanceof UnDefType) {
                brightnessValue.update(off ? PercentType.ZERO : PercentType.HUNDRED);
            }
            if (colorValue.getChannelState() instanceof UnDefType) {
                colorValue.update(off ? HSBType.BLACK : HSBType.WHITE);
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

        if (jsonState.colorTemp != null) {
            colorTempValue.update(new QuantityType(Objects.requireNonNull(jsonState.colorTemp), Units.MIRED));
            listener.updateChannelState(buildChannelUID(COLOR_TEMP_CHANNEL_ID), colorTempValue.getChannelState());

            colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_COLOR_TEMP.serializedName()));
        }

        if (jsonState.color != null) {
            // This corresponds to "deprecated" color mode handling, since we're not checking which color
            // mode is currently active.
            // HS is highest priority, then XY, then RGB
            // See
            // https://github.com/home-assistant/core/blob/4f965f0eca09f0d12ae1c98c6786054063a36b44/homeassistant/components/mqtt/light/schema_json.py#L258
            if (jsonState.color.h != null && jsonState.color.s != null) {
                colorValue.update(new HSBType(new DecimalType(Objects.requireNonNull(jsonState.color.h)),
                        new PercentType(Objects.requireNonNull(jsonState.color.s)), brightness));
                colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_HS.serializedName()));
            } else if (jsonState.color.x != null && jsonState.color.y != null) {
                HSBType newColor = HSBType.fromXY(jsonState.color.x.floatValue(), jsonState.color.y.floatValue());
                colorValue.update(new HSBType(newColor.getHue(), newColor.getSaturation(), brightness));
                colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_XY.serializedName()));
            } else if (jsonState.color.r != null && jsonState.color.g != null && jsonState.color.b != null) {
                colorValue.update(HSBType.fromRGB(jsonState.color.r, jsonState.color.g, jsonState.color.b));
                colorModeValue.update(new StringType(LightColorMode.COLOR_MODE_RGB.serializedName()));
            }
        }

        if (jsonState.colorMode != null) {
            colorModeValue.update(new StringType(jsonState.colorMode.serializedName()));
        }

        listener.updateChannelState(buildChannelUID(COLOR_MODE_CHANNEL_ID), colorModeValue.getChannelState());

        if (hasColorChannel) {
            listener.updateChannelState(buildChannelUID(COLOR_CHANNEL_ID), colorValue.getChannelState());
        } else if (brightnessChannel != null) {
            listener.updateChannelState(buildChannelUID(BRIGHTNESS_CHANNEL_ID), brightnessValue.getChannelState());
        } else {
            listener.updateChannelState(buildChannelUID(ON_OFF_CHANNEL_ID), onOffValue.getChannelState());
        }
    }
}
