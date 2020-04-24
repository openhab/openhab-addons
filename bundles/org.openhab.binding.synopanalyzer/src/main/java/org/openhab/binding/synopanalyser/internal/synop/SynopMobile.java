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
 * The {@link SynopMobile} is responsible for analyzing Mobile station
 * specifics Synop messages
 *
 * @author Jonarzz - Initial contribution
 */
public abstract class SynopMobile extends Synop {

    private String temp;

    private float latitude;
    private float longitude;

    private int verticalQuadrantMultiplier;
    private int horizontalQuadrantMultiplier;

    public SynopMobile(List<String> stringArray) {
        super(stringArray);

        setLatitude();
        setLongitudeAndQuadrant();
    }

    @Override
    protected void setStationCode() {
        if (stringArray.size() < 2 || (temp = stringArray.get(1)).length() > 10 || temp.contains("/")) {
            return;
        }

        stationCode = temp;
    }

    @Override
    protected void setHorizontalVisibilityInt() {
        if (stringArray.size() < 6 || !isValidString((temp = stringArray.get(5)))) {
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
        if (stringArray.size() < 8 || !isValidString((temp = stringArray.get(7)))) {
            return;
        }

        temperatureString = temp.substring(1, 5);
    }

    @Override
    protected void setWindString() {
        if (stringArray.size() < 7 || !isValidString((temp = stringArray.get(6)))) {
            return;
        }

        windString = temp;
    }

    @Override
    protected abstract void setPressureString();

    protected void setLatitude() {
        if (stringArray.size() < 4 || !isValidString((temp = stringArray.get(3)))) {
            return;
        }

        String latitudeString = temp.substring(2, 5);
        int tempInt = 0;

        try {
            tempInt = Integer.parseInt(latitudeString);
        } catch (NumberFormatException e) {
            latitude = Constants.INITIAL_VALUE;
            return;
        }

        latitude = (float) tempInt / 10;
    }

    protected void setLongitudeAndQuadrant() {
        if (stringArray.size() < 5 || !isValidString((temp = stringArray.get(4)))) {
            return;
        }

        setQuadrantMultipliers(temp.charAt(0));
        setLongitude(temp.substring(1, 5));
    }

    protected void setQuadrantMultipliers(char q) {
        switch (q) {
            case '1':
                verticalQuadrantMultiplier = 1;
                horizontalQuadrantMultiplier = 1;
                break;
            case '3':
                verticalQuadrantMultiplier = -1;
                horizontalQuadrantMultiplier = 1;
                break;
            case '5':
                verticalQuadrantMultiplier = -1;
                horizontalQuadrantMultiplier = -1;
                break;
            case '7':
                verticalQuadrantMultiplier = 1;
                horizontalQuadrantMultiplier = -1;
                break;
            default:
                verticalQuadrantMultiplier = 0;
                horizontalQuadrantMultiplier = 0;
                break;
        }
    }

    protected void setLongitude(String str) {
        int tempInt = 0;

        try {
            tempInt = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            longitude = Constants.INITIAL_VALUE;
            return;
        }

        longitude = (float) tempInt / 10;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getVerticalQuadrantMultiplier() {
        return verticalQuadrantMultiplier;
    }

    public int getHorizontalQuadrantMultiplier() {
        return horizontalQuadrantMultiplier;
    }
}
