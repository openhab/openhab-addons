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

package org.openhab.binding.sagercaster.internal.caster;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for handling the SagerWeatherCaster algorithm
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = SagerWeatherCaster.class, scope = ServiceScope.SINGLETON)
@NonNullByDefault
public class SagerWeatherCaster {
    public static final String UNDEF = "-";
    // Northern Polar Zone & Northern Tropical Zone
    private static final String[] NPZDIRECTIONS = { "S", "SW", "W", "NW", "N", "NE", "E", "SE" };
    // Northern Temperate Zone
    private static final String[] NTZDIRECTIONS = { "N", "NE", "E", "SE", "S", "SW", "W", "NW" };
    // Southern Polar Zone & Southern Tropical Zone
    private static final String[] SPZDIRECTIONS = { "N", "NW", "W", "SW", "S", "SE", "E", "NE" };
    // Southern Temperate Zone
    private static final String[] STZDIRECTIONS = { "S", "SE", "E", "NE", "N", "NW", "W", "SW" };

    private final Logger logger = LoggerFactory.getLogger(SagerWeatherCaster.class);
    private final Properties forecaster = new Properties();

    private Optional<SagerPrediction> prevision = Optional.empty();
    private String[] usedDirections = NTZDIRECTIONS; // Defaulted to Northern Zone

    private int currentBearing = -1;
    private int windEvolution = -1; // Whether the wind during the last 6 hours has changed its direction by
                                    // approximately 45 degrees or more
    private int sagerPressure = -1; // currentPressure is Sea Level Adjusted (Relative) barometer in hPa or mB
    private int pressureEvolution = -1; // pressureEvolution There are five points for registering the behavior of your
    // barometer for a period of about 6 hours prior to the forecast.
    private int nubes = -1;
    private int currentBeaufort = -1;
    private double cloudLevel = -1;
    private boolean raining = false;

