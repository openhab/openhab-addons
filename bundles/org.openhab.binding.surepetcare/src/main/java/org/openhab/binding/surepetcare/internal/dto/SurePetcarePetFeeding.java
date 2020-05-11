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
    public Long tagId;
    @SerializedName("device_id")
    public Long deviceId;
    @SerializedName("change")
    public List<Float> feedChange = new ArrayList<>();
    @SerializedName("at")
    public Date feedChangeAt;

    public ZonedDateTime getZonedFeedChangeAt() {
        return feedChangeAt == null ? null : feedChangeAt.toInstant().atZone(ZoneId.systemDefault());
    }
}
