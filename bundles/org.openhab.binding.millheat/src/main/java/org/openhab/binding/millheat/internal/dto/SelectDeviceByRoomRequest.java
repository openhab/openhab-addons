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
 * This DTO class wraps the select device by room request
 * 
 * @author Arne Seime - Initial contribution
 */
public class SelectDeviceByRoomRequest implements AbstractRequest {
    public final Long roomId;
    @SerializedName("timeZoneNum")
    public final String timeZone;

    public SelectDeviceByRoomRequest(final Long roomId, final String timeZone) {
        this.roomId = roomId;
        this.timeZone = timeZone;
    }

    @Override
    public String getRequestUrl() {
        return "selectDevicebyRoom";
    }
}
