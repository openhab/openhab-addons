/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.TooManyListenersException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants;
import org.openhab.binding.teleinfo.internal.handler.TeleinfoAbstractControllerHandler;
import org.openhab.binding.teleinfo.internal.reader.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeleinfoSerialControllerHandler} class defines a handler for serial controller.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public class TeleinfoSerialControllerHandler extends TeleinfoAbstractControllerHandler
        implements TeleinfoReceiveThreadListener {

    private final Logger logger = LoggerFactory.getLogger(TeleinfoSerialControllerHandler.class);

    private static final int SERIAL_RECEIVE_TIMEOUT = 250;

    private SerialPortManager serialPortManager;
    private org.eclipse.smarthome.io.transport.serial.SerialPort serialPort;
    private TeleinfoReceiveThread receiveThread;
    private @Nullable TeleinfoSerialControllerConfiguration config;

    public TeleinfoSerialControllerHandler(@NonNull Bridge thing, SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Teleinfo serial controller.");
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(TeleinfoSerialControllerConfiguration.class);

        if (config.serialport == null || StringUtils.isBlank(config.serialport)) {
            logger.error("Teleinfo port is not set.");
            return;
        }

        logger.info("Connecting to serial port '{}'", config.serialport);
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(config.serialport);
            logger.debug("portIdentifier = {}", portIdentifier);
            if (portIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        TeleinfoBindingConstants.ERROR_OFFLINE_SERIAL_NOT_FOUND);
                return;
            }
            logger.debug("Opening portIdentifier");
            SerialPort commPort = portIdentifier.open("org.openhab.binding.teleinfo", 5000);
            serialPort = commPort;

            serialPort.setSerialPortParams(1200, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(SERIAL_RECEIVE_TIMEOUT);
            logger.debug("Starting receive thread");
            receiveThread = new TeleinfoReceiveThread(serialPort);
            receiveThread.addListener(this);
            receiveThread.start();

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            serialPort.addEventListener(receiveThread);
            serialPort.notifyOnDataAvailable(true);

            logger.info("Serial port is initialized");
            updateStatus(ThingStatus.ONLINE);
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    TeleinfoBindingConstants.ERROR_OFFLINE_SERIAL_INUSE);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    TeleinfoBindingConstants.ERROR_OFFLINE_SERIAL_UNSUPPORTED);
        } catch (TooManyListenersException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    TeleinfoBindingConstants.ERROR_OFFLINE_SERIAL_LISTENERS);
        }
    }

    /**
     * Closes the connection to the ZWave controller.
     */
    @Override
    public void dispose() {
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
            }
            receiveThread.removeListener(this);
            receiveThread = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        logger.info("Stopped Teleinfo serial controller");

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void onFrameReceived(@NonNull TeleinfoReceiveThread receiveThread, @NonNull Frame frame) {
        fireOnFrameReceivedEvent(frame);
    }

}
