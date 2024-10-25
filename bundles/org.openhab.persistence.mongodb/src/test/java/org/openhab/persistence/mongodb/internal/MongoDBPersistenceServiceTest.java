/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.mongodb.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.HistoricItem;
import org.osgi.framework.BundleContext;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * This is the implementation of the test for MongoDB {@link PersistenceService}.
 *
 * @author René Ulbricht - Initial contribution
 */
public class MongoDBPersistenceServiceTest {

    /**
     * Tests the activate method of MongoDBPersistenceService.
     *
     * This test checks if the activate method correctly logs the MongoDB URL, database, and collection.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testActivate(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);

            // Set up logger
            ListAppender<ILoggingEvent> listAppender = DataCreationHelper.setupLogger(MongoDBPersistenceService.class,
                    Level.DEBUG);

            // Execution
            setupResult.service.activate(setupResult.bundleContext, setupResult.config);

            // Verification
            List<ILoggingEvent> logsList = listAppender.list;
            VerificationHelper.verifyLogMessage(logsList.get(0), "MongoDB URL " + dbContainer.getConnectionString(),
                    Level.DEBUG);
            VerificationHelper.verifyLogMessage(logsList.get(1), "MongoDB database " + setupResult.dbname, Level.DEBUG);
            VerificationHelper.verifyLogMessage(logsList.get(2), "MongoDB collection testCollection", Level.DEBUG);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the deactivate method of MongoDBPersistenceService.
     *
     * This test checks if the deactivate method correctly logs a message when the MongoDB persistence bundle is
     * stopping.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testDeactivate(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);

            setupResult.service.activate(setupResult.bundleContext, setupResult.config);

            // Set up logger
            ListAppender<ILoggingEvent> listAppender = DataCreationHelper.setupLogger(MongoDBPersistenceService.class,
                    Level.DEBUG);

            // Execution
            setupResult.service.deactivate(1);

            // Verification
            List<ILoggingEvent> logsList = listAppender.list;
            VerificationHelper.verifyLogMessage(logsList.get(0),
                    "MongoDB persistence bundle stopping. Disconnecting from database.", Level.DEBUG);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the getId method of MongoDBPersistenceService.
     *
     * This test checks if the getId method correctly returns the ID of the MongoDBPersistenceService, which should be
     * "mongodb".
     */
    @Test
    public void testGetId() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB(null, dbContainer);
            MongoDBPersistenceService service = setupResult.service;

            // Execution
            String id = service.getId();

            // Verification
            assertEquals("mongodb", id);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the getLabel method of MongoDBPersistenceService.
     *
     * This test checks if the getLabel method correctly returns the label of the MongoDBPersistenceService, which
     * should be "MongoDB".
     */
    @Test
    public void testGetLabel() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB(null, dbContainer);
            MongoDBPersistenceService service = setupResult.service;

            // Execution
            String label = service.getLabel(null);

