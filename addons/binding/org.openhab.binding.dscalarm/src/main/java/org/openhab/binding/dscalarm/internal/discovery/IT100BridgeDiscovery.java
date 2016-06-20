/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.internal.discovery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.dscalarm.handler.IT100BridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * This class is responsible for discovering the DSC IT100 RS232 Serial interface.
 *
 * @author Russell Stephens - Initial Contribution
 *
 */
public class IT100BridgeDiscovery {
    private final static Logger logger = LoggerFactory.getLogger(IT100BridgeDiscovery.class);

    static final int BAUD_RATE = 9600;
    static final int RECEIVE_TIMEOUT = 15000;
    static final String IT100_SEND_STRING = "00090\r\n";
    static final String IT100_DISCOVERY_RESPONSE = "500";
    private List<String> exemptPorts = new ArrayList<String>();

    private DSCAlarmBridgeDiscovery dscAlarmBridgeDiscovery = null;

    /**
     * Constructor.
     */
    public IT100BridgeDiscovery(DSCAlarmBridgeDiscovery dscAlarmBridgeDiscovery) {
        this.dscAlarmBridgeDiscovery = dscAlarmBridgeDiscovery;
    }

    /**
     * Method for Bridge Discovery.
     */
    public synchronized void discoverBridge() {
        logger.debug("Starting IT-100 Bridge Discovery.");

        Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();

        Collection<Thing> things = dscAlarmBridgeDiscovery.thingRegistry.getAll();

        for (Thing thing : things) {
            if (thing.getThingTypeUID().toString().equals("dscalarm:it100")) {
                logger.debug("discoverBridge(): Envisalink Found - [{}]!", thing.getUID());

                IT100BridgeHandler it100BridgeHandler = (IT100BridgeHandler) thing.getHandler();

                if (it100BridgeHandler != null) {
                    String portName = it100BridgeHandler.getSerialPortName();
                    if (!portName.isEmpty()) {
                        exemptPorts.add(portName);
                        logger.debug("discoverBridge(): Added IP Address [{}] to Exempt List!", portName);
                    }
                }
            }
        }

        while (ports.hasMoreElements()) {
            CommPortIdentifier portIdentifier = (CommPortIdentifier) ports.nextElement();

            String portName = portIdentifier.getName();

            if (!exemptPorts.contains(portName)) {
                if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    SerialPort serialPort = null;
                    OutputStreamWriter serialOutput = null;
                    BufferedReader serialInput = null;

                    try {
                        CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
                        serialPort = (SerialPort) commPort;
                        serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                        serialPort.enableReceiveThreshold(1);

                        serialOutput = new OutputStreamWriter(serialPort.getOutputStream(), "US-ASCII");
                        serialInput = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

                        serialPort.enableReceiveTimeout(RECEIVE_TIMEOUT);
                        String message = "";

                        serialOutput.write(IT100_SEND_STRING);
                        serialOutput.flush();

                        try {
                            message = serialInput.readLine();
                        } catch (IOException e) {
                            logger.debug("discoverBridge(): No Message Read from Serial Port '{}'", portIdentifier.getName());
                            continue;
                        }

                        if (message.substring(0, 3).equals(IT100_DISCOVERY_RESPONSE)) {
                            logger.debug("discoverBridge(): Serial Port '{}' Found!", portIdentifier.getName());
                            dscAlarmBridgeDiscovery.addIT100Bridge(portIdentifier.getName());
                        } else {
                            logger.debug("discoverBridge(): Incorrect Response from Serial Port! '{}' - {}", portIdentifier.getName(), message);
                        }

                    } catch (UnsupportedCommOperationException e) {
                        logger.debug("discoverBridge(): Unsupported Comm Operation Exception - '{}': {}", portIdentifier.getName(), e.toString());
                    } catch (PortInUseException e) {
                        logger.debug("discoverBridge(): Port in Use Exception - '{}': {}", portIdentifier.getName(), e.toString());
                    } catch (UnsupportedEncodingException e) {
                        logger.debug("discoverBridge(): Unsupported Encoding Exception - '{}': {}", portIdentifier.getName(), e.toString());
                    } catch (IOException e) {
                        logger.debug("discoverBridge(): IO Exception - '{}': ", portIdentifier.getName(), e.toString());
                    } finally {
                        if (serialInput != null) {
                            IOUtils.closeQuietly(serialInput);
                            serialInput = null;
                        }

                        if (serialOutput != null) {
                            IOUtils.closeQuietly(serialOutput);
                            serialOutput = null;
                        }

                        if (serialPort != null) {
                            serialPort.close();
                            serialPort = null;
                        }
                    }
                }
            } else {
                logger.debug("discoverBridge(): Serial Port already in Use by IT100 Bridge [{}]", portName);
            }

        }
    }
}
