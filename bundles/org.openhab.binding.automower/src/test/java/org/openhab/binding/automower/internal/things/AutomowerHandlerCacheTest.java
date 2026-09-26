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
package org.openhab.binding.automower.internal.things;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.automower.internal.bridge.AutomowerBridge;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Action;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Activity;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Battery;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Calendar;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.CalendarTask;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Capabilities;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Headlight;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.HeadlightMode;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.InactiveReason;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Metadata;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mode;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerApp;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerData;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkAreaAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Planner;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.PlannerOverride;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Position;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.RestrictedReason;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Settings;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.State;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Statistics;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZone;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZones;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.WorkArea;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

@SuppressWarnings("all")
class AutomowerHandlerCacheTest {
    @Test
    void successfulCalendarUpdateReplacesOnlyTargetWorkAreaTasks() throws Exception {
        TestContext context = createContext(true, false);
        try {
            CalendarTask oldTargetTask = task((short) 100, 17746L);
            CalendarTask untouchedTask = task((short) 200, 17747L);
            context.mower.getAttributes().getCalendar().setTasks(List.of(oldTargetTask, untouchedTask));

            context.handler.sendAutomowerCalendarTask(17746L, new short[] { 480 }, new short[] { 389 },
                    new boolean[] { true }, new boolean[] { false }, new boolean[] { true }, new boolean[] { false },
                    new boolean[] { true }, new boolean[] { false }, new boolean[] { false });

            verify(context.automowerBridge).sendAutomowerCalendarTask(any(), any(Boolean.class), any(), any());
            List<CalendarTask> cachedTasks = context.mower.getAttributes().getCalendar().getTasks();
            assertEquals(2, cachedTasks.size());
            assertEquals(480, cachedTasks.get(1).getStart());
            assertEquals(17746L, cachedTasks.get(1).getWorkAreaId());
            assertEquals(200, cachedTasks.get(0).getStart());
            assertEquals(17747L, cachedTasks.get(0).getWorkAreaId());
        } finally {
            context.handler.dispose();
        }
    }

    @Test
    void failedCalendarUpdateLeavesCachedTasksUnchanged() throws Exception {
        TestContext context = createContext(true, false);
        try {
            CalendarTask originalTask = task((short) 100, 17746L);
            context.mower.getAttributes().getCalendar().setTasks(List.of(originalTask));
            doThrow(new AutomowerCommunicationException("calendar unavailable")).when(context.automowerBridge)
                    .sendAutomowerCalendarTask(any(), any(Boolean.class), any(), any());

            context.handler.sendAutomowerCalendarTask(17746L, new short[] { 480 }, new short[] { 389 },
                    new boolean[] { true }, new boolean[] { false }, new boolean[] { true }, new boolean[] { false },
                    new boolean[] { true }, new boolean[] { false }, new boolean[] { false });

            assertEquals(1, context.mower.getAttributes().getCalendar().getTasks().size());
            assertEquals(100, context.mower.getAttributes().getCalendar().getTasks().get(0).getStart());
        } finally {
            context.handler.dispose();
        }
    }

    @Test
    void successfulSettingsAndWorkAreaUpdatesChangeLocalCache() throws Exception {
        TestContext context = createContext(false, false);
        try {
            context.handler.sendAutomowerSettings((byte) 7, HeadlightMode.EVENING_ONLY);
            context.handler.sendAutomowerWorkArea(17746L, false, (byte) 4, "Autumn", 45, 10);

            assertEquals(7, context.mower.getAttributes().getSettings().getCuttingHeight());
            assertEquals(HeadlightMode.EVENING_ONLY,
                    context.mower.getAttributes().getSettings().getHeadlight().getHeadlightMode());
            WorkArea workArea = context.mower.getAttributes().getWorkAreas().get(0);
            assertEquals("Autumn", workArea.getName());
            assertEquals(4, workArea.getCuttingHeight());
            assertEquals(45, workArea.getOrientation());
            assertEquals(10, workArea.getOrientationShift());
        } finally {
            context.handler.dispose();
        }
    }

    @Test
    void failedSettingsAndWorkAreaUpdatesPreserveLocalCache() throws Exception {
        TestContext context = createContext(false, false);
        try {
            doThrow(new AutomowerCommunicationException("settings unavailable")).when(context.automowerBridge)
                    .sendAutomowerSettings(any(), any());
            doThrow(new AutomowerCommunicationException("work area unavailable")).when(context.automowerBridge)
                    .sendAutomowerWorkArea(any(), anyLong(), any(MowerWorkAreaAttributes.class));

            context.handler.sendAutomowerSettings((byte) 7, HeadlightMode.EVENING_ONLY);
            context.handler.sendAutomowerWorkArea(17746L, false, (byte) 4, "Autumn", 45, 10);

            assertEquals(0, context.mower.getAttributes().getSettings().getCuttingHeight());
            assertEquals(null, context.mower.getAttributes().getSettings().getHeadlight().getHeadlightMode());
            WorkArea workArea = context.mower.getAttributes().getWorkAreas().get(0);
            assertEquals("Original", workArea.getName());
            assertEquals(3, workArea.getCuttingHeight());
            assertEquals(10, workArea.getOrientation());
            assertEquals(2, workArea.getOrientationShift());
        } finally {
            context.handler.dispose();
        }
    }

