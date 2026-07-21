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
package org.openhab.binding.atv.internal.discovery;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.atv.internal.AtvBindingConstants.*;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link AtvDiscoveryParticipant}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvDiscoveryParticipantTest {

    private static final String MAC = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_ID = "aabbccddeeff";

    private @NonNullByDefault({}) AtvDiscoveryParticipant participant;
    private @NonNullByDefault({}) ServiceInfo serviceInfo;

    @BeforeEach
    public void setUp() {
        participant = new AtvDiscoveryParticipant();
        serviceInfo = mock(ServiceInfo.class);
    }

    @Test
    public void testGetServiceType() {
        assertThat(participant.getServiceType(), is(MDNS_AIRPLAY));
    }

    @Test
    public void testGetSupportedThingTypeUIDs() {
        assertThat(participant.getSupportedThingTypeUIDs(), containsInAnyOrder(THING_TYPE_APPLETV, THING_TYPE_SPEAKER));
    }

    @Test
    public void testGetThingUIDForAppleTv() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);

        ThingUID uid = requireNonNull(participant.getThingUID(serviceInfo));

        assertThat(uid, is(new ThingUID(THING_TYPE_APPLETV, MAC_ID)));
    }

    @Test
    public void testGetThingUIDForSpeaker() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AudioAccessory5,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);

        ThingUID uid = requireNonNull(participant.getThingUID(serviceInfo));

        assertThat(uid, is(new ThingUID(THING_TYPE_SPEAKER, MAC_ID)));
    }

    @Test
    public void testGetThingUIDWithoutModelDefaultsToSpeaker() {
        when(serviceInfo.getPropertyString("model")).thenReturn(null);
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);

        ThingUID uid = requireNonNull(participant.getThingUID(serviceInfo));

        assertThat(uid, is(new ThingUID(THING_TYPE_SPEAKER, MAC_ID)));
    }

    @Test
    public void testGetThingUIDIsStableFromMac() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);

        ThingUID first = participant.getThingUID(serviceInfo);
        ThingUID second = participant.getThingUID(serviceInfo);

        assertThat(first, is(second));
    }

    @Test
    public void testGetThingUIDReturnsNullWhenDeviceIdMissing() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(null);

        assertThat(participant.getThingUID(serviceInfo), is(nullValue()));
    }

    @Test
    public void testGetThingUIDReturnsNullWhenDeviceIdBlank() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn("   ");

        assertThat(participant.getThingUID(serviceInfo), is(nullValue()));
    }

    @Test
    public void testCreateResultForAppleTv() throws Exception {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getName()).thenReturn("Living Room");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName("192.168.1.42") });

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_APPLETV, MAC_ID)));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_APPLETV));
        assertThat(result.getLabel(), is("Living Room (AppleTV14,1)"));
        assertThat(result.getRepresentationProperty(), is(CONFIG_MAC));
        assertThat(result.getProperties(), hasEntry(CONFIG_MAC, MAC));
        assertThat(result.getProperties(), hasEntry(CONFIG_HOST, "192.168.1.42"));
        assertThat(result.getProperties(), hasEntry(PROPERTY_MODEL, "AppleTV14,1"));
    }

    @Test
    public void testCreateResultForSpeaker() throws Exception {
        when(serviceInfo.getPropertyString("model")).thenReturn("AudioAccessory5,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getName()).thenReturn("Kitchen HomePod");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName("192.168.1.50") });

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_SPEAKER, MAC_ID)));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_SPEAKER));
        assertThat(result.getProperties(), hasEntry(PROPERTY_MODEL, "AudioAccessory5,1"));
    }

    @Test
    public void testCreateResultWithoutModelHasNoModelProperty() throws Exception {
        when(serviceInfo.getPropertyString("model")).thenReturn(null);
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getName()).thenReturn("Mystery Device");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName("192.168.1.60") });

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getThingTypeUID(), is(THING_TYPE_SPEAKER));
        assertThat(result.getProperties(), not(hasKey(PROPERTY_MODEL)));
        assertThat(result.getLabel(), is("Mystery Device"));
    }

    @Test
    public void testCreateResultIgnoresAppleComputers() throws Exception {
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getName()).thenReturn("Someone's Mac");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName("192.168.1.70") });

        for (String model : new String[] { "Mac16,12", "MacBookPro18,1", "MacBookAir10,1", "iMac21,1", "Macmini9,1" }) {
            when(serviceInfo.getPropertyString("model")).thenReturn(model);
            assertThat("expected " + model + " to be filtered", participant.createResult(serviceInfo), is(nullValue()));
        }
    }

    @Test
    public void testCreateResultReturnsNullWhenDeviceIdMissing() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(null);

        assertThat(participant.createResult(serviceInfo), is(nullValue()));
    }

    @Test
    public void testCreateResultReturnsNullWhenNoAddressAtAll() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[] {});
        when(serviceInfo.getInet6Addresses()).thenReturn(new Inet6Address[] {});

        assertThat(participant.createResult(serviceInfo), is(nullValue()));
    }

    @Test
    public void testCreateResultFallsBackToIPv6WhenNoIPv4() throws Exception {
        Inet6Address ipv6 = (Inet6Address) InetAddress.getByName("2001:db8::42");
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getName()).thenReturn("Living Room");
        when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[] {});
        when(serviceInfo.getInet6Addresses()).thenReturn(new Inet6Address[] { ipv6 });

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getProperties(), hasEntry(CONFIG_HOST, ipv6.getHostAddress()));
    }

    @Test
    public void testCreateResultFallsBackToHostnameWhenNoAddress() {
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getName()).thenReturn("Living Room");
        when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[] {});
        when(serviceInfo.getInet6Addresses()).thenReturn(new Inet6Address[] {});
        when(serviceInfo.getServer()).thenReturn("TV.local.");

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getProperties(), hasEntry(CONFIG_HOST, "TV.local"));
    }

    @Test
    public void testCreateResultSkipsLinkLocalIPv6WithoutServer() throws Exception {
        Inet6Address linkLocal = (Inet6Address) InetAddress.getByName("fe80::1");
        when(serviceInfo.getPropertyString("model")).thenReturn("AppleTV14,1");
        when(serviceInfo.getPropertyString("deviceid")).thenReturn(MAC);
        when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[] {});
        when(serviceInfo.getInet6Addresses()).thenReturn(new Inet6Address[] { linkLocal });

        assertThat(participant.createResult(serviceInfo), is(nullValue()));
    }
}
