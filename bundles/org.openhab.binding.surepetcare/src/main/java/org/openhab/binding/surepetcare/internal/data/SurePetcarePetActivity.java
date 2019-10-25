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
package org.openhab.binding.surepetcare.internal.data;

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
    private Integer tagId;
    @SerializedName("device_id")
    private Integer deviceId;
    @SerializedName("user_id")
    private Integer userId;
    @SerializedName("where")
    private Integer where;
    @SerializedName("since")
    private Date since;

    public SurePetcarePetActivity() {
    }

    public SurePetcarePetActivity(Integer location, Date since) {
        this.where = location;
        this.since = since;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getWhere() {
        return where;
    }

    public void setWhere(Integer where) {
        this.where = where;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public ZonedDateTime getLocationChanged() {
        return since == null ? null : since.toInstant().atZone(ZoneId.systemDefault()).withNano(0);
    }

}
