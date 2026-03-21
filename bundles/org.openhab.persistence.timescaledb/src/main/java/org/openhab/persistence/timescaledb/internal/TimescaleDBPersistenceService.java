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
package org.openhab.persistence.timescaledb.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * TimescaleDB persistence service for openHAB.
 *
 * <p>
 * Implements {@link ModifiablePersistenceService} to support store, query, and remove operations
 * against a TimescaleDB (PostgreSQL extension) hypertable.
 *
 * <p>
 * Item names are cached in-memory ({@code name → item_id}) to avoid a SELECT on every
 * {@link #store} call. The cache is populated lazily on first store per item.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
@Component(service = { PersistenceService.class, QueryablePersistenceService.class, ModifiablePersistenceService.class,
        TimescaleDBPersistenceService.class }, configurationPid = "org.openhab.persistence.timescaledb", configurationPolicy = ConfigurationPolicy.REQUIRE, property = Constants.SERVICE_PID
                + "=org.openhab.persistence.timescaledb")
@ConfigurableService(category = "persistence", label = "TimescaleDB Persistence Service", description_uri = TimescaleDBPersistenceService.CONFIG_URI)
public class TimescaleDBPersistenceService implements ModifiablePersistenceService {

    static final String CONFIG_URI = "persistence:timescaledb";

    private static final Logger LOGGER = LoggerFactory.getLogger(TimescaleDBPersistenceService.class);
    private static final String THREAD_POOL_NAME = "timescaledb";

    private static final String SERVICE_ID = "timescaledb";
    private static final String SERVICE_LABEL = "TimescaleDB";

    // item name → item_id, populated lazily
    private final Map<String, Integer> itemIdCache = new ConcurrentHashMap<>();

    private final ItemRegistry itemRegistry;
    private final TimescaleDBMetadataService metadataService;

    private @Nullable HikariDataSource dataSource;
    private @Nullable ScheduledFuture<?> downsampleJob;
    private @Nullable TimescaleDBDownsampleJob downsampleJobInstance;

    @Activate
    public TimescaleDBPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference TimescaleDBMetadataService metadataService) {
        this.itemRegistry = itemRegistry;
        this.metadataService = metadataService;
    }

    /** Package-private constructor for unit tests — skips OSGi activation, allows injecting a DataSource. */
    TimescaleDBPersistenceService(ItemRegistry itemRegistry, TimescaleDBMetadataService metadataService,
            @Nullable HikariDataSource dataSource) {
        this.itemRegistry = itemRegistry;
        this.metadataService = metadataService;
        this.dataSource = dataSource;
    }

    @Activate
    public void activate(final Map<String, Object> config) {
        String url = (String) config.getOrDefault("url", "");
        if (url.isBlank()) {
            LOGGER.warn("TimescaleDB persistence not configured: missing 'url'. "
                    + "Configure org.openhab.persistence.timescaledb:url.");
            return;
        }

        String user = (String) config.getOrDefault("user", "openhab");
        String password = (String) config.getOrDefault("password", "");
        int maxConnections = parseIntConfig(config, "maxConnections", 5);
        int connectTimeout = parseIntConfig(config, "connectTimeout", 5000);
        String chunkInterval = (String) config.getOrDefault("chunkInterval", "7 days");
        int retentionDays = parseIntConfig(config, "retentionDays", 0);
        int compressionAfterDays = parseIntConfig(config, "compressionAfterDays", 0);

        LOGGER.debug(
                "Activating TimescaleDB persistence: url={}, user={}, maxConnections={}, "
                        + "chunkInterval={}, retentionDays={}, compressionAfterDays={}",
                url, user, maxConnections, chunkInterval, retentionDays, compressionAfterDays);

        HikariDataSource ds;
        try {
            ds = createDataSource(url, user, password, maxConnections, connectTimeout);
        } catch (Exception e) {
            LOGGER.error("Failed to create TimescaleDB connection pool: {}", e.getMessage(), e);
            return;
        }
        dataSource = ds;

        try (Connection conn = ds.getConnection()) {
            TimescaleDBSchema.initialize(conn, chunkInterval, compressionAfterDays, retentionDays);
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize TimescaleDB schema: {}", e.getMessage(), e);
            ds.close();
            dataSource = null;
            return;
        }

        if (compressionAfterDays > 0) {
            LOGGER.warn("TimescaleDB: compressionAfterDays={} is set. Ensure all per-item retainRawDays "
                    + "are less than compressionAfterDays, otherwise downsampling will attempt to write into "
                    + "already-compressed (read-only) chunks and cause SQLExceptions.", compressionAfterDays);
        }

        // Schedule the daily downsampling job via the openHAB shared thread pool
        TimescaleDBDownsampleJob job = new TimescaleDBDownsampleJob(ds, metadataService);
        downsampleJobInstance = job;
        long initialDelay = secondsUntilMidnight();
        downsampleJob = ThreadPoolManager.getScheduledPool(THREAD_POOL_NAME).scheduleWithFixedDelay(job, initialDelay,
                TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        LOGGER.info("Downsampling job scheduled: first run in {}s, then every 24h", initialDelay);

        LOGGER.info("TimescaleDB persistence service activated");
    }

    /**
     * Triggers the downsampling job immediately in the calling thread.
     * Intended for use by the Karaf console command for on-demand testing.
     *
     * @return {@code true} if the job ran, {@code false} if the service is not yet activated.
     */
    public boolean runDownsampleNow() {
        TimescaleDBDownsampleJob job = downsampleJobInstance;
        if (job == null) {
            return false;
        }
        job.run();
        return true;
    }

    @Deactivate
    public void deactivate() {
        LOGGER.debug("Deactivating TimescaleDB persistence service");
        itemIdCache.clear();

        ScheduledFuture<?> job = downsampleJob;
        if (job != null) {
            job.cancel(false);
            downsampleJob = null;
        }
        downsampleJobInstance = null;

        HikariDataSource ds = dataSource;
        if (ds != null) {
            ds.close();
            dataSource = null;
        }
        LOGGER.info("TimescaleDB persistence service deactivated");
    }

    // -------------------------------------------------------------------------
    // PersistenceService
    // -------------------------------------------------------------------------

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_LABEL;
    }

    @Override
    public List<PersistenceStrategy> getSuggestedStrategies() {
        return Collections.emptyList();
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        store(item, ZonedDateTime.now(), item.getState(), alias);
    }

    // -------------------------------------------------------------------------
    // ModifiablePersistenceService (includes QueryablePersistenceService)
    // -------------------------------------------------------------------------

    @Override
    public void store(Item item, ZonedDateTime date, State state) {
        store(item, date, state, null);
    }

    @Override
    public void store(Item item, ZonedDateTime date, State state, @Nullable String alias) {
        if (state instanceof UnDefType) {
            LOGGER.trace("Skipping store for item '{}': state is UnDefType", item.getName());
            return;
        }

        TimescaleDBMapper.Row row = TimescaleDBMapper.toRow(state);
        if (row == null) {
            // Warning is already logged by the mapper
            return;
        }

        String name = alias != null ? alias : item.getName();
        @Nullable
        String label = item.getLabel();

        HikariDataSource ds = dataSource;
        if (ds == null) {
            LOGGER.warn("TimescaleDB data source not available — cannot store item '{}'", name);
            return;
        }

        try (Connection conn = ds.getConnection()) {
            int itemId = getOrCreateItemId(conn, name, label);
            TimescaleDBQuery.insert(conn, itemId, date, row);
        } catch (SQLException e) {
            LOGGER.error("Failed to store item '{}': {}", name, e.getMessage(), e);
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
            LOGGER.warn("FilterCriteria has no item name — returning empty query result");
            return Collections.emptyList();
        }

        String queryName = alias != null ? alias : itemName;

        @Nullable
        Integer itemId = itemIdCache.get(queryName);
        if (itemId == null) {
            HikariDataSource dsFallback = dataSource;
            if (dsFallback == null) {
                LOGGER.warn(
                        "TimescaleDB data source not available while resolving item_id for '{}' — returning empty query result",
                        queryName);
                return Collections.emptyList();
            }
            try (Connection connFallback = dsFallback.getConnection()) {
                Optional<Integer> resolved = TimescaleDBQuery.findItemId(connFallback, queryName);
                if (resolved.isEmpty()) {
                    LOGGER.debug("Item '{}' not present in TimescaleDB — returning empty query result", queryName);
                    return Collections.emptyList();
                }
                itemId = resolved.get();
                itemIdCache.put(queryName, itemId);
            } catch (SQLException e) {
                LOGGER.error("Failed to resolve item_id for item '{}': {}", queryName, e.getMessage(), e);
                return Collections.emptyList();
            }
        }

        Item item;
        try {
            item = itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            LOGGER.warn("Item '{}' not found in ItemRegistry — returning empty query result", itemName);
            return Collections.emptyList();
        }

        HikariDataSource ds = dataSource;
        if (ds == null) {
            LOGGER.warn("TimescaleDB data source not available — returning empty query result");
            return Collections.emptyList();
        }

        try (Connection conn = ds.getConnection()) {
            return TimescaleDBQuery.query(conn, item, itemId, filter);
        } catch (SQLException e) {
            LOGGER.error("Query failed for item '{}': {}", queryName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean remove(FilterCriteria filter) {
        String itemName = filter.getItemName();
        if (itemName == null) {
            LOGGER.warn("FilterCriteria has no item name — cannot remove data");
            return false;
        }

        @Nullable
        Integer itemId = itemIdCache.get(itemName);
        if (itemId == null) {
            HikariDataSource dsFallback = dataSource;
            if (dsFallback == null) {
                LOGGER.warn(
                        "TimescaleDB data source not available while resolving item_id for '{}' — cannot remove data",
                        itemName);
                return false;
            }
            try (Connection connFallback = dsFallback.getConnection()) {
                Optional<Integer> resolved = TimescaleDBQuery.findItemId(connFallback, itemName);
                if (resolved.isEmpty()) {
                    LOGGER.debug("Item '{}' not present in TimescaleDB — nothing to remove", itemName);
                    return false;
                }
                itemId = resolved.get();
                itemIdCache.put(itemName, itemId);
            } catch (SQLException e) {
                LOGGER.error("Failed to resolve item_id for item '{}': {}", itemName, e.getMessage(), e);
                return false;
            }
        }

        HikariDataSource ds = dataSource;
        if (ds == null) {
            LOGGER.warn("TimescaleDB data source not available — cannot remove data for item '{}'", itemName);
            return false;
        }

        try (Connection conn = ds.getConnection()) {
            int deleted = TimescaleDBQuery.remove(conn, itemId, filter);
            LOGGER.debug("Removed {} row(s) for item '{}'", deleted, itemName);
            return true;
        } catch (SQLException e) {
            LOGGER.error("Failed to remove data for item '{}': {}", itemName, e.getMessage(), e);
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private int getOrCreateItemId(Connection conn, String name, @Nullable String label) throws SQLException {
        Integer cached = itemIdCache.get(name);
        if (cached != null) {
            return cached;
        }
        int id = TimescaleDBQuery.getOrCreateItemId(conn, name, label);
        itemIdCache.put(name, id);
        return id;
    }

    private static HikariDataSource createDataSource(String url, String user, String password, int maxConnections,
            int connectTimeoutMs) {
        HikariConfig cfg = new HikariConfig();
        // Explicitly set the driver class name so HikariCP uses Class.forName() in the
        // bundle classloader instead of DriverManager.getDriver(). In an OSGi runtime
        // DriverManager lives in the boot classloader and cannot see the PostgreSQL driver
        // that is embedded in this bundle, which causes "Failed to get driver instance".
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(maxConnections);
        cfg.setConnectionTimeout(connectTimeoutMs);
        cfg.setPoolName("timescaledb-persistence");
        return new HikariDataSource(cfg);
    }

    static int parseIntConfig(Map<String, Object> config, String key, int defaultValue) {
        Object val = config.get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid integer value '{}' for config key '{}', using default {}", val, key, defaultValue);
            return defaultValue;
        }
    }

    static long secondsUntilMidnight() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay(now.getZone());
        return java.time.Duration.between(now, midnight).getSeconds();
    }
}
