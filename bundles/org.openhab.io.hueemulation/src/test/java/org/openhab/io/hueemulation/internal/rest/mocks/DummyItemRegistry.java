/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.rest.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.registry.RegistryChangeListener;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.items.RegistryHook;

/**
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DummyItemRegistry implements ItemRegistry {
    Map<String, Item> items = new TreeMap<>();
    List<RegistryChangeListener<Item>> listeners = new ArrayList<>();

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<Item> listener) {
        listeners.add(listener);
    }

    @Override
    public Collection<Item> getAll() {
        return items.values();
    }

    @NonNullByDefault({})
    @Override
    public Stream<Item> stream() {
        return items.values().stream();
    }

    @Override
    public @Nullable Item get(@Nullable String key) {
        return items.get(key);
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Item> listener) {
        listeners.remove(listener);
    }

    @Override
    public Item add(Item element) {
        Item put = items.put(element.getUID(), element);
        for (RegistryChangeListener<Item> l : listeners) {
            l.added(element);
        }
        return put;
    }

    @Override
    public @Nullable Item update(Item element) {
        Item put = items.put(element.getUID(), element);
        for (RegistryChangeListener<Item> l : listeners) {
            l.updated(put, element);
        }
        return put;
    }

    @Override
    public @Nullable Item remove(String key) {
        Item put = items.remove(key);
        for (RegistryChangeListener<Item> l : listeners) {
            l.removed(put);
        }
        return put;
    }

    @Override
    public Item getItem(@Nullable String name) {
        return items.get(name);
    }

    @Override
    public Item getItemByPattern(String name) {
        return items.get(name);
    }

    @Override
    public Collection<Item> getItems() {
        return items.values();
    }

    @Override
    public Collection<Item> getItemsOfType(String type) {
        return items.values();
    }

    @Override
    public Collection<Item> getItems(String pattern) {
        return items.values();
    }

    @NonNullByDefault({})
    @Override
    public Collection<Item> getItemsByTag(String... tags) {
        return items.values();
    }

    @NonNullByDefault({})
    @Override
    public Collection<Item> getItemsByTagAndType(String type, String... tags) {
        return items.values();
    }

    @NonNullByDefault({})
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Item> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags) {
        return (Collection<T>) items.values();
    }

    @Override
    public @Nullable Item remove(String itemName, boolean recursive) {
        Item put = items.remove(itemName);
        for (RegistryChangeListener<Item> l : listeners) {
            l.removed(put);
        }
        return put;
    }

    @NonNullByDefault({})
    @Override
    public void addRegistryHook(RegistryHook<Item> hook) {

    }

    @NonNullByDefault({})
    @Override
    public void removeRegistryHook(RegistryHook<Item> hook) {

    }
}
