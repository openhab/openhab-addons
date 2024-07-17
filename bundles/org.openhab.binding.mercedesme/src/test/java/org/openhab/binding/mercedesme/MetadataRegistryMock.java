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
package org.openhab.binding.mercedesme;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * {@link MetadataRegistryMock} object for unit testing
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MetadataRegistryMock implements MetadataRegistry {
    List<Metadata> metaDataList = new ArrayList<>();

    @Override
    public void addRegistryChangeListener(RegistryChangeListener<Metadata> listener) {
    }

    @Override
    public Collection<Metadata> getAll() {
        return metaDataList;
    }

    @Override
    public Stream<Metadata> stream() {
        return metaDataList.stream();
    }

    @Override
    public @Nullable Metadata get(MetadataKey key) {
        return null;
    }

    @Override
    public void removeRegistryChangeListener(RegistryChangeListener<Metadata> listener) {
    }

    @Override
    public Metadata add(Metadata element) {
        metaDataList.add(element);
        return element;
    }

    @Override
    public @Nullable Metadata update(Metadata element) {
        return element;
    }

    @Override
    public @Nullable Metadata remove(MetadataKey key) {
        return null;
    }

    @Override
    public boolean isInternalNamespace(String namespace) {
        return false;
    }

    @Override
    public Collection<String> getAllNamespaces(String itemname) {
        return List.of();
    }

    @Override
    public void removeItemMetadata(String itemname) {
    }
}
