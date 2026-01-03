/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluelink.internal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public final class MockApiData {
    public static final String TOKEN_RESPONSE = """
            {
                "token_type": "Bearer",
                "access_token": "test-access-token",
                "refresh_token": "test-refresh-token",
                "expires_in": "3600",
                "username": "test@example.com"
            }
            """;
    public static final String EU_DEVICE_REGISTRATION_RESPONSE = """
            {
              "retCode" : "S",
              "resCode" : "0000",
              "resMsg" : {
                "deviceId" : "122c2e30-d642-4d34-ba07-7ce7d787349a"
              },
              "msgId" : "5b101b3b-2b30-4f55-8920-5b7e3763beed"
            }
            """;

    public static final String US_ENROLLMENT_RESPONSE;
    public static final String US_VEHICLE_STATUS_RESPONSE;
    public static final String EU_VEHICLES_RESPONSE;
    public static final String EU_VEHICLE_STATUS_RESPONSE;
    public static final String TEST_USERNAME = "test@example.com";
    public static final String TEST_PASSWORD = "testpassword";
    public static final String TEST_REFRESH_TOKEN = "test-refresh-token";

    static {
        try {
            US_ENROLLMENT_RESPONSE = loadResource("/us/enrollment-details.json");
            US_VEHICLE_STATUS_RESPONSE = loadResource("/us/vehicle-status.json");
            EU_VEHICLES_RESPONSE = loadResource("/eu/vehicles.json");
            EU_VEHICLE_STATUS_RESPONSE = loadResource("/eu/vehicle-status.json");
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String loadResource(final String path) throws IOException {
        try (final var in = MockApiData.class.getResourceAsStream(path)) {
            if (in != null) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new IOException("cannot load resource: %s".formatted(path));
            }
        }
    }
}
