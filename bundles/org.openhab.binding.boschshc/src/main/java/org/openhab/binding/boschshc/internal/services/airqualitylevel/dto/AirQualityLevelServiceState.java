/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.airqualitylevel.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Represents the state of a device as reported from the Smart Home Controller
 *
 * @author Stefan Kästle - Initial contribution
 */
public class AirQualityLevelServiceState extends BoschSHCServiceState {

    public AirQualityLevelServiceState() {
        super("airQualityLevelState");
    }

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

    public String temperatureRating;
    public String humidityRating;

    public int purity;

    public ComfortZone comfortZone;

    public String purityRating;

    public double temperature;
    public String description;

    public double humidity;
    public String combinedRating;
}
