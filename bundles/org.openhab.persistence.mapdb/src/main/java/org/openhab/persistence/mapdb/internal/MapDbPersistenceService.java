/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This is the implementation of the MapDB {@link PersistenceService}. To learn
 * more about MapDB please visit their <a
 * href="http://www.mapdb.org/">website</a>.
 *
 * @author Jens Viebig - Initial contribution
 * @author Martin Kühl - Port to Eclipse SmartHome
 */
@NonNullByDefault
@Component(service = { PersistenceService.class, QueryablePersistenceService.class })
public class MapDbPersistenceService implements QueryablePersistenceService {

    private static final String SERVICE_NAME = "mapdb";

    private static final String DB_FOLDER_NAME = ConfigConstants.getUserDataFolder() + File.separator + "persistence" + File.separator + "mapdb";

    private static final String DB_FILE_NAME = "storage.mapdb";

    private final Logger logger = LoggerFactory.getLogger(MapDbPersistenceService.class);

    @NonNullByDefault({})
    private ExecutorService threadPool;

    /** holds the local instance of the MapDB database */
    @NonNullByDefault({})
    private DB db;
    @NonNullByDefault({})
    private Map<String, String> map;

    private transient Gson mapper = new GsonBuilder()
            .registerTypeHierarchyAdapter(State.class, new StateTypeAdapter())
            .create();

    public void activate() {
        logger.debug("MapDB persistence service is being activated");

        threadPool = ThreadPoolManager.getPool(getClass().getSimpleName());

        File folder = new File(DB_FOLDER_NAME);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                logger.warn("Failed to create one or more directories in the path '{}'", DB_FOLDER_NAME);
                logger.warn("MapDB persistence service activation has failed.");
                return;
            }
        }

        File dbFile = new File(DB_FOLDER_NAME, DB_FILE_NAME);
        db = DBMaker.newFileDB(dbFile).closeOnJvmShutdown().make();
        map = db.createTreeMap("itemStore").makeOrGet();
        logger.debug("MapDB persistence service is now activated");
    }

    public void deactivate() {
        logger.debug("MapDB persistence service deactivated");
        if (db != null) {
            db.close();
        }
        threadPool.shutdown();
    }

    @Override
    public String getId() {
        return SERVICE_NAME;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_NAME;
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return map.values().stream()
                .map(this::deserialize)
                .flatMap(MapDbPersistenceService::streamOptional)
                .collect(Collectors.<PersistenceItemInfo>toSet());
    }

    @Override
    public void store(Item item) {
        store(item, item.getName());
    }

    @Override
    public void store(Item item, String alias) {
        if (item.getState() instanceof UnDefType) {
            return;
        }

        // PersistenceManager passes SimpleItemConfiguration.alias which can be null
        if (alias == null) {
            alias = item.getName();
        }
        logger.debug("store called for {}", alias);

        State state = item.getState();
        MapDbItem mItem = new MapDbItem();
        mItem.setName(alias);
        mItem.setState(state);
        mItem.setTimestamp(new Date());
        String json = serialize(mItem);
        map.put(alias, json);
        commit();
        logger.debug("Stored '{}' with state '{}' in MapDB database", alias, state.toString());
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        String json = map.get(filter.getItemName());
        if (json == null) {
            return Collections.emptyList();
        }
        Optional<MapDbItem> item = deserialize(json);
        if (!item.isPresent()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(item.get());
    }

    private String serialize(MapDbItem item) {
        return mapper.toJson(item);
    }

    private Optional<MapDbItem> deserialize(String json) {
        MapDbItem item = mapper.<MapDbItem>fromJson(json, MapDbItem.class);
        if (item == null || !item.isValid()) {
            logger.warn("Deserialized invalid item: {}", item);
            return Optional.empty();
        }
        return Optional.of(item);
    }

    private void commit() {
        threadPool.submit(() -> db.commit());
    }

    private static <T> Stream<T> streamOptional(Optional<T> opt) {
        if (!opt.isPresent()) {
            return Stream.empty();
        }
        return Stream.of(opt.get());
    }
}
