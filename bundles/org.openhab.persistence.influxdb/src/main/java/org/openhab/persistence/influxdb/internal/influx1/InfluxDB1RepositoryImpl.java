/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal.influx1;

import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.COLUMN_TIME_NAME_V1;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.COLUMN_VALUE_NAME_V1;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.FIELD_VALUE_NAME;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.TAG_ITEM_NAME;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.openhab.persistence.influxdb.internal.InfluxRow;
import org.openhab.persistence.influxdb.internal.UnnexpectedConditionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link InfluxDBRepository} for InfluxDB 1.0
 *
 * @author Joan Pujol Espinar - Initial contribution. Most code has been moved
 *         from
 *         {@link org.openhab.persistence.influxdb.InfluxDBPersistenceService}
 *         where it was in previous version
 */
@NonNullByDefault
public class InfluxDB1RepositoryImpl implements InfluxDBRepository {
    private final Logger logger = LoggerFactory.getLogger(InfluxDB1RepositoryImpl.class);
    private InfluxDBConfiguration configuration;
    @Nullable
    private InfluxDB client;

    public InfluxDB1RepositoryImpl(InfluxDBConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    @Override
    public boolean connect() {
        final InfluxDB createdClient = InfluxDBFactory.connect(configuration.getUrl(), configuration.getUser(),
                configuration.getPassword());
        createdClient.setDatabase(configuration.getDatabaseName());
        createdClient.setRetentionPolicy(configuration.getRetentionPolicy());
        createdClient.enableBatch(200, 100, TimeUnit.MILLISECONDS);
        this.client = createdClient;
        return checkConnectionStatus();
    }

    @Override
    public void disconnect() {
        this.client = null;
    }

    @Override
    public boolean checkConnectionStatus() {
        boolean dbStatus = false;
        final InfluxDB currentClient = client;
        if (currentClient != null) {
            try {
                Pong pong = currentClient.ping();
                String version = pong.getVersion();
                // may be check for version >= 0.9
                if (version != null && !version.contains("unknown")) {
                    dbStatus = true;
                    logger.debug("database status is OK, version is {}", version);
                } else {
                    logger.warn("database ping error, version is: \"{}\" response time was \"{}\"", version,
                            pong.getResponseTime());
                    dbStatus = false;
                }
            } catch (RuntimeException e) {
                dbStatus = false;
                logger.error("database connection failed", e);
                handleDatabaseException(e);
            }
        } else {
            logger.warn("checkConnection: database is not connected");
        }
        return dbStatus;
    }

    private void handleDatabaseException(Exception e) {
        logger.warn("database error: {}", e.getMessage(), e);
    }

    @Override
    public void write(InfluxPoint point) {
        final InfluxDB currentClient = this.client;
        if (currentClient != null) {
            Point clientPoint = convertPointToClientFormat(point);
            currentClient.write(configuration.getDatabaseName(), configuration.getRetentionPolicy(), clientPoint);
        } else {
            logger.warn("Write point {} ignored due to client isn't connected", point);
        }
    }

    private Point convertPointToClientFormat(InfluxPoint point) {
        Point.Builder clientPoint = Point.measurement(point.getMeasurementName()).time(point.getTime().toEpochMilli(),
                TimeUnit.MILLISECONDS);
        setPointValue(point.getValue(), clientPoint);
        point.getTags().entrySet().forEach(e -> clientPoint.tag(e.getKey(), e.getValue()));
        return clientPoint.build();
    }

    private void setPointValue(@Nullable Object value, Point.Builder point) {
        if (value instanceof String) {
            point.addField(FIELD_VALUE_NAME, (String) value);
        } else if (value instanceof Number) {
            point.addField(FIELD_VALUE_NAME, (Number) value);
        } else if (value instanceof Boolean) {
            point.addField(FIELD_VALUE_NAME, (Boolean) value);
        } else if (value == null) {
            point.addField(FIELD_VALUE_NAME, (String) null);
        } else {
            throw new UnnexpectedConditionException("Not expected value type");
        }
    }

    @Override
    public List<InfluxRow> query(String query) {
        final InfluxDB currentClient = client;
        if (currentClient != null) {
            Query parsedQuery = new Query(query, configuration.getDatabaseName());
            List<QueryResult.Result> results = currentClient.query(parsedQuery, TimeUnit.MILLISECONDS).getResults();
            return convertClientResutToRepository(results);
        } else {
            logger.warn("Returning empty list because queryAPI isn't present");
            return Collections.emptyList();
        }
    }

    private List<InfluxRow> convertClientResutToRepository(List<QueryResult.Result> results) {
        List<InfluxRow> rows = new ArrayList<>();
        for (QueryResult.Result result : results) {
            List<QueryResult.Series> seriess = result.getSeries();
            if (result.getError() != null) {
                logger.warn("{}", result.getError());
                continue;
            }
            if (seriess == null) {
                logger.debug("query returned no series");
            } else {
                for (QueryResult.Series series : seriess) {
                    logger.trace("series {}", series.toString());
                    List<List<@Nullable Object>> valuess = series.getValues();
                    if (valuess == null) {
                        logger.debug("query returned no values");
                    } else {
                        List<String> columns = series.getColumns();
                        logger.trace("columns {}", columns);
                        if (columns != null) {
                            Integer timestampColumn = null;
                            Integer valueColumn = null;
                            Integer itemNameColumn = null;
                            for (int i = 0; i < columns.size(); i++) {
                                String columnName = columns.get(i);
                                if (columnName.equals(COLUMN_TIME_NAME_V1)) {
                                    timestampColumn = i;
                                } else if (columnName.equals(COLUMN_VALUE_NAME_V1)) {
                                    valueColumn = i;
                                } else if (columnName.equals(TAG_ITEM_NAME)) {
                                    itemNameColumn = i;
                                }
                            }
                            if (valueColumn == null || timestampColumn == null) {
                                throw new IllegalStateException("missing column");
                            }
                            for (int i = 0; i < valuess.size(); i++) {
                                Double rawTime = (Double) Objects.requireNonNull(valuess.get(i).get(timestampColumn));
                                Instant time = Instant.ofEpochMilli(rawTime.longValue());
                                @Nullable
                                Object value = valuess.get(i).get(valueColumn);
                                var currentI = i;
                                String itemName = Optional.ofNullable(itemNameColumn)
                                        .flatMap(inc -> Optional.ofNullable((String) valuess.get(currentI).get(inc)))
                                        .orElse(series.getName());
                                logger.trace("adding historic item {}: time {} value {}", itemName, time, value);
                                rows.add(new InfluxRow(time, itemName, value));
                            }
                        }
                    }
                }
            }
        }
        return rows;
    }

    @Override
    public Map<String, Integer> getStoredItemsCount() {
        return Collections.emptyMap();
    }
}
