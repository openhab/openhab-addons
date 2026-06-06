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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BaseCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.handler.MatterBaseThingHandler;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;

/**
 * Abstract base converter for all concentration measurement clusters (CO2, CO, PM1, PM10, PM2.5, Ozone, NO2, etc.).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractConcentrationMeasurementConverter<T extends BaseCluster> extends GenericConverter<T> {

    protected static final String ATTRIBUTE_MEASURED_VALUE = "measuredValue";
    protected static final String ATTRIBUTE_MIN_MEASURED_VALUE = "minMeasuredValue";
    protected static final String ATTRIBUTE_MAX_MEASURED_VALUE = "maxMeasuredValue";
    protected static final String ATTRIBUTE_PEAK_MEASURED_VALUE = "peakMeasuredValue";
    protected static final String ATTRIBUTE_AVERAGE_MEASURED_VALUE = "averageMeasuredValue";
    protected static final String ATTRIBUTE_UNCERTAINTY = "uncertainty";
    protected static final String ATTRIBUTE_MEASUREMENT_UNIT = "measurementUnit";
    protected static final String ATTRIBUTE_MEASUREMENT_MEDIUM = "measurementMedium";
    protected static final String ATTRIBUTE_LEVEL_VALUE = "levelValue";

    private final String measuredValueChannelId;
    private final ChannelTypeUID measuredValueChannelType;
    private final String measuredValueLabel;
    private final String measuredValueDescription;
    private final String levelValueChannelId;
    private final ChannelTypeUID levelValueChannelType;
    private final String peakMeasuredValueChannelId;
    private final ChannelTypeUID peakMeasuredValueChannelType;
    private final String averageMeasuredValueChannelId;
    private final ChannelTypeUID averageMeasuredValueChannelType;

    @Nullable
    private Unit<?> cachedUnit = null;

    public AbstractConcentrationMeasurementConverter(T cluster, MatterBaseThingHandler handler, int endpointNumber,
            String labelPrefix, String measuredValueChannelId, ChannelTypeUID measuredValueChannelType,
            String measuredValueLabel, String measuredValueDescription, String levelValueChannelId,
            ChannelTypeUID levelValueChannelType, String peakMeasuredValueChannelId,
            ChannelTypeUID peakMeasuredValueChannelType, String averageMeasuredValueChannelId,
            ChannelTypeUID averageMeasuredValueChannelType) {
        super(cluster, handler, endpointNumber, labelPrefix);
        this.measuredValueChannelId = measuredValueChannelId;
        this.measuredValueChannelType = measuredValueChannelType;
        this.measuredValueLabel = measuredValueLabel;
        this.measuredValueDescription = measuredValueDescription;
        this.levelValueChannelId = levelValueChannelId;
        this.levelValueChannelType = levelValueChannelType;
        this.peakMeasuredValueChannelId = peakMeasuredValueChannelId;
        this.peakMeasuredValueChannelType = peakMeasuredValueChannelType;
        this.averageMeasuredValueChannelId = averageMeasuredValueChannelId;
        this.averageMeasuredValueChannelType = averageMeasuredValueChannelType;
    }

    @Override
    public Map<Channel, @Nullable StateDescription> createChannels(ChannelGroupUID channelGroupUID) {
        Map<Channel, @Nullable StateDescription> channels = new HashMap<>();

        Channel measuredValueChannel = ChannelBuilder
                .create(new ChannelUID(channelGroupUID, measuredValueChannelId), "Number:Dimensionless")
                .withType(measuredValueChannelType).withLabel(measuredValueLabel)
                .withDescription(measuredValueDescription).build();

        StateDescription stateDescription = createStateDescriptionWithPattern();
        channels.put(measuredValueChannel, stateDescription);

        if (hasLevelIndication()) {
            Channel levelValueChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, levelValueChannelId), CoreItemFactory.NUMBER)
                    .withType(levelValueChannelType).build();
            channels.put(levelValueChannel, null);
        }

        if (hasPeakMeasurement()) {
            Channel peakChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, peakMeasuredValueChannelId), "Number:Dimensionless")
                    .withType(peakMeasuredValueChannelType).build();
            channels.put(peakChannel, null);
        }

        if (hasAverageMeasurement()) {
            Channel averageChannel = ChannelBuilder
                    .create(new ChannelUID(channelGroupUID, averageMeasuredValueChannelId), "Number:Dimensionless")
                    .withType(averageMeasuredValueChannelType).build();
            channels.put(averageChannel, null);
        }

        return channels;
    }

    /**
     * Creates a StateDescription with an appropriate pattern based on the default unit.
     *
     * @return StateDescription with pattern, or null if pattern cannot be determined
     */
    @Nullable
    private StateDescription createStateDescriptionWithPattern() {
        Unit<?> unit = getDefaultUnit();
        String pattern = getPatternForUnit(unit);

        if (pattern != null) {
            return StateDescriptionFragmentBuilder.create().withPattern(pattern).withReadOnly(true).build()
                    .toStateDescription();
        }
        return null;
    }

    /**
     * Gets the display pattern for a given unit.
     *
     * @param unit The unit to get the pattern for
     * @return Pattern string or null if no specific pattern
     */
    @Nullable
    private String getPatternForUnit(Unit<?> unit) {
        if (unit.equals(Units.PARTS_PER_MILLION)) {
            return "%.2f ppm";
        } else if (unit.equals(Units.PARTS_PER_BILLION)) {
            return "%.2f ppb";
        } else if (unit.equals(Units.MICROGRAM_PER_CUBICMETRE)) {
            return "%.1f µg/m³";
        } else if (unit.equals(Units.ONE)) {
            // Dimensionless/unknown - no specific pattern
            return "%.2f";
        }
        return null;
    }

    @Override
    public void onEvent(AttributeChangedMessage message) {
        switch (message.path.attributeName) {
            case ATTRIBUTE_MEASURED_VALUE:
                if (message.value instanceof Number number) {
                    updateState(measuredValueChannelId, toQuantityType(number));
                }
                break;
            case ATTRIBUTE_LEVEL_VALUE:
                if (message.value instanceof Number number) {
                    updateState(levelValueChannelId, new DecimalType(number.intValue()));
                }
                break;
            case ATTRIBUTE_PEAK_MEASURED_VALUE:
                if (message.value instanceof Number number) {
                    updateState(peakMeasuredValueChannelId, toQuantityType(number));
                }
                break;
            case ATTRIBUTE_AVERAGE_MEASURED_VALUE:
                if (message.value instanceof Number number) {
                    updateState(averageMeasuredValueChannelId, toQuantityType(number));
                }
                break;
            case ATTRIBUTE_MEASUREMENT_UNIT:
                // Unit changed - clear cache so it gets recalculated
                cachedUnit = null;
                break;
        }
        super.onEvent(message);
    }

    @Override
    public void initState() {
        Float measuredValue = getFloatAttribute("measuredValue");
        if (measuredValue != null) {
            updateState(measuredValueChannelId, toQuantityType(measuredValue));
        } else {
            updateState(measuredValueChannelId, UnDefType.NULL);
        }

        if (hasLevelIndication()) {
            Object levelValue = getClusterAttribute("levelValue");
            if (levelValue != null) {
                Integer levelInt = getEnumValue(levelValue);
                if (levelInt != null) {
                    updateState(levelValueChannelId, new DecimalType(levelInt));
                } else {
                    updateState(levelValueChannelId, UnDefType.NULL);
                }
            } else {
                updateState(levelValueChannelId, UnDefType.NULL);
            }
        }

        if (hasPeakMeasurement()) {
            Float peakMeasuredValue = getFloatAttribute("peakMeasuredValue");
            if (peakMeasuredValue != null) {
                updateState(peakMeasuredValueChannelId, toQuantityType(peakMeasuredValue));
            } else {
                updateState(peakMeasuredValueChannelId, UnDefType.NULL);
            }
        }

        if (hasAverageMeasurement()) {
            Float averageMeasuredValue = getFloatAttribute("averageMeasuredValue");
            if (averageMeasuredValue != null) {
                updateState(averageMeasuredValueChannelId, toQuantityType(averageMeasuredValue));
            } else {
                updateState(averageMeasuredValueChannelId, UnDefType.NULL);
            }
        }
    }

    /**
     * Converts a numeric value to a QuantityType using the appropriate unit from the device's measurementUnit
     * attribute.
     */
    protected QuantityType<?> toQuantityType(Number value) {
        Unit<?> unit = getUnit();
        return new QuantityType<>(BigDecimal.valueOf(value.doubleValue()), unit);
    }

    /**
     * Gets the appropriate openHAB Unit based on the device's measurementUnit attribute.
     * Caches the unit to avoid repeated reflection calls.
     */
    protected Unit<?> getUnit() {
        Unit<?> unit = cachedUnit;
        if (unit != null) {
            return unit;
        }

        Object measurementUnit = getClusterAttribute("measurementUnit");
        if (measurementUnit == null) {
            unit = getDefaultUnit();
            cachedUnit = unit;
            return unit;
        }

        Integer unitValue = getEnumValue(measurementUnit);
        if (unitValue == null) {
            unit = getDefaultUnit();
            cachedUnit = unit;
            return unit;
        }

        unit = measurementUnitToOpenHABUnit(unitValue);
        cachedUnit = unit;
        return unit;
    }

    /**
     * Maps Matter MeasurementUnitEnum values to openHAB Units.
     * 
     * @param unitValue The enum value from MeasurementUnitEnum
     * @return The corresponding openHAB Unit
     */
    protected Unit<?> measurementUnitToOpenHABUnit(Integer unitValue) {
        return switch (unitValue) {
            case 0 -> Units.PARTS_PER_MILLION;
            case 4 -> Units.MICROGRAM_PER_CUBICMETRE;
            case 1, 2, 3, 5, 6, 7 -> Units.ONE; // PPB, PPT, MGM3, NGM3, PM3, BQM3
            default -> getDefaultUnit();
        };
    }

    /**
     * Returns the default unit for this cluster type.
     */
    protected abstract Unit<?> getDefaultUnit();

    /**
     * Checks if the device supports the LevelIndication feature.
     */
    protected abstract boolean hasLevelIndication();

    /**
     * Checks if the device supports the PeakMeasurement feature.
     */
    protected abstract boolean hasPeakMeasurement();

    /**
     * Checks if the device supports the AverageMeasurement feature.
     */
    protected abstract boolean hasAverageMeasurement();

    /**
     * Helper method to get a Float attribute from the cluster.
     */
    @Nullable
    protected Float getFloatAttribute(String attributeName) {
        Object value = getClusterAttribute(attributeName);
        return value instanceof Float f ? f : null;
    }

    /**
     * Helper method to get any attribute from the cluster.
     */
    @Nullable
    protected Object getClusterAttribute(String attributeName) {
        try {
            var field = initializingCluster.getClass().getField(attributeName);
            return field.get(initializingCluster);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.debug("Could not access attribute {}: {}", attributeName, e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the integer value from a Matter enum object (LevelValueEnum, MeasurementUnitEnum, etc.).
     * 
     */
    @Nullable
    protected Integer getEnumValue(Object enumValue) {
        try {
            var method = enumValue.getClass().getMethod("getValue");
            Object result = method.invoke(enumValue);
            return result instanceof Integer i ? i : null;
        } catch (Exception e) {
            logger.debug("Could not extract enum value: {}", e.getMessage());
            return null;
        }
    }
}
