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
package org.openhab.persistence.influxdb.internal.influx1;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;
import static org.openhab.persistence.influxdb.internal.InfluxDBStateConvertUtils.stateToObject;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.influxdb.dto.Query;
import org.influxdb.querybuilder.BuiltQuery;
import org.influxdb.querybuilder.Select;
import org.influxdb.querybuilder.Where;
import org.influxdb.querybuilder.clauses.SimpleClause;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataUtils;
import org.openhab.persistence.influxdb.internal.InfluxDBVersion;

/**
 * Implementation of {@link FilterCriteriaQueryCreator} for InfluxDB 1.0
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class Influx1FilterCriteriaQueryCreatorImpl implements FilterCriteriaQueryCreator {

    private InfluxDBConfiguration configuration;
    private MetadataRegistry metadataRegistry;

    public Influx1FilterCriteriaQueryCreatorImpl(InfluxDBConfiguration configuration,
            MetadataRegistry metadataRegistry) {
        this.configuration = configuration;
        this.metadataRegistry = metadataRegistry;
    }

    @Override
    public String createQuery(FilterCriteria criteria, String retentionPolicy) {
        final String tableName;
        final String itemName = criteria.getItemName();
        boolean hasCriteriaName = itemName != null;

        tableName = calculateTableName(itemName);

        Select select = select().column("\"" + COLUMN_VALUE_NAME_V1 + "\"::field")
                .column("\"" + TAG_ITEM_NAME + "\"::tag")
                .fromRaw(null, fullQualifiedTableName(retentionPolicy, tableName, hasCriteriaName));

        Where where = select.where();

        if (itemName != null && !tableName.equals(itemName)) {
            where = where.and(BuiltQuery.QueryBuilder.eq(TAG_ITEM_NAME, itemName));
        }

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

    private String calculateTableName(@Nullable String itemName) {
        if (itemName == null) {
            return "/.*/";
        }

        String name = itemName;

        name = InfluxDBMetadataUtils.calculateMeasurementNameFromMetadataIfPresent(metadataRegistry, name, itemName);

        if (configuration.isReplaceUnderscore()) {
            name = name.replace('_', '.');
        }

        return name;
    }

    private String fullQualifiedTableName(String retentionPolicy, String tableName, boolean escapeTableName) {
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(retentionPolicy).append('"');
        sb.append(".");
        if (escapeTableName) {
            sb.append('"').append(tableName).append('"');
        } else {
            sb.append(tableName);
        }
        return sb.toString();
    }
}
