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
package org.openhab.binding.synopanalyser.internal.synop;

import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * The {@link Synop} is the ancestor common class for analyzing
 * Synop messages
 *
 * @author Jonarzz - Initial contribution
 */
@NonNullByDefault
public abstract class Synop {

    public static enum Overcast {
        UNDEFINED,
        CLEAR_SKY,
        CLOUDY,
        SKY_NOT_VISIBLE
    }

    public static enum HorizontalVisibility {
        UNDEFINED,
        LESS_THAN_1,
        LESS_THAN_10,
        LESS_THAN_50,
        MORE_THAN_50
    }

    private final int VALID_STRING_LENGTH = 5;

    protected final List<String> stringArray;

    // protected @Nullable String stationCode;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int windIndicator;

    private HorizontalVisibility horizontalVisibility = HorizontalVisibility.UNDEFINED;
    private float temperature;

    private Overcast overcast = Overcast.UNDEFINED;
    private int octa;
    private int windDirection;
    private int windSpeed;
    private float pressure;

    protected int horizontalVisibilityInt = Constants.INITIAL_VALUE;
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

    // protected abstract void setStationCode();

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
            hour = Constants.INITIAL_VALUE;
        }
        try {
            day = Integer.parseInt(str.substring(0, 2));
        } catch (NumberFormatException e) {
            day = Constants.INITIAL_VALUE;
        }
    }

    private void setWindIndicator(String str) {
        try {
            windIndicator = Character.getNumericValue(str.charAt(4));
        } catch (NumberFormatException e) {
            windIndicator = Constants.INITIAL_VALUE;
        }
    }

    private void setHorizontalVisibility() {
        setHorizontalVisibilityInt();

        if (horizontalVisibilityInt != Constants.INITIAL_VALUE) {

            if (horizontalVisibilityInt < Constants.HV_LESS_THAN_1_LIMIT
                    || horizontalVisibilityInt < Constants.HV_LESS_THAN_1_HP_LIMIT) {
                horizontalVisibility = HorizontalVisibility.LESS_THAN_1;
            } else if (horizontalVisibilityInt < Constants.HV_LESS_THAN_10_LIMIT
                    || horizontalVisibilityInt < Constants.HV_LESS_THAN_10_HP_LIMIT) {
                horizontalVisibility = HorizontalVisibility.LESS_THAN_10;
            } else if (horizontalVisibilityInt < Constants.HV_LESS_THAN_50_LIMIT
                    || horizontalVisibilityInt < Constants.HV_LESS_THAN_50_HP_LIMIT) {
                horizontalVisibility = HorizontalVisibility.LESS_THAN_50;
            } else {
                horizontalVisibility = HorizontalVisibility.MORE_THAN_50;
            }
        } else {
            horizontalVisibility = HorizontalVisibility.UNDEFINED;
        }
    }

    protected abstract void setHorizontalVisibilityInt();

    private void setTemperature() {
        setTemperatureString();
        temperature = Constants.INITIAL_VALUE;
        String temperatureString = this.temperatureString;
        if (temperatureString != null) {
            char firstChar = temperatureString.charAt(0);
            try {
                float temp = Float.parseFloat(temperatureString.substring(1, 4)) / 10;
                temperature = firstChar == Constants.PLUS_SIGN_TEMPERATURE ? temp
                        : firstChar == Constants.MINUS_SIGN_TEMPERATURE ? -temp : Constants.INITIAL_VALUE;
            } catch (NumberFormatException e) {
                // Silently catch it
            }
        }
    }

    protected abstract void setTemperatureString();

    private void setWindAndOvercast() {
        setWindString();
        if (windString != null) {
            String gustyFlag = windString.substring(0, 2);
            if ("00".equals(gustyFlag)) {
                setWindSpeed(true);
            } else {
                setOvercast();
                setWindDirection();
                setWindSpeed(false);
            }
        } else {
            overcast = Overcast.UNDEFINED;
            windDirection = Constants.INITIAL_VALUE;
            windSpeed = Constants.INITIAL_VALUE;
        }
    }

    private void setOvercast() {
        if (windString != null) {
            octa = Character.getNumericValue(windString.charAt(0));

            if (octa == 0) {
                overcast = Overcast.CLEAR_SKY;
            } else if (octa > 0 && octa < 8) {
                overcast = Overcast.CLOUDY;
            } else if (octa == 9) {
                overcast = Overcast.SKY_NOT_VISIBLE;
            } else {
                overcast = Overcast.UNDEFINED;
            }
        }
    }

    private void setWindDirection() {
        if (windString != null) {
            String windDirectionString = windString.substring(1, 3);

            if (windDirectionString.equals("99") || windDirectionString.equals("||")) {
                windDirection = Constants.INITIAL_VALUE;
            } else {
                try {
                    windDirection = Integer.parseInt(windDirectionString) * 10;
                } catch (NumberFormatException e) {
                    windDirection = Constants.INITIAL_VALUE;
                }
            }
        }
    }

    private void setWindSpeed(boolean gustyWind) {
        String speedString = null;
        if (windString != null) {
            speedString = windString.substring(gustyWind ? 2 : 3, 5);
            try {
                windSpeed = Integer.parseInt(speedString);
            } catch (NumberFormatException e) {
                windSpeed = Constants.INITIAL_VALUE;
            }
        }
    }

    protected abstract void setWindString();

    private void setPressure() {
        setPressureString();

        if (pressureString != null) {

            String pressureTemp = pressureString.substring(1, 5);

            if (pressureTemp.charAt(0) == '0') {
                pressureTemp = '1' + pressureTemp;
            }

            try {
                pressure = (float) Integer.parseInt(pressureTemp) / 10;
            } catch (NumberFormatException e) {
                pressure = Constants.INITIAL_VALUE;
            }
        }
    }

    protected abstract void setPressureString();

    protected boolean isValidString(String str) {
        return (str.length() == VALID_STRING_LENGTH);
    }

    /*
     * public Optional<String> getStationCode() {
     * if (stationCode != null) {
     * return Optional.of(stationCode);
     * } else {
     * return Optional.empty();
     * }
     * }
     */

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

    public Overcast getOvercast() {
        return overcast;
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
        return (getWindIndicator() == Constants.WS_WILDTYPE_IN_MPS
                || getWindIndicator() == Constants.WS_ANEMOMETER_IN_MPS) ? SmartHomeUnits.METRE_PER_SECOND
                        : SmartHomeUnits.KNOT;
    }

    public String getWindSource() {
        if (getWindIndicator() == Constants.WS_WILDTYPE_IN_MPS || getWindIndicator() == Constants.WS_WILDTYPE_IN_KNOT) {
            return "estimated";
        } else {
            return "anemometer";
        }
    }

}
