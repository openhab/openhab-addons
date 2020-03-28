/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb2.internal.influx2;

import static org.openhab.persistence.influxdb2.internal.InfluxDBConstants.*;
import static org.openhab.persistence.influxdb2.internal.InfluxDBStateConvertUtils.stateToString;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb2.internal.FilterCriteriaQueryCreator;

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
    @Override
    public String createQuery(FilterCriteria criteria, String bucket) {

        Flux flux = Flux.from(bucket);

        if (criteria.getBeginDateZoned() != null || criteria.getEndDateZoned() != null) {
            RangeFlux range = flux.range();
            if (criteria.getBeginDateZoned() != null)
                range = range.withStart(criteria.getBeginDateZoned().toInstant());
            if (criteria.getEndDateZoned() != null)
                range = range.withStop(criteria.getEndDateZoned().toInstant());
            flux = range;
        }

        if (criteria.getItemName() != null)
            flux = flux.filter().withPropertyValue(TAG_ITEM_NAME, criteria.getItemName());

        if (criteria.getState() != null && criteria.getOperator() != null) {
            Restrictions restrictions = Restrictions.and(Restrictions.field().equal(COLUMN_VALUE_NAME),
                    Restrictions.value().custom(criteria.getOperator().toString(), stateToString(criteria.getState())));
            flux = flux.filter(restrictions);
        }

        if (criteria.getOrdering() != null) {
            boolean desc = criteria.getOrdering() == FilterCriteria.Ordering.DESCENDING;
            flux = flux.sort().withDesc(desc).withColumns(new String[] { COLUMN_TIME_NAME });
        }

        flux.limit(criteria.getPageSize()).withPropertyValue("offset",
                criteria.getPageNumber() * criteria.getPageSize());

        return flux.toString();
    }

}
