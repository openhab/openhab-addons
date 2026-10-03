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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;

/**
 * Unit tests for {@link ItemRegistrySourceProvider}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ItemRegistrySourceProviderTest {

    private @Nullable ItemRegistry itemRegistry;
    private @Nullable MetadataRegistry metadataRegistry;
    private @Nullable ItemRegistrySourceProvider provider;
    private final List<EntityChange> emittedChanges = new ArrayList<>();

    @BeforeEach
    void setUp() {
        itemRegistry = mock(ItemRegistry.class);
        metadataRegistry = mock(MetadataRegistry.class);
        provider = new ItemRegistrySourceProvider(Objects.requireNonNull(itemRegistry),
                Objects.requireNonNull(metadataRegistry));
        provider.setOnChangeListener(emittedChanges::add);
        emittedChanges.clear();
    }

    @Test
    void reportsMetadataAndKeyCorrectly() {
        assertEquals("ITEMS", provider.getSourceName());
        assertTrue(provider.supportsSource("ITEMS"));

        Item item = mock(Item.class);
        when(item.getName()).thenReturn("LivingRoom_Light");
        assertEquals("LivingRoom_Light", provider.getKey(item));
    }

    @Test
    void adaptsItemToImmutableMap() {
        Item item = mock(Item.class);
        when(item.getName()).thenReturn("LivingRoom_Light");
        when(item.getType()).thenReturn("Switch");
        when(item.getLabel()).thenReturn("Living Room Light");
        when(item.getCategory()).thenReturn("Light");
        when(item.getTags()).thenReturn(Set.of("Switchable"));
        when(item.getGroupNames()).thenReturn(List.of("gLivingRoom"));

        Map<String, @Nullable Object> map = provider.adaptToMap(item);

        assertEquals("LivingRoom_Light", map.get("name"));
        assertEquals("Switch", map.get("type"));
        assertEquals("Living Room Light", map.get("label"));
        assertEquals("Light", map.get("category"));

        assertThrows(UnsupportedOperationException.class, () -> map.put("newKey", "value"));
    }

    @Test
    void includesMetadataInAdaptedMap() {
        Item item = mock(Item.class);
        when(item.getName()).thenReturn("LivingRoom_Light");
        Metadata metadata = new Metadata(new MetadataKey("stateDescription", "LivingRoom_Light"), "",
                Map.of("pattern", "%.1f °C"));
        when(metadataRegistry.getAllNamespaces("LivingRoom_Light")).thenReturn(List.of("stateDescription"));
        when(metadataRegistry.get(new MetadataKey("stateDescription", "LivingRoom_Light"))).thenReturn(metadata);

        Map<String, @Nullable Object> map = provider.adaptToMap(item);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, @Nullable Object>> metadataMap = (Map<String, Map<String, @Nullable Object>>) map
                .get("metadata");
        assertNotNull(metadataMap);
        assertEquals("", metadataMap.get("stateDescription").get("value"));
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) metadataMap.get("stateDescription").get("config");
        assertEquals("%.1f °C", config.get("pattern"));
    }

    @Test
    void emitsExpectedEventsOnRegistryChanges() {
        Item item1 = mock(Item.class);
        when(item1.getName()).thenReturn("Item1");

        Item item2 = mock(Item.class);
        when(item2.getName()).thenReturn("Item1");
        when(item2.getLabel()).thenReturn("Updated Label");

        // 1. Added
        provider.added(item1);
        assertEquals(1, emittedChanges.size());
        assertNull(emittedChanges.get(0).oldEntity());
        assertNotNull(emittedChanges.get(0).newEntity());

        // 2. Updated
        provider.updated(item1, item2);
        assertEquals(2, emittedChanges.size());
        assertNotNull(emittedChanges.get(1).oldEntity());
        assertNotNull(emittedChanges.get(1).newEntity());

        // 3. Removed
        provider.removed(item1);
        assertEquals(3, emittedChanges.size());
        assertNotNull(emittedChanges.get(2).oldEntity());
        assertNull(emittedChanges.get(2).newEntity());

        // 4. Bulk reset
        provider.allItemsChanged(List.of("Item1"));
        assertEquals(4, emittedChanges.size());
        assertNull(emittedChanges.get(3).oldEntity());
        assertNull(emittedChanges.get(3).newEntity());
    }

    @Test
    void emitsChangeEventsOnMetadataChanges() throws ItemNotFoundException {
        Item item = mock(Item.class);
        when(item.getName()).thenReturn("Item1");
        when(itemRegistry.getItems()).thenReturn(List.of(item));
        when(itemRegistry.getItem("Item1")).thenReturn(item);

        Metadata oldMetadata = new Metadata(new MetadataKey("unit", "Item1"), "°C", Map.of());
        Metadata newMetadata = new Metadata(new MetadataKey("unit", "Item1"), "°F", Map.of());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RegistryChangeListener<Metadata>> listenerCaptor = ArgumentCaptor
                .forClass(RegistryChangeListener.class);
        verify(metadataRegistry).addRegistryChangeListener(listenerCaptor.capture());
        RegistryChangeListener<Metadata> listener = listenerCaptor.getValue();
        listener.added(newMetadata);
        listener.updated(oldMetadata, newMetadata);
        listener.removed(oldMetadata);

        assertEquals(3, emittedChanges.size());
        EntityChange added = emittedChanges.get(0);
        assertEquals("ITEMS", added.source());
        assertNotNull(added.oldEntity());
        assertNull(metadataValue(added.oldEntity(), "unit"));
        assertEquals("°F", metadataValue(added.newEntity(), "unit"));

        EntityChange updated = emittedChanges.get(1);
        assertEquals("ITEMS", updated.source());
        assertEquals("°C", metadataValue(updated.oldEntity(), "unit"));
        assertEquals("°F", metadataValue(updated.newEntity(), "unit"));

        EntityChange removed = emittedChanges.get(2);
        assertEquals("ITEMS", removed.source());
        assertEquals("°C", metadataValue(removed.oldEntity(), "unit"));
        assertNotNull(removed.newEntity());
        assertNull(metadataValue(removed.newEntity(), "unit"));
    }

    @Test
    void metadataChangesForUnrelatedItemsDoNotTriggerRebuild() throws ItemNotFoundException {
        Item registeredItem = mock(Item.class);
        when(registeredItem.getName()).thenReturn("Item1");
        when(itemRegistry.getItems()).thenReturn(List.of(registeredItem));
        when(itemRegistry.getItem("Item1")).thenReturn(registeredItem);
        when(itemRegistry.getItem("Item2")).thenThrow(new ItemNotFoundException("Item2"));

        DynamicSourceRegistry dynamicSourceRegistry = new DynamicSourceRegistry(
                Map.of("ITEMS", Objects.requireNonNull(provider)), 0);
        Path dependentFile = Path.of("items.yaml");
        dynamicSourceRegistry.registerDependency(dependentFile, "ITEMS");
        List<Path> rebuiltFiles = new ArrayList<>();
        dynamicSourceRegistry.setOnFileRecompileListener(rebuiltFiles::add);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RegistryChangeListener<Metadata>> listenerCaptor = ArgumentCaptor
                .forClass(RegistryChangeListener.class);
        verify(metadataRegistry).addRegistryChangeListener(listenerCaptor.capture());
        RegistryChangeListener<Metadata> listener = listenerCaptor.getValue();

        listener.added(new Metadata(new MetadataKey("unit", "Item1"), "°C", Map.of()));
        assertEquals(List.of(dependentFile), rebuiltFiles);

        rebuiltFiles.clear();
        listener.updated(new Metadata(new MetadataKey("unit", "Item2"), "°C", Map.of()),
                new Metadata(new MetadataKey("unit", "Item2"), "°F", Map.of()));
        assertTrue(rebuiltFiles.isEmpty(), "A metadata change for an unregistered item must not rebuild the file");

        dynamicSourceRegistry.clear();
    }

    @Test
    void unregistersChangeListenerOnDeactivate() {
        provider.deactivate();
        verify(itemRegistry).removeRegistryChangeListener(Objects.requireNonNull(provider));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<RegistryChangeListener<Metadata>> listenerCaptor = ArgumentCaptor
                .forClass(RegistryChangeListener.class);
        verify(metadataRegistry).removeRegistryChangeListener(listenerCaptor.capture());
    }

    private static @Nullable Object metadataValue(@Nullable Map<String, @Nullable Object> entity, String namespace) {
        if (entity == null || !(entity.get("metadata") instanceof Map<?, ?> metadataMap)
                || !(metadataMap.get(namespace) instanceof Map<?, ?> metadataEntry)) {
            return null;
        }
        return metadataEntry.get("value");
    }
}
