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
package org.openhab.binding.sunsa.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PercentType;

/**
 * Utility class for mapping Sunsa device raw positions to local positions.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class PositionAdapters {
    public static final int POSITION_MIN_VALUE = -100;
    public static final int POSITION_MAX_VALUE = 100;

    private PositionAdapters() {
        /* prevent instantiation */ }

    public static interface PositionAdapter {
        /**
         * Return the raw position at the given interpolation factor (value between 0.0 to 1.0).
         */
        public int getRawPosition(final float interpolationFactor);

        /**
         * Return the local position [0, 100] at the given raw position.
         */
        public int getLocalPosition(final int rawPosition);
    }

    public static PositionAdapter configurablePositionAdapter(final int rangeStart, final int rangeEnd) {
        return new ConfigurablePositionAdapter(rangeStart, rangeEnd);
    }

    /**
     * Returns a position that's within [{@value #POSITION_MIN_VALUE}, {@value #POSITION_MAX_VALUE}].
     */
    public static int clampRawPosition(final int position) {
        return Math.max(Math.min(POSITION_MAX_VALUE, position), POSITION_MIN_VALUE);
    }

    private static class ConfigurablePositionAdapter implements PositionAdapter {
        private final int rangeStart;
        private final int rangeEnd;

        public ConfigurablePositionAdapter(int rangeStart, int rangeEnd) {
            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
        }

        @Override
        public int getRawPosition(float interpolationFactor) {
            final int range = rangeEnd - rangeStart;
            return Math.round(rangeStart + (range * interpolationFactor));
        }

        @Override
        public int getLocalPosition(int rawPosition) {
            final int range = rangeEnd - rangeStart;
            float interpolationFactor = (clampPosition(rawPosition) - rangeStart) / (range * 1.0f);
            return Math.round(interpolationFactor * PercentType.HUNDRED.intValue());
        }

        /**
         * Clamp the value between start and end range since this adapter
         * doesn't know how to handle those values.
         */
        private int clampPosition(final int position) {
            if (rangeEnd > rangeStart) {
                return Math.max(Math.min(rangeEnd, position), rangeStart);
            } else {
                return Math.min(Math.max(rangeEnd, position), rangeStart);
            }
        }
    }
}
