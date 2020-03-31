/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb2.internal.influx1;

import java.util.Date;

import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb2.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb2.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb2.internal.InfluxDBConstants;
import org.openhab.persistence.influxdb2.internal.InfluxDBStateConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link FilterCriteriaQueryCreator} for InfluxDB 1.0
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
public class Influx1FilterCriteriaQueryCreatorImpl implements FilterCriteriaQueryCreator {
    private final Logger logger = LoggerFactory.getLogger(Influx1FilterCriteriaQueryCreatorImpl.class);
    private final InfluxDBConfiguration configuration;

    public Influx1FilterCriteriaQueryCreatorImpl(InfluxDBConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String createQuery(FilterCriteria filter, String bucket) {
        StringBuffer query = new StringBuffer();
        query.append("select ").append(InfluxDBConstants.COLUMN_VALUE_NAME).append(' ').append("from \"")
                .append(configuration.getRetentionPolicy()).append("\".");

        if (filter.getItemName() != null) {
            query.append('"').append(filter.getItemName()).append('"');
        } else {
            query.append("/.*/");
        }

        logger.trace(
                "Filter: itemname: {}, ordering: {}, state: {},  operator: {}, getBeginDate: {}, getEndDate: {}, getPageSize: {}, getPageNumber: {}",
                filter.getItemName(), filter.getOrdering().toString(), filter.getState(), filter.getOperator(),
                filter.getBeginDate(), filter.getEndDate(), filter.getPageSize(), filter.getPageNumber());

        if ((filter.getState() != null && filter.getOperator() != null) || filter.getBeginDate() != null
                || filter.getEndDate() != null) {
            query.append(" where ");
            boolean foundState = false;
            boolean foundBeginDate = false;
            if (filter.getState() != null && filter.getOperator() != null) {
                String value = InfluxDBStateConvertUtils.stateToString(filter.getState());
                if (value != null) {
                    foundState = true;
                    query.append(InfluxDBConstants.COLUMN_VALUE_NAME);
                    query.append(" ");
                    query.append(filter.getOperator().toString());
                    query.append(" ");
                    query.append(value);
                }
            }

            if (filter.getBeginDate() != null) {
                foundBeginDate = true;
                if (foundState) {
                    query.append(" and");
                }
                query.append(" ");
                query.append(InfluxDBConstants.COLUMN_TIME_NAME);
                query.append(" > ");
                query.append(getTimeFilter(filter.getBeginDate()));
                query.append(" ");
            }

            if (filter.getEndDate() != null) {
                if (foundState || foundBeginDate) {
                    query.append(" and");
                }
                query.append(" ");
                query.append(InfluxDBConstants.COLUMN_TIME_NAME);
                query.append(" < ");
                query.append(getTimeFilter(filter.getEndDate()));
                query.append(" ");
            }
        }

        if (filter.getOrdering() == FilterCriteria.Ordering.DESCENDING) {
            query.append(String.format(" ORDER BY %s DESC", InfluxDBConstants.COLUMN_TIME_NAME));
            logger.debug("descending ordering ");
        }

        int limit = (filter.getPageNumber() + 1) * filter.getPageSize();
        query.append(" limit " + limit);
        logger.trace("appending limit {}", limit);

        int totalEntriesAffected = ((filter.getPageNumber() + 1) * filter.getPageSize());
        int startEntryNum = totalEntriesAffected
                - (totalEntriesAffected - (filter.getPageSize() * filter.getPageNumber()));
        logger.trace("startEntryNum {}", startEntryNum);

        final String queryString = query.toString();
        logger.debug("query string: {}", queryString);

        return queryString;
    }

    private String getTimeFilter(Date time) {
        // for some reason we need to query using 'seconds' only
        // passing milli seconds causes no results to be returned
        long milliSeconds = time.getTime();
        long seconds = milliSeconds / 1000;
        return seconds + "s";
    }
}
