/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb2.internal;

import static org.openhab.persistence.influxdb2.internal.InfluxDBConstants.TAG_ITEM_NAME;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.client.*;
import com.influxdb.client.domain.Ready;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

/**
 * Manages InfluxDB server interaction maintaining client connection
 * 
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDBRepository {
    private final Logger logger = LoggerFactory.getLogger(InfluxDBRepository.class);
    private InfluxDBConfiguration configuration;
    @Nullable
    private InfluxDBClient client;
    @Nullable
    private QueryApi queryAPI;
    @Nullable
    private WriteApi writeAPI;

    public InfluxDBRepository(InfluxDBConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns if the client has been successfully connected to server
     * 
     * @return True if it's connected, otherwise false
     */
    public boolean isConnected() {
        return client != null;
    }

    /**
     * Connect to InfluxDB server
     * 
     * @return True if successful, otherwise false
     */
    public boolean connect() {
        InfluxDBClientOptions clientOptions = InfluxDBClientOptions.builder().url(configuration.getUrl())
                .authenticateToken(configuration.getTokenAsCharArray()).org(configuration.getOrganization())
                .bucket(configuration.getBucket()).build();

        final InfluxDBClient createdClient = InfluxDBClientFactory.create(clientOptions);
        this.client = createdClient;
        logger.debug("Succesfully connected to InfluxDB. Instance ready={}", createdClient.ready());
        queryAPI = createdClient.getQueryApi();
        writeAPI = createdClient.getWriteApi();
        return true;
    }

    /**
     * Disconnect from InfluxDB server
     */
    public void disconnect() {
        final InfluxDBClient currentClient = this.client;
        if (currentClient != null)
            currentClient.close();
        this.client = null;
    }

    /**
     * Check if connection is currently ready
     * 
     * @return True if its ready, otherwise false
     */
    public boolean checkConnectionStatus() {
        final InfluxDBClient currentClient = client;
        if (currentClient != null) {
            Ready ready = currentClient.ready();
            boolean isUp = ready != null && ready.getStatus() == Ready.StatusEnum.READY;
            if (isUp) {
                logger.debug("database status is OK");
            } else {
                logger.warn("database not ready");
            }
            return isUp;
        } else {
            logger.warn("checkConnection: database is not connected");
            return false;
        }
    }

    /**
     * Write point to database
     * 
     * @param point
     */
    public void write(Point point) {
        final WriteApi currentWriteAPI = writeAPI;
        if (currentWriteAPI != null)
            currentWriteAPI.writePoint(point);
        else
            logger.error("Write point {} ignored due to writeAPI isn't present", point);
    }

    /**
     * Executes Flux query
     * 
     * @param query Query
     * @return Query results
     */
    public List<FluxTable> query(String query) {
        final QueryApi currentQueryAPI = queryAPI;
        if (currentQueryAPI != null)
            return currentQueryAPI.query(query);
        else {
            logger.error("Returning empty list because queryAPI isn't present");
            return Collections.emptyList();
        }
    }

    /**
     * Return all stored item names with it's count of stored points
     * 
     * @return Map with <ItemName,ItemCount> entries
     */
    public Map<String, Integer> getStoredItemsCount() {
        final QueryApi currentQueryAPI = queryAPI;

        if (currentQueryAPI != null) {
            Map<String, Integer> result = new LinkedHashMap<>();
            // Query wrote by hand https://github.com/influxdata/influxdb-client-java/issues/75
            String query = "from(bucket: \"" + configuration.getBucket() + "\")\n" + "  |> range(start:-365d)\n"
                    + "  |> filter(fn: (r) => exists r." + TAG_ITEM_NAME + " )\n" + "  |> group(columns: [\""
                    + TAG_ITEM_NAME + "\"], mode:\"by\")\n" + "  |> count()\n" + "  |> group()";

            List<FluxTable> queryResult = currentQueryAPI.query(query);
            queryResult.stream().findFirst().orElse(new FluxTable()).getRecords().forEach(row -> {
                result.put((String) row.getValueByKey(TAG_ITEM_NAME), ((Number) row.getValue()).intValue());
            });
            return result;
        } else {
            logger.error("Returning empty result  because queryAPI isn't present");
            return Collections.emptyMap();
        }
    }
}
