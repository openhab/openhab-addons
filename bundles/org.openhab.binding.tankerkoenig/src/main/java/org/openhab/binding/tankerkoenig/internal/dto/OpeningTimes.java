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
package org.openhab.binding.tankerkoenig.internal.dto;

/**
 * The {@link OpeningTimes} class is representing all OpeningTimes entries for a station from the api request (i.e array
 * of settings like "Montag" "09:00" "18:00")
 * plus the boolean WholeDay (open).
 *
 * @author Jürgen Baginski - Initial contribution
 */
public class OpeningTimes {

    private Boolean wholeDay;
    private OpeningTime[] openingTimes;
    private String id;

    public OpeningTimes(String id, Boolean wholeDay, OpeningTime[] lopeningTimes) {
        this.wholeDay = wholeDay;
        this.openingTimes = lopeningTimes;
        this.id = id;
    }

    public Boolean getWholeDay() {
        return wholeDay;
    }

    public void setWholeDay(Boolean wholeDay) {
        this.wholeDay = wholeDay;
    }

    public OpeningTime[] getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(OpeningTime[] openingTimes) {
        this.openingTimes = openingTimes;
    }

    public String getid() {
        return id;
    }

    public void setid(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WholeDay: ").append(this.getWholeDay().toString()).append("/ Days: ");
        for (OpeningTime ot : this.getOpeningTimes()) {
            sb.append(ot.toString());
        }
        return sb.toString();
    }
}
