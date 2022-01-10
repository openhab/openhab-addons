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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

/**
 * The DynamoDBTableNameResolver resolves DynamoDB table name for a given item.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class DynamoDBTableNameResolver {
    private final Logger logger = LoggerFactory.getLogger(DynamoDBTableNameResolver.class);

    private final String tablePrefix;
    private ExpectedTableSchema tableRevision;
    private String table;

    public DynamoDBTableNameResolver(ExpectedTableSchema tableRevision, String table, String tablePrefix) {
        this.tableRevision = tableRevision;
        this.table = table;
        this.tablePrefix = tablePrefix;
        switch (tableRevision) {
            case NEW:
                if (table.isBlank()) {
                    throw new IllegalArgumentException("table should be specified with NEW schema");
                }
                break;
            case MAYBE_LEGACY:
                if (table.isBlank()) {
                    throw new IllegalArgumentException("table should be specified with MAYBE_LEGACY schema");
                }
                // fall-through
            case LEGACY:
                if (tablePrefix.isBlank()) {
                    throw new IllegalArgumentException("tablePrefix should be specified with LEGACY schema");
                }
                break;
            default:
                throw new IllegalArgumentException("Bug");
        }
    }

    /**
     * Create instance of DynamoDBTableNameResolver using given DynamoDBItem. Item's class is used to determine the
     * table name.
     *
     *
     * @param item dto to use to determine table name
     * @return table name
     * @throws IllegalStateException when table schmea is not determined
     */
    public String fromItem(DynamoDBItem<?> item) {
        if (!isFullyResolved()) {
            throw new IllegalStateException();
        }
        switch (tableRevision) {
            case NEW:
                return getTableNameAccordingToNewSchema();
            case LEGACY:
                return getTableNameAccordingToLegacySchema(item);
            default:
                throw new IllegalArgumentException("Bug");
        }
    }

    /**
     * Get table name according to new schema. This instance does not have to have fully determined schema
     *
     * @return table name
     */
    private String getTableNameAccordingToNewSchema() {
        return table;
    }

    /**
     * Get table name according to legacy schema. This instance does not have to have fully determined schema
     *
     * @param item dto to use to determine table name
     * @return table name
     */
    private String getTableNameAccordingToLegacySchema(DynamoDBItem<?> item) {
        // Use the visitor pattern to deduce the table name
        return item.accept(new DynamoDBItemVisitor<String>() {

            @Override
            public String visit(DynamoDBBigDecimalItem dynamoBigDecimalItem) {
                return tablePrefix + "bigdecimal";
            }

            @Override
            public String visit(DynamoDBStringItem dynamoStringItem) {
                return tablePrefix + "string";
            }
        });
    }

    /**
     * Construct DynamoDBTableNameResolver corresponding to DynamoDBItem class
     *
     * @param clazz
     * @return
     */
    public String fromClass(Class<? extends DynamoDBItem<?>> clazz) {
        DynamoDBItem<?> dummy;
        try {
            // Construct new instance of this class (assuming presense no-argument constructor)
            // in order to re-use fromItem(DynamoDBItem) constructor
            dummy = clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Could not find suitable constructor for class %s", clazz));
        }
        return this.fromItem(dummy);
    }

    /**
     * Whether we have determined the schema and table names to use
     *
     * @return true when schema revision is clearly specified
     */
    public boolean isFullyResolved() {
        return tableRevision.isFullyResolved();
    }

    public CompletableFuture<Boolean> resolveSchema(DynamoDbAsyncClient lowLevelClient,
            Consumer<DescribeTableRequest.Builder> describeTableRequestMutator, ExecutorService executor) {
        CompletableFuture<Boolean> resolved = new CompletableFuture<>();
        if (isFullyResolved()) {
            resolved.complete(true);
        }

        String numberTableLegacy = getTableNameAccordingToLegacySchema(new DynamoDBBigDecimalItem());
        String stringTableLegacy = getTableNameAccordingToLegacySchema(new DynamoDBStringItem());
        CompletableFuture<@Nullable Boolean> tableSchemaNumbers = tableIsPresent(lowLevelClient,
                describeTableRequestMutator, executor, numberTableLegacy);
        CompletableFuture<@Nullable Boolean> tableSchemaStrings = tableIsPresent(lowLevelClient,
                describeTableRequestMutator, executor, stringTableLegacy);

        tableSchemaNumbers.thenAcceptBothAsync(tableSchemaStrings, (table1Present, table2Present) -> {
            if (table1Present != null && table2Present != null) {
                // Since the Booleans are not null, we know for sure whether table is present or not

                // If old tables do not exist, we default to new table layout/schema
                tableRevision = (!table1Present && !table2Present) ? ExpectedTableSchema.NEW
                        : ExpectedTableSchema.LEGACY;
            }
            resolved.complete(table1Present != null && table2Present != null);
        }, executor).exceptionally(e -> {
            // should not happen as individual futures have exceptions handled
            logger.error("Unexpected error. BUG", e);
            resolved.complete(false);
            return null;
        });

        return resolved;
    }

    /**
     *
     * @return whether table exists, or null when state is unknown
     */
    private CompletableFuture<@Nullable Boolean> tableIsPresent(DynamoDbAsyncClient lowLevelClient,
            Consumer<DescribeTableRequest.Builder> describeTableRequestMutator, ExecutorService executor,
            String tableName) {
        CompletableFuture<@Nullable Boolean> tableSchema = new CompletableFuture<>();
        lowLevelClient.describeTable(b -> b.tableName(tableName).applyMutation(describeTableRequestMutator))
                .thenApplyAsync(r -> r.table().tableStatus(), executor)
                .thenApplyAsync(tableStatus -> tableIsBeingRemoved(tableStatus) ? false : true)
                .thenAccept(r -> tableSchema.complete(r)).exceptionally(exception -> {
                    Throwable cause = exception.getCause();
                    if (cause instanceof ResourceNotFoundException) {
                        tableSchema.complete(false);
                    } else {
                        logger.warn(
                                "Could not verify whether table {} is present: {} {}. Cannot determine table schema.",
                                tableName,
                                cause == null ? exception.getClass().getSimpleName() : cause.getClass().getSimpleName(),
                                cause == null ? exception.getMessage() : cause.getMessage());
                        // Other error, we could not resolve schema...
                        tableSchema.complete(null);
                    }
                    return null;
                });
        return tableSchema;
    }

    private boolean tableIsBeingRemoved(TableStatus tableStatus) {
        return (tableStatus == TableStatus.ARCHIVING || tableStatus == TableStatus.DELETING
                || tableStatus == TableStatus.ARCHIVED);
    }

    public ExpectedTableSchema getTableSchema() {
        return tableRevision;
    }
}
