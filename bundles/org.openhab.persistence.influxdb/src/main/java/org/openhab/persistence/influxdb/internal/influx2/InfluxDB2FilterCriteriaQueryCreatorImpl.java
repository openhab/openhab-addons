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
package org.openhab.persistence.influxdb.internal.influx2;

import static com.influxdb.query.dsl.functions.restriction.Restrictions.measurement;
import static com.influxdb.query.dsl.functions.restriction.Restrictions.tag;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils.stateToObject;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.types.State;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataService;
import org.openhab.persistence.influxdb.internal.InfluxDBVersion;

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
    public String createQuery(FilterCriteria criteria, String retentionPolicy) {
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
        String name = influxDBMetadataService.getMeasurementNameOrDefault(itemName, itemName);
        String measurementName = configuration.isReplaceUnderscore() ? name.replace('_', '.') : name;
        flux = flux.filter(measurement().equal(measurementName));
        if (!measurementName.equals(itemName)) {
            flux = flux.filter(tag(TAG_ITEM_NAME).equal(itemName));
            flux = flux.keep(
                    new String[] { FIELD_MEASUREMENT_NAME, COLUMN_TIME_NAME_V2, COLUMN_VALUE_NAME_V2, TAG_ITEM_NAME });
        } else {
            flux = flux.keep(new String[] { FIELD_MEASUREMENT_NAME, COLUMN_TIME_NAME_V2, COLUMN_VALUE_NAME_V2 });
        }

        State filterState = criteria.getState();
        if (filterState != null && criteria.getOperator() != null) {
            Restrictions restrictions = Restrictions.and(Restrictions.field().equal(FIELD_VALUE_NAME),
                    Restrictions.value().custom(stateToObject(filterState),
                            getOperationSymbol(criteria.getOperator(), InfluxDBVersion.V2)));
            flux = flux.filter(restrictions);
        }

        flux = applyOrderingAndPageSize(criteria, flux);

        return flux.toString();
    }

    private Flux applyOrderingAndPageSize(FilterCriteria criteria, Flux flux) {
        var lastOptimization = criteria.getOrdering() == FilterCriteria.Ordering.DESCENDING
                && criteria.getPageSize() == 1;

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
