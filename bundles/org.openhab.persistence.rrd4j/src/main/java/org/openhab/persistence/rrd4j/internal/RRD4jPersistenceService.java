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
package org.openhab.persistence.rrd4j.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemUtil;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceCronStrategy;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDb.Builder;
import org.rrd4j.core.RrdDbPool;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the RRD4j {@link PersistenceService}. To learn
 * more about RRD4j please visit their
 * <a href="https://github.com/rrd4j/rrd4j">website</a>.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Jan N. Klug - some improvements
 * @author Karel Goderis - remove TimerThread dependency
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.rrd4j", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class RRD4jPersistenceService implements QueryablePersistenceService {

    public static final String SERVICE_ID = "rrd4j";

    private static final String DEFAULT_OTHER = "default_other";
    private static final String DEFAULT_NUMERIC = "default_numeric";
    private static final String DEFAULT_QUANTIFIABLE = "default_quantifiable";

    private static final Set<String> SUPPORTED_TYPES = Set.of(CoreItemFactory.SWITCH, CoreItemFactory.CONTACT,
            CoreItemFactory.DIMMER, CoreItemFactory.NUMBER, CoreItemFactory.ROLLERSHUTTER, CoreItemFactory.COLOR);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory("RRD4j"));

    private final Map<String, RrdDefConfig> rrdDefs = new ConcurrentHashMap<>();

    private final ConcurrentSkipListMap<Long, Map<String, Double>> storageMap = new ConcurrentSkipListMap<>();

    private static final String DATASOURCE_STATE = "state";

    private static final Path DB_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "persistence", "rrd4j").toAbsolutePath();

    private static final RrdDbPool DATABASE_POOL = new RrdDbPool();

    private final Logger logger = LoggerFactory.getLogger(RRD4jPersistenceService.class);
    private final ItemRegistry itemRegistry;
    private boolean active = false;

    public static Path getDatabasePath(String name) {
        return DB_FOLDER.resolve(name + ".rrd");
    }

    public static RrdDbPool getDatabasePool() {
        return DATABASE_POOL;
    }

    private final ScheduledFuture<?> storeJob;

    @Activate
    public RRD4jPersistenceService(final @Reference ItemRegistry itemRegistry, Map<String, Object> config) {
        this.itemRegistry = itemRegistry;
        storeJob = scheduler.scheduleWithFixedDelay(() -> doStore(false), 1, 1, TimeUnit.SECONDS);
        modified(config);
        active = true;
    }

    @Modified
    protected void modified(final Map<String, Object> config) {
        // clean existing definitions
        rrdDefs.clear();

        // add default configurations

        RrdDefConfig defaultNumeric = new RrdDefConfig(DEFAULT_NUMERIC);
        // use 10 seconds as a step size for numeric values and allow a 10 minute silence between updates
        defaultNumeric.setDef("GAUGE,600,U,U,10");
        // define 5 different boxes:
        // 1. granularity of 10s for the last hour
        // 2. granularity of 1m for the last week
        // 3. granularity of 15m for the last year
        // 4. granularity of 1h for the last 5 years
        // 5. granularity of 1d for the last 10 years
        defaultNumeric
                .addArchives("LAST,0.5,1,360:LAST,0.5,6,10080:LAST,0.5,90,36500:LAST,0.5,360,43800:LAST,0.5,8640,3650");
        rrdDefs.put(DEFAULT_NUMERIC, defaultNumeric);

        RrdDefConfig defaultQuantifiable = new RrdDefConfig(DEFAULT_QUANTIFIABLE);
        // use 10 seconds as a step size for numeric values and allow a 10 minute silence between updates
        defaultQuantifiable.setDef("GAUGE,600,U,U,10");
        // define 5 different boxes:
        // 1. granularity of 10s for the last hour
        // 2. granularity of 1m for the last week
        // 3. granularity of 15m for the last year
        // 4. granularity of 1h for the last 5 years
        // 5. granularity of 1d for the last 10 years
        defaultQuantifiable.addArchives(
                "AVERAGE,0.5,1,360:AVERAGE,0.5,6,10080:AVERAGE,0.5,90,36500:AVERAGE,0.5,360,43800:AVERAGE,0.5,8640,3650");
        rrdDefs.put(DEFAULT_QUANTIFIABLE, defaultQuantifiable);

        RrdDefConfig defaultOther = new RrdDefConfig(DEFAULT_OTHER);
        // use 5 seconds as a step size for discrete values and allow a 1h silence between updates
        defaultOther.setDef("GAUGE,3600,U,U,5");
        // define 4 different boxes:
        // 1. granularity of 5s for the last hour
        // 2. granularity of 1m for the last week
        // 3. granularity of 15m for the last year
        // 4. granularity of 4h for the last 10 years
        defaultOther.addArchives("LAST,0.5,1,720:LAST,0.5,12,10080:LAST,0.5,180,35040:LAST,0.5,2880,21900");
        rrdDefs.put(DEFAULT_OTHER, defaultOther);

        if (config.isEmpty()) {
            logger.debug("using default configuration only");
            return;
        }

        Iterator<String> keys = config.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();

            if ("service.pid".equals(key) || "component.name".equals(key)) {
                // ignore service.pid and name
                continue;
            }

            String[] subkeys = key.split("\\.");
            if (subkeys.length != 2) {
                logger.debug("config '{}' should have the format 'name.configkey'", key);
                continue;
            }

            Object v = config.get(key);
            if (v instanceof String) {
                String value = (String) v;
                String name = subkeys[0].toLowerCase();
                String property = subkeys[1].toLowerCase();

                if (value.isBlank()) {
                    logger.trace("Config is empty: {}", property);
                    continue;
                } else {
                    logger.trace("Processing config: {} = {}", property, value);
                }

                RrdDefConfig rrdDef = rrdDefs.get(name);
                if (rrdDef == null) {
                    rrdDef = new RrdDefConfig(name);
                    rrdDefs.put(name, rrdDef);
                }

                try {
                    if ("def".equals(property)) {
                        rrdDef.setDef(value);
                    } else if ("archives".equals(property)) {
                        rrdDef.addArchives(value);
                    } else if ("items".equals(property)) {
                        rrdDef.addItems(value);
                    } else {
                        logger.debug("Unknown property {} : {}", property, value);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Ignoring illegal configuration: {}", e.getMessage());
                }
            }
        }

        for (RrdDefConfig rrdDef : rrdDefs.values()) {
            if (rrdDef.isValid()) {
                logger.debug("Created {}", rrdDef);
            } else {
                logger.info("Removing invalid definition {}", rrdDef);
                rrdDefs.remove(rrdDef.name);
            }
        }
    }

    @Deactivate
    protected void deactivate() {
        active = false;
        storeJob.cancel(false);

        // make sure we really store everything
        doStore(true);
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "RRD4j";
    }

    @Override
    public void store(final Item item, @Nullable final String alias) {
        if (!active) {
            logger.warn("Tried to store {} but service is not yet ready (or shutting down).", item);
            return;
        }

        if (!isSupportedItemType(item)) {
            logger.trace("Ignoring item '{}' since its type {} is not supported", item.getName(), item.getType());
            return;
        }
        final String name = alias == null ? item.getName() : alias;

        Double value;

        if (item instanceof NumberItem nItem && item.getState() instanceof QuantityType<?> qState) {
            Unit<? extends Quantity<?>> unit = nItem.getUnit();
            if (unit != null) {
                QuantityType<?> convertedState = qState.toUnit(unit);
                if (convertedState != null) {
                    value = convertedState.doubleValue();
                } else {
                    value = null;
                    logger.warn(
                            "Failed to convert state '{}' to unit '{}'. Please check your item definition for correctness.",
                            qState, unit);
                }
            } else {
                value = qState.doubleValue();
            }
        } else {
            DecimalType state = item.getStateAs(DecimalType.class);
            if (state != null) {
                value = state.toBigDecimal().doubleValue();
            } else {
                value = null;
            }
        }

        if (value == null) {
            // we could not convert the value
            return;
        }

        long now = System.currentTimeMillis() / 1000;
        Double oldValue = storageMap.computeIfAbsent(now, t -> new ConcurrentHashMap<>()).put(name, value);
        if (oldValue != null && !oldValue.equals(value)) {
            logger.debug(
                    "Discarding value {} for item {} with timestamp {} because a new value ({}) arrived with the same timestamp.",
                    oldValue, name, now, value);
        }
    }

    private void doStore(boolean force) {
        while (!storageMap.isEmpty()) {
            long timestamp = storageMap.firstKey();
            long now = System.currentTimeMillis() / 1000;
            if (now > timestamp || force) {
                // no new elements can be added for this timestamp because we are already past that time or the service
                // requires forced storing
                Map<String, Double> values = storageMap.pollFirstEntry().getValue();
                values.forEach((name, value) -> writePointToDatabase(name, value, timestamp));
            } else {
                return;
            }
        }
    }

    private synchronized void writePointToDatabase(String name, double value, long timestamp) {
        RrdDb db = null;
        try {
            db = getDB(name, true);
        } catch (Exception e) {
            logger.warn("Failed to open rrd4j database '{}' to store data ({})", name, e.toString());
        }
        if (db == null) {
            return;
        }

        ConsolFun function = getConsolidationFunction(db);
        if (function != ConsolFun.AVERAGE) {
            try {
                // we store the last value again, so that the value change
                // in the database is not interpolated, but
                // happens right at this spot
                if (timestamp - 1 > db.getLastUpdateTime()) {
                    // only do it if there is not already a value
                    double lastValue = db.getLastDatasourceValue(DATASOURCE_STATE);
                    if (!Double.isNaN(lastValue)) {
                        Sample sample = db.createSample();
                        sample.setTime(timestamp - 1);
                        sample.setValue(DATASOURCE_STATE, lastValue);
                        sample.update();
                        logger.debug("Stored '{}' as value '{}' with timestamp {} in rrd4j database (again)", name,
                                lastValue, timestamp - 1);
                    }
                }
            } catch (IOException e) {
                logger.debug("Error storing last value (again) for {}: {}", e.getMessage(), name);
            }
        }
        try {
            Sample sample = db.createSample();
            sample.setTime(timestamp);
            double storeValue = value;
            if (db.getDatasource(DATASOURCE_STATE).getType() == DsType.COUNTER) {
                // counter values must be adjusted by stepsize
                storeValue = value * db.getRrdDef().getStep();
            }
            sample.setValue(DATASOURCE_STATE, storeValue);
            sample.update();
            logger.debug("Stored '{}' as value '{}' with timestamp {} in rrd4j database", name, storeValue, timestamp);
        } catch (Exception e) {
            logger.warn("Could not persist '{}' to rrd4j database: {}", name, e.getMessage());
        }
        try {
            db.close();
        } catch (IOException e) {
            logger.debug("Error closing rrd4j database: {}", e.getMessage());
        }
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        ZonedDateTime filterBeginDate = filter.getBeginDate();
        ZonedDateTime filterEndDate = filter.getEndDate();
        if (filterBeginDate != null && filterEndDate != null && filterBeginDate.isAfter(filterEndDate)) {
            throw new IllegalArgumentException("begin (" + filterBeginDate + ") before end (" + filterEndDate + ")");
        }

        String itemName = filter.getItemName();
        if (itemName == null) {
            logger.warn("Item name is missing in filter {}", filter);
            return List.of();
        }
        logger.trace("Querying rrd4j database for item '{}'", itemName);

        RrdDb db = null;
        try {
            db = getDB(itemName, false);
        } catch (Exception e) {
            logger.warn("Failed to open rrd4j database '{}' for querying ({})", itemName, e.toString());
            return List.of();
        }
        if (db == null) {
            logger.debug("Could not find item '{}' in rrd4j database", itemName);
            return List.of();
        }

        Item item = null;
        Unit<?> unit = null;
        try {
            item = itemRegistry.getItem(itemName);
            if (item instanceof NumberItem) {
                // we already retrieve the unit here once as it is a very costly operation,
                // see https://github.com/openhab/openhab-addons/issues/8928
                unit = ((NumberItem) item).getUnit();
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Could not find item '{}' in registry", itemName);
        }

        long start = 0L;
        long end = filterEndDate == null ? System.currentTimeMillis() / 1000
                : filterEndDate.toInstant().getEpochSecond();

        try {
            if (filterBeginDate == null) {
                // as rrd goes back for years and gets more and more
                // inaccurate, we only support descending order
                // and a single return value
                // if there is no begin date is given - this case is
                // required specifically for the historicState()
                // query, which we want to support
                if (filter.getOrdering() == Ordering.DESCENDING && filter.getPageSize() == 1
                        && filter.getPageNumber() == 0) {
                    if (filterEndDate == null) {
                        // we are asked only for the most recent value!
                        double lastValue = db.getLastDatasourceValue(DATASOURCE_STATE);
                        if (!Double.isNaN(lastValue)) {
                            HistoricItem rrd4jItem = new RRD4jItem(itemName, mapToState(lastValue, item, unit),
                                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(db.getLastArchiveUpdateTime() * 1000),
                                            ZoneId.systemDefault()));
                            return List.of(rrd4jItem);
                        } else {
                            return List.of();
                        }
                    } else {
                        start = end;
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "rrd4j does not allow querys without a begin date, unless order is descending and a single value is requested");
                }
            } else {
                start = filterBeginDate.toInstant().getEpochSecond();
            }

            // do not call method {@link RrdDb#createFetchRequest(ConsolFun, long, long, long)} if start > end to avoid
            // an IAE to be thrown
            if (start > end) {
                logger.debug("Could not query rrd4j database for item '{}': start ({}) > end ({})", itemName, start,
                        end);
                return List.of();
            }

            FetchRequest request = db.createFetchRequest(getConsolidationFunction(db), start, end, 1);
            FetchData result = request.fetchData();

            List<HistoricItem> items = new ArrayList<>();
            long ts = result.getFirstTimestamp();
            long step = result.getRowCount() > 1 ? result.getStep() : 0;
            for (double value : result.getValues(DATASOURCE_STATE)) {
                if (!Double.isNaN(value) && (((ts >= start) && (ts <= end)) || (start == end))) {
                    RRD4jItem rrd4jItem = new RRD4jItem(itemName, mapToState(value, item, unit),
                            ZonedDateTime.ofInstant(Instant.ofEpochSecond(ts), ZoneId.systemDefault()));
                    items.add(rrd4jItem);
                }
                ts += step;
            }
            return items;
        } catch (IOException e) {
            logger.warn("Could not query rrd4j database for item '{}': {}", itemName, e.getMessage());
            return List.of();
        } finally {
            try {
                db.close();
            } catch (IOException e) {
                logger.debug("Error closing rrd4j database: {}", e.getMessage());
            }
        }
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return Set.of();
    }

    protected synchronized @Nullable RrdDb getDB(String alias, boolean createFileIfAbsent) {
        RrdDb db = null;
        Path path = getDatabasePath(alias);
        try {
            Builder builder = RrdDb.getBuilder();
            builder.setPool(DATABASE_POOL);

            if (Files.exists(path)) {
                // recreate the RrdDb instance from the file
                builder.setPath(path.toString());
                db = builder.build();
            } else if (createFileIfAbsent) {
                if (!Files.exists(DB_FOLDER)) {
                    Files.createDirectories(DB_FOLDER);
                }
                RrdDef rrdDef = getRrdDef(alias, path);
                if (rrdDef != null) {
                    // create a new database file
                    builder.setRrdDef(rrdDef);
                    db = builder.build();
                } else {
                    logger.debug(
                            "Did not create rrd4j database for item '{}' since no rrd definition could be determined. This is likely due to an unsupported item type.",
                            alias);
                }
            }
        } catch (IOException e) {
            logger.error("Could not create rrd4j database file '{}': {}", path, e.getMessage());
        } catch (RejectedExecutionException e) {
            // this happens if the system is shut down
            logger.debug("Could not create rrd4j database file '{}': {}", path, e.getMessage());
        }
        return db;
    }

    private @Nullable RrdDefConfig getRrdDefConfig(String itemName) {
        RrdDefConfig useRdc = null;
        for (Map.Entry<String, RrdDefConfig> e : rrdDefs.entrySet()) {
            // try to find special config
            RrdDefConfig rdc = e.getValue();
            if (rdc.appliesTo(itemName)) {
                useRdc = rdc;
                break;
            }
        }
        if (useRdc == null) { // not defined, use defaults
            try {
                Item item = itemRegistry.getItem(itemName);
                if (!isSupportedItemType(item)) {
                    return null;
                }
                if (item instanceof NumberItem) {
                    NumberItem numberItem = (NumberItem) item;
                    useRdc = numberItem.getDimension() != null ? rrdDefs.get(DEFAULT_QUANTIFIABLE)
                            : rrdDefs.get(DEFAULT_NUMERIC);
                } else {
                    useRdc = rrdDefs.get(DEFAULT_OTHER);
                }
            } catch (ItemNotFoundException e) {
                logger.debug("Could not find item '{}' in registry", itemName);
                return null;
            }
        }
        logger.trace("Using rrd definition '{}' for item '{}'.", useRdc, itemName);
        return useRdc;
    }

    private @Nullable RrdDef getRrdDef(String itemName, Path path) {
        RrdDef rrdDef = new RrdDef(path.toString());
        RrdDefConfig useRdc = getRrdDefConfig(itemName);
        if (useRdc != null) {
            rrdDef.setStep(useRdc.step);
            rrdDef.setStartTime(System.currentTimeMillis() / 1000 - useRdc.step);
            rrdDef.addDatasource(DATASOURCE_STATE, useRdc.dsType, useRdc.heartbeat, useRdc.min, useRdc.max);
            for (RrdArchiveDef rad : useRdc.archives) {
                rrdDef.addArchive(rad.fcn, rad.xff, rad.steps, rad.rows);
            }
            return rrdDef;
        } else {
            return null;
        }
    }

    public ConsolFun getConsolidationFunction(RrdDb db) {
        try {
            return db.getRrdDef().getArcDefs()[0].getConsolFun();
        } catch (IOException e) {
            return ConsolFun.MAX;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private State mapToState(double value, @Nullable Item item, @Nullable Unit unit) {
        if (item instanceof GroupItem) {
            item = ((GroupItem) item).getBaseItem();
        }

        if (item instanceof SwitchItem && !(item instanceof DimmerItem)) {
            return value == 0.0d ? OnOffType.OFF : OnOffType.ON;
        } else if (item instanceof ContactItem) {
            return value == 0.0d ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        } else if (item instanceof DimmerItem || item instanceof RollershutterItem || item instanceof ColorItem) {
            // make sure Items that need PercentTypes instead of DecimalTypes do receive the right information
            return new PercentType((int) Math.round(value * 100));
        } else if (item instanceof NumberItem) {
            if (unit != null) {
                return new QuantityType(value, unit);
            }
        }
        return new DecimalType(value);
    }

    private boolean isSupportedItemType(Item item) {
        if (item instanceof GroupItem) {
            final Item baseItem = ((GroupItem) item).getBaseItem();
            if (baseItem != null) {
                item = baseItem;
            }
        }

        return SUPPORTED_TYPES.contains(ItemUtil.getMainItemType(item.getType()));
    }

    public List<String> getRrdFiles() {
        try (Stream<Path> stream = Files.list(DB_FOLDER)) {
            return stream.filter(file -> !Files.isDirectory(file) && file.toFile().getName().endsWith(".rrd"))
                    .map(file -> file.toFile().getName()).collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    private static class RrdArchiveDef {
        public @Nullable ConsolFun fcn;
        public double xff;
        public int steps, rows;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(" " + fcn);
            sb.append(" xff = ").append(xff);
            sb.append(" steps = ").append(steps);
            sb.append(" rows = ").append(rows);
            return sb.toString();
        }
    }

    private class RrdDefConfig {
        public String name;
        public @Nullable DsType dsType;
        public int heartbeat, step;
        public double min, max;
        public List<RrdArchiveDef> archives;
        public List<String> itemNames;

        private boolean isInitialized;

        public RrdDefConfig(String name) {
            this.name = name;
            archives = new ArrayList<>();
            itemNames = new ArrayList<>();
            isInitialized = false;
        }

        public void setDef(String defString) {
            String[] opts = defString.split(",");
            if (opts.length != 5) { // check if correct number of parameters
                logger.warn("invalid number of parameters {}: {}", name, defString);
                return;
            }

            if ("ABSOLUTE".equals(opts[0])) { // dsType
                dsType = DsType.ABSOLUTE;
            } else if ("COUNTER".equals(opts[0])) {
                dsType = DsType.COUNTER;
            } else if ("DERIVE".equals(opts[0])) {
                dsType = DsType.DERIVE;
            } else if ("GAUGE".equals(opts[0])) {
                dsType = DsType.GAUGE;
            } else {
                logger.warn("{}: dsType {} not supported", name, opts[0]);
            }

            heartbeat = Integer.parseInt(opts[1]);

            if ("U".equals(opts[2])) {
                min = Double.NaN;
            } else {
                min = Double.parseDouble(opts[2]);
            }

            if ("U".equals(opts[3])) {
                max = Double.NaN;
            } else {
                max = Double.parseDouble(opts[3]);
            }

            step = Integer.parseInt(opts[4]);

            isInitialized = true; // successfully initialized

            return;
        }

        public void addArchives(String archivesString) {
            String splitArchives[] = archivesString.split(":");
            for (String archiveString : splitArchives) {
                String[] opts = archiveString.split(",");
                if (opts.length != 4) { // check if correct number of parameters
                    logger.warn("invalid number of parameters {}: {}", name, archiveString);
                    return;
                }
                RrdArchiveDef arc = new RrdArchiveDef();

                if ("AVERAGE".equals(opts[0])) {
                    arc.fcn = ConsolFun.AVERAGE;
                } else if ("MIN".equals(opts[0])) {
                    arc.fcn = ConsolFun.MIN;
                } else if ("MAX".equals(opts[0])) {
                    arc.fcn = ConsolFun.MAX;
                } else if ("LAST".equals(opts[0])) {
                    arc.fcn = ConsolFun.LAST;
                } else if ("FIRST".equals(opts[0])) {
                    arc.fcn = ConsolFun.FIRST;
                } else if ("TOTAL".equals(opts[0])) {
                    arc.fcn = ConsolFun.TOTAL;
                } else {
                    logger.warn("{}: consolidation function  {} not supported", name, opts[0]);
                }
                arc.xff = Double.parseDouble(opts[1]);
                arc.steps = Integer.parseInt(opts[2]);
                arc.rows = Integer.parseInt(opts[3]);
                archives.add(arc);
            }
        }

        public void addItems(String itemsString) {
            Collections.addAll(itemNames, itemsString.split(","));
        }

        public boolean appliesTo(String item) {
            return itemNames.contains(item);
        }

        public boolean isValid() { // a valid configuration must be initialized
            // and contain at least one function
            return isInitialized && !archives.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(name);
            sb.append(" = ").append(dsType);
            sb.append(" heartbeat = ").append(heartbeat);
            sb.append(" min/max = ").append(min).append("/").append(max);
            sb.append(" step = ").append(step);
            sb.append(" ").append(archives.size()).append(" archives(s) = [");
            for (RrdArchiveDef arc : archives) {
                sb.append(arc.toString());
            }
            sb.append("] ");
            sb.append(itemNames.size()).append(" items(s) = [");
            for (String item : itemNames) {
                sb.append(item).append(" ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.RESTORE, PersistenceStrategy.Globals.CHANGE,
                new PersistenceCronStrategy("everyMinute", "0 * * * * ?"));
    }
}
