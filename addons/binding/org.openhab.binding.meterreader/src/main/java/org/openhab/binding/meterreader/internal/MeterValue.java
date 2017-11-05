/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal;

/**
 * Represents one value of the meter device.
 *
 * @author MatthiasS
 *
 */
public class MeterValue {

    private String obis;
    private String value;
    private String unit;

    public MeterValue(String obis, String value, String unit) {
        this.obis = obis;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Gets the values unit.
     *
     * @return the string representation of the values unit - otherwise null.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the value
     *
     * @return the value as String if available - otherwise null.
     */
    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((obis == null) ? 0 : obis.hashCode());
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MeterValue other = (MeterValue) obj;
        if (obis == null) {
            if (other.obis != null) {
                return false;
            }
        } else if (!obis.equals(other.obis)) {
            return false;
        }
        if (unit == null) {
            if (other.unit != null) {
                return false;
            }
        } else if (!unit.equals(other.unit)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MeterValue [obis=" + obis + ", value=" + value + ", unit=" + unit + "]";
    }

    public String getObisCode() {
        return this.obis;
    }

}