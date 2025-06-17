/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public class RingEventTO {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    public long id = 0;
    @SerializedName("created_at")
    public String createdAt = "";
    public boolean answered;
    public String kind = "";
    public boolean favorite;
    @SerializedName("snapshot_url")
    public @Nullable String snapshotUrl;
    public Map<String, String> recording = Map.of();
    public List<Object> events = List.of();
    public DoorbotTO doorbot = new DoorbotTO();

    /**
     * Get the date/time created as String.
     *
     * @return the date/time.
     */
    public DateTimeType getCreatedAt() {
        return new DateTimeType(
                ZonedDateTime.parse(createdAt, DATE_TIME_FORMATTER).withZoneSameInstant(ZoneId.systemDefault()));
    }
}
