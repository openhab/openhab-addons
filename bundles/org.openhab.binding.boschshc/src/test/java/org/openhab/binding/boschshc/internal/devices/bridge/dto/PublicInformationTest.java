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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link PublicInformation}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class PublicInformationTest {

    private @NonNullByDefault({}) PublicInformation fixture;

    @BeforeEach
    void beforeEach() {
        fixture = new PublicInformation();
        fixture.shcIpAddress = "192.168.0.123";
        fixture.macAddress = "64-da-a0-ab-cd-ef";
        fixture.shcGeneration = "SHC_1";
        fixture.apiVersions = List.of("2.9", "3.2");
    }

    @Test
    void getApiVersionsAsCommaSeparatedList() {
        assertEquals("2.9, 3.2", fixture.getApiVersionsAsCommaSeparatedList());
        fixture.apiVersions = null;
        assertNull(fixture.getApiVersionsAsCommaSeparatedList());
    }
}
