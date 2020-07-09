/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.models.groups;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model for events
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class Event {

    @SerializedName("ScheduleType")
    @Expose
    public Integer scheduleType = 0;
    @SerializedName("Clock")
    @Expose
    public String clock = "";
    @SerializedName("Temperature")
    @Expose
    public Integer temperature = 0;
    @SerializedName("Active")
    @Expose
    public Boolean active = false;
    @SerializedName("EventIsOnNextDay")
    @Expose
    public Boolean eventIsOnNextDay = false;
}
