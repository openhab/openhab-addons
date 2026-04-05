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
package org.openhab.binding.ddwrt.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DDWRTNetworkCache}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class DDWRTNetworkCacheTest {

    private @NonNullByDefault({}) DDWRTNetworkCache cache;

    @BeforeEach
    void setUp() {
        cache = new DDWRTNetworkCache();
    }

    // ---- Wireless Client CRUD ----

    @Test
    void testPutAndGetWirelessClient() {
        DDWRTWirelessClient client = new DDWRTWirelessClient("AA:BB:CC:DD:EE:FF");
        client.setHostname("TestPhone");
        cache.putWirelessClient("AA:BB:CC:DD:EE:FF", client);

        DDWRTWirelessClient result = cache.getWirelessClient("aa:bb:cc:dd:ee:ff");
        assertThat(result, is(notNullValue()));
        assertThat(result.getHostname(), is(equalTo("TestPhone")));
    }

    @Test
    void testGetWirelessClientNormalizesCase() {
        DDWRTWirelessClient client = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        cache.putWirelessClient("AA:BB:CC:DD:EE:FF", client);

        assertThat(cache.getWirelessClient("aa:bb:cc:dd:ee:ff"), is(notNullValue()));
        assertThat(cache.getWirelessClient("AA:BB:CC:DD:EE:FF"), is(notNullValue()));
    }

    @Test
    void testGetWirelessClientByHostname() {
        DDWRTWirelessClient client = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        client.setHostname("Lee-Pixel-8a");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", client);

        DDWRTWirelessClient result = cache.getWirelessClientByHostname("Lee-Pixel-8a");
        assertThat(result, is(notNullValue()));
        assertThat(result.getMac(), is(equalTo("aa:bb:cc:dd:ee:ff")));
    }

    @Test
    void testGetWirelessClientByHostnameSanitizedForm() {
        DDWRTWirelessClient client = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        client.setHostname("Lee-Pixel-8a");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", client);

        // Sanitized form matches thing ID
        DDWRTWirelessClient result = cache.getWirelessClientByHostname("leepixel8a");
        assertThat(result, is(notNullValue()));
        assertThat(result.getMac(), is(equalTo("aa:bb:cc:dd:ee:ff")));
    }

    @Test
    void testGetWirelessClientByHostnameNotFound() {
        assertThat(cache.getWirelessClientByHostname("nonexistent"), is(nullValue()));
    }

    @Test
    void testGetWirelessClientByHostnameEmpty() {
        assertThat(cache.getWirelessClientByHostname(""), is(nullValue()));
    }

    // ---- computeWirelessClient ----

    @Test
    void testComputeWirelessClientCreatesNew() {
        DDWRTWirelessClient result = cache.computeWirelessClient("aa:bb:cc:dd:ee:ff", client -> {
            client.setHostname("NewClient");
            client.setOnline(true);
            return client;
        });

        assertThat(result.getHostname(), is(equalTo("NewClient")));
        assertThat(result.isOnline(), is(true));
        assertThat(cache.getWirelessClient("aa:bb:cc:dd:ee:ff"), is(notNullValue()));
    }

    @Test
    void testComputeWirelessClientUpdatesExisting() {
        DDWRTWirelessClient original = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        original.setHostname("Phone");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", original);

        cache.computeWirelessClient("aa:bb:cc:dd:ee:ff", client -> {
            client.setIpAddress("192.168.1.50");
            return client;
        });

        DDWRTWirelessClient updated = cache.getWirelessClient("aa:bb:cc:dd:ee:ff");
        assertThat(updated, is(notNullValue()));
        assertThat(updated.getHostname(), is(equalTo("Phone")));
        assertThat(updated.getIpAddress(), is(equalTo("192.168.1.50")));
    }

    // ---- MAC randomization merge ----

    @Test
    void testMergeRandomizedMac() {
        DDWRTWirelessClient oldClient = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        oldClient.setHostname("Phone");
        oldClient.setApMac("11:22:33:44:55:66");
        oldClient.setSsid("MyWiFi");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", oldClient);

        // New MAC appears without hostname (hostname comes from DHCP ACK which triggers merge)
        DDWRTWirelessClient newClient = new DDWRTWirelessClient("11:22:33:44:55:00");
        cache.putWirelessClient("11:22:33:44:55:00", newClient);

        String oldMac = cache.mergeRandomizedMac("11:22:33:44:55:00", "Phone");
        assertThat(oldMac, is(equalTo("aa:bb:cc:dd:ee:ff")));

        // Old entry removed
        assertThat(cache.getWirelessClient("aa:bb:cc:dd:ee:ff"), is(nullValue()));

        // New entry has AP info carried over
        DDWRTWirelessClient merged = cache.getWirelessClient("11:22:33:44:55:00");
        assertThat(merged, is(notNullValue()));
        assertThat(merged.getApMac(), is(equalTo("11:22:33:44:55:66")));
        assertThat(merged.getSsid(), is(equalTo("MyWiFi")));
    }

    @Test
    void testMergeRandomizedMacNoMergeNeeded() {
        DDWRTWirelessClient client = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        client.setHostname("Phone");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", client);

        // Same MAC — no merge
        String result = cache.mergeRandomizedMac("aa:bb:cc:dd:ee:ff", "Phone");
        assertThat(result, is(nullValue()));
    }

    @Test
    void testMergeRandomizedMacEmptyHostname() {
        assertThat(cache.mergeRandomizedMac("aa:bb:cc:dd:ee:ff", ""), is(nullValue()));
    }

    @Test
    void testIsMergedAwayMac() {
        DDWRTWirelessClient oldClient = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        oldClient.setHostname("Phone");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", oldClient);

        // New MAC appears without hostname yet
        DDWRTWirelessClient newClient = new DDWRTWirelessClient("11:22:33:44:55:00");
        cache.putWirelessClient("11:22:33:44:55:00", newClient);

        cache.mergeRandomizedMac("11:22:33:44:55:00", "Phone");

        assertThat(cache.isMergedAwayMac("aa:bb:cc:dd:ee:ff"), is(true));
        assertThat(cache.isMergedAwayMac("11:22:33:44:55:00"), is(false));
    }

    // ---- CacheChangeListener ----

    @Test
    void testListenerNotifiedOnPut() {
        AtomicInteger callCount = new AtomicInteger(0);
        cache.addChangeListener("aa:bb:cc:dd:ee:ff", callCount::incrementAndGet);

        DDWRTWirelessClient client = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", client);

        assertThat(callCount.get(), is(equalTo(1)));
    }

    @Test
    void testListenerNotifiedOnCompute() {
        AtomicInteger callCount = new AtomicInteger(0);
        cache.addChangeListener("aa:bb:cc:dd:ee:ff", callCount::incrementAndGet);

        cache.computeWirelessClient("aa:bb:cc:dd:ee:ff", client -> {
            client.setOnline(true);
            return client;
        });

        assertThat(callCount.get(), is(equalTo(1)));
    }

    @Test
    void testListenerDeduplication() {
        AtomicInteger callCount = new AtomicInteger(0);
        CacheChangeListener listener = callCount::incrementAndGet;

        // Register the SAME listener under both MAC and hostname keys
        cache.addChangeListener("aa:bb:cc:dd:ee:ff", listener);
        cache.addChangeListener("phone", listener);

        DDWRTWirelessClient client = new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff");
        client.setHostname("Phone");
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", client);

        // Should be notified only ONCE despite matching both keys
        assertThat(callCount.get(), is(equalTo(1)));
    }

    @Test
    void testListenerRemoved() {
        AtomicInteger callCount = new AtomicInteger(0);
        CacheChangeListener listener = callCount::incrementAndGet;

        cache.addChangeListener("aa:bb:cc:dd:ee:ff", listener);
        cache.removeChangeListener("aa:bb:cc:dd:ee:ff", listener);

        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff"));

        assertThat(callCount.get(), is(equalTo(0)));
    }

    // ---- Radio CRUD ----

    @Test
    void testPutAndGetRadio() {
        DDWRTRadio radio = new DDWRTRadio("aa:bb:cc:dd:ee:ff", "wl0");
        radio.setSsid("TestNetwork");
        radio.setChannel(6);
        cache.putRadio(radio.getInterfaceId(), radio);

        DDWRTRadio result = cache.getRadio("aa:bb:cc:dd:ee:ff:wl0");
        assertThat(result, is(notNullValue()));
        assertThat(result.getSsid(), is(equalTo("TestNetwork")));
        assertThat(result.getChannel(), is(equalTo(6)));
    }

    @Test
    void testRadioListenerNotified() {
        AtomicInteger callCount = new AtomicInteger(0);
        cache.addChangeListener("aa:bb:cc:dd:ee:ff:wl0", callCount::incrementAndGet);

        DDWRTRadio radio = new DDWRTRadio("aa:bb:cc:dd:ee:ff", "wl0");
        cache.putRadio(radio.getInterfaceId(), radio);

        assertThat(callCount.get(), is(equalTo(1)));
    }

    // ---- Firewall Rules ----

    @Test
    void testPutAndGetFirewallRule() {
        DDWRTFirewallRule rule = new DDWRTFirewallRule("filter_rule3", "filter_rule3", "aa:bb:cc:dd:ee:ff");
        rule.setDescription("Bedtime 10-12");
        rule.setEnabled(true);
        cache.putFirewallRule("filter_rule3", rule);

        DDWRTFirewallRule result = cache.getFirewallRule("filter_rule3");
        assertThat(result, is(notNullValue()));
        assertThat(result.getDescription(), is(equalTo("Bedtime 10-12")));
        assertThat(result.isEnabled(), is(true));
    }

    // ---- DHCP Leases ----

    @Test
    void testPutAndGetDhcpLease() {
        DDWRTDhcpLease lease = new DDWRTDhcpLease("aa:bb:cc:dd:ee:ff");
        lease.setHostname("Phone");
        lease.setIpAddress("192.168.1.50");
        cache.putDhcpLease("AA:BB:CC:DD:EE:FF", lease);

        DDWRTDhcpLease result = cache.getDhcpLease("aa:bb:cc:dd:ee:ff");
        assertThat(result, is(notNullValue()));
        assertThat(result.getIpAddress(), is(equalTo("192.168.1.50")));
    }

    @Test
    void testGetDhcpLeaseByHostname() {
        DDWRTDhcpLease lease = new DDWRTDhcpLease("aa:bb:cc:dd:ee:ff");
        lease.setHostname("Phone");
        cache.putDhcpLease("aa:bb:cc:dd:ee:ff", lease);

        DDWRTDhcpLease result = cache.getDhcpLeaseByHostname("Phone");
        assertThat(result, is(notNullValue()));
        assertThat(result.getMac(), is(equalTo("aa:bb:cc:dd:ee:ff")));
    }

    @Test
    void testGetDhcpLeaseByHostnameNotFound() {
        assertThat(cache.getDhcpLeaseByHostname("nonexistent"), is(nullValue()));
    }

    // ---- Aggregate counts ----

    @Test
    void testTotalWirelessClientsCountsOnline() {
        DDWRTWirelessClient online = new DDWRTWirelessClient("aa:bb:cc:00:00:01");
        online.setOnline(true);
        cache.putWirelessClient("aa:bb:cc:00:00:01", online);

        DDWRTWirelessClient offline = new DDWRTWirelessClient("aa:bb:cc:00:00:02");
        offline.setOnline(false);
        cache.putWirelessClient("aa:bb:cc:00:00:02", offline);

        assertThat(cache.getTotalWirelessClients(), is(equalTo(1)));
    }

    // ---- clearAll ----

    @Test
    void testClearAll() {
        cache.putWirelessClient("aa:bb:cc:dd:ee:ff", new DDWRTWirelessClient("aa:bb:cc:dd:ee:ff"));
        cache.putRadio("aa:bb:cc:dd:ee:ff:wl0", new DDWRTRadio("aa:bb:cc:dd:ee:ff", "wl0"));
        cache.putFirewallRule("rule1", new DDWRTFirewallRule("rule1", "rule1", "aa:bb:cc:dd:ee:ff"));
        cache.putDhcpLease("aa:bb:cc:dd:ee:ff", new DDWRTDhcpLease("aa:bb:cc:dd:ee:ff"));

        cache.clearAll();

        assertThat(cache.getWirelessClients().size(), is(equalTo(0)));
        assertThat(cache.getRadios().size(), is(equalTo(0)));
        assertThat(cache.getFirewallRules().size(), is(equalTo(0)));
        assertThat(cache.getDhcpLeases().size(), is(equalTo(0)));
    }
}
