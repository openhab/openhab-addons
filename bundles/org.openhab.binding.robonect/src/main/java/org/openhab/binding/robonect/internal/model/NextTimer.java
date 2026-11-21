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
 * This object will be part of the status response and holds the next timer execution information.
 * 
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
public class NextTimer {

    private @Nullable String date;
    private @Nullable String time;
    private @Nullable String unix;

    /**
     * Returns the date (dd.MM.yy) of the next timer execution.
     *
     * @return - The date (dd.MM.yy) of the next timer execution.
     */
    public @Nullable String getDate() {
        return date;
    }

    /**
     * Returns the timestamp ({@code HH:mm}:ss) of the next timer execution.
     *
     * @return - the timestamp ({@code HH:mm}:ss) of the next timer execution
     */
    public @Nullable String getTime() {
        return time;
    }

    /**
     * Returns the next timer execution in the form of a unix timestamp.
     *
     * @return - the next timer execution in the form of a unix timestamp.
     */
    public @Nullable String getUnix() {
        return unix;
    }

    /**
     * Sets the date.
     *
     * @param date the date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Sets the time.
     *
     * @param time the time
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Sets the Unix timestamp.
     *
     * @param unix the Unix timestamp
     */
    public void setUnix(String unix) {
        this.unix = unix;
    }
}
