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
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 * 
 */
@XmlRootElement(name = "hkr")
@XmlType(propOrder = { "tist", "tsoll", "absenk", "komfort", "lock", "devicelock", "errorcode", "batterylow",
        "nextchange" })
public class HeatingModel {
    public static final BigDecimal TEMP_FACTOR = new BigDecimal("0.5");
    public static final BigDecimal TEMP_MIN = new BigDecimal("8.0");
    public static final BigDecimal TEMP_MAX = new BigDecimal("28.0");
    public static final BigDecimal TEMP_OFF = new BigDecimal("253.0");
    public static final BigDecimal TEMP_ON = new BigDecimal("254.0");
    public static final BigDecimal BATTERY_OFF = BigDecimal.ZERO;
    public static final BigDecimal BATTERY_ON = BigDecimal.ONE;

    protected BigDecimal tist;
    protected BigDecimal tsoll;
    protected BigDecimal absenk;
    protected BigDecimal komfort;
    protected BigDecimal lock;
    protected BigDecimal devicelock;
    protected String errorcode;
    protected BigDecimal batterylow;
    protected Nextchange nextchange;

    public BigDecimal getTist() {
        return tist != null ? tist.multiply(TEMP_FACTOR) : BigDecimal.ZERO;
    }

    public void setTist(BigDecimal tist) {
        this.tist = tist;
    }

    public BigDecimal getTsoll() {
        if (tsoll == null) {
            return BigDecimal.ZERO;
        } else if (tsoll.compareTo(TEMP_ON) == 0) {
            return TEMP_MAX.add(new BigDecimal("2.0"));
        } else if (tsoll.compareTo(TEMP_OFF) == 0) {
            return TEMP_MIN.subtract(new BigDecimal("2.0"));
        } else {
            return tsoll.multiply(TEMP_FACTOR);
        }
    }

    public void setTsoll(BigDecimal tsoll) {
        this.tsoll = tsoll;
    }

    public BigDecimal getKomfort() {
        return komfort != null ? komfort.multiply(TEMP_FACTOR) : BigDecimal.ZERO;
    }

    public void setKomfort(BigDecimal komfort) {
        this.komfort = komfort;
    }

    public BigDecimal getAbsenk() {
        return absenk != null ? absenk.multiply(TEMP_FACTOR) : BigDecimal.ZERO;
    }

    public void setAbsenk(BigDecimal absenk) {
        this.absenk = absenk;
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
                .append("errorcode", getErrorcode()).append("batterylow", getBatterylow())
                .append("nextchange", getNextchange()).toString();
    }

    @XmlType(name = "", propOrder = { "endperiod", "tchange" })
    public static class Nextchange {

        protected int endperiod;
        protected BigDecimal tchange;

        public int getEndperiod() {
            return endperiod;
        }

        public void setEndperiod(int endperiod) {
            this.endperiod = endperiod;
        }

        public BigDecimal getTchange() {
            return tchange != null ? tchange.multiply(TEMP_FACTOR) : BigDecimal.ZERO;
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
