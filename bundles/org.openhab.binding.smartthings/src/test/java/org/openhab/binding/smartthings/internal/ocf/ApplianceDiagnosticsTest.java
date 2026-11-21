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
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

/**
 * Appliance diagnostic redaction tests.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class ApplianceDiagnosticsTest {
    @Test
    void redactsSensitiveFieldsRecursivelyAndPreservesCapabilityDetails() {
        var input = JsonParser.parseString("""
                {"di":"device-uuid","n":"Kitchen Dryer","mnmo":"DV90","mnfv":"1.2",
                 "links":[{"href":"/power/0","rep":{"value":true,"ownerPSK":"secret",
                 "x.com.samsung.da.ownerId":"owner-uuid","password":"secret","access_token":"secret",
                 "serial_number":"123"}}]}
                """);

        String sanitized = ApplianceDiagnostics.sanitize(input).toString();

        assertTrue(sanitized.contains("\"mnmo\":\"DV90\""));
        assertTrue(sanitized.contains("\"mnfv\":\"1.2\""));
        assertTrue(sanitized.contains("\"value\":true"));
        assertEquals(7, sanitized.split("\\[REDACTED\\]", -1).length - 1);
        assertFalse(sanitized.contains("device-uuid"));
        assertFalse(sanitized.contains("Kitchen Dryer"));
        assertFalse(sanitized.contains("owner-uuid"));
        assertFalse(sanitized.contains("secret"));
        assertFalse(sanitized.contains("123"));
    }
}
