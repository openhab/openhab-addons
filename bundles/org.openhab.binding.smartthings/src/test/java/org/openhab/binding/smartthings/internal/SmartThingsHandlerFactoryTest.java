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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.handler.SmartThingsAccountHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * Tests for {@link SmartThingsHandlerFactory}.
 */
@NonNullByDefault
class SmartThingsHandlerFactoryTest {

    @Test
    void createHandlerAllowsMultipleAccountBridges() {
        TestSmartThingsHandlerFactory factory = createFactory();
        Bridge firstAccount = accountBridge("first");
        Bridge secondAccount = accountBridge("second");

        ThingHandler firstAccountHandler = factory.create(firstAccount);
        ThingHandler secondAccountHandler = factory.create(secondAccount);

        assertInstanceOf(SmartThingsAccountHandler.class, firstAccountHandler);
        assertInstanceOf(SmartThingsAccountHandler.class, secondAccountHandler);
        assertNotSame(firstAccountHandler, secondAccountHandler);
        assertSame(firstAccountHandler, factory.getOAuthHandler(firstAccount.getUID()));
        assertSame(secondAccountHandler, factory.getOAuthHandler(secondAccount.getUID()));
    }

    @Test
    void createHandlerAllowsChildThingsForEachRegisteredAccountBridge() {
        TestSmartThingsHandlerFactory factory = createFactory();
        Bridge firstAccount = accountBridge("first");
        Bridge secondAccount = accountBridge("second");

        assertInstanceOf(SmartThingsAccountHandler.class, factory.create(firstAccount));
        assertInstanceOf(SmartThingsAccountHandler.class, factory.create(secondAccount));

        assertInstanceOf(SmartThingsThingHandler.class, factory.create(childThing("first-washer", firstAccount)));
        assertInstanceOf(SmartThingsThingHandler.class, factory.create(childThing("second-washer", secondAccount)));
    }

    @Test
    void createHandlerRejectsChildThingsWithoutRegisteredAccountBridge() {
        TestSmartThingsHandlerFactory factory = createFactory();

        assertNull(factory.create(standaloneChildThing("standalone-washer")));
    }

    @Test
    void accountHandlersUseUniquePathsForEachBridge() {
        TestSmartThingsHandlerFactory factory = createFactory();

        SmartThingsBridgeHandler firstAccountHandler = assertInstanceOf(SmartThingsBridgeHandler.class,
                factory.create(accountBridge("first")));
        SmartThingsBridgeHandler secondAccountHandler = assertInstanceOf(SmartThingsBridgeHandler.class,
                factory.create(accountBridge("second")));

        assertEquals("/smartthings/first", firstAccountHandler.getAuthServletPath());
        assertEquals("/smartthings/first/cb", firstAccountHandler.getAuthCallbackPath());
        assertEquals("/smartthings/second", secondAccountHandler.getAuthServletPath());
        assertEquals("/smartthings/second/cb", secondAccountHandler.getAuthCallbackPath());
    }

    @Test
    void removeHandlerOnlyRemovesTheSelectedAccountBridge() {
        TestSmartThingsHandlerFactory factory = createFactory();
        Bridge firstAccount = accountBridge("first");
        Bridge secondAccount = accountBridge("second");
        ThingHandler firstAccountHandler = assertInstanceOf(SmartThingsAccountHandler.class,
                factory.create(firstAccount));
        assertInstanceOf(SmartThingsAccountHandler.class, factory.create(secondAccount));
        assertInstanceOf(SmartThingsThingHandler.class, factory.create(childThing("first-washer", firstAccount)));
        assertInstanceOf(SmartThingsThingHandler.class, factory.create(childThing("second-washer", secondAccount)));

        factory.remove(firstAccountHandler);

        assertNull(factory.create(childThing("first-washer-after-removal", firstAccount)));
        assertInstanceOf(SmartThingsThingHandler.class,
                factory.create(childThing("second-washer-after-removal", secondAccount)));
    }

    private TestSmartThingsHandlerFactory createFactory() {
        return new TestSmartThingsHandlerFactory();
    }

    private Bridge accountBridge(String id) {
        return BridgeBuilder.create(SmartThingsBindingConstants.THING_TYPE_ACCOUNT, id).build();
    }

    private Thing childThing(String id, Bridge bridge) {
        ThingUID bridgeUID = bridge.getUID();
        ThingTypeUID thingTypeUID = new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer");

        return ThingBuilder.create(thingTypeUID, id).withBridge(bridgeUID).withLabel(id).build();
    }

    private Thing standaloneChildThing(String id) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(SmartThingsBindingConstants.BINDING_ID, "Washer");

        return ThingBuilder.create(thingTypeUID, id).withLabel(id).build();
    }

    private static class TestSmartThingsHandlerFactory extends SmartThingsHandlerFactory {
        private final SmartThingsAuthService authService;

        TestSmartThingsHandlerFactory() {
            this(new SmartThingsAuthService());
        }

        TestSmartThingsHandlerFactory(SmartThingsAuthService authService) {
            super(mock(HttpService.class), authService, mock(TranslationProvider.class), mock(OAuthFactory.class),
                    mock(HttpClientFactory.class), mock(SmartThingsTypeRegistry.class), mock(ClientBuilder.class),
                    mock(SseEventSourceFactory.class));
            this.authService = authService;
        }

        public @Nullable ThingHandler create(Thing thing) {
            return createHandler(thing);
        }

        public void remove(ThingHandler thingHandler) {
            removeHandler(thingHandler);
        }

        public @Nullable SmartThingsOAuthHandler getOAuthHandler(ThingUID bridgeUID) {
            return authService.getSmartThingsOAuthHandler(bridgeUID);
        }
    }
}
