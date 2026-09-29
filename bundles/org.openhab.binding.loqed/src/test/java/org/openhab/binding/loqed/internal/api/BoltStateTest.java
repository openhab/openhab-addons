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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

/**
 * Tests conversion between LOQED API values and typed bolt states.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class BoltStateTest {
    @Test
    public void convertsApiValues() {
        assertEquals(BoltState.OPEN, BoltState.fromApiValueOrUnknown("open"));
        assertEquals(BoltState.DAY_LOCK, BoltState.fromApiValueOrUnknown("DAY_LOCK"));
        assertEquals(BoltState.NIGHT_LOCK, BoltState.fromApiValueOrUnknown("night_lock"));
        assertEquals(BoltState.UNKNOWN, BoltState.fromApiValueOrUnknown("invalid"));
    }

    @Test
    public void convertsLocalLatchAlias() {
        assertEquals(BoltState.DAY_LOCK, BoltState.fromApiValueOrUnknown("latch"));
        assertTrue(BoltState.fromApiValue("latch").filter(state -> state == BoltState.DAY_LOCK).isPresent());
    }

    @Test
    public void deserializesCloudApiValue() {
        LoqedLockData lockData = Objects
                .requireNonNull(new Gson().fromJson("{\"bolt_state\":\"night_lock\"}", LoqedLockData.class));

        assertEquals(BoltState.NIGHT_LOCK, lockData.boltState);
    }
}
