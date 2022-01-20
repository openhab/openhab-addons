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
package org.openhab.binding.teleinfo.internal.serial;

import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.InvalidFrameException;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoSerialControllerHandler} class defines a handler for serial controller.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoSerialControllerHandler extends TeleinfoAbstractControllerHandler
        implements TeleinfoReceiveThreadListener {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoSerialControllerHandler.class);

    private static final int SERIAL_RECEIVE_TIMEOUT_MS = 250;
    private static final int RECEIVER_THREAD_JOIN_DELAY_MS = 500;

    private SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;
    private @Nullable TeleinfoReceiveThread receiveThread;
    private @Nullable ScheduledFuture<?> keepAliveThread;
    private long invalidFrameCounter = 0;

    public TeleinfoSerialControllerHandler(Bridge thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        invalidFrameCounter = 0;

        keepAliveThread = scheduler.scheduleWithFixedDelay(() -> {
            if (!isInitialized()) {
                openSerialPortAndStartReceiving();
                updateStatus(ThingStatus.UNKNOWN);
            }
            logger.debug("Check Teleinfo receiveThread status...");
            logger.debug("isInitialized() = {}", isInitialized());
            TeleinfoReceiveThread receiveThreadRef = receiveThread;
            if (receiveThreadRef != null) {
                logger.debug("receiveThread.isAlive() = {}", receiveThreadRef.isAlive());
            }
            if (isInitialized() && (receiveThreadRef == null || !receiveThreadRef.isAlive())) {
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, ERROR_UNKNOWN_RETRY_IN_PROGRESS);
                logger.info("Try to restart Teleinfo receiving...");
                stopReceivingAndCloseSerialPort();
                openSerialPortAndStartReceiving();
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> keepAliveThreadRef = keepAliveThread;
        if (keepAliveThreadRef != null) {
            keepAliveThreadRef.cancel(true);
            keepAliveThread = null;
        }
        stopReceivingAndCloseSerialPort();

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void onFrameReceived(Frame frame) {
        updateStatus(ThingStatus.ONLINE);
        fireOnFrameReceivedEvent(frame);
    }

    @Override
    public void onInvalidFrameReceived(TeleinfoReceiveThread receiveThread, InvalidFrameException error) {
        invalidFrameCounter++;
        updateState(THING_SERIAL_CONTROLLER_CHANNEL_INVALID_FRAME_COUNTER, new DecimalType(invalidFrameCounter));
    }

    @Override
    public void onSerialPortInputStreamIOException(TeleinfoReceiveThread receiveThread, IOException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, ERROR_UNKNOWN_RETRY_IN_PROGRESS);
    }

    @Override
    public void continueOnReadNextFrameTimeoutException() {
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, ERROR_UNKNOWN_RETRY_IN_PROGRESS);
    }

    private void openSerialPortAndStartReceiving() {
        TeleinfoSerialControllerConfiguration config = getConfigAs(TeleinfoSerialControllerConfiguration.class);

        if (config.serialport.trim().isEmpty()) {
            logger.warn("Teleinfo port is not set.");
            return;
        }

        logger.debug("Connecting to serial port '{}'...", config.serialport);
        String currentOwner = null;
        try {
            final SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(config.serialport);
            logger.debug("portIdentifier = {}", portIdentifier);
            if (portIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        ERROR_OFFLINE_SERIAL_NOT_FOUND);
                return;
            }
            logger.debug("Opening portIdentifier");
            currentOwner = portIdentifier.getCurrentOwner();
            logger.debug("portIdentifier.getCurrentOwner() = {}", currentOwner);
            SerialPort commPort = portIdentifier.open("org.openhab.binding.teleinfo", 5000);
            serialPort = commPort;

            TeleinfoTicMode ticMode = TeleinfoTicMode.valueOf(config.ticMode);
            commPort.setSerialPortParams(ticMode.getBitrate(), SerialPort.DATABITS_7, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_EVEN);
            try {
                commPort.enableReceiveThreshold(1);
            } catch (UnsupportedCommOperationException e) {
                // rfc2217
            }
            try {
                commPort.enableReceiveTimeout(SERIAL_RECEIVE_TIMEOUT_MS);
            } catch (UnsupportedCommOperationException e) {
                // rfc2217
            }
            logger.debug("Starting receive thread");
            TeleinfoReceiveThread receiveThread = new TeleinfoReceiveThread(commPort, this,
                    config.autoRepairInvalidADPSgroupLine, ticMode, config.verifyChecksum);
            this.receiveThread = receiveThread;
            receiveThread.start();

            logger.debug("Connected to serial port '{}'", config.serialport);
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ERROR_OFFLINE_SERIAL_INUSE);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ERROR_OFFLINE_SERIAL_UNSUPPORTED);
        }
    }

    private void stopReceivingAndCloseSerialPort() {
        TeleinfoReceiveThread receiveThreadRef = receiveThread;
        if (receiveThreadRef != null) {
            receiveThreadRef.interrupt();
            try {
                receiveThreadRef.join(RECEIVER_THREAD_JOIN_DELAY_MS);
            } catch (InterruptedException e) {
            }
            receiveThreadRef.setListener(null);
            receiveThread = null;
        }
        SerialPort serialPortRef = serialPort;
        if (serialPortRef != null) {
            serialPortRef.close();
            serialPort = null;
        }
    }
}
