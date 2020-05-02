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

/**
 * The {@link Synop} is the ancestor common class for analyzing
 * Synop messages
 *
 * @author Jonarzz - Initial contribution
 */
public abstract class Synop {

    private final int VALID_STRING_LENGTH = 5;

    protected final List<String> stringArray;

    private String stationType;
    protected String stationCode;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int windIndicator;

    private String horizontalVisibility;
    private float temperature;

    private String overcast;
    private int octa;
    private int windDirection;
    private int windSpeed;
    private float pressure;

    protected int horizontalVisibilityInt;
    protected String temperatureString;
    protected String windString;
    protected String pressureString;

    public Synop(List<String> stringArray) {
        this.stringArray = stringArray;

        setStationType();
        setStationCode();
        setDateHourAndWindIndicator();
        setHorizontalVisibility();
        setTemperature();
        setWindAndOvercast();
        setPressure();
    }

    protected void setStationType() {
        if (!stringArray.isEmpty()) {
            if (stringArray.get(0).length() == 4) {
                stationType = stringArray.get(0);
            } else {
                stationType = "";
            }
        }
    }

    protected abstract void setStationCode();

    protected void setDateHourAndWindIndicator() {
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

        if (horizontalVisibilityInt == Constants.INITIAL_VALUE) {
            return;
        }

        if (horizontalVisibilityInt < Constants.HV_LESS_THAN_1_LIMIT
                || horizontalVisibilityInt < Constants.HV_LESS_THAN_1_HP_LIMIT) {
            horizontalVisibility = Constants.HV_LESS_THAN_1_STRING;
        } else if (horizontalVisibilityInt < Constants.HV_LESS_THAN_10_LIMIT
                || horizontalVisibilityInt < Constants.HV_LESS_THAN_10_HP_LIMIT) {
            horizontalVisibility = Constants.HV_LESS_THAN_10_STRING;
        } else if (horizontalVisibilityInt < Constants.HV_LESS_THAN_50_LIMIT
                || horizontalVisibilityInt < Constants.HV_LESS_THAN_50_HP_LIMIT) {
            horizontalVisibility = Constants.HV_LESS_THAN_50_STRING;
        } else {
            horizontalVisibility = Constants.HV_MORE_THAN_50_STRING;
        }
    }

    protected abstract void setHorizontalVisibilityInt();

    private void setTemperature() {
        setTemperatureString();

        if (temperatureString == null) {
            temperature = Constants.INITIAL_VALUE;
            return;
        }

        if (temperatureString.charAt(0) == Constants.PLUS_SIGN_TEMPERATURE) {
            temperature = 1;
        } else if (temperatureString.charAt(0) == Constants.MINUS_SIGN_TEMPERATURE) {
            temperature = -1;
        } else {
            temperature = Constants.INITIAL_VALUE;
            return;
        }

        try {
            float temp = Float.parseFloat(temperatureString.substring(1, 4)) / 10;
            temperature *= temp;
        } catch (NumberFormatException e) {
            temperature = Constants.INITIAL_VALUE;
        }
    }

    protected abstract void setTemperatureString();

    private void setWindAndOvercast() {
        setWindString();

        if (windString == null) {
            overcast = null;
            windDirection = Constants.INITIAL_VALUE;
            windSpeed = Constants.INITIAL_VALUE;
            return;
        }

        if (windString.substring(0, 2).equals("00")) {
            setWindSpeed(true);
        } else {
            setOvercast();
            setWindDirection();
            setWindSpeed(false);
        }
    }

    private void setOvercast() {
        octa = Character.getNumericValue(windString.charAt(0));

        if (octa < 0 || octa > 9) {
            return;
        }

        if (octa == 0) {
            overcast = Constants.CLEAR_SKY;
        } else if (octa > 0 && octa < 8) {
            overcast = Constants.CLOUDY;
        } else {
            overcast = Constants.SKY_NOT_VISIBLE;
        }
    }

    private void setWindDirection() {
        String windDirectionString = windString.substring(1, 3);

        if (windDirectionString.equals("99") || windDirectionString.equals("||")) {
            windDirection = Constants.INITIAL_VALUE;
            return;
        }

        try {
            windDirection = Integer.parseInt(windDirectionString) * 10;
        } catch (NumberFormatException e) {
            windDirection = Constants.INITIAL_VALUE;
        }
    }

    private void setWindSpeed(boolean gustyWind) {
        String speedString = null;

        if (gustyWind) {
            speedString = windString.substring(2, 5);
        } else {
            speedString = windString.substring(3, 5);
        }

        try {
            windSpeed = Integer.parseInt(speedString);
        } catch (NumberFormatException e) {
            windSpeed = Constants.INITIAL_VALUE;
        }
    }

    protected abstract void setWindString();

    private void setPressure() {
        setPressureString();

        if (pressureString == null) {
            return;
        }

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

    protected abstract void setPressureString();

    protected boolean isValidString(String str) {
        return (str.length() == VALID_STRING_LENGTH);
    }

    public String getStationType() {
        return stationType;
    }

    public String getStationCode() {
        return stationCode;
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

    public String getHorizontalVisibility() {
        return horizontalVisibility;
    }

    public float getTemperature() {
        return temperature;
    }

    public String getOvercast() {
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

    public String getWindUnit() {
        if (getWindIndicator() == Constants.WS_WILDTYPE_IN_MPS
                || getWindIndicator() == Constants.WS_ANEMOMETER_IN_MPS) {
            return Constants.WS_MPS;
        } else {
            return Constants.WS_KNOTS;
        }
    }

    public String getWindSource() {
        if (getWindIndicator() == Constants.WS_WILDTYPE_IN_MPS || getWindIndicator() == Constants.WS_WILDTYPE_IN_KNOT) {
            return "estimated";
        } else {
            return "anemometer";
        }
    }

    /**
     * display synop data in a human-readable format
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("station type : " + this.getStationType());
        sb.append("\nstation code : " + this.getStationCode());
        sb.append("\nobservation timestamp (y/m/d h) : " + this.getYear() + "/" + this.getMonth() + "/" + this.getDay()
                + " " + this.getHour() + ":00 UTC");

        sb.append("\nwind indicator : " + this.getWindIndicator());
        sb.append(
                "\nwind speed : " + this.getWindSpeed() + " " + this.getWindUnit() + " (" + this.getWindSource() + ")");
        sb.append("\nwind dir   : " + getWindDirection() + " degrees");

        sb.append("\novercast : " + this.getOvercast());
        sb.append("\novercast in Octa : " + this.getOcta() + "/8");

        sb.append("\nhorizontal visibility : " + this.getHorizontalVisibility());
        sb.append("\ntemperature : " + this.getTemperature());

        sb.append("\npressure   : " + getPressure() + " in hPa");

        return sb.toString();
    }

    /**
     * Retrieve the full report string value. This is the report in its original
     * form
     *
     * @return The original report string.
     */
    public String getReportString() {
        return String.join(" ", stringArray);
    }
}
