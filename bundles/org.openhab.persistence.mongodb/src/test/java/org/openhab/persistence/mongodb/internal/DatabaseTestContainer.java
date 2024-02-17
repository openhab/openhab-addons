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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
@NonNullByDefault
public class DatabaseTestContainer {
    // A map to store MongoDBContainer instances for different MongoDB versions.
    private static final Map<String, MongoDBContainer> mongoDBContainers = new HashMap<>();

    // The MongoDBContainer instance for this DatabaseTestContainer.
    private @Nullable MongoDBContainer mongoDBContainer;

    // The MongoServer instance for this DatabaseTestContainer.
    private @Nullable MongoServer server;

    // The InetSocketAddress instance for this DatabaseTestContainer.
    private @Nullable InetSocketAddress serverAddress;

    /**
     * Creates a new DatabaseTestContainer for a given MongoDB version.
     * If a MongoDBContainer for the given version already exists, it is reused.
     * 
     * @param mongoDBVersion The version of MongoDB to use.
     */
    public DatabaseTestContainer(String mongoDBVersion) {
        server = null;
        serverAddress = null;
        mongoDBContainer = mongoDBContainers.computeIfAbsent(mongoDBVersion, MongoDBContainer::new);
    }

    /**
     * Creates a new DatabaseTestContainer for an in-memory MongoDB server.
     */
    public DatabaseTestContainer(MemoryBackend memoryBackend) {
        mongoDBContainer = null;
        server = new MongoServer(memoryBackend);
        if (server != null) {
            serverAddress = server.bind();
        }
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
        @Nullable
        MongoDBContainer lc_mongoDBContainer = this.mongoDBContainer;
        @Nullable
        InetSocketAddress lc_serverAddress = this.serverAddress;
        @Nullable
        MongoServer lc_server = this.server;
        if (lc_mongoDBContainer != null) {
            return lc_mongoDBContainer.getConnectionString();
        } else if (lc_server != null && lc_serverAddress != null) {
            return String.format("mongodb://%s:%s", lc_serverAddress.getHostName(), lc_serverAddress.getPort());
        } else {
            return "";
        }
    }
}
