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
package org.openhab.persistence.influxdb.internal;

import static org.eclipse.jdt.annotation.DefaultLocation.FIELD;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigurableService;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemNotFoundException;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.library.items.ColorItem;
import org.eclipse.smarthome.core.library.items.ContactItem;
import org.eclipse.smarthome.core.library.items.DateTimeItem;
import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.LocationItem;
import org.eclipse.smarthome.core.library.items.NumberItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.items.SwitchItem;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.persistence.FilterCriteria;
import org.eclipse.smarthome.core.persistence.FilterCriteria.Ordering;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.persistence.PersistenceItemInfo;
import org.eclipse.smarthome.core.persistence.PersistenceService;
import org.eclipse.smarthome.core.persistence.QueryablePersistenceService;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the InfluxDB {@link PersistenceService}. It persists item values
 * using the <a href="http://influxdb.org">InfluxDB</a> time series database. The states (
 * {@link State}) of an {@link Item} are persisted in a time series with names equal to the name of
 * the item. All values are stored using integers or doubles, {@link OnOffType} and
 * {@link OpenClosedType} are stored using 0 or 1.
 *
 * The defaults for the database name, the database user and the database url are "openhab",
 * "openhab" and "http://127.0.0.1:8086".
 *
 * @author Theo Weiss - Initial Contribution, rewrite of org.openhab.persistence.influxdb > 0.9
 *         support
 * @author Dominik Vorreiter - Port to OH 2.0
 */
@NonNullByDefault({ FIELD })
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.influxdb", property = {
                Constants.SERVICE_PID + "=org.openhab.influxdb",
                ConfigurableService.SERVICE_PROPERTY_DESCRIPTION_URI + "=persistence:influxdb",
                ConfigurableService.SERVICE_PROPERTY_LABEL + "=InfluxDB persistence layer",
                ConfigurableService.SERVICE_PROPERTY_CATEGORY + "=persistence" })
public class InfluxDBPersistenceService implements QueryablePersistenceService {

    private static final String SERVICE_NAME = "influxdb";

    private static final String DEFAULT_URL = "http://127.0.0.1:8086";
    private static final String DEFAULT_DB = "openhab";
    private static final String DEFAULT_USER = "openhab";
    private static final String DEFAULT_PASSWORD = "habopen";
    private static final String DEFAULT_RETENTION_POLICY = "autogen";

    private static final String DIGITAL_VALUE_OFF = "0";
    private static final String DIGITAL_VALUE_ON = "1";

    private static final String COLUMN_VALUE_NAME = "value";
    private static final String COLUMN_TIME_NAME = "time";

    private static final String TAG_ITEM_NAME = "item";
    private static final String TAG_CATEGORY_NAME = "category";
    private static final String TAG_TYPE_NAME = "type";
    private static final String TAG_LABEL_NAME = "label";

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private static final Logger logger = LoggerFactory.getLogger(InfluxDBPersistenceService.class);

    private String dbName = DEFAULT_DB;
    private String url = DEFAULT_URL;
    private String user = DEFAULT_USER;
    private String password = DEFAULT_PASSWORD;
    private String retentionPolicy = DEFAULT_RETENTION_POLICY;

    private boolean replaceUnderscore;
    private boolean addCategoryTag;
    private boolean addTypeTag;
    private boolean addLabelTag;

    @Nullable
    @NonNullByDefault({})
    private InfluxDB influxDB;

