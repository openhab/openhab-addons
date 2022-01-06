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
package org.openhab.binding.kvv.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the result of a call to the KVV api to fetch the next departures at a specific stop.
 *
 * @author Maximilian Hess - Initial contribution
 *
 */
@NonNullByDefault
public class DepartureResult {

    public String stopName;

    public List<Departure> departures;

    public DepartureResult() {
        this.stopName = "";
        this.departures = new ArrayList<Departure>();
    }

    @Override
    public String toString() {
        return "DepartureResult [stopName=" + stopName + ", departures=" + departures + "]";
    }

    /**
     * Encapsulates a single {@link Departure}. Most important are 'route', 'destination' and 'destination'.
     *
     * @author Maximilian Hess - Initial contribution
     *
     */
    @NonNullByDefault
    public static class Departure {

        public String route = "";

        public String destination = "";

        public String direction = "";

        public String time = "";

        public String vehicleType = "";

        public boolean lowfloor;

        public boolean realtime;

        public int traction;

        public String stopPosition = "";

        @Override
        public String toString() {
            final String timePrefix = (this.time.endsWith("min")) ? " in " : " at ";
            return "Route " + this.route + timePrefix + this.time + " heading to " + this.destination;
        }
    }
}
