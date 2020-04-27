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
package org.openhab.binding.surepetcare.internal.dto;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcarePetActivity} is the Java class used to represent the
 * status of a pet. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 * @author Holger Eisold - Added pet feeder status
 */
public class SurePetcarePetActivity {

    @SerializedName("tag_id")
    public Integer tagId;
    @SerializedName("device_id")
    public Integer deviceId;
    @SerializedName("user_id")
    public Integer userId;
    @SerializedName("where")
    public Integer where;
    @SerializedName("since")
    public Date since;

    public SurePetcarePetActivity() {
    }

    public SurePetcarePetActivity(Integer location, Date since) {
        this.where = location;
        this.since = since;
    }

    public ZonedDateTime getLocationChanged() {
        return since == null ? null : since.toInstant().atZone(ZoneId.systemDefault()).withNano(0);
    }

}
