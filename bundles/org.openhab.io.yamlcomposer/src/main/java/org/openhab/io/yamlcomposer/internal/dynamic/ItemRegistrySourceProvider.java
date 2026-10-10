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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataRegistry;
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
    private final MetadataRegistry metadataRegistry;
    private final RegistryChangeListener<Metadata> metadataChangeListener = new RegistryChangeListener<>() {
        @Override
        public void added(Metadata element) {
            metadataChanged(null, element);
        }

        @Override
        public void removed(Metadata element) {
            metadataChanged(element, null);
        }

        @Override
        public void updated(Metadata oldElement, Metadata element) {
            metadataChanged(oldElement, element);
        }
    };
    private volatile Consumer<EntityChange> onChangeListener = change -> {
    };

    @Activate
    public ItemRegistrySourceProvider(@Reference ItemRegistry itemRegistry,
            @Reference MetadataRegistry metadataRegistry) {
        this.itemRegistry = itemRegistry;
        this.metadataRegistry = metadataRegistry;
        itemRegistry.addRegistryChangeListener(this);
        metadataRegistry.addRegistryChangeListener(metadataChangeListener);
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
        return adaptToMap(item, getMetadataForItem(item.getName()));
    }

    @Override
    public Map<String, Map<String, @Nullable Object>> getSourceMap() {
        Map<String, Map<String, Metadata>> metadataByItem = new LinkedHashMap<>();
        for (Metadata metadata : metadataRegistry.getAll()) {
            String itemName = metadata.getUID().getItemName();
            String namespace = metadata.getUID().getNamespace();
            metadataByItem.computeIfAbsent(itemName, key -> new LinkedHashMap<>()).put(namespace, metadata);
        }

        Map<String, Map<String, @Nullable Object>> sourceMap = new LinkedHashMap<>();
        for (Item item : getAllEntities()) {
            sourceMap.put(getKey(item), adaptToMap(item, metadataByItem.getOrDefault(item.getName(), Map.of())));
        }
        return Collections.unmodifiableMap(sourceMap);
    }

    private Map<String, @Nullable Object> adaptToMap(Item item, Map<String, Metadata> itemMetadata) {
        ItemDTO dto = ItemDTOMapper.map(item);
        Map<String, @Nullable Object> dtoMap = OBJECT_MAPPER.convertValue(dto,
                new TypeReference<Map<String, @Nullable Object>>() {
                });

        Map<String, @Nullable Object> metadataMap = new LinkedHashMap<>();
        itemMetadata.forEach((namespace, metadata) -> metadataMap.put(namespace, toMetadataMap(metadata)));
        if (!metadataMap.isEmpty()) {
            dtoMap.put("metadata", metadataMap);
        }

        return RegistryEntityUtils.immutableMap(dtoMap);
    }

    private Map<String, Metadata> getMetadataForItem(String itemName) {
        Map<String, Metadata> itemMetadata = new LinkedHashMap<>();
        for (Metadata metadata : metadataRegistry.getAll()) {
            if (itemName.equals(metadata.getUID().getItemName())) {
                itemMetadata.put(metadata.getUID().getNamespace(), metadata);
            }
        }
        return itemMetadata;
    }

    private Map<String, @Nullable Object> toMetadataMap(Metadata metadata) {
        Map<String, @Nullable Object> metadataEntry = new LinkedHashMap<>();
        metadataEntry.put("value", metadata.getValue());
        if (!metadata.getConfiguration().isEmpty()) {
            metadataEntry.put("config", metadata.getConfiguration());
        }
        return metadataEntry;
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

    private void metadataChanged(@Nullable Metadata oldMetadata, @Nullable Metadata newMetadata) {
        Metadata changedMetadata = newMetadata != null ? newMetadata : oldMetadata;
        if (changedMetadata == null) {
            return;
        }

        String itemName = changedMetadata.getUID().getItemName();

        try {
            itemRegistry.getItem(itemName);
            onChangeListener.accept(new EntityChange(SOURCE_NAME, null, null));
        } catch (ItemNotFoundException e) {
            // The metadata belongs to an item that is no longer in the registry.
            // We can ignore this case since the item removal will trigger a 'removed' event for the item itself.
        }
    }

    @Deactivate
    public void deactivate() {
        itemRegistry.removeRegistryChangeListener(this);
        metadataRegistry.removeRegistryChangeListener(metadataChangeListener);
    }
}
