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
package org.openhab.binding.robonect.internal.model;

/**
 * This object will be part of the status response and holds the next timer execution information.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class NextTimer {

    private String date;
    private String time;
    private String unix;

    /**
     * @return - The date (dd.MM.yy) of the next timer execution.
     */
    public String getDate() {
        return date;
    }

    /**
     * @return - the timestamp (HH:mm:ss) of the next timer execution
     */
    public String getTime() {
        return time;
    }

    /**
     * @return - the next timer execution in the form of a unix timestamp.
     */
    public String getUnix() {
        return unix;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUnix(String unix) {
        this.unix = unix;
    }
}
