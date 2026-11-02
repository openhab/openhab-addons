/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemFactory;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemUtil;
import org.openhab.core.items.Metadata;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsConfiguration;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsHistoricItem;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetadataService;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsMetricNameResolver;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPersistentItemInfo;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsPoint;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsRepository;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsStateConvertUtils;
import org.openhab.persistence.victoriametrics.internal.VictoriaMetricsStringStateLimiter;
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
 * This is the implementation of the VictoriaMetrics {@link PersistenceService}. It persists item values using the
 * <a href="https://victoriametrics.com">VictoriaMetrics</a> time series database. The states ({@link State}) of an
 * {@link Item} are persisted by default in a time series with names equal to the name of the item and the label
 * {@code item} set to the item name.
 *
 * @author Franz - Initial contribution
 * @author Joan Pujol Espinar - Original InfluxDB implementation
 * @author Michael Weger - Use Jetty, fix query with alias, store numeric states, components and string states
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.victoriametrics", //
        property = Constants.SERVICE_PID + "=org.openhab.victoriametrics")
@ConfigurableService(category = "persistence", label = "VictoriaMetrics Persistence Service", description_uri = VictoriaMetricsPersistenceService.CONFIG_URI)
public class VictoriaMetricsPersistenceService implements ModifiablePersistenceService {
    public static final String SERVICE_NAME = "victoriametrics";

    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsPersistenceService.class);

    private static final int COMMIT_INTERVAL = 3; // in s
    // default -maxLabelValueLen of VictoriaMetrics
    private static final int MAX_STRING_STATE_BYTES = 4096;
    protected static final String CONFIG_URI = "persistence:victoriametrics";

    // External dependencies
    private final ItemRegistry itemRegistry;
    private final VictoriaMetricsMetadataService metadataService;
    private final HttpClient httpClient;

    private final VictoriaMetricsConfiguration configuration;
    private final VictoriaMetricsMetricNameResolver metricNameResolver;
    private final VictoriaMetricsRepository repository;
    private final VictoriaMetricsStringStateLimiter stringStateLimiter;
    private boolean serviceActivated;

    // storage
    private final ScheduledFuture<?> storeJob;
    private final BlockingQueue<VictoriaMetricsPoint> pointsQueue = new LinkedBlockingQueue<>();
    private final Set<String> loggedUnsupportedItems = ConcurrentHashMap.newKeySet();

    // conversion
    private final Set<ItemFactory> itemFactories = new HashSet<>();
    private Map<String, Class<? extends State>> desiredClasses = new HashMap<>();

    @Activate
    public VictoriaMetricsPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference VictoriaMetricsMetadataService metadataService,
            final @Reference HttpClientFactory httpClientFactory, Map<String, Object> config) {
        this.itemRegistry = itemRegistry;
        this.metadataService = metadataService;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.configuration = new VictoriaMetricsConfiguration(config);
        this.metricNameResolver = new VictoriaMetricsMetricNameResolver(configuration, metadataService);
        if (configuration.isValid()) {
            this.repository = createVictoriaMetricsRepository();
            this.stringStateLimiter = new VictoriaMetricsStringStateLimiter(configuration.getMaxStringStates(),
                    repository::getStringStates);
            this.repository.connect();
            this.storeJob = ThreadPoolManager.getScheduledPool("org.openhab.victoriametrics")
                    .scheduleWithFixedDelay(this::commit, COMMIT_INTERVAL, COMMIT_INTERVAL, TimeUnit.SECONDS);
            serviceActivated = true;
        } else {
            throw new IllegalArgumentException("Configuration invalid.");
        }

        logger.info("VictoriaMetrics persistence service started.");
    }

    // Visible for testing
    protected VictoriaMetricsRepository createVictoriaMetricsRepository() {
        return new VictoriaMetricsRepository(configuration, metricNameResolver, httpClient);
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
            logger.warn("VictoriaMetrics failed to finally store {} points.", pointsQueue.size());
        }

        repository.disconnect();
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
            return repository.getStoredItemNames().stream().map(VictoriaMetricsPersistentItemInfo::new)
                    .collect(Collectors.toUnmodifiableSet());
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
            boolean removed = repository.remove(filter);
            if (removed) {
                stringStateLimiter.forget(Objects.requireNonNull(filter.getItemName()));
            }
            return removed;
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
            logger.trace("Query-Filter: {}", filter);

            List<VictoriaMetricsRepository.VictoriaMetricsRow> results = repository.query(filter, alias);
            return results.stream().map(r -> mapRowToHistoricItem(r, itemName)).collect(Collectors.toList());
        } else {
            logger.debug("Query for persisted data ignored, VictoriaMetrics is not connected");
            return List.of();
        }
    }

    private HistoricItem mapRowToHistoricItem(VictoriaMetricsRepository.VictoriaMetricsRow row, String itemName) {
        State state = VictoriaMetricsStateConvertUtils.objectToState(row.value(), itemName, itemRegistry);
        return new VictoriaMetricsHistoricItem(row.itemName(), state, row.time());
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
        if (repository.isConnected()) {
            return true;
        } else if (serviceActivated) {
            logger.debug("Connection lost, trying re-connection");
            return repository.connect();
        }
        return false;
    }

    private void commit() {
        if (!pointsQueue.isEmpty() && checkConnection()) {
            List<VictoriaMetricsPoint> queuedPoints = new ArrayList<>();
            pointsQueue.drainTo(queuedPoints);
            List<VictoriaMetricsPoint> points = stringStateLimiter.filter(queuedPoints);
            if (points == null) {
                logger.warn("Re-queuing {} elements, failed to read the stored string states.", queuedPoints.size());
                pointsQueue.addAll(queuedPoints);
                repository.disconnect();
            } else if (!repository.write(points)) {
                logger.warn("Re-queuing {} elements, failed to write batch.", points.size());
                pointsQueue.addAll(points);
                repository.disconnect();
            } else {
                logger.trace("Wrote {} elements to database", points.size());
            }
        }
    }

    /**
     * Convert incoming data to a {@link VictoriaMetricsPoint} for further processing. This is needed because storage
     * is asynchronous and the item data may have changed.
     * <p />
     * The method is package-private for testing.
     *
     * @param item the {@link Item} that needs conversion
     * @param storeAlias an (optional) alias for the item
     * @return a {@link CompletableFuture} that contains either <code>null</code> for item states that cannot be
     *         converted or the corresponding {@link VictoriaMetricsPoint}
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
            String alias = storeAlias != null && !storeAlias.isBlank() ? storeAlias : itemName;

            State storeState = Objects
                    .requireNonNullElse(state.as(desiredClasses.get(ItemUtil.getMainItemType(itemType))), state);
            VictoriaMetricsPoint.Builder pointBuilder = VictoriaMetricsPoint
                    .newBuilder(metricNameResolver.getMetricName(alias)).withTime(timeStamp)
                    .withTag(TAG_ITEM_NAME, alias);

            Number value = VictoriaMetricsStateConvertUtils.stateToObject(storeState);
            Map<String, BigDecimal> components = VictoriaMetricsStateConvertUtils.stateToComponents(storeState);
            if (value != null) {
                pointBuilder.withValue(value);
            } else if (components != null) {
                pointBuilder.withComponents(components);
            } else if (storeState instanceof StringType && isStringStatesEnabled(itemName)) {
                String stringState = storeState.toFullString();
                // an empty label value is the same as no label, VictoriaMetrics ignores series with longer values
                if (stringState.isEmpty()
                        || stringState.getBytes(StandardCharsets.UTF_8).length > MAX_STRING_STATE_BYTES) {
                    logger.debug("String state of item '{}' is not stored, it is empty or longer than {} bytes",
                            itemName, MAX_STRING_STATE_BYTES);
                    return null;
                }
                pointBuilder.withStringState(stringState);
            } else {
                if (loggedUnsupportedItems.add(itemName)) {
                    if (storeState instanceof StringType) {
                        logger.info(
                                "Item '{}' is not stored, string states must be enabled with the item metadata '{}'",
                                itemName, METADATA_STRING_STATES);
                    } else {
                        logger.info("Item '{}' is not stored, VictoriaMetrics only supports numeric states, not {}",
                                itemName, storeState.getClass().getSimpleName());
                    }
                }
                return null;
            }

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

            if (configuration.isAddUnitTag() && storeState instanceof QuantityType<?> quantity) {
                String unit = quantity.getUnit().toString();
                if (!unit.isBlank()) {
                    pointBuilder.withTag(TAG_UNIT_NAME, unit);
                }
            }

            metadataService.getMetaData(alias)
                    .ifPresent(metadata -> metadata.getConfiguration().forEach((key, tagValue) -> {
                        if (!METADATA_STRING_STATES.equals(key)) {
                            pointBuilder.withTag(VictoriaMetricsMetricNameResolver.sanitizeLabelName(key), tagValue);
                        }
                    }));

            return pointBuilder.build();
        });
    }

    private boolean isStringStatesEnabled(String itemName) {
        // the value is a Boolean when configured in the UI, but a String in .items files
        Optional<Metadata> metadata = metadataService.getMetaData(itemName);
        return metadata.isPresent()
                && Boolean.parseBoolean(String.valueOf(metadata.get().getConfiguration().get(METADATA_STRING_STATES)));
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
                Item item = factory.createItem(itemType, "victoriaMetricsItem");
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
