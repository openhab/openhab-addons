/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.discovery;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.energenie.EnergenieBindingConstants;
import org.openhab.binding.energenie.handler.EnergenieGatewayHandler;
import org.openhab.binding.energenie.handler.EnergenieSubdevicesHandler;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonDevice;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulHttpResponseException;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulJsonResponseException;
import org.openhab.binding.energenie.test.AbstractEnergenieOSGiTest;

/**
 * Tests for the {@link EnergenieDiscoveryService}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Mihaela Memova - Initial contribution
 *
 */
public class EnergenieDiscoveryOSGiTest {

    // MiHome internal API test data
    private static final int TEST_GATEWAY_ID = 4541;
    private static final String TEST_GATEWAY_LABEL = "TestGateway";
    private static final int TEST_DEVICE_ID = 51816;
    private static final EnergenieDeviceTypes TEST_DEVICE_TYPE = EnergenieDeviceTypes.MOTION_SENSOR;

    // ESH test data
    private static final ThingTypeUID TEST_THING_TYPE_UID = EnergenieBindingConstants.THING_TYPE_MOTION_SENSOR;
    private static final ThingUID TEST_THING_UID = new ThingUID(TEST_THING_TYPE_UID, Integer.toString(TEST_DEVICE_ID));
    private static final ThingTypeUID TEST_GATEWAY_TYPE_UID = EnergenieBindingConstants.THING_TYPE_GATEWAY;
    private static final ThingUID TEST_GATEWAY_UID = new ThingUID(TEST_GATEWAY_TYPE_UID,
            Integer.toString(TEST_GATEWAY_ID));

    private EnergenieDiscoveryService discoveryService;

    private boolean isResultExpected;

    private JsonDevice[] listGatewaysResponse;
    private JsonDevice[] listSubdevicesResponse;
    private List<Thing> registeredThings = new ArrayList<Thing>();

    EnergenieApiManager mockedApiManager;
    ThingRegistry mockedThingRegistry;
    DiscoveryServiceCallback mockedDiscoveryServiceCallback;

