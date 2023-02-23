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
package org.openhab.binding.draytonwiser.internal.model;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class LocalDateAndTimeDTO {

    private Integer year;
    private String month;
    private Integer date;
    private String day;
    private Integer time;

    public Integer getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public Integer getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public Integer getTime() {
        return time;
    }
}
