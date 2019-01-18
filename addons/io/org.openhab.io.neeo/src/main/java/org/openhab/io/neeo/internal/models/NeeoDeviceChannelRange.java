/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.neeo.internal.models;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * The model representing a Neeo Channel Range used for sliders to specify the min/max and unit (serialize/deserialize
 * json use only).
 *
 * Note that the unit is just a label that is put on the slider itself for the user
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceChannelRange {
    /** The range unit representing a number */
    private static final String UNIT_NUMBER = "";

    /** The range unit representing a percentage */
    private static final String UNIT_PERCENT = "%";

    /**
     * The default range with a minimum of 0, maximum of 100 and a unit of {@link #UNIT_PERCENT}
     */
    static final NeeoDeviceChannelRange DEFAULT = new NeeoDeviceChannelRange(0, 100, UNIT_PERCENT);

    /** The minimum value for the range */
    private final int minValue;

    /** The maximum value for the range */
    private final int maxValue;

    /** The unit of the range */
    private final String unit;

    /**
     * Create the channel range from the given values
     *
     * @param minValue the miminmum value
     * @param maxValue the maximum value
     * @param unit a unit for the range. Defaults to {@link #UNIT_NUMBER} if null or empty
     */
    public NeeoDeviceChannelRange(int minValue, int maxValue, String unit) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException(
                    "maxValue (" + maxValue + ") is smaller than minValue (" + minValue + ")");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = StringUtils.isEmpty(unit) ? UNIT_NUMBER : unit;
    }

    /**
     * Returns the minimum value for the range
     *
     * @return the minimum value for the range
     */
    public int getMinValue() {
        return minValue;
    }

    /**
     * Returns the maximum value for the range
     *
     * @return the maximum value for the range
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Returns the range unit
     *
     * @return a non-null, non-empty range unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Helper function to create a {@link NeeoDeviceChannelRange} from a given {@link Item}
     *
     * @param item a non-null item
     * @return a non-null {@link NeeoDeviceChannelRange} representing the {@link Item}
     */
    public static NeeoDeviceChannelRange from(Item item) {
        Objects.requireNonNull(item, "item cannot be null");

        final boolean supportsPercent = item.getAcceptedDataTypes().contains(PercentType.class);

        final StateDescription sd = item.getStateDescription();
        int min = 0, max = 100;

        if (sd != null && sd.getMinimum() != null) {
            min = sd.getMinimum().intValue();
        }

        if (sd != null && sd.getMaximum() != null) {
            max = sd.getMaximum().intValue();
        }

        if (max < min) {
            final int tmp = max;
            max = min;
            min = tmp;
        }

        return new NeeoDeviceChannelRange(min, max, supportsPercent ? UNIT_PERCENT : UNIT_NUMBER);
    }

    @Override
    public String toString() {
        return "NeeoDeviceChannelRange [minValue=" + minValue + ", maxValue=" + maxValue + ", unit=" + unit + "]";
    }
}
