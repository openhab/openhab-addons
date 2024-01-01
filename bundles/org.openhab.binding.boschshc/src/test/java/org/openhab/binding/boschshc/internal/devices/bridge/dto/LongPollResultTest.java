/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;

/**
 * Unit tests for LongPollResult
 *
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
class LongPollResultTest {

    @Test
    void noResultsForErrorResult() {
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(
                "{\"jsonrpc\":\"2.0\", \"error\": { \"code\":-32001, \"message\":\"No subscription with id: e8fei62b0-0\" } }",
                LongPollResult.class);
        assertNotNull(longPollResult);
        assertEquals(null, longPollResult.result);
    }
}
