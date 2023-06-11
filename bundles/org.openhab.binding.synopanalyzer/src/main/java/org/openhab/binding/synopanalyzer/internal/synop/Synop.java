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
package org.openhab.binding.synopanalyzer.internal.synop;

import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.Units;

/**
 * The {@link Synop} is the ancestor common class for analyzing
 * Synop messages
 *
 * @author Jonarzz - Initial contribution
 */
@NonNullByDefault
public abstract class Synop {
    protected static final int INITIAL_VALUE = -1000;
    private static final char PLUS_SIGN_TEMPERATURE = '0';
    private static final char MINUS_SIGN_TEMPERATURE = '1';

    /*
     * WS - WIND SPEED
     */
    private static final int WS_WILDTYPE_IN_MPS = 0;
    private static final int WS_ANEMOMETER_IN_MPS = 1;

    /*
     * HV - HORIZONTAL VISIBILITY [IN KILOMETERS]
     * VALUES FROM "00" TO "50" AND FROM "56" TO "99"
     * 00 MEANS HV = BELOW 0,1
     * DECIMAL SCOPE MEANS HV = XX / 10
     * UNIT SCOPE MEANS HV = XX - 50
     * 89 MEANS HV = OVER 70
     * 90-99 ROUGHLY NUMBERING :
     * 90 - < 0,05 km
     * 91 >= 0,05 < 0,2 km
     * 92 >= 0,2 < 0,5 km
     * 93 >= 0,5 < 1,0 km
     * 94 >= 1,0 < 2,0 km
     * 95 >= 2,0 < 4,0 km
     * 96 >= 4,0 < 10,0 km
     * 97 >= 10,0 < 20,0 km
     * 98 >= 20,0 < 50,0 km
     * 99 - > 50 km
     * HP - high precision
     */
    private static final int HV_LESS_THAN_1_LIMIT = 10;
    private static final int HV_LESS_THAN_10_LIMIT = 60;
    private static final int HV_LESS_THAN_50_LIMIT = 84;
    private static final int HV_LESS_THAN_1_HP_LIMIT = 93;
    private static final int HV_LESS_THAN_10_HP_LIMIT = 96;
    private static final int HV_LESS_THAN_50_HP_LIMIT = 98;

    public static enum HorizontalVisibility {
        UNDEFINED,
        LESS_THAN_1,
        LESS_THAN_10,
        LESS_THAN_50,
        MORE_THAN_50
    }

    private static final int VALID_STRING_LENGTH = 5;

    protected final List<String> stringArray;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int windIndicator;

    private HorizontalVisibility horizontalVisibility = HorizontalVisibility.UNDEFINED;
    private float temperature;

    private int octa;
    private int windDirection;
    private int windSpeed;
    private float pressure;

    protected int horizontalVisibilityInt = INITIAL_VALUE;
    protected @Nullable String temperatureString;
    protected @Nullable String windString;
    protected @Nullable String pressureString;

    public Synop(List<String> stringArray) {
        this.stringArray = stringArray;

        setDateHourAndWindIndicator();
        setHorizontalVisibility();
        setTemperature();
        setWindAndOvercast();
        setPressure();
    }

    protected abstract void setTemperatureString();

    protected abstract void setHorizontalVisibilityInt();

    protected abstract void setPressureString();

    protected abstract void setWindString();

    private void setDateHourAndWindIndicator() {
        String dayHourAndWindIndicator = "";

        if (this instanceof SynopLand && stringArray.size() > 1) {
            dayHourAndWindIndicator = stringArray.get(1);
        } else if (stringArray.size() > 2) {
            dayHourAndWindIndicator = stringArray.get(2);
        }

        if (!isValidString(dayHourAndWindIndicator)) {
            return;
        }

        setHourOfObservation(dayHourAndWindIndicator);
        setWindIndicator(dayHourAndWindIndicator);
    }

    private void setHourOfObservation(String str) {
        try {
            hour = Integer.parseInt(str.substring(2, 4));
        } catch (NumberFormatException e) {
            hour = INITIAL_VALUE;
        }
        try {
            day = Integer.parseInt(str.substring(0, 2));
        } catch (NumberFormatException e) {
            day = INITIAL_VALUE;
        }
    }

    private void setWindIndicator(String str) {
        try {
            windIndicator = Character.getNumericValue(str.charAt(4));
        } catch (NumberFormatException e) {
            windIndicator = INITIAL_VALUE;
        }
    }

