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
package org.openhab.binding.evcc.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;

/**
 * The {@link EvccLoadpointHandlerTest} is responsible for testing the EvccLoadpointHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccLoadpointHandlerTest {

    private final Thing thing = mock(Thing.class);
    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
    private final ArrayList<String> requestedUrls = new ArrayList<>();
    @Nullable
    private EvccLoadpointHandler handler;

    private EvccLoadpointHandler createHandler() {
        return new EvccLoadpointHandler(thing, channelTypeRegistry) {

            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
            }

            @Override
            protected void updateStatus(ThingStatus status) {
            }

            @Override
            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }

            @Nullable
            @Override
            protected Bridge getBridge() {
                return null;
            }

            @Override
            public void updateThing(Thing thing) {
            }

            @Override
            protected void updateState(ChannelUID channelUID, State state) {
            }

            @Override
            protected void performApiRequest(String url, String method, JsonElement payload) {
                requestedUrls.add(url);
            }
        };
    }

    @BeforeEach
    public void setup() {
        requestedUrls.clear();
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "loadpoint"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("index")).thenReturn("0");
        when(configuration.get("id")).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = createHandler();
    }

    @Test
    public void testEnableAndDisableSettingsUseNestedApiPaths() {
        EvccLoadpointHandler testHandler = Objects.requireNonNull(handler);
        testHandler.endpoint = "http://evcc/api/loadpoints/1";

        testHandler.handleCommand(new ChannelUID("test:thing:uid:loadpoint-enable-threshold"), new DecimalType(100));
        testHandler.handleCommand(new ChannelUID("test:thing:uid:loadpoint-enable-delay"), new DecimalType(10));
        testHandler.handleCommand(new ChannelUID("test:thing:uid:loadpoint-disable-threshold"), new DecimalType(-50));
        testHandler.handleCommand(new ChannelUID("test:thing:uid:loadpoint-disable-delay"), new DecimalType(20));
        ChannelUID modeChannel = new ChannelUID("test:thing:uid:loadpoint-mode");
        testHandler.handleCommand(modeChannel, new StringType("off"));
        testHandler.handleCommand(modeChannel, new StringType("now"));
        testHandler.handleCommand(modeChannel, new StringType("smart"));
        testHandler.handleCommand(modeChannel, new StringType("pv"));
        testHandler.handleCommand(modeChannel, new StringType("minpv"));
        ChannelUID alwaysChargeChannel = new ChannelUID("test:thing:uid:loadpoint-always-charge");
        testHandler.handleCommand(alwaysChargeChannel, new StringType("off"));
        testHandler.handleCommand(alwaysChargeChannel, new StringType("on"));
        testHandler.handleCommand(alwaysChargeChannel, new StringType("once"));

        assertEquals(java.util.List.of("http://evcc/api/loadpoints/1/enable/threshold/100",
                "http://evcc/api/loadpoints/1/enable/delay/10", "http://evcc/api/loadpoints/1/disable/threshold/-50",
                "http://evcc/api/loadpoints/1/disable/delay/20", "http://evcc/api/loadpoints/1/mode/off",
                "http://evcc/api/loadpoints/1/mode/now", "http://evcc/api/loadpoints/1/mode/smart",
                "http://evcc/api/loadpoints/1/mode/pv", "http://evcc/api/loadpoints/1/mode/minpv",
                "http://evcc/api/loadpoints/1/alwayscharge/off", "http://evcc/api/loadpoints/1/alwayscharge/on",
                "http://evcc/api/loadpoints/1/alwayscharge/once"), requestedUrls);
    }
}
