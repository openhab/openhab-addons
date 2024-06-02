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
package org.openhab.persistence.influxdb;

import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemUtil;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBHistoricItem;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBPersistentItemInfo;
import org.openhab.persistence.influxdb.internal.InfluxDBRepository;
import org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils;
import org.openhab.persistence.influxdb.internal.InfluxPoint;
import org.openhab.persistence.influxdb.internal.influx1.InfluxDB1RepositoryImpl;
import org.openhab.persistence.influxdb.internal.influx2.InfluxDB2RepositoryImpl;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the InfluxDB {@link PersistenceService}. It
 * persists item values using the <a href="http://influxdb.org">InfluxDB</a> time
 * series database. The states ({@link State}) of an {@link Item} are persisted
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
public class InfluxDBPersistenceService implements ModifiablePersistenceService {
    public static final String SERVICE_NAME = "influxdb";

    private final Logger logger = LoggerFactory.getLogger(InfluxDBPersistenceService.class);

    private static final int COMMIT_INTERVAL = 3; // in s
    protected static final String CONFIG_URI = "persistence:influxdb";

    // External dependencies
    private final ItemRegistry itemRegistry;
    private final InfluxDBMetadataService influxDBMetadataService;

    private final InfluxDBConfiguration configuration;
    private final InfluxDBRepository influxDBRepository;
    private boolean serviceActivated;

    // storage
    private final ScheduledFuture<?> storeJob;
    private final BlockingQueue<InfluxPoint> pointsQueue = new LinkedBlockingQueue<>();

    // conversion
    private final Set<ItemFactory> itemFactories = new HashSet<>();
    private Map<String, Class<? extends State>> desiredClasses = new HashMap<>();

