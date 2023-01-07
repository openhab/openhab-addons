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
package org.openhab.binding.evohome.internal.api.models.v2.dto.request;

/**
 * Builder for timed API requests
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public abstract class TimedRequestBuilder<T> implements RequestBuilder<T> {
    private boolean useEndTime;
    private int year;
    private int month;
    private int day;

    public RequestBuilder<T> withEndTime(int year, int month, int day) {
        this.useEndTime = true;
        this.year = year;
        this.month = month;
        this.day = day;
        return this;
    }

    protected boolean useEndTime() {
        return useEndTime;
    }

    protected int getYear() {
        return year;
    }

    protected int getMonth() {
        return month;
    }

    protected int getDay() {
        return day;
    }
}
