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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemNotUniqueException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.RegistryHook;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class BaseIntegrationTest {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBPersistenceService.class);
    protected static @Nullable DynamoDBPersistenceService service;
    protected static final Map<String, Item> ITEMS = new HashMap<>();

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @BeforeAll
    public static void initService() throws InterruptedException {
        ITEMS.put("dimmer", new DimmerItem("dimmer"));
        ITEMS.put("number", new NumberItem("number"));
        ITEMS.put("string", new StringItem("string"));
        ITEMS.put("switch", new SwitchItem("switch"));
        ITEMS.put("contact", new ContactItem("contact"));
        ITEMS.put("color", new ColorItem("color"));
        ITEMS.put("rollershutter", new RollershutterItem("rollershutter"));
        ITEMS.put("datetime", new DateTimeItem("datetime"));
        ITEMS.put("call", new CallItem("call"));
        ITEMS.put("location", new LocationItem("location"));
        ITEMS.put("player_playpause", new PlayerItem("player_playpause"));
        ITEMS.put("player_rewindfastforward", new PlayerItem("player_rewindfastforward"));

        service = new DynamoDBPersistenceService(new ItemRegistry() {
            @Override
            public Collection<Item> getItems(String pattern) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Item> getItems() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Item getItemByPattern(String name) throws ItemNotFoundException, ItemNotUniqueException {
                throw new UnsupportedOperationException();
            }

            @Override
            public Item getItem(String name) throws ItemNotFoundException {
                Item item = ITEMS.get(name);
                if (item == null) {
                    throw new ItemNotFoundException(name);
                }
                return item;
            }

            @Override
            public void addRegistryChangeListener(RegistryChangeListener<Item> listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Item> getAll() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Stream<Item> stream() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable Item get(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeRegistryChangeListener(RegistryChangeListener<Item> listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Item add(Item element) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable Item update(Item element) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable Item remove(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Item> getItemsOfType(String type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Item> getItemsByTag(String... tags) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Collection<Item> getItemsByTagAndType(String type, String... tags) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Item> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable Item remove(String itemName, boolean recursive) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addRegistryHook(RegistryHook<Item> hook) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void removeRegistryHook(RegistryHook<Item> hook) {
                throw new UnsupportedOperationException();
            }
        });

        Map<String, Object> config = new HashMap<>();
        String value = System.getProperty("DYNAMODBTEST_REGION");
        config.put("region", value != null ? value : "");
        value = System.getProperty("DYNAMODBTEST_ACCESS");
        config.put("accessKey", value != null ? value : "");
        value = System.getProperty("DYNAMODBTEST_SECRET");
        config.put("secretKey", value != null ? value : "");
        config.put("tablePrefix", "dynamodb-integration-tests-");

        // Disable buffering
        config.put("bufferSize", "0");

        for (Entry<String, Object> entry : config.entrySet()) {
            if (((String) entry.getValue()).isEmpty()) {
                LOGGER.warn(String.format(
                        "Expecting %s to have value for integration tests. Integration tests will be skipped",
                        entry.getKey()));
                service = null;
                return;
            }
        }

        service.activate(null, config);
        clearData();
    }

    protected static void clearData() {
        // Clear data
        for (String table : new String[] { "dynamodb-integration-tests-bigdecimal",
                "dynamodb-integration-tests-string" }) {
            try {
                service.getDb().getDynamoClient().deleteTable(table);
                service.getDb().getDynamoDB().getTable(table).waitForDelete();
            } catch (ResourceNotFoundException e) {
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted! Table might not have been deleted");
            }
        }
    }
}
