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
package org.openhab.binding.caddx.internal.discovery;

import java.io.IOException;
import java.util.Iterator;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.caddx.internal.CaddxCommunicator;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxProtocol;
import org.openhab.binding.caddx.internal.CaddxPanelListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the Caddx RS232 Serial interface.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxBridgeDiscovery implements CaddxPanelListener {
    private final Logger logger = LoggerFactory.getLogger(CaddxBridgeDiscovery.class);

    static final int[] BAUDRATES = { 9600, 19200, 38400, 57600, 115200 };
    static final byte[] CADDX_DISCOVERY_INTERFACE_CONFIGURATION_MESSAGE = { 0x21 };

    private SerialPortManager portManager;
    private CaddxDiscoveryService caddxDiscoveryService;

    /**
     * Constructor.
     */
    public CaddxBridgeDiscovery(SerialPortManager portManager, CaddxDiscoveryService caddxDiscoveryService) {
        this.caddxDiscoveryService = caddxDiscoveryService;
        this.portManager = portManager;
    }

    public synchronized void discoverBridge3() {
        logger.trace("Starting Caddx Bridge Discovery.");

        Stream<SerialPortIdentifier> ports = portManager.getIdentifiers();
        Iterator<SerialPortIdentifier> iterator = ports.iterator();
        while (iterator.hasNext()) {
            SerialPortIdentifier portIdentifier = iterator.next();

            try {
                for (int baudrate : BAUDRATES) {
                    if (checkforBridge(CaddxProtocol.Binary, portIdentifier.getName(), baudrate)) {
                        break;
                    }
                    if (checkforBridge(CaddxProtocol.Ascii, portIdentifier.getName(), baudrate)) {
                        break;
                    }
                }
            } catch (UnsupportedCommOperationException | PortInUseException | IOException
                    | TooManyListenersException e1) {
                logger.debug("Port: {} is not applicable.", portIdentifier.getName());
                continue;
            }

            // if the thread was interrupted then exit the discovery loop
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }

    volatile boolean bridgeFound = false;

    private boolean checkforBridge(CaddxProtocol protocol, String serialPort, int baudrate)
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException {
        logger.debug("Checking protocol: {}, port: {}, baud: {}", protocol, serialPort, baudrate);

        bridgeFound = false;
        CaddxCommunicator caddxCommunicator = new CaddxCommunicator(portManager, protocol, serialPort, baudrate);
        caddxCommunicator.addListener(this);
        caddxCommunicator.transmit(new CaddxMessage(CADDX_DISCOVERY_INTERFACE_CONFIGURATION_MESSAGE, false));

        // Wait for 4 seconds for a response
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        caddxCommunicator.stop();
        caddxCommunicator = null;
        return bridgeFound;
    }

    @Override
    public void caddxMessage(CaddxCommunicator communicator, CaddxMessage caddxMessage) {
        logger.trace("Received message. [0x{}] on {} {}", String.format("%02x", caddxMessage.getMessageType()),
                communicator.getSerialPortName(), communicator.getBaudRate());

        caddxDiscoveryService.addCaddxBridge(communicator.getProtocol(), communicator.getSerialPortName(),
                communicator.getBaudRate());
        bridgeFound = true;
    }
}
