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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;

@SuppressWarnings("all")
class AutomowerHandlerCommandTest {
    @Test
    void handleCommandDispatchesSettingsStatusStatisticsAndRefresh() {
        AutomowerHandler handler = spy(createHandler());
        doNothing().when(handler).sendAutomowerSettingsCuttingHeight((byte) 7);
        doNothing().when(handler).sendAutomowerSettingsHeadlightMode("ALWAYS_ON");
        doNothing().when(handler).sendAutomowerConfirmError();
        doNothing().when(handler).poll();
        doNothing().when(handler).sendAutomowerResetCuttingBladeUsageTime();
        doNothing().when(handler).updateAutomowerState();

        handler.handleCommand(channel(CHANNEL_SETTING_CUTTING_HEIGHT), new DecimalType(7));
        handler.handleCommand(channel(CHANNEL_SETTING_HEADLIGHT_MODE), new StringType("ALWAYS_ON"));
        handler.handleCommand(channel(CHANNEL_STATUS_ERROR_CODE), new DecimalType(0));
        handler.handleCommand(channel(CHANNEL_STATUS_POLL_UPDATE), OnOffType.ON);
        handler.handleCommand(channel(CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME), new DecimalType(0));
        handler.handleCommand(channel(CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME), new QuantityType<>(0, Units.SECOND));
        handler.handleCommand(channel(CHANNEL_STATUS_NAME), RefreshType.REFRESH);

        verify(handler).sendAutomowerSettingsCuttingHeight((byte) 7);
        verify(handler).sendAutomowerSettingsHeadlightMode("ALWAYS_ON");
        verify(handler).sendAutomowerConfirmError();
        verify(handler).poll();
        verify(handler, times(2)).sendAutomowerResetCuttingBladeUsageTime();
        verify(handler).updateAutomowerState();
    }

    @Test
    void handleCommandDispatchesCalendarAndCommandChannels() {
        AutomowerHandler handler = spy(createHandler());
        doNothing().when(handler).sendAutomowerCalendarTask(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.anyString());
        doNothing().when(handler).sendAutomowerCommand(org.mockito.ArgumentMatchers.any(AutomowerCommand.class),
                org.mockito.ArgumentMatchers.anyLong());
        doNothing().when(handler).sendAutomowerCommand(org.mockito.ArgumentMatchers.any(AutomowerCommand.class),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.isNull());
        doNothing().when(handler).sendAutomowerCommand(org.mockito.ArgumentMatchers.any(AutomowerCommand.class));

        handler.handleCommand(channel(GROUP_CALENDARTASK + "01-" + CHANNEL_CALENDARTASK_START), new DecimalType(480));
        handler.handleCommand(channel(CHANNEL_COMMAND_START), new DecimalType(30));
        handler.handleCommand(channel(CHANNEL_COMMAND_START_IN_WORK_AREA), new DecimalType(17746));
        handler.handleCommand(channel(CHANNEL_COMMAND_PAUSE), OnOffType.ON);

        verify(handler).sendAutomowerCalendarTask(new DecimalType(480), 0, null, CHANNEL_CALENDARTASK_START);
        verify(handler).sendAutomowerCommand(AutomowerCommand.START, 30L);
        verify(handler).sendAutomowerCommand(AutomowerCommand.START_IN_WORK_AREA, 17746L, null);
        verify(handler).sendAutomowerCommand(AutomowerCommand.PAUSE);
    }

    @Test
    void handleCommandIgnoresUnsupportedOrNonTriggeringCommands() {
        AutomowerHandler handler = spy(createHandler());

        handler.handleCommand(channel(CHANNEL_STATUS_ERROR_CODE), new DecimalType(1));
        handler.handleCommand(channel(CHANNEL_STATISTIC_CUTTING_BLADE_USAGE_TIME), new DecimalType(1));
        handler.handleCommand(channel("unsupported#channel"), OnOffType.ON);
        handler.handleCommand(new ChannelUID(new ThingUID("automower:automower:mower-1"), "invalid"), OnOffType.ON);

        verify(handler, never()).sendAutomowerConfirmError();
        verify(handler, never()).sendAutomowerResetCuttingBladeUsageTime();
        verify(handler, never()).poll();
        verify(handler, never()).sendAutomowerCommand(org.mockito.ArgumentMatchers.any(AutomowerCommand.class));
    }

    @SuppressWarnings("all")
    private AutomowerHandler createHandler() {
        Thing thing = Mockito.mock(Thing.class);
        ThingUID thingUID = new ThingUID("automower:automower:mower-1");
        Mockito.when(thing.getUID()).thenReturn(thingUID);
        AutomowerHandler handler = new AutomowerHandler(thing, (TimeZoneProvider) () -> ZoneId.of("UTC"));
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        return handler;
    }

    private ChannelUID channel(String channelId) {
        return new ChannelUID(new ThingUID("automower:automower:mower-1"), channelId);
    }
}
