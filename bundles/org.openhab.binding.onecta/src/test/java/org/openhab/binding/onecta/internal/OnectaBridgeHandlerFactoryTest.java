/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.handler.*;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthTokenRefresher;
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

    public static final String UNITID = "ThisIsAUnitID";

    private OnectaBridgeHandlerFactory handler;
    @Mock
    private HttpClientFactory httpClientFactoryMock;
    @Mock
    private OAuthTokenRefresher openHabOAuthTokenRefresher;
    @Mock
    private OnectaTranslationProvider onectaTranslationProvider;
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
    private BundleContext bundleContextMock;

    @BeforeEach
    public void setUp() {
        handler = new OnectaBridgeHandlerFactory(httpClientFactoryMock, openHabOAuthTokenRefresher,
                onectaTranslationProvider);
    }

    @Test
    public void supportsThingTypeTest() {
        assertEquals(true, handler.supportsThingType(THING_TYPE_BRIDGE));
        assertEquals(true, handler.supportsThingType(THING_TYPE_CLIMATECONTROL));
        assertEquals(true, handler.supportsThingType(THING_TYPE_GATEWAY));
        assertEquals(true, handler.supportsThingType(THING_TYPE_WATERTANK));
    }

    @Test
    public void createHandlerTest() throws NoSuchFieldException, IllegalAccessException {

        Field privateDataTransServiceField = BaseThingHandlerFactory.class.getDeclaredField("bundleContext");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, bundleContextMock);

        Thing bridgeThing = new DummyBridge(THING_TYPE_BRIDGE, onectaBridgeHandlerMock, ThingStatus.ONLINE);
        ThingHandler thingHandler = handler.createHandler(bridgeThing);
        assertInstanceOf(OnectaBridgeHandler.class, thingHandler);

        Configuration configuration = new Configuration();
        configuration.put(CONFIG_PAR_UNITID, UNITID);

        DummyThing dummyThing = new DummyThing(THING_TYPE_CLIMATECONTROL, onectaDeviceHandlerMock, ThingStatus.ONLINE);
        dummyThing.setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertInstanceOf(OnectaDeviceHandler.class, thingHandler);

        dummyThing = new DummyThing(THING_TYPE_GATEWAY, onectaGatewayHandlerMock, ThingStatus.ONLINE);
        dummyThing.setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertInstanceOf(OnectaGatewayHandler.class, thingHandler);

        dummyThing = new DummyThing(THING_TYPE_WATERTANK, onectaWaterTankHandlerMock, ThingStatus.ONLINE);
        dummyThing.setConfiguration(configuration);
        thingHandler = handler.createHandler(dummyThing);
        assertInstanceOf(OnectaWaterTankHandler.class, thingHandler);
    }
}
