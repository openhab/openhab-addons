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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;

/**
 * Persists transaction state so it survives an openHAB restart. Without this, a restart during an
 * active transaction restarts the id counter from scratch (risking an id the charger still holds),
 * loses the transaction-to-connector mapping needed to route the eventual StopTransaction, and loses
 * the transaction id a connector needs for RemoteStop and a TxProfile.
 *
 * <p>
 * Backed by an openHAB {@link Storage} keyed on the server bridge, holding a monotonic id counter
 * plus one entry per open transaction ({@code tx:<id> -> <chargePointId>\t<connectorId>}).
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TransactionStore {

    /** Where a transaction is running: which connector of which charge point. */
    public record Location(String chargePointId, int connectorId) {
    }

    private static final String SEQUENCE_KEY = "sequence";
    private static final String TX_PREFIX = "tx:";
    private static final char SEPARATOR = '\t';

    private final Storage<String> storage;
    // Guarded by this: the increment and its persistent write must be one atomic step. With separate
    // atomicity (e.g. an AtomicInteger plus an unsynchronized put) two concurrent allocations can
    // persist out of order, storing the LOWER id last — and after a restart the sequence would
    // resume below an id a charger still holds.
    private int sequence;

    public TransactionStore(Storage<String> storage) {
        this.storage = storage;
        this.sequence = readSequence(storage);
    }

    private static int readSequence(Storage<String> storage) {
        String stored = storage.get(SEQUENCE_KEY);
        if (stored != null) {
            try {
                return Integer.parseInt(stored);
            } catch (NumberFormatException e) {
                // fall through to 0
            }
        }
        return 0;
    }

    /**
     * The next transaction id, resumed across restarts so it never reissues an id a charger may
     * still hold for a transaction that outlived openHAB. Allocation and the persistent write happen
     * under one lock so the stored value can only ever increase — with separate atomicity, two
     * concurrent allocations can persist out of order and store the lower id last.
     */
    public synchronized int nextTransactionId() {
        int id = ++sequence;
        storage.put(SEQUENCE_KEY, Integer.toString(id));
        return id;
    }

    /**
     * Record an open transaction. Any earlier open transaction on the same connector is dropped
     * first — a connector has at most one at a time, and a StopTransaction that never arrived would
     * otherwise leave a stale entry behind.
     */
    public synchronized void begin(int transactionId, String chargePointId, int connectorId) {
        clear(chargePointId, connectorId);
        storage.put(TX_PREFIX + transactionId, chargePointId + SEPARATOR + connectorId);
    }

    /** Forget a transaction once it has stopped. */
    public synchronized void end(int transactionId) {
        storage.remove(TX_PREFIX + transactionId);
    }

    /** Where a transaction is running, or {@code null} if it is not known. */
    public synchronized @Nullable Location locate(int transactionId) {
        return parse(storage.get(TX_PREFIX + transactionId));
    }

    /** The open transaction id on a connector, or {@code null} if none — used to recover after a restart. */
    public synchronized @Nullable Integer openTransaction(String chargePointId, int connectorId) {
        for (String key : storage.getKeys()) {
            if (key.startsWith(TX_PREFIX) && matches(storage.get(key), chargePointId, connectorId)) {
                try {
                    return Integer.parseInt(key.substring(TX_PREFIX.length()));
                } catch (NumberFormatException e) {
                    // skip a malformed key
                }
            }
        }
        return null;
    }

    private void clear(String chargePointId, int connectorId) {
        for (String key : new ArrayList<>(storage.getKeys())) {
            if (key.startsWith(TX_PREFIX) && matches(storage.get(key), chargePointId, connectorId)) {
                storage.remove(key);
            }
        }
    }

    private static boolean matches(@Nullable String value, String chargePointId, int connectorId) {
        Location location = parse(value);
        return location != null && location.chargePointId().equals(chargePointId)
                && location.connectorId() == connectorId;
    }

    private static @Nullable Location parse(@Nullable String value) {
        if (value == null) {
            return null;
        }
        // chargePointId is stored first and the connector id (a plain integer) last, so splitting on
        // the last separator keeps a charge point id that happens to contain the separator intact.
        int split = value.lastIndexOf(SEPARATOR);
        if (split < 0) {
            return null;
        }
        try {
            return new Location(value.substring(0, split), Integer.parseInt(value.substring(split + 1)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
