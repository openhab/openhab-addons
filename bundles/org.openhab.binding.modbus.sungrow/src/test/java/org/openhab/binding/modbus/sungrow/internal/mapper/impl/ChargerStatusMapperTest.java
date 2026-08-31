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
package org.openhab.binding.modbus.sungrow.internal.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
class ChargerStatusMapperTest {

    @Test
    void testMappings() {
        ChargerStatusMapper mapper = ChargerStatusMapper.instance();

        assertEquals("Idle (unplugged)", mapper.map(BigDecimal.ONE));
        assertEquals("Standby (plugged)", mapper.map(BigDecimal.valueOf(2)));
        assertEquals("Charging", mapper.map(BigDecimal.valueOf(3)));
        assertEquals("Charging completed", mapper.map(BigDecimal.valueOf(6)));
    }
}
