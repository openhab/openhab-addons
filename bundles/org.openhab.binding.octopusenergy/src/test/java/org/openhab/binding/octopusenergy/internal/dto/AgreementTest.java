/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;

/**
 * The {@link AgreementTest} is a test class for {@link Agreement}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class AgreementTest {

    private static final ZonedDateTime UDT = OctopusEnergyBindingConstants.UNDEFINED_TIME;

    @Test
    void testGetProduct() {
        // positive tests
        try {
            assertEquals("GREEN-12M-20-09-22", new Agreement("E-1R-GREEN-12M-20-09-22-A", UDT, UDT).getProduct());
            assertEquals("VAR-19-04-12", new Agreement("E-1R-VAR-19-04-12-N", UDT, UDT).getProduct());
            assertEquals("AGILE-18-02-21", new Agreement("E-1R-AGILE-18-02-21-A", UDT, UDT).getProduct());
        } catch (RecordNotFoundException e) {
            fail(e);
        }

        // negative tests
        assertThrows(RecordNotFoundException.class, () -> {
            new Agreement("AGILE1802-21-A", UDT, UDT).getProduct();
        });
        assertThrows(RecordNotFoundException.class, () -> {
            new Agreement("AGILE1802", UDT, UDT).getProduct();
        });
        assertThrows(RecordNotFoundException.class, () -> {
            new Agreement("", UDT, UDT).getProduct();
        });
    }
}
