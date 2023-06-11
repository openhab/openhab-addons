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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherIllegalPropertyValueException;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@code Sensor} class defines the dto for Smarther API sensor object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Sensor {

    private List<Measure> measures;

    /**
     * Returns the list of measures this sensor takes.
     *
     * @return the measures this sensor takes, may be {@code null}
     */
    public @Nullable List<Measure> getMeasures() {
        return measures;
    }

    /**
     * Returns the measure taken by this sensor at the given index.
     *
     * @param index
     *            the index to get the measure for
     *
     * @return the requested measure, or {@code null} in case of no measure found at given index
     */
    public @Nullable Measure getMeasure(int index) {
        return (measures != null && measures.size() > index) ? measures.get(index) : null;
    }

    /**
     * Returns the overall state of the sensor.
     *
     * @return a {@link State} object representing the overall state of the sensor
     *
     * @throws {@link SmartherIllegalPropertyValueException}
     *             if the sensor internal raw state cannot be mapped to any valid value
     */
    public State toState() throws SmartherIllegalPropertyValueException {
        final Measure measure = getMeasure(0);
        return (measure != null) ? measure.toState() : UnDefType.UNDEF;
    }

    @Override
    public String toString() {
        return String.format("measures=%s", measures);
    }
}
