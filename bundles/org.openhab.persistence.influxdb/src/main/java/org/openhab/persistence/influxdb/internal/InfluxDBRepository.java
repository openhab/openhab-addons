/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
     * @return Map with <ItemName,ItemCount> entries
     */
    Map<String, Integer> getStoredItemsCount();

    /**
     * Executes Flux query
     *
     * @param query Query
     * @return Query results
     * 
     */
    List<InfluxRow> query(String query);

    /**
     * Write point to database
     *
     * @param influxPoint Point to write
     * @throws UnexpectedConditionException when an error occurs
     */
    void write(InfluxPoint influxPoint) throws UnexpectedConditionException;

    /**
     * create a query creator on this repository
     *
     * @return the query creator for this repository
     */
    FilterCriteriaQueryCreator createQueryCreator();

    record InfluxRow(Instant time, String itemName, Object value) {
    }
}
