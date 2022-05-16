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
package org.openhab.binding.boschspexor.internal.api.model;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generic Type of Sensor values
 *
 * @author Marc Fischer - Initial contribution *
 * @param <T>
 */
@NonNullByDefault
public class SensorValue<T> {
    private static final String UNKNOWN = "-";
    private static final String N_A = "N/A";
    public static final String TYPE_AIR_QUALITY_LEVEL = "AirQualityLevel";
    public static final String TYPE_PASSIVE_INFRARED = "PassiveInfrared";
    public static final String TYPE_GAS = "Gas";
    public static final String TYPE_LIGHT = "Light";
    public static final String TYPE_ACCELERATION = "Acceleration";
    public static final String TYPE_PRESSURE = "Pressure";
    public static final String TYPE_FIRE = "Fire";
    public static final String TYPE_MICROPHONE = "Microphone";
    public static final String TYPE_HUMIDITY = "Humidity";
    public static final String TYPE_TEMPERATURE = "Temperature";
    public static final String TYPE_AIR_QUALITY = "AirQuality";

    private Optional<T> value = Optional.empty();
    private String unit = N_A;
    private Optional<T> minValue = Optional.empty();
    private Optional<T> maxValue = Optional.empty();
    private String timestamp = UNKNOWN;

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value = Optional.ofNullable(value);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean hasMinValue() {
        return minValue.isPresent();
    }

    public T getMinValue() {
        return minValue.get();
    }

    public void setMinValue(T minValue) {
        this.minValue = Optional.ofNullable(minValue);
    }

    public boolean hasMaxValue() {
        return maxValue.isPresent();
    }

    public T getMaxValue() {
        return maxValue.get();
    }

    public void setMaxValue(T maxValue) {
        this.maxValue = Optional.ofNullable(maxValue);
    }

    public boolean hasTimestamp() {
        return !UNKNOWN.equalsIgnoreCase(timestamp);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
