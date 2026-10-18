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
package org.openhab.binding.avmfritz.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.binding.avmfritz.internal.AVMFritzDynamicCommandDescriptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests for {@link AVMFritzThingHandlerOSGiTest}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public abstract class AVMFritzThingHandlerOSGiTest extends JavaOSGiTest {

    protected @NonNullByDefault({}) Bridge bridge;
    protected @NonNullByDefault({}) BoxHandler bridgeHandler;

    @BeforeEach
    public void setUp() {
        bridge = buildBridge();
        assertNotNull(bridge.getConfiguration());

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);

        bridgeHandler = new BoxHandler(bridge, mock(), mock(AVMFritzDynamicCommandDescriptionProvider.class));
        assertNotNull(bridgeHandler);

        bridgeHandler.setCallback(callback);
        bridge.setHandler(bridgeHandler);
        assertNotNull(bridge.getHandler());

        // Discovery tests only need the handler's type and UID mapping. Initializing it would authenticate against
        // the configured host and make these tests depend on the network.
    }

    private Bridge buildBridge() {
        return BridgeBuilder.create(BRIDGE_THING_TYPE, "1") //
                .withLabel(BOX_MODEL_NAME) //
                .withConfiguration(new Configuration(Map.of( //
                        CONFIG_IP_ADDRESS, "fritz.box", //
                        CONFIG_PROTOCOL, "http", //
                        CONFIG_USER, "user", //
                        CONFIG_PASSWORD, "password", //
                        CONFIG_POLLING_INTERVAL, 15, //
                        CONFIG_SYNC_TIMEOUT, 2000))) //
                .build();
    }
}
