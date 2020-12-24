/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.plugwiseha.internal.api.model.DTO;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author B. van Wetten - Initial contribution
 */
@XStreamAlias("point_log")
public class Log extends PlugwiseBaseModel implements PlugwiseComparableDate<Log> {

    private String type;

    private String unit;

    private String measurement;

    @XStreamAlias("measurement_date")
    private ZonedDateTime measurementDate;

    @XStreamAlias("updated_date")
    private ZonedDateTime updatedDate;

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public Optional<String> getMeasurement() {
        return Optional.ofNullable(measurement);
    }

    public Optional<Boolean> getMeasurementAsBoolean() {
        if (measurement != null) {
            switch (measurement.toLowerCase()) {
                case "on":
                    return Optional.of(true);
                case "off":
                    return Optional.of(false);
                default:
                    return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public Optional<Double> getMeasurementAsDouble() {
        try {
            if (measurement != null) {
                return Optional.of(Double.parseDouble(measurement));
            } else {
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public ZonedDateTime getMeasurementDate() {
        return measurementDate;
    }

    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    public int compareDateWith(Log hasMeasurementDate) {
        return this.measurementDate.compareTo(hasMeasurementDate.getMeasurementDate());
    }

    public boolean isOlderThan(Log hasMeasurementDate) {
        return this.compareDateWith(hasMeasurementDate) < 0;
    }

    public boolean isNewerThan(Log hasMeasurementDate) {
        return this.compareDateWith(hasMeasurementDate) > 0;
    }
}
