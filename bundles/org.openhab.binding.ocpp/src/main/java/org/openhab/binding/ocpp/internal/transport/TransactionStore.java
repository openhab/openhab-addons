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
 * Persists transaction state so it survives an openHAB restart.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class TransactionStore {

    public record Location(String chargePointId, int connectorId) {
    }

    private static final String SEQUENCE_KEY = "sequence";
    private static final String TX_PREFIX = "tx:";
    private static final char SEPARATOR = '\t';

    private final Storage<String> storage;
    // Guarded by this: increment and persistent write must be one atomic step.
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
            }
        }
        return 0;
    }

    public synchronized int nextTransactionId() {
        int id = ++sequence;
        storage.put(SEQUENCE_KEY, Integer.toString(id));
        return id;
    }

    public synchronized void begin(int transactionId, String chargePointId, int connectorId) {
        clear(chargePointId, connectorId);
        storage.put(TX_PREFIX + transactionId, chargePointId + SEPARATOR + connectorId);
    }

    public synchronized void end(int transactionId) {
        storage.remove(TX_PREFIX + transactionId);
    }

    public synchronized @Nullable Location locate(int transactionId) {
        return parse(storage.get(TX_PREFIX + transactionId));
    }

    public synchronized @Nullable Integer openTransaction(String chargePointId, int connectorId) {
        for (String key : storage.getKeys()) {
            if (key.startsWith(TX_PREFIX) && matches(storage.get(key), chargePointId, connectorId)) {
                try {
                    return Integer.parseInt(key.substring(TX_PREFIX.length()));
                } catch (NumberFormatException e) {
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
        // Split on the last separator so a chargePointId containing one stays intact.
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
