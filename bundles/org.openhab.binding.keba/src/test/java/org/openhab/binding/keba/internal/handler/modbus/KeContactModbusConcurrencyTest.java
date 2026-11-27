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
package org.openhab.binding.keba.internal.handler.modbus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * Tests for Modbus lifecycle and scheduling decisions.
 *
 * @author Michael Weger - Initial contribution
 */
class KeContactModbusConcurrencyTest {

    @Test
    void staleModbusGenerationIsRejected() {
        assertTrue(KeContactModbusHandler.isStaleGeneration(1, 2));
        assertFalse(KeContactModbusHandler.isStaleGeneration(2, 2));
    }

    @Test
    void writeSpacingRequiresTheFullProtocolInterval() {
        long interval = TimeUnit.SECONDS.toNanos(5);
        assertFalse(KeContactModbusHandler.isWriteIntervalElapsed(interval - 1, interval));
        assertTrue(KeContactModbusHandler.isWriteIntervalElapsed(interval, interval));
        assertTrue(KeContactModbusHandler.isWriteIntervalElapsed(interval + 1, interval));
    }
}
