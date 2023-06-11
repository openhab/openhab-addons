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
package org.openhab.binding.plugwiseha.internal.api.model.dto;

import java.time.ZonedDateTime;
import java.util.Optional;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author B. van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
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

    public Optional<String> getMeasurementUnit() {
        return Optional.ofNullable(unit);
    }

    public ZonedDateTime getMeasurementDate() {
        return measurementDate;
    }

    public ZonedDateTime getUpdatedDate() {
        return updatedDate;
    }

    @Override
    public int compareDateWith(Log compareTo) {
        if (compareTo == null) {
            return -1;
        }
        ZonedDateTime compareToDate = compareTo.getMeasurementDate();
        ZonedDateTime compareFromDate = this.getMeasurementDate();
        if (compareFromDate == null) {
            return -1;
        } else if (compareToDate == null) {
            return 1;
        } else {
            return compareFromDate.compareTo(compareToDate);
        }
    }

    @Override
    public boolean isNewerThan(Log hasModifiedDate) {
        return compareDateWith(hasModifiedDate) > 0;
    }

    @Override
    public boolean isOlderThan(Log hasModifiedDate) {
        return compareDateWith(hasModifiedDate) < 0;
    }
}
