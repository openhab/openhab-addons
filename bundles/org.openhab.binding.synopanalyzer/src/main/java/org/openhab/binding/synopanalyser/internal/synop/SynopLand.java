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
 * The {@link SynopLand} is responsible for analyzing Land station
 * specifics Synop messages
 *
 * @author Jonarzz - Initial contribution
 */
public class SynopLand extends Synop {

    private String temp;

    private int rainfall;
    private String rainfallString;

    public SynopLand(List<String> stringArray) {
        super(stringArray);

        setRainfall();
    }

    @Override
    protected void setStationCode() {
        if (stringArray.size() < 3 || (temp = stringArray.get(2)).length() > 10 || temp.contains("/")) {
            return;
        }

        stationCode = temp;
    }

    @Override
    protected void setHorizontalVisibilityInt() {
        if (stringArray.size() < 4 || !isValidString((temp = stringArray.get(3)))) {
            horizontalVisibilityInt = Constants.INITIAL_VALUE;
            return;
        }

        try {
            horizontalVisibilityInt = Integer.parseInt(temp.substring(3, 5));
        } catch (NumberFormatException e) {
            horizontalVisibilityInt = Constants.INITIAL_VALUE;
        }
    }

    @Override
    protected void setTemperatureString() {
        if (stringArray.size() < 6 || !isValidString(stringArray.get(5))) {
            return;
        }

        if (stringArray.get(5).charAt(0) == '0') {
            if (stringArray.size() < 7 || !isValidString(stringArray.get(6))) {
                return;
            }

            temperatureString = stringArray.get(6).substring(1, 5);
        } else if (isValidString(stringArray.get(5))) {
            temperatureString = stringArray.get(5).substring(1, 5);
        }
    }

    @Override
    protected void setWindString() {
        if (stringArray.size() < 5 || !isValidString((temp = stringArray.get(4)))) {
            return;
        }

        windString = temp;
    }

    @Override
    protected void setPressureString() {
        if (stringArray.size() < 8 || stringArray.get(7).charAt(0) != '3'
                || !isValidString((temp = stringArray.get(7)))) {
            return;
        }

        pressureString = temp;
    }

    private void setRainfall() {
        setRainfallString();

        if (rainfallString == null) {
            rainfall = Constants.INITIAL_VALUE;
            return;
        }

        try {
            rainfall = Integer.parseInt(rainfallString.substring(1, 4));
            if (rainfall >= 990) {
                rainfall = 0;
            }
        } catch (NumberFormatException e) {
            rainfall = Constants.INITIAL_VALUE;
        }
    }

    protected void setRainfallString() {
        if (stringArray.size() < 11 || stringArray.get(10).charAt(0) != '6'
                || !isValidString((temp = stringArray.get(10)))) {
            return;
        }

        rainfallString = temp;
    }

    @Override
    public String getStationCode() {
        return stationCode;
    }

    public int getRainfall() {
        return rainfall;
    }
}
