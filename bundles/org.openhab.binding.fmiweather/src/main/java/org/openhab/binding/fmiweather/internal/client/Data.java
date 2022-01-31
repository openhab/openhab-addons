/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather.internal.client;

import java.math.BigDecimal;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple class for numeric holding data
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class Data {

    /**
     * Array of timestamps, as epoch seconds
     */
    public final long[] timestampsEpochSecs;

    /**
     * Array of values, some of which may be null when value is not present.
     */
    public final @Nullable BigDecimal[] values;

    /**
     *
     * @param timestampsEpochSecs
     * @param values
     * @throws IllegalArgumentException if length of timestampsEpochSecs and values do not match
     */
    public Data(long[] timestampsEpochSecs, BigDecimal[] values) {
        if (timestampsEpochSecs.length != values.length) {
            throw new IllegalArgumentException("length of arguments do not match");
        }
        this.timestampsEpochSecs = timestampsEpochSecs;
        this.values = values;
    }

    @Override
    public String toString() {
        return new StringBuilder("ResponseDataValues(timestampsEpochSecs=").append(Arrays.toString(timestampsEpochSecs))
                .append(", values=").append(Arrays.deepToString(values)).append(")").toString();
    }
}
