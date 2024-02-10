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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.ItemRegistry;
import org.osgi.framework.BundleContext;

import com.mongodb.client.MongoDatabase;

/**
 * This class provides helper methods to create test items.
 * 
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public class SetupResult {
    public MongoDBPersistenceService service;
    public MongoDatabase database;
    public BundleContext bundleContext;
    public Map<String, Object> config;
    public ItemRegistry itemRegistry;
    public String dbname;

    public SetupResult(MongoDBPersistenceService service, MongoDatabase database, BundleContext bundleContext,
            Map<String, Object> config, ItemRegistry itemRegistry, String dbname) {
        this.service = service;
        this.database = database;
        this.dbname = dbname;
        this.bundleContext = bundleContext;
        this.config = config;
        this.itemRegistry = itemRegistry;
    }
}
