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
package org.openhab.binding.wemo.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.types.UDN;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.net.mac.MacResolver;
import org.openhab.core.thing.Thing;

/**
 * Tests for {@link WemoDiscoveryParticipant}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class WemoDiscoveryParticipantTest {

    private static class TestableWemoDiscoveryParticipant extends WemoDiscoveryParticipant {
        private @Nullable DiscoveryResult discoveredResult;

        TestableWemoDiscoveryParticipant(MacResolver macResolver) {
            super(macResolver);
        }

        @Override
        protected void thingDiscovered(DiscoveryResult result) {
            discoveredResult = result;
        }
    }

    @Test
    void discoveryResultIncludesMacAddress() throws Exception {
        RemoteDevice device = createDevice();
        MacResolver macResolver = Objects.requireNonNull(mock(MacResolver.class));
        when(macResolver.resolveMac("192.168.1.66")).thenReturn(CompletableFuture.completedFuture("60:38:E0:61:1C:85"));

        DiscoveryResult result = Objects.requireNonNull(new WemoDiscoveryParticipant(macResolver).createResult(device));

        assertThat(result.getProperties().get(Thing.PROPERTY_MAC_ADDRESS), is("60:38:E0:61:1C:85"));
        assertThat(result.getProperties().containsKey("ipAddress"), is(false));
        assertThat(result.getLabel(), is("Left lamp"));
    }

    @Test
    void discoveryResultIsCreatedWhenMacAddressCannotBeResolved() throws Exception {
        RemoteDevice device = createDevice();
        MacResolver macResolver = Objects.requireNonNull(mock(MacResolver.class));
        when(macResolver.resolveMac("192.168.1.66")).thenReturn(CompletableFuture.completedFuture(null));

        DiscoveryResult result = Objects.requireNonNull(new WemoDiscoveryParticipant(macResolver).createResult(device));

        assertThat(result.getProperties().containsKey(Thing.PROPERTY_MAC_ADDRESS), is(false));
        assertThat(result.getLabel(), is("Left lamp"));
    }

    @Test
    void discoveryResultIsDeferredUntilMacAddressIsResolved() throws Exception {
        RemoteDevice device = createDevice();
        MacResolver macResolver = Objects.requireNonNull(mock(MacResolver.class));
        CompletableFuture<@Nullable String> macFuture = new CompletableFuture<>();
        when(macResolver.resolveMac("192.168.1.66")).thenReturn(macFuture);
        TestableWemoDiscoveryParticipant participant = new TestableWemoDiscoveryParticipant(macResolver);

        assertNull(participant.createResult(device));

        macFuture.complete("60:38:E0:61:1C:85");

        DiscoveryResult result = participant.discoveredResult;
        assertNotNull(result);
        assertThat(result.getProperties().get(Thing.PROPERTY_MAC_ADDRESS), is("60:38:E0:61:1C:85"));
    }

    @Test
    void deferredDiscoveryContinuesWhenMacAddressCannotBeResolved() throws Exception {
        RemoteDevice device = createDevice();
        MacResolver macResolver = Objects.requireNonNull(mock(MacResolver.class));
        CompletableFuture<@Nullable String> macFuture = new CompletableFuture<>();
        when(macResolver.resolveMac("192.168.1.66")).thenReturn(macFuture);
        TestableWemoDiscoveryParticipant participant = new TestableWemoDiscoveryParticipant(macResolver);

        assertNull(participant.createResult(device));

        macFuture.complete(null);

        DiscoveryResult result = participant.discoveredResult;
        assertNotNull(result);
        assertThat(result.getProperties().containsKey(Thing.PROPERTY_MAC_ADDRESS), is(false));
    }

    private RemoteDevice createDevice() throws Exception {
        RemoteDevice device = Objects.requireNonNull(mock(RemoteDevice.class));
        RemoteDeviceIdentity identity = Objects.requireNonNull(mock(RemoteDeviceIdentity.class));
        DeviceDetails details = Objects.requireNonNull(mock(DeviceDetails.class));
        ManufacturerDetails manufacturer = Objects.requireNonNull(mock(ManufacturerDetails.class));
        ModelDetails model = Objects.requireNonNull(mock(ModelDetails.class));
        String descriptorUrl = "http://192.168.1.66:49153/setup.xml";

        when(device.getIdentity()).thenReturn(identity);
        when(identity.getUdn()).thenReturn(new UDN("Socket-1_0-221642K010011C"));
        when(identity.getDescriptorURL()).thenReturn(URI.create(descriptorUrl).toURL());
        when(device.getDetails()).thenReturn(details);
        when(details.getFriendlyName()).thenReturn("Left lamp");
        when(details.getManufacturerDetails()).thenReturn(manufacturer);
        when(manufacturer.getManufacturer()).thenReturn("Belkin International Inc.");
        when(details.getModelDetails()).thenReturn(model);
        when(model.getModelName()).thenReturn("Socket");
        return device;
    }
}
