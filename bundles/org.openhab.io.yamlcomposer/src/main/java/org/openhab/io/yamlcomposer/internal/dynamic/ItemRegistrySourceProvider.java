/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal.dynamic;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.dto.ItemDTO;
import org.openhab.core.items.dto.ItemDTOMapper;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides Item Registry entities to dynamic sources and notifies listeners of discrete Item changes.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(service = { DynamicSourceProvider.class, ItemRegistrySourceProvider.class })
public class ItemRegistrySourceProvider implements DynamicSourceProvider<Item>, ItemRegistryChangeListener {
    private static final String SOURCE_NAME = "ITEMS";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ItemRegistry itemRegistry;
    private volatile Consumer<EntityChange> onChangeListener = change -> {
    };

    @Activate
    public ItemRegistrySourceProvider(@Reference ItemRegistry itemRegistry) {
        this.itemRegistry = itemRegistry;
        itemRegistry.addRegistryChangeListener(this);
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }

    @Override
    public boolean supportsSource(String source) {
        return SOURCE_NAME.equals(source);
    }

    @Override
    public Collection<Item> getAllEntities() {
        return itemRegistry.getItems();
    }

    @Override
    public String getKey(Item entity) {
        return entity.getName();
    }

    @Override
    public Map<String, @Nullable Object> adaptToMap(Item item) {
        ItemDTO dto = ItemDTOMapper.map(item);
        Map<String, @Nullable Object> dtoMap = OBJECT_MAPPER.convertValue(dto,
                new TypeReference<Map<String, @Nullable Object>>() {
                });

        return RegistryEntityUtils.immutableMap(dtoMap);
    }

    @Override
    public void setOnChangeListener(Consumer<EntityChange> listener) {
        this.onChangeListener = listener;
    }

    @Override
    public void added(Item element) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, null, adaptToMap(element)));
    }

    @Override
    public void removed(Item element) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, adaptToMap(element), null));
    }

    @Override
    public void updated(Item oldElement, Item element) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, adaptToMap(oldElement), adaptToMap(element)));
    }

    @Override
    public void allItemsChanged(Collection<String> oldItemNames) {
        onChangeListener.accept(new EntityChange(SOURCE_NAME, null, null));
    }

    @Deactivate
    public void deactivate() {
        itemRegistry.removeRegistryChangeListener(this);
    }
}
