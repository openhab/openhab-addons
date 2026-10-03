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
import org.openhab.core.items.MetadataKey;
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
        ItemDTO dto = ItemDTOMapper.map(item);
        Map<String, @Nullable Object> dtoMap = OBJECT_MAPPER.convertValue(dto,
                new TypeReference<Map<String, @Nullable Object>>() {
                });

        Map<String, @Nullable Object> metadataMap = new LinkedHashMap<>();
        for (String namespace : metadataRegistry.getAllNamespaces(item.getName())) {
            Metadata metadata = metadataRegistry.get(new MetadataKey(namespace, item.getName()));
            if (metadata != null) {
                Map<String, @Nullable Object> metadataEntry = new LinkedHashMap<>();
                metadataEntry.put("value", metadata.getValue());
                if (!metadata.getConfiguration().isEmpty()) {
                    metadataEntry.put("config", metadata.getConfiguration());
                }
                metadataMap.put(namespace, metadataEntry);
            }
        }
        if (!metadataMap.isEmpty()) {
            dtoMap.put("metadata", metadataMap);
        }

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

    private void metadataChanged(@Nullable Metadata oldMetadata, @Nullable Metadata newMetadata) {
        Metadata changedMetadata = newMetadata != null ? newMetadata : oldMetadata;
        if (changedMetadata == null) {
            return;
        }

        String itemName = changedMetadata.getUID().getItemName();

        try {
            Item item = itemRegistry.getItem(itemName);
            String namespace = changedMetadata.getUID().getNamespace();
            Map<String, @Nullable Object> oldEntity = adaptToMap(item, namespace, oldMetadata);
            Map<String, @Nullable Object> newEntity = adaptToMap(item, namespace, newMetadata);
            onChangeListener.accept(new EntityChange(SOURCE_NAME, oldEntity, newEntity));
        } catch (ItemNotFoundException e) {
            // The metadata belongs to an item that is no longer in the registry.
            // We can ignore this case since the item removal will trigger a 'removed' event for the item itself.
        }
    }

    private Map<String, @Nullable Object> adaptToMap(Item item, String namespace, @Nullable Metadata changedMetadata) {
        Map<String, @Nullable Object> entityMap = new LinkedHashMap<>(adaptToMap(item));
        Map<String, @Nullable Object> metadataMap = new LinkedHashMap<>();
        Object currentMetadata = entityMap.get("metadata");
        if (currentMetadata instanceof Map<?, ?> currentMap) {
            currentMap.forEach((key, value) -> {
                if (key instanceof String metadataNamespace) {
                    metadataMap.put(metadataNamespace, value);
                }
            });
        }

        metadataMap.remove(namespace);
        if (changedMetadata != null) {
            Map<String, @Nullable Object> metadataEntry = new LinkedHashMap<>();
            metadataEntry.put("value", changedMetadata.getValue());
            if (!changedMetadata.getConfiguration().isEmpty()) {
                metadataEntry.put("config", changedMetadata.getConfiguration());
            }
            metadataMap.put(namespace, metadataEntry);
        }
        if (metadataMap.isEmpty()) {
            entityMap.remove("metadata");
        } else {
            entityMap.put("metadata", metadataMap);
        }
        return RegistryEntityUtils.immutableMap(entityMap);
    }

    @Deactivate
    public void deactivate() {
        itemRegistry.removeRegistryChangeListener(this);
        metadataRegistry.removeRegistryChangeListener(metadataChangeListener);
    }
}
