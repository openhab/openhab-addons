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
package org.openhab.binding.upnpcontrol.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.types.UDN;
import org.jupnp.registry.Registry;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.upnpcontrol.internal.handler.UpnpHandler;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.net.NetworkAddressService;

/**
 * Tests for {@link UpnpControlHandlerFactory}.
 *
 * @author Holger Friedrich - Initial contribution
 */
@SuppressWarnings({ "null" })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class UpnpControlHandlerFactoryTest {

    private static final String ROOT_UDN = "a765b19b-059b-1a35-0080-000678efac3f";
    private static final String RENDERER_UDN = "7502937b-e137-1685-0080-000678efac3f";

    private @NonNullByDefault({}) @Mock UpnpIOService upnpIOService;
    private @NonNullByDefault({}) @Mock UpnpService upnpService;
    private @NonNullByDefault({}) @Mock Registry registry;
    private @NonNullByDefault({}) @Mock AudioHTTPServer audioHTTPServer;
    private @NonNullByDefault({}) @Mock NetworkAddressService networkAddressService;
    private @NonNullByDefault({}) @Mock UpnpDynamicStateDescriptionProvider stateDescriptionProvider;
    private @NonNullByDefault({}) @Mock UpnpDynamicCommandDescriptionProvider commandDescriptionProvider;
    private @NonNullByDefault({}) @Mock UpnpHandler handler;

    private @NonNullByDefault({}) UpnpControlHandlerFactory factory;
    private @NonNullByDefault({}) RemoteDevice renderer;
    private @NonNullByDefault({}) RemoteDevice root;

    @BeforeEach
    public void setUp() {
        when(upnpService.getRegistry()).thenReturn(registry);
        factory = new UpnpControlHandlerFactory(upnpIOService, upnpService, audioHTTPServer, networkAddressService,
                stateDescriptionProvider, commandDescriptionProvider, Map.of());

        // like an AV receiver: a root device without services, with an embedded media renderer
        renderer = device(RENDERER_UDN);
        root = device(ROOT_UDN, renderer);
    }

    @Test
    public void handlerIsConfiguredFromEmbeddedDeviceKnownToTheRegistry() {
        when(registry.getRemoteDevice(new UDN(RENDERER_UDN), false)).thenReturn(renderer);

        factory.addHandler(RENDERER_UDN, handler);

        verify(handler).updateDeviceConfig(renderer);
    }

    @Test
    public void handlerForUnknownDeviceIsNotConfigured() {
        factory.addHandler(RENDERER_UDN, handler);

        verify(handler, never()).updateDeviceConfig(any());
    }

    @Test
    public void addedRootDeviceConfiguresHandlerOfEmbeddedDeviceOnce() {
        factory.addHandler(RENDERER_UDN, handler);

        factory.remoteDeviceAdded(registry, root);

        verify(handler, times(1)).updateDeviceConfig(renderer);
    }

    @Test
    public void updatedRootDeviceConfiguresHandlerOfEmbeddedDeviceOnce() {
        factory.addHandler(RENDERER_UDN, handler);

        factory.remoteDeviceUpdated(registry, root);

        verify(handler, times(1)).updateDeviceConfig(renderer);
    }

    private static RemoteDevice device(String udn, RemoteDevice... embeddedDevices) {
        RemoteDeviceIdentity identity = mock(RemoteDeviceIdentity.class);
        when(identity.getUdn()).thenReturn(new UDN(udn));
        RemoteDevice device = mock(RemoteDevice.class);
        when(device.getIdentity()).thenReturn(identity);
        when(device.getEmbeddedDevices()).thenReturn(embeddedDevices);
        return device;
    }
}
