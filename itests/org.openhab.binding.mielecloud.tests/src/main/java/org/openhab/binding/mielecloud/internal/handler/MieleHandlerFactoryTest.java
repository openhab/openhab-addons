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
package org.openhab.binding.mielecloud.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.*;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.auth.OpenHabOAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class MieleHandlerFactoryTest extends JavaOSGiTest {
    private static final String DEVICE_IDENTIFIER = "000124430016";

    private static final ThingUID WASHING_MACHINE_TYPE = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_WASHING_MACHINE, DEVICE_IDENTIFIER);
    private static final ThingUID OVEN_DEVICE_TYPE = new ThingUID(MieleCloudBindingConstants.THING_TYPE_OVEN,
            DEVICE_IDENTIFIER);
    private static final ThingUID HOB_DEVICE_TYPE = new ThingUID(MieleCloudBindingConstants.THING_TYPE_HOB,
            DEVICE_IDENTIFIER);
    private static final ThingUID FRIDGE_FREEZER_DEVICE_TYPE = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_FRIDGE_FREEZER, DEVICE_IDENTIFIER);
    private static final ThingUID HOOD_DEVICE_TYPE = new ThingUID(MieleCloudBindingConstants.THING_TYPE_HOOD,
            DEVICE_IDENTIFIER);
    private static final ThingUID COFFEE_DEVICE_TYPE = new ThingUID(MieleCloudBindingConstants.THING_TYPE_COFFEE_SYSTEM,
            DEVICE_IDENTIFIER);
    private static final ThingUID WINE_STORAGE_DEVICE_TYPE = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_WINE_STORAGE, DEVICE_IDENTIFIER);
    private static final ThingUID DRYER_DEVICE_TYPE = new ThingUID(MieleCloudBindingConstants.THING_TYPE_DRYER,
            DEVICE_IDENTIFIER);
    private static final ThingUID DISHWASHER_DEVICE_TYPE = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_DISHWASHER, DEVICE_IDENTIFIER);
    private static final ThingUID DISH_WARMER_DEVICE_TYPE = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_DISH_WARMER, DEVICE_IDENTIFIER);
    private static final ThingUID ROBOTIC_VACUUM_CLEANER_DEVICE_TYPE = new ThingUID(
            MieleCloudBindingConstants.THING_TYPE_ROBOTIC_VACUUM_CLEANER, DEVICE_IDENTIFIER);

    @Nullable
    private ThingRegistry thingRegistry;

    private ThingRegistry getThingRegistry() {
        assertNotNull(thingRegistry);
        return Objects.requireNonNull(thingRegistry);
    }

    @BeforeEach
    public void setUp() throws Exception {
        registerVolatileStorageService();
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertNotNull(thingRegistry, "Thing registry is missing");

        // Ensure the MieleWebservice is not initialized.
        MieleHandlerFactory factory = getService(ThingHandlerFactory.class, MieleHandlerFactory.class);
        assertNotNull(factory);

        // Assume an access token has already been stored
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(ACCESS_TOKEN);

        OAuthClientService oAuthClientService = mock(OAuthClientService.class);
        when(oAuthClientService.getAccessTokenResponse()).thenReturn(accessTokenResponse);

        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        when(oAuthFactory.getOAuthClientService(MieleCloudBindingIntegrationTestConstants.EMAIL))
                .thenReturn(oAuthClientService);

        OpenHabOAuthTokenRefresher tokenRefresher = getService(OAuthTokenRefresher.class,
                OpenHabOAuthTokenRefresher.class);
        assertNotNull(tokenRefresher);
        setPrivate(Objects.requireNonNull(tokenRefresher), "oauthFactory", oAuthFactory);
    }

    @Test
    public void testHandlerCanBeCreatedForGenesisBridge() throws Exception {
        // when:
        Bridge bridge = BridgeBuilder
                .create(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
                        MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID)
                .withConfiguration(
                        new Configuration(Collections.singletonMap(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL,
                                MieleCloudBindingIntegrationTestConstants.EMAIL)))
                .withLabel(MIELE_CLOUD_ACCOUNT_LABEL).build();
        assertNotNull(bridge);

        getThingRegistry().add(bridge);

        // then:
        waitForAssert(() -> {
            assertNotNull(bridge.getHandler());
            assertTrue(bridge.getHandler() instanceof MieleBridgeHandler, "Handler type is wrong");
        });

        MieleBridgeHandler handler = (MieleBridgeHandler) bridge.getHandler();
        assertNotNull(handler);
    }

    @Test
    public void testWebserviceIsInitializedOnHandlerInitialization() throws Exception {
        // given:
        Bridge bridge = BridgeBuilder
                .create(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
                        MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID)
                .withConfiguration(
                        new Configuration(Collections.singletonMap(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL,
                                MieleCloudBindingIntegrationTestConstants.EMAIL)))
                .withLabel(MIELE_CLOUD_ACCOUNT_LABEL).build();
        assertNotNull(bridge);

        getThingRegistry().add(bridge);

        waitForAssert(() -> {
            assertNotNull(bridge.getHandler());
            assertTrue(bridge.getHandler() instanceof MieleBridgeHandler, "Handler type is wrong");
        });

        MieleBridgeHandler handler = (MieleBridgeHandler) bridge.getHandler();
        assertNotNull(handler);

        // when:
        handler.initialize();

        // then:
        assertEquals(ACCESS_TOKEN,
                handler.getThing().getProperties().get(MieleCloudBindingConstants.PROPERTY_ACCESS_TOKEN));

        MieleWebservice webservice = getPrivate(handler, "webService");
        assertNotNull(webservice);
        Optional<String> accessToken = getPrivate(webservice, "accessToken");
        assertEquals(Optional.of(ACCESS_TOKEN), accessToken);
    }

    private void verifyHandlerCreation(MieleWebservice webservice, Thing thing,
            Class<? extends ThingHandler> expectedHandlerClass)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        getThingRegistry().add(thing);

        // then:
        waitForAssert(() -> {
            ThingHandler handler = thing.getHandler();
            assertNotNull(handler);
            assertTrue(expectedHandlerClass.isAssignableFrom(handler.getClass()), "Handler type is wrong");
        });
    }

    private void testHandlerCanBeCreatedForMieleDevice(ThingTypeUID thingTypeUid, ThingUID thingUid, String label,
            Class<? extends ThingHandler> expectedHandlerClass)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        // given:
        MieleWebservice webservice = mock(MieleWebservice.class);

        MieleHandlerFactory factory = getService(ThingHandlerFactory.class, MieleHandlerFactory.class);
        assertNotNull(factory);

        // when:
        Thing device = ThingBuilder.create(thingTypeUid, thingUid)
                .withConfiguration(new Configuration(Collections
                        .singletonMap(MieleCloudBindingConstants.CONFIG_PARAM_DEVICE_IDENTIFIER, DEVICE_IDENTIFIER)))
                .withLabel(label).build();

        assertNotNull(device);
        verifyHandlerCreation(webservice, device, expectedHandlerClass);
    }

    @Test
    public void testHandlerCanBeCreatedForGenesisBridgeWithEmptyConfiguration() throws Exception {
        // when:
        Bridge bridge = BridgeBuilder
                .create(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
                        MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID)
                .withConfiguration(
                        new Configuration(Collections.singletonMap(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL,
                                MieleCloudBindingIntegrationTestConstants.EMAIL)))
                .withLabel(MIELE_CLOUD_ACCOUNT_LABEL).build();
        assertNotNull(bridge);

        getThingRegistry().add(bridge);

        // then:
        waitForAssert(() -> {
            assertNotNull(bridge.getHandler());
            assertTrue(bridge.getHandler() instanceof MieleBridgeHandler, "Handler type is wrong");
        });
    }

    @Test
    public void testHandlerCanBeCreatedForWashingDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_WASHING_MACHINE,
                WASHING_MACHINE_TYPE, "DA-6996", WashingDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForOvenDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_OVEN, OVEN_DEVICE_TYPE, "OV-6887",
                OvenDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForHobDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_HOB, HOB_DEVICE_TYPE, "HB-3887",
                HobDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForFridgeFreezerDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_FRIDGE_FREEZER,
                FRIDGE_FREEZER_DEVICE_TYPE, "CD-6097", CoolingDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForHoodDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_HOOD, HOOD_DEVICE_TYPE, "HD-2097",
                HoodDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForCoffeeDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_COFFEE_SYSTEM, COFFEE_DEVICE_TYPE,
                "DA-6997", CoffeeSystemThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForWineStorageDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_WINE_STORAGE,
                WINE_STORAGE_DEVICE_TYPE, "WS-6907", WineStorageDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForDryerDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_DRYER, DRYER_DEVICE_TYPE, "DR-0907",
                DryerDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForDishwasherDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_DISHWASHER, DISHWASHER_DEVICE_TYPE,
                "DR-0907", DishwasherDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForDishWarmerDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_DISH_WARMER,
                DISH_WARMER_DEVICE_TYPE, "DW-0907", DishWarmerDeviceThingHandler.class);
    }

    @Test
    public void testHandlerCanBeCreatedForRoboticVacuumCleanerDevice()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        testHandlerCanBeCreatedForMieleDevice(MieleCloudBindingConstants.THING_TYPE_ROBOTIC_VACUUM_CLEANER,
                ROBOTIC_VACUUM_CLEANER_DEVICE_TYPE, "RVC-0907", RoboticVacuumCleanerDeviceThingHandler.class);
    }

    /**
     * Registers a volatile storage service.
     */
    @Override
    protected void registerVolatileStorageService() {
        registerService(new VolatileStorageService());
    }
}
