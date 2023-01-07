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
package org.openhab.binding.weathercompany.internal.model;

/**
 * The {@link PwsObservationsDTO} contains the most recent weather condition
 * observations from the Personl Weather Station (PWS).
 *
 * @author Mark Hilbush - Initial contribution
 */
public class PwsObservationsDTO {
    /*
     * An array of length 1 of observations that represent the
     * most recent PWS observations
     */
    public Observations[] observations;

    public class Observations {
        /*
         * Object containing fields that use a defined unit of measure.
         * The object label is dependent on the units parameter assigned
         * in the request
         */
        public Imperial imperial;

        /*
         * Two-character country code
         */
        public String country;

        /*
         * Time in UNIX seconds
         */
        public long epoch;

        /*
         * The relative humidity of the air
         */
        public Double humidity;

        /*
         * Latitude of the PWS
         */
        public Double lat;

        /*
         * Longitude of the PWS
         */
        public Double lon;

        /*
         * Neighborhood associated with the PWS location
         */
        public String neighborhood;

        /*
         * Time observation is valid in local apparent time by timezone - tz
         * Format "2019-03-06 17:44:44"
         */
        public String obsTimeLocal;

        /*
         * GMT (UTC) time
         * Format ISO 8601 - yyyy-MM-dd'T'HH:mm:ssZZ
         */
        public String obsTimeUtc;

        /*
         * Quality control indicator:
         * -1: No quality control check performed
         * 0: This observation was marked as possibly incorrect by our quality control algorithm
         * 1: This observation passed quality control checks
         */
        public Integer qcStatus;

        /*
         * Frequency of data report updates in minutes
         */
        public Object realtimeFrequency;

        /*
         * Software type of the PWS
         */
        public String softwareType;

        /*
         * Solar radiation in Watts/meter2
         */
        public Double solarRadiation;

        /*
         * ID as registered by wunderground.com
         */
        public String stationID;

        /*
         * UV reading of the intensity of solar radiation
         */
        public Double uv;

        /*
         * Wind direction in degrees
         */
        public Integer winddir;
    }

    public class Imperial {
        /*
         * The temperature which air must be cooled at constant pressure to reach saturation
         */
        public Double dewpt;

        /*
         * Elevation of the PWS in feet
         */
        public Double elev;

        /*
         * An apparent temperature. It represents what the air
         * temperature “feels like” on exposed human skin due to the combined effect
         * of warm temperatures and high humidity.
         */
        public Double heatIndex;

        /*
         * Instantaneous precipitation rate. How much rain would fall if the
         * precipitation intensity did not change for one hour
         */
        public Double precipRate;

        /*
         * Accumulated precipitation for today from midnight to present.
         */
        public Double precipTotal;

        /*
         * Mean Sea Level Pressure, the equivalent pressure reading at sea level recorded at this station
         */
        public Double pressure;

        /*
         * Temperature
         */
        public Double temp;

        /*
         * An apparent temperature. It represents what the air temperature “feels like” on exposed human
         * skin due to the combined effect of the cold temperatures and wind speed.
         */
        public Double windChill;

        /*
         * Sudden and temporary variations of the average Wind Speed.
         */
        public Double windGust;

        /*
         * The wind is treated as a vector; hence, winds must have direction and magnitude (speed).
         * The wind information reported in the hourly current conditions corresponds to
         * a 10-minute average called the sustained wind speed
         */
        public Double windSpeed;
    }
}
