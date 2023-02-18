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
package org.openhab.persistence.jdbc.internal;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.jdbc.internal.db.JdbcBaseDAO;
import org.openhab.persistence.jdbc.internal.dto.Column;
import org.openhab.persistence.jdbc.internal.dto.ItemsVO;
import org.openhab.persistence.jdbc.internal.exceptions.JdbcException;
import org.openhab.persistence.jdbc.internal.exceptions.JdbcSQLException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the JDBC {@link PersistenceService}.
 *
 * @author Helmut Lehmeyer - Initial contribution
 * @author Kai Kreuzer - Migration to 3.x
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.jdbc", //
        property = Constants.SERVICE_PID + "=org.openhab.jdbc")
@ConfigurableService(category = "persistence", label = "JDBC Persistence Service", description_uri = JdbcPersistenceServiceConstants.CONFIG_URI)
public class JdbcPersistenceService extends JdbcMapper implements ModifiablePersistenceService {

    private final Logger logger = LoggerFactory.getLogger(JdbcPersistenceService.class);

    private final ItemRegistry itemRegistry;

    @Activate
    public JdbcPersistenceService(final @Reference ItemRegistry itemRegistry,
            final @Reference TimeZoneProvider timeZoneProvider) {
        super(timeZoneProvider);
        this.itemRegistry = itemRegistry;
    }

    /**
     * Called by the SCR to activate the component with its configuration read
     * from CAS
     *
     * @param bundleContext
     *            BundleContext of the Bundle that defines this component
     * @param configuration
     *            Configuration properties for this component obtained from the
     *            ConfigAdmin service
     */
    @Activate
    public void activate(BundleContext bundleContext, Map<Object, Object> configuration) {
        logger.debug("JDBC::activate: persistence service activated");
        updateConfig(configuration);
    }

    /**
     * Called by the SCR to deactivate the component when either the
     * configuration is removed or mandatory references are no longer satisfied
     * or the component has simply been stopped.
     *
     * @param reason
     *            Reason code for the deactivation:<br>
     *            <ul>
     *            <li>0 – Unspecified
     *            <li>1 – The component was disabled
     *            <li>2 – A reference became unsatisfied
     *            <li>3 – A configuration was changed
     *            <li>4 – A configuration was deleted
     *            <li>5 – The component was disposed
     *            <li>6 – The bundle was stopped
     *            </ul>
     */
    @Deactivate
    public void deactivate(final int reason) {
        logger.debug("JDBC::deactivate:  persistence bundle stopping. Disconnecting from database. reason={}", reason);
        // closeConnection();
        initialized = false;
    }

    @Override
    public String getId() {
        logger.debug("JDBC::getName: returning name 'jdbc' for queryable persistence service.");
        return JdbcPersistenceServiceConstants.SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return JdbcPersistenceServiceConstants.SERVICE_LABEL;
    }

    @Override
    public void store(Item item) {
        internalStore(item, null, item.getState());
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        // alias is not supported
        internalStore(item, null, item.getState());
    }

    @Override
    public void store(Item item, ZonedDateTime date, State state) {
        internalStore(item, date, state);
    }

    private void internalStore(Item item, @Nullable ZonedDateTime date, State state) {
        // Do not store undefined/uninitialized data
        if (state instanceof UnDefType) {
            logger.debug("JDBC::store: ignore Item '{}' because it is UnDefType", item.getName());
            return;
        }
        if (!checkDBAccessability()) {
            logger.warn(
                    "JDBC::store: No connection to database. Cannot persist state '{}' for item '{}'! Will retry connecting to database when error count:{} equals errReconnectThreshold:{}",
                    state, item, errCnt, conf.getErrReconnectThreshold());
            return;
        }
        try {
            long timerStart = System.currentTimeMillis();
            storeItemValue(item, state, date);
            if (logger.isDebugEnabled()) {
                logger.debug("JDBC: Stored item '{}' as '{}' in SQL database at {} in {} ms.", item.getName(), state,
                        new Date(), System.currentTimeMillis() - timerStart);
            }
        } catch (JdbcException e) {
            logger.warn("JDBC::store: Unable to store item", e);
        }
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return getItems();
    }

