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
package org.openhab.binding.ondilo.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ondilo.internal.OndiloBindingConstants.THING_TYPE_BRIDGE;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

/**
 * Tests for {@link OndiloHandlerFactory}'s per-bridge discovery service registration bookkeeping.
 *
 * @author Michael Weger - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class OndiloHandlerFactoryTest {

    private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

    private @Nullable OndiloHandlerFactory factory;

    @BeforeEach
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setUp() {
        BundleContext bundleContextMock = mock(BundleContext.class);
        // Route each registered discovery service back through getService(), so unregistering it can genuinely
        // stop its background scheduler instead of leaking it for the duration of the test run.
        when(bundleContextMock.registerService(anyString(), any(), any(Dictionary.class))).thenAnswer(invocation -> {
            Object service = invocation.getArgument(1);
            ServiceReference reference = mock(ServiceReference.class);
            ServiceRegistration registration = mock(ServiceRegistration.class);
            when(registration.getReference()).thenReturn(reference);
            when(bundleContextMock.getService(reference)).thenReturn(service);
            registrations.add(registration);
            return registration;
        });

        ComponentContext componentContextMock = mock(ComponentContext.class);
        when(componentContextMock.getBundleContext()).thenReturn(bundleContextMock);

        factory = new OndiloHandlerFactory(componentContextMock, mock(OAuthFactory.class), mock(LocaleProvider.class));
    }

    private static Bridge mockBridge(String id) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID(THING_TYPE_BRIDGE, id));
        when(bridge.getThingTypeUID()).thenReturn(THING_TYPE_BRIDGE);
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of()));
        return bridge;
    }

    @Test
    public void removeHandlerOnlyUnregistersTheMatchingBridge() {
        ThingHandler handler1 = factory.createHandler(mockBridge("bridge1"));
        ThingHandler handler2 = factory.createHandler(mockBridge("bridge2"));
        assertNotNull(handler1);
        assertNotNull(handler2);
        assertEquals(2, registrations.size());

        ServiceRegistration<?> registration1 = registrations.get(0);
        ServiceRegistration<?> registration2 = registrations.get(1);

        factory.removeHandler(handler1);

        verify(registration1).unregister();
        verify(registration2, never()).unregister();

        factory.removeHandler(handler2);

        verify(registration2).unregister();
    }
}
