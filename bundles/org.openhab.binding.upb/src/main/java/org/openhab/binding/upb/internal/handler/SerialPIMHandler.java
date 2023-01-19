/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.upb.internal.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upb.internal.Constants;
import org.openhab.binding.upb.internal.message.MessageBuilder;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler responsible for serial PIM communications.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public class SerialPIMHandler extends PIMHandler {
    private static final int SERIAL_RECEIVE_TIMEOUT_MS = 100;
    private static final int BAUD_RATE = 4800;
    private static final int SERIAL_PORT_OPEN_INIT_DELAY_MS = 500;
    private static final int SERIAL_PORT_OPEN_RETRY_DELAY_MS = 30_000;

    private final Logger logger = LoggerFactory.getLogger(SerialPIMHandler.class);

    private SerialPortManager serialPortManager;
    private volatile @Nullable SerialIoThread receiveThread;
    private volatile @Nullable ScheduledFuture<?> futSerialPortInit;

    public SerialPIMHandler(final Bridge thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Serial UPB PIM {}.", getThing().getUID());
        super.initialize();

        final String portId = (String) getConfig().get(Constants.CONFIGURATION_PORT);
        if (portId == null || portId.isEmpty()) {
            logger.debug("serial port is not set");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    Constants.OFFLINE_SERIAL_PORT_NOT_SET);
            return;
        }

        futSerialPortInit = scheduler.schedule(() -> openSerialPort(portId), SERIAL_PORT_OPEN_INIT_DELAY_MS,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> futSerialPortInit = this.futSerialPortInit;
        if (futSerialPortInit != null) {
            futSerialPortInit.cancel(true);
            this.futSerialPortInit = null;
        }
        final SerialIoThread receiveThread = this.receiveThread;
        if (receiveThread != null) {
            receiveThread.terminate();
            try {
                receiveThread.join(1000);
            } catch (final InterruptedException e) {
                // ignore
            }
            this.receiveThread = null;
        }
        logger.debug("Stopped UPB serial handler");
        super.dispose();
    }

    private void openSerialPort(final String portId) {
        try {
            final SerialPort serialPort = tryOpenSerialPort(portId);
            if (serialPort == null) {
                futSerialPortInit = scheduler.schedule(() -> openSerialPort(portId), SERIAL_PORT_OPEN_RETRY_DELAY_MS,
                        TimeUnit.MILLISECONDS);
                return;
            }
            logger.debug("Starting receive thread");
            final SerialIoThread receiveThread = new SerialIoThread(serialPort, this, getThing().getUID());
            this.receiveThread = receiveThread;
            // Once the receiver starts, it may set the PIM status to ONLINE
            // so we must ensure all initialization is finished at that point.
            receiveThread.start();
            updateStatus(ThingStatus.ONLINE);
        } catch (final RuntimeException e) {
            logger.warn("failed to open serial port", e);
        }
    }

    private @Nullable SerialPort tryOpenSerialPort(final String portId) {
        logger.debug("opening serial port {}", portId);
        final SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(portId);
        if (portIdentifier == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    Constants.OFFLINE_SERIAL_EXISTS);
            return null;
        }

        final SerialPort serialPort;
        try {
            serialPort = portIdentifier.open("org.openhab.binding.upb", 1000);
        } catch (final PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    Constants.OFFLINE_SERIAL_INUSE);
            return null;
        }
        try {
            serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            try {
                serialPort.enableReceiveThreshold(1);
                serialPort.enableReceiveTimeout(SERIAL_RECEIVE_TIMEOUT_MS);
            } catch (final UnsupportedCommOperationException e) {
                // ignore - not supported for RFC2217 ports
            }
        } catch (final UnsupportedCommOperationException e) {
            logger.debug("cannot open serial port", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    Constants.OFFLINE_SERIAL_UNSUPPORTED);
            return null;
        }
        logger.debug("Serial port is initialized");
        return serialPort;
    }

    @Override
    public CompletionStage<CmdStatus> sendPacket(final MessageBuilder msg) {
        final SerialIoThread receiveThread = this.receiveThread;
        if (receiveThread != null) {
            return receiveThread.enqueue(msg.build());
        } else {
            return exceptionallyCompletedFuture(new IllegalStateException("I/O thread not active"));
        }
    }

    /**
     * Returns a new {@code CompletableFuture} that is already exceptionally completed with
     * the given exception.
     *
     * @param throwable the exception
     * @param <T> an arbitrary type for the returned future; can be anything since the future
     *            will be exceptionally completed and thus there will never be a value of type
     *            {@code T}
     * @return a future that exceptionally completed with the supplied exception
     * @throws NullPointerException if the supplied throwable is {@code null}
     * @since 0.1.0
     */
    public static <T> CompletableFuture<T> exceptionallyCompletedFuture(final Throwable throwable) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(throwable);
        return future;
    }
}
