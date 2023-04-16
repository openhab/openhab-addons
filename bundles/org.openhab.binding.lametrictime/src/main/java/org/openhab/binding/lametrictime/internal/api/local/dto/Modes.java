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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for modes.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Modes {
    private TimeBased timeBased;
    private WhenDark whenDark;

    public TimeBased getTimeBased() {
        return timeBased;
    }

    public void setTimeBased(TimeBased timeBased) {
        this.timeBased = timeBased;
    }

    public Modes withTimeBased(TimeBased timeBased) {
        this.timeBased = timeBased;
        return this;
    }

    public WhenDark getWhenDark() {
        return whenDark;
    }

    public void setWhenDark(WhenDark whenDark) {
        this.whenDark = whenDark;
    }

    public Modes withWhenDark(WhenDark whenDark) {
        this.whenDark = whenDark;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Modes [timeBased=");
        builder.append(timeBased);
        builder.append(", whenDark=");
        builder.append(whenDark);
        builder.append("]");
        return builder.toString();
    }
}
