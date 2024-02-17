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
package org.openhab.binding.hue.internal.api.dto.clip2.helper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.api.dto.clip2.Alerts;
import org.openhab.binding.hue.internal.api.dto.clip2.ColorTemperature;
import org.openhab.binding.hue.internal.api.dto.clip2.ColorXy;
import org.openhab.binding.hue.internal.api.dto.clip2.Dimming;
import org.openhab.binding.hue.internal.api.dto.clip2.Effects;
import org.openhab.binding.hue.internal.api.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.api.dto.clip2.MirekSchema;
import org.openhab.binding.hue.internal.api.dto.clip2.OnState;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.TimedEffects;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ActionType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.EffectType;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
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

    private static final Set<ResourceType> LIGHT_TYPES = Set.of(ResourceType.LIGHT, ResourceType.GROUPED_LIGHT);

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
     * @param command the new state command should be a {@code QuantityType<Temperature>} (but it can also handle
     *            {@code DecimalType}).
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
     * Setter for fixed or timed effect field:
     * Use the given command value to set the target fixed or timed effects resource DTO value based on the attributes
     * of the source resource (if any).
     *
     * @param target the target resource.
     * @param command the new state command should be a StringType.
     * @param source another resource containing the allowed effect action values.
     *
     * @return the target resource.
     */
    public static Resource setEffect(Resource target, Command command, @Nullable Resource source) {
        if ((command instanceof StringType) && Objects.nonNull(source)) {
            EffectType commandEffectType = EffectType.of(((StringType) command).toString());
            Effects sourceFixedEffects = source.getFixedEffects();
            if (Objects.nonNull(sourceFixedEffects) && sourceFixedEffects.allows(commandEffectType)) {
                target.setFixedEffects(new Effects().setEffect(commandEffectType));
            }
            TimedEffects sourceTimedEffects = source.getTimedEffects();
            if (Objects.nonNull(sourceTimedEffects) && sourceTimedEffects.allows(commandEffectType)) {
                Duration duration = sourceTimedEffects.getDuration();
                target.setTimedEffects(((TimedEffects) new TimedEffects().setEffect(commandEffectType))
                        .setDuration(Objects.nonNull(duration) ? duration : TimedEffects.DEFAULT_DURATION));
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
        if (Objects.nonNull(targetDimming)) {
            Double sourceMinDimLevel = Objects.isNull(sourceDimming) ? null : sourceDimming.getMinimumDimmingLevel();
            if (Objects.nonNull(sourceMinDimLevel)) {
                targetDimming.setMinimumDimmingLevel(sourceMinDimLevel);
            }
        }

        // color
        ColorXy targetColor = target.getColorXy();
        ColorXy sourceColor = source.getColorXy();
        if (Objects.isNull(targetColor) && Objects.nonNull(sourceColor)) {
            target.setColorXy(sourceColor);
            targetColor = target.getColorXy();
        }

        // color gamut
        Gamut sourceGamut = Objects.isNull(sourceColor) ? null : sourceColor.getGamut();
        if (Objects.nonNull(targetColor) && Objects.nonNull(sourceGamut)) {
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
        if (Objects.nonNull(targetColorTemp)) {
            MirekSchema sourceMirekSchema = Objects.isNull(sourceColorTemp) ? null : sourceColorTemp.getMirekSchema();
            if (Objects.nonNull(sourceMirekSchema)) {
                targetColorTemp.setMirekSchema(sourceMirekSchema);
            }
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

        // fixed effects
        Effects targetFixedEffects = target.getFixedEffects();
        Effects sourceFixedEffects = source.getFixedEffects();
        if (Objects.isNull(targetFixedEffects) && Objects.nonNull(sourceFixedEffects)) {
            target.setFixedEffects(sourceFixedEffects);
            targetFixedEffects = target.getFixedEffects();
        }

        // fixed effects allowed values
        if (Objects.nonNull(targetFixedEffects)) {
            List<String> values = Objects.isNull(sourceFixedEffects) ? List.of() : sourceFixedEffects.getStatusValues();
            if (!values.isEmpty()) {
                targetFixedEffects.setStatusValues(values);
            }
        }

        // timed effects
        TimedEffects targetTimedEffects = target.getTimedEffects();
        TimedEffects sourceTimedEffects = source.getTimedEffects();
        if (Objects.isNull(targetTimedEffects) && Objects.nonNull(sourceTimedEffects)) {
            target.setTimedEffects(sourceTimedEffects);
            targetTimedEffects = target.getTimedEffects();
        }

        // timed effects allowed values and duration
        if (Objects.nonNull(targetTimedEffects)) {
            List<String> values = Objects.isNull(sourceTimedEffects) ? List.of() : sourceTimedEffects.getStatusValues();
            if (!values.isEmpty()) {
                targetTimedEffects.setStatusValues(values);
            }
            Duration duration = Objects.isNull(sourceTimedEffects) ? null : sourceTimedEffects.getDuration();
            if (Objects.nonNull(duration)) {
                targetTimedEffects.setDuration(duration);
            }
        }
        return target;
    }

    /**
     * Merge on/dimming/color fields from light and grouped light resources.
     * Subsequent resources will be merged into the first one.
     * Full state resources are not supported by this method.
     */
    public static Collection<Resource> mergeLightResources(Collection<Resource> resources) {
        Map<String, Resource> resourceIndex = new HashMap<>();
        Iterator<Resource> iterator = resources.iterator();
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            String id = resource.getId();

            if (resource.hasFullState()) {
                throw new IllegalStateException("Resource " + id + " has full state, this is not expected");
            }

            Resource indexedResource = resourceIndex.get(id);
            if (indexedResource == null) {
                resourceIndex.put(id, resource);
                continue;
            }

            if (!LIGHT_TYPES.contains(resource.getType()) || !resource.hasHSBField()) {
                continue;
            }

            OnState onState = resource.getOnState();
            if (onState != null) {
                indexedResource.setOnState(onState);
                resource.setOnState(null);
            }
            Dimming dimming = resource.getDimming();
            if (dimming != null) {
                indexedResource.setDimming(dimming);
                resource.setDimming(null);
            }
            ColorXy colorXy = resource.getColorXy();
            if (colorXy != null) {
                indexedResource.setColorXy(colorXy);
                resource.setColorXy(null);
            }

            if (!resource.hasAnyRelevantField()) {
                iterator.remove();
            }
        }

        return resources;
    }
}
