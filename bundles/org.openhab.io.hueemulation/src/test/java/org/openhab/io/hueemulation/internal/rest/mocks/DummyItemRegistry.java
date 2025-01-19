/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;

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

    @Override
    public Stream<Item> stream() {
        return items.values().stream();
    }

    @Override
    public @Nullable Item get(String key) {
        return items.get(key);
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Item> listener) {
        listeners.remove(listener);
    }

    @Override
    public Item add(Item element) {
        items.put(element.getUID(), element);
        for (RegistryChangeListener<Item> l : listeners) {
            l.added(element);
        }
        return element;
    }

    @Override
    public @Nullable Item update(Item element) {
        Item put = items.put(element.getUID(), element);
        if (put != null) {
            for (RegistryChangeListener<Item> l : listeners) {
                l.updated(put, element);
            }
        }
        return put;
    }

    @Override
    public @Nullable Item remove(String key) {
        Item put = items.remove(key);
        if (put != null) {
            for (RegistryChangeListener<Item> l : listeners) {
                l.removed(put);
            }
        }
        return put;
    }

    @Override
    public Item getItem(String name) throws ItemNotFoundException {
        Item item = items.get(name);
        if (item == null) {
            throw new ItemNotFoundException(name);
        }
        return item;
    }

    @Override
    public Item getItemByPattern(String name) throws ItemNotFoundException {
        Item item = items.get(name);
        if (item == null) {
            throw new ItemNotFoundException(name);
        }
        return item;
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

    @Override
    public Collection<Item> getItemsByTag(String... tags) {
        return items.values();
    }

    @Override
    public Collection<Item> getItemsByTagAndType(String type, String... tags) {
        return items.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Item> Collection<T> getItemsByTag(Class<T> typeFilter, String... tags) {
        return (Collection<T>) items.values();
    }

    @Override
    public @Nullable Item remove(String itemName, boolean recursive) {
        Item put = items.remove(itemName);
        if (put != null) {
            for (RegistryChangeListener<Item> l : listeners) {
                l.removed(put);
            }
        }
        return put;
    }
}
