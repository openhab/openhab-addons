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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * This is the implementation of the test for MongoDB {@link PersistenceService}.
 *
 * @author René Ulbricht - Initial contribution
 */
public class MongoDBPersistenceServiceTest {

    @Test
    public void testMongoMemory() {
        // Mock the MongoDB server
        MongoServer server = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = server.bind();

        // Create a MongoClient connected to the mock server
        MongoClient mongoClient = MongoClients
                .create("mongodb://" + serverAddress.getHostName() + ":" + serverAddress.getPort());

        // Create a database and collection
        MongoDatabase database = mongoClient.getDatabase("testDatabase");
        MongoCollection<Document> collection = database.getCollection("testCollection");

        // Create a document to insert
        Document document = new Document("key", "value");

        // Insert the document into the collection
        collection.insertOne(document);

        // Query the data
        Document result = collection.find().first();

        // Verify the data
        assertEquals(document, result);

        // Clean up
        collection.drop();
        database.drop();
        mongoClient.close();
        server.shutdown();
    }
}
