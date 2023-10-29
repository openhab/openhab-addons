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
package org.openhab.persistence.dynamodb.internal;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.core.client.config.ClientAsyncConfiguration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * This is the implementation of the DynamoDB {@link PersistenceService}. It persists item values
 * using the <a href="https://aws.amazon.com/dynamodb/">Amazon DynamoDB</a> database. The states (
 * {@link State}) of an {@link Item} are persisted in DynamoDB tables.
 *
 * The service creates tables automatically, one for numbers, and one for strings.
 *
 * @see AbstractDynamoDBItem#fromStateNew for details how different items are persisted
 *
 * @author Sami Salonen - Initial contribution
 * @author Kai Kreuzer - Migration to 3.x
 *
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.dynamodb", //
        property = Constants.SERVICE_PID + "=org.openhab.dynamodb")
@ConfigurableService(category = "persistence", label = "DynamoDB Persistence Service", description_uri = DynamoDBPersistenceService.CONFIG_URI)
public class DynamoDBPersistenceService implements QueryablePersistenceService {

    private static final int MAX_CONCURRENCY = 100;

    protected static final String CONFIG_URI = "persistence:dynamodb";

    private static final String DYNAMODB_THREADPOOL_NAME = "dynamodbPersistenceService";

    private final ItemRegistry itemRegistry;
    private final UnitProvider unitProvider;
    private @Nullable DynamoDbEnhancedAsyncClient client;
    private @Nullable DynamoDbAsyncClient lowLevelClient;
    private static final Logger logger = LoggerFactory.getLogger(DynamoDBPersistenceService.class);
    private boolean isProperlyConfigured;
    private @Nullable DynamoDBConfig dbConfig;
    private @Nullable DynamoDBTableNameResolver tableNameResolver;
    private final ExecutorService executor = ThreadPoolManager.getPool(DYNAMODB_THREADPOOL_NAME);
    private static final Duration TIMEOUT_API_CALL = Duration.ofSeconds(60);
    private static final Duration TIMEOUT_API_CALL_ATTEMPT = Duration.ofSeconds(5);
    private Map<Class<? extends DynamoDBItem<?>>, DynamoDbAsyncTable<? extends DynamoDBItem<?>>> tableCache = new ConcurrentHashMap<>(
            2);

    private @Nullable URI endpointOverride;

    void overrideConfig(AwsRequestOverrideConfiguration.Builder config) {
        config.apiCallAttemptTimeout(TIMEOUT_API_CALL_ATTEMPT).apiCallTimeout(TIMEOUT_API_CALL);
    }

    void overrideConfig(ClientOverrideConfiguration.Builder config) {
        DynamoDBConfig localDbConfig = dbConfig;
        config.apiCallAttemptTimeout(TIMEOUT_API_CALL_ATTEMPT).apiCallTimeout(TIMEOUT_API_CALL);
        if (localDbConfig != null) {
            localDbConfig.getRetryPolicy().ifPresent(config::retryPolicy);
        }
    }

