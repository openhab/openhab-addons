/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

/**
 * @author Marco Meyer - Initial contribution
 */
public class NextTimer {
    private String date;
    private String time;
    private String unix;

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

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
