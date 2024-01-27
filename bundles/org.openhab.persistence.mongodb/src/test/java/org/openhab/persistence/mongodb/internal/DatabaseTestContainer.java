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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.testcontainers.containers.MongoDBContainer;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

/**
 * This class provides a container for MongoDB for testing purposes.
 * It uses the Testcontainers library to manage the MongoDB container.
 * It also provides an in-memory MongoDB server for testing.
 * 
 * @author René Ulbricht - Initial contribution
 */
public class DatabaseTestContainer {
    // A map to store MongoDBContainer instances for different MongoDB versions.
    private static final Map<String, MongoDBContainer> mongoDBContainers = new HashMap<>();

    // The MongoDBContainer instance for this DatabaseTestContainer.
    private MongoDBContainer mongoDBContainer;

    // The MongoServer instance for this DatabaseTestContainer.
    private MongoServer server;

    // The MemoryBackend instance for this DatabaseTestContainer.
    private MemoryBackend memoryBackend;

    // The InetSocketAddress instance for this DatabaseTestContainer.
    private InetSocketAddress serverAddress;

    /**
     * Creates a new DatabaseTestContainer for a given MongoDB version.
     * If a MongoDBContainer for the given version already exists, it is reused.
     * 
     * @param mongoDBVersion The version of MongoDB to use.
     */
    public DatabaseTestContainer(String mongoDBVersion) {
        mongoDBContainer = mongoDBContainers.computeIfAbsent(mongoDBVersion, MongoDBContainer::new);
    }

    /**
     * Creates a new DatabaseTestContainer for an in-memory MongoDB server.
     */
    public DatabaseTestContainer(MemoryBackend memoryBackend) {
        server = new MongoServer(memoryBackend);
        serverAddress = server.bind();
    }

    /**
     * Starts the MongoDB container or the in-memory MongoDB server.
     */
    public void start() {
        if (mongoDBContainer != null && !mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
    }

    /**
     * Don't do anything.
     */
    public void stop() {
    }

    /**
     * Returns the connection string for connecting to the MongoDB container or the in-memory MongoDB server.
     * 
     * @return The connection string.
     */
    public String getConnectionString() {
        if (mongoDBContainer != null) {
            return mongoDBContainer.getConnectionString();
        } else if (server != null) {
            return String.format("mongodb://%s:%s", serverAddress.getHostName(), serverAddress.getPort());
        } else {
            return null;
        }
    }
}
