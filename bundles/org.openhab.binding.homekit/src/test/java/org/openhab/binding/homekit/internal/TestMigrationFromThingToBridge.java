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
package org.openhab.binding.homekit.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.homekit.internal.dto.Accessory;
import org.openhab.binding.homekit.internal.handler.HomekitAccessoryHandler;
import org.openhab.binding.homekit.internal.persistence.HomekitKeyStore;
import org.openhab.binding.homekit.internal.persistence.HomekitTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.framework.Bundle;

/**
 * Tests for the migration from a Thing to a Bridge.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class TestMigrationFromThingToBridge {
    /**
     * Test subclass to allow injection of a test scheduler
     */
    protected class TestHomekitAccessoryHandler extends HomekitAccessoryHandler {
        private final ScheduledExecutorService injectedTestScheduler;

        public TestHomekitAccessoryHandler(Thing thing, HomekitTypeProvider typeProvider,
                ChannelTypeRegistry channelTypeRegistry, ChannelGroupTypeRegistry channelGroupTypeRegistry,
                HomekitKeyStore keyStore, TranslationProvider i18nProvider, Bundle bundle, ThingRegistry thingRegistry,
                ScheduledExecutorService scheduler) {
            super(thing, typeProvider, channelTypeRegistry, channelGroupTypeRegistry, keyStore, i18nProvider, bundle,
                    thingRegistry);
            this.injectedTestScheduler = scheduler;
        }

        @Override
        protected ScheduledExecutorService getScheduler() {
            return injectedTestScheduler;
        }
    }

    private Accessory createAccessory(Long aid) {
        Accessory accessory = new Accessory();
        accessory.aid = aid;
        accessory.services = new ArrayList<>();
        return accessory;
    }

    private void injectField(Object target, String fieldName, Object value) {
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            } catch (IllegalAccessException e) {
                fail("Failed to inject field:  " + fieldName, e);
            }
        }
        fail("Could not find field '" + fieldName + "' in class hierarchy");
    }

    private void invokeMethod(Object target, String methodName) {
        Class<?> current = target.getClass();
        while (current != null && current != Object.class) {
            try {
                Method method = current.getDeclaredMethod(methodName);
                method.setAccessible(true);
                method.invoke(target);
                return;
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            } catch (Exception e) {
                fail("Failed to invoke method: " + methodName, e);
            }
        }
        fail("Could not find method '" + methodName + "' in class hierarchy");
    }

    private @Nullable Object getField(Object target, String name) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field f = type.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private HomekitAccessoryHandler createHandler(Map<Long, Accessory> accessories, ThingTypeUID thingTypeUID,
            String thingId, List<Runnable> capturedRunnables, ThingRegistry thingRegistry,
            ThingHandlerCallback callback) {

        Thing thing = mock(Thing.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> scheduledFuture = mock(ScheduledFuture.class);
        HomekitTypeProvider typeProvider = mock(HomekitTypeProvider.class);
        ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
        ChannelGroupTypeRegistry channelGroupTypeRegistry = mock(ChannelGroupTypeRegistry.class);
        HomekitKeyStore keyStore = mock(HomekitKeyStore.class);
        TranslationProvider translationProvider = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);

        ThingUID thingUID = new ThingUID(thingTypeUID, thingId);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(thingTypeUID);
        when(thing.getLabel()).thenReturn("Test Accessory");
        when(thing.getLocation()).thenReturn("Living Room");
        when(thing.getBridgeUID()).thenReturn(null);

        Configuration config = new Configuration();
        config.put(HomekitBindingConstants.CONFIG_IP_ADDRESS, "192.168.1.100:1234");
        config.put(HomekitBindingConstants.CONFIG_UNIQUE_ID, "test-unique-id");
        config.put(HomekitBindingConstants.CONFIG_REFRESH_INTERVAL, BigDecimal.valueOf(60));
        when(thing.getConfiguration()).thenReturn(config);

        Map<String, String> properties = new HashMap<>();
        properties.put(HomekitBindingConstants.PROPERTY_UNIQUE_ID, "test-unique-property-id");
        properties.put(HomekitBindingConstants.PROPERTY_ACCESSORY_CATEGORY, "test-category");
        when(thing.getProperties()).thenReturn(properties);

        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);
        when(callback.getBridge(ArgumentMatchers.any(ThingUID.class))).thenReturn(null);

        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            capturedRunnables.add(runnable);
            return scheduledFuture;
        }).when(scheduler).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), ArgumentMatchers.any(TimeUnit.class));

        HomekitAccessoryHandler handler = new TestHomekitAccessoryHandler(thing, typeProvider, channelTypeRegistry,
                channelGroupTypeRegistry, keyStore, translationProvider, bundle, thingRegistry, scheduler);

        // Inject accessories map
        injectField(handler, "accessories", accessories);

        // Set callback and inject scheduler
        handler.setCallback(callback);

        // Ensure isBridgedAccessory is false (stand-alone accessory, not a child of a bridge)
        injectField(handler, "isBridgedAccessory", false);

        return handler;
    }

    @Test
    public void testMigrationNotTriggeredForSingleAccessory() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(0, capturedRunnables.size(), "No migration task should be scheduled for single accessory");
    }

    @Test
    public void testMigrationNotTriggeredForBridgeType() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_BRIDGE,
                "test-bridge", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(0, capturedRunnables.size(), "No migration task should be scheduled for bridge type");
    }

    @Test
    public void testMigrationNotTriggeredForBridgedAccessoryType() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories,
                HomekitBindingConstants.THING_TYPE_BRIDGED_ACCESSORY, "test-bridged-accessory", capturedRunnables,
                thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(0, capturedRunnables.size(), "No migration task should be scheduled for bridged accessory type");
    }

    @Test
    public void testMigrationTriggeredForMultipleAccessories() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");
    }

    @Test
    public void testBridgeInheritsThingProperties() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be captured");

        capturedRunnables.get(0).run();

        verify(thingRegistry).remove(ArgumentMatchers.any(ThingUID.class));

        ArgumentCaptor<Bridge> addCaptor = ArgumentCaptor.forClass(Bridge.class);
        verify(thingRegistry).add(addCaptor.capture());

        Bridge newBridge = addCaptor.getValue();
        assertEquals(HomekitBindingConstants.THING_TYPE_BRIDGE, newBridge.getThingTypeUID());
        assertEquals("Test Accessory", newBridge.getLabel());
        assertEquals("Living Room", newBridge.getLocation());
        assertEquals(handler.getThing().getProperties(), newBridge.getProperties());
        assertEquals(handler.getThing().getConfiguration().getProperties(),
                newBridge.getConfiguration().getProperties());
    }

    @Test
    public void testMigratingFlagUpdatedDuringMigration() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be captured");

        Object migrating = assertDoesNotThrow(() -> getField(handler, "migrating"));
        assertTrue(migrating instanceof AtomicBoolean);
        assertTrue(((AtomicBoolean) migrating).get());

        capturedRunnables.get(0).run();

        assertFalse(((AtomicBoolean) migrating).get());
    }

    @Test
    public void testStatusUpdatedDuringMigration() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(ArgumentMatchers.any(Thing.class), statusCaptor.capture());

        boolean foundMigrationStatus = statusCaptor.getAllValues().stream()
                .anyMatch(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_PENDING);

        assertTrue(foundMigrationStatus, "Status should be OFFLINE with CONFIGURATION_PENDING during migration");
    }

    @Test
    public void testUnpairBlockedDuringMigration() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");

        String result = handler.unpair();

        assertTrue(result.contains("ERROR"));
        assertTrue(result.contains("migration in progress"));
    }

    @Test
    public void testMigrationHandlesRegistryError() {
        List<Runnable> capturedRunnables = new ArrayList<>();
        ThingRegistry thingRegistry = mock(ThingRegistry.class);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.isChannelLinked(ArgumentMatchers.any(ChannelUID.class))).thenReturn(false);

        Map<Long, Accessory> accessories = new ConcurrentHashMap<>();
        accessories.put(1L, createAccessory(1L));
        accessories.put(2L, createAccessory(2L));

        HomekitAccessoryHandler handler = createHandler(accessories, HomekitBindingConstants.THING_TYPE_ACCESSORY,
                "test-accessory", capturedRunnables, thingRegistry, callback);

        invokeMethod(handler, "onConnectedThingAccessoriesLoaded");

        assertEquals(1, capturedRunnables.size(), "Migration task should be scheduled");

        doThrow(new IllegalStateException("Registry error")).when(thingRegistry)
                .add(ArgumentMatchers.any(Bridge.class));

        capturedRunnables.get(0).run();

        verify(thingRegistry).add(ArgumentMatchers.any(Bridge.class));
        verify(thingRegistry, never()).remove(ArgumentMatchers.any(ThingUID.class));

        Object migrating = assertDoesNotThrow(() -> getField(handler, "migrating"));
        assertTrue(migrating instanceof AtomicBoolean);
        assertFalse(((AtomicBoolean) migrating).get());
    }
}
