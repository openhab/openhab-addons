/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IdentifyNodeMessageClass;

/**
 * Test cases for IdentifyNode message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class IdentifyNodeMessageClassTest {
    ArgumentCaptor<Boolean> listening;
    ArgumentCaptor<Boolean> frequentlyListening;
    ArgumentCaptor<Boolean> routing;
    ArgumentCaptor<Integer> version;
    ArgumentCaptor<Boolean> security;
    ArgumentCaptor<Boolean> beaming;
    ArgumentCaptor<Integer> maxBaud;
    ArgumentCaptor<Basic> basicClass;
    ArgumentCaptor<Generic> genericClass;
    ArgumentCaptor<Specific> specificClass;

    ZWaveNode runIdentifyNodeTest(byte[] packetData) {
        byte[] outgoing = { 0x01, 0x04, 0x00, 0x41, 0x01, (byte) 0xBB };
        SerialMessage outgoingMsg = new SerialMessage(outgoing);
        SerialMessage incomingMsg = new SerialMessage(packetData);

        // Check the packet is not corrupted and is a command class request
        assertEquals(true, incomingMsg.isValid);
        assertEquals(SerialMessageType.Response, incomingMsg.getMessageType());
        assertEquals(SerialMessageClass.IdentifyNode, incomingMsg.getMessageClass());

        // Mock the controller so we return the node
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveDeviceClass deviceClass = Mockito.mock(ZWaveDeviceClass.class);

        Mockito.when(controller.getNode(1)).thenReturn(node);
        Mockito.when(node.getDeviceClass()).thenReturn(deviceClass);

        listening = ArgumentCaptor.forClass(Boolean.class);
        Mockito.doNothing().when(node).setListening(listening.capture());

        frequentlyListening = ArgumentCaptor.forClass(Boolean.class);
        Mockito.doNothing().when(node).setFrequentlyListening(frequentlyListening.capture());

        routing = ArgumentCaptor.forClass(Boolean.class);
        Mockito.doNothing().when(node).setRouting(routing.capture());

        version = ArgumentCaptor.forClass(Integer.class);
        Mockito.doNothing().when(node).setVersion(version.capture());

        security = ArgumentCaptor.forClass(Boolean.class);
        Mockito.doNothing().when(node).setSecurity(security.capture());

        beaming = ArgumentCaptor.forClass(Boolean.class);
        Mockito.doNothing().when(node).setBeaming(beaming.capture());

        maxBaud = ArgumentCaptor.forClass(Integer.class);
        Mockito.doNothing().when(node).setMaxBaud(maxBaud.capture());

        basicClass = ArgumentCaptor.forClass(Basic.class);
        Mockito.doNothing().when(deviceClass).setBasicDeviceClass(basicClass.capture());

        genericClass = ArgumentCaptor.forClass(Generic.class);
        Mockito.doNothing().when(deviceClass).setGenericDeviceClass(genericClass.capture());

        specificClass = ArgumentCaptor.forClass(Specific.class);
        Mockito.doNothing().when(deviceClass).setSpecificDeviceClass(specificClass.capture());

        IdentifyNodeMessageClass handler = new IdentifyNodeMessageClass();
        try {
            handler.handleResponse(controller, outgoingMsg, incomingMsg);
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return node;
    }

    @Test
    public void SpecificClass_Controller_AeonV2() {
        byte[] packetData = { 0x01, 0x09, 0x01, 0x41, (byte) 0x92, 0x16, 0x00, 0x02, 0x02, 0x01, 0x33 };
        runIdentifyNodeTest(packetData);

        assertEquals(listening.getValue(), true);
        assertEquals(frequentlyListening.getValue(), false);
        assertEquals(routing.getValue(), false);
        assertEquals(version.getValue(), new Integer(3));
        assertEquals(security.getValue(), false);
        assertEquals(beaming.getValue(), true);
        assertEquals(maxBaud.getValue(), new Integer(40000));
        assertEquals(basicClass.getValue(), Basic.STATIC_CONTROLLER);
        assertEquals(genericClass.getValue(), Generic.STATIC_CONTROLLER);
        assertEquals(specificClass.getValue(), Specific.PC_CONTROLLER);
    }

    @Test
    public void SpecificClass_SimpleRemote_POPP_KFOB() {
        byte[] packetData = { 0x01, 0x09, 0x01, 0x41, 0x13, (byte) 0x96, 0x01, 0x01, 0x01, 0x06, 0x34 };
        runIdentifyNodeTest(packetData);

        assertEquals(listening.getValue(), false);
        assertEquals(frequentlyListening.getValue(), false);
        assertEquals(routing.getValue(), false);
        assertEquals(version.getValue(), new Integer(4));
        assertEquals(security.getValue(), false);
        assertEquals(beaming.getValue(), true);
        assertEquals(maxBaud.getValue(), new Integer(40000));
        assertEquals(basicClass.getValue(), Basic.CONTROLLER);
        assertEquals(genericClass.getValue(), Generic.REMOTE_CONTROLLER);
        assertEquals(specificClass.getValue(), Specific.SIMPLE_REMOTE_CONTROLLER);
    }

}
