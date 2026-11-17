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
package org.openhab.binding.meteostick.internal.handler;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.meteostick.internal.MeteostickBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Reproduces the reconnect gap: after a successful connect, a serial-port break (the pty the binding holds disappears)
 * leaves the binding permanently dead because it never re-opens the port.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
public class MeteostickBridgeHandlerReconnectTest {

    private static final ThingUID BRIDGE_UID = new ThingUID(MeteostickBindingConstants.THING_TYPE_BRIDGE, "test");
    private static final String PORT = "/dev/ttyMeteostick";

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final SerialPortManager serialPortManager = mock(SerialPortManager.class);
    private final SerialPortIdentifier identifier = mock(SerialPortIdentifier.class);
    private final SerialPort serialPort = mock(SerialPort.class);

    private final AtomicInteger openCount = new AtomicInteger();

    private final Bridge bridge = mock(Bridge.class);
    private @Nullable PipedOutputStream pipeOut;
    private @Nullable PipedInputStream pipeIn;

    private @Nullable MeteostickBridgeHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        when(bridge.getUID()).thenReturn(BRIDGE_UID);
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of("port", PORT, "mode", BigDecimal.ONE)));

        pipeOut = new PipedOutputStream();
        pipeIn = new PipedInputStream(requireNonNull(pipeOut), 8192);

        when(serialPortManager.getIdentifier(PORT)).thenReturn(identifier);
        when(identifier.open(anyString(), anyInt())).thenAnswer(invocation -> {
            openCount.incrementAndGet();
            return serialPort;
        });
        when(serialPort.getInputStream()).thenReturn(pipeIn);

        handler = new MeteostickBridgeHandler(bridge, serialPortManager);
        handler.setCallback(callback);
        handler.reconnectIntervalSeconds = 1; // shorten the reconnect poll so the test does not wait 60 s

        handler.initialize();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (handler != null) {
            handler.dispose();
        }
        requireNonNull(pipeOut).close();
        requireNonNull(pipeIn).close();
    }

    @Test
    public void testReconnectsAfterSerialPortBreak() throws Exception {
        // The port is opened once during initialization and the bridge comes ONLINE when a packet arrives.
        awaitTrue("initial connect", 2000, () -> openCount.get() >= 1);
        writeInbound("B 19.3 982.59\r");
        verify(callback, timeout(2000)).statusUpdated(eq(bridge),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));

        // The upstream side disappears (socat restarts and the pty the binding holds is gone): reads now return EOF.
        pipeOut.close();

        // The binding must re-open the port; today it spins on EOF and never recovers.
        awaitTrue("reconnect after serial-port break", 5000, () -> openCount.get() >= 2);
    }

    private void writeInbound(String line) throws IOException {
        requireNonNull(pipeOut).write(line.getBytes(US_ASCII));
        requireNonNull(pipeOut).flush();
    }

    private static void awaitTrue(String what, long timeoutMs, BooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + MILLISECONDS.toNanos(timeoutMs);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(20);
        }
        fail("Timed out waiting for " + what);
    }
}
