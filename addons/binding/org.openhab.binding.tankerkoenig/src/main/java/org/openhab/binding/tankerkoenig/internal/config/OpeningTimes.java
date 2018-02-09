/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.internal.config;

/**
 * The {@link OpeningTimes} class is representing all OpeningTimes entries for a station from the api request (i.e array
 * of settings like "Montag" "09:00" "18:00")
 * plus the boolean WholeDay (open).
 *
 * @author Jürgen Baginski
 *
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
