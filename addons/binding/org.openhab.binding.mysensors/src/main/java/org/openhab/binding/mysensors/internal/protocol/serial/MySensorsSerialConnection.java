/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol.serial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.handler.MySensorsBridgeHandler;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;

/**
 * Connection to the serial interface where the MySensors Gateway is connected.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class MySensorsSerialConnection extends MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsSerialConnection.class);

    private String serialPort = "";
    private int baudRate = 115200;
    private int sendDelay = 0;

    private NRSerialPort serialConnection = null;

    public MySensorsSerialConnection(MySensorsBridgeHandler bridgeHandler, String serialPort, int baudRate,
            int sendDelay) {
        super(bridgeHandler);

        this.serialPort = serialPort;
        this.baudRate = baudRate;
        this.sendDelay = sendDelay;
    }

    /**
     * Tries to accomplish a connection via a serial port to the serial gateway.
     */
    @Override
    public boolean _connect() {
        logger.debug("Connecting to {} [baudRate:{}]", serialPort, baudRate);

        boolean ret = false;

        updateSerialProperties(serialPort);
        // deleteLockFile(serialPort);

        serialConnection = new NRSerialPort(serialPort, baudRate);
        if (serialConnection.connect()) {
            logger.debug("Successfully connected to serial port.");

            try {
                logger.debug("Waiting {} seconds to allow correct reset trigger on serial connection opening",
                        MySensorsBindingConstants.RESET_TIME / 1000);
                Thread.sleep(MySensorsBindingConstants.RESET_TIME);
            } catch (InterruptedException e) {
                logger.error("Interrupted reset time wait");
            }

            mysConReader = new MySensorsSerialReader(serialConnection.getInputStream(), this);
            mysConWriter = new MySensorsSerialWriter(serialConnection.getOutputStream(), this, sendDelay);

            ret = startReaderWriterThread(mysConReader, mysConWriter);
        } else {
            logger.error("Can't connect to serial port. Wrong port?");
        }

        return ret;
    }

    /**
     * Initiates a clean disconnect from the serial gateway.
     */
    @Override
    public void _disconnect() {
        logger.debug("Shutting down serial connection!");

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
            mysConWriter = null;
        }

        if (mysConReader != null) {
            mysConReader.stopReader();
            mysConReader = null;
        }

        if (serialConnection != null) {
            try {
                serialConnection.disconnect();
            } catch (Exception e) {
            }
            serialConnection = null;
        }

    }

    /**
     * By default, RXTX searches only devices /dev/ttyS* and
     * /dev/ttyUSB*, and will therefore not find devices that
     * have been symlinked. Adding them however is tricky, see below.
     *
     * @param devName is the device used as COM/UART port
     */
    private void updateSerialProperties(String devName) {

        //
        // first go through the port identifiers to find any that are not in
        // "gnu.io.rxtx.SerialPorts"
        //
        ArrayList<String> allPorts = new ArrayList<String>();
        @SuppressWarnings("rawtypes")
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                allPorts.add(id.getName());
            }
        }
        logger.trace("Ports found from identifiers: {}", StringUtils.join(allPorts, ":"));
        //
        // now add our port so it's in the list
        //
        if (!allPorts.contains(devName)) {
            allPorts.add(devName);
        }
        //
        // add any that are already in "gnu.io.rxtx.SerialPorts"
        // so we don't accidentally overwrite some of those ports

        String ports = System.getProperty("gnu.io.rxtx.SerialPorts");
        if (ports != null) {
            ArrayList<String> propPorts = new ArrayList<String>(Arrays.asList(ports.split(":")));
            for (String p : propPorts) {
                if (!allPorts.contains(p)) {
                    allPorts.add(p);
                }
            }
        }
        String finalPorts = StringUtils.join(allPorts, ":");
        logger.debug("Final port list: {}", finalPorts);

        //
        // Finally overwrite the "gnu.io.rxtx.SerialPorts" System property.
        //
        // Note: calling setProperty() is not threadsafe. All bindings run in
        // the same address space, System.setProperty() is globally visible
        // to all bindings.
        // This means if multiple bindings use the serial port there is a
        // race condition where two bindings could be changing the properties
        // at the same time
        //
        System.setProperty("gnu.io.rxtx.SerialPorts", finalPorts);
    }
}
