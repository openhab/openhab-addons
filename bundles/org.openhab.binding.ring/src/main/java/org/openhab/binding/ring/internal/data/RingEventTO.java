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
package org.openhab.binding.ring.internal.data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public class RingEventTO {
    public long id = 0;
    public String createdAt = "";
    public boolean answered;
    public String kind = "";
    public boolean favorite;
    public @Nullable String snapshotUrl;
    public Map<String, String> recording = Map.of();
    public List<Object> events = List.of();
    public DoorbotTO doorbot = new DoorbotTO();

    /**
     * Get the date/time created as String.
     *
     * @return the date/time.
     */
    public String getCreatedAt() {
        ZonedDateTime gmtTime = LocalDateTime
                .parse(createdAt, DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ss.SSS'Z'")).atZone(ZoneId.of("GMT"));
        LocalDateTime localTime = gmtTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return localTime.toString();
    }
}
