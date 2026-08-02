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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ocpp.internal.transport.TransactionStore.Location;
import org.openhab.core.storage.Storage;

/**
 * Tests that {@link TransactionStore} survives a restart: ids keep climbing rather than colliding
 * with a transaction a charger still holds, and the transaction-to-connector mapping persists.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TransactionStoreTest {

    /** A minimal in-memory Storage that outlives a store instance, standing in for a restart. */
    private static final class MemoryStorage implements Storage<String> {
        private final Map<String, String> map = new HashMap<>();

        @Override
        public @Nullable String put(String key, @Nullable String value) {
            return value == null ? map.remove(key) : map.put(key, value);
        }

        @Override
        public @Nullable String remove(String key) {
            return map.remove(key);
        }

        @Override
        public boolean containsKey(String key) {
            return map.containsKey(key);
        }

        @Override
        public @Nullable String get(String key) {
            return map.get(key);
        }

        @Override
        public Collection<String> getKeys() {
            return new HashSet<>(map.keySet());
        }

        @Override
        public Collection<@Nullable String> getValues() {
            return new ArrayList<>(map.values());
        }
    }

    @Test
    void idsAreMonotonicAndResumeAfterARestart() {
        MemoryStorage storage = new MemoryStorage();
        TransactionStore store = new TransactionStore(storage);
        assertEquals(1, store.nextTransactionId());
        assertEquals(2, store.nextTransactionId());
        assertEquals(3, store.nextTransactionId());

        // A fresh store on the same backing storage is the restart: ids must continue, not reset to 1
        // and risk reissuing an id a charger still holds.
        TransactionStore afterRestart = new TransactionStore(storage);
        assertEquals(4, afterRestart.nextTransactionId());
    }

    @Test
    void anOpenTransactionCanBeLocatedAndRecovered() {
        MemoryStorage storage = new MemoryStorage();
        TransactionStore store = new TransactionStore(storage);
        store.begin(7, "charx", 2);

        assertEquals(new Location("charx", 2), store.locate(7));
        assertEquals(Integer.valueOf(7), store.openTransaction("charx", 2));
        // A different connector has nothing open.
        assertNull(store.openTransaction("charx", 1));

        // The mapping is what a restart reads back to route a late StopTransaction.
        assertEquals(new Location("charx", 2), new TransactionStore(storage).locate(7));
    }

    @Test
    void endForgetsTheTransaction() {
        MemoryStorage storage = new MemoryStorage();
        TransactionStore store = new TransactionStore(storage);
        store.begin(7, "charx", 2);
        store.end(7);

        assertNull(store.locate(7));
        assertNull(store.openTransaction("charx", 2));
    }

    @Test
    void aNewTransactionOnAConnectorDropsAStaleOne() {
        MemoryStorage storage = new MemoryStorage();
        TransactionStore store = new TransactionStore(storage);
        store.begin(7, "charx", 2);
        // A StopTransaction for 7 never arrived; a new transaction starts on the same connector.
        store.begin(9, "charx", 2);

        assertNull(store.locate(7), "the stale transaction must not linger");
        assertEquals(Integer.valueOf(9), store.openTransaction("charx", 2));
    }
}
