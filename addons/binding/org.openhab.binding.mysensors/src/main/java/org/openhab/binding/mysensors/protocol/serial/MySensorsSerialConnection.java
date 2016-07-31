/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.protocol.serial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;

/**
 * @author Tim Oberföll
 *
 *         Connection to the serial interface where the MySensors Gateway is conncted
 */
public class MySensorsSerialConnection extends MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsSerialConnection.class);

    private String serialPort = "";
    private int baudRate = 115200;
    private int sendDelay = 0;
    private boolean skipStartupCheck = false;

    private NRSerialPort serialConnection = null;

    private MySensorsSerialWriter mysConWriter = null;
    private MySensorsSerialReader mysConReader = null;

    public MySensorsSerialConnection(String serialPort, int baudRate, int sendDelay, boolean skipStartupCheck) {
        super(skipStartupCheck);

        this.serialPort = serialPort;
        this.baudRate = baudRate;
        this.sendDelay = sendDelay;
        this.skipStartupCheck = skipStartupCheck;

    }

    @Override
    public boolean connect() {
        logger.debug("Connecting to {} [baudRate:{}]", serialPort, baudRate);

        updateSerialProperties(serialPort);
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

            connected = startReaderWriterThread(mysConReader, mysConWriter);
        } else {
            logger.error("Can't connect to serial port. Wrong port?");
        }

        return connected;
    }

    @Override
    public void disconnect() {
        logger.debug("Shutting down serial connection!");

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
        }

        if (mysConReader != null) {
            mysConReader.stopReader();
        }

        if (serialConnection != null && serialConnection.isConnected()) {
            serialConnection.disconnect();
        }

    }

    private void updateSerialProperties(String devName) {

        /*
         * By default, RXTX searches only devices /dev/ttyS* and
         * /dev/ttyUSB*, and will therefore not find devices that
         * have been symlinked. Adding them however is tricky, see below.
         */

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
        logger.trace("ports found from identifiers: {}", StringUtils.join(allPorts, ":"));
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
        logger.trace("final port list: {}", finalPorts);

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
