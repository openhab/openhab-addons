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
package org.openhab.binding.dbquery.internal.dbimpl.influx2;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.InfluxDB2BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.Cancellable;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.Ready;
import com.influxdb.query.FluxRecord;

/**
 * Real implementation of {@link InfluxDBClientFacade}
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class InfluxDBClientFacadeImpl implements InfluxDBClientFacade {
    private final Logger logger = LoggerFactory.getLogger(InfluxDBClientFacadeImpl.class);

    private final InfluxDB2BridgeConfiguration config;

    private @Nullable InfluxDBClient client;
    private @Nullable QueryApi queryAPI;

    public InfluxDBClientFacadeImpl(InfluxDB2BridgeConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean connect() {
        var clientOptions = InfluxDBClientOptions.builder().url(config.getUrl()).org(config.getOrganization())
                .bucket(config.getBucket()).authenticateToken(config.getToken().toCharArray()).build();

        final InfluxDBClient createdClient = InfluxDBClientFactory.create(clientOptions);
        this.client = createdClient;
        var currentQueryAPI = createdClient.getQueryApi();
        this.queryAPI = currentQueryAPI;

        boolean connected = checkConnectionStatus();
        if (connected) {
            logger.debug("Successfully connected to InfluxDB. Instance ready={}", createdClient.ready());
        } else {
            logger.warn("Not able to connect to InfluxDB with config {}", config);
        }

        return connected;
    }

    private boolean checkConnectionStatus() {
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

    @Override
    public boolean isConnected() {
        return checkConnectionStatus();
    }

    @Override
    public boolean disconnect() {
        final InfluxDBClient currentClient = client;
        if (currentClient != null) {
            currentClient.close();
            client = null;
            queryAPI = null;
            logger.debug("Succesfully disconnected from InfluxDB");
        } else {
            logger.debug("Already disconnected");
        }
        return true;
    }

    @Override
    public void query(String query, BiConsumer<Cancellable, FluxRecord> onNext, Consumer<? super Throwable> onError,
            Runnable onComplete) {
        var currentQueryAPI = queryAPI;
        if (currentQueryAPI != null) {
            currentQueryAPI.query(query, onNext, onError, onComplete);
        } else {
            logger.warn("Query ignored as current queryAPI is null");
        }
    }
}
