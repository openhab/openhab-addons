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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * The {@link EvccPlanHandlerTest} is responsible for testing the EvccPlanHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class EvccPlanHandlerTest extends AbstractThingHandlerTestClass<EvccPlanHandler> {

    private boolean updateStateCalled = false;
    private int updateStateCounter = 0;
    private String capturedUrl = "";
    private JsonElement capturedPayload = JsonNull.INSTANCE;

    @Override
    protected EvccPlanHandler createHandler() {
        return new EvccPlanHandler(thing, channelTypeRegistry, ZoneId.of("Europe/Berlin")) {

            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
                lastThingStatus = status;
                lastThingStatusDetail = detail;
            }

            @Override
            protected void updateStatus(ThingStatus status) {
                lastThingStatus = status;
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
            public void updateState(ChannelUID uid, State state) {
                updateStateCalled = true;
                updateStateCounter++;
            }

            @Override
            protected boolean sendCommand(String url, JsonElement payload) {
                capturedUrl = url;
                capturedPayload = payload;
                return true;
            }
        };
    }

    @SuppressWarnings("null")
    @BeforeEach
    public void setup() {
        when(thing.getUID()).thenReturn(new ThingUID("evcc:plan:uid"));
        when(thing.getProperties())
                .thenReturn(Map.of(PROPERTY_INDEX, "0", PROPERTY_VEHICLE_ID, "vehicle_1", PROPERTY_TYPE, "plan"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get(PROPERTY_INDEX)).thenReturn("0");
        when(configuration.get(PROPERTY_VEHICLE_ID)).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = spy(createHandler());
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        LocaleProvider lp = mock(LocaleProvider.class);
        TranslationProvider tp = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);
        BundleContext ctx = mock(BundleContext.class);
        when(bridgeHandler.getBaseURL()).thenReturn("http://evcc/api");
        when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);
        when(lp.getLocale()).thenReturn(Locale.ENGLISH);
        when(bridgeHandler.getLocaleProvider()).thenReturn(lp);
        when(bridgeHandler.getI18nProvider()).thenReturn(tp);
        when(bundle.getBundleContext()).thenReturn(ctx);

        handler.bridgeHandler = bridgeHandler;
    }

    @Test
    void updatingOneTimePlanShouldNormalizeTimeAndBuildUrl() {
        handler.initialize();
        handler.prepareApiResponseForChannelStateUpdate(exampleResponse);

        assertTrue(updateStateCalled);
        assertEquals(6, updateStateCounter);

        // Set new SoC & time via handleCommand (pending commands collection)
        ChannelUID socCh = new ChannelUID(thing.getUID(), CHANNEL_PLAN_SOC);
        ChannelUID timeCh = new ChannelUID(thing.getUID(), "plan-time");
        ChannelUID precCh = new ChannelUID(thing.getUID(), CHANNEL_PLAN_PRECONDITION);
        ChannelUID updateCh = new ChannelUID(thing.getUID(), CHANNEL_SEND_UPDATE);

        State socState = new StringType("85 %");
        // Offset + milliseconds (should normalize to Instant Z)
        State timeState = new StringType("2025-12-20T09:00:00.000+0100");
        State precState = new StringType("1800 s");

        handler.handleCommand(socCh, (Command) socState);
        handler.handleCommand(timeCh, (Command) timeState);
        handler.handleCommand(precCh, (Command) precState);
        // Trigger update
        handler.handleCommand(updateCh, OnOffType.ON);

        assertFalse(capturedUrl.isEmpty(), "Url should not be empty!");
        // Expect: base / vehicles / vehicle_1 / plan/soc / 85 / 2025-12-20T08:00:00Z ?precondition=1800
        assertTrue(capturedUrl.startsWith("http://evcc/api/vehicles/vehicle_1/plan/soc/85/"));
        assertTrue(capturedUrl.contains("2025-12-20T08:00:00Z")); // normalized to Instant (Z)
        assertTrue(capturedUrl.endsWith("?precondition=1800"));
    }

    @Test
    void updatingRepeatingPlanShouldConvertWeekdaysAndMoveTimeKey() {
        when(thing.getUID()).thenReturn(new ThingUID("evcc:plan:uid"));
        when(thing.getProperties())
                .thenReturn(Map.of(PROPERTY_INDEX, "1", PROPERTY_VEHICLE_ID, "vehicle_1", PROPERTY_TYPE, "plan"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get(PROPERTY_INDEX)).thenReturn("1");
        when(configuration.get(PROPERTY_VEHICLE_ID)).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = spy(createHandler());
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        LocaleProvider lp = mock(LocaleProvider.class);
        TranslationProvider tp = mock(TranslationProvider.class);
        Bundle bundle = mock(Bundle.class);
        BundleContext ctx = mock(BundleContext.class);
        when(bridgeHandler.getBaseURL()).thenReturn("http://evcc/api");
        when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);
        when(lp.getLocale()).thenReturn(Locale.ENGLISH);
        when(bridgeHandler.getLocaleProvider()).thenReturn(lp);
        when(bridgeHandler.getI18nProvider()).thenReturn(tp);
        when(bundle.getBundleContext()).thenReturn(ctx);

        handler.bridgeHandler = bridgeHandler;

        handler.initialize();
        handler.prepareApiResponseForChannelStateUpdate(exampleResponse);

        // Provide new values via handleCommand (pending commands)
        ChannelUID socCh = new ChannelUID(thing.getUID(), CHANNEL_PLAN_SOC);
        ChannelUID timeCh = new ChannelUID(thing.getUID(), "plan-time");
        ChannelUID wdCh = new ChannelUID(thing.getUID(), "plan-weekdays");
        ChannelUID preCCh = new ChannelUID(thing.getUID(), CHANNEL_PLAN_PRECONDITION);
        ChannelUID updateCh = new ChannelUID(thing.getUID(), CHANNEL_SEND_UPDATE);

        State socState = new StringType("85 %");
        State timeState = new StringType("2025-12-20T09:00:00.000+0100");
        State wdState = new StringType("Monday;Wednesday;Sunday"); // Sunday maps to 0
        State precState = new StringType("1800 s");

        handler.handleCommand(socCh, (Command) socState);
        handler.handleCommand(timeCh, (Command) timeState);
        handler.handleCommand(wdCh, (Command) wdState);
        handler.handleCommand(preCCh, (Command) precState);

        // Trigger update
        handler.handleCommand(updateCh, org.openhab.core.library.types.OnOffType.ON);

        assertFalse(capturedUrl.isEmpty(), "Url should not be empty!");
        assertTrue(capturedUrl.startsWith("http://evcc/api/vehicles/vehicle_1/plan/repeating"));
        assertNotEquals(JsonNull.INSTANCE, capturedPayload, "Payload must be captured");
        JsonObject plan = capturedPayload.getAsJsonArray().get(0).getAsJsonObject();

        // repeatingTime should be moved to time in payload
        assertTrue(plan.has("time"));
        assertEquals("08:00", plan.get("time").getAsString());
        assertFalse(plan.has("repeatingTime"));

        // weekdays should be numeric array [1,3,0]
        assertTrue(plan.has("weekdays"));
        JsonArray w = plan.get("weekdays").getAsJsonArray();
        assertEquals(3, w.size());
        assertEquals(1, w.get(0).getAsInt()); // Monday
        assertEquals(3, w.get(1).getAsInt()); // Wednesday
        assertEquals(0, w.get(2).getAsInt()); // Sunday

        // soc and precondition should be numeric primitives
        assertEquals(85, plan.get("soc").getAsInt());
        assertEquals(1800, plan.get("precondition").getAsInt());
    }
}
