/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.automation.optimalwindow.internal.handler;

import java.time.ZonedDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.optimalwindow.internal.calc.ForecastSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.QueryablePersistenceService;
import org.openhab.core.types.State;

/**
 * Reads forecast values from a persistence service.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class PersistenceForecastSource implements ForecastSource {
    private final PersistenceServiceRegistry persistenceServiceRegistry;

    public PersistenceForecastSource(PersistenceServiceRegistry persistenceServiceRegistry) {
        this.persistenceServiceRegistry = persistenceServiceRegistry;
    }

    @Override
    public SortedMap<Long, Double> getValues(String itemName, @Nullable String serviceId, ZonedDateTime begin,
            ZonedDateTime end) throws IllegalStateException {
        PersistenceService service = serviceId != null ? persistenceServiceRegistry.get(serviceId)
                : persistenceServiceRegistry.getDefault();
        if (!(service instanceof QueryablePersistenceService queryableService)) {
            throw new IllegalStateException("Persistence service '" + (serviceId != null ? serviceId : "default")
                    + "' is not available or not queryable");
        }

        FilterCriteria filter = new FilterCriteria().setItemName(itemName).setBeginDate(begin).setEndDate(end)
                .setOrdering(Ordering.ASCENDING);

        SortedMap<Long, Double> result = new TreeMap<>();
        for (HistoricItem historicItem : queryableService.query(filter)) {
            Double value = toDouble(historicItem.getState());
            if (value != null) {
                result.put(historicItem.getInstant().toEpochMilli(), value);
            }
        }
        return result;
    }

    private static @Nullable Double toDouble(State state) {
        if (state instanceof QuantityType<?> quantity) {
            return quantity.doubleValue();
        } else if (state instanceof DecimalType decimal) {
            return decimal.doubleValue();
        }
        return null;
    }
}
