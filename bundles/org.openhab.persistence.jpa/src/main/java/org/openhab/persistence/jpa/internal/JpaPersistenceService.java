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
package org.openhab.persistence.jpa.internal;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA based implementation of QueryablePersistenceService.
 *
 * @author Manfred Bergmann - Initial contribution
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        QueryablePersistenceService.class }, configurationPid = "org.openhab.jpa", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class JpaPersistenceService implements QueryablePersistenceService {
    private final Logger logger = LoggerFactory.getLogger(JpaPersistenceService.class);

    private final ItemRegistry itemRegistry;

    private @Nullable EntityManagerFactory emf = null;

    private @NonNullByDefault({}) JpaConfiguration config;

    @Activate
    public JpaPersistenceService(final @Reference ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
    }

    /**
     * lazy loading because update() is called after activate()
     *
     * @return EntityManagerFactory
     */
    protected @Nullable EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = newEntityManagerFactory();
        }
        return emf;
    }

    @Activate
    public void activate(BundleContext context, Map<String, Object> properties) {
        logger.debug("Activating jpa persistence service");
        config = new JpaConfiguration(properties);
    }

    /**
     * Closes the EntityPersistenceFactory
     */
    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating jpa persistence service");
        closeEntityManagerFactory();
    }

    @Override
    public String getId() {
        return "jpa";
    }

    @Override
    public String getLabel(@Nullable Locale locale) {
        return "JPA";
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

        if (!JpaConfiguration.isInitialized) {
            logger.debug("Trying to create EntityManagerFactory but we don't have configuration yet!");
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
            logger.error("Error on converting state value to string: {}", e1.getMessage());
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
            logger.error("Error on persisting item! Rolling back!", e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }

        logger.debug("Storing item...done");
    }

    @Override
    public Set<PersistenceItemInfo> getItemInfo() {
        return Collections.emptySet();
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        logger.debug("Querying for historic item: {}", filter.getItemName());

        if (!JpaConfiguration.isInitialized) {
            logger.warn("Trying to create EntityManagerFactory but we don't have configuration yet!");
            return Collections.emptyList();
        }

        String itemName = filter.getItemName();
        Item item = getItemFromRegistry(itemName);

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
            logger.debug("{}", String.format("Convert to HistoricItem: %d", historicList.size()));

            em.getTransaction().commit();

            return historicList;
        } catch (Exception e) {
            logger.error("Error on querying database!", e);
            em.getTransaction().rollback();
        } finally {
            em.close();
        }

        return Collections.emptyList();
    }

    /**
     * Creates a new EntityManagerFactory with properties read from openhab.cfg via JpaConfiguration.
     *
     * @return initialized EntityManagerFactory
     */
    protected EntityManagerFactory newEntityManagerFactory() {
        logger.trace("Creating EntityManagerFactory...");

        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", config.dbConnectionUrl);
        properties.put("javax.persistence.jdbc.driver", config.dbDriverClass);
        if (config.dbUserName != null) {
            properties.put("javax.persistence.jdbc.user", config.dbUserName);
        }
        if (config.dbPassword != null) {
            properties.put("javax.persistence.jdbc.password", config.dbPassword);
        }
        if (config.dbUserName != null && config.dbPassword == null) {
            logger.warn("JPA persistence - it is recommended to use a password to protect data store");
        }
        if (config.dbSyncMapping != null && !config.dbSyncMapping.isBlank()) {
            logger.warn("You are settings openjpa.jdbc.SynchronizeMappings, I hope you know what you're doing!");
            properties.put("openjpa.jdbc.SynchronizeMappings", config.dbSyncMapping);
        }

        EntityManagerFactory fac = Persistence.createEntityManagerFactory(getPersistenceUnitName(), properties);
        logger.debug("Creating EntityManagerFactory...done");

        return fac;
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
        return Collections.emptyList();
    }
}
