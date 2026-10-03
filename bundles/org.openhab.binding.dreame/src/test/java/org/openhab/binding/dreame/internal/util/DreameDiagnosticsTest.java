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
package org.openhab.binding.dreame.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class DreameDiagnosticsTest {

    @Test
    void masksLoginFormSecrets() {
        String sanitized = DreameDiagnostics
                .sanitize("grant_type=password&username=user@example.com&password=0123456789abcdef&type=account");

        assertEquals("grant_type=password&username=***&password=***&type=account", sanitized);
    }

    @Test
    void recursivelyMasksJsonSecretsAndDeviceIdentifiers() {
        String sanitized = DreameDiagnostics.sanitize(
                """
                        {"access_token":"access-secret","jti":"session-secret","data":{"id":"record-secret","did":"123456789","masterUid":"owner-secret","name":"Garden","mac":"10:20:30:40:50:60","sn":"serial-secret"},
                         "records":[{"property":"device-secret","refresh_token":"refresh-secret"}]}
                        """);

        assertFalse(sanitized.contains("access-secret"));
        assertFalse(sanitized.contains("owner-secret"));
        assertFalse(sanitized.contains("device-secret"));
        assertFalse(sanitized.contains("refresh-secret"));
        assertFalse(sanitized.contains("Garden"));
        assertFalse(sanitized.contains("session-secret"));
        assertFalse(sanitized.contains("record-secret"));
        assertFalse(sanitized.contains("10:20:30:40:50:60"));
        assertFalse(sanitized.contains("serial-secret"));
        assertTrue(sanitized.contains("***6789"));
    }

    @Test
    void doesNotReturnMalformedPayloadsUnredacted() {
        assertEquals("***", DreameDiagnostics.sanitize("{secret payload"));
    }
}
