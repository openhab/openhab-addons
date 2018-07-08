/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.sml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.meterreader.connectors.ConnectorBase;
import org.openhab.binding.meterreader.internal.helper.Baudrate;
import org.openhab.binding.meterreader.internal.helper.SerialParameter;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.transport.Transport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * Represents a serial SML device connector.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
public final class SmlSerialConnector extends ConnectorBase<SmlFile> {

    private SerialPortManager serialManager;
    private SerialPort serialPort;
    private DataInputStream is;

    private DataOutputStream os;
    private int baudrate;

    /**
     * Constructor to create a serial connector instance.
     *
     * @param portName the port where the device is connected as defined in openHAB configuration.
     */
    public SmlSerialConnector(String portName) {
        super(portName);
        BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<@NonNull SerialPortManager> serialPortManagerService = bundleContext
                .getServiceReference(SerialPortManager.class);
        this.serialManager = bundleContext.getService(serialPortManagerService);
    }

    /**
     * Constructor to create a serial connector instance with a specific serial parameter.
     *
     * @param portName the port where the device is connected as defined in openHAB configuration.
     * @param baudrate
     * @throws IOException
     */
    public SmlSerialConnector(String portName, int baudrate, int baudrateChangeDelay) {
        this(portName);
        this.baudrate = baudrate;
    }

    @Override
    protected SmlFile readNext(byte[] initMessage) throws IOException {
        if (initMessage != null) {
            logger.debug("Writing init message: {}", Hex.encodeHexString(initMessage));
            os.write(initMessage);
            os.flush();
        }
        return new Transport().getSMLFile(is);
    }

    /**
     * @throws IOException
     * @{inheritDoc}
     */
    @Override
    public void openConnection() throws IOException {
        try {
            closeConnection();
            SerialPortIdentifier id = serialManager.getIdentifier(getPortName());
            if (id != null) {
                serialPort = id.open("meterreaderbinding", 0);
                SerialParameter serialParameter = SerialParameter._8N1;
                int baudrateToUse = this.baudrate == Baudrate.AUTO.getBaudrate() ? Baudrate._9600.getBaudrate()
                        : this.baudrate;
                serialPort.setSerialPortParams(baudrateToUse, serialParameter.getDatabits(),
                        serialParameter.getStopbits(), serialParameter.getParity());
                // serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                serialPort.notifyOnDataAvailable(true);
                is = new DataInputStream(new BufferedInputStream(serialPort.getInputStream()));
                os = new DataOutputStream(new BufferedOutputStream(serialPort.getOutputStream()));
            }
            throw new IllegalStateException(MessageFormat.format("Cannot open connection to {0}", getPortName()));

        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Error at SerialConnector.openConnection: unable to get inputstream for port {0}.", getPortName()),
                    e);
        }
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void closeConnection() {
        try {
            if (is != null) {
                is.close();
                is = null;
            }
        } catch (Exception e) {
            logger.error("Failed to close serial input stream", e);
        }
        try {
            if (os != null) {
                os.close();
                os = null;
            }
        } catch (Exception e) {
            logger.error("Failed to close serial output stream", e);
        }
        try {
            if (serialPort != null) {
                serialPort.close();
                serialPort = null;
            }
        } catch (Exception e) {
            logger.error("Error at SerialConnector.closeConnection", e);
        }
    }

}
