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
package org.openhab.binding.smartthings.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.dto.AppRequest;

/**
 * Tests for {@link SmartThingsApi}.
 */
@NonNullByDefault
class SmartThingsApiTest {

    @Test
    void createAppUsesBrowserReachableOAuthRedirectUri() {
        AppRequest request = SmartThingsApi.createAppRequest("openhab123", "https://openhab.example.org/smartthings/cb",
                "https://openhab.example.org/smartthings");

        assertEquals("https://openhab.example.org/smartthings", request.oauth.redirectUris[0]);
        assertEquals("https://openhab.example.org/smartthings/cb", request.apiOnly.targetUrl());
    }
}