    private void setHorizontalVisibility() {
        setHorizontalVisibilityInt();
        if (horizontalVisibilityInt != INITIAL_VALUE) {
            if (horizontalVisibilityInt < HV_LESS_THAN_1_LIMIT || horizontalVisibilityInt < HV_LESS_THAN_1_HP_LIMIT) {
                horizontalVisibility = HorizontalVisibility.LESS_THAN_1;
            } else if (horizontalVisibilityInt < HV_LESS_THAN_10_LIMIT
                    || horizontalVisibilityInt < HV_LESS_THAN_10_HP_LIMIT) {
                horizontalVisibility = HorizontalVisibility.LESS_THAN_10;
            } else if (horizontalVisibilityInt < HV_LESS_THAN_50_LIMIT
                    || horizontalVisibilityInt < HV_LESS_THAN_50_HP_LIMIT) {
                horizontalVisibility = HorizontalVisibility.LESS_THAN_50;
            } else {
                horizontalVisibility = HorizontalVisibility.MORE_THAN_50;
            }
        } else {
            horizontalVisibility = HorizontalVisibility.UNDEFINED;
        }
    }

    private void setTemperature() {
        setTemperatureString();
        temperature = INITIAL_VALUE;
        String temperatureString = this.temperatureString;
        if (temperatureString != null) {
            char firstChar = temperatureString.charAt(0);
            try {
                float temp = Float.parseFloat(temperatureString.substring(1, 4)) / 10;
                temperature = firstChar == PLUS_SIGN_TEMPERATURE ? temp
                        : firstChar == MINUS_SIGN_TEMPERATURE ? -temp : INITIAL_VALUE;
            } catch (NumberFormatException ignore) {
            }
        }
    }

    private void setWindAndOvercast() {
        setWindString();
        String localWind = windString;
        if (localWind != null) {
            String gustyFlag = localWind.substring(0, 2);
            if ("00".equals(gustyFlag)) {
                setWindSpeed(true);
            } else {
                setOcta();
                setWindDirection();
                setWindSpeed(false);
            }
        } else {
            windDirection = INITIAL_VALUE;
            windSpeed = INITIAL_VALUE;
        }
    }

    private void setOcta() {
        String localWind = windString;
        if (localWind != null) {
            octa = Character.getNumericValue(localWind.charAt(0));
        } else {
            octa = -1;
        }
    }

    private void setWindDirection() {
        String localWind = windString;
        if (localWind != null) {
            String windDirectionString = localWind.substring(1, 3);

            if ("99".equals(windDirectionString) || "||".equals(windDirectionString)) {
                windDirection = INITIAL_VALUE;
            } else {
                try {
                    windDirection = Integer.parseInt(windDirectionString) * 10;
                } catch (NumberFormatException e) {
                    windDirection = INITIAL_VALUE;
                }
            }
        }
    }

    private void setWindSpeed(boolean gustyWind) {
        String speedString = null;
        String localWind = windString;
        if (localWind != null) {
            speedString = localWind.substring(gustyWind ? 2 : 3, 5);
            try {
                windSpeed = Integer.parseInt(speedString);
            } catch (NumberFormatException e) {
                windSpeed = INITIAL_VALUE;
            }
        }
    }

    private void setPressure() {
        setPressureString();
        String localPressure = pressureString;
        if (localPressure != null) {
            String pressureTemp = localPressure.substring(1, 5);
            if (pressureTemp.charAt(0) == '0') {
                pressureTemp = '1' + pressureTemp;
            }
            try {
                pressure = (float) Integer.parseInt(pressureTemp) / 10;
            } catch (NumberFormatException e) {
                pressure = INITIAL_VALUE;
            }
        }
    }

    protected boolean isValidString(String str) {
        return (str.length() == VALID_STRING_LENGTH);
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getWindIndicator() {
        return windIndicator;
    }

    public HorizontalVisibility getHorizontalVisibility() {
        return horizontalVisibility;
    }

    public float getTemperature() {
        return temperature;
    }

    public int getWindDirection() {
        return windDirection;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public float getPressure() {
        return pressure;
    }

    public int getOcta() {
        return octa;
    }

    public Unit<Speed> getWindUnit() {
        return (getWindIndicator() == WS_WILDTYPE_IN_MPS || getWindIndicator() == WS_ANEMOMETER_IN_MPS)
                ? Units.METRE_PER_SECOND
                : Units.KNOT;
    }
}
