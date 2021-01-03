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
package org.openhab.persistence.dynamodb.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.FilterCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class DynamoDBTableNameResolverTest extends BaseIntegrationTest {

    private final Logger logger = LoggerFactory.getLogger(DynamoDBTableNameResolverTest.class);
    public static final boolean LEGACY_MODE = false; // not relevant for these tests but required by BaseIntegrationTest

    @Test
    public void testLegacyWithDynamoDBBigDecimalItem() {
        assertEquals("integration-tests-bigdecimal",
                new DynamoDBTableNameResolver(ExpectedTableSchema.LEGACY, "", "integration-tests-")
                        .fromItem(new DynamoDBBigDecimalItem()));
    }

    @Test
    public void testLegacyWithDynamoDBStringItem() {
        assertEquals("integration-tests-string",
                new DynamoDBTableNameResolver(ExpectedTableSchema.LEGACY, "", "integration-tests-")
                        .fromItem(new DynamoDBStringItem()));
    }

    @Test
    public void testWithDynamoDBBigDecimalItem() {
        assertEquals("integration-tests",
                new DynamoDBTableNameResolver(ExpectedTableSchema.NEW, "integration-tests", "")
                        .fromItem(new DynamoDBBigDecimalItem()));
    }

    @Test
    public void testWithDynamoDBStringItem() {
        assertEquals("integration-tests",
                new DynamoDBTableNameResolver(ExpectedTableSchema.NEW, "integration-tests", "")
                        .fromItem(new DynamoDBStringItem()));
    }

    @Test
    public void testBothLegacyAndNewParametersNeedToBeSpecifiedWithUnclearTableSchema() {
        assertThrows(IllegalArgumentException.class, () -> {
            assertEquals("integration-tests",
                    new DynamoDBTableNameResolver(ExpectedTableSchema.MAYBE_LEGACY, "integration-tests", "")
                            .fromItem(new DynamoDBStringItem()));
        });
        assertThrows(IllegalArgumentException.class, () -> {
            assertEquals("integration-tests", new DynamoDBTableNameResolver(ExpectedTableSchema.MAYBE_LEGACY, "", "bb")
                    .fromItem(new DynamoDBStringItem()));
        });
    }

    @Test
    public void testResolveLegacyTablesPresent() throws InterruptedException {
        // Run test only with embedded server. Otherwise there is risk of writing data using default table names
        assumeTrue(embeddedServer != null);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        DynamoDBPersistenceService maybeLegacyService = null;
        final DynamoDBPersistenceService legacyService = newService(true, true, null, DynamoDBConfig.DEFAULT_TABLE_NAME,
                DynamoDBConfig.DEFAULT_TABLE_PREFIX);
        assertEquals(ExpectedTableSchema.LEGACY, legacyService.tableNameResolver.getTableSchema());

        NumberItem item = (@NonNull NumberItem) ITEMS.get("number");
        final FilterCriteria criteria = new FilterCriteria();
        criteria.setItemName(item.getName());

        try {

            // Old tables do not exit --> resolves to new schema
            assertEquals(ExpectedTableSchema.NEW, resolveMaybeLegacy(legacyService, executor));

            // Write data using legacy tables
            item.setState(new DecimalType(0));
            legacyService.store(item);

            // Since table exist now, DynamoDBTableNameResolver should resolve
            waitForAssert(() -> {
                // Old tables are now there --> should resolve to old schema
                assertEquals(ExpectedTableSchema.LEGACY, resolveMaybeLegacy(legacyService, executor));
            });

            // Create 2 new services, with unknown schemas (MAYBE_LEGACY), pointing to same database
            maybeLegacyService = newService(null, false, legacyService.endpointOverride, null, null);
            assertEquals(ExpectedTableSchema.MAYBE_LEGACY, maybeLegacyService.tableNameResolver.getTableSchema());
            assertEquals(legacyService.endpointOverride, maybeLegacyService.endpointOverride);

            // maybeLegacyService2 still does not know the schema
            assertEquals(ExpectedTableSchema.MAYBE_LEGACY, maybeLegacyService.tableNameResolver.getTableSchema());
            // ... but it will be resolved automatically on query
            final DynamoDBPersistenceService maybeLegacyService2Final = maybeLegacyService;
            waitForAssert(() -> {
                assertEquals(1, Lists.newArrayList(maybeLegacyService2Final.query(criteria)).size());
                // also the schema gets resolved
                assertEquals(ExpectedTableSchema.LEGACY, maybeLegacyService2Final.tableNameResolver.getTableSchema());
            });

        } finally {
            executor.shutdown();
            if (maybeLegacyService != null) {
                maybeLegacyService.deactivate();
            }
            legacyService.deactivate();
        }
    }

    /**
     *
     * @param legacyService service that has the client to use
     * @param executor
     * @return
     */
    private ExpectedTableSchema resolveMaybeLegacy(DynamoDBPersistenceService legacyService, ExecutorService executor) {
        DynamoDBTableNameResolver resolver = new DynamoDBTableNameResolver(ExpectedTableSchema.MAYBE_LEGACY,
                DynamoDBConfig.DEFAULT_TABLE_NAME, DynamoDBConfig.DEFAULT_TABLE_PREFIX);
        assertFalse(resolver.isFullyResolved());
        try {
            boolean resolved = resolver.resolveSchema(legacyService.getLowLevelClient(),
                    b -> b.overrideConfiguration(legacyService::overrideConfig), executor).get();
            assertTrue(resolved);
            return resolver.getTableSchema();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
            throw new IllegalStateException(); // Make compiler happy
        }
    }
}
