/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.jpa.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.jpa.internal.model.JpaPersistentItem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

/**
 * JPA based implementation of QueryablePersistenceService.
 *
 * @author Manfred Bergmann - Initial contribution
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.jpa", //
        property = Constants.SERVICE_PID + "=org.openhab.jpa")
@ConfigurableService(category = "persistence", label = "JPA Persistence Service", description_uri = JpaPersistenceService.CONFIG_URI)
public class JpaPersistenceService implements QueryablePersistenceService {

    private static final String SERVICE_ID = "jpa";
    private static final String SERVICE_LABEL = "JPA";
    protected static final String CONFIG_URI = "persistence:jpa";

    private final Logger logger = LoggerFactory.getLogger(JpaPersistenceService.class);

    private final ItemRegistry itemRegistry;

    private @Nullable EntityManagerFactory emf;

    private @NonNullByDefault({}) JpaConfiguration config;

    private boolean initialized;

    @Activate
    public JpaPersistenceService(BundleContext context, Map<String, @Nullable Object> properties,
            final @Reference ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        logger.debug("Activating JPA persistence service");
        try {
            config = new JpaConfiguration(properties);
            initialized = true;
        } catch (IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
        }
    }

    /**
     * lazy loading because update() is called after activate()
     *
     * @return EntityManagerFactory
     */
    protected EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory emf = this.emf;
        if (emf == null) {
            emf = newEntityManagerFactory();
            this.emf = emf;
        }
        return emf;
    }

    /**
     * Closes the EntityPersistenceFactory
     */
    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating JPA persistence service");
        closeEntityManagerFactory();
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
    public void store(Item item) {
        store(item, null);
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        logger.debug("Storing item: {}", item.getName());

        if (item.getState() instanceof UnDefType) {
            logger.debug("This item is of undefined type. Cannot persist it!");
            return;
        }

        if (!initialized) {
            logger.debug("Cannot create EntityManagerFactory without a valid configuration!");
            return;
        }

        // determine item name to be stored
        String name = (alias != null) ? alias : item.getName();

        JpaPersistentItem pItem = new JpaPersistentItem();
        try {
            String newValue = StateHelper.toString(item.getState());
            pItem.setValue(newValue);
            logger.debug("Stored new value: {}", newValue);
        } catch (Exception e1) {
            logger.error("Error while converting state value to string: {}", e1.getMessage());
            return;
        }
        pItem.setName(name);
        pItem.setRealName(item.getName());
        pItem.setTimestamp(new Date());

        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            logger.debug("Persisting item...");
            // In RESOURCE_LOCAL calls to EntityManager require a begin/commit
            em.getTransaction().begin();
            em.persist(pItem);
            em.getTransaction().commit();
            logger.debug("Persisting item...done");
        } catch (Exception e) {
            if (e.getCause() instanceof EntityExistsException) {
                // there's a UNIQUE constraint in the database, and we tried to write
                // a duplicate timestamp. Just ignore
                logger.debug("Failed to persist item {} because of duplicate timestamp", name);
            } else {
                logger.error("Error while persisting item! Rolling back!", e);
            }
            em.getTransaction().rollback();
        } finally {
            em.close();
        }

        logger.debug("Storing item...done");
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return Set.of();
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        logger.debug("Querying for historic item: {}", filter.getItemName());

        if (!initialized) {
            logger.warn("Cannot create EntityManagerFactory without a valid configuration!");
            return List.of();
        }

        String itemName = filter.getItemName();
        if (itemName == null) {
            logger.warn("Item name is missing in filter {}", filter);
            return List.of();
        }
        Item item = getItemFromRegistry(itemName);
        if (item == null) {
            logger.debug("Item '{}' does not exist in the item registry", itemName);
            return List.of();
        }

        String sortOrder;
        if (filter.getOrdering() == Ordering.ASCENDING) {
            sortOrder = "ASC";
        } else {
            sortOrder = "DESC";
        }

        boolean hasBeginDate = false;
        boolean hasEndDate = false;
        String queryString = "SELECT n FROM " + JpaPersistentItem.class.getSimpleName()
                + " n WHERE n.realName = :itemName";
        if (filter.getBeginDate() != null) {
            queryString += " AND n.timestamp >= :beginDate";
            hasBeginDate = true;
        }
        if (filter.getEndDate() != null) {
            queryString += " AND n.timestamp <= :endDate";
            hasEndDate = true;
        }
        queryString += " ORDER BY n.timestamp " + sortOrder;

        logger.debug("The query: {}", queryString);

        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            // In RESOURCE_LOCAL calls to EntityManager require a begin/commit
            em.getTransaction().begin();

            logger.debug("Creating query...");
            Query query = em.createQuery(queryString);
            query.setParameter("itemName", item.getName());
            if (hasBeginDate) {
                query.setParameter("beginDate", Date.from(filter.getBeginDate().toInstant()));
            }
            if (hasEndDate) {
                query.setParameter("endDate", Date.from(filter.getEndDate().toInstant()));
            }

            query.setFirstResult(filter.getPageNumber() * filter.getPageSize());
            query.setMaxResults(filter.getPageSize());
            logger.debug("Creating query...done");

            logger.debug("Retrieving result list...");
            @SuppressWarnings("unchecked")
            List<JpaPersistentItem> result = query.getResultList();
            logger.debug("Retrieving result list...done");

            List<HistoricItem> historicList = JpaHistoricItem.fromResultList(result, item);
            logger.debug("Convert to HistoricItem: {}", historicList.size());

            em.getTransaction().commit();

            return historicList;
        } catch (Exception e) {
            logger.error("Error while querying database!", e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }

        return List.of();
    }

    /**
     * Creates a new EntityManagerFactory with properties read from openhab.cfg via JpaConfiguration.
     *
     * @return initialized EntityManagerFactory
     */
    protected EntityManagerFactory newEntityManagerFactory() {
        logger.trace("Creating EntityManagerFactory...");

        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", config.dbConnectionUrl);
        properties.put("jakarta.persistence.jdbc.driver", config.dbDriverClass);
        if (!config.dbUserName.isBlank()) {
            properties.put("jakarta.persistence.jdbc.user", config.dbUserName);
        }
        if (!config.dbPassword.isBlank()) {
            properties.put("jakarta.persistence.jdbc.password", config.dbPassword);
        }
        if (config.dbUserName.isBlank() && config.dbPassword.isBlank()) {
            logger.info("It is recommended to use a password to protect the JPA persistence data store");
        }
        if (!config.dbSyncMapping.isBlank()) {
            logger.info("You are setting openjpa.jdbc.SynchronizeMappings, I hope you know what you're doing!");
            properties.put("openjpa.jdbc.SynchronizeMappings", config.dbSyncMapping);
        }

        EntityManagerFactory factory = Persistence.createEntityManagerFactory(getPersistenceUnitName(), properties);
        logger.debug("Creating EntityManagerFactory...done");

        return factory;
    }

    /**
     * Closes EntityManagerFactory
     */
    protected void closeEntityManagerFactory() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
        logger.debug("Closing down entity objects...done");
    }

    /**
     * Checks if EntityManagerFactory is open
     *
     * @return true when open, false otherwise
     */
    protected boolean isEntityManagerFactoryOpen() {
        return emf != null && emf.isOpen();
    }

    /**
     * Return the persistence unit as in persistence.xml file.
     *
     * @return the persistence unit name
     */
    protected String getPersistenceUnitName() {
        return "default";
    }

    /**
     * Retrieves the item for the given name from the item registry
     *
     * @param itemName
     * @return item
     */
    private @Nullable Item getItemFromRegistry(String itemName) {
        try {
            return itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e1) {
            logger.error("Unable to get item type for {}", itemName);
        }
        return null;
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        return List.of();
    }
}
