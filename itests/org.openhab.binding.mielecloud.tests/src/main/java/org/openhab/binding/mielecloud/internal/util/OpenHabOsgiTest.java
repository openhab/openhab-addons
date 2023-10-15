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
package org.openhab.binding.mielecloud.internal.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.MIELE_CLOUD_ACCOUNT_LABEL;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.handler.MieleBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Parent class for openHAB OSGi tests offering helper methods for common interactions with the openHAB runtime and its
 * services.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public abstract class OpenHabOsgiTest extends JavaOSGiTest {
    @Nullable
    private Inbox inbox;
    @Nullable
    private ThingRegistry thingRegistry;

    protected Inbox getInbox() {
        assertNotNull(inbox);
        return Objects.requireNonNull(inbox);
    }

    protected ThingRegistry getThingRegistry() {
        assertNotNull(thingRegistry);
        return Objects.requireNonNull(thingRegistry);
    }

    @BeforeEach
    public void setUpEshOsgiTest() {
        registerVolatileStorageService();
        inbox = getService(Inbox.class);
        setUpThingRegistry();
    }

    private void setUpThingRegistry() {
        thingRegistry = getService(ThingRegistry.class, ThingRegistry.class);
        assertNotNull(thingRegistry, "Thing registry is missing");
    }

    /**
     * Sets up a {@link Bridge} with an attached {@link MieleBridgeHandler} and registers it with the
     * {@link ManagedThingProvider} and {@link ThingRegistry}.
     */
    public void setUpBridge() {
        ManagedThingProvider managedThingProvider = getService(ManagedThingProvider.class);
        assertNotNull(managedThingProvider);

        Bridge bridge = BridgeBuilder
                .create(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
                        MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID)
                .withConfiguration(new Configuration(Map.of(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL,
                        MieleCloudBindingIntegrationTestConstants.EMAIL)))
                .withLabel(MIELE_CLOUD_ACCOUNT_LABEL).build();
        assertNotNull(bridge);

        managedThingProvider.add(bridge);

        waitForAssert(() -> {
            assertNotNull(bridge.getHandler());
            assertTrue(bridge.getHandler() instanceof MieleBridgeHandler, "Handler type is wrong");
        });
    }

    /**
     * Registers a volatile storage service.
     */
    @Override
    protected void registerVolatileStorageService() {
        registerService(new VolatileStorageService());
    }
}