    @NonNullByDefault({})
    private ItemRegistry itemRegistry;
    @NonNullByDefault({})
    private MetadataRegistry metadataRegistry;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        logger.trace("ItemRegistry has been set");
    }

    protected void unsetItemRegistry(ItemRegistry itemRegistry) {
        this.itemRegistry = null;
        logger.trace("ItemRegistry has been unset");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    protected void setMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
        logger.trace("MetadataRegistry has been set");
    }

    protected void unsetMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = null;
        logger.trace("MetadataRegistry has been unset");
    }

    @Activate
    public void activate(final BundleContext bundleContext, final Map<String, Object> config) {
        logger.debug("InfluxDB persistence service is being activated");

        modified(config);

        // check connection; errors will only be logged, hoping the connection will work at a later
        // time.
        if (!checkConnection()) {
            logger.warn("database connection does not work for now, will retry to use the database.");
        }

        logger.debug("InfluxDB persistence service is now activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("InfluxDB persistence service deactivated");
        disconnect();
    }

    @Modified
    protected void modified(Map<String, Object> config) {
        if (config != null) {
            logger.debug("Config has been modified.");

            disconnect();

            url = (String) config.getOrDefault("url", DEFAULT_URL);

            user = (String) config.getOrDefault("user", DEFAULT_USER);
            password = (String) config.getOrDefault("password", DEFAULT_PASSWORD);

            dbName = (String) config.getOrDefault("db", DEFAULT_DB);
            retentionPolicy = (String) config.getOrDefault("retentionPolicy", DEFAULT_RETENTION_POLICY);

            replaceUnderscore = getConfigBooleanValue(config, "replaceUnderscore", false);
            addCategoryTag = getConfigBooleanValue(config, "addCategoryTag", false);
            addLabelTag = getConfigBooleanValue(config, "addLabelTag", false);
            addTypeTag = getConfigBooleanValue(config, "addTypeTag", false);

            connect();
        }
    }

    private Boolean getConfigBooleanValue(@NonNull Map<String, Object> config, @NonNull String key,
            @NonNull Boolean defaultValue) {
        Object object = config.get(key);

        if (object == null) {
            return defaultValue;
        }

        if (object instanceof Boolean) {
            return (Boolean) object;
        }

        return "true".equalsIgnoreCase((String) object);
    }

    @SuppressWarnings("null")
    private void connect() {
        if (influxDB == null) {
            logger.debug("Connecting to InfluxDB using url {}, user {} and a {} char password.", url, user,
                    password.length());

            // reuse an existing InfluxDB object because concerning the database it has no state connection
            influxDB = InfluxDBFactory.connect(url, user, password);
            influxDB.setDatabase(dbName);
            influxDB.setRetentionPolicy(retentionPolicy);
            influxDB.enableBatch(BatchOptions.DEFAULTS.actions(100).flushDuration(500)
                    .exceptionHandler((failedPoints, throwable) -> {
                        logger.error("InfluxDB exception", throwable);
                        for (Point point : failedPoints) {
                            logger.error("Lost {}", point.toString());
                        }
                    }));
        }
    }

    private boolean checkConnection() {
        if (influxDB != null) {
            Pong pong = influxDB.ping();
            String version = pong.getVersion();
            // may be check for version >= 0.9
            if (version != null && !version.contains("unknown")) {
                logger.debug("database status is OK, version is {}", version);
                return true;
            } else {
                logger.warn("database ping error, version is: {}, response time is {} ms", version,
                        pong.getResponseTime());
            }
        } else {
            logger.warn("checkConnection: database is not connected");
        }

        return false;
    }

    private void disconnect() {
        try {
            if (influxDB != null) {
                influxDB.close();
            }
        } finally {
            influxDB = null;
        }
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
        logger.debug("Calling getItemInfo()");

        Query influxdbQuery = new Query("SHOW TAG VALUES ON \"" + dbName + "\" WITH KEY = \"" + TAG_ITEM_NAME + "\"");
        Set<PersistenceItemInfo> historicItems = new HashSet<PersistenceItemInfo>();

        if (influxDB != null) {
            QueryResult queryResult = influxDB.query(influxdbQuery, timeUnit);

            forAllDo(queryResult.getResults(), result -> {
                if (result.hasError()) {
                    logger.error("This result has an error: {}", result.getError());
                }

                forAllDo(result.getSeries(), series -> {
                    int idxName = series.getColumns().indexOf("value");

                    if (idxName >= 0) {
                        forAllDo(series.getValues(), value -> {
                            String name = (String) value.get(idxName);
                            historicItems.add(new InfluxDBItem(name));
                        });
                    }
                });
            });

            logger.debug("Returning getItemInfo() with {} items", historicItems.size());
            return historicItems;
        } else {
            logger.warn("InfluxDB is not yet connected");
            return Collections.emptySet();
        }
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

        if (influxDB == null) {
            logger.warn("InfluxDB is not yet connected");
            return;
        }

        State state;
        if (item.getAcceptedCommandTypes().contains(HSBType.class)) {
            state = Optional.<State> ofNullable(item.getStateAs(HSBType.class)).orElse(item.getState());
        } else if (item.getAcceptedDataTypes().contains(PercentType.class)) {
            state = Optional.<State> ofNullable(item.getStateAs(PercentType.class)).orElse(item.getState());
        } else {
            // All other items should return the best format by default
            state = item.getState();
        }

        String itemName = item.getName();
        String measurementName = (alias != null && !alias.isEmpty()) ? alias : itemName;

        if (replaceUnderscore) {
            measurementName = measurementName.replace('_', '.');
        }

        Object value = stateToObject(state);

        Builder measurement = Point.measurement(measurementName).time(System.currentTimeMillis(), timeUnit);

        measurement.field(COLUMN_VALUE_NAME, value);
        measurement.tag(TAG_ITEM_NAME, itemName);

        if (addCategoryTag) {
            measurement.tag(TAG_CATEGORY_NAME, Optional.ofNullable(item.getCategory()).orElse("n/a"));
        }

        if (addTypeTag) {
            measurement.tag(TAG_TYPE_NAME, item.getType());
        }

        if (addLabelTag) {
            measurement.tag(TAG_LABEL_NAME, Optional.ofNullable(item.getLabel()).orElse("n/a"));
        }

        if (metadataRegistry != null) {
            MetadataKey key = new MetadataKey(SERVICE_NAME, item.getName());
            Metadata metadata = metadataRegistry.get(key);
            if (metadata != null) {
                metadata.getConfiguration().forEach((tagName, tagValue) -> {
                    measurement.tag(tagName, tagValue.toString());
                });
            }
        }

        Point point = measurement.build();

        logger.trace("Storing item {} in InfluxDB point {}", item, point);

        if (influxDB != null) {
            influxDB.write(dbName, retentionPolicy, point);
        }
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        logger.debug("Got a query for historic points!");

        if (influxDB == null) {
            logger.warn("InfluxDB is not yet connected");
            return Collections.emptyList();
        }

        logger.trace(
                "Filter: itemname: {}, ordering: {}, state: {},  operator: {}, getBeginDate: {}, getEndDate: {}, getPageSize: {}, getPageNumber: {}",
                filter.getItemName(), filter.getOrdering().toString(), filter.getState(), filter.getOperator(),
                filter.getBeginDateZoned(), filter.getEndDateZoned(), filter.getPageSize(), filter.getPageNumber());

        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM \"").append(dbName).append("\".\"").append(retentionPolicy).append("\"./.*/");

        if ((filter.getState() != null && filter.getOperator() != null) || filter.getBeginDateZoned() != null
                || filter.getEndDateZoned() != null || filter.getItemName() != null) {
            query.append(" WHERE");

            boolean foundState = false;
            boolean foundBeginDate = false;
            boolean foundName = false;

            if (filter.getItemName() != null) {
                foundName = true;

                query.append(" ");
                query.append(TAG_ITEM_NAME);
                query.append(" = '");
                query.append(filter.getItemName());
                query.append("'");
            }

            if (filter.getState() != null && filter.getOperator() != null) {
                String value = stateToString(filter.getState());
                if (value != null) {
                    foundState = true;
                    if (foundName) {
                        query.append(" AND");
                    }
                    query.append(" ");
                    query.append(COLUMN_VALUE_NAME);
                    query.append(" ");
                    query.append(filter.getOperator().toString());
                    query.append(" ");
                    query.append(value);
                }
            }

            if (filter.getBeginDateZoned() != null) {
                foundBeginDate = true;
                if (foundName || foundState) {
                    query.append(" AND");
                }
                query.append(" ");
                query.append(COLUMN_TIME_NAME);
                query.append(" > ");
                query.append(getTimeFilter(filter.getBeginDateZoned()));
                query.append(" ");
            }

            if (filter.getEndDateZoned() != null) {
                if (foundName || foundState || foundBeginDate) {
                    query.append(" AND");
                }
                query.append(" ");
                query.append(COLUMN_TIME_NAME);
                query.append(" < ");
                query.append(getTimeFilter(filter.getEndDateZoned()));
                query.append(" ");
            }

        }

        if (filter.getOrdering() == Ordering.DESCENDING) {
            query.append(String.format(" ORDER BY %s DESC", COLUMN_TIME_NAME));
        }

        int limit = (filter.getPageNumber() + 1) * filter.getPageSize();
        query.append(" LIMIT " + limit);

        Query influxdbQuery = new Query(query.toString(), dbName);

        logger.debug("Query: {}", influxdbQuery.getCommand());

        List<HistoricItem> historicItems = new ArrayList<HistoricItem>();

        if (influxDB != null) {
            QueryResult queryResult = influxDB.query(influxdbQuery, timeUnit);

            forAllDo(queryResult.getResults(), result -> {
                if (result.hasError()) {
                    logger.warn("This result has an error: {}", result.getError());
                }

                forAllDo(result.getSeries(), series -> {
                    List<String> columns = series.getColumns();

                    logger.trace("Columns for measurement {}: {}", series.getName(), columns);

                    int idxItem = columns.indexOf(TAG_ITEM_NAME);
                    int idxValue = columns.indexOf(COLUMN_VALUE_NAME);
                    int idxTime = columns.indexOf(COLUMN_TIME_NAME);

                    if (idxItem < 0 || idxValue < 0 || idxTime < 0) {
                        logger.warn("Fields not found! idxItem {}, idxValue {}, idxTime {}", idxItem, idxValue,
                                idxTime);
                    } else {
                        forAllDo(series.getValues(), value -> {
                            String itemName = (String) value.get(idxItem);
                            Double rawTime = (Double) value.get(idxTime);
                            State state = objectToState(value.get(idxValue), itemName);

                            Date time = new Date(rawTime.longValue());

                            historicItems.add(new InfluxDBItem(itemName, state, time));
                        });
                    }
                });
            });
        }

        logger.debug("Returning query() with {} items", historicItems.size());
        return historicItems;
    }

    private String getTimeFilter(ZonedDateTime time) {
        // for some reason we need to query using 'seconds' only
        // passing milli seconds causes no results to be returned
        long milliSeconds = time.toInstant().toEpochMilli();
        long seconds = milliSeconds / 1000;
        return seconds + "s";
    }

    /**
     * This method returns an integer if possible if not a double is returned. This is an optimization
     * for influxdb because integers have less overhead.
     *
     * @param value the BigDecimal to be converted
     * @return A double if possible else a double is returned.
     */
    private Object convertBigDecimalToNum(BigDecimal value) {
        Object convertedValue;
        if (value.scale() == 0) {
            convertedValue = value.toBigInteger();
        } else {
            convertedValue = value.doubleValue();
        }
        return convertedValue;
    }

    /**
     * Converts {@link State} to objects fitting into influxdb values.
     *
     * @param state to be converted
     * @return integer or double value for DecimalType, 0 or 1 for OnOffType and OpenClosedType,
     *         integer for DateTimeType, String for all others
     */
    private Object stateToObject(@NonNull State state) {
        Object value;
        if (state instanceof HSBType) {
            value = ((HSBType) state).toString();
        } else if (state instanceof PointType) {
            value = point2String((PointType) state);
        } else if (state instanceof DecimalType) {
            value = convertBigDecimalToNum(((DecimalType) state).toBigDecimal());
        } else if (state instanceof QuantityType<?>) {
            value = convertBigDecimalToNum(((QuantityType<?>) state).toBigDecimal());
        } else if (state instanceof OnOffType) {
            value = (OnOffType) state == OnOffType.ON ? 1 : 0;
        } else if (state instanceof OpenClosedType) {
            value = (OpenClosedType) state == OpenClosedType.OPEN ? 1 : 0;
        } else if (state instanceof DateTimeType) {
            value = ((DateTimeType) state).getZonedDateTime().toInstant().toEpochMilli();
        } else {
            value = state.toString();
        }
        return value;
    }

    /**
     * Converts {@link State} to a String suitable for influxdb queries.
     *
     * @param state to be converted
     * @return {@link String} equivalent of the {@link State}
     */
    private String stateToString(State state) {
        String value;
        if (state instanceof DecimalType) {
            value = ((DecimalType) state).toBigDecimal().toString();
        } else if (state instanceof QuantityType<?>) {
            value = ((QuantityType<?>) state).toBigDecimal().toString();
        } else if (state instanceof PointType) {
            value = point2String((PointType) state);
        } else if (state instanceof OnOffType) {
            value = ((OnOffType) state) == OnOffType.ON ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof OpenClosedType) {
            value = ((OpenClosedType) state) == OpenClosedType.OPEN ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof DateTimeType) {
            value = String.valueOf(((DateTimeType) state).getZonedDateTime().toInstant().toEpochMilli());
        } else {
            value = state.toString();
        }
        return value;
    }

    /**
     * Converts a value to a {@link State} which is suitable for the given {@link Item}. This is
     * needed for querying a {@link HistoricState}.
     *
     * @param value to be converted to a {@link State}
     * @param itemName name of the {@link Item} to get the {@link State} for
     * @return the state of the item represented by the itemName parameter, else the string value of
     *         the Object parameter
     */
    private State objectToState(Object value, String itemName) {
        String valueStr = String.valueOf(value);
        if (itemRegistry != null) {
            try {
                Item item = itemRegistry.getItem(itemName);
                if (item instanceof GroupItem) {
                    item = ((GroupItem) item).getBaseItem();
                }
                if (item instanceof ColorItem) {
                    return new HSBType(valueStr);
                } else if (item instanceof LocationItem) {
                    return new PointType(valueStr);
                } else if (item instanceof NumberItem) {
                    return new DecimalType(valueStr);
                } else if (item instanceof DimmerItem) {
                    return new PercentType(valueStr);
                } else if (item instanceof SwitchItem) {
                    return string2DigitalValue(valueStr).equals(DIGITAL_VALUE_OFF) ? OnOffType.OFF : OnOffType.ON;
                } else if (item instanceof ContactItem) {
                    return (string2DigitalValue(valueStr).equals(DIGITAL_VALUE_OFF)) ? OpenClosedType.CLOSED
                            : OpenClosedType.OPEN;
                } else if (item instanceof RollershutterItem) {
                    return new PercentType(valueStr);
                } else if (item instanceof DateTimeItem) {
                    Instant i = Instant.ofEpochSecond(new BigDecimal(valueStr).longValue());
                    ZonedDateTime z = ZonedDateTime.ofInstant(i, TimeZone.getDefault().toZoneId());
                    return new DateTimeType(z);
                } else {
                    return new StringType(valueStr);
                }
            } catch (ItemNotFoundException e) {
                logger.warn("Could not find item '{}' in registry", itemName);
            }
        }
        // just return a StringType as a fallback
        return new StringType(valueStr);
    }

    /**
     * Maps a string value which expresses a {@link BigDecimal.ZERO } to DIGITAL_VALUE_OFF, all others
     * to DIGITAL_VALUE_ON
     *
     * @param value to be mapped
     * @return
     */
    private String string2DigitalValue(String value) {
        BigDecimal num = new BigDecimal(value);
        if (num.compareTo(BigDecimal.ZERO) == 0) {
            return DIGITAL_VALUE_OFF;
        } else {
            return DIGITAL_VALUE_ON;
        }
    }

    private String point2String(PointType point) {
        StringBuffer buf = new StringBuffer();
        buf.append(point.getLatitude().toString());
        buf.append(",");
        buf.append(point.getLongitude().toString());
        if (!point.getAltitude().equals(DecimalType.ZERO)) {
            buf.append(",");
            buf.append(point.getAltitude().toString());
        }
        return buf.toString(); // latitude, longitude, altitude
    }

    private static <T> void forAllDo(Collection<T> collection, Consumer<T> closure) {
        if (collection != null && closure != null) {
            for (Iterator<T> it = collection.iterator(); it.hasNext();) {
                closure.accept(it.next());
            }
        }
    }
}
