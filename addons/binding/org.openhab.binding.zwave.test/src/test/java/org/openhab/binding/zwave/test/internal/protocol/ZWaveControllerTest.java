/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

public class ZWaveControllerTest {

    void callHandleIncomingMessage(ZWaveController controller, byte[] msg) throws Throwable {
        SerialMessage packet = new SerialMessage(msg);

        try {
            Method privateMethod;
            privateMethod = ZWaveController.class.getDeclaredMethod("handleIncomingMessage", SerialMessage.class);
            privateMethod.setAccessible(true);
            privateMethod.invoke(controller, packet);

        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            throw (e.getTargetException());
        }
    }

    @Test
    public void AddNotificationListener() {
        ZWaveController controller = new ZWaveController(null);

        ZWaveEventListener eventListener = Mockito.mock(ZWaveEventListener.class);
        controller.addEventListener(eventListener);
        ZWaveCommandClassValueEvent event = new ZWaveCommandClassValueEvent(0, 0, null, null);

        // Notify an event and make sure we see it
        controller.notifyEventListeners(event);
        Mockito.verify(eventListener, Mockito.times(1)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());

        // Register the handler again - this should be filtered
        controller.addEventListener(eventListener);

        // Notify the event again - we should only see one notification
        controller.notifyEventListeners(event);
        Mockito.verify(eventListener, Mockito.times(2)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());
    }

    @Test
    public void incomingPacket() {
        byte[] msgOk = { 0x01, 0x05, 0x00, 0x4a, 0x00, 0x01, (byte) 0xb1 };
        byte[] msgNok = { 0x01, 0x05, 0x00, (byte) 0xcc, 0x00, 0x01, 0x37 };

        ZWaveController controller = new ZWaveController(null);

        ZWaveEventListener eventListener = Mockito.mock(ZWaveEventListener.class);
        controller.addEventListener(eventListener);

        // Send the AddNode message
        // It's just a simple message that will result in an event that we can detect
        SerialMessage packet = new SerialMessage(msgOk);
        controller.incomingPacket(packet);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        // Should result in 1 event
        Mockito.verify(eventListener, Mockito.times(1)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());

        Mockito.reset(eventListener);

        // Send a broken message - it's valid, but not a message we know
        packet = new SerialMessage(msgNok);
        controller.incomingPacket(packet);
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        // Should not result in additional events
        Mockito.verify(eventListener, Mockito.times(0)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());
    }

    @Test
    public void handleIncomingMessage() {
        byte[] msgOk = { 0x01, 0x05, 0x00, 0x4a, 0x00, 0x01, (byte) 0xb1 };
        byte[] msgNok = { 0x01, 0x05, 0x00, (byte) 0xcc, 0x00, 0x01, 0x37 };

        ZWaveController controller = new ZWaveController(null);

        ZWaveEventListener eventListener = Mockito.mock(ZWaveEventListener.class);
        controller.addEventListener(eventListener);

        // Should result in 1 event
        Mockito.reset(eventListener);
        try {
            callHandleIncomingMessage(controller, msgOk);
        } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Mockito.verify(eventListener, Mockito.times(1)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());

        Mockito.reset(eventListener);
        try {
            callHandleIncomingMessage(controller, msgNok);
            fail("Should have thrown an exception");
        } catch (Throwable e) {
            e.printStackTrace();
            assertNotNull(e);
        }
        Mockito.verify(eventListener, Mockito.times(0)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());
    }
}
