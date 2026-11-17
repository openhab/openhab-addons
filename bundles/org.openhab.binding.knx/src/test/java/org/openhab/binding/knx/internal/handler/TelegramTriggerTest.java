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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.channel.KNXChannel;
import org.openhab.binding.knx.internal.channel.KNXChannelFactory;
import org.openhab.binding.knx.internal.client.AbstractKNXClient;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.monitor.DataType;
import org.openhab.binding.knx.internal.profiles.KNXProfileAdvisor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.RefreshType;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks the trigger channel at the point where its event leaves the handler.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TelegramTriggerTest {
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 20);
    private static final GroupAddress FIRST = new GroupAddress(3, 1, 10);
    private static final GroupAddress SECOND = new GroupAddress(3, 1, 11);
    private static final GroupAddress OTHER = new GroupAddress(3, 1, 12);
    private static final byte[] TEMPERATURE = { 0x0c, 0x33 };
    private static final ThingUID THING = new ThingUID("knx", "device", "thing");
    private static final ThingUID BRIDGE = new ThingUID("knx", "ip", "bridge");
    private static final ChannelUID TRIGGER = new ChannelUID(THING, "telegrams");
    private static final ChannelUID LIGHT = new ChannelUID(THING, "light");

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final KNXClient client = mock(KNXClient.class);
    private final AbstractKNXClient busClient = mock(AbstractKNXClient.class);
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final List<Runnable> submitted = new java.util.ArrayList<>();

    private static Channel trigger(String ga) {
        return ChannelBuilder.create(TRIGGER).withType(new ChannelTypeUID("knx", CHANNEL_TELEGRAM_TRIGGER))
                .withKind(ChannelKind.TRIGGER).withConfiguration(new Configuration(Map.of("ga", ga))).build();
    }

    private static Channel light(String ga) {
        return ChannelBuilder.create(LIGHT, "Switch").withType(new ChannelTypeUID("knx", CHANNEL_SWITCH))
                .withConfiguration(new Configuration(Map.of("ga", ga))).build();
    }

    private DeviceThingHandler start(Channel... channels) {
        Thing thing = ThingBuilder.create(THING_TYPE_DEVICE, THING).withBridge(BRIDGE).withChannels(List.of(channels))
                .build();
        Bridge bridge = mock(Bridge.class);
        IPBridgeThingHandler bridgeHandler = mock(IPBridgeThingHandler.class);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(bridgeHandler.getClient()).thenReturn(client);
        when(bridgeHandler.getScheduler()).thenReturn(scheduler);
        when(client.isConnected()).thenReturn(true);
        when(callback.getBridge(BRIDGE)).thenReturn(bridge);
        when(callback.isChannelLinked(any())).thenReturn(true);
        when(scheduler.submit(any(Runnable.class))).thenAnswer(call -> {
            submitted.add(call.getArgument(0, Runnable.class));
            return null;
        });
        DeviceThingHandler result = new DeviceThingHandler(thing);
        result.setCallback(callback);
        result.initialize();
        return result;
    }

    private void runScheduledReads() {
        new java.util.ArrayList<>(submitted).forEach(Runnable::run);
    }

    @Test
    void aWriteFiresWithTheValueAsText() {
        DeviceThingHandler h = start(trigger("9.001:3/1/10"));

        h.onGroupWrite(busClient, SENSOR, FIRST, TEMPERATURE);

        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("21.5 °C"));
    }

    @Test
    void aSwitchValueIsOnOrOff() {
        DeviceThingHandler h = start(trigger("1.001:3/1/10"));

        h.onGroupWrite(busClient, SENSOR, FIRST, new byte[] { 1 });
        h.onGroupWrite(busClient, SENSOR, FIRST, new byte[] { 0 });

        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("ON"));
        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("OFF"));
    }

    @Test
    void aReadResponseDoesNotFire() {
        DeviceThingHandler h = start(trigger("9.001:3/1/10"));

        h.onGroupReadResponse(busClient, SENSOR, FIRST, TEMPERATURE);

        verify(callback, never()).channelTriggered(any(), any(), any());
    }

    @Test
    void aReadRequestDoesNotFire() {
        DeviceThingHandler h = start(trigger("9.001:3/1/10"));

        h.onGroupRead(busClient, SENSOR, FIRST, new byte[0]);

        verify(callback, never()).channelTriggered(any(), any(), any());
    }

    @Test
    void everyAddressOfTheChannelFiresAndNoOtherDoes() {
        DeviceThingHandler h = start(trigger("9.001:3/1/10+3/1/11"));

        h.onGroupWrite(busClient, SENSOR, FIRST, TEMPERATURE);
        h.onGroupWrite(busClient, SENSOR, SECOND, TEMPERATURE);
        h.onGroupWrite(busClient, SENSOR, OTHER, TEMPERATURE);

        verify(callback, times(2)).channelTriggered(any(), eq(TRIGGER), eq("21.5 °C"));
        assertTrue(h.listensTo(FIRST));
        assertTrue(h.listensTo(SECOND));
        assertFalse(h.listensTo(OTHER));
    }

    @Test
    void withoutADataTypeTheEventCarriesTheRawBytes() {
        DeviceThingHandler h = start(trigger("3/1/10"));

        h.onGroupWrite(busClient, SENSOR, FIRST, TEMPERATURE);
        h.onGroupWrite(busClient, SENSOR, FIRST, new byte[] { 1 });

        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("0C33"));
        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("01"));
    }

    @Test
    void aValueThatDoesNotFitTheTypeCarriesTheRawBytes() {
        DeviceThingHandler h = start(trigger("9.001:3/1/10"));

        h.onGroupWrite(busClient, SENSOR, FIRST, new byte[] { 1 });

        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("01"));
    }

    @Test
    void aDataTypeTheBindingDoesNotKnowCarriesTheRawBytes() {
        DeviceThingHandler h = start(trigger("999.999:3/1/10"));

        h.onGroupWrite(busClient, SENSOR, FIRST, TEMPERATURE);

        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("0C33"));
    }

    @Test
    void theReadFlagReadsNothingAtStartUpOnLinkOrOnRefresh() {
        DeviceThingHandler h = start(trigger("<3/1/10+<3/1/11"));

        h.channelLinked(TRIGGER);
        h.handleCommand(TRIGGER, RefreshType.REFRESH);
        runScheduledReads();

        verifyNoInteractions(scheduler);
        verify(client, never()).readDatapoint(any());
        assertTrue(submitted.isEmpty());
    }

    @Test
    void aNormalChannelWithTheReadFlagStillReadsInTheSameThing() {
        DeviceThingHandler h = start(trigger("<3/1/10"), light("<3/1/12"));

        h.channelLinked(TRIGGER);
        h.channelLinked(LIGHT);
        h.handleCommand(LIGHT, RefreshType.REFRESH);
        runScheduledReads();

        verify(client, atLeastOnce()).readDatapoint(argThat(datapoint -> OTHER.equals(datapoint.getMainAddress())));
        verify(client, never()).readDatapoint(argThat(datapoint -> FIRST.equals(datapoint.getMainAddress())));
    }

    @Test
    void aCommandOnTheChannelDoesNothing() throws Exception {
        DeviceThingHandler h = start(trigger("1.001:3/1/10"));

        h.handleCommand(TRIGGER, OnOffType.ON);

        verify(client, never()).writeToKNX(any());
        verify(client, never()).respondToKNX(any());
        verify(callback, never()).channelTriggered(any(), any(), any());
    }

    @Test
    void whatOpenHabSendsToTheAddressDoesNotFire() throws Exception {
        DeviceThingHandler h = start(trigger("1.001:3/1/10"), light("3/1/10"));

        h.handleCommand(LIGHT, OnOffType.ON);

        verify(client).writeToKNX(any());
        verify(callback, never()).channelTriggered(any(), any(), any());
    }

    @Test
    void aTriggerChannelNeverAnswersAReadRequest() throws Exception {
        DeviceThingHandler h = start(trigger("1.001:3/1/10"));

        h.onGroupRead(busClient, SENSOR, FIRST, new byte[0]);

        verify(client, never()).respondToKNX(any());
        verify(callback, never()).postCommand(any(), any());
    }

    @Test
    void channelsOfOtherTypesBehaveAsBefore() {
        DeviceThingHandler h = start(light("3/1/10"), trigger("3/1/10"));

        h.onGroupWrite(busClient, SENSOR, FIRST, new byte[] { 1 });
        h.onGroupReadResponse(busClient, SENSOR, FIRST, new byte[] { 0 });

        verify(callback).stateUpdated(LIGHT, OnOffType.ON);
        verify(callback).stateUpdated(LIGHT, OnOffType.OFF);
        verify(callback).channelTriggered(any(), eq(TRIGGER), eq("01"));
        verify(callback, never()).stateUpdated(eq(TRIGGER), any());
    }

    @Test
    void theMonitorSkipsATriggerChannelWithoutADataTypeAndUsesOneWithIt() {
        KNXChannel without = KNXChannelFactory.createKnxChannel(trigger("3/1/10"));
        KNXChannel with = KNXChannelFactory.createKnxChannel(trigger("9.001:3/1/10"));

        assertNull(DeviceThingHandler.monitorDataType(List.of(without), FIRST));
        DataType dataType = DeviceThingHandler.monitorDataType(List.of(with), FIRST);
        assertNotNull(dataType);
        assertEquals("9.001", dataType.dpt());
    }

    @Test
    void theProfileAdvisorLeavesTriggerChannelsToTheFramework() {
        KNXProfileAdvisor advisor = new KNXProfileAdvisor();

        assertNull(advisor.getSuggestedProfileTypeUID(trigger("3/1/10"), null));
        assertNotNull(advisor.getSuggestedProfileTypeUID(light("3/1/10"), "Switch"));
    }
}