    /**
     * Queries the {@link PersistenceService} for data with a given filter
     * criteria
     *
     * @param filter
     *            the filter to apply to the query
     * @return a time series of items
     */
    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        if (!checkDBAccessability()) {
            logger.warn("JDBC::query: database not connected, query aborted for item '{}'", filter.getItemName());
            return List.of();
        }

        // Get the item name from the filter
        // Also get the Item object so we can determine the type
        Item item = null;
        String itemName = filter.getItemName();
        logger.debug("JDBC::query: item is {}", itemName);
        try {
            item = itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e1) {
            logger.error("JDBC::query: unable to get item for itemName: '{}'. Ignore and give up!", itemName);
            return List.of();
        }

        if (item instanceof GroupItem) {
            // For Group Item is BaseItem needed to get correct Type of Value.
            item = GroupItem.class.cast(item).getBaseItem();
            logger.debug("JDBC::query: item is instanceof GroupItem '{}'", itemName);
            if (item == null) {
                logger.debug("JDBC::query: BaseItem of GroupItem is null. Ignore and give up!");
                return List.of();
            }
            if (item instanceof GroupItem) {
                logger.debug("JDBC::query: BaseItem of GroupItem is a GroupItem too. Ignore and give up!");
                return List.of();
            }
        }

        String table = itemNameToTableNameMap.get(itemName);
        if (table == null) {
            logger.debug("JDBC::query: unable to find table for item with name: '{}', no data in database.", itemName);
            return List.of();
        }

