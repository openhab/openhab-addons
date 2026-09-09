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
package org.openhab.io.eebus.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openmuc.jeebus.spine.api.Entity;

/**
 * Tests for {@link EEBusChangeListener}, specifically the two lifecycle bugs found in review on
 * PR #21313: a full item-registry reload re-registering an already-bound use case, and a metadata
 * config update on an already-bound item being silently swallowed instead of either applying or
 * warning. Both would have been invisible without a test that actually counts
 * {@code entity.addUseCase()} calls.
 */
@NonNullByDefault
class EEBusChangeListenerTest {

    private static final String ITEM_NAME = "Test_Lpc_Limit";
    private static final MetadataKey METADATA_KEY = new MetadataKey("eebus", ITEM_NAME);

    private final ItemRegistry itemRegistry = mock(ItemRegistry.class);
    private final MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);
    private final EventPublisher eventPublisher = mock(EventPublisher.class);
    private final Entity entity = mock(Entity.class);

    private static Metadata lpcMetadata(Map<String, Object> config) {
        return new Metadata(METADATA_KEY, "lpc", config);
    }

    @Test
    void allItemsChangedDoesNotReRegisterAUseCaseAlreadyBoundToTheSameItem() {
        Item item = new NumberItem(ITEM_NAME);
        when(itemRegistry.getItems()).thenReturn(List.of(item));
        when(metadataRegistry.get(METADATA_KEY)).thenReturn(lpcMetadata(Map.of()));

        EEBusChangeListener listener = new EEBusChangeListener(itemRegistry, metadataRegistry, eventPublisher, entity);
        verify(entity, times(1)).addUseCase(any());

        // Simulates a full item/metadata registry reload where the same item is still tagged -
        // must not call addUseCase() a second time, since jeebus.spine has no API to remove the
        // first registration.
        listener.allItemsChanged(List.of());

        verify(entity, times(1)).addUseCase(any());
    }

    @Test
    void allItemsChangedStillBindsAGenuinelyNewlyTaggedItem() {
        when(itemRegistry.getItems()).thenReturn(List.of());
        EEBusChangeListener listener = new EEBusChangeListener(itemRegistry, metadataRegistry, eventPublisher, entity);
        verify(entity, times(0)).addUseCase(any());

        Item item = new NumberItem(ITEM_NAME);
        when(itemRegistry.getItems()).thenReturn(List.of(item));
        when(metadataRegistry.get(METADATA_KEY)).thenReturn(lpcMetadata(Map.of()));
        listener.allItemsChanged(List.of());

        verify(entity, times(1)).addUseCase(any());
    }

    @Test
    void metadataUpdateOnAnAlreadyBoundItemDoesNotReRegisterTheUseCase() {
        Item item = new NumberItem(ITEM_NAME);
        when(itemRegistry.getItems()).thenReturn(List.of(item));
        Metadata original = lpcMetadata(Map.of("nominalMax", 4200));
        when(metadataRegistry.get(METADATA_KEY)).thenReturn(original);

        new EEBusChangeListener(itemRegistry, metadataRegistry, eventPublisher, entity);
        verify(entity, times(1)).addUseCase(any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<RegistryChangeListener<Metadata>> captor = ArgumentCaptor.forClass(RegistryChangeListener.class);
        verify(metadataRegistry).addRegistryChangeListener(captor.capture());

        // Simulates editing nominalMax/failsafeLimit/failsafeDuration on the already-bound item -
        // the add-on has no way to reconfigure an already-attached use case, so this must stay a
        // no-op rather than registering a second one.
        Metadata updated = lpcMetadata(Map.of("nominalMax", 11000));
        captor.getValue().updated(original, updated);

        verify(entity, times(1)).addUseCase(any());
    }

    @Test
    void distinctLpcAndLppItemsBothGetTheirOwnUseCase() {
        Item lpcItem = new NumberItem(ITEM_NAME);
        Item lppItem = new NumberItem("Test_Lpp_Limit");
        MetadataKey lppKey = new MetadataKey("eebus", "Test_Lpp_Limit");
        when(itemRegistry.getItems()).thenReturn(List.of(lpcItem, lppItem));
        when(metadataRegistry.get(METADATA_KEY)).thenReturn(lpcMetadata(Map.of()));
        when(metadataRegistry.get(lppKey)).thenReturn(new Metadata(lppKey, "lpp", Map.of()));

        new EEBusChangeListener(itemRegistry, metadataRegistry, eventPublisher, entity);

        verify(entity, times(2)).addUseCase(any());
    }
}
