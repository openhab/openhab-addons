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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;

/**
 * Unit tests for {@link ItemRegistrySourceProvider}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
class ItemRegistrySourceProviderTest {

    private @Nullable ItemRegistry itemRegistry;
    private @Nullable ItemRegistrySourceProvider provider;
    private final List<EntityChange> emittedChanges = new ArrayList<>();

    @BeforeEach
    void setUp() {
        itemRegistry = mock(ItemRegistry.class);
        provider = new ItemRegistrySourceProvider(Objects.requireNonNull(itemRegistry));
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
    void unregistersChangeListenerOnDeactivate() {
        provider.deactivate();
        verify(itemRegistry).removeRegistryChangeListener(Objects.requireNonNull(provider));
    }
}
