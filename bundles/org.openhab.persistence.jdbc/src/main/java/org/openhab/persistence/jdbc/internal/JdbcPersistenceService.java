/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
@ConfigurableService(category = "persistence", label = "JDBC Persistence Service", description_uri = JdbcPersistenceService.CONFIG_URI)
public class JdbcPersistenceService extends JdbcMapper implements ModifiablePersistenceService {

    private static final String SERVICE_ID = "jdbc";
    private static final String SERVICE_LABEL = "JDBC";
    protected static final String CONFIG_URI = "persistence:jdbc";

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
        return SERVICE_ID;
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return SERVICE_LABEL;
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
        long timerStart = System.currentTimeMillis();
        storeItemValue(item, state, date);
        if (logger.isDebugEnabled()) {
            logger.debug("JDBC: Stored item '{}' as '{}' in SQL database at {} in {} ms.", item.getName(), state,
                    new Date(), System.currentTimeMillis() - timerStart);
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

        String table = sqlTables.get(itemName);
        if (table == null) {
            logger.debug("JDBC::query: unable to find table for item with name: '{}', no data in database.", itemName);
            return List.of();
        }

        long timerStart = System.currentTimeMillis();
        List<HistoricItem> items = getHistItemFilterQuery(filter, conf.getNumberDecimalcount(), table, item);
        if (logger.isDebugEnabled()) {
            logger.debug("JDBC: Query for item '{}' returned {} rows in {} ms", itemName, items.size(),
                    System.currentTimeMillis() - timerStart);
        }

        // Success
        errCnt = 0;
        return items;
    }

    public void updateConfig(Map<Object, Object> configuration) {
        logger.debug("JDBC::updateConfig");

        conf = new JdbcConfiguration(configuration);
        if (conf.valid && checkDBAccessability()) {
            checkDBSchema();
            // connection has been established ... initialization completed!
            initialized = true;
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

        String table = sqlTables.get(itemName);
        if (table == null) {
            logger.debug("JDBC::remove: unable to find table for item with name: '{}', no data in database.", itemName);
            return false;
        }

        long timerStart = System.currentTimeMillis();
        boolean result = deleteItemValues(filter, table);
        if (logger.isDebugEnabled()) {
            logger.debug("JDBC: Deleted values for item '{}' in SQL database at {} in {} ms.", itemName, new Date(),
                    System.currentTimeMillis() - timerStart);
        }

        return result;
    }
}
