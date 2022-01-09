/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.onewire.owserver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverConnection;
import org.openhab.binding.onewire.internal.owserver.OwserverConnectionState;
import org.openhab.binding.onewire.test.OwserverTestServer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.test.TestPortUtil;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.types.State;

/**
 * Tests cases for {@link OwserverConnection}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class OwserverConnectionTest extends JavaTest {
    private static final String TEST_HOST = "127.0.0.1";

    private @Nullable OwserverTestServer testServer;
    private @Nullable OwserverConnection owserverConnection;

    private @Mock @NonNullByDefault({}) OwserverBridgeHandler bridgeHandler;

    private int testPort;

    @BeforeEach
    public void setup() throws Exception {
        CompletableFuture<Boolean> serverStarted = new CompletableFuture<>();
        testPort = TestPortUtil.findFreePort();
        try {
            final OwserverTestServer testServer = new OwserverTestServer(testPort);
            testServer.startServer(serverStarted);
            this.testServer = testServer;
        } catch (IOException e) {
            fail("could not start test server");
        }

        final OwserverConnection owserverConnection = new OwserverConnection(bridgeHandler);
        owserverConnection.setHost(TEST_HOST);
        owserverConnection.setPort(testPort);
        this.owserverConnection = owserverConnection;

        serverStarted.get(); // wait for the server thread to start
    }

    @AfterEach
    public void tearDown() {
        try {
            final OwserverTestServer testServer = this.testServer;
            if (testServer != null) {
                testServer.stopServer();
            }
        } catch (IOException e) {
            fail("could not stop test server");
        }
    }

    @Test
    public void successfullConnectionReportedToBridgeHandler() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();

        Mockito.verify(bridgeHandler).reportConnectionState(OwserverConnectionState.OPENED);
    }

    @Test
    public void failedConnectionReportedToBridgeHandler() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.setPort(1);

        owserverConnection.start();

        Mockito.verify(bridgeHandler, timeout(100)).reportConnectionState(OwserverConnectionState.FAILED);
    }

    @Test
    public void testGetDirectory() throws OwException {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();

        List<SensorId> directory = owserverConnection.getDirectory("/");

        assertEquals(3, directory.size());
        assertEquals(new SensorId("/00.0123456789ab"), directory.get(0));
        assertEquals(new SensorId("/00.0123456789ac"), directory.get(1));
        assertEquals(new SensorId("/00.0123456789ad"), directory.get(2));
    }

    @Test
    public void testCheckPresence() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();
        State presence = owserverConnection.checkPresence("present");
        assertEquals(OnOffType.ON, presence);

        presence = owserverConnection.checkPresence("notpresent");
        assertEquals(OnOffType.OFF, presence);
    }

    @Test
    public void testReadDecimalType() throws OwException {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();

        DecimalType number = (DecimalType) owserverConnection.readDecimalType("testsensor/decimal");

        assertEquals(17.4, number.doubleValue(), 0.01);
    }

    @Test
    public void testReadDecimalTypeArray() throws OwException {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();

        List<State> numbers = owserverConnection.readDecimalTypeArray("testsensor/decimalarray");

        assertEquals(3834, ((DecimalType) numbers.get(0)).intValue());
        assertEquals(0, ((DecimalType) numbers.get(1)).intValue());
    }

    @Test
    public void testGetPages() throws OwException {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();

        OwPageBuffer pageBuffer = owserverConnection.readPages("testsensor");
        assertEquals(31, pageBuffer.getByte(5, 7));
    }

    @Test
    public void testWriteDecimalType() throws OwException {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            fail("connection is null");
            return;
        }
        owserverConnection.start();

        owserverConnection.writeDecimalType("testsensor/decimal", new DecimalType(2009));

        Mockito.verify(bridgeHandler, never()).reportConnectionState(OwserverConnectionState.FAILED);
    }
}
