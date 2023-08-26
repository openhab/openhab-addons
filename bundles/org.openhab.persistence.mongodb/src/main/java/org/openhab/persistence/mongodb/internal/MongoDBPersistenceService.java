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
package org.openhab.persistence.mongodb.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * This is the implementation of the MongoDB {@link PersistenceService}.
 *
 * @author Thorsten Hoeger - Initial contribution
 * @author Stephan Brunner - Query fixes, Cleanup
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.mongodb", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MongoDBPersistenceService implements QueryablePersistenceService {

    private static final String FIELD_ID = "_id";
    private static final String FIELD_ITEM = "item";
    private static final String FIELD_REALNAME = "realName";
    private static final String FIELD_TIMESTAMP = "timestamp";
    private static final String FIELD_VALUE = "value";

    private final Logger logger = LoggerFactory.getLogger(MongoDBPersistenceService.class);

    private String url = "";
    private String db = "";
    private String collection = "";
    private boolean collectionPerItem;

    private boolean initialized = false;

    protected final ItemRegistry itemRegistry;

    private @Nullable MongoClient cl;

    @Activate
    public MongoDBPersistenceService(final @Reference ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    @Activate
    public void activate(final BundleContext bundleContext, final Map<String, Object> config) {
        @Nullable
        String configUrl = (String) config.get("url");
        logger.debug("MongoDB URL {}", configUrl);
        if (configUrl == null || configUrl.isBlank()) {
            logger.warn("The MongoDB database URL is missing - please configure the mongodb:url parameter.");
            return;
        }
        url = configUrl;

        @Nullable
        String configDb = (String) config.get("database");
        logger.debug("MongoDB database {}", configDb);
        if (configDb == null || configDb.isBlank()) {
            logger.warn("The MongoDB database name is missing - please configure the mongodb:database parameter.");
            return;
        }
        db = configDb;

        @Nullable
        String dbCollection = (String) config.get("collection");
        logger.debug("MongoDB collection {}", dbCollection);
        collection = dbCollection == null ? "" : dbCollection;
        collectionPerItem = dbCollection == null || dbCollection.isBlank();

        if (!tryConnectToDatabase()) {
            logger.warn("Failed to connect to MongoDB server. Trying to reconnect later.");
        }

        initialized = true;
    }

    @Deactivate
    public void deactivate(final int reason) {
        logger.debug("MongoDB persistence bundle stopping. Disconnecting from database.");
        disconnectFromDatabase();
    }

    @Override
    public String getId() {
        return "mongodb";
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "MongoDB";
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        // Don't log undefined/uninitialized data
        if (item.getState() instanceof UnDefType) {
            return;
        }

        // If we've not initialized the bundle, then return
        if (!initialized) {
            logger.warn("MongoDB not initialized");
            return;
        }

        // Connect to mongodb server if we're not already connected
        // If we can't connect, log.
        if (!tryConnectToDatabase()) {
            logger.warn(
                    "mongodb: No connection to database. Cannot persist item '{}'! Will retry connecting to database next time.",
                    item);
            return;
        }

        String realItemName = item.getName();
        String collectionName = collectionPerItem ? realItemName : this.collection;

        @Nullable
        DBCollection collection = connectToCollection(collectionName);

        if (collection == null) {
            // Logging is done in connectToCollection()
            return;
        }

        String name = (alias != null) ? alias : realItemName;
        Object value = this.convertValue(item.getState());

        DBObject obj = new BasicDBObject();
        obj.put(FIELD_ID, new ObjectId());
        obj.put(FIELD_ITEM, name);
        obj.put(FIELD_REALNAME, realItemName);
        obj.put(FIELD_TIMESTAMP, new Date());
        obj.put(FIELD_VALUE, value);
        collection.save(obj);

        logger.debug("MongoDB save {}={}", name, value);
    }

    private Object convertValue(State state) {
        Object value;
        if (state instanceof PercentType type) {
            value = type.toBigDecimal().doubleValue();
        } else if (state instanceof DateTimeType type) {
            value = Date.from(type.getZonedDateTime().toInstant());
        } else if (state instanceof DecimalType type) {
            value = type.toBigDecimal().doubleValue();
        } else {
            value = state.toString();
        }
        return value;
    }

    @Override
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return Collections.emptySet();
    }

    /**
     * Checks if we have a database connection.
     * Also tests if communication with the MongoDB-Server is available.
     *
     * @return true if connection has been established, false otherwise
     */
    private synchronized boolean isConnected() {
        if (cl == null) {
            return false;
        }

        // Also check if the connection is valid.
        // Network problems may cause failure sometimes,
        // even if the connection object was successfully created before.
        try {
            cl.getAddress();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * (Re)connects to the database
     *
     * @return True, if the connection was successfully established.
     */
    private synchronized boolean tryConnectToDatabase() {
        if (isConnected()) {
            return true;
        }

        try {
            logger.debug("Connect MongoDB");
            disconnectFromDatabase();

            this.cl = new MongoClient(new MongoClientURI(this.url));

            // The mongo always succeeds in creating the connection.
            // We have to actually force it to test the connection to try to connect to the server.
            cl.getAddress();

            logger.debug("Connect MongoDB ... done");
            return true;
        } catch (Exception e) {
            logger.error("Failed to connect to database {}: {}", this.url, e.getMessage(), e);
            disconnectFromDatabase();
            return false;
        }
    }

    /**
     * Fetches the currently valid database.
     *
     * @return The database object
     */
    private synchronized @Nullable MongoClient getDatabase() {
        return cl;
    }

    /**
     * Connects to the Collection
     *
     * @return The collection object when collection creation was successful. Null otherwise.
     */
    private @Nullable DBCollection connectToCollection(String collectionName) {
        try {
            @Nullable
            MongoClient db = getDatabase();

            if (db == null) {
                logger.error("Failed to connect to collection {}: Connection not ready", collectionName);
                return null;
            }

            DBCollection mongoCollection = db.getDB(this.db).getCollection(collectionName);

            BasicDBObject idx = new BasicDBObject();
            idx.append(FIELD_ITEM, 1).append(FIELD_TIMESTAMP, 1);
            mongoCollection.createIndex(idx);

            return mongoCollection;
        } catch (Exception e) {
            logger.error("Failed to connect to collection {}: {}", collectionName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Disconnects from the database
     */
    private synchronized void disconnectFromDatabase() {
        if (this.cl != null) {
            this.cl.close();
        }

        cl = null;
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        if (!initialized) {
            return Collections.emptyList();
        }

        if (!tryConnectToDatabase()) {
            return Collections.emptyList();
        }

        String realItemName = filter.getItemName();
        if (realItemName == null) {
            logger.warn("Item name is missing in filter {}", filter);
            return List.of();
        }

        String collectionName = collectionPerItem ? realItemName : this.collection;
        @Nullable
        DBCollection collection = connectToCollection(collectionName);

        // If collection creation failed, return nothing.
        if (collection == null) {
            // Logging is done in connectToCollection()
            return Collections.emptyList();
        }

        @Nullable
        Item item = getItem(realItemName);

        if (item == null) {
            logger.warn("Item {} not found", realItemName);
            return Collections.emptyList();
        }

        List<HistoricItem> items = new ArrayList<>();
        BasicDBObject query = new BasicDBObject();
        if (filter.getItemName() != null) {
            query.put(FIELD_ITEM, filter.getItemName());
        }
        State filterState = filter.getState();
        if (filterState != null && filter.getOperator() != null) {
            @Nullable
            String op = convertOperator(filter.getOperator());

            if (op == null) {
                logger.error("Failed to convert operator {} to MongoDB operator", filter.getOperator());
                return Collections.emptyList();
            }

            Object value = convertValue(filterState);
            query.put(FIELD_VALUE, new BasicDBObject(op, value));
        }

        BasicDBObject dateQueries = new BasicDBObject();
        if (filter.getBeginDate() != null) {
            dateQueries.put("$gte", Date.from(filter.getBeginDate().toInstant()));
        }
        if (filter.getEndDate() != null) {
            dateQueries.put("$lte", Date.from(filter.getEndDate().toInstant()));
        }
        if (!dateQueries.isEmpty()) {
            query.put(FIELD_TIMESTAMP, dateQueries);
        }

        logger.debug("Query: {}", query);

        Integer sortDir = (filter.getOrdering() == Ordering.ASCENDING) ? 1 : -1;
        DBCursor cursor = collection.find(query).sort(new BasicDBObject(FIELD_TIMESTAMP, sortDir))
                .skip(filter.getPageNumber() * filter.getPageSize()).limit(filter.getPageSize());

        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();

            final State state;
            if (item instanceof NumberItem) {
                state = new DecimalType(obj.getDouble(FIELD_VALUE));
            } else if (item instanceof DimmerItem) {
                state = new PercentType(obj.getInt(FIELD_VALUE));
            } else if (item instanceof SwitchItem) {
                state = OnOffType.valueOf(obj.getString(FIELD_VALUE));
            } else if (item instanceof ContactItem) {
                state = OpenClosedType.valueOf(obj.getString(FIELD_VALUE));
            } else if (item instanceof RollershutterItem) {
                state = new PercentType(obj.getInt(FIELD_VALUE));
            } else if (item instanceof DateTimeItem) {
                state = new DateTimeType(
                        ZonedDateTime.ofInstant(obj.getDate(FIELD_VALUE).toInstant(), ZoneId.systemDefault()));
            } else {
                state = new StringType(obj.getString(FIELD_VALUE));
            }

            items.add(new MongoDBItem(realItemName, state,
                    ZonedDateTime.ofInstant(obj.getDate(FIELD_TIMESTAMP).toInstant(), ZoneId.systemDefault())));
        }

        return items;
    }

    private @Nullable String convertOperator(Operator operator) {
        switch (operator) {
            case EQ:
                return "$eq";
            case GT:
                return "$gt";
            case GTE:
                return "$gte";
            case LT:
                return "$lt";
            case LTE:
                return "$lte";
            case NEQ:
                return "$neq";
            default:
                return null;
        }
    }

    private @Nullable Item getItem(String itemName) {
        try {
            return itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e1) {
            logger.error("Unable to get item type for {}", itemName);
        }
        return null;
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return Collections.emptyList();
    }
}
