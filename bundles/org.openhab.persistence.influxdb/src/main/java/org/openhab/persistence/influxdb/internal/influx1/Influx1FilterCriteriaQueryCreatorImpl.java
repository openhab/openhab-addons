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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.influxdb.dto.Query;
import org.influxdb.querybuilder.Appender;
import org.influxdb.querybuilder.BuiltQuery;
import org.influxdb.querybuilder.Select;
import org.influxdb.querybuilder.Where;
import org.influxdb.querybuilder.clauses.SimpleClause;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.persistence.influxdb.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb.internal.InfluxDBMetadataUtils;
import org.openhab.persistence.influxdb.internal.InfluxDBVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link FilterCriteriaQueryCreator} for InfluxDB 1.0
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class Influx1FilterCriteriaQueryCreatorImpl implements FilterCriteriaQueryCreator {
    private Logger logger = LoggerFactory.getLogger(Influx1FilterCriteriaQueryCreatorImpl.class);

    private InfluxDBConfiguration configuration;
    private MetadataRegistry metadataRegistry;
    private ItemRegistry itemRegistry;

    public Influx1FilterCriteriaQueryCreatorImpl(InfluxDBConfiguration configuration, MetadataRegistry metadataRegistry,
            ItemRegistry itemRegistry) {
        this.configuration = configuration;
        this.metadataRegistry = metadataRegistry;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public String createQuery(FilterCriteria criteria, String retentionPolicy) {
        final String tableName;
        final String itemName = criteria.getItemName();
        boolean hasCriteriaName = itemName != null;
        Optional<Item> item = Optional.ofNullable(itemName).map(this::safeGetItem);

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
            var desiredUnit = item.filter(i -> i instanceof NumberItem).map(i -> ((NumberItem) i).getUnit());
            where = where.and(new SimpleClause(COLUMN_VALUE_NAME_V1,
                    getOperationSymbol(criteria.getOperator(), InfluxDBVersion.V1),
                    stateToObject(criteria.getState(), desiredUnit.orElse(null))));
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
        Appender.appendName(retentionPolicy, sb);
        sb.append(".");
        if (escapeTableName) {
            Appender.appendName(tableName, sb);
        } else {
            sb.append(tableName);
        }
        return sb.toString();
    }

    private @Nullable Item safeGetItem(String itemName) {
        try {
            return itemRegistry.getItem(itemName);
        } catch (ItemNotFoundException e) {
            logger.warn("No item found with name {}", itemName, e);
            return null;
        }
    }
}
