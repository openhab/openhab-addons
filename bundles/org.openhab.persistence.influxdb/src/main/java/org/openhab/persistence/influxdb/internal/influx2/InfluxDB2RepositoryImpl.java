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
package org.openhab.persistence.influxdb.internal.influx2;

import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBConstants;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.openhab.persistence.influxdb.internal.UnexpectedConditionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.Ready;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;

/**
 * Implementation of {@link InfluxDBRepository} for InfluxDB 2.0
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDB2RepositoryImpl implements InfluxDBRepository {
    private final Logger logger = LoggerFactory.getLogger(InfluxDB2RepositoryImpl.class);
    private final InfluxDBConfiguration configuration;
    private final InfluxDBMetadataService influxDBMetadataService;

    private @Nullable InfluxDBClient client;
    private @Nullable QueryApi queryAPI;
    private @Nullable WriteApi writeAPI;

    public InfluxDB2RepositoryImpl(InfluxDBConfiguration configuration,
            InfluxDBMetadataService influxDBMetadataService) {
        this.configuration = configuration;
        this.influxDBMetadataService = influxDBMetadataService;
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    @Override
    public boolean connect() {
        InfluxDBClientOptions.Builder optionsBuilder = InfluxDBClientOptions.builder().url(configuration.getUrl())
                .org(configuration.getDatabaseName()).bucket(configuration.getRetentionPolicy());
        char[] token = configuration.getToken().toCharArray();
        if (token.length > 0) {
            optionsBuilder.authenticateToken(token);
        } else {
            optionsBuilder.authenticate(configuration.getUser(), configuration.getPassword().toCharArray());
        }
        InfluxDBClientOptions clientOptions = optionsBuilder.build();

        final InfluxDBClient createdClient = InfluxDBClientFactory.create(clientOptions);
        this.client = createdClient;

        queryAPI = createdClient.getQueryApi();
        writeAPI = createdClient.getWriteApi();
        logger.debug("Successfully connected to InfluxDB. Instance ready={}", createdClient.ready());

        return checkConnectionStatus();
    }

    @Override
    public void disconnect() {
        final InfluxDBClient currentClient = this.client;
        if (currentClient != null) {
            currentClient.close();
        }
        this.client = null;
    }

    @Override
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

    @Override
    public void write(InfluxPoint point) throws UnexpectedConditionException {
        final WriteApi currentWriteAPI = writeAPI;
        if (currentWriteAPI != null) {
            currentWriteAPI.writePoint(convertPointToClientFormat(point));
        } else {
            logger.warn("Write point {} ignored due to writeAPI isn't present", point);
        }
    }

    private Point convertPointToClientFormat(InfluxPoint point) throws UnexpectedConditionException {
        Point clientPoint = Point.measurement(point.getMeasurementName()).time(point.getTime(), WritePrecision.MS);
        setPointValue(point.getValue(), clientPoint);
        point.getTags().forEach(clientPoint::addTag);
        return clientPoint;
    }

    private void setPointValue(@Nullable Object value, Point point) throws UnexpectedConditionException {
        if (value instanceof String) {
            point.addField(FIELD_VALUE_NAME, (String) value);
        } else if (value instanceof Number) {
            point.addField(FIELD_VALUE_NAME, (Number) value);
        } else if (value instanceof Boolean) {
            point.addField(FIELD_VALUE_NAME, (Boolean) value);
        } else if (value == null) {
            point.addField(FIELD_VALUE_NAME, (String) null);
        } else {
            throw new UnexpectedConditionException("Not expected value type");
        }
    }

    @Override
    public List<InfluxRow> query(String query) {
        final QueryApi currentQueryAPI = queryAPI;
        if (currentQueryAPI != null) {
            List<FluxTable> clientResult = currentQueryAPI.query(query);
            return clientResult.stream().flatMap(this::mapRawResultToHistoric).toList();
        } else {
            logger.warn("Returning empty list because queryAPI isn't present");
            return List.of();
        }
    }

    private Stream<InfluxRow> mapRawResultToHistoric(FluxTable rawRow) {
        return rawRow.getRecords().stream().map(r -> {
            String itemName = (String) r.getValueByKey(InfluxDBConstants.TAG_ITEM_NAME);
            if (itemName == null) {
                itemName = r.getMeasurement();
            }
            Object value = r.getValueByKey(COLUMN_VALUE_NAME_V2);
            Instant time = (Instant) r.getValueByKey(COLUMN_TIME_NAME_V2);
            return new InfluxRow(time, itemName, value);
        });
    }

    @Override
    public Map<String, Integer> getStoredItemsCount() {
        final QueryApi currentQueryAPI = queryAPI;

        if (currentQueryAPI != null) {
            Map<String, Integer> result = new LinkedHashMap<>();
            // Query wrote by hand https://github.com/influxdata/influxdb-client-java/issues/75
            String query = "from(bucket: \"" + configuration.getRetentionPolicy() + "\")\n"
                    + "  |> range(start:-365d)\n" + "  |> filter(fn: (r) => exists r." + TAG_ITEM_NAME + " )\n"
                    + "  |> group(columns: [\"" + TAG_ITEM_NAME + "\"], mode:\"by\")\n" + "  |> count()\n"
                    + "  |> group()";

            List<FluxTable> queryResult = currentQueryAPI.query(query);
            Objects.requireNonNull(queryResult.stream().findFirst().orElse(new FluxTable())).getRecords()
                    .forEach(row -> {
                        result.put((String) row.getValueByKey(TAG_ITEM_NAME), ((Number) row.getValue()).intValue());
                    });
            return result;
        } else {
            logger.warn("Returning empty result  because queryAPI isn't present");
            return Collections.emptyMap();
        }
    }

    @Override
    public FilterCriteriaQueryCreator createQueryCreator() {
        return new InfluxDB2FilterCriteriaQueryCreatorImpl(configuration, influxDBMetadataService);
    }
}
