/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.mockito.Mockito.mock;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;
import static org.openhab.core.config.discovery.inbox.InboxPredicates.forThingTypeUID;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Christoph Knauf - Initial contribution
 * @author Markus Rathgeb - migrated to plain Java test
 */
@NonNullByDefault
public class HueBridgeNupnpDiscoveryOSGITest extends JavaOSGiTest {

    private @NonNullByDefault({}) HueBridgeNupnpDiscovery sut;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();
    private @Nullable DiscoveryListener discoveryListener;
    private @NonNullByDefault({}) Inbox inbox;

    private static final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    private static final String IP1 = "192.168.31.17";
    private static final String IP2 = "192.168.30.28";
    private static final String IP3 = "192.168.30.29";
    private static final String SN1 = "001788fffe20057f";
    private static final String SN2 = "001788fffe141b41";
    private static final String SN3 = "001788fffe141b42";
    private static final ThingUID BRIDGE_THING_UID_1 = new ThingUID(BRIDGE_THING_TYPE_UID, SN1);
    private static final ThingUID BRIDGE_THING_UID_2 = new ThingUID(BRIDGE_THING_TYPE_UID, SN2);
    private static final ThingUID BRIDGE_THING_UID_3 = new ThingUID(BRIDGE_THING_TYPE_UID, SN3);

    private final String validBridgeDiscoveryResult = "[{\"id\":\"" + SN1 + "\",\"internalipaddress\":" + IP1
            + "},{\"id\":\"" + SN2 + "\",\"internalipaddress\":" + IP2 + "},{\"id\":\"" + SN3
            + "\",\"internalipaddress\":" + IP3 + "}]";
    private @Nullable String discoveryResult;
    private String expBridgeDescription1 = "{\"name\":\"Philips hue\",\"datastoreversion\":\"149\",\"swversion\":\"1957113050\",\"apiversion\":\"1.57.0\",\"mac\":\"00:11:22:33:44\",\"bridgeid\":\"$SN\",\"factorynew\":false,\"replacesbridgeid\":null,\"modelid\":\"BSB002\",\"starterkitid\":\"\"}";
    private String expBridgeDescription2 = "{\"name\":\"Hue Bridge\",\"datastoreversion\":\"161\",\"swversion\":\"1959194040\",\"apiversion\":\"1.59.0\",\"mac\":\"00:11:22:33:44\",\"bridgeid\":\"$SN\",\"factorynew\":false,\"replacesbridgeid\":null,\"modelid\":\"BSB002\",\"starterkitid\":\"\"}";

    private void checkDiscoveryResult(@Nullable DiscoveryResult result, String expIp, String expSn) {
        if (result == null) {
            return;
        }
        assertThat(result.getBridgeUID(), nullValue());
        assertThat(result.getLabel(), is(String.format(DISCOVERY_LABEL_PATTERN, expIp)));
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
        public ConfigurableBridgeNupnpDiscoveryMock(ThingRegistry thingRegistry) {
            super(thingRegistry);
        }

        @Override
        protected @Nullable String doGetRequest(String url) throws IOException {
            if (url.contains("meethue")) {
                return discoveryResult;
            } else if (url.contains(IP1)) {
                return expBridgeDescription1.replaceAll("$SN", SN1);
            } else if (url.contains(IP2)) {
                return expBridgeDescription1.replaceAll("$SN", SN2);
            } else if (url.contains(IP3)) {
                return expBridgeDescription2.replaceAll("$SN", SN3);
            }
            throw new IOException();
        }

        @Override
        protected boolean isClip2Supported(String ipAddress) {
            return false;
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
        assertThat(sut.getSupportedThingTypes().size(), is(2));
        assertThat(sut.getSupportedThingTypes().contains(THING_TYPE_BRIDGE), is(true));
    }

    @Test
    public void validBridgesAreDiscovered() {
        List<DiscoveryResult> oldResults = inbox.stream().filter(forThingTypeUID(BRIDGE_THING_TYPE_UID))
                .collect(Collectors.toList());
        for (final DiscoveryResult oldResult : oldResults) {
            inbox.remove(oldResult.getThingUID());
        }

        sut = new ConfigurableBridgeNupnpDiscoveryMock(mock(ThingRegistry.class));
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
            public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
                return null;
            }
        });

        sut.startScan();

        waitForAssert(() -> {
            assertThat(results.size(), is(3));
            assertThat(results.get(BRIDGE_THING_UID_1), is(notNullValue()));
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_1), IP1, SN1);
            assertThat(results.get(BRIDGE_THING_UID_2), is(notNullValue()));
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_2), IP2, SN2);
            assertThat(results.get(BRIDGE_THING_UID_3), is(notNullValue()));
            checkDiscoveryResult(results.get(BRIDGE_THING_UID_3), IP3, SN3);

            final List<DiscoveryResult> inboxResults = inbox.stream().filter(forThingTypeUID(BRIDGE_THING_TYPE_UID))
                    .collect(Collectors.toList());
            assertTrue(inboxResults.size() >= 3);

            assertThat(inboxResults.stream().filter(result -> result.getThingUID().equals(BRIDGE_THING_UID_1))
                    .findFirst().orElse(null), is(notNullValue()));
            assertThat(inboxResults.stream().filter(result -> result.getThingUID().equals(BRIDGE_THING_UID_2))
                    .findFirst().orElse(null), is(notNullValue()));
            assertThat(inboxResults.stream().filter(result -> result.getThingUID().equals(BRIDGE_THING_UID_3))
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

        sut = new ConfigurableBridgeNupnpDiscoveryMock(mock(ThingRegistry.class));
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
            public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                    @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
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
        expBridgeDescription1 = "";
        discoveryResult = "[{\"id\":\"001788fffe20057f\",\"internalipaddress\":" + IP1 + "}]";
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
