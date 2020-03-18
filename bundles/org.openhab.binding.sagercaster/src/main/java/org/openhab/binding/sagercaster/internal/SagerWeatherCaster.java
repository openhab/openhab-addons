/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
/**
 *       eltiempo.selfip.com - Sager Weathercaster Algorhithm
 *
 *          Copyright © 2008 Naish666 (eltiempo.selfip.com)
 *               October 2008 - v1.0
 *          Java transposition done by Gaël L'hopital - 2015
 **
 *     This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***
 * BT's Global Sager Weathercaster PHP Scripts For Cumulus (Weathercaster)
 * by "Buford T. Justice" / "BTJustice"
 * http://www.freewebs.com/btjustice/bt-forecasters.html
 * 2014-02-05
 *
 * You may redistribute and use these PHP Scripts any way you wish as long as
 * they remain FREE and money is not charged for their use directly or indirectly.
 * If these PHP Scripts are used in your work or are modified in any way, please
 * retain the full credit header.
 * Based Upon:
 * The Sager Weathercaster:  A Scientific Instrument for Accurate Prediction of
 * the Weather
 * Copyright © 1969 by Raymond M. Sager and E. F. Sager
 " The Sager Weathercaster predicts the weather quickly and accurately.  It has been
 * in use since 1942.
 * Not a novelty, not a toy, this is a highly dependable, scientifically designed
 * tool of inestimable value to travelers, farmers, hunters, sailors, yachtsmen, campers,
 * fishermen, students -- in fact, to everyone who needs or wants to know what
 * the weather will be."
 * 378 possible forecasts determined from 4996 dial codes.
 */