        try {
            long timerStart = System.currentTimeMillis();
            List<HistoricItem> items = getHistItemFilterQuery(filter, conf.getNumberDecimalcount(), table, item);
            if (logger.isDebugEnabled()) {
                logger.debug("JDBC: Query for item '{}' returned {} rows in {} ms", itemName, items.size(),
                        System.currentTimeMillis() - timerStart);
            }
            // Success
            errCnt = 0;
            return items;
        } catch (JdbcSQLException e) {
            logger.warn("JDBC::query: Unable to query item", e);
            return List.of();
        }
    }

    public void updateConfig(Map<Object, Object> configuration) {
        logger.debug("JDBC::updateConfig");

        conf = new JdbcConfiguration(configuration);
        if (conf.valid && checkDBAccessability()) {
            namingStrategy = new NamingStrategy(conf);
            try {
                checkDBSchema();
                // connection has been established ... initialization completed!
                initialized = true;
            } catch (JdbcSQLException e) {
                logger.error("Failed to check database schema", e);
                initialized = false;
            }
        } else {
            initialized = false;
        }

        logger.debug("JDBC::updateConfig: configuration complete for service={}.", getId());
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of(PersistenceStrategy.Globals.CHANGE);
    }

    @Override
    public boolean remove(FilterCriteria filter) throws IllegalArgumentException {
        if (!checkDBAccessability()) {
            logger.warn("JDBC::remove: database not connected, remove aborted for item '{}'", filter.getItemName());
            return false;
        }

        // Get the item name from the filter
        // Also get the Item object so we can determine the type
        String itemName = filter.getItemName();
        logger.debug("JDBC::remove: item is {}", itemName);
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must not be null");
        }

        String table = itemNameToTableNameMap.get(itemName);
        if (table == null) {
            logger.debug("JDBC::remove: unable to find table for item with name: '{}', no data in database.", itemName);
            return false;
        }

        try {
            long timerStart = System.currentTimeMillis();
            deleteItemValues(filter, table);
            if (logger.isDebugEnabled()) {
                logger.debug("JDBC: Deleted values for item '{}' in SQL database at {} in {} ms.", itemName, new Date(),
                        System.currentTimeMillis() - timerStart);
            }
            return true;
        } catch (JdbcSQLException e) {
            logger.debug("JDBC::remove: Unable to remove values for item", e);
            return false;
        }
    }

    /**
     * Get a list of names of persisted items.
     */
    public Collection<String> getItemNames() {
        return itemNameToTableNameMap.keySet();
    }

    /**
     * Get a map of item names to table names.
     */
    public Map<String, String> getItemNameToTableNameMap() {
        return itemNameToTableNameMap;
    }

    /**
     * Check schema of specific item table for integrity issues.
     *
     * @param tableName for which columns should be checked
     * @param itemName that corresponds to table
     * @return Collection of strings, each describing an identified issue
     * @throws JdbcSQLException on SQL errors
     */
    public Collection<String> getSchemaIssues(String tableName, String itemName) throws JdbcSQLException {
        List<String> issues = new ArrayList<>();

        if (!checkDBAccessability()) {
            logger.warn("JDBC::getSchemaIssues: database not connected");
            return issues;
        }

        Item item;
        try {
            item = itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return issues;
        }
        JdbcBaseDAO dao = conf.getDBDAO();
        String timeDataType = dao.sqlTypes.get("tablePrimaryKey");
        if (timeDataType == null) {
            return issues;
        }
        String valueDataType = dao.getDataType(item);
        List<Column> columns = getTableColumns(tableName);
        for (Column column : columns) {
            String columnName = column.getColumnName();
            if ("time".equalsIgnoreCase(columnName)) {
                if (!"time".equals(columnName)) {
                    issues.add("Column name 'time' expected, but is '" + columnName + "'");
                }
                if (!timeDataType.equalsIgnoreCase(column.getColumnType())
                        && !timeDataType.equalsIgnoreCase(column.getColumnTypeAlias())) {
                    issues.add("Column type '" + timeDataType + "' expected, but is '"
                            + column.getColumnType().toUpperCase() + "'");
                }
                if (column.getIsNullable()) {
                    issues.add("Column 'time' expected to be NOT NULL, but is nullable");
                }
            } else if ("value".equalsIgnoreCase(columnName)) {
                if (!"value".equals(columnName)) {
                    issues.add("Column name 'value' expected, but is '" + columnName + "'");
                }
                if (!valueDataType.equalsIgnoreCase(column.getColumnType())
                        && !valueDataType.equalsIgnoreCase(column.getColumnTypeAlias())) {
                    issues.add("Column type '" + valueDataType + "' expected, but is '"
                            + column.getColumnType().toUpperCase() + "'");
                }
                if (!column.getIsNullable()) {
                    issues.add("Column 'value' expected to be nullable, but is NOT NULL");
                }
            } else {
                issues.add("Column '" + columnName + "' not expected");
            }
        }
        return issues;
    }

    /**
     * Fix schema issues.
     *
     * @param tableName for which columns should be repaired
     * @param itemName that corresponds to table
     * @return true if table was altered, otherwise false
     * @throws JdbcSQLException on SQL errors
     */
    public boolean fixSchemaIssues(String tableName, String itemName) throws JdbcSQLException {
        if (!checkDBAccessability()) {
            logger.warn("JDBC::fixSchemaIssues: database not connected");
            return false;
        }

        Item item;
        try {
            item = itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            return false;
        }
        JdbcBaseDAO dao = conf.getDBDAO();
        String timeDataType = dao.sqlTypes.get("tablePrimaryKey");
        if (timeDataType == null) {
            return false;
        }
        String valueDataType = dao.getDataType(item);
        List<Column> columns = getTableColumns(tableName);
        boolean isFixed = false;
        for (Column column : columns) {
            String columnName = column.getColumnName();
            if ("time".equalsIgnoreCase(columnName)) {
                if (!"time".equals(columnName)
                        || (!timeDataType.equalsIgnoreCase(column.getColumnType())
                                && !timeDataType.equalsIgnoreCase(column.getColumnTypeAlias()))
                        || column.getIsNullable()) {
                    alterTableColumn(tableName, "time", timeDataType, false);
                    isFixed = true;
                }
            } else if ("value".equalsIgnoreCase(columnName)) {
                if (!"value".equals(columnName)
                        || (!valueDataType.equalsIgnoreCase(column.getColumnType())
                                && !valueDataType.equalsIgnoreCase(column.getColumnTypeAlias()))
                        || !column.getIsNullable()) {
                    alterTableColumn(tableName, "value", valueDataType, true);
                    isFixed = true;
                }
            }
        }
        return isFixed;
    }

    /**
     * Get a list of all items with corresponding tables and an {@link ItemTableCheckEntryStatus} indicating
     * its condition.
     *
     * @return list of {@link ItemTableCheckEntry}
     */
    public List<ItemTableCheckEntry> getCheckedEntries() throws JdbcSQLException {
        List<ItemTableCheckEntry> entries = new ArrayList<>();

        if (!checkDBAccessability()) {
            logger.warn("JDBC::getCheckedEntries: database not connected");
            return entries;
        }

        var orphanTables = getItemTables().stream().map(ItemsVO::getTableName).collect(Collectors.toSet());
        for (Entry<String, String> entry : itemNameToTableNameMap.entrySet()) {
            String itemName = entry.getKey();
            String tableName = entry.getValue();
            entries.add(getCheckedEntry(itemName, tableName, orphanTables.contains(tableName)));
            orphanTables.remove(tableName);
        }
        for (String orphanTable : orphanTables) {
            entries.add(new ItemTableCheckEntry("", orphanTable, ItemTableCheckEntryStatus.ORPHAN_TABLE));
        }
        return entries;
    }

    private ItemTableCheckEntry getCheckedEntry(String itemName, String tableName, boolean tableExists) {
        boolean itemExists;
        try {
            itemRegistry.getItem(itemName);
            itemExists = true;
        } catch (ItemNotFoundException e) {
            itemExists = false;
        }

        ItemTableCheckEntryStatus status;
        if (!tableExists) {
            if (itemExists) {
                status = ItemTableCheckEntryStatus.TABLE_MISSING;
            } else {
                status = ItemTableCheckEntryStatus.ITEM_AND_TABLE_MISSING;
            }
        } else if (itemExists) {
            status = ItemTableCheckEntryStatus.VALID;
        } else {
            status = ItemTableCheckEntryStatus.ITEM_MISSING;
        }
        return new ItemTableCheckEntry(itemName, tableName, status);
    }

    /**
     * Clean up inconsistent item: Remove from index and drop table.
     * Tables with any rows are skipped, unless force is set.
     *
     * @param itemName Name of item to clean
     * @param force If true, non-empty tables will be dropped too
     * @return true if item was cleaned up
     * @throws JdbcSQLException
     */
    public boolean cleanupItem(String itemName, boolean force) throws JdbcSQLException {
        if (!checkDBAccessability()) {
            logger.warn("JDBC::cleanupItem: database not connected");
            return false;
        }

        String tableName = itemNameToTableNameMap.get(itemName);
        if (tableName == null) {
            return false;
        }
        ItemTableCheckEntry entry = getCheckedEntry(itemName, tableName, ifTableExists(tableName));
        return cleanupItem(entry, force);
    }

    /**
     * Clean up inconsistent item: Remove from index and drop table.
     * Tables with any rows are skipped.
     *
     * @param entry
     * @return true if item was cleaned up
     * @throws JdbcSQLException
     */
    public boolean cleanupItem(ItemTableCheckEntry entry) throws JdbcSQLException {
        return cleanupItem(entry, false);
    }

    private boolean cleanupItem(ItemTableCheckEntry entry, boolean force) throws JdbcSQLException {
        if (!checkDBAccessability()) {
            logger.warn("JDBC::cleanupItem: database not connected");
            return false;
        }

        ItemTableCheckEntryStatus status = entry.getStatus();
        String tableName = entry.getTableName();
        switch (status) {
            case ITEM_MISSING:
                if (!force && getRowCount(tableName) > 0) {
                    return false;
                }
                dropTable(tableName);
                // Fall through to remove from index.
            case TABLE_MISSING:
            case ITEM_AND_TABLE_MISSING:
                if (!conf.getTableUseRealCaseSensitiveItemNames()) {
                    ItemsVO itemsVo = new ItemsVO();
                    itemsVo.setItemName(entry.getItemName());
                    itemsVo.setItemsManageTable(conf.getItemsManageTable());
                    deleteItemsEntry(itemsVo);
                }
                itemNameToTableNameMap.remove(entry.getItemName());
                return true;
            case ORPHAN_TABLE:
            case VALID:
            default:
                // Nothing to clean.
                return false;
        }
    }
}
