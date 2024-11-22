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
package org.openhab.binding.growatt.internal.dto;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link GrottDevice} is a DTO containing data fields received from the Grott application.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrottDevice {

    // @formatter:off
    public static final Type GROTT_DEVICE_ARRAY = new TypeToken<ArrayList<GrottDevice>>() {}.getType();
    // @formatter:on

    private @Nullable @SerializedName("device") String deviceId;
    private @Nullable @SerializedName("time") String timeStamp;
    private @Nullable GrottValues values;

    public String getDeviceId() {
        String deviceId = this.deviceId;
        return deviceId != null ? deviceId : "unknown";
    }

    public @Nullable GrottValues getValues() {
        return values;
    }

    public @Nullable Instant getTimeStamp() {
        try {
            return ZonedDateTime.of(LocalDateTime.parse(Objects.requireNonNull(timeStamp)), ZoneId.systemDefault())
                    .toInstant();
        } catch (NullPointerException | DateTimeException e) {
        }
        return null;
    }
}
