/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.entity.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.CommandMapper;
import org.openhab.binding.hasslink.internal.entity.util.OptionUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LightEntity} class represents a light entity type in the Home Assistant binding.
 * It provides methods to build channels and update states specific to light devices.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class LightEntity implements EntityType {

    private static final String MODE_BRIGHTNESS = "brightness";
    private static final String MODE_COLOR_TEMP = "color_temp";

    private static final String HS_COLOR_MODE = "hs";
    private static final String RGB_COLOR_MODE = "rgb";
    private static final String RGBW_COLOR_MODE = "rgbw";
    private static final String RGBWW_COLOR_MODE = "rgbww";
    private static final String XY_COLOR_MODE = "xy";
    private static final Set<String> COLOR_MODES = Set.of(HS_COLOR_MODE, RGB_COLOR_MODE, RGBW_COLOR_MODE,
            RGBWW_COLOR_MODE, XY_COLOR_MODE);

    private final Logger logger = LoggerFactory.getLogger(LightEntity.class);

    @Override
    public String getType() {
        return "light";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        ChannelSpecsBuilder builder = ChannelSpecsBuilder.create(entityState, context);

        Set<String> supportedModes = new HashSet<>(entityState.getAttributeAsStringList("supported_color_modes"));

        ItemType primaryItemType = ItemType.SWITCH;

        if (!supportedModes.isEmpty()) {
            boolean colorSupported = !Collections.disjoint(supportedModes, COLOR_MODES);
            boolean brightnessSupported = !colorSupported && supportedModes.contains(MODE_BRIGHTNESS);
            boolean colorTempSupported = supportedModes.contains(MODE_COLOR_TEMP);

            if (colorSupported) {
                primaryItemType = ItemType.COLOR;
            } else if (brightnessSupported) {
                primaryItemType = ItemType.DIMMER;
            }

            if (colorTempSupported) {
                builder.add("color_temp", ItemType.number("Temperature"));

                Integer minKelvin = entityState.getAttributeAsInt("min_color_temp_kelvin");
                Integer maxKelvin = entityState.getAttributeAsInt("max_color_temp_kelvin");
                if (minKelvin != null && maxKelvin != null) {
                    builder.add("color_temp_percent", ItemType.DIMMER);
                }
            }
        }
        return builder //
                .addPrimaryChannel(primaryItemType) //
                .addAttr("effect", ItemType.STRING) //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String attribute,
            EntityContext context) {
        return switch (attribute) {
            case "color_temp" -> {
                Integer minKelvin = entityState.getAttributeAsInt("min_color_temp_kelvin");
                Integer maxKelvin = entityState.getAttributeAsInt("max_color_temp_kelvin");
                if (minKelvin != null && maxKelvin != null) {
                    yield OptionUtils.extractRange(entityState, "min_color_temp_kelvin", "max_color_temp_kelvin", null);
                }

                // Some older integrations may use mireds instead of kelvin, so we check for those attributes as well
                String colorTemp = entityState.getAttributeAsString("color_temp");
                Integer minMireds = entityState.getAttributeAsInt("min_mireds");
                Integer maxMireds = entityState.getAttributeAsInt("max_mireds");
                if (colorTemp != null && minMireds != null && maxMireds != null) {
                    yield OptionUtils.extractRange(entityState, "min_mireds", "max_mireds", null);
                }
                yield null;
            }

            case "effect" -> OptionUtils.extractStateOptions(entityState, "effect_list");

            default -> null;
        };
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        StateMapBuilder builder = StateMapBuilder.create(entityState, context);

        parseColorTemp(builder, entityState);

        boolean isOn = !"off".equalsIgnoreCase(entityState.state());

        // Color
        List<?> hsColor = entityState.getAttributeAsList("hs_color");
        List<?> rgbColor = entityState.getAttributeAsList("rgb_color");
        Integer rawBrightness = entityState.getAttributeAsInt("brightness");
        PercentType brightness = PercentType.HUNDRED;

        if (rawBrightness != null) {
            brightness = new PercentType(new BigDecimal((rawBrightness / 255.0) * 100.0));
        }

        State primaryState;

        if (hsColor != null && hsColor.size() >= 2 //
                && hsColor.get(0) instanceof Number rawHue //
                && hsColor.get(1) instanceof Number rawSaturation) {

            DecimalType h = new DecimalType(rawHue.doubleValue());
            PercentType s = new PercentType(new BigDecimal(rawSaturation.doubleValue()));
            primaryState = new HSBType(h, s, brightness);

        } else if (rgbColor != null && rgbColor.size() >= 3 //
                && rgbColor.get(0) instanceof Number r //
                && rgbColor.get(1) instanceof Number g //
                && rgbColor.get(2) instanceof Number b) {

            HSBType rgbHSB = HSBType.fromRGB(r.intValue(), g.intValue(), b.intValue());
            primaryState = new HSBType(rgbHSB.getHue(), rgbHSB.getSaturation(), brightness);

        } else if (rawBrightness != null) {
            primaryState = isOn ? brightness : OnOffType.OFF;
        } else {
            primaryState = OnOffType.from(isOn);
        }

        if (primaryState instanceof HSBType hsb && !isOn) {
            primaryState = new HSBType(hsb.getHue(), hsb.getSaturation(), PercentType.ZERO);
        }

        if (entityState.isUnavailableOrUnknown()) {
            primaryState = UnDefType.UNDEF;
        }

        return builder // THIS IS FIXED BY SPOTLESS
                .putPrimaryState(primaryState) //
                .putString("effect") //
                .build();
    }

    private void parseColorTemp(StateMapBuilder builder, EntityState entityState) {
        Integer colorTempKelvin = entityState.getAttributeAsInt("color_temp_kelvin");
        Integer colorTempMireds = entityState.getAttributeAsInt("color_temp");

        if (colorTempKelvin != null) {
            builder.put("color_temp", new QuantityType<>(colorTempKelvin, Units.KELVIN));

            Integer minKelvin = entityState.getAttributeAsInt("min_color_temp_kelvin");
            Integer maxKelvin = entityState.getAttributeAsInt("max_color_temp_kelvin");
            if (minKelvin != null && maxKelvin != null) {
                double percentValue = (colorTempKelvin - minKelvin) * 100.0 / (maxKelvin - minKelvin);
                percentValue = Math.max(0.0, Math.min(100.0, percentValue));
                // Low Kelvin value = warm, high Kelvin value = cool,
                // but our percentage wants 0% = cool, 100% = warm
                // so we invert the percentage to match the expected behavior
                double invertedPercentValue = 100.0 - percentValue;
                builder.put("color_temp_percent", new PercentType(BigDecimal.valueOf(invertedPercentValue)));
            }
        } else if (colorTempMireds != null && colorTempMireds > 0) {
            QuantityType<?> miredQuantity = new QuantityType<>(colorTempMireds, Units.MIRED);
            QuantityType<?> kelvinQuantity = miredQuantity.toInvertibleUnit(Units.KELVIN);
            if (kelvinQuantity != null) {
                builder.put("color_temp", kelvinQuantity);
            }

            Integer minMireds = entityState.getAttributeAsInt("min_mireds");
            Integer maxMireds = entityState.getAttributeAsInt("max_mireds");
            if (minMireds != null && maxMireds != null) {
                double percentValue = (colorTempMireds - minMireds) * 100.0 / (maxMireds - minMireds);
                percentValue = Math.max(0.0, Math.min(100.0, percentValue));
                builder.put("color_temp_percent", new PercentType(BigDecimal.valueOf(percentValue)));
            }
        }
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {

        return switch (attribute) {
            case EntityType.PRIMARY_ATTR -> handlePrimaryCommand(entityId, command, entityState);

            case "color_temp" -> extractKelvin(command)
                    .map(kelvin -> new ServiceCall("light", "turn_on", entityId, Map.of("color_temp_kelvin", kelvin)));

            case "color_temp_percent" -> {
                if (entityState == null) {
                    yield Optional.empty();
                }

                if (command instanceof OnOffType onOff) {
                    command = onOff.equals(OnOffType.OFF) ? PercentType.ZERO : PercentType.HUNDRED;
                }

                if (!(command instanceof PercentType percent)) {
                    yield Optional.empty();
                }

                Integer minKelvin = entityState.getAttributeAsInt("min_color_temp_kelvin");
                Integer maxKelvin = entityState.getAttributeAsInt("max_color_temp_kelvin");
                if (minKelvin != null && maxKelvin != null) {
                    double directPercent = 100.0 - percent.doubleValue();
                    long kelvin = Math.round(minKelvin + (directPercent / 100.0) * (maxKelvin - minKelvin));
                    yield Optional
                            .of(new ServiceCall("light", "turn_on", entityId, Map.of("color_temp_kelvin", kelvin)));
                }

                Integer minMireds = entityState.getAttributeAsInt("min_mireds");
                Integer maxMireds = entityState.getAttributeAsInt("max_mireds");
                if (minMireds != null && maxMireds != null) {
                    long mireds = Math.round(minMireds + (percent.doubleValue() / 100.0) * (maxMireds - minMireds));
                    yield Optional.of(new ServiceCall("light", "turn_on", entityId, Map.of("color_temp", mireds)));
                }
                yield Optional.empty();
            }

            case "effect" -> CommandMapper.onString(command, "light", "turn_on", "effect", entityId);

            default -> Optional.empty();
        };
    }

    private Optional<ServiceCall> handlePrimaryCommand(String entityId, Command command, @Nullable EntityState entityState) {

        return switch (command) {
            case OnOffType onOff -> CommandMapper.onOff(onOff, "light", entityId);

            // HSBType extends PercentType, so HSBType must come BEFORE PercentType
            case HSBType hsb when hsb.getBrightness().equals(PercentType.ZERO) ->
                CommandMapper.onOff(OnOffType.OFF, "light", entityId);

            case HSBType hsb -> {
                Set<String> supportedModes = entityState != null //
                        ? new HashSet<>(entityState.getAttributeAsStringList("supported_color_modes")) //
                        : Set.of();

                boolean supportsRgb = supportedModes.contains(RGB_COLOR_MODE) //
                        || supportedModes.contains(RGBW_COLOR_MODE) //
                        || supportedModes.contains(RGBWW_COLOR_MODE);
                boolean supportsHs = supportedModes.contains(HS_COLOR_MODE);
                boolean supportsXy = supportedModes.contains(XY_COLOR_MODE);

                if (supportsRgb && !supportsHs && !supportsXy) {
                    yield CommandMapper.onHSBToRGB(hsb, "light", "turn_on", "rgb_color", entityId);
                }
                yield CommandMapper.onHSB(hsb, "light", entityId);
            }

            case PercentType percent when percent.equals(PercentType.ZERO) ->
                CommandMapper.onOff(OnOffType.OFF, "light", entityId);

            case PercentType percent ->
                CommandMapper.onPercentScaled(percent, "light", "turn_on", "brightness", entityId, 0, 255);

            default -> Optional.empty();
        };
    }

    private Optional<Long> extractKelvin(Command command) {
        if (command instanceof QuantityType<?> quantity) {
            if (quantity.toInvertibleUnit(Units.KELVIN) instanceof QuantityType<?> kelvinQuantity) {
                return Optional.of(kelvinQuantity.longValue());
            } else {
                logger.warn("Received color temperature command with unsupported unit: {}", quantity.getUnit());
                return Optional.empty();
            }
        } else if (command instanceof DecimalType decimal) {
            long val = decimal.longValue();
            if (val <= 0) {
                return Optional.empty();
            }
            long kelvin = val <= 1000 ? Math.round(1_000_000.0 / val) : val;
            return Optional.of(kelvin);
        }
        return Optional.empty();
    }
}
