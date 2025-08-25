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
package org.openhab.binding.myenergi.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myenergi.internal.exception.RecordNotFoundException;

/**
 * The {@link MyenergiDataTest} is a test class for {@link MyEnergiData}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class MyenergiDataTest {

    private static final MyenergiData BASE = new MyenergiData();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        ZappiSummary z1 = new ZappiSummary(123);
        z1.firmwareVersion = "1";
        BASE.addZappi(z1);
    }

    @Test
    void testUpdateZappi() throws RecordNotFoundException {
        assertEquals(1, BASE.getZappis().size());
        ZappiSummary z2 = new ZappiSummary(124);
        z2.firmwareVersion = "1";
        BASE.updateZappi(z2);

        assertEquals(2, BASE.getZappis().size());
        ZappiSummary z3 = new ZappiSummary(123);
        z3.firmwareVersion = "2";
        BASE.updateZappi(z3);

        assertEquals(2, BASE.getZappis().size());
        assertEquals("2", BASE.getZappiBySerialNumber(123).firmwareVersion);
    }
}
