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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SmartThingsLocalCallbackListener}.
 */
@NonNullByDefault
class SmartThingsLocalCallbackListenerTest {

    @Test
    void callbackListenerBindsToAllInterfacesForRemoteBrowserSetup() throws Exception {
        SmartThingsLocalCallbackListener listener = new SmartThingsLocalCallbackListener();

        try {
            listener.startCallbackListener();

            ServerSocket serverSocket = waitForServerSocket(listener);

            assertTrue(serverSocket.getInetAddress().isAnyLocalAddress());
        } finally {
            listener.stopCallbackListener();
        }
    }

    @Test
    void stoppingCallbackListenerFromCallbackThreadDoesNotInterruptCurrentThread() throws Exception {
        SmartThingsLocalCallbackListener listener = new SmartThingsLocalCallbackListener();
        setCallbackThread(listener, Thread.currentThread());

        try {
            listener.stopCallbackListener();

            assertFalse(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private void setCallbackThread(SmartThingsLocalCallbackListener listener, Thread thread) throws Exception {
        Field threadField = SmartThingsLocalCallbackListener.class.getDeclaredField("callbackThread");
        threadField.setAccessible(true);
        threadField.set(listener, thread);
    }

    private ServerSocket waitForServerSocket(SmartThingsLocalCallbackListener listener) throws Exception {
        Field socketField = SmartThingsLocalCallbackListener.class.getDeclaredField("callbackServerSocket");
        socketField.setAccessible(true);

        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while (System.nanoTime() < deadline) {
            @Nullable
            ServerSocket serverSocket = (ServerSocket) socketField.get(listener);
            if (serverSocket != null) {
                return serverSocket;
            }
            Thread.sleep(20);
        }

        throw new AssertionError("Callback listener did not start");
    }
}
