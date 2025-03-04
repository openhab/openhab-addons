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
package org.openhab.binding.tado.internal.auth.oauth;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceCodeResponse} contains a response from the {@link DeviceCodeGrantFlowService}
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class DeviceCodeResponse {

    private static final String STRING_DELIMITER = ";";

    public static DeviceCodeResponse fromString(String delimitedString) {
        String[] values = delimitedString.split(STRING_DELIMITER);
        if (values.length == 4) {
            DeviceCodeResponse result = new DeviceCodeResponse("", "", 0, 0);
            result.deviceCode = values[0];
            result.userUri = values[1];
            result.interval = Duration.parse(values[2]);
            result.expireTime = Instant.parse(values[3]);
            return result;
        }
        throw new IllegalArgumentException(delimitedString);
    }

    private String deviceCode;
    private String userUri;
    private Duration interval;
    private Instant expireTime;

    public DeviceCodeResponse(String deviceCode, String userUri, long expiresIn, long interval) {
        this.deviceCode = deviceCode;
        this.expireTime = Instant.now().plusSeconds(expiresIn);
        this.interval = Duration.ofSeconds(interval);
        this.userUri = userUri;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public Duration getInterval() {
        return interval;
    }

    public String getUserUri() {
        return userUri;
    }

    public boolean isExpired(Instant givenTime) {
        return givenTime.isAfter(expireTime);
    }

    @Override
    public String toString() {
        return String.join(STRING_DELIMITER, deviceCode, userUri, interval.toString(), expireTime.toString());
    }
}
