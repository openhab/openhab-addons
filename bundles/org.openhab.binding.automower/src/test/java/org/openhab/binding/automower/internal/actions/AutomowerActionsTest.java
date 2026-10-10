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
package org.openhab.binding.automower.internal.actions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.HeadlightMode;
import org.openhab.binding.automower.internal.things.AutomowerCommand;
import org.openhab.binding.automower.internal.things.AutomowerHandler;

class AutomowerActionsTest {
    @Test
    void lifecycleActionsDelegateToHandler() {
        AutomowerHandler handler = mockHandler();
        AutomowerActions actions = actionsFor(handler);

        actions.confirmError();
        actions.resetCuttingBladeUsageTime();
        actions.poll();

        verify(handler).sendAutomowerConfirmError();
        verify(handler).sendAutomowerResetCuttingBladeUsageTime();
        verify(handler).poll();
    }

    @Test
    void mutationActionsDelegateAllArguments() {
        AutomowerHandler handler = mockHandler();
        AutomowerActions actions = actionsFor(handler);
        short[] starts = { 480 };
        short[] durations = { 389 };
        boolean[] enabledDays = { true };
        boolean[] disabledDays = { false };

        actions.setSettings((byte) 7, "EVENING_ONLY");
        actions.setWorkArea(17746L, true, (byte) 5);
        actions.setWorkAreaName(17746L, "Autumn");
        actions.setWorkAreaOrientation(17746L, 45, 10);
        actions.setStayOutZone("zone-1", false);
        actions.setCalendarTask(17746L, starts, durations, enabledDays, disabledDays, enabledDays, disabledDays,
                enabledDays, disabledDays, disabledDays);

        verify(handler).sendAutomowerSettings((byte) 7, HeadlightMode.EVENING_ONLY);
        verify(handler).sendAutomowerWorkArea(17746L, true, (byte) 5);
        verify(handler).sendAutomowerWorkAreaName("17746", "Autumn");
        verify(handler).sendAutomowerWorkAreaOrientation("17746", 45, 10);
        verify(handler).sendAutomowerStayOutZone("zone-1", false);
        verify(handler).sendAutomowerCalendarTask(17746L, starts, durations, enabledDays, disabledDays, enabledDays,
                disabledDays, enabledDays, disabledDays, disabledDays);
    }

    @Test
    void commandActionsDelegateParameters() {
        AutomowerHandler handler = mockHandler();
        AutomowerActions actions = actionsFor(handler);

        actions.start(30);
        actions.startInWorkArea(17746L, 45L);
        actions.pause();
        actions.resumeSchedule();
        actions.park(60);
        actions.parkUntilNextSchedule();
        actions.parkUntilFurtherNotice();
        actions.parkWithExternalReason(90, 200001);

        verify(handler).sendAutomowerCommand(AutomowerCommand.START, 30);
        verify(handler).sendAutomowerCommand(AutomowerCommand.START_IN_WORK_AREA, 17746L, 45L);
        verify(handler).sendAutomowerCommand(AutomowerCommand.PAUSE);
        verify(handler).sendAutomowerCommand(AutomowerCommand.RESUME_SCHEDULE);
        verify(handler).sendAutomowerCommand(AutomowerCommand.PARK, 60);
        verify(handler).sendAutomowerCommand(AutomowerCommand.PARK_UNTIL_NEXT_SCHEDULE);
        verify(handler).sendAutomowerCommand(AutomowerCommand.PARK_UNTIL_FURTHER_NOTICE);
        verify(handler).sendAutomowerCommand(AutomowerCommand.PARK, null, 90L, 200001L);
    }

    @Test
    void invalidExternalReasonDoesNotReachHandler() {
        AutomowerHandler handler = mockHandler();
        AutomowerActions actions = actionsFor(handler);

        actions.parkWithExternalReason(90, 199999);
        actions.parkWithExternalReason(1501, 200001);

        verifyNoInteractions(handler);
    }

    @Test
    void settingsActionAllowsCuttingHeightWithoutHeadlightMode() {
        AutomowerHandler handler = mockHandler();
        AutomowerActions actions = actionsFor(handler);

        actions.setSettings((byte) 7, null);

        verify(handler).sendAutomowerSettings((byte) 7, null);
    }

    @Test
    void invalidHeadlightModeDoesNotReachHandler() {
        AutomowerHandler handler = mockHandler();
        AutomowerActions actions = actionsFor(handler);

        actions.setSettings((byte) 7, "INVALID_MODE");

        verifyNoInteractions(handler);
    }

    private AutomowerActions actionsFor(AutomowerHandler handler) {
        AutomowerActions actions = new AutomowerActions();
        actions.setThingHandler(handler);
        return actions;
    }

    @SuppressWarnings("all")
    private AutomowerHandler mockHandler() {
        return mock(AutomowerHandler.class);
    }
}