    @Activate
    public DynamoDBPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference UnitProvider unitProvider) {
        this.itemRegistry = itemRegistry;
        this.unitProvider = unitProvider;
    }

    /**
     * For tests
     */
    DynamoDBPersistenceService(final ItemRegistry itemRegistry, final UnitProvider unitProvider,
            @Nullable URI endpointOverride) {
        this.itemRegistry = itemRegistry;
        this.unitProvider = unitProvider;
        this.endpointOverride = endpointOverride;
    }

    /**
     * For tests
     */
    @Nullable
    URI getEndpointOverride() {
        return endpointOverride;
    }

    @Nullable
    DynamoDbAsyncClient getLowLevelClient() {
        return lowLevelClient;
    }

    ExecutorService getExecutor() {
        return executor;
    }

    @Nullable
    DynamoDBTableNameResolver getTableNameResolver() {
        return tableNameResolver;
    }

    @Nullable
    DynamoDBConfig getDbConfig() {
        return dbConfig;
    }

    @Activate
    public void activate(final @Nullable BundleContext bundleContext, final Map<String, Object> config) {
        disconnect();
        DynamoDBConfig localDbConfig = dbConfig = DynamoDBConfig.fromConfig(config);
        if (localDbConfig == null) {
            // Configuration was invalid. Abort service activation.
            // Error is already logger in fromConfig.
            return;
        }
        tableNameResolver = new DynamoDBTableNameResolver(localDbConfig.getTableRevision(), localDbConfig.getTable(),
                localDbConfig.getTablePrefixLegacy());
        try {
            if (!ensureClient()) {
                logger.error("Error creating dynamodb database client. Aborting service activation.");
                return;
            }
        } catch (Exception e) {
            logger.error("Error constructing dynamodb client", e);
            return;
        }

        isProperlyConfigured = true;
        logger.debug("dynamodb persistence service activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("dynamodb persistence service deactivated");
        logIfManyQueuedTasks();
        disconnect();
    }

    /**
     * Initializes Dynamo DB client and determines schema
     *
     * If construction fails, error is logged and false is returned.
     *
     * @return whether initialization was successful.
     */
    private boolean ensureClient() {
        DynamoDBConfig localDbConfig = dbConfig;
        if (localDbConfig == null) {
            return false;
        }
        if (client == null) {
            try {
                synchronized (this) {
                    if (this.client != null) {
                        return true;
                    }
                    DynamoDbAsyncClientBuilder lowlevelClientBuilder = DynamoDbAsyncClient.builder()
                            .defaultsMode(DefaultsMode.STANDARD)
                            .credentialsProvider(StaticCredentialsProvider.create(localDbConfig.getCredentials()))
                            .httpClient(NettyNioAsyncHttpClient.builder().maxConcurrency(MAX_CONCURRENCY).build())
                            .asyncConfiguration(
                                    ClientAsyncConfiguration.builder()
                                            .advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR,
                                                    executor)
                                            .build())
                            .overrideConfiguration(this::overrideConfig).region(localDbConfig.getRegion());
                    if (endpointOverride != null) {
                        logger.debug("DynamoDB has been overriden to {}", endpointOverride);
                        lowlevelClientBuilder.endpointOverride(endpointOverride);
                    }
                    DynamoDbAsyncClient lowlevelClient = lowlevelClientBuilder.build();
                    client = DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(lowlevelClient).build();
                    this.lowLevelClient = lowlevelClient;
                }
            } catch (Exception e) {
                logger.error("Error constructing dynamodb client", e);
                return false;
            }
        }
        return true;
    }

    private CompletableFuture<Boolean> resolveTableSchema() {
        DynamoDBTableNameResolver localTableNameResolver = tableNameResolver;
        DynamoDbAsyncClient localLowLevelClient = lowLevelClient;
        if (localTableNameResolver == null || localLowLevelClient == null) {
            throw new IllegalStateException("tableNameResolver or localLowLevelClient not available");
        }
        if (localTableNameResolver.isFullyResolved()) {
            return CompletableFuture.completedFuture(true);
        } else {
            synchronized (localTableNameResolver) {
                if (localTableNameResolver.isFullyResolved()) {
                    return CompletableFuture.completedFuture(true);
                }
                return localTableNameResolver.resolveSchema(localLowLevelClient,
                        b -> b.overrideConfiguration(this::overrideConfig), executor).thenApplyAsync(resolved -> {
                            if (resolved && localTableNameResolver.getTableSchema() == ExpectedTableSchema.LEGACY) {
                                logger.warn(
                                        "Using legacy table format. Is it recommended to migrate to the new table format: specify the 'table' parameter and unset the old 'tablePrefix' parameter.");
                            }
                            return resolved;
                        }, executor);
            }
        }
    }

    private <T extends DynamoDBItem<?>> DynamoDbAsyncTable<T> getTable(Class<T> dtoClass) {
        DynamoDbEnhancedAsyncClient localClient = client;
        DynamoDBTableNameResolver localTableNameResolver = tableNameResolver;
        if (!ensureClient() || localClient == null || localTableNameResolver == null) {
            throw new IllegalStateException("Client not ready");
        }
        ExpectedTableSchema expectedTableSchemaRevision = localTableNameResolver.getTableSchema();
        String tableName = localTableNameResolver.fromClass(dtoClass);
        final TableSchema<T> schema = getDynamoDBTableSchema(dtoClass, expectedTableSchemaRevision);
        @SuppressWarnings("unchecked") // OK since this is the only place tableCache is populated
        DynamoDbAsyncTable<T> table = (DynamoDbAsyncTable<T>) tableCache.computeIfAbsent(dtoClass,
                clz -> localClient.table(tableName, schema));
        if (table == null) {
            // Invariant. To make null checker happy
            throw new IllegalStateException();
        }
        return table;
    }

    private static <T extends DynamoDBItem<?>> TableSchema<T> getDynamoDBTableSchema(Class<T> dtoClass,
            ExpectedTableSchema expectedTableSchemaRevision) {
        if (dtoClass.equals(DynamoDBBigDecimalItem.class)) {
            @SuppressWarnings("unchecked") // OK thanks to above conditional
            TableSchema<T> schema = (TableSchema<T>) (expectedTableSchemaRevision == ExpectedTableSchema.NEW
                    ? DynamoDBBigDecimalItem.TABLE_SCHEMA_NEW
                    : DynamoDBBigDecimalItem.TABLE_SCHEMA_LEGACY);
            return schema;
        } else if (dtoClass.equals(DynamoDBStringItem.class)) {
            @SuppressWarnings("unchecked") // OK thanks to above conditional
            TableSchema<T> schema = (TableSchema<T>) (expectedTableSchemaRevision == ExpectedTableSchema.NEW
                    ? DynamoDBStringItem.TABLE_SCHEMA_NEW
                    : DynamoDBStringItem.TABLE_SCHEMA_LEGACY);
            return schema;
        } else {
            throw new IllegalStateException("Unknown DTO class. Bug");
        }
    }

    private void disconnect() {
        DynamoDbAsyncClient localLowLevelClient = lowLevelClient;
        if (client == null || localLowLevelClient == null) {
            return;
        }
        localLowLevelClient.close();
        lowLevelClient = null;
        client = null;
        dbConfig = null;
        tableNameResolver = null;
        isProperlyConfigured = false;
        tableCache.clear();
    }

    protected boolean isReadyToStore() {
        return isProperlyConfigured && ensureClient();
    }

    @Override
    public String getId() {
        return "dynamodb";
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "DynamoDB";
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        logIfManyQueuedTasks();
        Instant start = Instant.now();
        String filterDescription = filterToString(filter);
        logger.trace("Got a query with filter {}", filterDescription);
        DynamoDbEnhancedAsyncClient localClient = client;
        DynamoDBTableNameResolver localTableNameResolver = tableNameResolver;
        if (!isProperlyConfigured) {
            logger.debug("Configuration for dynamodb not yet loaded or broken. Returning empty query results.");
            return Collections.emptyList();
        }
        if (!ensureClient() || localClient == null || localTableNameResolver == null) {
            logger.warn("DynamoDB not connected. Returning empty query results.");
            return Collections.emptyList();
        }

        //
        // Resolve unclear table schema if needed
        //
        try {
            Boolean resolved = resolveTableSchema().get();
            if (!resolved) {
                logger.warn("Table schema not resolved, cannot query data.");
                return Collections.emptyList();
            }
        } catch (InterruptedException e) {
            logger.warn("Table schema resolution interrupted, cannot query data");
            return Collections.emptyList();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            logger.warn("Table schema resolution errored, cannot query data: {} {}",
                    cause == null ? e.getClass().getSimpleName() : cause.getClass().getSimpleName(),
                    cause == null ? e.getMessage() : cause.getMessage());
            return Collections.emptyList();
        }
        try {
            //
            // Proceed with query
            //
            String itemName = filter.getItemName();
            if (itemName == null) {
                logger.warn("Item name is missing in filter {}", filter);
                return List.of();
            }
            Item item = getItemFromRegistry(itemName);
            if (item == null) {
                logger.warn("Could not get item {} from registry! Returning empty query results.", itemName);
                return Collections.emptyList();
            }
            if (item instanceof GroupItem groupItem) {
                item = groupItem.getBaseItem();
                logger.debug("Item is instanceof GroupItem '{}'", itemName);
                if (item == null) {
                    logger.debug("BaseItem of GroupItem is null. Ignore and give up!");
                    return Collections.emptyList();
                }
                if (item instanceof GroupItem) {
                    logger.debug("BaseItem of GroupItem is a GroupItem too. Ignore and give up!");
                    return Collections.emptyList();
                }
            }
            boolean legacy = localTableNameResolver.getTableSchema() == ExpectedTableSchema.LEGACY;
            Class<? extends DynamoDBItem<?>> dtoClass = AbstractDynamoDBItem.getDynamoItemClass(item.getClass(),
                    legacy);
            String tableName = localTableNameResolver.fromClass(dtoClass);
            DynamoDbAsyncTable<? extends DynamoDBItem<?>> table = getTable(dtoClass);
            logger.debug("Item {} (of type {}) will be tried to query using DTO class {} from table {}", itemName,
                    item.getClass().getSimpleName(), dtoClass.getSimpleName(), tableName);

            QueryEnhancedRequest queryExpression = DynamoDBQueryUtils.createQueryExpression(dtoClass,
                    localTableNameResolver.getTableSchema(), item, filter, unitProvider);

            CompletableFuture<List<DynamoDBItem<?>>> itemsFuture = new CompletableFuture<>();
            final SdkPublisher<? extends DynamoDBItem<?>> itemPublisher = table.query(queryExpression).items();
            Subscriber<DynamoDBItem<?>> pageSubscriber = new PageOfInterestSubscriber<DynamoDBItem<?>>(itemsFuture,
                    filter.getPageNumber(), filter.getPageSize());
            itemPublisher.subscribe(pageSubscriber);
            // NumberItem.getUnit() is expensive, we avoid calling it in the loop
            // by fetching the unit here.
            final Item localItem = item;
            final Unit<?> itemUnit = localItem instanceof NumberItem ni ? ni.getUnit() : null;
            try {
                @SuppressWarnings("null")
                List<HistoricItem> results = itemsFuture.get().stream().map(dynamoItem -> {
                    HistoricItem historicItem = dynamoItem.asHistoricItem(localItem, itemUnit);
                    if (historicItem == null) {
                        logger.warn(
                                "Dynamo item {} serialized state '{}' cannot be converted to item {} {}. Item type changed since persistence. Ignoring",
                                dynamoItem.getClass().getSimpleName(), dynamoItem.getState(),
                                localItem.getClass().getSimpleName(), localItem.getName());
                        return null;
                    }
                    logger.trace("Dynamo item {} converted to historic item: {}", localItem, historicItem);
                    return historicItem;
                }).filter(value -> value != null).collect(Collectors.toList());
                logger.debug("Query completed in {} ms. Filter was {}",
                        Duration.between(start, Instant.now()).toMillis(), filterDescription);
                return results;
            } catch (InterruptedException e) {
                logger.warn("Query interrupted. Filter was {}", filterDescription);
                return Collections.emptyList();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof ResourceNotFoundException) {
                    logger.trace("Query failed since the DynamoDB table '{}' does not exist. Filter was {}", tableName,
                            filterDescription);
                } else if (logger.isTraceEnabled()) {
                    logger.trace("Query failed. Filter was {}", filterDescription, e);
                } else {
                    logger.warn("Query failed {} {}. Filter was {}",
                            cause == null ? e.getClass().getSimpleName() : cause.getClass().getSimpleName(),
                            cause == null ? e.getMessage() : cause.getMessage(), filterDescription);
                }
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Unexpected error with query having filter {}: {} {}. Returning empty query results.",
                    filterDescription, e.getClass().getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves the item for the given name from the item registry
     *
     * @param itemName
     * @return item with the given name, or null if no such item exists in item registry.
     */
    private @Nullable Item getItemFromRegistry(String itemName) {
        try {
            return itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e1) {
            return null;
        }
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.RESTORE, PersistenceStrategy.Globals.CHANGE);
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        // Timestamp and capture state immediately as rest of the store is asynchronous (state might change in between)
        ZonedDateTime time = ZonedDateTime.now();

        logIfManyQueuedTasks();
        if (!(item instanceof GenericItem)) {
            return;
        }
        if (item.getState() instanceof UnDefType) {
            logger.debug("Undefined item state received. Not storing item {}.", item.getName());
            return;
        }
        if (!isReadyToStore()) {
            logger.warn("Not ready to store (config error?), not storing item {}.", item.getName());
            return;
        }
        // Get Item describing the real type of data
        // With non-group items this is same as the argument item. With Group items, this is item describing the type of
        // state stored in the group.
        final Item itemTemplate;
        try {
            itemTemplate = getEffectiveItem(item);
        } catch (IllegalStateException e) {
            // Exception is raised when underlying item type cannot be determined with Group item
            // Logged already
            return;
        }

        String effectiveName = (alias != null) ? alias : item.getName();

        // We do not want to rely item.state since async context below can execute much later.
        // We 'copy' the item for local use. copyItem also normalizes the unit with NumberItems.
        final GenericItem copiedItem = copyItem(itemTemplate, item, effectiveName, null, unitProvider);

        resolveTableSchema().thenAcceptAsync(resolved -> {
            if (!resolved) {
                logger.warn("Table schema not resolved, not storing item {}.", copiedItem.getName());
                return;
            }

            DynamoDbEnhancedAsyncClient localClient = client;
            DynamoDbAsyncClient localLowlevelClient = lowLevelClient;
            DynamoDBConfig localConfig = dbConfig;
            DynamoDBTableNameResolver localTableNameResolver = tableNameResolver;
            if (!isProperlyConfigured || localClient == null || localLowlevelClient == null || localConfig == null
                    || localTableNameResolver == null) {
                logger.warn("Not ready to store (config error?), not storing item {}.", item.getName());
                return;
            }

            Integer expireDays = localConfig.getExpireDays();

            final DynamoDBItem<?> dto;
            switch (localTableNameResolver.getTableSchema()) {
                case NEW:
                    dto = AbstractDynamoDBItem.fromStateNew(copiedItem, time, expireDays);
                    break;
                case LEGACY:
                    dto = AbstractDynamoDBItem.fromStateLegacy(copiedItem, time);
                    break;
                default:
                    throw new IllegalStateException("Unexpected. Bug");
            }
            logger.trace("store() called with item {} {} '{}', which was converted to DTO {}",
                    copiedItem.getClass().getSimpleName(), effectiveName, copiedItem.getState(), dto);
            dto.accept(new DynamoDBItemVisitor<TableCreatingPutItem<? extends DynamoDBItem<?>>>() {

                @Override
                public TableCreatingPutItem<? extends DynamoDBItem<?>> visit(
                        DynamoDBBigDecimalItem dynamoBigDecimalItem) {
                    return new TableCreatingPutItem<DynamoDBBigDecimalItem>(DynamoDBPersistenceService.this,
                            dynamoBigDecimalItem, getTable(DynamoDBBigDecimalItem.class));
                }

                @Override
                public TableCreatingPutItem<? extends DynamoDBItem<?>> visit(DynamoDBStringItem dynamoStringItem) {
                    return new TableCreatingPutItem<DynamoDBStringItem>(DynamoDBPersistenceService.this,
                            dynamoStringItem, getTable(DynamoDBStringItem.class));
                }
            }).putItemAsync();
        }, executor).exceptionally(e -> {
            logger.error("Unexcepted error", e);
            return null;
        });
    }

    private Item getEffectiveItem(Item item) {
        final Item effectiveItem;
        if (item instanceof GroupItem groupItem) {
            Item baseItem = groupItem.getBaseItem();
            if (baseItem == null) {
                // if GroupItem:<ItemType> is not defined in
                // *.items using StringType
                logger.debug(
                        "Cannot detect ItemType for {} because the GroupItems' base type isn't set in *.items File.",
                        item.getName());
                Iterator<Item> firstGroupMemberItem = groupItem.getMembers().iterator();
                if (firstGroupMemberItem.hasNext()) {
                    effectiveItem = firstGroupMemberItem.next();
                } else {
                    throw new IllegalStateException("GroupItem " + item.getName()
                            + " does not have children nor base item set, cannot determine underlying item type. Aborting!");
                }
            } else {
                effectiveItem = baseItem;
            }
        } else {
            effectiveItem = item;
        }
        return effectiveItem;
    }

    /**
     * Copy item and optionally override name and state
     *
     * State is normalized to source item's unit with Quantity NumberItems and QuantityTypes
     *
     * @param itemTemplate 'template item' to be used to construct the new copy. It is also used to determine UoM unit
     *            and get GenericItem.type
     * @param item item that is used to acquire name and state
     * @param nameOverride name override for the resulting copy
     * @param stateOverride state override for the resulting copy
     * @param unitProvider the unit provider for number with dimension
     * @throws IllegalArgumentException when state is QuantityType and not compatible with item
     */
    static GenericItem copyItem(Item itemTemplate, Item item, @Nullable String nameOverride,
            @Nullable State stateOverride, UnitProvider unitProvider) {
        final GenericItem copiedItem;
        try {
            if (itemTemplate instanceof NumberItem) {
                copiedItem = (GenericItem) itemTemplate.getClass()
                        .getDeclaredConstructor(String.class, String.class, UnitProvider.class)
                        .newInstance(itemTemplate.getType(), nameOverride == null ? item.getName() : nameOverride,
                                unitProvider);
            } else {
                copiedItem = (GenericItem) itemTemplate.getClass().getDeclaredConstructor(String.class)
                        .newInstance(nameOverride == null ? item.getName() : nameOverride);
            }

        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalArgumentException(item.toString(), e);
        }
        State state = stateOverride == null ? item.getState() : stateOverride;
        if (state instanceof QuantityType<?> type && itemTemplate instanceof NumberItem numberItem) {
            Unit<?> itemUnit = numberItem.getUnit();
            if (itemUnit != null) {
                State convertedState = type.toUnit(itemUnit);
                if (convertedState == null) {
                    logger.error("Unexpected unit conversion failure: {} to item unit {}", state, itemUnit);
                    throw new IllegalArgumentException(
                            String.format("Unexpected unit conversion failure: %s to item unit %s", state, itemUnit));
                }
                state = convertedState;
            }
        }
        copiedItem.setState(state);
        return copiedItem;
    }

    private void logIfManyQueuedTasks() {
        if (executor instanceof ThreadPoolExecutor localExecutor) {
            if (localExecutor.getQueue().size() >= 5) {
                logger.trace("executor queue size: {}, remaining space {}. Active threads {}",
                        localExecutor.getQueue().size(), localExecutor.getQueue().remainingCapacity(),
                        localExecutor.getActiveCount());
            } else if (localExecutor.getQueue().size() >= 50) {
                logger.warn(
                        "Many ({}) tasks queued in executor! This might be sign of bad design or bug in the addon code.",
                        localExecutor.getQueue().size());
            }
        }
    }

    private String filterToString(FilterCriteria filter) {
        return String.format(
                "FilterCriteria@%s(item=%s, pageNumber=%d, pageSize=%d, time=[%s, %s, %s], state=[%s, %s of %s] )",
                System.identityHashCode(filter), filter.getItemName(), filter.getPageNumber(), filter.getPageSize(),
                filter.getBeginDate(), filter.getEndDate(), filter.getOrdering(), filter.getOperator(),
                filter.getState(), filter.getState() == null ? "null" : filter.getState().getClass().getSimpleName());
    }
}
