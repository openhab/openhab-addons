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
package org.openhab.binding.sagercaster.internal.caster;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class holds the result of the SagerCaster algorithm
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SagerPrediction {
    private final String prediction;

    public SagerPrediction(String sagerCode) {
        this.prediction = sagerCode;
    }

    public String getSagerCode() {
        return prediction;
    }

    public String getForecast() {
        return Character.toString(prediction.charAt(0));
    }

    public String getWindVelocity() {
        return Character.toString(prediction.charAt(1));
    }

    public String getWindDirection() {
        return Character.toString(prediction.charAt(2));
    }

    public String getWindDirection2() {
        return prediction.length() > 3 ? Character.toString(prediction.charAt(3)) : SagerWeatherCaster.UNDEF;
    }
}
