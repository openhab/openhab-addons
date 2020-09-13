/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.test.TestPortUtil;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.binding.onewire.internal.owserver.OwserverConnection;
import org.openhab.binding.onewire.internal.owserver.OwserverConnectionState;
import org.openhab.binding.onewire.test.OwserverTestServer;

/**
 * Tests cases for {@link OwserverConnection}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwserverConnectionTest extends JavaTest {
    private static final String TEST_HOST = "127.0.0.1";

    private @Nullable OwserverTestServer testServer;
    private @Nullable OwserverConnection owserverConnection;

    @Mock
    private @NonNullByDefault({}) OwserverBridgeHandler bridgeHandler;

    private int testPort;

    @Before
    public void setup() throws Exception {
        initMocks(this);

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

    @After
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
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();

        Mockito.verify(bridgeHandler).reportConnectionState(OwserverConnectionState.OPENED);
    }

    @Test
    public void failedConnectionReportedToBridgeHandler() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.setPort(1);

        owserverConnection.start();

        Mockito.verify(bridgeHandler, timeout(100)).reportConnectionState(OwserverConnectionState.FAILED);
    }

    @Test
    public void testGetDirectory() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();
        try {
            List<SensorId> directory = owserverConnection.getDirectory("/");

            assertEquals(3, directory.size());
            assertEquals(new SensorId("/00.0123456789ab"), directory.get(0));
            assertEquals(new SensorId("/00.0123456789ac"), directory.get(1));
            assertEquals(new SensorId("/00.0123456789ad"), directory.get(2));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testCheckPresence() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();
        State presence = owserverConnection.checkPresence("present");
        assertEquals(OnOffType.ON, presence);

        presence = owserverConnection.checkPresence("notpresent");
        assertEquals(OnOffType.OFF, presence);
    }

    @Test
    public void testReadDecimalType() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();
        try {
            DecimalType number = (DecimalType) owserverConnection.readDecimalType("testsensor/decimal");

            assertEquals(17.4, number.doubleValue(), 0.01);
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testReadDecimalTypeArray() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();
        try {
            List<State> numbers = owserverConnection.readDecimalTypeArray("testsensor/decimalarray");

            assertEquals(3834, ((DecimalType) numbers.get(0)).intValue());
            assertEquals(0, ((DecimalType) numbers.get(1)).intValue());
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testGetPages() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();
        try {
            OwPageBuffer pageBuffer = owserverConnection.readPages("testsensor");

            assertEquals(31, pageBuffer.getByte(5, 7));
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }

    @Test
    public void testWriteDecimalType() {
        final OwserverConnection owserverConnection = this.owserverConnection;
        if (owserverConnection == null) {
            Assert.fail("connection is null");
            return;
        }
        owserverConnection.start();
        try {
            owserverConnection.writeDecimalType("testsensor/decimal", new DecimalType(2009));

            Mockito.verify(bridgeHandler, never()).reportConnectionState(OwserverConnectionState.FAILED);
        } catch (OwException e) {
            Assert.fail("caught unexpected OwException");
        }
    }
}
