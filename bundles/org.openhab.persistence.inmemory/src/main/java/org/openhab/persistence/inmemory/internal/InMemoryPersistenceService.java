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
package org.openhab.persistence.inmemory.internal;

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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.ModifiablePersistenceService;
import org.openhab.core.persistence.PersistenceItemInfo;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.strategy.PersistenceStrategy;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the InMemory {@link PersistenceService}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { PersistenceService.class, ModifiablePersistenceService.class })
public class InMemoryPersistenceService implements ModifiablePersistenceService {

    private static final String SERVICE_ID = "inmemory";
    private static final String SERVICE_LABEL = "InMemory";

    private final Map<String, TreeSet<PersistEntry>> persistMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(InMemoryPersistenceService.class);

    @Activate
    public void activate() {
        logger.debug("InMemory persistence service is now activated");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("InMemory persistence service deactivated");
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
    public boolean remove(FilterCriteria filter) throws IllegalArgumentException {
        String itemName = filter.getItemName();
        if (itemName == null) {
            return false;
        }

        TreeSet<PersistEntry> valueSet = persistMap.get(itemName);
        if (valueSet == null) {
            return false;
        }

        List<PersistEntry> toRemove = valueSet.stream().filter(e -> applies(e, filter)).toList();
        toRemove.forEach(valueSet::remove);

        return true;
    }

    @Override
    public Iterable<HistoricItem> query(FilterCriteria filter) {
        String itemName = filter.getItemName();
        if (itemName == null) {
            return List.of();
        }

        TreeSet<PersistEntry> valueSet = persistMap.get(itemName);
        if (valueSet == null) {
            return List.of();
        }

        return valueSet.stream().filter(e -> applies(e, filter)).map(e -> toHistoricItem(itemName, e)).toList();
    }

    @Override
    public List<PersistenceStrategy> getDefaultStrategies() {
        // persist nothing by default
        return List.of();
    }

    private PersistenceItemInfo toItemInfo(Map.Entry<String, TreeSet<PersistEntry>> itemEntry) {
        return new PersistenceItemInfo() {

            @Override
            public String getName() {
                return itemEntry.getKey();
            }

            @Override
            public @Nullable Integer getCount() {
                return itemEntry.getValue().size();
            }

            @Override
            public @Nullable Date getEarliest() {
                return Date.from(itemEntry.getValue().first().timestamp().toInstant());
            }

            @Override
            public @Nullable Date getLatest() {
                return Date.from(itemEntry.getValue().last().timestamp().toInstant());
            }
        };
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

        TreeSet<PersistEntry> valueSet = Objects.requireNonNull(persistMap.computeIfAbsent(itemName,
                k -> new TreeSet<>(Comparator.comparing(PersistEntry::timestamp))));
        valueSet.add(new PersistEntry(timestamp, state));
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
    };
}
