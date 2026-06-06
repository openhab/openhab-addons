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
    public static final String TOKEN_RESPONSE_CA;
    public static final String TOKEN_RESPONSE_US;
    public static final String TOKEN_RESPONSE_EU;
    public static final String ENROLLMENT_RESPONSE_US;
    public static final String VEHICLES_RESPONSE_EU;
    public static final String DEVICE_REGISTRATION_RESPONSE_EU;
    public static final String VEHICLE_STATUS_RESPONSE_CA;
    public static final String VEHICLE_STATUS_RESPONSE_US;
    public static final String VEHICLE_STATUS_RESPONSE_EU;
    public static final String TEST_USERNAME = "test@example.com";
    public static final String TEST_PASSWORD = "testpassword";
    public static final String TEST_REFRESH_TOKEN = "test-refresh-token";

    static {
        try {
            TOKEN_RESPONSE_CA = loadResource("/login-response-ca.json");
            TOKEN_RESPONSE_US = loadResource("/login-response-us.json");
            TOKEN_RESPONSE_EU = loadResource("/login-response-eu.json");
            ENROLLMENT_RESPONSE_US = loadResource("/enrollment-details-us.json");
            VEHICLES_RESPONSE_EU = loadResource("/vehicles-eu.json");
            DEVICE_REGISTRATION_RESPONSE_EU = loadResource("/device-registration-eu.json");
            VEHICLE_STATUS_RESPONSE_US = loadResource("/vehicle-status-us.json");
            VEHICLE_STATUS_RESPONSE_CA = loadResource("/vehicle-status-ca.json");
            VEHICLE_STATUS_RESPONSE_EU = loadResource("/vehicle-status-eu.json");
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
