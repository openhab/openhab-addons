/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kvv.internal;

import java.util.List;

/**
 * Represents the result of a call to the KVV api to fetch the next departures at a specific stop.
 *
 * @author Maximilian Hess - Initial contribution
 *
 */
public class DepartureResult {

    /** timestamp */
    private String timestamp;

    /** name of the stop */
    private String stopName;

    /** Ordered list of departures */
    private List<Departure> departures;

    /**
     * Creates a new and empty {@link DepartureResult}
     */
    public DepartureResult() {
    }

    /**
     * Returns the timestamp.
     *
     * @return the timestamp.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp.
     *
     * @param timestamp the new timestamp.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the name of the stop.
     *
     * @return the name of the stop.
     */
    public String getStopName() {
        return stopName;
    }

    /**
     * Sets the name of the stop.
     *
     * @param stopName the new name of the stop
     */
    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    /**
     * Returns an ordered list of the next departures.
     *
     * @return an ordered list of the next departures.
     */
    public List<Departure> getDepartures() {
        return departures;
    }

    /**
     * Sets the ordered list of the next departures.
     *
     * @param departures the new departures
     */
    public void setDepartures(List<Departure> departures) {
        this.departures = departures;
    }

    @Override
    public String toString() {
        return "DepartureResult [timestamp=" + timestamp + ", stopName=" + stopName + ", departures=" + departures
                + "]";
    }

    /**
     * Encapsulates a single {@link Departure}. Most important are 'route', 'destination' and 'destination'.
     *
     * @author Maximilian Hess - Initial contribution
     *
     */
    public class Departure {

        /** the route (the 'name' of the train), e.g. 'S5' */
        private String route;

        /** the destination of the train */
        private String destination;

        /** the direction of the train */
        private String direction;

        /** the estimated time available */
        private String time;

        /** the type of the train */
        private String vehicleType;

        /** is the train a low-floor train 'niederfluhrbahn'? */
        private boolean lowfloor;

        /** the timestamp */
        private boolean realtime;

        /** the traction */
        private int traction;

        /** the position of the stop */
        private String stopPosition;

        /**
         * Creates a new, empty {@link Departure}.
         */
        public Departure() {
        }

        /**
         * Returns the the route (the 'name' of the train), e.g. 'S5'.
         *
         * @return the the route (the 'name' of the train), e.g. 'S5'.
         */
        public String getRoute() {
            return route;
        }

        /**
         * Sets the route.
         *
         * @param route the new route
         */
        public void setRoute(String route) {
            this.route = route;
        }

        /**
         * Returns the destination.
         *
         * @return the destination.
         */
        public String getDestination() {
            return destination;
        }

        /**
         * Sets the destination.
         *
         * @param destination the new destination
         */
        public void setDestination(String destination) {
            this.destination = destination;
        }

        /**
         * Returns the direction.
         *
         * @return the direction.
         */
        public String getDirection() {
            return direction;
        }

        /**
         * Sets the direction.
         *
         * @param direction the new direction
         */
        public void setDirection(String direction) {
            this.direction = direction;
        }

        /**
         * Returns the estimated time available.
         *
         * @return the estimated time available.
         */
        public String getTime() {
            return time;
        }

        /**
         * Sets the estimated time available.
         *
         * @param time the new estimated time available
         */
        public void setTime(String time) {
            this.time = time;
        }

        /**
         * Returns the vehicle type.
         *
         * @return the vehicle type.
         */
        public String getVehicleType() {
            return vehicleType;
        }

        /**
         * Sets the vehicle type.
         *
         * @param vehicleType the new vehicle type.
         */
        public void setVehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
        }

        /**
         * Returns {@code true} if the train is a low-floor train or {@code false} if not.
         *
         * @return {@code true} if the train is a low-floor train or {@code false} if not.
         */
        public boolean isLowfloor() {
            return lowfloor;
        }

        /**
         * Sets {@code true} if the train is a low-floor train or {@code false} if not.
         *
         * @param lowfloor the new low-floor status
         */
        public void setLowfloor(boolean lowfloor) {
            this.lowfloor = lowfloor;
        }

        /**
         * Returns {@code true} if the train is in time or {@code false} if not.
         *
         * @return {@code true} if the train is in time or {@code false} if not.
         */
        public boolean isRealtime() {
            return realtime;
        }

        /**
         * Sets the realtime status.
         *
         * @param realtime the new realtime status
         */
        public void setRealtime(boolean realtime) {
            this.realtime = realtime;
        }

        /**
         * Returns the traction.
         *
         * @return the traction.
         */
        public int getTraction() {
            return traction;
        }

        /**
         * Sets the traction.
         *
         * @param traction the new traction.
         */
        public void setTraction(int traction) {
            this.traction = traction;
        }

        /**
         * Returns the stop position.
         *
         * @return the stop position.
         */
        public String getStopPosition() {
            return stopPosition;
        }

        /**
         * Sets the stop position.
         *
         * @param stopPosition the new stop position
         */
        public void setStopPosition(String stopPosition) {
            this.stopPosition = stopPosition;
        }

        @Override
        public String toString() {
            final String timePrefix = (this.time.endsWith("min")) ? " in " : " at ";
            return "Route " + this.route + timePrefix + this.time + " heading to " + this.destination;
        }
    }

}
