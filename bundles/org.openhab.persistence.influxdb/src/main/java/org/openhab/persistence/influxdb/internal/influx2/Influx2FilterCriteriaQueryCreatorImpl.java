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
package org.openhab.persistence.influxdb.internal.influx2;

import static com.influxdb.query.dsl.functions.restriction.Restrictions.measurement;
import static com.influxdb.query.dsl.functions.restriction.Restrictions.tag;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils.stateToObject;

import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataUtils;
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
public class Influx2FilterCriteriaQueryCreatorImpl implements FilterCriteriaQueryCreator {

    private InfluxDBConfiguration configuration;
    private MetadataRegistry metadataRegistry;

    public Influx2FilterCriteriaQueryCreatorImpl(InfluxDBConfiguration configuration,
            MetadataRegistry metadataRegistry) {
        this.configuration = configuration;
        this.metadataRegistry = metadataRegistry;
    }

    @Override
    public String createQuery(FilterCriteria criteria, String retentionPolicy) {
        Flux flux = Flux.from(retentionPolicy);

        RangeFlux range = flux.range();
        if (criteria.getBeginDate() != null) {
            range = range.withStart(criteria.getBeginDate().toInstant());
        } else {
            range = flux.range(-100L, ChronoUnit.YEARS); // Flux needs a mandatory start range
        }
        if (criteria.getEndDate() != null) {
            range = range.withStop(criteria.getEndDate().toInstant());
        }
        flux = range;

        String itemName = criteria.getItemName();
        if (itemName != null) {
            String measurementName = calculateMeasurementName(itemName);
            boolean needsToUseItemTagName = !measurementName.equals(itemName);

            flux = flux.filter(measurement().equal(measurementName));
            if (needsToUseItemTagName) {
                flux = flux.filter(tag(TAG_ITEM_NAME).equal(itemName));
            }

            if (needsToUseItemTagName)
                flux = flux.keep(new String[] { FIELD_MEASUREMENT_NAME, COLUMN_TIME_NAME_V2, COLUMN_VALUE_NAME_V2,
                        TAG_ITEM_NAME });
            else
                flux = flux.keep(new String[] { FIELD_MEASUREMENT_NAME, COLUMN_TIME_NAME_V2, COLUMN_VALUE_NAME_V2 });
        }

        if (criteria.getState() != null && criteria.getOperator() != null) {
            Restrictions restrictions = Restrictions.and(Restrictions.field().equal(FIELD_VALUE_NAME),
                    Restrictions.value().custom(stateToObject(criteria.getState()),
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

    private String calculateMeasurementName(String itemName) {
        String name = itemName;

        name = InfluxDBMetadataUtils.calculateMeasurementNameFromMetadataIfPresent(metadataRegistry, name, itemName);

        if (configuration.isReplaceUnderscore()) {
            name = name.replace('_', '.');
        }

        return name;
    }
}
