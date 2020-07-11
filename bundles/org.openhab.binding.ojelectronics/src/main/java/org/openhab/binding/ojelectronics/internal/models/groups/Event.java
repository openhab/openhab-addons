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

import com.google.gson.annotations.SerializedName;

/**
 * Model for events
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class Event {

    @SerializedName("ScheduleType")
    public Integer scheduleType = 0;
    @SerializedName("Clock")
    public String clock = "";
    @SerializedName("Temperature")
    public Integer temperature = 0;
    @SerializedName("Active")
    public Boolean active = false;
    @SerializedName("EventIsOnNextDay")
    public Boolean eventIsOnNextDay = false;
}
