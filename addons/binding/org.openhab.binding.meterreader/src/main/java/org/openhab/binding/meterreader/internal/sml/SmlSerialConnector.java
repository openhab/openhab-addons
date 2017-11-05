/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.meterreader.internal.sml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.codec.binary.Hex;
import org.openhab.binding.meterreader.connectors.ConnectorBase;
import org.openhab.binding.meterreader.internal.helper.Baudrate;
import org.openhab.binding.meterreader.internal.helper.SerialParameter;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.transport.MessageExtractor;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.factory.DefaultSerialPortFactory;

/**
 * Represents a serial SML device connector.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
public final class SmlSerialConnector extends ConnectorBase<SmlFile> {
    SerialPort serialPort;
    InputStream inputStream;
    DataInputStream is;

    /**
     * The name of the port where the device is connected as defined in openHAB configuration.
     */
    private String portName;
    private DataOutputStream os;
    private int baudrate;

    /**
     * Contructor to create a serial connector instance.
     *
     * @param portName the port where the device is connected as defined in openHAB configuration.
     */
    public SmlSerialConnector(String portName) {
        super();
        this.portName = portName;
    }

    /**
     * Contructor to create a serial connector instance with a specific serial parameter.
     *
     * @param portName the port where the device is connected as defined in openHAB configuration.
     * @param baudrate
     */
    public SmlSerialConnector(String portName, int baudrate, int baudrateChangeDelay) {
        super();
        this.portName = portName;
        this.baudrate = baudrate;
    }

    /**
     * @throws IOException
     * @throws ConnectorException
     * @{inheritDoc}
     */
    @Override
    protected SmlFile getMeterValuesInternal(byte[] initMessage) throws IOException {
        SmlFile smlFile = null;

        MessageExtractor extractor;

        if (initMessage != null) {
            logger.debug("Writing init message: {}", Hex.encodeHexString(initMessage));
            os.write(initMessage);
            os.flush();
        }
        try {
            extractor = new MessageExtractor(is, 5000);
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(extractor.getSmlMessage()));

            smlFile = new SmlFile();

            while (is.available() > 0) {
                SmlMessage message = new SmlMessage();

                if (!message.decode(is)) {
                    throw new IOException("Could not decode message");
                } else {
                    smlFile.add(message);
                }
            }
        } catch (IOException e) {
            logger.error("Error at SerialConnector.getMeterValuesInternal: {}", e.getMessage());
            throw e;
        }

        return smlFile;
    }

    /**
     * @throws IOException
     * @{inheritDoc}
     */
    @Override
    public void openConnection() throws IOException {
        DefaultSerialPortFactory serialPortFactory = new DefaultSerialPortFactory();
        try {
            serialPort = serialPortFactory.createSerialPort(portName);
            SerialParameter serialParameter = SerialParameter._8N1;
            int baudrateToUse = this.baudrate == Baudrate.AUTO.getBaudrate() ? Baudrate._9600.getBaudrate()
                    : this.baudrate;
            serialPort.setSerialPortParams(baudrateToUse, serialParameter.getDatabits(), serialParameter.getStopbits(),
                    serialParameter.getParity());
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
            serialPort.notifyOnDataAvailable(true);
            is = new DataInputStream(new BufferedInputStream(serialPort.getInputStream()));
            os = new DataOutputStream(new BufferedOutputStream(serialPort.getOutputStream()));
        } catch (PortInUseException e) {
            throw new IOException(MessageFormat
                    .format("Error at SerialConnector.openConnection: port {0} is already in use.", this.portName), e);
        } catch (UnsupportedCommOperationException e) {
            throw new IOException(MessageFormat.format(
                    "Error at SerialConnector.openConnection: params for port {0} are not supported.", this.portName),
                    e);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Error at SerialConnector.openConnection: unable to get inputstream for port {0}.", this.portName),
                    e);
        } catch (NoSuchPortException e) {
            throw new IOException(MessageFormat
                    .format("Error at SerialConnector.openConnection: serial port not found {0}.", this.portName), e);
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
            }
        } catch (Exception e) {
            logger.error("Failed to close serial input stream", e);
        }
        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            logger.error("Failed to close serial output stream", e);
        }
        try {
            if (serialPort != null) {
                serialPort.close();
            }
        } catch (Exception e) {
            logger.error("Error at SerialConnector.closeConnection", e);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((portName == null) ? 0 : portName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SmlSerialConnector other = (SmlSerialConnector) obj;
        if (portName == null) {
            if (other.portName != null) {
                return false;
            }
        } else if (!portName.equals(other.portName)) {
            return false;
        }
        return true;
    }

}
