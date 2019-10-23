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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcarePetFeeding} is the Java class used to represent the
 * status of a pet. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 * @author Holger Eisold - Added pet feeder status
 */
public class SurePetcarePetFeeding {

    @SerializedName("tag_id")
    private Integer tagId;
    @SerializedName("device_id")
    private Integer deviceId;
    @SerializedName("change")
    private List<Float> feedChange = new ArrayList<Float>();
    @SerializedName("at")
    private Date feedChangeAt;

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public List<Float> getFeedChange() {
        return feedChange;
    }

    public void setFeedChange(List<Float> feedChange) {
        this.feedChange = feedChange;
    }

    public Date getAt() {
        return feedChangeAt;
    }

    public void setAt(Date feedChangeAt) {
        this.feedChangeAt = feedChangeAt;
    }

    public ZonedDateTime getZonedFeedChangeAt() {
        if (feedChangeAt != null) {
            return feedChangeAt.toInstant().atZone(ZoneId.systemDefault());
        } else {
            return null;
        }
    }
}
