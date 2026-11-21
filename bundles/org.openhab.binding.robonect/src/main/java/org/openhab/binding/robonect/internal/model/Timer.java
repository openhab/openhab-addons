/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An object holding the timer information of the mower status.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
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

        /**
         * Creates a timer mode with its protocol code.
         *
         * @param code numeric timer mode code
         */
        TimerMode(int code) {
            this.code = code;
        }

        /**
         * Returns the protocol code for this timer mode.
         *
         * @return the numeric timer mode code
         */
        public int getCode() {
            return code;
        }

        /**
         * Finds a timer mode by its protocol code.
         *
         * @param code numeric timer mode code
         * @return the matching mode, or inactive if the code is unknown
         */
        public static TimerMode fromCode(int code) {
            for (TimerMode status : TimerMode.values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return INACTIVE;
        }
    }

    private @Nullable TimerMode status;

    private @Nullable NextTimer next;

    /**
     * Returns the timer mode; see {@link TimerMode}.
     *
     * @return - the timer mode. see {@link TimerMode}
     */
    public @Nullable TimerMode getStatus() {
        return status;
    }

    /**
     * Returns information about when the next timer execution will be. See {@link NextTimer}.
     *
     * @return - information about when the next timer execution will be. See {@link NextTimer}
     */
    public @Nullable NextTimer getNext() {
        return next;
    }

    /**
     * Sets the status.
     *
     * @param status the status
     */
    public void setStatus(TimerMode status) {
        this.status = status;
    }

    /**
     * Sets the next.
     *
     * @param next the next
     */
    public void setNext(NextTimer next) {
        this.next = next;
    }
}
