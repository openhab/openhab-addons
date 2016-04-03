/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.security;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveIoHandler;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;

public class SecureInclusionTest {
    private final String networkKey = "";

    private ZWaveIoHandler ioHandler;
    private ZWaveController controller;

    private Map<Byte, SerialMessage> startMessages = new HashMap<Byte, SerialMessage>();

    void initStartup() {
        startMessages.put((byte) 0x15, new SerialMessage(new byte[] { 0x01, 0x10, 0x01, 0x15, 0x5A, 0x2D, 0x57, 0x61,
                0x76, 0x65, 0x20, 0x32, 0x2E, 0x37, 0x38, 0x00, 0x01, (byte) 0x9B }));
    }

    private ZWaveController getController() {

        ioHandler = new ZWaveDataProcessor();
        ZWaveController controller = new ZWaveController(ioHandler);

        return controller;
    }

    @Test
    public void SecureInclusion() {
        initStartup();
        ZWaveSecurityCommandClass.setRealNetworkKey(networkKey);
        controller = getController();

        // ZWaveInclusionEvent inclusionEvent = new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.IncludeSlaveFound, 14);
        // controller.notifyEventListeners(inclusionEvent);

        // Create the node we are including
        // ZWaveNode node = new ZWaveNode(0, 14, controller);
        // node.initialiseNode();

        int running = 500;
        while (running-- > 0) {
            try {
                Thread.sleep(100); // 1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        // hexStringToByteArray();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    class ZWaveDataProcessor implements ZWaveIoHandler {

        @Override
        public void deviceDiscovered(int node) {

        }

        @Override
        public void sendPacket(SerialMessage message) {
            byte[] x = message.getMessageBuffer();

            SerialMessage response = startMessages.get(message.getMessageBuffer()[3]);

            controller.incomingPacket(response);
        }

    }

}
