/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class DummyMetadataRegistry implements MetadataRegistry {
    Map<MetadataKey, Metadata> items = new HashMap<>();
    List<RegistryChangeListener<Metadata>> listeners = new ArrayList<>();

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<Metadata> listener) {
        listeners.add(listener);
    }

    @Override
    public Collection<Metadata> getAll() {
        return items.values();
    }

    @Override
    public Stream<Metadata> stream() {
        return items.values().stream();
    }

    @Override
    public @Nullable Metadata get(@Nullable MetadataKey key) {
        return items.get(key);
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Metadata> listener) {
        listeners.remove(listener);
    }

    @Override
    public Metadata add(Metadata element) {
        items.put(element.getUID(), element);
        for (RegistryChangeListener<Metadata> l : listeners) {
            l.added(element);
        }
        return element;
    }

    @Override
    public @Nullable Metadata update(Metadata element) {
        Metadata put = items.put(element.getUID(), element);
        if (put != null) {
            for (RegistryChangeListener<Metadata> l : listeners) {
                l.updated(put, element);
            }
        }
        return put;
    }

    @Override
    public @Nullable Metadata remove(MetadataKey key) {
        Metadata put = items.remove(key);
        if (put != null) {
            for (RegistryChangeListener<Metadata> l : listeners) {
                l.removed(put);
            }
        }
        return put;
    }

    @Override
    public boolean isInternalNamespace(String namespace) {
        return false;
    }

    @Override
    public Collection<String> getAllNamespaces(String itemname) {
        return stream().map(Metadata::getUID).filter(key -> key.getItemName().equals(itemname))
                .map(MetadataKey::getNamespace).collect(Collectors.toSet());
    }

    @Override
    public void removeItemMetadata(String name) {
    }
}
