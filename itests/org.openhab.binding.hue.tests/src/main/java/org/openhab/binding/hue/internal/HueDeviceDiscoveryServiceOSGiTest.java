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
package org.openhab.binding.hue.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.config.HueBridgeConfig;
import org.openhab.binding.hue.internal.connection.HueBridge;
import org.openhab.binding.hue.internal.discovery.HueDeviceDiscoveryService;
import org.openhab.binding.hue.internal.dto.FullLight;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultFlag;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Tests for {@link HueDeviceDiscoveryService}.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Andre Fuechsel - added test 'assert start search is called()'
 *         - modified tests after introducing the generic thing types
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Markus Rathgeb - migrated to plain Java test
 */
public class HueDeviceDiscoveryServiceOSGiTest extends AbstractHueOSGiTestParent {

    protected DiscoveryListener discoveryListener;
    protected ThingRegistry thingRegistry;
    protected Bridge hueBridge;
    protected HueBridgeHandler hueBridgeHandler;
    protected HueDeviceDiscoveryService discoveryService;

    protected final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("hue", "bridge");
    protected final ThingUID BRIDGE_THING_UID = new ThingUID(BRIDGE_THING_TYPE_UID, "testBridge");

    @BeforeEach
    public void setUp() {
        registerVolatileStorageService();

        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));

        Configuration configuration = new Configuration();
        configuration.put(HOST, "1.2.3.4");
        configuration.put(USER_NAME, "testUserName");
        configuration.put(Thing.PROPERTY_SERIAL_NUMBER, "testSerialNumber");
        configuration.put("useSelfSignedCertificate", false);

        hueBridge = (Bridge) thingRegistry.createThingOfType(BRIDGE_THING_TYPE_UID, BRIDGE_THING_UID, null, "Bridge",
                configuration);

        assertThat(hueBridge, is(notNullValue()));
        thingRegistry.add(hueBridge);

        hueBridgeHandler = getThingHandler(hueBridge, HueBridgeHandler.class);
        assertThat(hueBridgeHandler, is(notNullValue()));

        discoveryService = getService(DiscoveryService.class, HueDeviceDiscoveryService.class);
        assertThat(discoveryService, is(notNullValue()));
    }

    @AfterEach
    public void cleanUp() {
        thingRegistry.remove(BRIDGE_THING_UID);
        waitForAssert(() -> {
            assertNull(getService(DiscoveryService.class, HueDeviceDiscoveryService.class));
        });
    }

    private void registerDiscoveryListener(DiscoveryListener discoveryListener) {
        unregisterCurrentDiscoveryListener();
        this.discoveryListener = discoveryListener;
        discoveryService.addDiscoveryListener(this.discoveryListener);
    }

    private void unregisterCurrentDiscoveryListener() {
        if (this.discoveryListener != null) {
            discoveryService.removeDiscoveryListener(this.discoveryListener);
        }
    }

    @Test
    public void hueLightRegistration() {
        FullLight light = new FullLight();
        light.setId("1");
        light.setUniqueID("AA:BB:CC:DD:EE:FF:00:11-XX");
        light.setModelID("LCT001");
        light.setType("Extended color light");

        AtomicReference<DiscoveryResult> resultWrapper = new AtomicReference<>();

        registerDiscoveryListener(new DiscoveryListener() {
            @Override
            public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
                resultWrapper.set(result);
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

        discoveryService.addLightDiscovery(light);
        waitForAssert(() -> {
            assertTrue(resultWrapper.get() != null);
        });

        final DiscoveryResult result = resultWrapper.get();
        assertThat(result.getFlag(), is(DiscoveryResultFlag.NEW));
        assertThat(result.getThingUID().toString(), is("hue:0210:testBridge:" + light.getId()));
        assertThat(result.getThingTypeUID(), is(THING_TYPE_EXTENDED_COLOR_LIGHT));
        assertThat(result.getBridgeUID(), is(hueBridge.getUID()));
        assertThat(result.getProperties().get(LIGHT_ID), is(light.getId()));
    }

    @Test
    public void startSearchIsCalled() throws IOException, ApiException {
        final AtomicBoolean searchHasBeenTriggered = new AtomicBoolean(false);

        HueBridge mockedHueBridge = new HueBridge(mock(HttpClient.class), "ip", 443, HueBridgeConfig.HTTPS, "username",
                Executors.newScheduledThreadPool(1)) {
            @Override
            public HueResult get(String address) throws CommunicationException {
                if (address.endsWith("testUserName")) {
                    String body = "{\"lights\":{}}";
                    return new HueResult(body, HttpStatus.OK_200);
                } else if (address.endsWith("lights") || address.endsWith("sensors") || address.endsWith("groups")) {
                    String body = "{}";
                    return new HueResult(body, HttpStatus.OK_200);
                } else if (address.endsWith("testUserName/config")) {
                    String body = "{\"apiversion\": \"1.26.0\"}";
                    return new HueResult(body, HttpStatus.OK_200);
                } else {
                    return new HueResult("", HttpStatus.NOT_FOUND_404);
                }
            }

            @Override
            public HueResult post(String address, String body) throws CommunicationException {
                if (address.endsWith("lights")) {
                    String bodyReturn = "{\"success\": {\"/lights\": \"Searching for new devices\"}}";
                    searchHasBeenTriggered.set(true);
                    return new HueResult(bodyReturn, HttpStatus.OK_200);
                } else {
                    return new HueResult("", HttpStatus.NOT_FOUND_404);
                }
            }

            @Override
            public HueResult put(String address, String body) throws CommunicationException {
                return new HueResult("", HttpStatus.OK_200);
            }
        };

        installHttpClientMock(hueBridgeHandler, mockedHueBridge);

        ThingStatusInfo online = ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build();
        waitForAssert(() -> {
            assertThat(hueBridge.getStatusInfo(), is(online));
        });

        discoveryService.startScan();
        waitForAssert(() -> {
            assertTrue(searchHasBeenTriggered.get());
        });
    }

    private void installHttpClientMock(HueBridgeHandler hueBridgeHandler, HueBridge mockedHueBridge) {
        waitForAssert(() -> {
            try {
                // mock HttpClient
                final Field hueBridgeField = HueBridgeHandler.class.getDeclaredField("hueBridge");
                hueBridgeField.setAccessible(true);
                hueBridgeField.set(hueBridgeHandler, mockedHueBridge);

                final Object hueBridgeValue = hueBridgeField.get(hueBridgeHandler);
                assertThat(hueBridgeValue, is(notNullValue()));

                final Field usernameField = HueBridge.class.getDeclaredField("username");
                usernameField.setAccessible(true);
                usernameField.set(hueBridgeValue, hueBridgeHandler.getThing().getConfiguration().get(USER_NAME));
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
                fail("Reflection usage error");
            }
        });
        hueBridgeHandler.initialize();
    }
}
