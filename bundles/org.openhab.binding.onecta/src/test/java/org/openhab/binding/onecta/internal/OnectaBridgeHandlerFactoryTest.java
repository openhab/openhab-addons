/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.handler.*;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
public class OnectaBridgeHandlerFactoryTest {

    public static final String USERID = "Userid";
    public static final String PASSWORD = "Password";
    public static final String UNITID = "ThisIsAUnitID";

    private Map<String, Object> bridgeProperties = new HashMap<>();
    private Configuration thingConfiguration = new Configuration();

    private OnectaBridgeHandlerFactory handler;

    @Mock
    private HttpClientFactory httpClientFactoryMock;
    @Mock
    private TimeZoneProvider timeZoneProviderMock;
    @Mock
    private OnectaBridgeHandler onectaBridgeHandlerMock;
    @Mock
    private OnectaDeviceHandler onectaDeviceHandlerMock;
    @Mock
    private OnectaGatewayHandler onectaGatewayHandlerMock;
    @Mock
    private OnectaWaterTankHandler onectaWaterTankHandlerMock;
    @Mock
    private OnectaIndoorUnitHandler onectaIndoorUnitHandlerMock;
    @Mock
    private BundleContext bundleContextMock;

    @BeforeEach
    public void setUp() {
        handler = new OnectaBridgeHandlerFactory(httpClientFactoryMock);
        bridgeProperties.put(CONFIG_PAR_USERID, USERID);
        bridgeProperties.put(CONFIG_PAR_PASSWORD, PASSWORD);
        bridgeProperties.put(CONFIG_PAR_REFRESHINTERVAL, "10");
        bridgeProperties.put(CONFIG_PAR_UNITID, UNITID);
        thingConfiguration.setProperties(bridgeProperties);
    }

    @Test
    public void supportsThingTypeTest() {
        assertEquals(true, handler.supportsThingType(THING_TYPE_BRIDGE));
        assertEquals(true, handler.supportsThingType(THING_TYPE_CLIMATECONTROL));
        assertEquals(true, handler.supportsThingType(THING_TYPE_GATEWAY));
        assertEquals(true, handler.supportsThingType(THING_TYPE_WATERTANK));
        assertEquals(true, handler.supportsThingType(THING_TYPE_INDOORUNIT));
    }

    @Test
    public void createHandlerTest() throws NoSuchFieldException, IllegalAccessException {

        Field privateDataTransServiceField = BaseThingHandlerFactory.class.getDeclaredField("bundleContext");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, bundleContextMock);

        Thing bridgeThing = new DummyBridge(THING_TYPE_BRIDGE, onectaBridgeHandlerMock, ThingStatus.ONLINE);
        ThingHandler thingHandler = handler.createHandler(bridgeThing);
        assertEquals(true, thingHandler instanceof OnectaBridgeHandler);

        Configuration configuration = new Configuration();
        configuration.put(CONFIG_PAR_UNITID, UNITID);

        Thing dummyThing = new DummyThing(THING_TYPE_CLIMATECONTROL, onectaDeviceHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaDeviceHandler);

        dummyThing = new DummyThing(THING_TYPE_GATEWAY, onectaGatewayHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaGatewayHandler);

        dummyThing = new DummyThing(THING_TYPE_WATERTANK, onectaWaterTankHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaWaterTankHandler);

        dummyThing = new DummyThing(THING_TYPE_INDOORUNIT, onectaIndoorUnitHandlerMock, ThingStatus.ONLINE);
        ((DummyThing) dummyThing).setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertEquals(true, thingHandler instanceof OnectaIndoorUnitHandler);
    }
}
