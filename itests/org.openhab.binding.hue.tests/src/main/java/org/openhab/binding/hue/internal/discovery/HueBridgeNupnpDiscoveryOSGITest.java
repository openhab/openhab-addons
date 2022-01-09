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
package org.openhab.binding.hue.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.hue.internal.HueBindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.core.config.discovery.inbox.InboxPredicates.forThingTypeUID;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 *
 * @author Christoph Knauf - Initial contribution
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueBridgeNupnpDiscoveryOSGITest extends JavaOSGiTest {

    HueBridgeNupnpDiscovery sut;
    VolatileStorageService volatileStorageService = new VolatileStorageService();
    DiscoveryListener discoveryListener;
    Inbox inbox;

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    final String ip1 = "192.168.31.17";
    final String ip2 = "192.168.30.28";
    final String sn1 = "00178820057f";
    final String sn2 = "001788141b41";
    final ThingUID BRIDGE_THING_UID_1 = new ThingUID(BRIDGE_THING_TYPE_UID, sn1);
    final ThingUID BRIDGE_THING_UID_2 = new ThingUID(BRIDGE_THING_TYPE_UID, sn2);
    final String validBridgeDiscoveryResult = "[{\"id\":\"001788fffe20057f\",\"internalipaddress\":" + ip1
            + "},{\"id\":\"001788fffe141b41\",\"internalipaddress\":" + ip2 + "}]";
    String discoveryResult;
    String expBridgeDescription = "" + //
            "<?xml version=\"1.0\"?>" + //
            "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">" + //
            "  <specVersion>" + //
            "    <major>1</major>" + //
            "    <minor>0</minor>" + //
            "  </specVersion>" + //
            "  <URLBase>http://$IP:80/</URLBase>" + //
            "  <device>" + //
            "    <deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>" + //
            "    <friendlyName>Philips hue ($IP)</friendlyName>" + //
            "    <manufacturer>Royal Philips Electronics</manufacturer>" + //
            "    <manufacturerURL>http://www.philips.com</manufacturerURL>" + //
            "<modelDescription>Philips hue Personal Wireless Lighting</modelDescription>" + //
            "<modelName>Philips hue bridge 2012</modelName>" + //
            "<modelNumber>1000000000000</modelNumber>" + //
            "<modelURL>http://www.meethue.com</modelURL>" + //
            "    <serialNumber>93eadbeef13</serialNumber>" + //
            "    <UDN>uuid:01234567-89ab-cdef-0123-456789abcdef</UDN>" + //
            "    <serviceList>" + //
            "      <service>" + //
            "        <serviceType>(null)</serviceType>" + //
            "        <serviceId>(null)</serviceId>" + //
            "        <controlURL>(null)</controlURL>" + //
            "        <eventSubURL>(null)</eventSubURL>" + //
            "        <SCPDURL>(null)</SCPDURL>" + //
            "      </service>" + //
            "    </serviceList>" + //
            "    <presentationURL>index.html</presentationURL>" + //
            "    <iconList>" + //
            "      <icon>" + //
            "        <mimetype>image/png</mimetype>" + //
            "        <height>48</height>" + //
            "        <width>48</width>" + //
            "        <depth>24</depth>" + //
            "        <url>hue_logo_0.png</url>" + //
            "      </icon>" + //
            "      <icon>" + //
            "        <mimetype>image/png</mimetype>" + //
            "        <height>120</height>" + //
            "        <width>120</width>" + //
            "        <depth>24</depth>" + //
            "        <url>hue_logo_3.png</url>" + //
            "      </icon>" + //
            "    </iconList>" + //
            "  </device>" + //
            "</root>";

    private void checkDiscoveryResult(DiscoveryResult result, String expIp, String expSn) {
        assertThat(result.getBridgeUID(), nullValue());
        assertThat(result.getLabel(), is(HueBridgeNupnpDiscovery.LABEL_PATTERN.replace("IP", expIp)));
        assertThat(result.getProperties().get("ipAddress"), is(expIp));
        assertThat(result.getProperties().get("serialNumber"), is(expSn));
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener();
        this.discoveryListener = discoveryListener;
        sut.addDiscoveryListener(this.discoveryListener);
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            sut.removeDiscoveryListener(this.discoveryListener);
        }
    }

    // Mock class which only overrides the doGetRequest method in order to make the class testable
    class ConfigurableBridgeNupnpDiscoveryMock extends HueBridgeNupnpDiscovery {
        @Override
        protected String doGetRequest(String url) throws IOException {
            if (url.contains("meethue")) {
                return discoveryResult;
            } else if (url.contains(ip1)) {
                return expBridgeDescription.replaceAll("$IP", ip1);
            } else if (url.contains(ip2)) {
                return expBridgeDescription.replaceAll("$IP", ip2);
            }
            throw new IOException();
        }
    }

    @BeforeEach
    public void setUp() {
        registerService(volatileStorageService);

        sut = getService(DiscoveryService.class, HueBridgeNupnpDiscovery.class);
        assertThat(sut, is(notNullValue()));

        inbox = getService(Inbox.class);
        assertThat(inbox, is(notNullValue()));

        unregisterCurrentDiscoveryListener();
    }

    @Test
    public void bridgeThingTypeIsSupported() {
        assertThat(sut.getSupportedThingTypes().size(), is(1));
        assertThat(sut.getSupportedThingTypes().iterator().next(), is(THING_TYPE_BRIDGE));
    }

    @Test
    public void validBridgesAreDiscovered() {
        List<DiscoveryResult> oldResults = inbox.stream().filter(forThingTypeUID(BRIDGE_THING_TYPE_UID))
                .collect(Collectors.toList());
        for (final DiscoveryResult oldResult : oldResults) {
            inbox.remove(oldResult.getThingUID());
        }

        sut = new ConfigurableBridgeNupnpDiscoveryMock();
        registerService(sut, DiscoveryService.class.getName());
        discoveryResult = validBridgeDiscoveryResult;
        final Map<ThingUID, DiscoveryResult> results = new HashMap<>();
        registerDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                results.put(result.getThingUID(), result);
            }

            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
                return null;
            }
        });

        sut.startScan();

        waitForAssert(() -> {
            assertThat(results.size(), is(2));
            assertThat(results.get(BRIDGE_THING_UID_1), is(notNullValue()));
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_1), ip1, sn1);
            assertThat(results.get(BRIDGE_THING_UID_2), is(notNullValue()));
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_2), ip2, sn2);

            final List<DiscoveryResult> inboxResults = inbox.stream().filter(forThingTypeUID(BRIDGE_THING_TYPE_UID))
                    .collect(Collectors.toList());
            assertTrue(inboxResults.size() >= 2);

            assertThat(inboxResults.stream().filter(result -> result.getThingUID().equals(BRIDGE_THING_UID_1))
                    .findFirst().orElse(null), is(notNullValue()));
            assertThat(inboxResults.stream().filter(result -> result.getThingUID().equals(BRIDGE_THING_UID_2))
                    .findFirst().orElse(null), is(notNullValue()));
        });
    }

    @Test
    public void invalidBridgesAreNotDiscovered() {
        List<DiscoveryResult> oldResults = inbox.stream().filter(forThingTypeUID(BRIDGE_THING_TYPE_UID))
                .collect(Collectors.toList());
        for (final DiscoveryResult oldResult : oldResults) {
            inbox.remove(oldResult.getThingUID());
        }

        sut = new ConfigurableBridgeNupnpDiscoveryMock();
        registerService(sut, DiscoveryService.class.getName());
        final Map<ThingUID, DiscoveryResult> results = new HashMap<>();
        registerDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                results.put(result.getThingUID(), result);
            }

            @Override
            public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            }

            @Override
            public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    Collection<ThingTypeUID> thingTypeUIDs, ThingUID bridgeUID) {
                return null;
            }
        });

        // missing ip
        discoveryResult = "[{\"id\":\"001788fffe20057f\",\"internalipaddress\":}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // missing id
        discoveryResult = "[{\"id\":\"\",\"internalipaddress\":192.168.30.22}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // id < 10
        discoveryResult = "[{\"id\":\"012345678\",\"internalipaddress\":192.168.30.22}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // bridge indicator not part of id
        discoveryResult = "[{\"id\":\"0123456789\",\"internalipaddress\":192.168.30.22}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // bridge indicator at wrong position (-1)
        discoveryResult = "[{\"id\":\"01234" + HueBridgeNupnpDiscovery.BRIDGE_INDICATOR
                + "7891\",\"internalipaddress\":192.168.30.22}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // bridge indicator at wrong position (+1)
        discoveryResult = "[{\"id\":\"0123456" + HueBridgeNupnpDiscovery.BRIDGE_INDICATOR
                + "7891\",\"internalipaddress\":192.168.30.22}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // bridge not reachable
        discoveryResult = "[{\"id\":\"001788fffe20057f\",\"internalipaddress\":192.168.30.1}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        // invalid bridge description
        expBridgeDescription = "";
        discoveryResult = "[{\"id\":\"001788fffe20057f\",\"internalipaddress\":" + ip1 + "}]";
        sut.startScan();
        waitForAssert(() -> {
            assertThat(results.size(), is(0));
        });

        waitForAssert(() -> {
            assertThat(
                    inbox.stream().filter(forThingTypeUID(BRIDGE_THING_TYPE_UID)).collect(Collectors.toList()).size(),
                    is(0));
        });
    }
}
