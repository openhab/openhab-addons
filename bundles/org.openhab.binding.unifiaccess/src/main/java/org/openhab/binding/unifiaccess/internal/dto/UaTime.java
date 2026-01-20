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
package org.openhab.binding.unifiaccess.internal.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Shared time utilities for UniFi Access DTO helpers.
 *
 * @author Dan Cunningham - Initial contribution
 */
public final class UaTime {
    private UaTime() {
    }

    private static final DateTimeFormatter ISO_ZONED = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter SPACEY = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static Instant fromEpochSeconds(Long epochSeconds) {
        return (epochSeconds == null) ? null : Instant.ofEpochSecond(epochSeconds);
    }

    public static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(s, ISO_ZONED).toInstant();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(s, SPACEY).toInstant(ZoneOffset.UTC);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static LocalTime parseHhmmss(String hhmmss) {
        try {
            return (hhmmss == null || hhmmss.isBlank()) ? null : LocalTime.parse(hhmmss, HHMMSS);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean within(LocalTime t, LocalTime start, LocalTime end) {
        if (t == null || start == null || end == null || start.equals(end)) {
            return false;
        }
        if (start.isBefore(end)) {
            return !t.isBefore(start) && t.isBefore(end);
        }
        // overnight
        return !t.isBefore(start) || t.isBefore(end);
    }
}
