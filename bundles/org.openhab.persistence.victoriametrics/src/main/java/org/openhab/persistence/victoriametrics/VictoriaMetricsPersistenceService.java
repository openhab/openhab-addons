/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.persistence.victoriametrics;

import static org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConstants.*;

import java.time.Instant;
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.victoriametrics.internal.*;
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
 * Implementation of the VictoriaMetrics {@link PersistenceService}.
 * Persists item values using the <a href="http://victoriametrics.org">VictoriaMetrics</a> time series database.
 *
 * @author Franz - Initial contribution
 * @author Joan Pujol Espinar - Original InfluxDB implementation
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.victoriametrics", property = Constants.SERVICE_PID
                + "=org.openhab.victoriametrics")
@ConfigurableService(category = "persistence", label = "VictoriaMetrics Persistence Service", description_uri = VictoriaMetricsPersistenceService.CONFIG_URI)
public class VictoriaMetricsPersistenceService implements ModifiablePersistenceService {
    public static final String SERVICE_NAME = "victoriametrics";

    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsPersistenceService.class);

    private static final int COMMIT_INTERVAL = 3; // in s
    protected static final String CONFIG_URI = "persistence:victoriametrics";

    // External dependencies
    private final ItemRegistry itemRegistry;
    private final VictoriaMetricsMetadataService victoriaMetadataService;

    private final VictoriaMetricsConfiguration configuration;
    private final VictoriaMetricsRepository victoriaRepository;
    private boolean serviceActivated;

    // Storage
    private final ScheduledFuture<?> storeJob;
    private final BlockingQueue<VictoriaMetricsPoint> pointsQueue = new LinkedBlockingQueue<>();

    // Conversion
    private final Set<ItemFactory> itemFactories = new HashSet<>();
    private Map<String, Class<? extends State>> desiredClasses = new HashMap<>();

    @Activate
    public VictoriaMetricsPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference VictoriaMetricsMetadataService victoriaMetadataService, Map<String, Object> config) {
        this.itemRegistry = itemRegistry;
        this.victoriaMetadataService = victoriaMetadataService;
        this.configuration = new VictoriaMetricsConfiguration(config);
        if (configuration.isValid()) {
            this.victoriaRepository = createVictoriaMetricsRepository();
            this.victoriaRepository.connect();
            this.storeJob = ThreadPoolManager.getScheduledPool("org.openhab.victoriametrics")
                    .scheduleWithFixedDelay(this::commit, COMMIT_INTERVAL, COMMIT_INTERVAL, TimeUnit.SECONDS);
            serviceActivated = true;
        } else {
            throw new IllegalArgumentException("Configuration invalid.");
        }
        logger.info("VictoriaMetrics persistence service started.");
    }

    protected VictoriaMetricsRepository createVictoriaMetricsRepository() {
        return new VictoriaMetricsRepository(configuration, victoriaMetadataService);
    }

    @Deactivate
    public void deactivate() {
        serviceActivated = false;
        storeJob.cancel(false);
        commit(); // Ensure we at least tried to store the data
        if (!pointsQueue.isEmpty()) {
            logger.warn("VictoriaMetrics failed to finally store {} points.", pointsQueue.size());
        }
        victoriaRepository.disconnect();
        logger.info("VictoriaMetrics persistence service stopped.");
    }

    @Override
    public String getId() {
        return SERVICE_NAME;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "VictoriaMetrics";
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        if (checkConnection()) {
            return victoriaRepository.getStoredItemsCount().entrySet().stream()
                    .map(VictoriaMetricsPersistentItemInfo::new).collect(Collectors.toUnmodifiableSet());
        } else {
            logger.info("getItemInfo ignored, VictoriaMetrics is not connected");
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
            logger.warn("VictoriaMetrics service not ready. Storing {} rejected.", item);
            return;
        }
        convert(item, state, date.toInstant(), alias).thenAccept(point -> {
            if (point == null) {
                logger.trace("Ignoring item {}, conversion to a VictoriaMetrics point failed.", item.getName());
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
            return victoriaRepository.remove(filter);
        } else {
            logger.debug("Remove query {} ignored, VictoriaMetrics is not connected.", filter);
            return false;
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        return query(filter, null);
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter, @Nullable String alias) {
        String itemName = filter.getItemName();
        if (itemName == null) {
            logger.warn("Item name is missing in filter {} when querying data.", filter);
            return List.of();
        }
        if (serviceActivated && checkConnection()) {
            logger.trace(
                    "Query-Filter: itemname: {}, ordering: {}, state: {},  operator: {}, getBeginDate: {}, getEndDate: {}, getPageSize: {}, getPageNumber: {}",
                    itemName, filter.getOrdering(), filter.getState(), filter.getOperator(), filter.getBeginDate(),
                    filter.getEndDate(), filter.getPageSize(), filter.getPageNumber());
            List<VictoriaMetricsRepository.VictoriaRow> results = victoriaRepository.query(filter);
            return results.stream().map(r -> mapRowToHistoricItem(r, itemName)).collect(Collectors.toList());
        } else {
            logger.debug("Query for persisted data ignored, VictoriaMetrics is not connected");
            return List.of();
        }
    }

    private HistoricItem mapRowToHistoricItem(VictoriaMetricsRepository.VictoriaRow row, String itemName) {
        State state = VictoriaMetricsStateConvertUtils.objectToState(row.value(), itemName, itemRegistry);
        return new VictoriaMetricsHistoricItem(row.itemName(), state, row.time());
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.RESTORE, PersistenceStrategy.Globals.CHANGE);
    }

    private boolean checkConnection() {
        if (victoriaRepository.isConnected()) {
            return true;
        } else if (serviceActivated) {
            logger.debug("Connection lost, trying re-connection");
            return victoriaRepository.connect();
        }
        return false;
    }

    private void commit() {
        if (!pointsQueue.isEmpty() && checkConnection()) {
            List<VictoriaMetricsPoint> points = new ArrayList<>();
            pointsQueue.drainTo(points);
            if (!victoriaRepository.write(points)) {
                logger.warn("Re-queuing {} elements, failed to write batch.", points.size());
                pointsQueue.addAll(points);
                victoriaRepository.disconnect();
            } else {
                logger.trace("Wrote {} elements to database", points.size());
            }
        }
    }

    /**
     * Convert incoming data to a VictoriaMetricsPoint for further processing. Storage is asynchronous, so item data may
     * have changed.
     */
    CompletableFuture<@Nullable VictoriaMetricsPoint> convert(Item item, State state, Instant timeStamp,
            @Nullable String storeAlias) {
        String itemName = item.getName();
        String itemLabel = item.getLabel();
        String category = item.getCategory();
        String itemType = item.getType();

        if (state instanceof UnDefType) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            String alias = (storeAlias != null && !storeAlias.isBlank()) ? storeAlias : itemName;
            String measurementName = victoriaMetadataService.getMeasurementNameOrDefault(alias);

            if (configuration.isCamelToSnakeCase()) {
                measurementName = VictoriaMetricsCaseConvertUtils.camelToSnake(measurementName);
            }

            measurementName = configuration.getMeasurementPrefix() + measurementName;

            State storeState = Objects
                    .requireNonNullElse(state.as(desiredClasses.get(ItemUtil.getMainItemType(itemType))), state);
            Object value = VictoriaMetricsStateConvertUtils.stateToObject(storeState);

            VictoriaMetricsPoint.Builder pointBuilder = VictoriaMetricsPoint.newBuilder(measurementName)
                    .withTime(timeStamp).withValue(value).withTag(TAG_ITEM_NAME, alias);

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
            if (configuration.isAddUnitTag() && state instanceof QuantityType<?> q) {
                String unit = q.getUnit().getSymbol();
                if (!unit.isBlank()) {
                    pointBuilder.withTag(TAG_UNIT_NAME, unit);
                }
            }

            victoriaMetadataService.getMetaData(alias)
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
                Item item = factory.createItem(itemType, "victoriaItem");
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
