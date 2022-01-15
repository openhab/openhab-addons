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
package org.openhab.persistence.dynamodb.internal;

import java.lang.reflect.InvocationTargetException;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.persistence.FilterCriteria.Ordering;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Expression.Builder;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Utility class
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class DynamoDBQueryUtils {
    /**
     * Construct dynamodb query from filter
     *
     * @param dtoClass dto class
     * @param expectedTableSchema table schema to query against
     * @param item item corresponding to filter
     * @param filter filter for the query
     * @return DynamoDBQueryExpression corresponding to the given FilterCriteria
     * @throws IllegalArgumentException when schema is not fully resolved
     */
    public static QueryEnhancedRequest createQueryExpression(Class<? extends DynamoDBItem<?>> dtoClass,
            ExpectedTableSchema expectedTableSchema, Item item, FilterCriteria filter) {
        if (!expectedTableSchema.isFullyResolved()) {
            throw new IllegalArgumentException("Schema not resolved");
        }
        QueryEnhancedRequest.Builder queryBuilder = QueryEnhancedRequest.builder()
                .scanIndexForward(filter.getOrdering() == Ordering.ASCENDING);
        addFilterbyItemAndTimeFilter(queryBuilder, expectedTableSchema, filter.getItemName(), filter);
        addStateFilter(queryBuilder, expectedTableSchema, item, dtoClass, filter);
        addProjection(dtoClass, expectedTableSchema, queryBuilder);
        return queryBuilder.build();
    }

    /**
     * Add projection for key parameters only, not expire date
     */
    private static void addProjection(Class<? extends DynamoDBItem<?>> dtoClass,
            ExpectedTableSchema expectedTableSchema, QueryEnhancedRequest.Builder queryBuilder) {
        boolean legacy = expectedTableSchema == ExpectedTableSchema.LEGACY;
        if (legacy) {
            queryBuilder.attributesToProject(DynamoDBItem.ATTRIBUTE_NAME_ITEMNAME_LEGACY,
                    DynamoDBItem.ATTRIBUTE_NAME_TIMEUTC_LEGACY, DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_LEGACY);
        } else {
            acceptAsEmptyDTO(dtoClass, new DynamoDBItemVisitor<@Nullable Void>() {
                @Override
                public @Nullable Void visit(DynamoDBStringItem dynamoStringItem) {
                    queryBuilder.attributesToProject(DynamoDBItem.ATTRIBUTE_NAME_ITEMNAME,
                            DynamoDBItem.ATTRIBUTE_NAME_TIMEUTC, DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_STRING);
                    return null;
                }

                @Override
                public @Nullable Void visit(DynamoDBBigDecimalItem dynamoBigDecimalItem) {
                    queryBuilder.attributesToProject(DynamoDBItem.ATTRIBUTE_NAME_ITEMNAME,
                            DynamoDBItem.ATTRIBUTE_NAME_TIMEUTC, DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_NUMBER);
                    return null;
                }
            });
        }
    }

    private static void addStateFilter(QueryEnhancedRequest.Builder queryBuilder,
            ExpectedTableSchema expectedTableSchema, Item item, Class<? extends DynamoDBItem<?>> dtoClass,
            FilterCriteria filter) {
        final Expression expression;
        Builder itemStateTypeExpressionBuilder = Expression.builder()
                .expression(String.format("attribute_exists(#attr)"));
        boolean legacy = expectedTableSchema == ExpectedTableSchema.LEGACY;
        acceptAsEmptyDTO(dtoClass, new DynamoDBItemVisitor<@Nullable Void>() {
            @Override
            public @Nullable Void visit(DynamoDBStringItem dynamoStringItem) {
                itemStateTypeExpressionBuilder.putExpressionName("#attr",
                        legacy ? DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_LEGACY
                                : DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_STRING);
                return null;
            }

            @Override
            public @Nullable Void visit(DynamoDBBigDecimalItem dynamoBigDecimalItem) {
                itemStateTypeExpressionBuilder.putExpressionName("#attr",
                        legacy ? DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_LEGACY
                                : DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_NUMBER);
                return null;
            }
        });
        if (filter.getOperator() != null && filter.getState() != null) {
            // Convert filter's state to DynamoDBItem in order get suitable string representation for the state
            Expression.Builder stateFilterExpressionBuilder = Expression.builder()
                    .expression(String.format("#attr %s :value", operatorAsString(filter.getOperator())));
            // Following will throw IllegalArgumentException when filter state is not compatible with
            // item. This is acceptable.
            GenericItem stateToFind = DynamoDBPersistenceService.copyItem(item, item, filter.getItemName(),
                    filter.getState());
            acceptAsDTO(stateToFind, legacy, new DynamoDBItemVisitor<@Nullable Void>() {
                @Override
                public @Nullable Void visit(DynamoDBStringItem serialized) {
                    stateFilterExpressionBuilder.putExpressionName("#attr",
                            legacy ? DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_LEGACY
                                    : DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_STRING);
                    stateFilterExpressionBuilder.putExpressionValue(":value",
                            AttributeValue.builder().s(serialized.getState()).build());
                    return null;
                }

                @SuppressWarnings("null")
                @Override
                public @Nullable Void visit(DynamoDBBigDecimalItem serialized) {
                    stateFilterExpressionBuilder.putExpressionName("#attr",
                            legacy ? DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_LEGACY
                                    : DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_NUMBER);
                    stateFilterExpressionBuilder.putExpressionValue(":value",
                            AttributeValue.builder().n(serialized.getState().toPlainString()).build());
                    return null;
                }
            });
            expression = Expression.join(stateFilterExpressionBuilder.build(), itemStateTypeExpressionBuilder.build(),
                    "AND");

            queryBuilder.filterExpression(expression);
        } else {
            expression = itemStateTypeExpressionBuilder.build();
        }
        queryBuilder.filterExpression(expression);
    }

    private static void addFilterbyItemAndTimeFilter(QueryEnhancedRequest.Builder queryBuilder,
            ExpectedTableSchema expectedTableSchema, String partition, final FilterCriteria filter) {
        boolean hasBegin = filter.getBeginDate() != null;
        boolean hasEnd = filter.getEndDate() != null;
        boolean legacy = expectedTableSchema == ExpectedTableSchema.LEGACY;

        AttributeConverter<ZonedDateTime> timeConverter = AbstractDynamoDBItem.getTimestampConverter(legacy);

        if (!hasBegin && !hasEnd) {
            // No need to place time filter filter but we do filter by partition
            queryBuilder.queryConditional(QueryConditional.keyEqualTo(k -> k.partitionValue(partition)));
        } else if (hasBegin && !hasEnd) {
            queryBuilder.queryConditional(QueryConditional.sortGreaterThan(
                    k -> k.partitionValue(partition).sortValue(timeConverter.transformFrom(filter.getBeginDate()))));
        } else if (!hasBegin && hasEnd) {
            queryBuilder.queryConditional(QueryConditional.sortLessThan(
                    k -> k.partitionValue(partition).sortValue(timeConverter.transformFrom(filter.getEndDate()))));
        } else {
            assert hasBegin && hasEnd; // invariant
            queryBuilder.queryConditional(QueryConditional.sortBetween(
                    k -> k.partitionValue(partition).sortValue(timeConverter.transformFrom(filter.getBeginDate())),
                    k -> k.partitionValue(partition).sortValue(timeConverter.transformFrom(filter.getEndDate()))));
        }
    }

    /**
     * Convert op to string suitable for dynamodb filter expression
     *
     * @param op
     * @return string representation corresponding to the given the Operator
     */
    private static String operatorAsString(Operator op) {
        switch (op) {
            case EQ:
                return "=";
            case NEQ:
                return "<>";
            case LT:
                return "<";
            case LTE:
                return "<=";
            case GT:
                return ">";
            case GTE:
                return ">=";

            default:
                throw new IllegalStateException("Unknown operator " + op);
        }
    }

    private static <T> void acceptAsDTO(Item item, boolean legacy, DynamoDBItemVisitor<T> visitor) {
        ZonedDateTime dummyTimestamp = ZonedDateTime.now();
        if (legacy) {
            AbstractDynamoDBItem.fromStateLegacy(item, dummyTimestamp).accept(visitor);
        } else {
            AbstractDynamoDBItem.fromStateNew(item, dummyTimestamp, null).accept(visitor);
        }
    }

    private static <T> void acceptAsEmptyDTO(Class<? extends DynamoDBItem<?>> dtoClass,
            DynamoDBItemVisitor<T> visitor) {
        try {
            dtoClass.getDeclaredConstructor().newInstance().accept(visitor);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
