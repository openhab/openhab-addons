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
package org.openhab.binding.tapocontrol.internal.devices.wifi.hub;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoBaseDeviceData;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo-Hub Information class
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoHubData extends TapoBaseDeviceData {
    @SerializedName("in_alarm")
    @Expose(serialize = false, deserialize = true)
    private boolean alarmActive = false;

    @SerializedName("in_alarm_source")
    @Expose(serialize = false, deserialize = true)
    private String alarmSource = "";

    /***********************************
     *
     * GET VALUES
     *
     ************************************/
    public boolean alarmIsActive() {
        return alarmActive;
    }

    public String getAlarmSource() {
        return alarmSource;
    }
}
