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
import org.influxdb.InfluxDBException;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.exceptions.InfluxException;

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
    private final InfluxDBConfiguration configuration;
    private final FilterCriteriaQueryCreator queryCreator;
    private @Nullable InfluxDB client;

    public InfluxDB1RepositoryImpl(InfluxDBConfiguration configuration,
            InfluxDBMetadataService influxDBMetadataService) {
        this.configuration = configuration;
        this.queryCreator = new InfluxDB1FilterCriteriaQueryCreatorImpl(configuration, influxDBMetadataService);
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
        final InfluxDB currentClient = client;
        if (currentClient != null) {
            currentClient.close();
        }
        this.client = null;
    }

    @Override
    public boolean checkConnectionStatus() {
        final InfluxDB currentClient = client;
        if (currentClient != null) {
            try {
                Pong pong = currentClient.ping();
                String version = pong.getVersion();
                // may be check for version >= 0.9
                if (version != null && !version.contains("unknown")) {
                    logger.debug("database status is OK, version is {}", version);
                    return true;
                } else {
                    logger.warn("database ping error, version is: \"{}\" response time was \"{}\"", version,
                            pong.getResponseTime());
                }
            } catch (RuntimeException e) {
                logger.warn("database error: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("checkConnection: database is not connected");
        }
        return false;
    }

    @Override
    public boolean write(List<InfluxPoint> influxPoints) {
        final InfluxDB currentClient = this.client;
        if (currentClient == null) {
            return false;
        }
        try {
            List<Point> points = influxPoints.stream().map(this::convertPointToClientFormat).filter(Optional::isPresent)
                    .map(Optional::get).toList();
            BatchPoints batchPoints = BatchPoints.database(configuration.getDatabaseName())
                    .retentionPolicy(configuration.getRetentionPolicy()).points(points).build();
            currentClient.write(batchPoints);
        } catch (InfluxException | InfluxDBException e) {
            logger.debug("Writing to database failed", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean remove(FilterCriteria filter) {
        logger.warn("Removing data is not supported in InfluxDB v1.");
        return false;
    }

    private Optional<Point> convertPointToClientFormat(InfluxPoint point) {
        Point.Builder clientPoint = Point.measurement(point.getMeasurementName()).time(point.getTime().toEpochMilli(),
                TimeUnit.MILLISECONDS);
        Object value = point.getValue();
        if (value instanceof String string) {
            clientPoint.addField(FIELD_VALUE_NAME, string);
        } else if (value instanceof Number number) {
            clientPoint.addField(FIELD_VALUE_NAME, number);
        } else if (value instanceof Boolean boolean1) {
            clientPoint.addField(FIELD_VALUE_NAME, boolean1);
        } else if (value == null) {
            clientPoint.addField(FIELD_VALUE_NAME, "null");
        } else {
            logger.warn("Could not convert {}, discarding this datapoint", point);
            return Optional.empty();
        }
        point.getTags().forEach(clientPoint::tag);
        return Optional.of(clientPoint.build());
    }

    @Override
    public List<InfluxRow> query(FilterCriteria filter, String retentionPolicy) {
        try {
            final InfluxDB currentClient = client;
            if (currentClient != null) {
                String query = queryCreator.createQuery(filter, retentionPolicy);
                logger.trace("Query {}", query);
                Query parsedQuery = new Query(query, configuration.getDatabaseName());
                List<QueryResult.Result> results = currentClient.query(parsedQuery, TimeUnit.MILLISECONDS).getResults();
                return convertClientResultToRepository(results);
            } else {
                throw new InfluxException("API not present");
            }
        } catch (InfluxException | InfluxDBException e) {
            logger.warn("Failed to execute query '{}': {}", filter, e.getMessage());
            return List.of();
        }
    }

    private List<InfluxRow> convertClientResultToRepository(List<QueryResult.Result> results) {
        List<InfluxRow> rows = new ArrayList<>();
        for (QueryResult.Result result : results) {
            List<QueryResult.Series> allSeries = result.getSeries();
            if (result.getError() != null) {
                logger.warn("{}", result.getError());
                continue;
            }
            if (allSeries == null) {
                logger.debug("query returned no series");
            } else {
                for (QueryResult.Series series : allSeries) {
                    logger.trace("series {}", series);
                    String defaultItemName = series.getName();
                    List<List<Object>> allValues = series.getValues();
                    if (allValues == null) {
                        logger.debug("query returned no values");
                    } else {
                        List<String> columns = series.getColumns();
                        logger.trace("columns {}", columns);
                        if (columns != null) {
                            int timestampColumn = columns.indexOf(COLUMN_TIME_NAME_V1);
                            int valueColumn = columns.indexOf(COLUMN_VALUE_NAME_V1);
                            int itemNameColumn = columns.indexOf(TAG_ITEM_NAME);
                            if (valueColumn == -1 || timestampColumn == -1) {
                                throw new IllegalStateException("missing column");
                            }
                            for (List<Object> valueObject : allValues) {
                                Double rawTime = (Double) valueObject.get(timestampColumn);
                                Instant time = Instant.ofEpochMilli(rawTime.longValue());
                                Object value = valueObject.get(valueColumn);
                                String itemName = itemNameColumn == -1 ? defaultItemName
                                        : Objects.requireNonNullElse((String) valueObject.get(itemNameColumn),
                                                defaultItemName);
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
