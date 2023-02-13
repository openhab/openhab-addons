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
package org.openhab.persistence.influxdb;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBHistoricItem;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBPersistentItemInfo;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.openhab.persistence.influxdb.internal.ItemToStorePointCreator;
import org.openhab.persistence.influxdb.internal.UnexpectedConditionException;
import org.openhab.persistence.influxdb.internal.influx1.InfluxDB1RepositoryImpl;
import org.openhab.persistence.influxdb.internal.influx2.InfluxDB2RepositoryImpl;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the InfluxDB {@link PersistenceService}. It
 * persists item values using the <a href="http://influxdb.org">InfluxDB time
 * series database. The states ( {@link State}) of an {@link Item} are persisted
 * by default in a time series with names equal to the name of the item.
 *
 * This addon supports 1.X and 2.X versions, as two versions are incompatible
 * and use different drivers the specific code for each version is accessed by
 * {@link InfluxDBRepository} and {@link FilterCriteriaQueryCreator} interfaces
 * and specific implementation reside in
 * {@link org.openhab.persistence.influxdb.internal.influx1} and
 * {@link org.openhab.persistence.influxdb.internal.influx2} packages
 *
 * @author Theo Weiss - Initial contribution, rewrite of
 *         org.openhab.persistence.influxdb
 * @author Joan Pujol Espinar - Addon rewrite refactoring code and adding
 *         support for InfluxDB 2.0. Some tag code is based from not integrated
 *         branch from Dominik Vorreiter
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.influxdb", //
        property = Constants.SERVICE_PID + "=org.openhab.influxdb")
@ConfigurableService(category = "persistence", label = "InfluxDB Persistence Service", description_uri = InfluxDBPersistenceService.CONFIG_URI)
public class InfluxDBPersistenceService implements QueryablePersistenceService {
    public static final String SERVICE_NAME = "influxdb";

    private final Logger logger = LoggerFactory.getLogger(InfluxDBPersistenceService.class);

    protected static final String CONFIG_URI = "persistence:influxdb";

    // External dependencies
    private final ItemRegistry itemRegistry;
    private final InfluxDBMetadataService influxDBMetadataService;

    private final InfluxDBConfiguration configuration;
    private final ItemToStorePointCreator itemToStorePointCreator;
    private final InfluxDBRepository influxDBRepository;
    private boolean tryReconnection;

    @Activate
    public InfluxDBPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference InfluxDBMetadataService influxDBMetadataService, Map<String, Object> config) {
        this.itemRegistry = itemRegistry;
        this.influxDBMetadataService = influxDBMetadataService;
        this.configuration = new InfluxDBConfiguration(config);
        if (configuration.isValid()) {
            this.influxDBRepository = createInfluxDBRepository();
            this.influxDBRepository.connect();
            this.itemToStorePointCreator = new ItemToStorePointCreator(configuration, influxDBMetadataService);
            tryReconnection = true;
        } else {
            throw new IllegalArgumentException("Configuration invalid.");
        }

        logger.info("InfluxDB persistence service started.");
    }

    // Visible for testing
    protected InfluxDBRepository createInfluxDBRepository() throws IllegalArgumentException {
        return switch (configuration.getVersion()) {
            case V1 -> new InfluxDB1RepositoryImpl(configuration, influxDBMetadataService);
            case V2 -> new InfluxDB2RepositoryImpl(configuration, influxDBMetadataService);
            default -> throw new IllegalArgumentException("Failed to instantiate repository.");
        };
    }

    /**
     * Disconnect from database when service is deactivated
     */
    @Deactivate
    public void deactivate() {
        tryReconnection = false;
        influxDBRepository.disconnect();
        logger.info("InfluxDB persistence service stopped.");
    }

    @Override
    public String getId() {
        return SERVICE_NAME;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "InfluxDB persistence layer";
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        if (checkConnection()) {
            return influxDBRepository.getStoredItemsCount().entrySet().stream().map(InfluxDBPersistentItemInfo::new)
                    .collect(Collectors.toUnmodifiableSet());
        } else {
            logger.info("getItemInfo ignored, InfluxDB is not connected");
            return Set.of();
        }
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        if (checkConnection()) {
            InfluxPoint point = itemToStorePointCreator.convert(item, alias);
            if (point != null) {
                try {
                    influxDBRepository.write(point);
                    logger.trace("Stored item {} in InfluxDB point {}", item, point);
                } catch (UnexpectedConditionException e) {
                    logger.warn("Failed to store item {} in InfluxDB point {}", point, item);
                }
            } else {
                logger.trace("Ignoring item {}, conversion to an InfluxDB point failed.", item);
            }
        } else {
            logger.debug("store ignored, InfluxDB is not connected");
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        if (checkConnection()) {
            logger.trace(
                    "Query-Filter: itemname: {}, ordering: {}, state: {},  operator: {}, getBeginDate: {}, getEndDate: {}, getPageSize: {}, getPageNumber: {}",
                    filter.getItemName(), filter.getOrdering().toString(), filter.getState(), filter.getOperator(),
                    filter.getBeginDate(), filter.getEndDate(), filter.getPageSize(), filter.getPageNumber());
            String query = influxDBRepository.createQueryCreator().createQuery(filter,
                    configuration.getRetentionPolicy());
            logger.trace("Query {}", query);
            List<InfluxDBRepository.InfluxRow> results = influxDBRepository.query(query);
            return results.stream().map(this::mapRowToHistoricItem).collect(Collectors.toList());
        } else {
            logger.debug("Query for persisted data ignored, InfluxDB is not connected");
            return List.of();
        }
    }

    private HistoricItem mapRowToHistoricItem(InfluxDBRepository.InfluxRow row) {
        State state = InfluxDBStateConvertUtils.objectToState(row.value(), row.itemName(), itemRegistry);
        return new InfluxDBHistoricItem(row.itemName(), state,
                ZonedDateTime.ofInstant(row.time(), ZoneId.systemDefault()));
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.RESTORE, PersistenceStrategy.Globals.CHANGE);
    }

    /**
     * check connection and try reconnect
     *
     * @return true if connected
     */
    private boolean checkConnection() {
        if (influxDBRepository.isConnected()) {
            return true;
        } else if (tryReconnection) {
            logger.debug("Connection lost, trying re-connection");
            return influxDBRepository.connect();
        }
        return false;
    }
}
