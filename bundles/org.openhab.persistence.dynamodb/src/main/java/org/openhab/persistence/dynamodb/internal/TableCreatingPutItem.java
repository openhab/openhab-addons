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

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * PutItem request which creates table if needed.
 *
 * Designed such that competing PutItem requests should complete successfully, only one of them
 * 'winning the race' and creating the table.
 *
 *
 * PutItem
 * . |
 * . \ (ERR: ResourceNotFoundException) (1)
 * ....|
 * ....CreateTable
 * ....|.........\
 * .... \ (OK)....\ (ERR: ResourceInUseException) (2)
 * ......|..................|
 * ..... |..................|
 * ..... |...........Wait for table to become active
 * ..... |......................\
 * ..... |......................| (OK)
 * ..... |......................|
 * ..... |......................PutItem
 * ..... |
 * ..... |
 * ..... Wait for table to become active
 * ......|
 * .......\
 * ........| (OK)
 * ........|
 * ........\
 * ....... Configure TTL (no-op with legacy schema)
 * ..........|
 * ...........\ (OK)
 * ...........|
 * ...........PutItem
 *
 *
 * (1) Most likely table does not exist yet
 * (2) Raised when Table created by someone else
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class TableCreatingPutItem<T extends DynamoDBItem<?>> {
    private final Logger logger = LoggerFactory.getLogger(TableCreatingPutItem.class);

    private final DynamoDBPersistenceService service;
    private T dto;
    private DynamoDbAsyncTable<T> table;
    private CompletableFuture<Void> aggregateFuture = new CompletableFuture<Void>();
    private Instant start = Instant.now();
    private ExecutorService executor;
    private DynamoDbAsyncClient lowLevelClient;
    private DynamoDBConfig dbConfig;
    private DynamoDBTableNameResolver tableNameResolver;

    public TableCreatingPutItem(DynamoDBPersistenceService service, T dto, DynamoDbAsyncTable<T> table) {
        this.service = service;
        this.dto = dto;
        this.table = table;
        this.executor = this.service.getExecutor();
        DynamoDbAsyncClient localLowLevelClient = this.service.getLowLevelClient();
        DynamoDBConfig localDbConfig = this.service.getDbConfig();
        DynamoDBTableNameResolver localTableNameResolver = this.service.getTableNameResolver();
        if (localLowLevelClient == null || localDbConfig == null || localTableNameResolver == null) {
            throw new IllegalStateException("Service is not ready");
        }
        lowLevelClient = localLowLevelClient;
        dbConfig = localDbConfig;
        tableNameResolver = localTableNameResolver;
    }

    public CompletableFuture<Void> putItemAsync() {
        start = Instant.now();
        return internalPutItemAsync(false, true);
    }

    private CompletableFuture<Void> internalPutItemAsync(boolean createTable, boolean recursionAllowed) {
        if (createTable) {
            // Try again, first creating the table
            Instant tableCreationStart = Instant.now();
            table.createTable(CreateTableEnhancedRequest.builder()
                    .provisionedThroughput(
                            ProvisionedThroughput.builder().readCapacityUnits(dbConfig.getReadCapacityUnits())
                                    .writeCapacityUnits(dbConfig.getWriteCapacityUnits()).build())
                    .build())//
                    .whenCompleteAsync((resultTableCreation, exceptionTableCreation) -> {
                        if (exceptionTableCreation == null) {
                            logger.trace("PutItem: Table created in {} ms. Proceeding to TTL creation.",
                                    Duration.between(tableCreationStart, Instant.now()).toMillis());
                            //
                            // Table creation OK. Configure TTL
                            //
                            boolean legacy = tableNameResolver.getTableSchema() == ExpectedTableSchema.LEGACY;
                            waitForTableToBeActive().thenComposeAsync(_void -> {
                                if (legacy) {
                                    // We have legacy table schema. TTL configuration is skipped
                                    return CompletableFuture.completedFuture(null);
                                } else {
                                    // We have the new table schema -> configure TTL
                                    // for the newly created table
                                    return lowLevelClient.updateTimeToLive(req -> req
                                            .overrideConfiguration(this.service::overrideConfig)
                                            .tableName(table.tableName()).timeToLiveSpecification(spec -> spec
                                                    .attributeName(DynamoDBItem.ATTRIBUTE_NAME_EXPIRY).enabled(true)));
                                }
                            }, executor)
                                    //
                                    // Table is ready and TTL configured (possibly with error)
                                    //
                                    .whenCompleteAsync((resultTTL, exceptionTTL) -> {
                                        if (exceptionTTL == null) {
                                            //
                                            // TTL configuration OK, continue with PutItem
                                            //
                                            logger.trace("PutItem: TTL configured successfully");
                                            internalPutItemAsync(false, false);
                                        } else {
                                            //
                                            // TTL configuration failed, abort
                                            //
                                            logger.trace("PutItem: TTL configuration failed");
                                            Throwable exceptionTTLCause = exceptionTTL.getCause();
                                            aggregateFuture.completeExceptionally(
                                                    exceptionTTLCause == null ? exceptionTTL : exceptionTTLCause);
                                        }
                                    }, executor);
                        } else {
                            // Table creation failed. We give up and complete the aggregate
                            // future -- unless the error was ResourceInUseException, in which case wait for
                            // table to become active and try again
                            Throwable cause = exceptionTableCreation.getCause();
                            if (cause instanceof ResourceInUseException) {
                                logger.trace(
                                        "PutItem: table creation failed (will be retried) with {} {}. Perhaps tried to create table that already exists. Trying one more time without creating table.",
                                        cause.getClass().getSimpleName(), cause.getMessage());
                                // Wait table to be active, then retry PutItem
                                waitForTableToBeActive().whenCompleteAsync((_tableWaitResponse, tableWaitException) -> {
                                    if (tableWaitException != null) {
                                        // error when waiting for table to become active
                                        Throwable tableWaitExceptionCause = tableWaitException.getCause();
                                        logger.warn(
                                                "PutItem: failed (final) with {} {} when waiting to become active. Aborting.",
                                                tableWaitExceptionCause == null
                                                        ? tableWaitException.getClass().getSimpleName()
                                                        : tableWaitExceptionCause.getClass().getSimpleName(),
                                                tableWaitExceptionCause == null ? tableWaitException.getMessage()
                                                        : tableWaitExceptionCause.getMessage());
                                        aggregateFuture.completeExceptionally(
                                                tableWaitExceptionCause == null ? tableWaitException
                                                        : tableWaitExceptionCause);
                                    }
                                }, executor)
                                        // table wait OK, retry PutItem
                                        .thenRunAsync(() -> internalPutItemAsync(false, false), executor);
                            } else {
                                logger.warn("PutItem: failed (final) with {} {}. Aborting.",
                                        cause == null ? exceptionTableCreation.getClass().getSimpleName()
                                                : cause.getClass().getSimpleName(),
                                        cause == null ? exceptionTableCreation.getMessage() : cause.getMessage());
                                aggregateFuture.completeExceptionally(cause == null ? exceptionTableCreation : cause);
                            }
                        }
                    }, executor);

        } else {
            // First try, optimistically assuming that table exists
            table.putItem(dto).whenCompleteAsync((result, exception) -> {
                if (exception == null) {
                    logger.trace("PutItem: DTO {} was successfully written in {} ms.", dto,
                            Duration.between(start, Instant.now()).toMillis());
                    aggregateFuture.complete(result);
                } else {
                    // PutItem failed. We retry i failure was due to non-existing table. Retry is triggered by calling
                    // this method again with createTable=true)
                    // With other errors, we abort.
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
            }, executor);
        }
        return aggregateFuture;
    }

    private CompletableFuture<Void> waitForTableToBeActive() {
        return lowLevelClient.waiter()
                .waitUntilTableExists(
                        req -> req.tableName(table.tableName()).overrideConfiguration(this.service::overrideConfig))
                .thenAcceptAsync(tableWaitResponse -> {
                    // if waiter fails, the future is completed exceptionally (not entering this step)
                    ResponseOrException<DescribeTableResponse> responseOrException = tableWaitResponse.matched();
                    logger.trace("PutItem: Table wait completed sucessfully with {} attempts: {}",
                            tableWaitResponse.attemptsExecuted(), toString(responseOrException));
                }, executor);
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
