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
package org.openhab.binding.millheat.internal.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * This DTO class wraps the set holiday parameter request
 *
 * @see HomeDTO
 * @see GetHomesResponse
 * @author Arne Seime - Initial contribution
 */
public class SetHolidayParameterRequest implements AbstractRequest {

    public static final String PROP_TEMP = "holidayTemp";
    public static final String PROP_MODE = "isHoliday";
    public static final String PROP_MODE_ADVANCED = "holidayTempType";
    public static final String PROP_START = "holidayStartTime";
    public static final String PROP_END = "holidayEndTime";

    // {"timeZoneNum":"-01:00","value":11,"homeList":[{"homeId":XXXXXXXXXXXX}],"key":"holidayTemp"}
    public List<HomeID> homeList = new ArrayList<>();
    @SerializedName("timeZoneNum")
    public final String timeZone;
    public final String key;
    public final Object value;

    /*
     * Valid parameters: holidayTemp (degrees), holidayStartTime (secs since epoch), holidayEndTime (secs since epoch),
     * isHoliday (boolean), holidayTempType (0 == advanced vacation mode - room uses it's own away temp, 1 == uses
     * holidayTemp)
     */
    public SetHolidayParameterRequest(Long homeId, String timeZone, String parameter, Object value) {
        homeList.add(new HomeID(homeId));
        this.timeZone = timeZone;
        this.key = parameter;
        this.value = value;
    }

    @Override
    public String getRequestUrl() {
        return "holidayChooseHome";
    }

    private class HomeID {
        @SuppressWarnings("unused")
        public Long homeId;

        public HomeID(Long homeId) {
            super();
            this.homeId = homeId;
        }
    }
}
