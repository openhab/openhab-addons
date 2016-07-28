/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveMultiLevelSwitchConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveMultiLevelSwitchConverterTest {

    private ZWaveControllerHandler controller;
    private ZWaveThingChannel channel;
    private ZWaveCommandClassValueEvent event;
    private ZWaveNode node;
    private PercentType percentType;
    private ZWaveMultiLevelSwitchCommandClass commandClass;

    @Before
    public void setup() {
        controller = mock(ZWaveControllerHandler.class);
        channel = mock(ZWaveThingChannel.class);
        event = mock(ZWaveCommandClassValueEvent.class);
        node = mock(ZWaveNode.class);
        percentType = mock(PercentType.class);
        commandClass = mock(ZWaveMultiLevelSwitchCommandClass.class);
    }

    @Test
    public void handleEvent_PercentType0invertPercentFalse_returnPercentType0() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(0), state);
    }

    @Test
    public void handleEvent_PercentType99invertPercentFalse_returnPercentType100() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(99);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(100), state);
    }

    @Test
    public void handleEvent_PercentType0invertPercentTrue_returnPercentType100() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(100), state);
    }

    @Test
    public void handleEvent_PercentType1invertPercentTrue_returnPercentType100() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(1);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(100), state);
    }

    @Test
    public void handleEvent_OnOffType0invertFalse_returnOnOffTypeOff() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void handleEvent_OnOffType1invertFalse_returnOnOffTypeOn() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(1);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void handleEvent_OnOffType0invertTrue_returnOnOffTypeOn() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void handleEvent_OnOffType1invertTrue_returnOnOffTypeOff() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(1);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void receiveCommand_PercentType0invertPercentFalse_setValue0() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(0);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(0);
    }

    @Test
    public void receiveCommand_PercentType0invertPercentTrue_setValue99() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(0);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(99);
    }

    @Test
    public void receiveCommand_PercentType80invertPercentFalse_setValue80() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(80);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(80);
    }

    @Test
    public void receiveCommand_PercentType80invertPercentTrue_setValue20() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(80);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(20);
    }

    @Test
    public void receiveCommand_PercentType100invertPercentFalse_setValue99() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(100);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(99);
    }

    private void setupReceiveCommand() {
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        when(channel.getEndpoint()).thenReturn(1);
        when(node.resolveCommandClass(CommandClass.SWITCH_MULTILEVEL, channel.getEndpoint())).thenReturn(commandClass);
        when(node.encapsulate(any(SerialMessage.class), any(ZWaveMultiLevelSwitchCommandClass.class), anyInt()))
                .thenReturn(new SerialMessage());
    }

}
