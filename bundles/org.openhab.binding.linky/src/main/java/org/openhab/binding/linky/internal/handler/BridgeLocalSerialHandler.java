/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.handler;

import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.InvalidFrameException;
import org.openhab.binding.linky.internal.LinkyChannel;
import org.openhab.binding.linky.internal.LinkyFrame;
import org.openhab.binding.linky.internal.LinkySerialConfiguration;
import org.openhab.binding.linky.internal.LinkySerialInputStream;
import org.openhab.binding.linky.internal.types.TeleinfoTicMode;
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

import com.google.gson.Gson;

/**
 * The {@link TeleinfoSerialControllerHandler} class defines a handler for serial controller.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class BridgeLocalSerialHandler extends BridgeLocalBaseHandler {

    private final Logger logger = LoggerFactory.getLogger(BridgeLocalSerialHandler.class);

    private static final int SERIAL_RECEIVE_TIMEOUT_MS = 10000;

    private SerialPortManager serialPortManager;
    private @Nullable ScheduledFuture<?> pollingJob;
    private long invalidFrameCounter = 0;

    public BridgeLocalSerialHandler(Bridge bridge, SerialPortManager serialPortManager, Gson gson) {
        super(bridge, gson);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        invalidFrameCounter = 0;

        logger.debug("Check Teleinfo receiveThread status...");
        logger.debug("isInitialized() = {}", isInitialized());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        pollingJob = scheduler.schedule(this::pollingCode, 1, TimeUnit.SECONDS);
    }

    public void pollingCode() {
        LinkySerialConfiguration config = getConfigAs(LinkySerialConfiguration.class);
        TeleinfoTicMode ticMode = TeleinfoTicMode.valueOf(config.ticMode);
        boolean autoRepair = config.autoRepairInvalidADPSgroupLine;
        boolean verifyChecksum = config.verifyChecksum;
        boolean interrupted = false;

        SerialPort serialPort = openSerialPortAndStartReceiving();

        if (serialPort != null) {
            logger.debug("Start to wait for data ...{}", config.serialport);

            try (LinkySerialInputStream teleinfoStream = new LinkySerialInputStream(serialPort.getInputStream(),
                    autoRepair, ticMode, verifyChecksum)) {
                while (!interrupted) {
                    try {
                        LinkyFrame nextFrame = teleinfoStream.readNextFrame();
                        if (nextFrame != null) {
                            onFrameReceived(nextFrame);
                        }
                    } catch (InvalidFrameException e) {
                        logger.warn("Got invalid frame. Detail: \"{}\"", e.getLocalizedMessage());
                        onInvalidFrameReceived(e);
                    } catch (IOException e) {
                        // logger.warn("Got I/O exception. Detail: \"{}\"", e.getLocalizedMessage(), e);
                        // onSerialPortInputStreamIOException(e);
                        // break;
                    } catch (IllegalStateException e) {
                        logger.warn("Got illegal state exception", e);
                    }
                }
            } catch (IOException e) {
                logger.warn("An error occurred during serial port input stream opening", e);
            }

            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /*
     * @Override
     *
     * public Collection<Class<? extends ThingHandlerService>> getServices() {
     * return Set.of(TeleinfoDiscoveryService.class);
     * }
     */

    public void onFrameReceived(LinkyFrame frame) {
        updateStatus(ThingStatus.ONLINE);
        logger.info("frame received!!");

        String prmId = frame.get(LinkyChannel.PRM);
        if (prmId != null) {
            ThingLinkyLocalHandler handler = getHandlerForPrmId(prmId);
            if (handler != null) {
                handler.handleFrame(frame);
            }
        }
    }

    public void onInvalidFrameReceived(InvalidFrameException error) {
        invalidFrameCounter++;
        updateState(THING_SERIAL_CONTROLLER_CHANNEL_INVALID_FRAME_COUNTER, new DecimalType(invalidFrameCounter));
    }

    public void onSerialPortInputStreamIOException(IOException e) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, ERROR_UNKNOWN_RETRY_IN_PROGRESS);
    }

    // public void continueOnReadNextFrameTimeoutException() {
    // updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, ERROR_UNKNOWN_RETRY_IN_PROGRESS);
    // }

    private @Nullable SerialPort openSerialPortAndStartReceiving() {
        LinkySerialConfiguration config = getConfigAs(LinkySerialConfiguration.class);

        if (config.serialport.trim().isEmpty()) {
            logger.warn("Teleinfo port is not set.");
            return null;
        }

        logger.debug("Connecting to serial port '{}'...", config.serialport);
        String currentOwner = null;
        try {
            final SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(config.serialport);
            logger.debug("portIdentifier = {}", portIdentifier);
            if (portIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        ERROR_OFFLINE_SERIAL_NOT_FOUND);
                return null;
            }
            logger.debug("Opening portIdentifier");
            currentOwner = portIdentifier.getCurrentOwner();
            logger.debug("portIdentifier.getCurrentOwner() = {}", currentOwner);
            SerialPort commPort = portIdentifier.open("org.openhab.binding.teleinfo", 5000);

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
            logger.debug("Connected to serial port '{}'", config.serialport);
            return commPort;
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ERROR_OFFLINE_SERIAL_INUSE);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    ERROR_OFFLINE_SERIAL_UNSUPPORTED);
        }

        return null;
    }
}