    @Test
    void successfulStayOutZoneUpdateChangesLocalCache() throws Exception {
        TestContext context = createContext(false, true);
        try {
            context.handler.sendAutomowerStayOutZone("zone-1", false);

            assertEquals(false, context.mower.getAttributes().getStayOutZones().getZones().get(0).isEnabled());
        } finally {
            context.handler.dispose();
        }
    }

    private TestContext createContext(boolean hasWorkAreas, boolean hasStayOutZones) throws Exception {
        Thing thing = mock(Thing.class);
        Bridge bridge = mock(Bridge.class);
        AutomowerBridgeHandler bridgeHandler = mock(AutomowerBridgeHandler.class);
        AutomowerBridge automowerBridge = mock(AutomowerBridge.class);
        ThingUID bridgeUID = new ThingUID("automower:bridge:bridge-1");
        when(thing.getUID()).thenReturn(new ThingUID("automower:automower:mower-1"));
        when(thing.getBridgeUID()).thenReturn(bridgeUID);
        when(thing.getThingTypeUID()).thenReturn(AutomowerHandler.SUPPORTED_THING_TYPES.iterator().next());
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridgeHandler.getAutomowerBridge()).thenReturn(automowerBridge);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);

        TimeZoneProvider timeZoneProvider = () -> ZoneId.of("UTC");
        AutomowerHandler handler = new AutomowerHandler(thing, timeZoneProvider);
        handler.setCallback(callback);
        Mower mower = new Mower();
        mower.setId("mower-1");
        MowerData data = new MowerData();
        data.setMetadata(new Metadata());
        data.setBattery(new Battery());
        org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.System system = new org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.System();
        system.setName("Automower");
        system.setModel("435X AWD");
        data.setSystem(system);
        data.setCapabilities(capabilities(hasWorkAreas, hasStayOutZones));
        MowerApp mowerApp = new MowerApp();
        mowerApp.setMode(Mode.MAIN_AREA);
        mowerApp.setActivity(Activity.MOWING);
        mowerApp.setInactiveReason(InactiveReason.NONE);
        mowerApp.setState(State.IN_OPERATION);
        mowerApp.setIsErrorConfirmable(false);
        data.setMower(mowerApp);
        data.setCalendar(new Calendar());
        data.setPlanner(new Planner());
        data.getPlanner().setOverride(new PlannerOverride());
        data.getPlanner().getOverride().setAction(Action.NOT_ACTIVE);
        data.getPlanner().setRestrictedReason(RestrictedReason.NONE);
        data.addPosition(new Position());
        Settings settings = new Settings();
        settings.setHeadlight(new Headlight());
        data.setSettings(settings);
        data.setStatistics(new Statistics());
        WorkArea workArea = new WorkArea();
        workArea.setWorkAreaId(17746L);
        workArea.setName("Original");
        workArea.setCuttingHeight((byte) 3);
        workArea.setOrientation(10);
        workArea.setOrientationShift(2);
        data.setWorkAreas(List.of(workArea));
        StayOutZone stayOutZone = new StayOutZone();
        stayOutZone.setId("zone-1");
        stayOutZone.setEnabled(true);
        StayOutZones stayOutZones = new StayOutZones();
        stayOutZones.setDirty(false);
        stayOutZones.setZones(List.of(stayOutZone));
        data.setStayOutZones(stayOutZones);
        mower.setAttributes(data);
        setField(handler, "mowerState", mower);
        return new TestContext(handler, automowerBridge, mower);
    }

    private Capabilities capabilities(boolean hasWorkAreas, boolean hasStayOutZones) throws Exception {
        Capabilities capabilities = new Capabilities();
        setField(capabilities, "workAreas", hasWorkAreas);
        setField(capabilities, "stayOutZones", hasStayOutZones);
        return capabilities;
    }

    private CalendarTask task(short start, long workAreaId) {
        CalendarTask task = new CalendarTask();
        task.setStart(start);
        task.setDuration((short) 60);
        task.setWorkAreaId(workAreaId);
        return task;
    }

    private void setField(Object target, String fieldName, @Nullable Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private record TestContext(AutomowerHandler handler, AutomowerBridge automowerBridge, Mower mower) {
    }
}
