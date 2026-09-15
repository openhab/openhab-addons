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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioSmartHoseSnapshot;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/** Tests bridge-level Smart Hose Timer snapshot reconciliation. */
@NonNullByDefault
@SuppressWarnings("null")
class RachioSmartHoseSnapshotRefreshTest {
    @Test
    void refreshLoadsOneConsistentSnapshotAndUsesCacheUntilItExpires() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = handler(api);
        RachioSmartHoseStatusListener listener = Mockito.mock(RachioSmartHoseStatusListener.class);
        handler.smartHoseStatusListeners.add(listener);

        RachioBaseStation baseStation = baseStation("base-station");
        RachioValve valve = valve("valve");
        RachioValveProgram program = program("program");
        when(api.listBaseStations("")).thenReturn(List.of(baseStation));
        when(api.listValves(baseStation.id)).thenReturn(List.of(valve));
        when(api.listValveProgramsV2ByBaseStation(baseStation.id)).thenReturn(List.of(program));

        RachioSmartHoseSnapshot snapshot = handler.getSmartHoseSnapshot();
        RachioSmartHoseSnapshot cachedSnapshot = handler.getSmartHoseSnapshot();

        assertThat(snapshot.baseStations().get(baseStation.id), sameInstance(baseStation));
        assertThat(snapshot.valves().get(valve.id), sameInstance(valve));
        assertThat(snapshot.programs().get(program.id), sameInstance(program));
        assertThat(valve.baseStationId, is(baseStation.id));
        assertThat(program.baseStationId, is(baseStation.id));
        assertThat(cachedSnapshot, sameInstance(snapshot));
        verify(api, times(1)).listBaseStations("");
        verify(listener, times(1)).onSmartHoseStateChanged(snapshot);
    }

    @Test
    void unchangedContentAdvancesSnapshotAgeWithoutNotifyingHandlers() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = handler(api);
        RachioSmartHoseStatusListener listener = Mockito.mock(RachioSmartHoseStatusListener.class);
        handler.smartHoseStatusListeners.add(listener);
        RachioValve previousValve = valve("valve");
        previousValve.baseStationId = "base-station";
        RachioValveProgram previousProgram = program("program");
        previousProgram.baseStationId = "base-station";
        RachioSmartHoseSnapshot previous = new RachioSmartHoseSnapshot(
                Map.of("base-station", baseStation("base-station")), Map.of("valve", previousValve),
                Map.of("program", previousProgram), Instant.EPOCH);
        setField(handler, "smartHoseSnapshot", previous);

        when(api.listBaseStations("")).thenReturn(List.of(baseStation("base-station")));
        when(api.listValves("base-station")).thenReturn(List.of(valve("valve")));
        when(api.listValveProgramsV2ByBaseStation("base-station")).thenReturn(List.of(program("program")));

        handler.refreshSmartHoseSnapshot(true);

        RachioSmartHoseSnapshot refreshed = getField(handler, "smartHoseSnapshot");
        assertThat(refreshed.retrievedAt().isAfter(previous.retrievedAt()), is(true));
        verify(listener, never()).onSmartHoseStateChanged(refreshed);
    }

    @Test
    void scheduledThrottleRetainsTheLastSuccessfulSnapshot() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = handler(api);
        RachioSmartHoseStatusListener listener = Mockito.mock(RachioSmartHoseStatusListener.class);
        handler.smartHoseStatusListeners.add(listener);
        RachioSmartHoseSnapshot previous = new RachioSmartHoseSnapshot(
                Map.of("base-station", baseStation("base-station")), Map.of(), Map.of(), Instant.EPOCH);
        setField(handler, "smartHoseSnapshot", previous);
        when(api.listBaseStations("")).thenThrow(throttledException());

        handler.refreshSmartHoseSnapshot(true);

        assertThat(getField(handler, "smartHoseSnapshot"), sameInstance(previous));
        verify(listener, never()).onSmartHoseStateChanged(previous);
    }

    private static RachioBridgeHandler handler(RachioApi api) throws ReflectiveOperationException {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, Mockito.mock(HttpClient.class));
        when(api.getPersonId()).thenReturn("");
        setField(handler, "rachioApi", api);
        return handler;
    }

    private static RachioBaseStation baseStation(String id) {
        RachioBaseStation baseStation = new RachioBaseStation();
        baseStation.id = id;
        baseStation.name = "Base station";
        return baseStation;
    }

    private static RachioValve valve(String id) {
        RachioValve valve = new RachioValve();
        valve.id = id;
        valve.name = "Valve";
        return valve;
    }

    private static RachioValveProgram program(String id) {
        RachioValveProgram program = new RachioValveProgram();
        program.id = id;
        program.name = "Program";
        return program;
    }

    private static RachioApiThrottledException throttledException() {
        return new RachioApiThrottledException(new RateLimitThrottleException(Priority.VERY_LOW, 0.1, 0.2),
                new RachioApiResult());
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
