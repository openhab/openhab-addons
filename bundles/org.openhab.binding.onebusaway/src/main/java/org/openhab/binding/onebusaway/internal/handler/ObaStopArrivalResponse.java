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
package org.openhab.binding.onebusaway.internal.handler;

/**
 * The {@link ObaStopArrivalResponse} is a representation of the OneBusAway response for requesting a stops arrival
 * data.
 *
 * @see <a href=
 *      "http://developer.onebusaway.org/modules/onebusaway-application-modules/current/api/where/methods/arrivals-and-departures-for-stop.html">arrivals-and-departures-for-stop
 *      documentation</a>
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class ObaStopArrivalResponse {
    public long currentTime;
    public Data data;

    public class Data {
        public Entry entry;
    }

    public class Entry {
        public ArrivalAndDeparture[] arrivalsAndDepartures;
    }

    public class ArrivalAndDeparture implements Comparable<ArrivalAndDeparture> {
        public boolean predicted;
        public long predictedArrivalTime;
        public long predictedDepartureTime;
        public long scheduledArrivalTime;
        public long scheduledDepartureTime;
        public String routeLongName;
        public String routeShortName;
        public String routeId;
        public String stopId;
        public String tripHeadsign;

        /**
         * Assumes other is for the same routeId and stopId. Sorts based on arrival time.
         */
        @Override
        public int compareTo(ArrivalAndDeparture other) {
            // Prefer predicated over scheduled times for order
            return (int) ((predicted ? predictedArrivalTime : scheduledArrivalTime)
                    - (other.predicted ? other.predictedArrivalTime : other.scheduledArrivalTime));
        }
    }
}
