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
package org.openhab.persistence.mapdb.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is the implementation of the MapDB {@link PersistenceService}. To learn more about MapDB please visit their
 * <a href="http://www.mapdb.org/">website</a>.
 *
 * @author Jens Viebig - Initial contribution
 * @author Martin Kühl - Port to 3.x
 */
@NonNullByDefault
@Component(service = { PersistenceService.class, QueryablePersistenceService.class })
public class MapDbPersistenceService implements QueryablePersistenceService {

    private static final String SERVICE_ID = "mapdb";
    private static final String SERVICE_LABEL = "MapDB";
    private static final Path DB_DIR = new File(OpenHAB.getUserDataFolder(), "persistence").toPath().resolve("mapdb");
    private static final Path BACKUP_DIR = DB_DIR.resolve("backup");
    private static final String DB_FILE_NAME = "storage.mapdb";

    private final Logger logger = LoggerFactory.getLogger(MapDbPersistenceService.class);

    private final ExecutorService threadPool = ThreadPoolManager.getPool(getClass().getSimpleName());

    /** holds the local instance of the MapDB database */

    private @NonNullByDefault({}) DB db;
    private @NonNullByDefault({}) Map<String, String> map;

    private transient Gson mapper = new GsonBuilder().registerTypeHierarchyAdapter(State.class, new StateTypeAdapter())
            .create();

    @Activate
    public void activate() {
        logger.debug("MapDB persistence service is being activated");

        try {
            Files.createDirectories(DB_DIR);
        } catch (IOException e) {
            logger.warn("Failed to create one or more directories in the path '{}'", DB_DIR);
            logger.warn("MapDB persistence service activation has failed.");
            return;
        }

        File dbFile = DB_DIR.resolve(DB_FILE_NAME).toFile();
        try {
            db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
            map = db.createTreeMap("itemStore").makeOrGet();
        } catch (RuntimeException re) {
            Throwable cause = re.getCause();
            if (cause instanceof ClassNotFoundException) {
                ClassNotFoundException cnf = (ClassNotFoundException) cause;
                logger.warn(
                        "The MapDB in {} is incompatible with openHAB {}: {}. A new and empty MapDB will be used instead.",
                        dbFile, OpenHAB.getVersion(), cnf.getMessage());

                try {
                    Files.createDirectories(BACKUP_DIR);
                } catch (IOException ioe) {
                    logger.warn("Failed to create one or more directories in the path '{}'", BACKUP_DIR);
                    logger.warn("MapDB persistence service activation has failed.");
                    return;
                }

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(DB_DIR)) {
                    long epochMilli = Instant.now().toEpochMilli();
                    for (Path path : stream) {
                        if (!Files.isDirectory(path)) {
                            Path newPath = BACKUP_DIR.resolve(epochMilli + "--" + path.getFileName());
                            Files.move(path, newPath);
                            logger.info("Moved incompatible MapDB file '{}' to '{}'", path, newPath);
                        }
                    }
                } catch (IOException ioe) {
                    logger.warn("Failed to read files from '{}': {}", DB_DIR, ioe.getMessage());
                    logger.warn("MapDB persistence service activation has failed.");
                    return;
                }

                db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
                map = db.createTreeMap("itemStore").makeOrGet();
            } else {
                logger.warn("Failed to create or open the MapDB: {}", re.getMessage());
                logger.warn("MapDB persistence service activation has failed.");
            }
        }
        logger.debug("MapDB persistence service is now activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("MapDB persistence service deactivated");
        if (db != null) {
            db.close();
        }
    }

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_LABEL;
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return map.values().stream().map(this::deserialize).flatMap(MapDbPersistenceService::streamOptional)
                .collect(Collectors.<PersistenceItemInfo> toUnmodifiableSet());
    }

    @Override
    public void store(Item item) {
        store(item, item.getName());
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        if (item.getState() instanceof UnDefType) {
            return;
        }

        // PersistenceManager passes SimpleItemConfiguration.alias which can be null
        String localAlias = alias == null ? item.getName() : alias;
        logger.debug("store called for {}", localAlias);

        State state = item.getState();
        MapDbItem mItem = new MapDbItem();
        mItem.setName(localAlias);
        mItem.setState(state);
        mItem.setTimestamp(new Date());
        String json = serialize(mItem);
        map.put(localAlias, json);
        commit();
        if (logger.isDebugEnabled()) {
            logger.debug("Stored '{}' with state '{}' as '{}' in MapDB database", localAlias, state, json);
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        String json = map.get(filter.getItemName());
        if (json == null) {
            return List.of();
        }
        Optional<MapDbItem> item = deserialize(json);
        return item.isPresent() ? List.of(item.get()) : List.of();
    }

    private String serialize(MapDbItem item) {
        return mapper.toJson(item);
    }

    @SuppressWarnings("null")
    private Optional<MapDbItem> deserialize(String json) {
        MapDbItem item = mapper.<MapDbItem> fromJson(json, MapDbItem.class);
        if (item == null || !item.isValid()) {
            logger.warn("Deserialized invalid item: {}", item);
            return Optional.empty();
        } else if (logger.isDebugEnabled()) {
            logger.debug("Deserialized '{}' with state '{}' from '{}'", item.getName(), item.getState(), json);
        }

        return Optional.of(item);
    }

    private void commit() {
        threadPool.submit(() -> db.commit());
    }

    private static <T> Stream<T> streamOptional(Optional<T> opt) {
        return opt.isPresent() ? Stream.of(opt.get()) : Stream.empty();
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.RESTORE, PersistenceStrategy.Globals.CHANGE);
    }
}