    @Before
    public void setUp() throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        initMocks();
        discoveryService = new EnergenieDiscoveryService(mockedApiManager);
        discoveryService.setDiscoveryServiceCallback(mockedDiscoveryServiceCallback);
        isResultExpected = true;
    }

    private void initMocks() throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        mockedApiManager = mock(EnergenieApiManager.class);
        when(mockedApiManager.listSubdevices()).thenAnswer(new Answer<JsonSubdevice[]>() {
            @Override
            public JsonSubdevice[] answer(InvocationOnMock invocation) {
                return (JsonSubdevice[]) listSubdevicesResponse;
            }
        });
        when(mockedApiManager.listGateways()).thenAnswer(new Answer<JsonGateway[]>() {
            @Override
            public JsonGateway[] answer(InvocationOnMock arg0) throws Throwable {
                return (JsonGateway[]) listGatewaysResponse;
            }
        });
        when(mockedApiManager.getConfiguration()).thenReturn(new EnergenieApiConfiguration(
                AbstractEnergenieOSGiTest.TEST_USERNAME, AbstractEnergenieOSGiTest.TEST_PASSWORD));

        mockedThingRegistry = mock(ThingRegistry.class);
        when(mockedThingRegistry.get(any(ThingUID.class))).thenAnswer(new Answer<Thing>() {
            @Override
            public Thing answer(InvocationOnMock arg0) throws Throwable {
                ThingUID uid = (ThingUID) arg0.getArgument(0);
                Optional<Thing> optionalThing = registeredThings.stream().filter(x -> x.getUID().equals(uid))
                        .findFirst();
                return optionalThing.orElse(null);
            }
        });

        mockedDiscoveryServiceCallback = mock(DiscoveryServiceCallback.class);
        when(mockedDiscoveryServiceCallback.getExistingThing(any(ThingUID.class))).thenAnswer(new Answer<Thing>() {
            @Override
            public Thing answer(InvocationOnMock arg0) throws Throwable {
                ThingUID thingUID = arg0.getArgument(0);
                return mockedThingRegistry.get(thingUID);
            }
        });
    }

    @After
    public void tearDown() {
        registeredThings.clear();
        listSubdevicesResponse = null;
        listGatewaysResponse = null;
        isResultExpected = false;
    }

    @Test
    public void createResultForGatewayWithoutThing() {
        // Set up the backend response
        listSubdevicesResponse = new JsonSubdevice[0];
        listGatewaysResponse = new JsonGateway[1];
        listGatewaysResponse[0] = new JsonGateway(TEST_GATEWAY_ID, TEST_GATEWAY_LABEL);

        DiscoveryListener discoveryListenerMock = new DiscoveryListener() {
            @Override
            public void thingRemoved(@NonNull DiscoveryService source, @NonNull ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(@NonNull DiscoveryService source, @NonNull DiscoveryResult result) {
                if (isResultExpected) {
                    assertGatewayDiscoveryResult(result);
                    // Only one result is expected
                    isResultExpected = false;
                } else {
                    fail("Unexpected result " + result.toString());
                }
            }

            @Override
            public @Nullable Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source,
                    long timestamp, @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs,
                    @Nullable ThingUID bridgeUID) {
                return null;
            }
        };

        discoveryService.addDiscoveryListener(discoveryListenerMock);
        discoveryService.startScan();

        assertFalse(isResultExpected);
    }

    @Test
    public void doNotCreateResultWhenNoGatewayIsFound() {
        // Set up the backend response
        listSubdevicesResponse = new JsonSubdevice[0];
        listGatewaysResponse = new JsonGateway[0];

        DiscoveryListener discoveryListenerMock = new DiscoveryListener() {
            @Override
            public void thingRemoved(@NonNull DiscoveryService source, @NonNull ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(@NonNull DiscoveryService source, @NonNull DiscoveryResult result) {
                fail("Unexpected Discovery Result " + result.toString());
            }

            @Override
            public @Nullable Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source,
                    long timestamp, @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs,
                    @Nullable ThingUID bridgeUID) {
                return null;
            }
        };

        discoveryService.addDiscoveryListener(discoveryListenerMock);
        discoveryService.startScan();
    }

    @Test
    public void doNotCreateResultForGatewayWithAThing() {
        // Add a bridge to the registry
        Map<String, String> properties = new HashMap<>();
        properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, Integer.toString(TEST_GATEWAY_ID));
        Bridge bridge = BridgeBuilder.create(TEST_GATEWAY_TYPE_UID, TEST_GATEWAY_UID).withProperties(properties)
                .build();
        registeredThings.add(bridge);

        // Set up the backend response
        JsonGateway gateway = new JsonGateway();
        listGatewaysResponse = new JsonGateway[1];
        listGatewaysResponse[0] = new JsonGateway(TEST_GATEWAY_ID, TEST_GATEWAY_LABEL);

        // Add the listener
        DiscoveryListener discoveryListenerMock = new DiscoveryListener() {
            @Override
            public void thingRemoved(@NonNull DiscoveryService source, @NonNull ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(@NonNull DiscoveryService source, @NonNull DiscoveryResult result) {
                fail("Unexpected Discovery Result");
            }

            @Override
            public @Nullable Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source,
                    long timestamp, @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs,
                    @Nullable ThingUID bridgeUID) {
                return null;
            }
        };

        discoveryService.addDiscoveryListener(discoveryListenerMock);
        discoveryService.startScan();
    }

    @Test
    public void createResultForSubdeviceWithoutAThing() {
        // Add a bridge to the registry
        Map<String, String> properties = new HashMap<>();
        properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, Integer.toString(TEST_GATEWAY_ID));

        Bridge bridge = BridgeBuilder.create(TEST_GATEWAY_TYPE_UID, TEST_GATEWAY_UID).withProperties(properties)
                .build();
        registeredThings.add(bridge);

        // Set up the backend response
        listSubdevicesResponse = new JsonSubdevice[1];
        listSubdevicesResponse[0] = new JsonSubdevice(TEST_DEVICE_ID, TEST_GATEWAY_ID, TEST_DEVICE_TYPE);
        listGatewaysResponse = new JsonGateway[0];

        DiscoveryListener discoveryListenerMock = new DiscoveryListener() {
            @Override
            public void thingRemoved(@NonNull DiscoveryService source, @NonNull ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(@NonNull DiscoveryService source, @NonNull DiscoveryResult result) {
                if (isResultExpected) {
                    assertSubdeviceDiscoveryResult(result);
                    // Only one result is expected
                    isResultExpected = false;
                } else {
                    fail("Unexpected result " + result.toString());
                }
            }

            @Override
            public @Nullable Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source,
                    long timestamp, @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs,
                    @Nullable ThingUID bridgeUID) {
                return null;
            }
        };

        discoveryService.addDiscoveryListener(discoveryListenerMock);
        discoveryService.startScan();
        assertFalse(isResultExpected);
    }

    @Test
    public void doNotCreateResultWhenNoSubdeviceIsFound() {
        // Set up the backend response
        listSubdevicesResponse = new JsonSubdevice[0];
        listGatewaysResponse = new JsonGateway[0];

        DiscoveryListener discoveryListenerMock = new DiscoveryListener() {
            @Override
            public void thingRemoved(@NonNull DiscoveryService source, @NonNull ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(@NonNull DiscoveryService source, @NonNull DiscoveryResult result) {
                fail("Unexpected Discovery Result " + result.toString());
            }

            @Override
            public @Nullable Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source,
                    long timestamp, @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs,
                    @Nullable ThingUID bridgeUID) {
                return null;
            }
        };

        discoveryService.addDiscoveryListener(discoveryListenerMock);
        discoveryService.startScan();
    }

    @Test
    public void doNotCreateResultForSubdeviceWithAThing() {
        // Add a thing to the registry
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(EnergenieBindingConstants.PROPERTY_DEVICE_ID, Integer.toString(TEST_DEVICE_ID));
        Thing thing = ThingBuilder.create(TEST_THING_TYPE_UID, TEST_THING_UID).withProperties(properties).build();
        registeredThings.add(thing);

        // Set up the backend response
        listGatewaysResponse = new JsonGateway[0];
        listSubdevicesResponse = new JsonSubdevice[1];
        listSubdevicesResponse[0] = new JsonSubdevice(TEST_DEVICE_ID, TEST_GATEWAY_ID, TEST_DEVICE_TYPE);

        // Add the listener
        DiscoveryListener discoveryListenerMock = new DiscoveryListener() {
            @Override
            public void thingRemoved(@NonNull DiscoveryService source, @NonNull ThingUID thingUID) {
            }

            @Override
            public void thingDiscovered(@NonNull DiscoveryService source, @NonNull DiscoveryResult result) {
                fail("Unexpected Discovery Result");
            }

            @Override
            public @Nullable Collection<@NonNull ThingUID> removeOlderResults(@NonNull DiscoveryService source,
                    long timestamp, @Nullable Collection<@NonNull ThingTypeUID> thingTypeUIDs,
                    @Nullable ThingUID bridgeUID) {
                return null;
            }
        };

        discoveryService.addDiscoveryListener(discoveryListenerMock);
        discoveryService.startScan();
    }

    private void assertGatewayDiscoveryResult(DiscoveryResult result) {
        assertEquals("DiscoveryResult has incorrect ThingUID", TEST_GATEWAY_UID, result.getThingUID());

        Map<String, Object> properties = result.getProperties();
        assertEquals("DiscoveryResult has incorrect " + EnergenieBindingConstants.PROPERTY_DEVICE_ID + " property",
                TEST_GATEWAY_ID, properties.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID));
        assertEquals("DiscoveryResult has incorrect " + EnergenieBindingConstants.PROPERTY_TYPE + " property",
                EnergenieDeviceTypes.GATEWAY.toString(), properties.get(EnergenieBindingConstants.PROPERTY_TYPE));
        assertEquals(
                "DiscoveryResult doesn't contain default required configuration paramter "
                        + EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                EnergenieGatewayHandler.DEFAULT_UPDATE_INTERVAL,
                properties.get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
    }

    private void assertSubdeviceDiscoveryResult(DiscoveryResult result) {
        assertEquals("DiscoveryResult has incorrect BridgeUID", result.getBridgeUID(), TEST_GATEWAY_UID);

        assertEquals("DiscoveryResult has incorrect ThingUID", result.getThingUID(), TEST_THING_UID);

        Map<String, Object> properties = result.getProperties();
        assertEquals("DiscoveryResult has incorrect " + EnergenieBindingConstants.PROPERTY_DEVICE_ID + " property",
                TEST_DEVICE_ID, properties.get(EnergenieBindingConstants.PROPERTY_DEVICE_ID));
        assertEquals("DiscoveryResult has incorrect " + EnergenieBindingConstants.PROPERTY_GATEWAY_ID + " property",
                TEST_GATEWAY_ID, properties.get(EnergenieBindingConstants.PROPERTY_GATEWAY_ID));
        assertEquals("DiscoveryResult has incorrect " + EnergenieBindingConstants.PROPERTY_TYPE + " property",
                TEST_DEVICE_TYPE, properties.get(EnergenieBindingConstants.PROPERTY_TYPE));
        assertEquals(
                "DiscoveryResult doesn't contain default required configuration parameter "
                        + EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL,
                EnergenieSubdevicesHandler.DEFAULT_UPDATE_INTERVAL,
                properties.get(EnergenieBindingConstants.CONFIG_UPDATE_INTERVAL));
    }

}
