/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.dto;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.jdt.annotation.Nullable;

/**
 * See {@link DeviceListModel}.
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet DECT
 * @author Christoph Weitkamp - Added channel 'battery_level'
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "hkr")
public class HeatingModel implements BatteryModel {
    public static final BigDecimal TEMP_FACTOR = new BigDecimal("0.5");
    public static final BigDecimal BIG_DECIMAL_TWO = new BigDecimal("2.0");
    public static final BigDecimal TEMP_CELSIUS_MIN = new BigDecimal("8.0");
    public static final BigDecimal TEMP_CELSIUS_MAX = new BigDecimal("28.0");
    public static final BigDecimal TEMP_FRITZ_MIN = new BigDecimal("16.0");
    public static final BigDecimal TEMP_FRITZ_MAX = new BigDecimal("56.0");
    public static final BigDecimal TEMP_FRITZ_OFF = new BigDecimal("253.0");
    public static final BigDecimal TEMP_FRITZ_ON = new BigDecimal("254.0");
    public static final BigDecimal TEMP_FRITZ_UNDEFINED = new BigDecimal("255.0");

    private BigDecimal tist;
    private BigDecimal tsoll;
    private BigDecimal absenk;
    private BigDecimal komfort;
    private BigDecimal lock;
    private BigDecimal devicelock;
    private String errorcode;
    private BigDecimal batterylow;
    private @Nullable BigDecimal windowopenactiv;
    private @Nullable BigDecimal windowopenactiveendtime;
    private @Nullable BigDecimal boostactive;
    private @Nullable BigDecimal boostactiveendtime;
    private BigDecimal battery;
    private NextChangeModel nextchange;
    private BigDecimal summeractive;
    private BigDecimal holidayactive;

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
        if (BigDecimal.ONE.equals(getHolidayactive())) {
            return MODE_VACATION;
        } else if (getNextchange() != null && getNextchange().getEndperiod() != 0) {
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
        } else if (BigDecimal.ONE.equals(getWindowopenactiv())) {
            return MODE_WINDOW_OPEN;
        } else if (komfort != null && komfort.compareTo(tsoll) == 0) {
            return MODE_COMFORT;
        } else if (absenk != null && absenk.compareTo(tsoll) == 0) {
            return MODE_ECO;
        } else if (BigDecimal.ONE.equals(getBoostactive()) || TEMP_FRITZ_MAX.compareTo(tsoll) == 0) {
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

    @Override
    public BigDecimal getBatterylow() {
        return batterylow;
    }

    public void setBatterylow(BigDecimal batterylow) {
        this.batterylow = batterylow;
    }

    public @Nullable BigDecimal getWindowopenactiv() {
        return windowopenactiv;
    }

    public @Nullable BigDecimal getWindowopenactiveendtime() {
        return windowopenactiveendtime;
    }

    public void setWindowopenactiv(BigDecimal windowopenactiv) {
        this.windowopenactiv = windowopenactiv;
    }

    public @Nullable BigDecimal getBoostactive() {
        return boostactive;
    }

    public @Nullable BigDecimal getBoostactiveendtime() {
        return boostactiveendtime;
    }

    @Override
    public BigDecimal getBattery() {
        return battery;
    }

    public void setBattery(BigDecimal battery) {
        this.battery = battery;
    }

    public NextChangeModel getNextchange() {
        return nextchange;
    }

    public void setNextchange(NextChangeModel nextchange) {
        this.nextchange = nextchange;
    }

    public BigDecimal getSummeractive() {
        return summeractive;
    }

    public void setSummeractive(BigDecimal summeractive) {
        this.summeractive = summeractive;
    }

    public BigDecimal getHolidayactive() {
        return holidayactive;
    }

    public void setHolidayactive(BigDecimal holidayactive) {
        this.holidayactive = holidayactive;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[tist=").append(tist).append(",tsoll=").append(tsoll).append(",absenk=")
                .append(absenk).append(",komfort=").append(komfort).append(",lock=").append(lock).append(",devicelock=")
                .append(devicelock).append(",errorcode=").append(errorcode).append(",batterylow=").append(batterylow)
                .append(",windowopenactiv=").append(windowopenactiv).append(",windowopenactiveendtime=")
                .append(windowopenactiveendtime).append(",boostactive=").append(boostactive)
                .append(",boostactiveendtime=").append(boostactiveendtime).append(",battery=").append(battery)
                .append(",nextchange=").append(nextchange).append(",summeractive=").append(summeractive)
                .append(",holidayactive=").append(holidayactive).append("]").toString();
    }

    /**
     * Converts a celsius value to a FRITZ!Box value.
     * Valid celsius values: 8 to 28 °C > 16 to 56,
     * 16 &lt;= 8°C, 17 = 8.5°C...... 56 >= 28°C, 254 = ON, 253 = OFF
     *
     * @param celsiusValue The celsius value to be converted
     * @return The FRITZ!Box value
     */
    public static BigDecimal fromCelsius(BigDecimal celsiusValue) {
        if (celsiusValue == null) {
            return BigDecimal.ZERO;
        } else if (TEMP_CELSIUS_MIN.compareTo(celsiusValue) == 1) {
            return TEMP_FRITZ_MIN;
        } else if (TEMP_CELSIUS_MAX.compareTo(celsiusValue) == -1) {
            return TEMP_FRITZ_MAX;
        }
        return BIG_DECIMAL_TWO.multiply(celsiusValue);
    }

    /**
     * Converts a FRITZ!Box value to a celsius value.
     *
     * @param fritzValue The FRITZ!Box value to be converted
     * @return The celsius value
     */
    public static BigDecimal toCelsius(BigDecimal fritzValue) {
        if (fritzValue == null) {
            return BigDecimal.ZERO;
        } else if (TEMP_FRITZ_ON.compareTo(fritzValue) == 0) {
            return TEMP_CELSIUS_MAX.add(BIG_DECIMAL_TWO);
        } else if (TEMP_FRITZ_OFF.compareTo(fritzValue) == 0) {
            return TEMP_CELSIUS_MIN.subtract(BIG_DECIMAL_TWO);
        }
        return TEMP_FACTOR.multiply(fritzValue);
    }

    /**
     * Normalizes a celsius value.
     * Valid celsius steps: 0.5°C
     *
     * @param celsiusValue The celsius value to be normalized
     * @return The normalized celsius value
     */
    public static BigDecimal normalizeCelsius(BigDecimal celsiusValue) {
        BigDecimal divisor = celsiusValue.divide(TEMP_FACTOR, 0, RoundingMode.HALF_UP);
        return TEMP_FACTOR.multiply(divisor);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class NextChangeModel {
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

        @Override
        public String toString() {
            return new StringBuilder().append("[endperiod=").append(endperiod).append(",tchange=").append(tchange)
                    .append("]").toString();
        }
    }
}
