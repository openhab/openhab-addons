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
package org.openhab.persistence.inmemory.internal;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.ConfigurableService;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the volatile {@link PersistenceService}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { PersistenceService.class,
        ModifiablePersistenceService.class }, configurationPid = "org.openhab.inmemory", //
        property = Constants.SERVICE_PID + "=org.openhab.inmemory")
@ConfigurableService(category = "persistence", label = "InMemory Persistence Service", description_uri = InMemoryPersistenceService.CONFIG_URI)
public class InMemoryPersistenceService implements ModifiablePersistenceService {

    private static final String SERVICE_ID = "inmemory";
    private static final String SERVICE_LABEL = "In Memory";

    protected static final String CONFIG_URI = "persistence:inmemory";
    private final String MAX_ENTRIES_CONFIG = "maxEntries";
    private final long MAX_ENTRIES_DEFAULT = 512;

    private final Logger logger = LoggerFactory.getLogger(InMemoryPersistenceService.class);

    private final Map<String, PersistItem> persistMap = new ConcurrentHashMap<>();
    private long maxEntries = MAX_ENTRIES_DEFAULT;

    @Activate
    public void activate(Map<String, Object> config) {
        modified(config);
        logger.debug("InMemory persistence service is now activated.");
    }

    @Modified
    public void modified(Map<String, Object> config) {
        maxEntries = ConfigParser.valueAsOrElse(config.get(MAX_ENTRIES_CONFIG), Long.class, MAX_ENTRIES_DEFAULT);

        persistMap.values().forEach(persistItem -> {
            Lock lock = persistItem.lock();
            lock.lock();
            try {
                while (persistItem.database().size() > maxEntries) {
                    persistItem.database().pollFirst();
                }
            } finally {
                lock.unlock();
            }
        });
    }

    @Deactivate
    public void deactivate() {
        logger.debug("InMemory persistence service deactivated.");
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
        return persistMap.entrySet().stream().map(this::toItemInfo).collect(Collectors.toSet());
    }

    @Override
    public void store(Item item) {
        internalStore(item.getName(), ZonedDateTime.now(), item.getState());
    }

    @Override
    public void store(Item item, @Nullable String alias) {
        String finalName = Objects.requireNonNullElse(alias, item.getName());
        internalStore(finalName, ZonedDateTime.now(), item.getState());
    }

    @Override
    public void store(Item item, ZonedDateTime date, State state) {
        internalStore(item.getName(), date, state);
    }

    @Override
    public void store(Item item, ZonedDateTime date, State state, @Nullable String alias) {
        internalStore(Objects.requireNonNullElse(alias, item.getName()), date, state);
    }

    @Override
    public boolean remove(FilterCriteria filter) throws IllegalArgumentException {
        String itemName = filter.getItemName();
        if (itemName == null) {
            return false;
        }

        PersistItem persistItem = persistMap.get(itemName);
        if (persistItem == null) {
            return false;
        }

        Lock lock = persistItem.lock();
        lock.lock();
        try {
            List<PersistEntry> toRemove = persistItem.database().stream().filter(e -> applies(e, filter)).toList();
            toRemove.forEach(persistItem.database()::remove);
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        String itemName = filter.getItemName();
        if (itemName == null) {
            return List.of();
        }

        PersistItem persistItem = persistMap.get(itemName);
        if (persistItem == null) {
            return List.of();
        }

        Lock lock = persistItem.lock();
        lock.lock();

        Comparator<PersistEntry> comparator = filter.getOrdering() == FilterCriteria.Ordering.ASCENDING
                ? Comparator.comparing(PersistEntry::timestamp)
                : Comparator.comparing(PersistEntry::timestamp).reversed();

        try {
            return persistItem.database().stream().filter(e -> applies(e, filter)).sorted(comparator)
                    .map(e -> toHistoricItem(itemName, e)).toList();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        // persist nothing by default
        return List.of();
    }

    private PersistenceItemInfo toItemInfo(Map.Entry<String, PersistItem> itemEntry) {
        Lock lock = itemEntry.getValue().lock();
        lock.lock();
        try {
            String name = itemEntry.getKey();
            Integer count = itemEntry.getValue().database().size();
            Instant earliest = itemEntry.getValue().database().first().timestamp().toInstant();
            Instant latest = itemEntry.getValue().database.last().timestamp.toInstant();
            return new PersistenceItemInfo() {

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public @Nullable Integer getCount() {
                    return count;
                }

                @Override
                public @Nullable Date getEarliest() {
                    return Date.from(earliest);
                }

                @Override
                public @Nullable Date getLatest() {
                    return Date.from(latest);
                }
            };
        } finally {
            lock.unlock();
        }
    }

    private HistoricItem toHistoricItem(String itemName, PersistEntry entry) {
        return new HistoricItem() {
            @Override
            public ZonedDateTime getTimestamp() {
                return entry.timestamp();
            }

            @Override
            public State getState() {
                return entry.state();
            }

            @Override
            public String getName() {
                return itemName;
            }
        };
    }

    private void internalStore(String itemName, ZonedDateTime timestamp, State state) {
        if (state instanceof UnDefType) {
            return;
        }

        PersistItem persistItem = Objects.requireNonNull(persistMap.computeIfAbsent(itemName,
                k -> new PersistItem(new TreeSet<>(Comparator.comparing(PersistEntry::timestamp)),
                        new ReentrantLock())));

        Lock lock = persistItem.lock();
        lock.lock();
        try {
            persistItem.database().add(new PersistEntry(timestamp, state));

            while (persistItem.database.size() > maxEntries) {
                persistItem.database().pollFirst();
            }
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings({ "rawType", "unchecked" })
    private boolean applies(PersistEntry entry, FilterCriteria filter) {
        ZonedDateTime beginDate = filter.getBeginDate();
        if (beginDate != null && entry.timestamp().isBefore(beginDate)) {
            return false;
        }
        ZonedDateTime endDate = filter.getEndDate();
        if (endDate != null && entry.timestamp().isAfter(endDate)) {
            return false;
        }

        State refState = filter.getState();
        FilterCriteria.Operator operator = filter.getOperator();
        if (refState == null) {
            // no state filter
            return true;
        }

        if (operator == FilterCriteria.Operator.EQ) {
            return entry.state().equals(refState);
        }

        if (operator == FilterCriteria.Operator.NEQ) {
            return !entry.state().equals(refState);
        }

        if (entry.state() instanceof Comparable comparableState && entry.state.getClass().equals(refState.getClass())) {
            if (operator == FilterCriteria.Operator.GT) {
                return comparableState.compareTo(refState) > 0;
            }
            if (operator == FilterCriteria.Operator.GTE) {
                return comparableState.compareTo(refState) >= 0;
            }
            if (operator == FilterCriteria.Operator.LT) {
                return comparableState.compareTo(refState) < 0;
            }
            if (operator == FilterCriteria.Operator.LTE) {
                return comparableState.compareTo(refState) <= 0;
            }
        } else {
            logger.warn("Using operator {} but state {} is not comparable!", operator, refState);
        }
        return true;
    }

    private record PersistEntry(ZonedDateTime timestamp, State state) {
    }

    private record PersistItem(TreeSet<PersistEntry> database, Lock lock) {
    }
}
