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
package org.openhab.binding.millheat.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the home dto json structure
 *
 * @author Arne Seime - Initial contribution
 */
public class HomeDTO {
    public long homeId;
    @SerializedName("homeAlways")
    public boolean alwaysHome;
    @SerializedName("homeName")
    public String name;
    @SerializedName("isHoliday")
    public boolean holiday;
    public Long holidayStartTime;
    public String timeZone;
    public Integer modeMinute;
    public Long modeStartTime;
    public Integer holidayTemp;
    public Integer modeHour;
    public Integer currentMode;
    public Long holidayEndTime;
    public Integer homeType;
    public String programId;
    public int holidayTempType;
}
