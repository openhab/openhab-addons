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
package org.openhab.binding.solarmax.internal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * The {@link SolarMaxHandlerTest} tests that a poll still running from a previous configuration doesn't change the
 * thing after it has been reinitialized.
 *
 * @author Christian Schreiner - Initial contribution
 */
@NonNullByDefault
public class SolarMaxHandlerTest {

    private static final String HOST = "127.0.0.1";

    // recorded from a SolarMax 10MT2 (device address 1)
    private static final String RESPONSE = "{01;FB;8F|64:PAC=290;SYS=4E28,0;SAL=0;UD01=1011;UD02=F99;ID01=1E;"
            + "ID02=1B;PDC=1EC;PD01=108;PD02=E4;TNF=138C;UL1=900;TKK=1F;KDY=5;KT0=14ED8|216B}";

    private static final long TEST_TIMEOUT_SECONDS = 30;

    // all commands don't fit into a single request (max. 16 per request), so a poll needs two requests
    private static final int REQUESTS_PER_POLL = 2;

    /**
     * a fake device holding back the response to the last request of the first poll until released, so that poll has
     * already got past everything an interrupt by cancelling the polling job could abort
     */
    private static class BlockingSolarMax implements AutoCloseable {
        private final ServerSocket serverSocket;
        private final CountDownLatch lastRequestReceived = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);

        BlockingSolarMax() throws IOException {
            serverSocket = new ServerSocket(0, 50, InetAddress.getByName(HOST));
            Thread thread = new Thread(() -> {
                int connections = 0;
                while (!serverSocket.isClosed()) {
                    try (Socket socket = serverSocket.accept()) {
                        readRequest(socket);
                        if (++connections == REQUESTS_PER_POLL) {
                            lastRequestReceived.countDown();
                            release.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        }
                        OutputStream out = socket.getOutputStream();
                        out.write(RESPONSE.getBytes(StandardCharsets.US_ASCII));
                        out.flush();
                    } catch (IOException e) {
                        // server socket closed or client went away
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }

        private static void readRequest(Socket socket) throws IOException {
            InputStream in = socket.getInputStream();
            int b = in.read();
            while (b != -1 && b != '}') {
                b = in.read();
            }
        }

        int port() {
            return serverSocket.getLocalPort();
        }

        @Override
        public void close() throws IOException {
            release.countDown();
            serverSocket.close();
        }
    }

    private static int unusedPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 50, InetAddress.getByName(HOST))) {
            return socket.getLocalPort();
        }
    }

    private static Thing thing(Map<String, Object> config) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("solarmax", "inverter", "test"));
        when(thing.getConfiguration()).thenReturn(new Configuration(config));
        return thing;
    }

    private static Map<String, Object> config(int port) {
        return Map.of("host", HOST, "portNumber", port, "refreshInterval", 3600, "timeout", 10000, "retries", 0,
                "offlineAfterFailures", 1);
    }

    @Test
    public void pollFromPreviousConfigurationIsDiscarded() throws Exception {
        try (BlockingSolarMax oldDevice = new BlockingSolarMax()) {
            Thing thing = thing(config(oldDevice.port()));
            ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
            SolarMaxHandler handler = new SolarMaxHandler(thing);
            handler.setCallback(callback);

            handler.initialize();
            assertTrue(oldDevice.lastRequestReceived.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS));

            // reconfigure to a device that isn't reachable while the old poll is still waiting for its response
            Thing updatedThing = thing(config(unusedPort()));
            handler.thingUpdated(updatedThing);
            oldDevice.release.countDown();

            verify(callback, timeout(TEST_TIMEOUT_SECONDS * 1000)).statusUpdated(eq(updatedThing),
                    argThat(info -> info.getStatus() == ThingStatus.OFFLINE));
            handler.dispose();

            // the successful old poll must neither have set the thing ONLINE nor reset the new failure counter
            verify(callback, never()).statusUpdated(any(),
                    argThat((ThingStatusInfo info) -> info.getStatus() == ThingStatus.ONLINE));
        }
    }
}
