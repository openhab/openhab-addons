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
package org.openhab.persistence.dynamodb.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * TODO: documentation
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
class TableCreatingPutItem {
    private final Logger logger = LoggerFactory.getLogger(DynamoDBPersistenceService.class);

    private final DynamoDBPersistenceService service;
    private DynamoDBItem<?> dto;
    @SuppressWarnings("rawtypes")
    private DynamoDbAsyncTable<DynamoDBItem> table;
    private CompletableFuture<Void> aggregateFuture = new CompletableFuture<Void>();
    private Instant start = Instant.now();

    public TableCreatingPutItem(DynamoDBPersistenceService service, DynamoDBItem<?> dto,
            @SuppressWarnings("rawtypes") DynamoDbAsyncTable<DynamoDBItem> table) {
        this.service = service;
        this.dto = dto;
        this.table = table;
    }

    public CompletableFuture<Void> putItemAsync() {
        start = Instant.now();
        return internalPutItemAsync(false, true);
    }

    private CompletableFuture<Void> internalPutItemAsync(boolean createTable, boolean recursionAllowed) {
        if (createTable) {
            // Try again, first create the table, calculate new deadline, wait for table to become active (before
            // new deadline), and finally retry PutItem
            Instant tableCreationStart = Instant.now();
            table.createTable(CreateTableEnhancedRequest.builder()
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(this.service.dbConfig.getReadCapacityUnits())
                            .writeCapacityUnits(this.service.dbConfig.getWriteCapacityUnits()).build())
                    .build()).thenComposeAsync(_void -> waitForTableToBeActive(), this.service.executor)
                    .thenComposeAsync(_void -> {
                        boolean legacy = this.service.tableNameResolver.getTableSchema() == ExpectedTableSchema.LEGACY;
                        if (legacy) {
                            return CompletableFuture.completedFuture(null);
                        } else {
                            // new table schema, we also configure TTL for the table
                            return this.service.lowLevelClient
                                    .updateTimeToLive(req -> req.overrideConfiguration(this.service::overrideConfig)
                                            .tableName(table.tableName()).timeToLiveSpecification(spec -> spec
                                                    .attributeName(DynamoDBItem.ATTRIBUTE_NAME_EXPIRY).enabled(true)));
                        }
                    }, this.service.executor).thenComposeAsync(_void -> table.putItem(dto), this.service.executor)
                    .whenCompleteAsync((result, exception) -> {
                        // PutItem failed even after creating the table. We give up and complete the aggregate
                        // future
                        if (exception == null) {
                            Instant now = Instant.now();
                            logger.trace(
                                    "PutItem: DTO {} was successfully written in {} ms. Table was created in {} ms.",
                                    dto, Duration.between(start, now).toMillis(),
                                    Duration.between(tableCreationStart, now).toMillis());
                            aggregateFuture.complete(result);
                        } else {
                            Throwable cause = exception.getCause();
                            if (cause instanceof ResourceInUseException) {
                                logger.trace(
                                        "PutItem: table creation failed (will be retried) with {} {}. Perhaps tried to create table that already exists. Trying one more time without creating table.",
                                        cause.getClass().getSimpleName(), cause.getMessage());
                                // Wait table to be active, then retry PutItem
                                waitForTableToBeActive().whenComplete((r, e) -> {
                                    if (e != null) {
                                        Throwable c = e.getCause();
                                        logger.warn(
                                                "PutItem: failed (final) with {} {} when waiting to become active. Aborting.",
                                                c == null ? exception.getClass().getSimpleName()
                                                        : c.getClass().getSimpleName(),
                                                c == null ? exception.getMessage() : c.getMessage());
                                    }
                                }).thenRunAsync(() -> internalPutItemAsync(false, false), this.service.executor);
                            } else {
                                logger.warn("PutItem: failed (final) with {} {}. Aborting.",
                                        cause == null ? exception.getClass().getSimpleName()
                                                : cause.getClass().getSimpleName(),
                                        cause == null ? exception.getMessage() : cause.getMessage());
                                aggregateFuture.completeExceptionally(exception);
                            }
                        }
                    }, this.service.executor);
        } else {
            // First try, optimistically assuming that table exists
            table.putItem(dto).whenCompleteAsync((result, exception) -> {
                if (exception == null) {
                    logger.trace("PutItem: DTO {} was successfully written in {} ms. There was no need to create table",
                            dto, Duration.between(start, Instant.now()).toMillis());
                    aggregateFuture.complete(result);
                } else {
                    // PutItem failed. We retry (calling this method again with parameter true) if it failed due to
                    // table not existing, otherwise we abort.
                    if (!(exception instanceof CompletionException)) {
                        logger.error("PutItem: Expecting only CompletionException, got {} {}. BUG",
                                exception.getClass().getName(), exception.getMessage());
                        aggregateFuture.completeExceptionally(new IllegalStateException("unexpected exception"));
                    }
                    Throwable cause = exception.getCause();
                    if (cause instanceof ResourceNotFoundException && recursionAllowed) {
                        logger.trace(
                                "PutItem: Table '{}' was not present. Retrying, this time creating the table first",
                                table.tableName());
                        internalPutItemAsync(true, true);
                    } else {
                        logger.warn("PutItem: failed (final) with {} {}. Aborting.",
                                cause == null ? exception.getClass().getSimpleName() : cause.getClass().getSimpleName(),
                                cause == null ? exception.getMessage() : cause.getMessage());
                        aggregateFuture.completeExceptionally(cause == null ? exception : cause);
                    }
                }
            }, this.service.executor);
        }
        return aggregateFuture;
    }

    private CompletableFuture<Void> waitForTableToBeActive() {
        return this.service.lowLevelClient.waiter()
                .waitUntilTableExists(
                        req -> req.tableName(table.tableName()).overrideConfiguration(this.service::overrideConfig))
                .thenAcceptAsync(tableWaitResponse -> {
                    // if waiter fails, the future is completed exceptionally (not entering this step)
                    ResponseOrException<DescribeTableResponse> responseOrException = tableWaitResponse.matched();
                    logger.trace("PutItem: Table wait completed sucessfully with {} attempts: {}",
                            tableWaitResponse.attemptsExecuted(), toString(responseOrException));
                }, this.service.executor);
    }

    private String toString(ResponseOrException<?> responseOrException) {
        if (responseOrException.response().isPresent()) {
            return String.format("response=%s", responseOrException.response().get());
        } else if (responseOrException.exception().isPresent()) {
            Throwable exception = responseOrException.exception().get();
            return String.format("exception=%s %s", exception.getClass().getSimpleName(), exception.getMessage());
        } else {
            return String.format("<N/A>");
        }
    }
}
