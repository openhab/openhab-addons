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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.caddx.internal.CaddxBindingConstants;
import org.openhab.binding.caddx.internal.CaddxCommunicator;
import org.openhab.binding.caddx.internal.CaddxMessage;
import org.openhab.binding.caddx.internal.CaddxMessageType;
import org.openhab.binding.caddx.internal.CaddxPanelListener;
import org.openhab.binding.caddx.internal.CaddxProtocol;
import org.openhab.binding.caddx.internal.config.CaddxBridgeConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for discovering the Caddx RS232 Serial interface.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.caddx")
@NonNullByDefault
public class CaddxBridgeDiscovery extends AbstractDiscoveryService implements CaddxPanelListener {
    private final Logger logger = LoggerFactory.getLogger(CaddxBridgeDiscovery.class);

    private @NonNullByDefault({}) SerialPortManager portManager;
    static final int[] BAUDRATES = { 9600, 19200, 38400, 57600, 115200 };
    private volatile boolean bridgeFound = false;

    /**
     * Constructor.
     */
    public CaddxBridgeDiscovery() {
        super(CaddxBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15, true);
    }

    @Override
    protected void startScan() {
        logger.trace("Start Caddx Bridge discovery.");

        if (portManager != null) {
            discoverBridge();
        }
    }

    public synchronized void discoverBridge() {
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

    private boolean checkforBridge(CaddxProtocol protocol, String serialPort, int baudrate)
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException {
        logger.debug("Checking protocol: {}, port: {}, baud: {}", protocol, serialPort, baudrate);

        bridgeFound = false;
        CaddxCommunicator caddxCommunicator = new CaddxCommunicator(portManager, protocol, serialPort, baudrate);
        caddxCommunicator.addListener(this);
        caddxCommunicator.transmit(new CaddxMessage(CaddxMessageType.Interface_Configuration_Request, ""));

        // Wait for 4 seconds for a response
        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        caddxCommunicator.stop();
        return bridgeFound;
    }

    @Override
    public void caddxMessage(CaddxCommunicator communicator, CaddxMessage caddxMessage) {
        logger.trace("Received message. [0x{}] on {} {}", String.format("%02x", caddxMessage.getMessageType()),
                communicator.getSerialPortName(), communicator.getBaudRate());

        addCaddxBridge(communicator.getProtocol(), communicator.getSerialPortName(), communicator.getBaudRate());
        bridgeFound = true;
    }

    /**
     * Method to add a Caddx Bridge to the Smarthome Inbox.
     *
     * @param port
     */
    private void addCaddxBridge(CaddxProtocol protocol, String port, int baudrate) {
        logger.trace("addCaddxBridge(): Adding new Caddx Bridge on {} {} to Smarthome inbox", port, baudrate);

        String bridgeID = "";
        boolean containsChar = port.contains("/");

        if (containsChar) {
            String[] parts = port.split("/");
            String id = parts[parts.length - 1].toUpperCase();
            bridgeID = id.replaceAll("\\W", "_");
        } else {
            String id = port.toUpperCase();
            bridgeID = id.replaceAll("\\W", "_");
        }

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(CaddxBridgeConfiguration.PROTOCOL, protocol);
        properties.put(CaddxBridgeConfiguration.SERIAL_PORT, port);
        properties.put(CaddxBridgeConfiguration.BAUD, baudrate);

        ThingUID thingUID = new ThingUID(CaddxBindingConstants.CADDXBRIDGE_THING_TYPE, bridgeID);
        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withLabel("Caddx Bridge - " + port).build());
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        logger.trace("setSerialPortManager called.");
        this.portManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        logger.trace("unsetSerialPortManager called.");
        this.portManager = null;
    }
}
