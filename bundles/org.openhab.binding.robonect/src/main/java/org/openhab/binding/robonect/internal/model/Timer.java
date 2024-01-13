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
package org.openhab.binding.robonect.internal.model;

/**
 * An object holding the timer information of the mower status.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class Timer {

    /**
     * an enum defining the possible timer status.
     */
    public enum TimerMode {
        /**
         * timer is inactive. No timer is set or the mower is not in AUTO mode.
         */
        INACTIVE(0),

        /**
         * timer is active. The period of the timer is active and the mower is executing it in AUTO mode.
         */
        ACTIVE(1),

        /**
         * timer is standby. A timer is set, the mower is in AUTO mode but the timer period did not start yet.
         */
        STANDBY(2);

        private int code;

        TimerMode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TimerMode fromCode(int code) {
            for (TimerMode status : TimerMode.values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return INACTIVE;
        }
    }

    private TimerMode status;

    private NextTimer next;

    /**
     * @return - the timer mode. see {@link TimerMode}
     */
    public TimerMode getStatus() {
        return status;
    }

    /**
     * @return - information about when the next timer execution will be. See {@link NextTimer}
     */
    public NextTimer getNext() {
        return next;
    }

    public void setStatus(TimerMode status) {
        this.status = status;
    }

    public void setNext(NextTimer next) {
        this.next = next;
    }
}