    @Activate
    public SagerWeatherCaster() {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("/sagerForecaster.properties")) {
            forecaster.load(input);
        } catch (IOException e) {
            logger.warn("Error during Sager Forecaster startup", e);
        }
    }

    public String[] getUsedDirections() {
        return usedDirections;
    }

    public void setBearing(int newBearing, int oldBearing) {
        int windEvol = sagerWindTrend(oldBearing, newBearing);
        if ((windEvol != windEvolution) || (newBearing != currentBearing)) {
            currentBearing = newBearing;
            windEvolution = windEvol;
            updatePrediction();
        }
    }

    public void setPressure(double newPressure, double oldPressure) {
        int newSagerPressure = sagerPressureLevel(newPressure);
        int pressEvol = sagerPressureTrend(newPressure, oldPressure);
        if ((pressEvol != pressureEvolution) || (newSagerPressure != sagerPressure)) {
            sagerPressure = newSagerPressure;
            pressureEvolution = pressEvol;
            updatePrediction();
        }
    }

    public void setCloudLevel(int cloudiness) {
        cloudLevel = cloudiness;
        sagerNubesUpdate();
    }

    public void setRaining(boolean isRaining) {
        raining = isRaining;
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
        return windEvolution;
    }

    public int getPressureEvolution() {
        return pressureEvolution;
    }

    private void sagerNubesUpdate() {
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

    private static int sagerPressureLevel(double current) {
        if (current > 1029.46) {
            return 1;
        } else if (current > 1019.3) {
            return 2;
        } else if (current > 1012.53) {
            return 3;
        } else if (current > 1005.76) {
            return 4;
        } else if (current > 999) {
            return 5;
        } else if (current > 988.8) {
            return 6;
        } else if (current > 975.28) {
            return 7;
        }
        return 8;
    }

    private static int sagerPressureTrend(double current, double historic) {
        double evol = current - historic;

        if (evol > 1.4) {
            return 1; // Rising Rapidly
        } else if (evol > 0.68) {
            return 2; // Rising Slowly
        } else if (evol > -0.68) {
            return 3; // Normal
        } else if (evol > -1.4) {
            return 4; // Decreasing Slowly
        }
        return 5; // Decreasing Rapidly
    }

    private static int sagerWindTrend(double historic, double position) {
        int result = 1; // Steady
        double angle = 180 - Math.abs(Math.abs(position - historic) - 180);
        if (angle > 45) {
            int evol = (int) (historic + angle);
            evol -= (evol > 360) ? 360 : 0;
            result = (evol == position) ? 2 : 3; // Veering : Backing
        }
        return result;
    }

    private String getCompass() {
        double step = 360.0 / NTZDIRECTIONS.length;
        double b = Math.floor((currentBearing + (step / 2.0)) / step);
        return NTZDIRECTIONS[(int) (b % NTZDIRECTIONS.length)];
    }

    private void updatePrediction() {
        int zWind = Arrays.asList(usedDirections).indexOf(getCompass());
        String d1 = UNDEF;
        switch (zWind) {
            case 0:
                if (windEvolution == 3) {
                    d1 = "A";
                } else if (windEvolution == 1) {
                    d1 = "B";
                } else if (windEvolution == 2) {
                    d1 = "C";
                }
                break;
            case 1:
                if (windEvolution == 3) {
                    d1 = "D";
                } else if (windEvolution == 1) {
                    d1 = "E";
                } else if (windEvolution == 2) {
                    d1 = "F";
                }
                break;
            case 2:
                if (windEvolution == 3) {
                    d1 = "G";
                } else if (windEvolution == 1) {
                    d1 = "H";
                } else if (windEvolution == 2) {
                    d1 = "J";
                }
                break;
            case 3:
                if (windEvolution == 3) {
                    d1 = "K";
                } else if (windEvolution == 1) {
                    d1 = "L";
                } else if (windEvolution == 2) {
                    d1 = "M";
                }
                break;
            case 4:
                if (windEvolution == 3) {
                    d1 = "N";
                } else if (windEvolution == 1) {
                    d1 = "O";
                } else if (windEvolution == 2) {
                    d1 = "P";
                }
                break;
            case 5:
                if (windEvolution == 3) {
                    d1 = "Q";
                } else if (windEvolution == 1) {
                    d1 = "R";
                } else if (windEvolution == 2) {
                    d1 = "S";
                }
                break;
            case 6:
                if (windEvolution == 3) {
                    d1 = "T";
                } else if (windEvolution == 1) {
                    d1 = "U";
                } else if (windEvolution == 2) {
                    d1 = "V";
                }
                break;
            case 7:
                if (windEvolution == 3) {
                    d1 = "W";
                } else if (windEvolution == 1) {
                    d1 = "X";
                } else if (windEvolution == 2) {
                    d1 = "Y";
                }
                break;
            default:
                if (currentBeaufort == 0) {
                    d1 = "Z";
                }
        }
        String forecast = forecaster.getProperty(
                d1 + String.valueOf(sagerPressure) + String.valueOf(pressureEvolution) + String.valueOf(nubes));
        prevision = Optional.ofNullable(forecast != null ? new SagerPrediction(forecast) : null);
    }

    public String getForecast() {
        return prevision.map(p -> p.getForecast()).orElse(UNDEF);
    }

    public String getWindVelocity() {
        return prevision.map(p -> p.getWindVelocity()).orElse(UNDEF);
    }

    public String getWindDirection() {
        return prevision.map(p -> p.getWindDirection()).orElse(UNDEF);
    }

    public String getWindDirection2() {
        return prevision.map(p -> p.getWindDirection2()).orElse(UNDEF);
    }

    public String getSagerCode() {
        return prevision.map(p -> p.getSagerCode()).orElse(UNDEF);
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

    public int getPredictedBeaufort() {
        int result = currentBeaufort;
        switch (getWindVelocity()) {
            case "N":
                result += 1;
                break;
            case "F":
                result = 4;
                break;
            case "S":
                result = 6;
                break;
            case "G":
                result = 8;
                break;
            case "W":
                result = 10;
                break;
            case "H":
                result = 12;
                break;
            case "D":
                result -= 1;
                break;
        }
        return result;
    }
}
