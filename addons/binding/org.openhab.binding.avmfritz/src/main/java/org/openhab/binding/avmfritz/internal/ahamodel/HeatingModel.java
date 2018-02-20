/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.ahamodel;

import static org.openhab.binding.avmfritz.BindingConstants.*;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * See {@link DevicelistModel}.
 * 
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * 
 */
@XmlRootElement(name = "hkr")
@XmlType(propOrder = { "tist", "tsoll", "absenk", "komfort", "lock", "devicelock", "errorcode", "batterylow",
        "nextchange" })
public class HeatingModel {
    public static final BigDecimal TEMP_FACTOR = new BigDecimal("0.5");
    public static final BigDecimal TEMP_CELSIUS_MIN = new BigDecimal("8.0");
    public static final BigDecimal TEMP_CELSIUS_MAX = new BigDecimal("28.0");
    public static final BigDecimal TEMP_FRITZ_MIN = new BigDecimal("16.0");
    public static final BigDecimal TEMP_FRITZ_MAX = new BigDecimal("56.0");
    public static final BigDecimal TEMP_FRITZ_OFF = new BigDecimal("253.0");
    public static final BigDecimal TEMP_FRITZ_ON = new BigDecimal("254.0");
    public static final BigDecimal TEMP_FRITZ_UNDEFINED = new BigDecimal("255.0");
    public static final BigDecimal BATTERY_OFF = BigDecimal.ZERO;
    public static final BigDecimal BATTERY_ON = BigDecimal.ONE;

    private BigDecimal tist;
    private BigDecimal tsoll;
    private BigDecimal absenk;
    private BigDecimal komfort;
    private BigDecimal lock;
    private BigDecimal devicelock;
    private String errorcode;
    private BigDecimal batterylow;
    private Nextchange nextchange;

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

    public BigDecimal getKomfort() {
        return komfort;
    }

    public void setKomfort(BigDecimal komfort) {
        this.komfort = komfort;
    }

    public BigDecimal getAbsenk() {
        return absenk;
    }

    public void setAbsenk(BigDecimal absenk) {
        this.absenk = absenk;
    }

    public String getMode() {
        if (getNextchange() != null && getNextchange().getEndperiod() != 0) {
            return MODE_AUTO;
        } else {
            return MODE_MANUAL;
        }
    }

    public String getRadiatorMode() {
        if (tsoll == null) {
            return MODE_UNKNOWN;
        } else if (TEMP_FRITZ_ON.compareTo(tsoll) == 0) {
            return MODE_ON;
        } else if (TEMP_FRITZ_OFF.compareTo(tsoll) == 0) {
            return MODE_OFF;
        } else if (tsoll.compareTo(komfort) == 0) {
            return MODE_COMFORT;
        } else if (tsoll.compareTo(absenk) == 0) {
            return MODE_ECO;
        } else if (TEMP_FRITZ_MAX.compareTo(tsoll) == 0) {
            return MODE_BOOST;
        } else {
            return MODE_ON;
        }
    }

    public BigDecimal getLock() {
        return lock;
    }

    public void setLock(BigDecimal lock) {
        this.lock = lock;
    }

    public BigDecimal getDevicelock() {
        return devicelock;
    }

    public void setDevicelock(BigDecimal devicelock) {
        this.devicelock = devicelock;
    }

    public String getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    public BigDecimal getBatterylow() {
        return batterylow;
    }

    public void setBatterylow(BigDecimal batterylow) {
        this.batterylow = batterylow;
    }

    public Nextchange getNextchange() {
        return nextchange;
    }

    public void setNextchange(Nextchange nextchange) {
        this.nextchange = nextchange;
    }

    public String toString() {
        return new ToStringBuilder(this).append("tist", getTist()).append("tsoll", getTsoll())
                .append("absenk", getAbsenk()).append("komfort", getKomfort()).append("lock", getLock())
                .append("devicelock", getDevicelock()).append("errorcode", getErrorcode())
                .append("batterylow", getBatterylow()).append("nextchange", getNextchange()).toString();
    }

    public static BigDecimal fromCelsius(BigDecimal celsiusValue) {
        if (celsiusValue == null) {
            return BigDecimal.ZERO;
        } else if (TEMP_CELSIUS_MIN.compareTo(celsiusValue) == 1) {
            return TEMP_FRITZ_MIN;
        } else if (TEMP_CELSIUS_MAX.compareTo(celsiusValue) == -1) {
            return TEMP_FRITZ_MAX;
        }
        return celsiusValue.divide(TEMP_FACTOR);
    }

    public static BigDecimal toCelsius(BigDecimal fritzValue) {
        if (fritzValue == null) {
            return BigDecimal.ZERO;
        } else if (TEMP_FRITZ_ON.compareTo(fritzValue) == 0) {
            return TEMP_CELSIUS_MAX.add(new BigDecimal("2.0"));
        } else if (TEMP_FRITZ_OFF.compareTo(fritzValue) == 0) {
            return TEMP_CELSIUS_MIN.subtract(new BigDecimal("2.0"));
        }
        return TEMP_FACTOR.multiply(fritzValue);
    }

    @XmlType(name = "", propOrder = { "endperiod", "tchange" })
    public static class Nextchange {

        private int endperiod;
        private BigDecimal tchange;

        public int getEndperiod() {
            return endperiod;
        }

        public void setEndperiod(int endperiod) {
            this.endperiod = endperiod;
        }

        public BigDecimal getTchange() {
            return tchange;
        }

        public void setTchange(BigDecimal tchange) {
            this.tchange = tchange;
        }

        public String toString() {
            return new ToStringBuilder(this).append("endperiod", getEndperiod()).append("tchange", getTchange())
                    .toString();
        }

    }
}
