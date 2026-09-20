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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String METER_PREFIX = "meter:";
    private static final char SEPARATOR = '\t';

    private final Logger logger = LoggerFactory.getLogger(TransactionStore.class);

    private final Storage<String> storage;
    // Guarded by this: increment and persistent write must be one atomic step.
    private int sequence;

    public TransactionStore(Storage<String> storage) {
        this.storage = storage;
        this.sequence = readSequence();
    }

    private int readSequence() {
        String stored = storage.get(SEQUENCE_KEY);
        if (stored != null) {
            try {
                return Integer.parseInt(stored);
            } catch (NumberFormatException e) {
                logger.warn("Persisted transaction sequence '{}' is not a number; numbering restarts at 1", stored);
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
        begin(transactionId, chargePointId, connectorId, null);
    }

    public synchronized void begin(int transactionId, String chargePointId, int connectorId,
            @Nullable Integer meterStart) {
        clear(chargePointId, connectorId);
        storage.put(TX_PREFIX + transactionId, chargePointId + SEPARATOR + connectorId);
        if (meterStart != null) {
            storage.put(METER_PREFIX + transactionId, Integer.toString(meterStart));
        } else {
            storage.remove(METER_PREFIX + transactionId);
        }
    }

    public synchronized void end(int transactionId) {
        storage.remove(TX_PREFIX + transactionId);
        storage.remove(METER_PREFIX + transactionId);
    }

    public synchronized @Nullable Integer meterStart(int transactionId) {
        String stored = storage.get(METER_PREFIX + transactionId);
        if (stored == null) {
            return null;
        }
        try {
            return Integer.valueOf(stored);
        } catch (NumberFormatException e) {
            logger.warn(
                    "Persisted meter start '{}' for transaction {} is not a number; the session reports no " + "energy",
                    stored, transactionId);
            return null;
        }
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
                    logger.warn("Persisted transaction key '{}' has no numeric id; it is treated as absent", key);
                }
            }
        }
        return null;
    }

    private void clear(String chargePointId, int connectorId) {
        for (String key : new ArrayList<>(storage.getKeys())) {
            if (key.startsWith(TX_PREFIX) && matches(storage.get(key), chargePointId, connectorId)) {
                storage.remove(key);
                storage.remove(METER_PREFIX + key.substring(TX_PREFIX.length()));
            }
        }
    }

    private boolean matches(@Nullable String value, String chargePointId, int connectorId) {
        Location location = parse(value);
        return location != null && location.chargePointId().equals(chargePointId)
                && location.connectorId() == connectorId;
    }

    private @Nullable Location parse(@Nullable String value) {
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
            logger.warn("Persisted transaction entry '{}' has no numeric connector; it is treated as absent", value);
            return null;
        }
    }
}
