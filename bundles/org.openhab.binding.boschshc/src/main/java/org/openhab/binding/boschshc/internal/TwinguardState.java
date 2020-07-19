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
package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

public class TwinguardState {

    /*
     * {"maxTemperature":25,"minTemperature":20,"custom":false,"name":"HALLWAY","maxHumidity":60,"minHumidity":40,
     * "maxPurity":1000}
     */
    class ComfortZone {
        double maxTemperature;
        double minTemperature;
        boolean custom;
        String name;
        double maxHumidity;
        double minHumidity;
        double maxPurity;
    }

    /**
     * {"temperatureRating":"GOOD","humidityRating":"MEDIUM","purity":620,"comfortZone":....,"@type":"airQualityLevelState",
     * "purityRating":"GOOD","temperature":23.77,"description":"LITTLE_DRY","humidity":32.69,"combinedRating":"MEDIUM"}
     */

    String temperatureRating;
    String humidityRating;

    int purity;

    ComfortZone comfortZone;

    @SerializedName("@type")
    String type;

    String purityRating;

    double temperature;
    String description;

    double humidity;
    String combinedRating;
}
