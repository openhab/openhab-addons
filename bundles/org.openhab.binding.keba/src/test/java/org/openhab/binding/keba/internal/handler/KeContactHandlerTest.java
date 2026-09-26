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
package org.openhab.binding.keba.internal.handler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Thing;

class KeContactHandlerTest {

    @Test
    void ignoresShortProductValuesWithoutCrashingPollingParsing() {
        KeContactHandler handler = new KeContactHandler(mock(Thing.class), mock(KeContactTransceiver.class));
        ByteBuffer response = ByteBuffer.wrap("{\"Product\":\"P30\"}".getBytes());

        assertDoesNotThrow(() -> handler.onData(response));
    }

    @Test
    void ignoresUnknownProductSeriesWithoutCrashingPollingParsing() {
        KeContactHandler handler = new KeContactHandler(mock(Thing.class), mock(KeContactTransceiver.class));
        ByteBuffer response = ByteBuffer.wrap("{\"Product\":\"KC-P30-123456Z-XXX\"}".getBytes());

        assertDoesNotThrow(() -> handler.onData(response));
    }
}
