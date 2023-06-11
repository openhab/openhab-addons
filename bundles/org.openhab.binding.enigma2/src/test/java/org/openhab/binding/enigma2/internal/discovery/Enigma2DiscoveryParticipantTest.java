/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.internal.discovery;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.net.Inet4Address;
import java.net.InetAddress;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.Enigma2HttpClient;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link Enigma2DiscoveryParticipantTest} class is responsible for testing {@link Enigma2DiscoveryParticipant}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class Enigma2DiscoveryParticipantTest {
    @Nullable
    private ServiceInfo serviceInfo;
    @Nullable
    private Enigma2HttpClient enigma2HttpClient;
    @Nullable
    private Enigma2DiscoveryParticipant enigma2DiscoveryParticipant;

    @BeforeEach
    public void setUp() {
        enigma2HttpClient = mock(Enigma2HttpClient.class);
        serviceInfo = mock(ServiceInfo.class);
        enigma2DiscoveryParticipant = spy(new Enigma2DiscoveryParticipant());
        when(enigma2DiscoveryParticipant.getEnigma2HttpClient()).thenReturn(requireNonNull(enigma2HttpClient));
    }

    @Test
    public void testGetSupportedThingTypeUIDs() {
        assertThat(enigma2DiscoveryParticipant.getSupportedThingTypeUIDs(),
                contains(Enigma2BindingConstants.THING_TYPE_DEVICE));
    }

    @Test
    public void testGetServiceType() {
        assertThat(enigma2DiscoveryParticipant.getServiceType(), is("_http._tcp.local."));
    }

    @Test
    public void testCreateResult() throws Exception {
        when(serviceInfo.getName()).thenReturn("enigma2");
        when(enigma2HttpClient.get("http://192.168.10.3/web/about"))
                .thenReturn("<e2abouts><e2about><e2enigmaversion>2020-01-11</e2enigmaversion></e2about></e2abouts>");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getAllByName("192.168.10.3")[0] });
        DiscoveryResult discoveryResult = enigma2DiscoveryParticipant.createResult(requireNonNull(serviceInfo));
        assertThat(discoveryResult, is(notNullValue()));
        assertThat(discoveryResult.getLabel(), is("enigma2"));
        assertThat(discoveryResult.getThingUID(),
                is(new ThingUID(Enigma2BindingConstants.THING_TYPE_DEVICE, "192_168_10_3")));
        assertThat(discoveryResult.getProperties(), is(notNullValue()));
        assertThat(discoveryResult.getProperties(), hasEntry(Enigma2BindingConstants.CONFIG_HOST, "192.168.10.3"));
        assertThat(discoveryResult.getProperties(), hasEntry(Enigma2BindingConstants.CONFIG_REFRESH, 5));
        assertThat(discoveryResult.getProperties(), hasEntry(Enigma2BindingConstants.CONFIG_TIMEOUT, 5));
    }

    @Test
    public void testCreateResultNotFound() throws Exception {
        when(enigma2HttpClient.get("http://192.168.10.3/web/about")).thenReturn("any");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getAllByName("192.168.10.3")[0] });
        assertThat(enigma2DiscoveryParticipant.createResult(requireNonNull(serviceInfo)), is(nullValue()));
    }

    @Test
    public void testGetThingUID() throws Exception {
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getAllByName("192.168.10.3")[0] });
        assertThat(enigma2DiscoveryParticipant.getThingUID(requireNonNull(serviceInfo)),
                is(new ThingUID(Enigma2BindingConstants.THING_TYPE_DEVICE, "192_168_10_3")));
    }

    @Test
    public void testGetThingUIDTwoAddresses() throws Exception {
        when(serviceInfo.getName()).thenReturn("enigma2");
        Inet4Address[] addresses = { (Inet4Address) InetAddress.getAllByName("192.168.10.3")[0],
                (Inet4Address) InetAddress.getAllByName("192.168.10.4")[0] };
        when(serviceInfo.getInet4Addresses()).thenReturn(addresses);
        assertThat(enigma2DiscoveryParticipant.getThingUID(requireNonNull(serviceInfo)),
                is(new ThingUID(Enigma2BindingConstants.THING_TYPE_DEVICE, "192_168_10_3")));
    }
}
