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
package org.openhab.io.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.common.registry.RegistryChangeListener;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.storage.Storage;
import org.openhab.io.homekit.internal.accessories.AbstractHomekitAccessoryImpl;

import io.github.hapjava.server.impl.HomekitRoot;

/**
 * Tests configuration updates for nested HomeKit accessories.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class HomekitChangeListenerTest {
    private final Map<String, Item> items = new LinkedHashMap<>();
    private final Map<MetadataKey, Metadata> metadata = new HashMap<>();

    private final ItemRegistry itemRegistry = mock(ItemRegistry.class);
    private final MetadataRegistry metadataRegistry = mock(MetadataRegistry.class);
    private final Storage<Object> storage = mock();
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final ScheduledFuture<?> future = mock(ScheduledFuture.class);
    private final Clock clock = mock(Clock.class);
    private final HomekitRoot bridge = mock(HomekitRoot.class);

    private @Nullable HomekitChangeListener changeListener;
    private @Nullable Runnable pendingAction;
    private long now;

    @BeforeEach
    void setUp() throws ItemNotFoundException {
        when(itemRegistry.getItems()).thenAnswer(invocation -> new ArrayList<>(items.values()));
        when(itemRegistry.get(anyString())).thenAnswer(invocation -> items.get(invocation.getArgument(0)));
        when(itemRegistry.getItem(anyString())).thenAnswer(invocation -> {
            String name = invocation.getArgument(0);
            Item item = items.get(name);
            if (item == null) {
                throw new ItemNotFoundException(name);
            }
            return item;
        });
        when(metadataRegistry.get(any(MetadataKey.class)))
                .thenAnswer(invocation -> metadata.get(invocation.getArgument(0)));
        when(clock.millis()).thenAnswer(invocation -> now);
        doAnswer(invocation -> {
            pendingAction = invocation.getArgument(0);
            return future;
        }).when(scheduler).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @AfterEach
    void tearDown() {
        HomekitChangeListener listener = changeListener;
        if (listener != null) {
            listener.stop();
        }
    }

    @Test
    void addingNestedCharacteristicRefreshesRoot() throws Exception {
        GroupItem root = irrigation("irrigation");
        GroupItem valve = valve("valve", root);
        HomekitChangeListener listener = start();
        AbstractHomekitAccessoryImpl before = accessory(root);
        int revision = listener.getConfigurationRevision();

        NumberItem duration = add(new NumberItem("duration"), valve, "Valve.Duration");
        listener.added(duration);
        flush();

        assertRefreshed(root, before);
        assertEquals(revision + 1, listener.getConfigurationRevision());
        verify(bridge).setConfigurationIndex(revision + 1);
    }

    @Test
    void removingNestedCharacteristicRefreshesRoot() {
        GroupItem root = irrigation("irrigation");
        GroupItem valve = valve("valve", root);
        NumberItem duration = add(new NumberItem("duration"), valve, "Valve.Duration");
        HomekitChangeListener listener = start();
        AbstractHomekitAccessoryImpl before = accessory(root);
        int revision = listener.getConfigurationRevision();

        valve.removeMember(duration);
        items.remove(duration.getName());
        listener.removed(duration);
        flush();

        assertRefreshed(root, before);
        assertEquals(revision + 1, listener.getConfigurationRevision());
    }

    @Test
    void changingNestedMetadataRefreshesRoot() {
        GroupItem root = irrigation("irrigation");
        GroupItem valve = valve("valve", root);
        NumberItem duration = add(new NumberItem("duration"), valve, "Valve.Duration");
        HomekitChangeListener listener = start();
        AbstractHomekitAccessoryImpl before = accessory(root);
        int revision = listener.getConfigurationRevision();
        ArgumentCaptor<RegistryChangeListener<Metadata>> captor = ArgumentCaptor.captor();
        verify(metadataRegistry).addRegistryChangeListener(captor.capture());

        MetadataKey key = new MetadataKey("homekit", duration.getName());
        Metadata replacement = new Metadata(key, "Valve.RemainingDuration", null);
        Metadata old = Objects.requireNonNull(metadata.put(key, replacement));
        captor.getValue().updated(old, replacement);
        flush();

        assertRefreshed(root, before);
        assertEquals(revision + 1, listener.getConfigurationRevision());
    }

    @Test
    void movingNestedCharacteristicRefreshesBothRoots() {
        GroupItem firstRoot = irrigation("firstIrrigation");
        GroupItem firstValve = valve("firstValve", firstRoot);
        GroupItem secondRoot = irrigation("secondIrrigation");
        GroupItem secondValve = valve("secondValve", secondRoot);
        NumberItem oldDuration = add(new NumberItem("duration"), firstValve, "Valve.Duration");
        HomekitChangeListener listener = start();
        AbstractHomekitAccessoryImpl firstBefore = accessory(firstRoot);
        AbstractHomekitAccessoryImpl secondBefore = accessory(secondRoot);
        int revision = listener.getConfigurationRevision();

        firstValve.removeMember(oldDuration);
        NumberItem newDuration = add(new NumberItem("duration"), secondValve, "Valve.Duration");
        listener.updated(oldDuration, newDuration);
        flush();

        assertRefreshed(firstRoot, firstBefore);
        assertRefreshed(secondRoot, secondBefore);
        assertEquals(revision + 1, listener.getConfigurationRevision());
    }

    @Test
    void unchangedNestedDefinitionDoesNotAdvanceRevision() {
        GroupItem root = irrigation("irrigation");
        GroupItem valve = valve("valve", root);
        NumberItem duration = add(new NumberItem("duration"), valve, "Valve.Duration");
        HomekitChangeListener listener = start();
        String before = accessory(root).toJson();
        int revision = listener.getConfigurationRevision();

        listener.updated(duration, duration);
        flush();

        assertEquals(before, accessory(root).toJson());
        assertEquals(revision, listener.getConfigurationRevision());
    }

    @Test
    void cyclicAccessoryGroupsDoNotRepeatTraversal() {
        GroupItem root = irrigation("irrigation");
        GroupItem valve = valve("valve", root);
        HomekitChangeListener listener = start();
        root.addGroupName(valve.getName());
        AtomicInteger lookups = new AtomicInteger();
        when(itemRegistry.get(anyString())).thenAnswer(invocation -> {
            assertTrue(lookups.incrementAndGet() <= 10, "Accessory group traversal did not terminate");
            return items.get(invocation.getArgument(0));
        });

        listener.added(valve);

        assertNotNull(pendingAction);
    }

    private HomekitChangeListener start() {
        HomekitChangeListener listener = new HomekitChangeListener(itemRegistry, new HomekitSettings(),
                metadataRegistry, storage, 1, scheduler, clock);
        changeListener = listener;
        listener.setBridge(bridge);
        clearInvocations(bridge);
        return listener;
    }

    private void flush() {
        now += 2000;
        Runnable action = Objects.requireNonNull(pendingAction, "No accessory update was scheduled");
        pendingAction = null;
        action.run();
    }

    private AbstractHomekitAccessoryImpl accessory(GroupItem root) {
        return (AbstractHomekitAccessoryImpl) Objects
                .requireNonNull(Objects.requireNonNull(changeListener).getAccessories().get(root.getName()));
    }

    private void assertRefreshed(GroupItem root, AbstractHomekitAccessoryImpl before) {
        AbstractHomekitAccessoryImpl after = accessory(root);
        assertNotEquals(before.toJson(), after.toJson());
        assertEquals(before.getId(), after.getId());
        verify(bridge).removeAccessory(before);
        verify(bridge).addAccessory(after);
    }

    private GroupItem irrigation(String name) {
        GroupItem root = add(new GroupItem(name), null, "IrrigationSystem");
        add(new SwitchItem(name + "_active"), root, "IrrigationSystem.Active,IrrigationSystem.InUseStatus");
        add(new StringItem(name + "_program"), root, "IrrigationSystem.ProgramMode");
        return root;
    }

    private GroupItem valve(String name, GroupItem root) {
        GroupItem valve = add(new GroupItem(name), root, "Valve");
        MetadataKey key = new MetadataKey("homekit", name);
        metadata.put(key, new Metadata(key, "Valve", Map.of("ServiceIndex", BigDecimal.ONE)));
        add(new SwitchItem(name + "_active"), valve, "Valve.ActiveStatus,Valve.InUseStatus");
        return valve;
    }

    private <T extends GenericItem> T add(T item, @Nullable GroupItem parent, String tag) {
        item.setLabel(item.getName());
        if (parent != null) {
            item.addGroupName(parent.getName());
            parent.addMember(item);
        }
        items.put(item.getName(), item);
        MetadataKey key = new MetadataKey("homekit", item.getName());
        metadata.put(key, new Metadata(key, tag, null));
        return item;
    }
}
