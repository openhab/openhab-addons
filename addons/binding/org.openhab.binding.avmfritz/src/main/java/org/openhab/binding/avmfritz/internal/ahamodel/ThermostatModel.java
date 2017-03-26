/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
 *
 * @author Robert Bausdorf
 *
 *
 */
@XmlRootElement(name = "hkr")
@XmlType(propOrder = { "tist", "tsoll", "absenk", "komfort" })
public class ThermostatModel {
    public static final BigDecimal TEMP_FACTOR = new BigDecimal("0.5");
    public static final BigDecimal TSOLL_MAX_VALUE = new BigDecimal("56");
    public static final BigDecimal TSOLL_MIN_VALUE = new BigDecimal("16");
    public static final BigDecimal TSOLL_OFF_VALUE = new BigDecimal("253");
    public static final BigDecimal CELSIUS_MAX_VALUE = new BigDecimal("28");
    public static final BigDecimal CELSIUS_MIN_VALUE = new BigDecimal("8");

    private BigDecimal tist;

    private BigDecimal tsoll;

    private BigDecimal absenk;

    private BigDecimal komfort;

    public BigDecimal getTist() {
        return tist;
    }

    public void setTist(BigDecimal tist) {
        this.tist = tist;
    }

    public BigDecimal getTsoll() {
        return tsoll;
    }

    public void setTsoll(BigDecimal tsoll) {
        this.tsoll = tsoll;
    }

    public BigDecimal getAbsenk() {
        return absenk;
    }

    public void setAbsenk(BigDecimal absenk) {
        this.absenk = absenk;
    }

    public BigDecimal getKomfort() {
        return komfort;
    }

    public void setKomfort(BigDecimal komfort) {
        this.komfort = komfort;
    }

    public static BigDecimal CovertCelsiusToSetTemperture(BigDecimal celsius) {
        if (celsius.compareTo(CELSIUS_MAX_VALUE) == 1) {
            return TSOLL_MAX_VALUE;
        } else if (celsius.compareTo(CELSIUS_MIN_VALUE) == -1) {
            return TSOLL_MIN_VALUE;
        } else {
            return celsius.divide(TEMP_FACTOR);
        }
    }

    public static BigDecimal CovertSetTempertureToCelsius(BigDecimal setTemperature) {
        return setTemperature.multiply(TEMP_FACTOR);
    }

    public BigDecimal getSetTemperature() {
        return tsoll != null ? tsoll : TSOLL_MIN_VALUE;
    }

    public BigDecimal getTargetTemperatureCelsius() {
        if (tsoll == null || this.tsoll.equals(TSOLL_OFF_VALUE)) {
            return CELSIUS_MIN_VALUE;
        }
        return CovertSetTempertureToCelsius(tsoll);
    }

    public BigDecimal getComfortTemperatureCelsius() {
        return komfort != null ? CovertSetTempertureToCelsius(komfort) : CELSIUS_MIN_VALUE;
    }

    public BigDecimal getSavingTemperatureCelsius() {
        return absenk != null ? CovertSetTempertureToCelsius(absenk) : CELSIUS_MIN_VALUE;
    }

    public boolean isClosed() {
        return tsoll != null ? tsoll.equals(TSOLL_OFF_VALUE) : false;

    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tsoll", this.getTsoll()).append("tist", this.getTist())
                .append("komfort", this.getKomfort()).append("absenk", this.getAbsenk()).toString();
    }

}
