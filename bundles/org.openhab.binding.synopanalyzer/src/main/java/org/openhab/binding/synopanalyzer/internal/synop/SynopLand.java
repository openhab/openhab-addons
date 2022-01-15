/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SynopLand} is responsible for analyzing Land station
 * specifics Synop messages
 *
 * @author Jonarzz - Initial contribution
 */
@NonNullByDefault
public class SynopLand extends Synop {
    private int rainfall = INITIAL_VALUE;

    public SynopLand(List<String> stringArray) {
        super(stringArray);

        if (stringArray.size() >= 11) {
            String rainfallString = stringArray.get(10);
            if (isValidString(rainfallString) && rainfallString.charAt(0) == 6) {
                try {
                    rainfall = Integer.parseInt(rainfallString.substring(1, 4));
                    if (rainfall >= 990) {
                        rainfall = 0;
                    }
                } catch (NumberFormatException ignore) {
                }

            }
        }
    }

    @Override
    protected void setHorizontalVisibilityInt() {
        if (stringArray.size() >= 4) {
            String horizontalVisibility = stringArray.get(3);
            if (isValidString(horizontalVisibility)) {
                try {
                    horizontalVisibilityInt = Integer.parseInt(horizontalVisibility.substring(3, 5));
                } catch (NumberFormatException ignore) {
                }
            }
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
        if (stringArray.size() >= 5) {
            String windString = stringArray.get(4);
            if (isValidString(windString)) {
                this.windString = windString;
            }
        }
    }

    @Override
    protected void setPressureString() {
        if (stringArray.size() >= 8) {
            String pressureString = stringArray.get(7);
            if (isValidString(pressureString) && pressureString.charAt(0) == '3') {
                this.pressureString = pressureString;
            }
        }
    }

    public int getRainfall() {
        return rainfall;
    }
}
