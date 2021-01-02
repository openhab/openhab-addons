/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal.influx1;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils.stateToObject;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.influxdb.dto.Query;
import org.influxdb.querybuilder.Appender;
import org.influxdb.querybuilder.BuiltQuery;
import org.influxdb.querybuilder.Select;
import org.influxdb.querybuilder.Where;
import org.influxdb.querybuilder.clauses.SimpleClause;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBVersion;

/**
 * Implementation of {@link FilterCriteriaQueryCreator} for InfluxDB 1.0
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class Influx1FilterCriteriaQueryCreatorImpl implements FilterCriteriaQueryCreator {

    @Override
    public String createQuery(FilterCriteria criteria, String retentionPolicy) {
        final String tableName;
        boolean hasCriteriaName = criteria.getItemName() != null;
        if (hasCriteriaName) {
            tableName = criteria.getItemName();
        } else {
            tableName = "/.*/";
        }

        Select select = select(COLUMN_VALUE_NAME_V1).fromRaw(null,
                fullQualifiedTableName(retentionPolicy, tableName, hasCriteriaName));

        Where where = select.where();
        if (criteria.getBeginDate() != null) {
            where = where.and(
                    BuiltQuery.QueryBuilder.gte(COLUMN_TIME_NAME_V1, criteria.getBeginDate().toInstant().toString()));
        }
        if (criteria.getEndDate() != null) {
            where = where.and(
                    BuiltQuery.QueryBuilder.lte(COLUMN_TIME_NAME_V1, criteria.getEndDate().toInstant().toString()));
        }

        if (criteria.getState() != null && criteria.getOperator() != null) {
            where = where.and(new SimpleClause(COLUMN_VALUE_NAME_V1,
                    getOperationSymbol(criteria.getOperator(), InfluxDBVersion.V1),
                    stateToObject(criteria.getState())));
        }

        if (criteria.getOrdering() == FilterCriteria.Ordering.DESCENDING) {
            select = select.orderBy(desc());
        } else if (criteria.getOrdering() == FilterCriteria.Ordering.ASCENDING) {
            select = select.orderBy(asc());
        }

        if (criteria.getPageSize() != Integer.MAX_VALUE) {
            if (criteria.getPageNumber() != 0) {
                select = select.limit(criteria.getPageSize(), criteria.getPageSize() * criteria.getPageNumber());
            } else {
                select = select.limit(criteria.getPageSize());
            }
        }

        final Query query = (Query) select;
        return query.getCommand();
    }

    private String fullQualifiedTableName(String retentionPolicy, String tableName, boolean escapeTableName) {
        StringBuilder sb = new StringBuilder();
        Appender.appendName(retentionPolicy, sb);
        sb.append(".");
        if (escapeTableName) {
            Appender.appendName(tableName, sb);
        } else {
            sb.append(tableName);
        }
        return sb.toString();
    }
}
