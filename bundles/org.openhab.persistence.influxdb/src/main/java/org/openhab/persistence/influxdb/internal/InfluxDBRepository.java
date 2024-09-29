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
package org.openhab.persistence.influxdb.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.FilterCriteria;

/**
 * Manages InfluxDB server interaction maintaining client connection
 *
 * @author Joan Pujol Espinar - Initial contribution
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
     * @return Map with {@code <ItemName,ItemCount>} entries
     */
    Map<String, Integer> getStoredItemsCount();

    /**
     * Executes Flux query
     *
     * @param filter the query filter
     * @return Query results
     * 
     */
    List<InfluxRow> query(FilterCriteria filter, String retentionPolicy);

    /**
     * Write points to database
     *
     * @param influxPoints {@link List<InfluxPoint>} to write
     * @return <code>true</code> if points have been written, <code>false</code> otherwise
     */
    boolean write(List<InfluxPoint> influxPoints);

    /**
     * Execute delete query
     *
     * @param filter the query filter
     * @return <code>true</code> if query executed successfully, <code>false</code> otherwise
     */
    boolean remove(FilterCriteria filter);

    record InfluxRow(Instant time, String itemName, Object value) {
    }
}
