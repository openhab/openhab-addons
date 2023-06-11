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
package org.openhab.binding.upb.internal;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.upb.internal.handler.MessageListener;
import org.openhab.binding.upb.internal.handler.SerialIoThread;
import org.openhab.binding.upb.internal.handler.UPBIoHandler.CmdStatus;
import org.openhab.binding.upb.internal.message.Command;
import org.openhab.binding.upb.internal.message.MessageBuilder;
import org.openhab.binding.upb.internal.message.UPBMessage;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.thing.ThingUID;

/**
 * @author Marcus Better - Initial contribution
 */
public class SerialIoThreadTest {

    private static final String ENABLE_MESSAGE_MODE_CMD = "\u001770028E\n";

    private final ThingUID thingUID = new ThingUID("a", "b", "c");
    private final Listener msgListener = new Listener();
    private final PipedOutputStream in = new PipedOutputStream();
    private final OutputStreamWriter inbound = new OutputStreamWriter(in, US_ASCII);
    private final PipedOutputStream out = new PipedOutputStream();

    private @Mock SerialPort serialPort;
    private SerialIoThread thread;
    private InputStreamReader outbound;
    final char[] buf = new char[256];

    @BeforeEach
    public void setup() throws IOException {
        serialPort = mock(SerialPort.class);
        outbound = new InputStreamReader(new PipedInputStream(out), US_ASCII);
        when(serialPort.getInputStream()).thenReturn(new PipedInputStream(in));
        when(serialPort.getOutputStream()).thenReturn(out);
        thread = new SerialIoThread(serialPort, msgListener, thingUID);
        thread.start();
    }

    @AfterEach
    public void cleanup() {
        thread.terminate();
    }

    @Test
    public void testName() {
        assertEquals("OH-binding-a:b:c-serial-reader", thread.getName());
        assertTrue(thread.isDaemon());
    }

    @Test
    public void receive() throws Exception {
        writeInbound("PU8905FA011220FFFF47\r");
        final UPBMessage msg = msgListener.readInbound();
        assertEquals(Command.ACTIVATE, msg.getCommand());
        assertEquals(1, msg.getDestination());
        writeInbound("PU8905FA011221FFFF48\r");
        final UPBMessage msg2 = msgListener.readInbound();
        assertEquals(Command.DEACTIVATE, msg2.getCommand());
        verifyMessageModeCmd();
    }

    @Test
    public void send() throws Exception {
        final String msg = MessageBuilder.forCommand(Command.GOTO).args((byte) 10).network((byte) 2)
                .destination((byte) 5).build();
        final CompletionStage<CmdStatus> fut = thread.enqueue(msg);
        verifyMessageModeCmd();
        final int n = outbound.read(buf);
        assertEquals("\u001408100205FF220AB6\r", new String(buf, 0, n));
        ack();
        final CmdStatus res = fut.toCompletableFuture().join();
        assertEquals(CmdStatus.ACK, res);
    }

    @Test
    public void resend() throws Exception {
        final String msg = MessageBuilder.forCommand(Command.GOTO).args((byte) 10).network((byte) 2)
                .destination((byte) 5).build();
        final CompletableFuture<CmdStatus> fut = thread.enqueue(msg).toCompletableFuture();
        verifyMessageModeCmd();
        int n = outbound.read(buf);
        assertEquals("\u001408100205FF220AB6\r", new String(buf, 0, n));
        nak();

        // should re-send
        n = outbound.read(buf);
        assertEquals("\u001408100205FF220AB6\r", new String(buf, 0, n));
        assertFalse(fut.isDone());
        ack();
        final CmdStatus res = fut.join();
        assertEquals(CmdStatus.ACK, res);
    }

    @Test
    public void resendMaxAttempts() throws Exception {
        final String msg = MessageBuilder.forCommand(Command.GOTO).args((byte) 10).network((byte) 2)
                .destination((byte) 5).build();
        final CompletableFuture<CmdStatus> fut = thread.enqueue(msg).toCompletableFuture();
        verifyMessageModeCmd();
        int n = outbound.read(buf);
        assertEquals("\u001408100205FF220AB6\r", new String(buf, 0, n));
        nak();

        // retry
        n = outbound.read(buf);
        assertEquals("\u001408100205FF220AB6\r", new String(buf, 0, n));
        assertFalse(fut.isDone());
        // no response - wait for ack timeout

        // last retry
        n = outbound.read(buf);
        assertEquals("\u001408100205FF220AB6\r", new String(buf, 0, n));
        assertFalse(fut.isDone());
        nak();
        final CmdStatus res = fut.join();
        assertEquals(CmdStatus.NAK, res);
    }

    private void ack() throws IOException {
        writeInbound("PK\r");
    }

    private void nak() throws IOException {
        writeInbound("PN\r");
    }

    private void writeInbound(String s) throws IOException {
        inbound.write(s);
        inbound.flush();
    }

    private void verifyMessageModeCmd() throws IOException {
        final int n = outbound.read(buf, 0, ENABLE_MESSAGE_MODE_CMD.length());
        assertEquals(ENABLE_MESSAGE_MODE_CMD, new String(buf, 0, n));
    }

    private static class Listener implements MessageListener {

        private final BlockingQueue<UPBMessage> messages = new LinkedBlockingQueue<>();

        @Override
        public void incomingMessage(final UPBMessage msg) {
            messages.offer(msg);
        }

        @Override
        public void onError(final Throwable t) {
        }

        public UPBMessage readInbound() {
            try {
                return messages.take();
            } catch (InterruptedException e) {
                return null;
            }
        }
    }
}
