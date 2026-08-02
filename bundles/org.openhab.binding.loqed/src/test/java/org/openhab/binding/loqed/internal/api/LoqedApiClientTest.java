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
package org.openhab.binding.loqed.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests parsing responses from the LOQED Integrations API.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedApiClientTest {
    @Test
    public void rejectsMalformedResponse() {
        assertThrows(LoqedResponseException.class, () -> LoqedApiClient.parseLocks("{"));
    }

    @Test
    public void rejectsMissingOrNullData() {
        assertThrows(LoqedResponseException.class, () -> LoqedApiClient.parseLocks("{}"));
        assertThrows(LoqedResponseException.class, () -> LoqedApiClient.parseLocks("{\"data\":null}"));
    }

    @Test
    public void convertsUnknownBoltStateToUnknown() throws Exception {
        List<LoqedLockData> locks = LoqedApiClient
                .parseLocks("{\"data\":[{\"id\":\"lock-id\",\"bolt_state\":\"future_state\"}]}");

        assertEquals(1, locks.size());
        assertEquals(BoltState.UNKNOWN, locks.getFirst().boltState);
    }
}
