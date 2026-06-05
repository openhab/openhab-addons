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
package org.openhab.persistence.influxdb.internal.influx2;

import static com.influxdb.query.dsl.functions.restriction.Restrictions.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils.stateToObject;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.types.State;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;

import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.RangeFlux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;

/**
 * Implementation of {@link FilterCriteriaQueryCreator} for InfluxDB 2.0
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDB2FilterCriteriaQueryCreatorImpl implements FilterCriteriaQueryCreator {
    private final InfluxDBConfiguration configuration;
    private final InfluxDBMetadataService influxDBMetadataService;

    public InfluxDB2FilterCriteriaQueryCreatorImpl(InfluxDBConfiguration configuration,
            InfluxDBMetadataService influxDBMetadataService) {
        this.configuration = configuration;
        this.influxDBMetadataService = influxDBMetadataService;
    }

    @Override
    public String createQuery(FilterCriteria criteria, String retentionPolicy, @Nullable String alias) {
        Flux flux = Flux.from(retentionPolicy);

        RangeFlux range = flux.range();
        if (criteria.getBeginDate() != null) {
            range.withStart(criteria.getBeginDate().toInstant());
        } else {
            range.withStart(-100L, ChronoUnit.YEARS); // Flux needs a mandatory start range
        }
        if (criteria.getEndDate() != null) {
            range.withStop(criteria.getEndDate().toInstant());
        } else {
            range.withStop(100L, ChronoUnit.YEARS);
        }
        flux = range;

        String itemName = Objects.requireNonNull(criteria.getItemName()); // we checked non-null before
        final String localAlias = alias != null ? alias : itemName;
        String name = influxDBMetadataService.getMeasurementNameOrDefault(localAlias);
        String measurementName = configuration.isReplaceUnderscore() ? name.replace('_', '.') : name;
        flux = flux.filter(measurement().equal(measurementName));
        // Data is stored with TAG_ITEM_NAME set to the alias (see InfluxDBPersistenceService.convert()),
        // so filter and condition must use localAlias, not itemName.
        boolean needsItemTag = !measurementName.equals(localAlias);
        if (needsItemTag) {
            flux = flux.filter(tag(TAG_ITEM_NAME).equal(localAlias));
        }

        State filterState = criteria.getState();
        if (filterState != null) {
            Restrictions restrictions = Restrictions.and(Restrictions.field().equal(FIELD_VALUE_NAME),
                    Restrictions.value().custom(stateToObject(filterState), criteria.getOperator().getSymbol()));
            flux = flux.filter(restrictions);
        }

        // Apply grouping/ordering/pagination before keep() so they can push down to the storage
        // layer, and so that the state-value filter above (which relies on _field) still sees that
        // column. keep() runs last, purely to project the output columns.
        flux = applyOrderingAndPageSize(criteria, flux);

        if (needsItemTag) {
            flux = flux.keep(
                    new String[] { FIELD_MEASUREMENT_NAME, COLUMN_TIME_NAME_V2, COLUMN_VALUE_NAME_V2, TAG_ITEM_NAME });
        } else {
            flux = flux.keep(new String[] { FIELD_MEASUREMENT_NAME, COLUMN_TIME_NAME_V2, COLUMN_VALUE_NAME_V2 });
        }

        return flux.toString();
    }

    private Flux applyOrderingAndPageSize(FilterCriteria criteria, Flux flux) {
        // Only the first page may use last(); pageNumber > 0 requires an offset, which last()
        // cannot express (it would always return the most recent point), so fall back to
        // sort()/limit() in that case.
        var lastOptimization = criteria.getOrdering() == FilterCriteria.Ordering.DESCENDING
                && criteria.getPageSize() == 1 && criteria.getPageNumber() == 0;

        // A single measurement can map to several InfluxDB series: incidental tags such as
        // category/type/label, or schema changes over the item's lifetime, split the data into
        // distinct series under the same measurement. last(), sort() and limit() all operate per
        // series, so collapse the series into a single per-measurement table first; otherwise
        // last() returns one (arbitrary, possibly stale) row per series and sort()/limit()/offset
        // are applied per series, corrupting ordering and pagination. group() stays adjacent to
        // the storage read, so the query still pushes down (group() + last() in particular fuses
        // into a single ReadGroupAggregate that seeks straight to the most recent point).
        flux = flux.groupBy(new String[] { FIELD_MEASUREMENT_NAME });

        if (lastOptimization) {
            flux = flux.last();
        } else {
            if (criteria.getOrdering() != null) {
                boolean desc = criteria.getOrdering() == FilterCriteria.Ordering.DESCENDING;
                flux = flux.sort().withDesc(desc).withColumns(new String[] { COLUMN_TIME_NAME_V2 });
            }

            if (criteria.getPageSize() != Integer.MAX_VALUE) {
                flux = flux.limit(criteria.getPageSize()).withPropertyValue("offset",
                        criteria.getPageNumber() * criteria.getPageSize());
            }
        }
        return flux;
    }
}
