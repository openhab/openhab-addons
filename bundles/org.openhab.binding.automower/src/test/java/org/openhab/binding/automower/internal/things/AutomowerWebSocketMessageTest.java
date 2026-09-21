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

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Calendar;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.CalendarTask;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Capabilities;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Headlight;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.HeadlightMode;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Message;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MessageAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Metadata;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerApp;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerData;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerMessages;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Planner;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.PlannerOverride;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Position;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Settings;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonParser;

class AutomowerWebSocketMessageTest {
    @Test
    void toZonedDateTimeUsesMowerTimeZoneWithoutChangingLocalTime() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();
        ZoneId mowerZone = ZoneId.of("Europe/Berlin");
        ZonedDateTime apiTimestamp = ZonedDateTime.of(2024, 1, 15, 12, 34, 56, 0, ZoneId.of("UTC"));

        ZonedDateTime result = handler.toZonedDateTime(apiTimestamp.toInstant().toEpochMilli(), mowerZone);

        assertEquals(ZonedDateTime.of(2024, 1, 15, 12, 34, 56, 0, mowerZone), result);
    }

    @Test
    void positionEventV2UpdatesCoordinates() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "position-event-v2",
                  "attributes": {
                    "position": {
                      "latitude": 49.4886,
                      "longitude": 11.4403
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        assertEquals(49.4886, mower.getAttributes().getLastPosition().getLatitude());
        assertEquals(11.4403, mower.getAttributes().getLastPosition().getLongitude());
    }

    @Test
    void plannerEventV2UpdatesPlannerFields() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "planner-event-v2",
                  "attributes": {
                    "planner": {
                      "nextStartTimestamp": 1720000000,
                      "override": {
                        "action": "FORCE_MOW"
                      },
                      "restrictedReason": "WEEK_SCHEDULE",
                      "externalReason": 42
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        Planner planner = mower.getAttributes().getPlanner();
        assertEquals(1720000000L, planner.getNextStartTimestamp());
        assertEquals(org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Action.FORCE_MOW,
                planner.getOverride().getAction());
        assertEquals(
                org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.RestrictedReason.WEEK_SCHEDULE,
                planner.getRestrictedReason());
        assertEquals(42, planner.getExternalReason());
    }

    @Test
    void mowerEventV2UpdatesMowerFields() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "mower-event-v2",
                  "attributes": {
                    "mower": {
                      "mode": "MAIN_AREA",
                      "activity": "PARKED_IN_CS",
                      "inactiveReason": "NONE",
                      "state": "ERROR",
                      "errorCode": 123,
                      "isErrorConfirmable": true,
                      "errorCodeTimestamp": 1720000000,
                      "workAreaId": 17746
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        MowerApp mowerApp = mower.getAttributes().getMower();
        assertEquals(org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mode.MAIN_AREA,
                mowerApp.getMode());
        assertEquals(org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Activity.PARKED_IN_CS,
                mowerApp.getActivity());
        assertEquals(org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.InactiveReason.NONE,
                mowerApp.getInactiveReason());
        assertEquals(org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.State.ERROR,
                mowerApp.getState());
        assertEquals(123, mowerApp.getErrorCode());
        assertEquals(true, mowerApp.getIsErrorConfirmable());
        assertEquals(1720000000L, mowerApp.getErrorCodeTimestamp());
        assertEquals(17746L, mowerApp.getWorkAreaId());
    }

    @Test
    void headlightsEventV2UpdatesHeadlightMode() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "headlights-event-v2",
                  "attributes": {
                    "headlight": {
                      "mode": "EVENING_ONLY"
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        assertEquals(HeadlightMode.EVENING_ONLY, mower.getAttributes().getSettings().getHeadlight().getHeadlightMode());
    }

    @Test
    void cuttingHeightEventV2UpdatesCuttingHeight() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "cuttingHeight-event-v2",
                  "attributes": {
                    "cuttingHeight": {
                      "height": 5
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        assertEquals(5, mower.getAttributes().getSettings().getCuttingHeight());
    }

    @Test
    void calendarEventV2UpdatesTasksInMinutesAndWorkArea() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "calendar-event-v2",
                  "attributes": {
                    "calendar": {
                      "tasks": [
                        {
                          "start": 480,
                          "duration": 389,
                          "workAreaId": 17746,
                          "monday": true,
                          "tuesday": true,
                          "wednesday": true,
                          "thursday": false,
                          "friday": true,
                          "saturday": false,
                          "sunday": false
                        }
                      ]
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        CalendarTask task = mower.getAttributes().getCalendar().getTasks().get(0);
        assertEquals(480, task.getStart());
        assertEquals(389, task.getDuration());
        assertEquals(17746L, task.getWorkAreaId());
        assertEquals(true, task.getMonday());
        assertEquals(true, task.getTuesday());
        assertEquals(true, task.getWednesday());
        assertEquals(true, task.getFriday());
    }

    @Test
    void batteryEventV2UpdatesBatteryPercent() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "battery-event-v2",
                  "attributes": {
                    "battery": {
                      "batteryPercent": 87
                    }
                  }
                }
                """).getAsJsonObject());

        Mower mower = (Mower) getField(handler, "mowerState");
        assertEquals(87, mower.getAttributes().getBattery().getBatteryPercent());
    }

    @Test
    void messageEventV2UpdatesMessageFields() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();
        MowerMessages mowerMessages = new MowerMessages();
        MessageAttributes messageAttributes = new MessageAttributes();
        Message message = new Message();
        messageAttributes.getMessages().add(message);
        mowerMessages.setAttributes(messageAttributes);
        setField(handler, "mowerMessages", mowerMessages);

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "message-event-v2",
                  "attributes": {
                    "message": {
                      "time": 1720000000,
                      "code": 149,
                      "severity": "ERROR",
                      "latitude": 49.4886,
                      "longitude": 11.4403
                    }
                  }
                }
                """).getAsJsonObject());

        assertEquals(1720000000L, message.getTime());
        assertEquals(149, message.getCode());
        assertEquals("ERROR", message.getSeverity());
        assertEquals(49.4886, message.getLatitude());
        assertEquals(11.4403, message.getLongitude());
    }

    @Test
    void messageEventV2ClearsMissingGpsCoordinates() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();
        MowerMessages mowerMessages = new MowerMessages();
        MessageAttributes messageAttributes = new MessageAttributes();
        Message message = new Message();
        message.setLatitude(49.4886);
        message.setLongitude(11.4403);
        messageAttributes.getMessages().add(message);
        mowerMessages.setAttributes(messageAttributes);
        setField(handler, "mowerMessages", mowerMessages);

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "message-event-v2",
                  "attributes": {
                    "message": {
                      "time": 1720000001,
                      "code": 150,
                      "severity": "WARNING"
                    }
                  }
                }
                """).getAsJsonObject());

        assertEquals(1720000001L, message.getTime());
        assertEquals(150, message.getCode());
        assertEquals("WARNING", message.getSeverity());
        assertEquals(null, message.getLatitude());
        assertEquals(null, message.getLongitude());
    }

    @Test
    void legacyMessagesEventIsIgnored() throws ReflectiveOperationException {
        AutomowerHandler handler = createHandler();
        MowerMessages mowerMessages = new MowerMessages();
        MessageAttributes messageAttributes = new MessageAttributes();
        Message message = new Message();
        message.setCode(7);
        messageAttributes.getMessages().add(message);
        mowerMessages.setAttributes(messageAttributes);
        setField(handler, "mowerMessages", mowerMessages);

        handler.processWebSocketMessage(JsonParser.parseString("""
                {
                  "id": "mower-1",
                  "type": "messages-event-v2",
                  "attributes": {
                    "message": {"code": 149}
                  }
                }
                """).getAsJsonObject());

        assertEquals(7, message.getCode());
    }

    @SuppressWarnings("nullUncheckedConversion")
    private AutomowerHandler createHandler() throws ReflectiveOperationException {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID("automower:automower:mower-1"));
        TimeZoneProvider timeZoneProvider = () -> ZoneId.of("UTC");
        AutomowerHandler handler = new AutomowerHandler(thing, timeZoneProvider);
        handler.initialize();

        Mower mower = new Mower();
        mower.setId("mower-1");
        MowerData data = new MowerData();
        data.setMetadata(new Metadata());
        data.setBattery(new org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Battery());
        data.setSystem(new org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.System());
        data.setCapabilities(new Capabilities());
        data.addPosition(new Position());
        data.setMower(new MowerApp());
        data.setCalendar(new Calendar());
        data.setPlanner(new Planner());
        data.getPlanner().setOverride(new PlannerOverride());
        data.setSettings(new Settings());
        data.getSettings().setHeadlight(new Headlight());
        setField(handler, "mowerState", mower);
        mower.setAttributes(data);

        MowerMessages mowerMessages = new MowerMessages();
        MessageAttributes messageAttributes = new MessageAttributes();
        messageAttributes.getMessages().add(new Message());
        mowerMessages.setAttributes(messageAttributes);
        setField(handler, "mowerMessages", mowerMessages);
        return handler;
    }

    private void setField(Object target, String fieldName, @Nullable Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