    @Activate
    public InfluxDBPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference InfluxDBMetadataService influxDBMetadataService, Map<String, Object> config) {
        this.itemRegistry = itemRegistry;
        this.influxDBMetadataService = influxDBMetadataService;
        this.configuration = new InfluxDBConfiguration(config);
        if (configuration.isValid()) {
            this.influxDBRepository = createInfluxDBRepository();
            this.influxDBRepository.connect();
            this.storeJob = ThreadPoolManager.getScheduledPool("org.openhab.influxdb")
                    .scheduleWithFixedDelay(this::commit, COMMIT_INTERVAL, COMMIT_INTERVAL, TimeUnit.SECONDS);
            serviceActivated = true;
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
        serviceActivated = false;

        storeJob.cancel(false);
        commit(); // ensure we at least tried to store the data;

        if (!pointsQueue.isEmpty()) {
            logger.warn("InfluxDB failed to finally store {} points.", pointsQueue.size());
        }

        influxDBRepository.disconnect();
        logger.info("InfluxDB persistence service stopped.");
    }

    @Override
    public String getId() {
        return SERVICE_NAME;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "InfluxDB";
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
        store(item, ZonedDateTime.now(), item.getState(), alias);
    }

    @Override
    public void store(Item item, ZonedDateTime date, State state) {
        store(item, date, state, null);
    }

    @Override
    public void store(Item item, ZonedDateTime date, State state, @Nullable String alias) {
        if (!serviceActivated) {
            logger.warn("InfluxDB service not ready. Storing {} rejected.", item);
            return;
        }
        convert(item, state, date.toInstant(), null).thenAccept(point -> {
            if (point == null) {
                logger.trace("Ignoring item {}, conversion to an InfluxDB point failed.", item.getName());
                return;
            }
            if (pointsQueue.offer(point)) {
                logger.trace("Queued {} for item {}", point, item);
            } else {
                logger.warn("Failed to queue {} for item {}", point, item);
            }
        });
    }

    @Override
    public boolean remove(FilterCriteria filter) throws IllegalArgumentException {
        if (serviceActivated && checkConnection()) {
            if (filter.getItemName() == null) {
                logger.warn("Item name is missing in filter {} when trying to remove data.", filter);
                return false;
            }
            return influxDBRepository.remove(filter);
        } else {
            logger.debug("Remove query {} ignored, InfluxDB is not connected.", filter);
            return false;
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        if (serviceActivated && checkConnection()) {
            logger.trace(
                    "Query-Filter: itemname: {}, ordering: {}, state: {},  operator: {}, getBeginDate: {}, getEndDate: {}, getPageSize: {}, getPageNumber: {}",
                    filter.getItemName(), filter.getOrdering().toString(), filter.getState(), filter.getOperator(),
                    filter.getBeginDate(), filter.getEndDate(), filter.getPageSize(), filter.getPageNumber());
            if (filter.getItemName() == null) {
                logger.warn("Item name is missing in filter {} when querying data.", filter);
                return List.of();
            }

            List<InfluxDBRepository.InfluxRow> results = influxDBRepository.query(filter,
                    configuration.getRetentionPolicy());
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
        } else if (serviceActivated) {
            logger.debug("Connection lost, trying re-connection");
            return influxDBRepository.connect();
        }
        return false;
    }

    private void commit() {
        if (!pointsQueue.isEmpty() && checkConnection()) {
            List<InfluxPoint> points = new ArrayList<>();
            pointsQueue.drainTo(points);
            if (!influxDBRepository.write(points)) {
                logger.warn("Re-queuing {} elements, failed to write batch.", points.size());
                pointsQueue.addAll(points);
                influxDBRepository.disconnect();
            } else {
                logger.trace("Wrote {} elements to database", points.size());
            }
        }
    }

    /**
     * Convert incoming data to an {@link InfluxPoint} for further processing. This is needed because storage is
     * asynchronous and the item data may have changed.
     * <p />
     * The method is package-private for testing.
     *
     * @param item the {@link Item} that needs conversion
     * @param storeAlias an (optional) alias for the item
     * @return a {@link CompletableFuture} that contains either <code>null</code> for item states that cannot be
     *         converted or the corresponding {@link InfluxPoint}
     */
    CompletableFuture<@Nullable InfluxPoint> convert(Item item, State state, Instant timeStamp,
            @Nullable String storeAlias) {
        String itemName = item.getName();
        String itemLabel = item.getLabel();
        String category = item.getCategory();
        String itemType = item.getType();

        if (state instanceof UnDefType) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            String measurementName = storeAlias != null && !storeAlias.isBlank() ? storeAlias : itemName;
            measurementName = influxDBMetadataService.getMeasurementNameOrDefault(itemName, measurementName);

            if (configuration.isReplaceUnderscore()) {
                measurementName = measurementName.replace('_', '.');
            }

            State storeState = Objects
                    .requireNonNullElse(state.as(desiredClasses.get(ItemUtil.getMainItemType(itemType))), state);
            Object value = InfluxDBStateConvertUtils.stateToObject(storeState);

            InfluxPoint.Builder pointBuilder = InfluxPoint.newBuilder(measurementName).withTime(timeStamp)
                    .withValue(value).withTag(TAG_ITEM_NAME, itemName);

            if (configuration.isAddCategoryTag()) {
                String categoryName = Objects.requireNonNullElse(category, "n/a");
                pointBuilder.withTag(TAG_CATEGORY_NAME, categoryName);
            }

            if (configuration.isAddTypeTag()) {
                pointBuilder.withTag(TAG_TYPE_NAME, itemType);
            }

            if (configuration.isAddLabelTag()) {
                String labelName = Objects.requireNonNullElse(itemLabel, "n/a");
                pointBuilder.withTag(TAG_LABEL_NAME, labelName);
            }

            influxDBMetadataService.getMetaData(itemName)
                    .ifPresent(metadata -> metadata.getConfiguration().forEach(pointBuilder::withTag));

            return pointBuilder.build();
        });
    }

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC)
    public void setItemFactory(ItemFactory itemFactory) {
        itemFactories.add(itemFactory);
        calculateItemTypeClasses();
    }

    public void unsetItemFactory(ItemFactory itemFactory) {
        itemFactories.remove(itemFactory);
        calculateItemTypeClasses();
    }

    private synchronized void calculateItemTypeClasses() {
        Map<String, Class<? extends State>> desiredClasses = new HashMap<>();
        itemFactories.forEach(factory -> {
            for (String itemType : factory.getSupportedItemTypes()) {
                Item item = factory.createItem(itemType, "influxItem");
                if (item != null) {
                    item.getAcceptedCommandTypes().stream()
                            .filter(commandType -> commandType.isAssignableFrom(State.class)).findFirst()
                            .map(commandType -> (Class<? extends State>) commandType.asSubclass(State.class))
                            .ifPresent(desiredClass -> desiredClasses.put(itemType, desiredClass));
                }
            }
        });
        this.desiredClasses = desiredClasses;
    }
}
