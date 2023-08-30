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
package org.openhab.binding.hue.internal.dto.clip2.helper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.Alerts;
import org.openhab.binding.hue.internal.dto.clip2.ColorTemperature;
import org.openhab.binding.hue.internal.dto.clip2.ColorXy;
import org.openhab.binding.hue.internal.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.dto.clip2.Effects;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.MirekSchema;
import org.openhab.binding.hue.internal.dto.clip2.OnState;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.dto.clip2.enums.EffectType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.Command;
import org.openhab.core.util.ColorUtil;
import org.openhab.core.util.ColorUtil.Gamut;

/**
 * Advanced setter methods for fields in the Resource class for special cases where setting the new value in the target
 * resource depends on logic using the values of other fields in a another source Resource.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Setters {

    /**
     * Setter for Alert field:
     * Use the given command value to set the target resource DTO value based on the attributes of the source resource
     * (if any).
     *
     * @param target the target resource.
     * @param command the new state command should be a StringType.
     * @param source another resource containing the allowed alert action values.
     *
     * @return the target resource.
     */
    public static Resource setAlert(Resource target, Command command, @Nullable Resource source) {
        if ((command instanceof StringType) && Objects.nonNull(source)) {
            Alerts otherAlert = source.getAlerts();
            if (Objects.nonNull(otherAlert)) {
                ActionType actionType = ActionType.of(((StringType) command).toString());
                if (otherAlert.getActionValues().contains(actionType)) {
                    target.setAlerts(new Alerts().setAction(actionType));
                }
            }
        }
        return target;
    }

    /**
     * Setter for Color Temperature field:
     * Use the given command value to set the target resource DTO value based on the attributes of the source resource
     * (if any).
     *
     * @param target the target resource.
     * @param command the new state command should be a QuantityType<Temperature> (but it can also handle DecimalType).
     * @param source another resource containing the MirekSchema.
     *
     * @return the target resource.
     */
    public static Resource setColorTemperatureAbsolute(Resource target, Command command, @Nullable Resource source) {
        QuantityType<?> mirek;
        if (command instanceof QuantityType<?>) {
            QuantityType<?> quantity = (QuantityType<?>) command;
            Unit<?> unit = quantity.getUnit();
            if (Units.KELVIN.equals(unit)) {
                mirek = quantity.toInvertibleUnit(Units.MIRED);
            } else if (Units.MIRED.equals(unit)) {
                mirek = quantity;
            } else {
                QuantityType<?> kelvin = quantity.toInvertibleUnit(Units.KELVIN);
                mirek = Objects.nonNull(kelvin) ? kelvin.toInvertibleUnit(Units.MIRED) : null;
            }
        } else if (command instanceof DecimalType) {
            mirek = QuantityType.valueOf(((DecimalType) command).doubleValue(), Units.KELVIN)
                    .toInvertibleUnit(Units.MIRED);
        } else {
            mirek = null;
        }
        if (Objects.nonNull(mirek)) {
            MirekSchema schema = target.getMirekSchema();
            schema = Objects.nonNull(schema) ? schema : Objects.nonNull(source) ? source.getMirekSchema() : null;
            schema = Objects.nonNull(schema) ? schema : MirekSchema.DEFAULT_SCHEMA;
            ColorTemperature colorTemperature = target.getColorTemperature();
            colorTemperature = Objects.nonNull(colorTemperature) ? colorTemperature : new ColorTemperature();
            double min = schema.getMirekMinimum();
            double max = schema.getMirekMaximum();
            double val = Math.max(min, Math.min(max, mirek.doubleValue()));
            target.setColorTemperature(colorTemperature.setMirek(val));
        }
        return target;
    }

    /**
     * Setter for Color Temperature field:
     * Use the given command value to set the target resource DTO value based on the attributes of the source resource
     * (if any).
     *
     * @param target the target resource.
     * @param command the new state command should be a PercentType.
     * @param source another resource containing the MirekSchema.
     *
     * @return the target resource.
     */
    public static Resource setColorTemperaturePercent(Resource target, Command command, @Nullable Resource source) {
        if (command instanceof PercentType) {
            MirekSchema schema = target.getMirekSchema();
            schema = Objects.nonNull(schema) ? schema : Objects.nonNull(source) ? source.getMirekSchema() : null;
            schema = Objects.nonNull(schema) ? schema : MirekSchema.DEFAULT_SCHEMA;
            ColorTemperature colorTemperature = target.getColorTemperature();
            colorTemperature = Objects.nonNull(colorTemperature) ? colorTemperature : new ColorTemperature();
            double min = schema.getMirekMinimum();
            double max = schema.getMirekMaximum();
            double val = min + ((max - min) * ((PercentType) command).doubleValue() / 100f);
            target.setColorTemperature(colorTemperature.setMirek(val));
        }
        return target;
    }

    /**
     * Setter for Color Xy field:
     * Use the given command value to set the target resource DTO value based on the attributes of the source resource
     * (if any). Use the HS parts of the HSB value to set the value of the 'ColorXy' JSON element, and ignore the 'B'
     * part.
     *
     * @param target the target resource.
     * @param command the new state command should be an HSBType with the new color XY value.
     * @param source another resource containing the color Gamut.
     *
     * @return the target resource.
     */
    public static Resource setColorXy(Resource target, Command command, @Nullable Resource source) {
        if (command instanceof HSBType) {
            Gamut gamut = target.getGamut();
            gamut = Objects.nonNull(gamut) ? gamut : Objects.nonNull(source) ? source.getGamut() : null;
            gamut = Objects.nonNull(gamut) ? gamut : ColorUtil.DEFAULT_GAMUT;
            HSBType hsb = (HSBType) command;
            ColorXy color = target.getColorXy();
            target.setColorXy((Objects.nonNull(color) ? color : new ColorXy()).setXY(ColorUtil.hsbToXY(hsb, gamut)));
        }
        return target;
    }

    /**
     * Setter for Dimming field:
     * Use the given command value to set the target resource DTO value based on the attributes of the source resource
     * (if any).
     *
     * @param target the target resource.
     * @param command the new state command should be a PercentType with the new dimming parameter.
     * @param source another resource containing the minimum dimming level.
     *
     * @return the target resource.
     */
    public static Resource setDimming(Resource target, Command command, @Nullable Resource source) {
        if (command instanceof PercentType) {
            Double min = target.getMinimumDimmingLevel();
            min = Objects.nonNull(min) ? min : Objects.nonNull(source) ? source.getMinimumDimmingLevel() : null;
            min = Objects.nonNull(min) ? min : Dimming.DEFAULT_MINIMUM_DIMMIMG_LEVEL;
            PercentType brightness = (PercentType) command;
            if (brightness.doubleValue() < min.doubleValue()) {
                brightness = new PercentType(new BigDecimal(min, Resource.PERCENT_MATH_CONTEXT));
            }
            Dimming dimming = target.getDimming();
            dimming = Objects.nonNull(dimming) ? dimming : new Dimming();
            dimming.setBrightness(brightness.doubleValue());
            target.setDimming(dimming);
        }
        return target;
    }

    /**
     * Setter for Effect field:
     * Use the given command value to set the target resource DTO value based on the attributes of the source resource
     * (if any).
     *
     * @param target the target resource.
     * @param command the new state command should be a StringType.
     * @param source another resource containing the allowed effect action values.
     *
     * @return the target resource.
     */
    public static Resource setEffect(Resource target, Command command, @Nullable Resource source) {
        if ((command instanceof StringType) && Objects.nonNull(source)) {
            Effects otherEffects = source.getEffects();
            if (Objects.nonNull(otherEffects)) {
                EffectType effectType = EffectType.of(((StringType) command).toString());
                if (otherEffects.allows(effectType)) {
                    target.setEffects(new Effects().setEffect(effectType));
                }
            }
        }
        return target;
    }

    /**
     * Setter to copy persisted fields from the source Resource into the target Resource. If the field in the target is
     * null and the same field in the source is not null, then the value from the source is copied to the target. This
     * method allows 'hasSparseData' resources to expand themselves to include necessary fields taken over from a
     * previously cached full data resource.
     *
     * @param target the target resource.
     * @param source another resource containing the values to be taken over.
     *
     * @return the target resource.
     */
    public static Resource setResource(Resource target, Resource source) {
        // on
        OnState targetOnOff = target.getOnState();
        OnState sourceOnOff = source.getOnState();
        if (Objects.isNull(targetOnOff) && Objects.nonNull(sourceOnOff)) {
            target.setOnState(sourceOnOff);
        }
        // dimming
        Dimming targetDimming = target.getDimming();
        Dimming sourceDimming = source.getDimming();
        if (Objects.isNull(targetDimming) && Objects.nonNull(sourceDimming)) {
            target.setDimming(sourceDimming);
            targetDimming = target.getDimming();
        }
        // minimum dimming level
        Double targetMinDimmingLevel = Objects.nonNull(targetDimming) ? targetDimming.getMinimumDimmingLevel() : null;
        Double sourceMinDimmingLevel = Objects.nonNull(sourceDimming) ? sourceDimming.getMinimumDimmingLevel() : null;
        if (Objects.isNull(targetMinDimmingLevel) && Objects.nonNull(sourceMinDimmingLevel)) {
            targetDimming = Objects.nonNull(targetDimming) ? targetDimming : new Dimming();
            targetDimming.setMinimumDimmingLevel(sourceMinDimmingLevel);
        }
        // color
        ColorXy targetColor = target.getColorXy();
        ColorXy sourceColor = source.getColorXy();
        if (Objects.isNull(targetColor) && Objects.nonNull(sourceColor)) {
            target.setColorXy(sourceColor);
            targetColor = target.getColorXy();
        }
        // color gamut
        Gamut targetGamut = Objects.nonNull(targetColor) ? targetColor.getGamut() : null;
        Gamut sourceGamut = Objects.nonNull(sourceColor) ? sourceColor.getGamut() : null;
        if (Objects.isNull(targetGamut) && Objects.nonNull(sourceGamut)) {
            targetColor = Objects.nonNull(targetColor) ? targetColor : new ColorXy();
            targetColor.setGamut(sourceGamut);
        }
        // color temperature
        ColorTemperature targetColorTemp = target.getColorTemperature();
        ColorTemperature sourceColorTemp = source.getColorTemperature();
        if (Objects.isNull(targetColorTemp) && Objects.nonNull(sourceColorTemp)) {
            target.setColorTemperature(sourceColorTemp);
            targetColorTemp = target.getColorTemperature();
        }
        // mirek schema
        MirekSchema targetMirekSchema = Objects.nonNull(targetColorTemp) ? targetColorTemp.getMirekSchema() : null;
        MirekSchema sourceMirekSchema = Objects.nonNull(sourceColorTemp) ? sourceColorTemp.getMirekSchema() : null;
        if (Objects.isNull(targetMirekSchema) && Objects.nonNull(sourceMirekSchema)) {
            targetColorTemp = Objects.nonNull(targetColorTemp) ? targetColorTemp : new ColorTemperature();
            targetColorTemp.setMirekSchema(sourceMirekSchema);
        }
        // metadata
        MetaData targetMetaData = target.getMetaData();
        MetaData sourceMetaData = source.getMetaData();
        if (Objects.isNull(targetMetaData) && Objects.nonNull(sourceMetaData)) {
            target.setMetadata(sourceMetaData);
        }
        // alerts
        Alerts targetAlerts = target.getAlerts();
        Alerts sourceAlerts = source.getAlerts();
        if (Objects.isNull(targetAlerts) && Objects.nonNull(sourceAlerts)) {
            target.setAlerts(sourceAlerts);
        }
        // effects
        Effects targetEffects = target.getEffects();
        Effects sourceEffects = source.getEffects();
        if (Objects.isNull(targetEffects) && Objects.nonNull(sourceEffects)) {
            targetEffects = sourceEffects;
            target.setEffects(sourceEffects);
            targetEffects = target.getEffects();
        }
        // effects values
        List<String> targetStatusValues = Objects.nonNull(targetEffects) ? targetEffects.getStatusValues() : null;
        List<String> sourceStatusValues = Objects.nonNull(sourceEffects) ? sourceEffects.getStatusValues() : null;
        if (Objects.isNull(targetStatusValues) && Objects.nonNull(sourceStatusValues)) {
            targetEffects = Objects.nonNull(targetEffects) ? targetEffects : new Effects();
            targetEffects.setStatusValues(sourceStatusValues);
        }
        return target;
    }
}
