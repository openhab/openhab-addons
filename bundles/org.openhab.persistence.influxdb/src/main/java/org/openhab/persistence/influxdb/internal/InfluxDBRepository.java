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
package org.openhab.persistence.influxdb.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.FilterCriteria;

/**
 * Manages InfluxDB server interaction maintaining client connection
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Eugen Freiter - Classify write results to drop rejected points
 */
@NonNullByDefault
public interface InfluxDBRepository {
    /**
     * Returns if the client is successfully connected to server
     *
     * @return True if it's connected, otherwise false
     */
    boolean isConnected();

    /**
     * Connect to InfluxDB server
     *
     * @return <code>true</code> if successful, otherwise <code>false</code>
     */
    boolean connect();

    /**
     * Disconnect from InfluxDB server
     */
    void disconnect();

    /**
     * Check if connection is currently ready
     *
     * @return True if it's ready, otherwise false
     */
    boolean checkConnectionStatus();

    /**
     * Return all stored item names with its count of stored points
     *
     * @return Map with {@code <ItemName,ItemCount>}
     * @throws UnsupportedOperationException if influxDB persistence service version implementation does not support
     *             this method
     */
    Map<String, Integer> getStoredItemsCount() throws UnsupportedOperationException;

    /**
     * Executes Flux query
     *
     * @param filter the query filter
     * @param alias
     * @return Query results
     *
     */
    List<InfluxRow> query(FilterCriteria filter, String retentionPolicy, @Nullable String alias);

    /**
     * Write points to database
     *
     * @param influxPoints {@link List<InfluxPoint>} to write
     * @return the {@link WriteResult}, telling whether the points were written, should be retried or were rejected
     */
    WriteResult write(List<InfluxPoint> influxPoints);

    /**
     * Execute delete query
     *
     * @param filter the query filter
     * @return <code>true</code> if query executed successfully, <code>false</code> otherwise
     */
    boolean remove(FilterCriteria filter);

    record InfluxRow(Instant time, String itemName, Object value) {
    }

    /**
     * Result of {@link #write(List)}
     *
     * @param outcome what happened to the points
     * @param message the error reported by the client or the server, empty on success
     */
    record WriteResult(Outcome outcome, String message) {

        public static final WriteResult SUCCESS = new WriteResult(Outcome.SUCCESS, "");

        public enum Outcome {
            /** The points have been written. */
            SUCCESS,
            /** The write failed for a reason that may go away, e.g. a connection problem; retry the points. */
            RETRY,
            /**
             * The server refused the points because of their content, e.g. a field type conflict; writing the same
             * points again will fail again. InfluxDB still stores the valid points of a partial write.
             */
            REJECTED
        }

        public static WriteResult retry(String message) {
            return new WriteResult(Outcome.RETRY, message);
        }

        public static WriteResult rejected(String message) {
            return new WriteResult(Outcome.REJECTED, message);
        }
    }
}
