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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author Marcin Czeczko - Initial contribution
 */
public class CalendarTask {
    /**
     * Start time expressed in minutes after midnight.
     */
    private Integer start;

    /**
     * Duration time expressed in minutes
     */
    private Integer duration;
    private Boolean monday;
    private Boolean tuesday;
    private Boolean wednesday;
    private Boolean thursday;
    private Boolean friday;
    private Boolean saturday;
    private Boolean sunday;

    public Integer getStart() {
        return start;
    }

    public Integer getDuration() {
        return duration;
    }

    public Boolean getMonday() {
        return monday;
    }

    public Boolean getTuesday() {
        return tuesday;
    }

    public Boolean getWednesday() {
        return wednesday;
    }

    public Boolean getThursday() {
        return thursday;
    }

    public Boolean getFriday() {
        return friday;
    }

    public Boolean getSaturday() {
        return saturday;
    }

    public Boolean getSunday() {
        return sunday;
    }
}
