/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for the daily schedules
 *
 * @author James Kinsman - Initial Contribution
 *
 */
public class DailySchedules {

    @SerializedName("dailySchedules")
    private List<DailySchedule> schedules;

    public DailySchedules() {
        schedules = new ArrayList<>();
    }

    public List<DailySchedule> getSchedules() {
        return schedules;
    }
}
