/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Timer} class returns the On and Off AC Timer values
 * to the channels.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - Add TimeParser and TimeData classes
 */
@NonNullByDefault
public class Timer {

    private boolean status;
    private int hours;
    private int minutes;

    /**
     * Timer class parameters
     * 
     * @param status on or off
     * @param hours hours
     * @param minutes minutes
     */
    public Timer(boolean status, int hours, int minutes) {
        this.status = status;
        this.hours = hours;
        this.minutes = minutes;
    }

    /**
     * Timer format for the trace log
     */
    public String toString() {
        if (status) {
            return String.format("enabled: %s, hours: %d, minutes: %d", status, hours, minutes);
        } else {
            return String.format("enabled: %s", status);
        }
    }

    /**
     * Timer format of the OH channel
     * 
     * @return conforming String
     */
    public String toChannel() {
        if (status) {
            return String.format("%02d:%02d", hours, minutes);
        } else {
            return "";
        }
    }

    /**
     * This splits the On or off timer channels command back to hours and minutes
     * so the AC start and stop timers can be set
     */
    public class TimeParser {
        /**
         * Parse Time string into components
         * 
         * @param time conforming string
         * @return hours and minutes
         */
        public int[] parseTime(String time) {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            return new int[] { hours, minutes };
        }
    }

    /**
     * This allows the continuity of the current timer settings
     * when new commands on other channels are set.
     */
    public class TimerData {
        /**
         * Status if timer is on
         */
        public boolean status;

        /**
         * Current hours
         */
        public int hours;

        /**
         * Current minutes
         */
        public int minutes;

        /**
         * Sets the TimerData from the response
         * 
         * @param status true if timer is on
         * @param hours hours left
         * @param minutes minutes left
         */
        public TimerData(boolean status, int hours, int minutes) {
            this.status = status;
            this.hours = hours;
            this.minutes = minutes;
        }
    }
}