            // Verification
            assertEquals("MongoDB", label);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store method of MongoDBPersistenceService with a NumberItem.
     *
     * This test checks if the store method correctly stores a NumberItem in the MongoDB database.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testStoreNumber(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            NumberItem item = DataCreationHelper.createNumberItem("TestItem", 10.1);

            // Execution
            service.store(item, null);

            // Verification
            MongoCollection<Document> collection = database.getCollection("testCollection");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            VerificationHelper.verifyDocument(insertedDocument, "TestItem", 10.1);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store method of MongoDBPersistenceService with a StringItem.
     *
     * This test checks if the store method correctly stores a StringItem in the MongoDB database.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testStoreString(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            StringItem item = DataCreationHelper.createStringItem("TestItem", "TestValue");

            // Execution
            service.store(item, null);

            // Verification
            MongoCollection<Document> collection = database.getCollection("testCollection");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            VerificationHelper.verifyDocument(insertedDocument, "TestItem", "TestValue");
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store method of MongoDBPersistenceService with multiple items in a single collection.
     *
     * This test checks if the store method correctly stores multiple items in the same MongoDB collection.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testStoreSingleCollection(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            StringItem strItem1 = DataCreationHelper.createStringItem("TestItem", "TestValue");
            StringItem strItem2 = DataCreationHelper.createStringItem("SecondTestItem", "SecondTestValue");

            // Execution
            service.store(strItem1, null);
            service.store(strItem2, null);

            // Verification
            MongoCollection<Document> collection = database.getCollection("testCollection");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(2, documents.size()); // Assert that there are two documents

            Document insertedDocument1 = documents.get(0); // Get the first document
            VerificationHelper.verifyDocument(insertedDocument1, "TestItem", "TestValue");

            Document insertedDocument2 = documents.get(1); // Get the second document
            VerificationHelper.verifyDocument(insertedDocument2, "SecondTestItem", "SecondTestValue");
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store method of MongoDBPersistenceService with multiple items.
     *
     * This test checks if the store method correctly stores multiple items in the same MongoDB collection.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testStoreMultipleItemsSingleCollection(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            StringItem strItem1 = DataCreationHelper.createStringItem("TestItem1", "TestValue1");
            StringItem strItem2 = DataCreationHelper.createStringItem("TestItem2", "TestValue2");

            // Execution
            service.store(strItem1, null);
            service.store(strItem2, null);

            // Verification
            MongoCollection<Document> collection = database.getCollection("testCollection");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(2, documents.size()); // Assert that there are two documents

            Document insertedDocument1 = documents.get(0); // Get the first document
            VerificationHelper.verifyDocument(insertedDocument1, "TestItem1", "TestValue1");

            Document insertedDocument2 = documents.get(1); // Get the second document
            VerificationHelper.verifyDocument(insertedDocument2, "TestItem2", "TestValue2");
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store method of MongoDBPersistenceService with a StringItem and an alias.
     *
     * This test checks if the store method correctly stores a StringItem with an alias in the MongoDB database.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testStoreStringWithAlias(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            StringItem item = DataCreationHelper.createStringItem("TestItem", "TestValue");

            // Execution
            service.store(item, "AliasName");

            // Verification
            MongoCollection<Document> collection = database.getCollection("testCollection");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            VerificationHelper.verifyDocumentWithAlias(insertedDocument, "AliasName", "TestItem", "TestValue");
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the query method of MongoDBPersistenceService with NumberItems in a single collection.
     *
     * This test checks if the query method correctly retrieves NumberItems from a single MongoDB collection.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testQueryNumberItemsInOneCollection(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;

            // Add items to the ItemRegistry
            NumberItem itemReg1 = DataCreationHelper.createNumberItem("TestItem", 0);
            NumberItem itemReg2 = DataCreationHelper.createNumberItem("TestItem2", 0);
            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem")).thenReturn(itemReg1);
                Mockito.when(setupResult.itemRegistry.getItem("TestItem2")).thenReturn(itemReg2);
            } catch (ItemNotFoundException e) {
            }

            service.activate(setupResult.bundleContext, setupResult.config);

            // Store some items
            for (int i = 0; i < 10; i++) {
                NumberItem item1 = DataCreationHelper.createNumberItem("TestItem", i);
                NumberItem item2 = DataCreationHelper.createNumberItem("TestItem2", i * 2);
                service.store(item1, null);
                service.store(item2, null);
            }

            // Execution
            FilterCriteria filter1 = DataCreationHelper.createFilterCriteria("TestItem");
            Iterable<HistoricItem> result1 = service.query(filter1);

            FilterCriteria filter2 = DataCreationHelper.createFilterCriteria("TestItem2");
            Iterable<HistoricItem> result2 = service.query(filter2);

            // Verification
            VerificationHelper.verifyQueryResult(result1, 0, 1, 10);
            VerificationHelper.verifyQueryResult(result2, 0, 2, 10);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the query method of MongoDBPersistenceService with NumberItems in multiple collections.
     *
     * This test checks if the query method correctly retrieves NumberItems from multiple MongoDB collections.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testQueryNumberItemsInMultipleCollections(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB(null, dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            BundleContext bundleContext = setupResult.bundleContext;
            Map<String, Object> config = setupResult.config;

            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem", 0));
                Mockito.when(setupResult.itemRegistry.getItem("TestItem2"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem2", 0));
            } catch (ItemNotFoundException e) {
            }

            service.activate(bundleContext, config);

            // Store some items
            for (int i = 0; i < 10; i++) {
                NumberItem item1 = DataCreationHelper.createNumberItem("TestItem", i);
                NumberItem item2 = DataCreationHelper.createNumberItem("TestItem2", i * 2);
                service.store(item1, null);
                service.store(item2, null);
            }

            // Execution
            FilterCriteria filter1 = DataCreationHelper.createFilterCriteria("TestItem");
            Iterable<HistoricItem> result1 = service.query(filter1);

            FilterCriteria filter2 = DataCreationHelper.createFilterCriteria("TestItem2");
            Iterable<HistoricItem> result2 = service.query(filter2);

            // Verification
            VerificationHelper.verifyQueryResult(result1, 0, 1, 10);
            VerificationHelper.verifyQueryResult(result2, 0, 2, 10);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the query method of MongoDBPersistenceService with NumberItems in a single collection and a time range.
     *
     * This test checks if the query method correctly retrieves NumberItems from a single MongoDB collection within a
     * specified time range.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testQueryNumberItemsInOneCollectionTimeRange(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem", 0));
                Mockito.when(setupResult.itemRegistry.getItem("TestItem2"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem2", 0));
            } catch (ItemNotFoundException e) {
            }

            service.activate(setupResult.bundleContext, setupResult.config);

            // Get the collection
            MongoCollection<Document> collection = database.getCollection("testCollection");

            // Store items directly to the database with defined timestamps
            for (int i = 0; i < 10; i++) {
                Document obj = DataCreationHelper.createDocument("TestItem", i, LocalDate.now().minusDays(i));
                collection.insertOne(obj);

                Document obj2 = DataCreationHelper.createDocument("TestItem2", i * 2, LocalDate.now().minusDays(i));
                collection.insertOne(obj2);
            }

            // Execution
            FilterCriteria filter1 = DataCreationHelper.createFilterCriteria("TestItem",
                    ZonedDateTime.now().minusDays(5), null);
            Iterable<HistoricItem> result1 = service.query(filter1);

            // Verification
            VerificationHelper.verifyQueryResult(result1, 4, -1, 5);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the query method of MongoDBPersistenceService with NumberItems in a single collection and a state equals
     * filter.
     *
     * This test checks if the query method correctly retrieves NumberItems from a single MongoDB collection that match
     * a specified state.
     * It uses different database backends provided by the provideDatabaseBackends method.
     *
     * @param dbContainer The container running the MongoDB instance.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideDatabaseBackends")
    public void testQueryNumberItemsInOneCollectionStateEquals(DatabaseTestContainer dbContainer) {
        try {
            // Preparation
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem", 0));
            } catch (ItemNotFoundException e) {
            }

            service.activate(setupResult.bundleContext, setupResult.config);

            // Get the collection
            MongoCollection<Document> collection = database.getCollection("testCollection");

            // Store items directly to the database with defined timestamps
            for (int i = 0; i < 10; i++) {
                Document obj = DataCreationHelper.createDocument("TestItem", i, LocalDate.now().minusDays(i));
                collection.insertOne(obj);
            }

            Document obj = DataCreationHelper.createDocument("TestItem", 4.0, LocalDate.now());
            collection.insertOne(obj);

            // Execution
            FilterCriteria filter1 = DataCreationHelper.createFilterCriteria("TestItem", null, null);
            filter1.setState(new DecimalType(4.0));
            filter1.setOperator(FilterCriteria.Operator.EQ);

            Iterable<HistoricItem> result1 = service.query(filter1);

            // Verification
            VerificationHelper.verifyQueryResult(result1, 4, 0, 2);
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store method of the MongoDBPersistenceService with all types of openHAB items.
     * Each item is stored in the collection in the MongoDB database.
     *
     * @param item The item to store in the database.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideOpenhabItemTypes")
    public void testStoreAllOpenhabItemTypesSingleCollection(GenericItem item) {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            // Execution
            service.store(item, null);

            // Verification
            MongoCollection<Document> collection = database.getCollection("testCollection");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            VerificationHelper.verifyDocument(insertedDocument, item.getName(), item.getState());
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the store and query method for various image sizes of the MongoDBPersistenceService
     * Each item is queried with the type from one collection in the MongoDB database.
     *
     * @param item The item to store in the database.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideOpenhabImageItemsInDifferentSizes")
    public void testStoreAndQueryyLargerImages(ImageItem item) {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;

            service.activate(setupResult.bundleContext, setupResult.config);
            try {
                Mockito.when(setupResult.itemRegistry.getItem(item.getName())).thenReturn(item);
            } catch (ItemNotFoundException e) {
            }
            try {
                service.store(item, null);
            } catch (org.bson.BsonMaximumSizeExceededException e) {
                if (item.getName().equals("ImageItem20MB")) {
                    // this is expected
                    return;
                } else {
                    throw e;
                }
            }

            // Execution
            FilterCriteria filter = DataCreationHelper.createFilterCriteria(item.getName());
            Iterable<HistoricItem> result = service.query(filter);
            // Verification

            VerificationHelper.verifyQueryResult(result, item.getState());
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the old way of storing data and query method of the MongoDBPersistenceService with all types of openHAB
     * items.
     * Each item is queried with the type from one collection in the MongoDB database.
     *
     * @param item The item to store in the database.
     */
    @ParameterizedTest
    @MethodSource("org.openhab.persistence.mongodb.internal.DataCreationHelper#provideOpenhabItemTypes")
    public void testOldDataQueryAllOpenhabItemTypesSingleCollection(GenericItem item) {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);
            try {
                Mockito.when(setupResult.itemRegistry.getItem(item.getName())).thenReturn(item);
            } catch (ItemNotFoundException e) {
            }
            MongoCollection<Document> collection = database.getCollection("testCollection");
            DataCreationHelper.storeOldData(collection, item.getName(), item.getState());
            // after storing, we have to adjust the expected values for ImageItems, ColorItems as well as DateTimeItems
            if (item instanceof ImageItem) {
                item.setState(new RawType(new byte[0], "application/octet-stream"));
            } else if (item instanceof ColorItem) {
                item.setState(new HSBType("0,0,0"));
            }

            // Execution
            FilterCriteria filter = DataCreationHelper.createFilterCriteria(item.getName());
            Iterable<HistoricItem> result = service.query(filter);
            // Verification

            if (item instanceof DateTimeItem) {
                // verify just the date part
                assertEquals(((DateTimeType) item.getState()).getZonedDateTime().toLocalDate(),
                        ((DateTimeType) result.iterator().next().getState()).getZonedDateTime().toLocalDate());
            } else {
                VerificationHelper.verifyQueryResult(result, item.getState());
            }
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the writting of NumberItems including units
     * Each item should be written to the database with the unit information
     *
     * @param item The item to store in the database.
     */
    @Test
    public void testStoreNumberItemWithUnit() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);
            MongoCollection<Document> collection = database.getCollection("testCollection");

            NumberItem item = DataCreationHelper.createNumberItem("Number:Energy", "TestItem",
                    new QuantityType<>("10.1 kWh"));

            // Execution
            service.store(item, null);

            // Verification
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            assertEquals(10.1, insertedDocument.get(MongoDBFields.FIELD_VALUE));
            assertEquals("kWh", insertedDocument.get(MongoDBFields.FIELD_UNIT));
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the reading of NumberItems including units
     * Each item should be written to the database with the unit information
     *
     * @param item The item to store in the database.
     */
    @Test
    public void testQueryNumberItemWithUnit() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testCollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);
            MongoCollection<Document> collection = database.getCollection("testCollection");

            NumberItem item = DataCreationHelper.createNumberItem("Number:Energy", "TestItem",
                    new QuantityType<>("10.1 MWh"));
            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem")).thenReturn(item);
            } catch (ItemNotFoundException e) {
            }

            Document obj = new Document();
            obj.put(MongoDBFields.FIELD_ID, new ObjectId());
            obj.put(MongoDBFields.FIELD_ITEM, "TestItem");
            obj.put(MongoDBFields.FIELD_REALNAME, "TestItem");
            obj.put(MongoDBFields.FIELD_TIMESTAMP, new Date());
            obj.put(MongoDBFields.FIELD_VALUE, 201.5);
            obj.put(MongoDBFields.FIELD_UNIT, "Wh");
            collection.insertOne(obj);

            // Execution
            FilterCriteria filter = DataCreationHelper.createFilterCriteria("TestItem");
            Iterable<HistoricItem> result = service.query(filter);
            VerificationHelper.verifyQueryResult(result, new QuantityType<>("201.5 Wh"));
        } finally {
            dbContainer.stop();
        }
    }

    /**
     * Tests the toString of a MongoDBItem
     * 
     *
     * @param item The item to store in the database.
     */
    @Test
    public void testHistoricItemToString() {
        // Preparation
        Instant now = Instant.now();
        HistoricItem item = new MongoDBItem("TestItem", new DecimalType(10.1), now);

        // Execution
        String result = item.toString();

        // Verification
        // Jan 29, 2024, 8:43:26 PM: TestItem -> 10.1
        String expected = DateFormat.getDateTimeInstance().format(Date.from(now)) + ": TestItem -> 10.1";
        assertEquals(expected, result);
    }

    /*
     * Test the store method which stores a item state as well as a timestampe (ZonedDateTime) and check the result in
     * the database
     */
    @Test
    public void testStoreItemWithTimestamp() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB(null, dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);
            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem", 0));
            } catch (ItemNotFoundException e) {
            }

            // Execution
            NumberItem item = DataCreationHelper.createNumberItem("TestItem", 10.1);
            DecimalType historicState = new DecimalType(11110.1);
            ZonedDateTime now = ZonedDateTime.now();
            service.store(item, now, historicState);

            // Verification
            MongoCollection<Document> collection = database.getCollection("TestItem");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            VerificationHelper.verifyDocument(insertedDocument, "TestItem", historicState);
            assertEquals(Date.from(now.toInstant()), insertedDocument.get(MongoDBFields.FIELD_TIMESTAMP));
        } finally {
            dbContainer.stop();
        }
    }

    /*
     * Test the store method which stores a item state as well as a timestampe (ZonedDateTime) and check the result in
     * the database
     */
    @Test
    public void testStoreItemWithTimestampAndAlias() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB(null, dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);
            try {
                Mockito.when(setupResult.itemRegistry.getItem("TestItem"))
                        .thenReturn(DataCreationHelper.createNumberItem("TestItem", 0));
            } catch (ItemNotFoundException e) {
            }

            // Execution
            NumberItem item = DataCreationHelper.createNumberItem("TestItem", 10.1);
            DecimalType historicState = new DecimalType(11110.1);
            ZonedDateTime now = ZonedDateTime.now();
            service.store(item, now, historicState, "AliasName");

            // Verification
            MongoCollection<Document> collection = database.getCollection("TestItem");
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is only one document

            Document insertedDocument = documents.get(0); // Get the first (and only) document

            VerificationHelper.verifyDocumentWithAlias(insertedDocument, "AliasName", "TestItem", historicState);
            assertEquals(Date.from(now.toInstant()), insertedDocument.get(MongoDBFields.FIELD_TIMESTAMP));
        } finally {
            dbContainer.stop();
        }
    }

    /*
     * Test the remove method to remove one item from the database
     */
    @Test
    public void testremoveOneItem() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testcollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            for (double i = 0; i < 10.00; i += 0.3) {
                service.store(DataCreationHelper.createNumberItem("TestItem", i));
            }
            service.store(DataCreationHelper.createNumberItem("TestItemOther", 10.1));

            // Execution
            service.remove(DataCreationHelper.createFilterCriteria("TestItem", null, null));

            // Verification
            MongoCollection<Document> collection = database.getCollection("testcollection");

            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            assertEquals(1, documents.size()); // Assert that there is the other document

            VerificationHelper.verifyDocument(documents.get(0), "TestItemOther", 10.1);
        } finally {
            dbContainer.stop();
        }
    }

    /*
     * Test the remove method to remove values of a given timerange for one item
     */
    @Test
    public void testremoveATimeRangeFromOneItem() {
        // Preparation
        DatabaseTestContainer dbContainer = new DatabaseTestContainer(new MemoryBackend());
        try {
            SetupResult setupResult = DataCreationHelper.setupMongoDB("testcollection", dbContainer);
            MongoDBPersistenceService service = setupResult.service;
            MongoDatabase database = setupResult.database;

            service.activate(setupResult.bundleContext, setupResult.config);

            List<PersistenceTestItem> testDataList = DataCreationHelper.createTestData(service, "TestItem",
                    "TestItemOther");

            // Execution
            // Calculate the start and end dates
            ZonedDateTime startDate = ZonedDateTime.now().plusDays(3).truncatedTo(ChronoUnit.DAYS);
            ZonedDateTime endDate = ZonedDateTime.now().plusDays(17).truncatedTo(ChronoUnit.DAYS).plusDays(1)
                    .minusNanos(1);

            // Create the filter and remove the data
            service.remove(DataCreationHelper.createFilterCriteria("TestItem", startDate, endDate));

            // Verification
            MongoCollection<Document> collection = database.getCollection("testcollection");

            // Query the database for all data points
            List<Document> documents = (ArrayList<Document>) collection.find().into(new ArrayList<>());

            // Create a set of the returned data points
            Set<PersistenceTestItem> returnedData = documents.stream()
                    .map(doc -> new PersistenceTestItem(doc.getString(MongoDBFields.FIELD_ITEM),
                            ZonedDateTime.ofInstant(doc.getDate(MongoDBFields.FIELD_TIMESTAMP).toInstant(),
                                    ZoneId.systemDefault()),
                            doc.getDouble(MongoDBFields.FIELD_VALUE)))
                    .collect(Collectors.toSet());

            // Create a set of the expected data points
            Set<PersistenceTestItem> expectedData = testDataList
                    .stream().filter(testData -> !(testData.itemName.equals("TestItem")
                            && testData.date.isAfter(startDate) && testData.date.isBefore(endDate)))
                    .collect(Collectors.toSet());

            for (PersistenceTestItem expectedItem : expectedData) {
                // Assert that this item is in the returned data
                assertTrue(returnedData.contains(expectedItem),
                        "Expected item not found in returned data: " + expectedItem);
            }

            // Iterate over the returned data
            for (PersistenceTestItem returnedItem : returnedData) {
                // Assert that this item is in the expected data
                assertTrue(expectedData.contains(returnedItem),
                        "Unexpected item found in returned data: " + returnedItem);
            }
        } finally {
            dbContainer.stop();
        }
    }
}
