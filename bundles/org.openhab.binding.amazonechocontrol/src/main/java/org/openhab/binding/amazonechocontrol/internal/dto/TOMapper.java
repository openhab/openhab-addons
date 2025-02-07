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
package org.openhab.binding.amazonechocontrol.internal.dto;

import static org.eclipse.jetty.util.StringUtil.isNotBlank;

import java.net.HttpCookie;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.types.Notification;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link TOMapper} contains mappers for TOs
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class TOMapper {
    @SuppressWarnings("unchecked")
    private static final TypeToken<Map<String, Object>> MAP_TYPE_TOKEN = (TypeToken<Map<String, Object>>) TypeToken
            .getParameterized(Map.class, String.class, Object.class);

    private TOMapper() {
        // prevent instantiation
    }

    public static Map<String, Object> mapToMap(Gson gson, Object o) {
        String json = gson.toJson(o);
        return Objects.requireNonNullElse(gson.fromJson(json, MAP_TYPE_TOKEN), Map.of());
    }

    public static DeviceIdTO mapAnnouncementTargetDevice(DeviceTO device) {
        DeviceIdTO targetDevice = new DeviceIdTO();
        targetDevice.deviceTypeId = device.deviceType;
        targetDevice.deviceSerialNumber = device.serialNumber;
        return targetDevice;
    }

    public static CookieTO mapCookie(HttpCookie httpCookie) {
        CookieTO cookie = new CookieTO();
        cookie.name = httpCookie.getName();
        cookie.value = httpCookie.getValue();
        cookie.secure = String.valueOf(httpCookie.getSecure());
        cookie.httpOnly = String.valueOf(httpCookie.isHttpOnly());
        return cookie;
    }

    public static HttpCookie mapCookie(CookieTO cookie, String domain) {
        HttpCookie httpCookie = new HttpCookie(cookie.name, cookie.value);
        httpCookie.setPath(cookie.path);
        httpCookie.setDomain(domain);
        String secure = cookie.secure;
        if (secure != null) {
            httpCookie.setSecure(Boolean.getBoolean(secure));
        }
        return httpCookie;
    }

    public static @Nullable Notification map(NotificationTO notification, ZonedDateTime requestTime,
            ZonedDateTime now) {
        if (!"ON".equals(notification.status) || notification.deviceSerialNumber == null) {
            return null;
        }
        ZonedDateTime alarmTime;
        if ("Reminder".equals(notification.type) || "Alarm".equals(notification.type)
                || "MusicAlarm".equals(notification.type)) {
            LocalDate localDate = isNotBlank(notification.originalDate) ? LocalDate.parse(notification.originalDate)
                    : now.toLocalDate();
            LocalTime localTime = isNotBlank(notification.originalTime) ? LocalTime.parse(notification.originalTime)
                    : LocalTime.MIDNIGHT;
            ZonedDateTime originalTime = ZonedDateTime.of(localDate, localTime, ZoneId.systemDefault());

            if (notification.alarmTime == 0 || !isNotBlank(notification.recurringPattern)) {
                alarmTime = originalTime;
            } else {
                // the alarm time needs to be DST adjusted
                alarmTime = Instant.ofEpochMilli(notification.alarmTime).atZone(ZoneId.systemDefault());
                int alarmOffset = originalTime.getOffset().getTotalSeconds() - alarmTime.getOffset().getTotalSeconds();
                alarmTime = alarmTime.plusSeconds(alarmOffset);
            }
        } else if ("Timer".equals(notification.type) && notification.remainingTime > 0) {
            alarmTime = requestTime.plus(notification.remainingTime, ChronoUnit.MILLIS);
        } else {
            return null;
        }
        return new Notification(notification.deviceSerialNumber, notification.type, alarmTime);
    }
}
