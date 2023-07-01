/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.tpm;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.condition.OS.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.openhab.binding.knx.internal.tpm.TpmInterface.SecuredPassword;
import org.openhab.core.auth.SecurityException;

/**
 *
 * @author Holger Friedrich - Initial contribution
 *
 *         This tests are intended to test the interface class for Trusted Platform Modules (TPM).
 *         The tests will succeed, in case no TPM is available during test execution.
 *         Some tests will be skipped on Windows, as some operations require admin access to the TPM.
 *
 */
@NonNullByDefault
class TpmTest {

    @Test
    void testTpmInfo() {
        TpmInterface tpmIf = null;

        try {
            tpmIf = new TpmInterface();
        } catch (SecurityException ignored) {
            // TPM might not be availabe
        }

        if (tpmIf != null) {
            assertDoesNotThrow(tpmIf::getTpmManufacturerShort);
            assertDoesNotThrow(tpmIf::getTpmModel);
            assertDoesNotThrow(tpmIf::getTpmFirmwareVersion);
            assertDoesNotThrow(tpmIf::getTpmTcgLevel);
            assertDoesNotThrow(tpmIf::getTpmTcgRevision);
            assertDoesNotThrow(tpmIf::getTpmVersion);
        }
    }

    /**
     * Test encryption and decryption. Skipped on Windows as admin access seems to be required.
     */
    @Test
    @DisabledOnOs(WINDOWS) // this test fails on Windows as user
    void testTpmEncDec() {
        TpmInterface tpmIf = null;
        SecuredPassword sPwd = null;

        try {
            tpmIf = new TpmInterface();
        } catch (SecurityException ignored) {
            // TPM might not be availabe
        }

        if (tpmIf != null) {
            try {
                final String secret = "password";
                sPwd = tpmIf.encryptSecret(secret);

                TpmInterface tpmIf2 = null;
                try {
                    tpmIf2 = new TpmInterface();
                } catch (SecurityException e) {
                    assertEquals("", e.toString());
                }

                assertNotEquals(null, tpmIf2);

                if (tpmIf2 != null) { // always true, avoid warning
                    assertEquals(secret, tpmIf2.decryptSecret(sPwd));
                }
            } catch (SecurityException e) {
                assertEquals("", e.toString() + " " + Objects.toString(e.getCause(), ""));
            }
        }
    }

    @Test
    void testTpmRandom() {
        TpmInterface tpmIf = null;

        try {
            tpmIf = new TpmInterface();
        } catch (SecurityException ignored) {
            // TPM might not be availabe
        }

        if (tpmIf != null) {
            byte[] r = tpmIf.getRandom(20);
            assertEquals(20, r.length);
        }
    }
}