package org.openhab.binding.sagercaster.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class is responsible for handling the SagerWeatherCaster algorithm
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SagerWeatherCaster {

    // Northern Polar Zone & Northern Tropical Zone
    private final static String[] NPZDIRECTIONS = { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };
    // Northern Temperate Zone
    private final static String[] NTZDIRECTIONS = { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
    // Southern Polar Zone & Southern Tropical Zone
    private final static String[] SPZDIRECTIONS = { "N", "NW", "W", "SW", "S", "SE", "E", "NE" };
    // Southern Temperate Zone
    private final static String[] STZDIRECTIONS = { "S", "SE", "E", "NE", "N", "NW", "W", "SW" };
    private final static Map<String, @Nullable String> sagerForecaster = new HashMap<>();

    private @Nullable Prevision prevision;
    private @NonNullByDefault({}) String[] usedDirections;

    private int currentBearing = -1;
    private int windEvolution = -1; // Whether the wind during the last 6 hours has changed its direction by
                                    // approximately 45
    // degrees or more
    private int sagerPressure = -1; // currentPressure is Sea Level Adjusted (Relative) barometer in hPa or mB
    private int pressureEvolution = -1; // pressureEvolution There are five points for registering the behavior of your
    // barometer for a period of about 6 hours prior to the forecast.
    private int nubes = -1;
    private int currentBeaufort = -1;
    private double cloudLevel = -1;
    private boolean raining = false;

    public SagerWeatherCaster() {
        Properties prop = new Properties();
        InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("/sagerForecaster.properties");
        try {
            prop.load(input);
            Set<String> propertyNames = prop.stringPropertyNames();
            propertyNames.forEach(entry -> sagerForecaster.put(entry, prop.getProperty(entry)));
        } catch (IOException e) {
            throw new IllegalStateException("Sager Forecaster properties file not found or not accessible");
        }
    }

    public String[] getUsedDirections() {
        return usedDirections;
    }

    public void setBearing(int newBearing, int oldBearing) {
        int windEvol = sagerWindTrend(oldBearing, newBearing);
        if ((windEvol != this.windEvolution) || (newBearing != currentBearing)) {
            this.currentBearing = newBearing;
            this.windEvolution = windEvol;
            updatePrediction();
        }
    }

    public void setPressure(double newPressure, double oldPressure) {
        int newSagerPressure = sagerPressureLevel(newPressure);
        int pressEvol = sagerPressureTrend(newPressure, oldPressure);
        if ((pressEvol != this.pressureEvolution) || (newSagerPressure != sagerPressure)) {
            this.sagerPressure = newSagerPressure;
            this.pressureEvolution = pressEvol;
            updatePrediction();
        }
    }

    public void setCloudLevel(int cloudiness) {
        this.cloudLevel = cloudiness;
        sagerNubesUpdate();
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
        sagerNubesUpdate();
    }

    public void setBeaufort(int beaufortIndex) {
        if (currentBeaufort != beaufortIndex) {
            currentBeaufort = beaufortIndex;
            updatePrediction();
        }
    }

    public int getBeaufort() {
        return currentBeaufort;
    }

    public int getWindEvolution() {
        return this.windEvolution;
    }

    public int getPressureEvolution() {
        return this.pressureEvolution;
    }

    public void sagerNubesUpdate() {
        int result;
        if (!raining) {
            if (cloudLevel > 80) {
                result = 4; // overcast
            } else if (cloudLevel > 50) {
                result = 3; // mostly overcast
            } else if (cloudLevel > 20) {
                result = 2; // partly cloudy
            } else {
                result = 1; // clear
            }
        } else {
            result = 5; // raining
        }
        if (result != nubes) {
            nubes = result;
            updatePrediction();
        }
    }

    public static int sagerPressureLevel(double current) {
        int result = 1;
        if (current > 1029.46) {
            result = 1;
        } else if (current > 1019.3) {
            result = 2;
        } else if (current > 1012.53) {
            result = 3;
        } else if (current > 1005.76) {
            result = 4;
        } else if (current > 999) {
            result = 5;
        } else if (current > 988.8) {
            result = 6;
        } else if (current > 975.28) {
            result = 7;
        } else {
            result = 8;
        }
        return result;
    }

    public static int sagerPressureTrend(double current, double historic) {
        double evol = current - historic;
        int result = 0;

        if (evol > 1.4) {
            result = 1; // Rising Rapidly
        } else if (evol > 0.68) {
            result = 2; // Rising Slowly
        } else if (evol > -0.68) {
            result = 3; // Normal
        } else if (evol > -1.4) {
            result = 4; // Decreasing Slowly
        } else {
            result = 5; // Decreasing Rapidly
        }

        return result;
    }

    public static int sagerWindTrend(double historic, double position) {
        int result = 1; // Steady
        double angle = 180 - Math.abs(Math.abs(position - historic) - 180);
        if (angle > 45) {
            int evol = (int) (historic + angle);
            if (evol > 360) {
                evol = evol - 360;
            }
            if (evol == position) {
                result = 2; // Veering
            } else {
                result = 3; // Backing
            }
        }
        return result;
    }

    private String getCompass() {
        double step = 360.0 / NTZDIRECTIONS.length;
        double b = Math.floor((this.currentBearing + (step / 2.0)) / step);
        return NTZDIRECTIONS[(int) (b % NTZDIRECTIONS.length)];
    }

    private void updatePrediction() {
        int zWind = Arrays.asList(usedDirections).indexOf(getCompass());
        String d1 = "-";

        if (zWind == 0) {
            if (windEvolution == 3) {
                d1 = "A";
            } else if (windEvolution == 1) {
                d1 = "B";
            } else if (windEvolution == 2) {
                d1 = "C";
            }
        } else if (zWind == 1) {
            if (windEvolution == 3) {
                d1 = "D";
            } else if (windEvolution == 1) {
                d1 = "E";
            } else if (windEvolution == 2) {
                d1 = "F";
            }
        } else if (zWind == 2) {
            if (windEvolution == 3) {
                d1 = "G";
            } else if (windEvolution == 1) {
                d1 = "H";
            } else if (windEvolution == 2) {
                d1 = "J";
            }
        } else if (zWind == 3) {
            if (windEvolution == 3) {
                d1 = "K";
            } else if (windEvolution == 1) {
                d1 = "L";
            } else if (windEvolution == 2) {
                d1 = "M";
            }
        } else if (zWind == 4) {
            if (windEvolution == 3) {
                d1 = "N";
            } else if (windEvolution == 1) {
                d1 = "O";
            } else if (windEvolution == 2) {
                d1 = "P";
            }
        } else if (zWind == 5) {
            if (windEvolution == 3) {
                d1 = "Q";
            } else if (windEvolution == 1) {
                d1 = "R";
            } else if (windEvolution == 2) {
                d1 = "S";
            }
        } else if (zWind == 6) {
            if (windEvolution == 3) {
                d1 = "T";
            } else if (windEvolution == 1) {
                d1 = "U";
            } else if (windEvolution == 2) {
                d1 = "V";
            }
        } else if (zWind == 7) {
            if (windEvolution == 3) {
                d1 = "W";
            } else if (windEvolution == 1) {
                d1 = "X";
            } else if (windEvolution == 2) {
                d1 = "Y";
            }
        } else if (currentBeaufort == 0) {
            d1 = "Z";
        }

        String forecast = sagerForecaster
                .get(d1 + String.valueOf(sagerPressure) + String.valueOf(pressureEvolution) + String.valueOf(nubes));
        if (forecast != null) {
            prevision = new Prevision(forecast);
        } else {
            prevision = null;
        }
    }

    public String getForecast() {
        if (prevision != null) {
            char forecast = prevision.zForecast;
            return Character.toString(forecast);
        } else {
            return "-";
        }
    }

    public String getWindVelocity() {
        if (prevision != null) {
            char windVelocity = prevision.zWindVelocity;
            return Character.toString(windVelocity);
        } else {
            return "-";
        }
    }

    public String getWindDirection() {
        if (prevision != null) {
            int direction = prevision.zWindDirection;
            return String.valueOf(direction);
        } else {
            return "-";
        }
    }

    public String getWindDirection2() {
        if (prevision != null) {
            int direction = prevision.zWindDirection2;
            return String.valueOf(direction);
        } else {
            return "-";
        }
    }

    public void setLatitude(double latitude) {
        if (latitude >= 66.6) {
            usedDirections = NPZDIRECTIONS;
        } else if (latitude >= 23.5) {
            usedDirections = NTZDIRECTIONS;
        } else if (latitude >= 0) {
            usedDirections = NPZDIRECTIONS;
        } else if (latitude > -23.5) {
            usedDirections = SPZDIRECTIONS;
        } else if (latitude > -66.6) {
            usedDirections = STZDIRECTIONS;
        } else {
            usedDirections = SPZDIRECTIONS;
        }
    }

    private class Prevision {
        public char zForecast;
        public char zWindVelocity;
        public int zWindDirection;
        public int zWindDirection2;

        public Prevision(String forecast) {
            zForecast = forecast.charAt(0);
            zWindVelocity = forecast.charAt(1);
            zWindDirection = Character.getNumericValue(forecast.charAt(2));
            if (forecast.length() > 3) {
                zWindDirection2 = Character.getNumericValue(forecast.charAt(3));
            } else {
                zWindDirection2 = -1;
            }
        }
    }
}
