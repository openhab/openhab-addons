/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.stiebelheatpump.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.openhab.binding.stiebelheatpump.internal.StiebelHeatPumpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * connector for serial port communication.
 *
 * @author Evert van Es (originaly copied from)
 * @author Peter Kreutzer
 */
public class SerialConnector implements ProtocolConnector {

    private static final Logger logger = LoggerFactory.getLogger(SerialConnector.class);

    InputStream in = null;
    DataOutputStream out = null;
    SerialPort serialPort = null;
    ByteStreamPipe byteStreamPipe = null;

    private CircularByteBuffer buffer;

    @Override
    public void connect(String device, int baudrate) {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(device);

            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            serialPort = (SerialPort) commPort;
            setSerialPortParameters(baudrate);

            in = serialPort.getInputStream();
            out = new DataOutputStream(serialPort.getOutputStream());

            out.flush();

            buffer = new CircularByteBuffer(Byte.MAX_VALUE * Byte.MAX_VALUE + 2 * Byte.MAX_VALUE);
            byteStreamPipe = new ByteStreamPipe(in, buffer);
            byteStreamPipe.startTask();

        } catch (NoSuchPortException e) {
            String portNames = "";
            Enumeration ports = CommPortIdentifier.getPortIdentifiers();

            while (ports.hasMoreElements()) {
                CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
                portNames = portNames + ":" + port.getName();
            }
            throw new RuntimeException("Serial port with given name does not exist. Available ports: " + portNames, e);
        } catch (Exception e) {
            throw new RuntimeException("Could not init port", e);
        }
    }

    @Override
    public void disconnect() {
        logger.debug("Interrupt serial connection");
        byteStreamPipe.stopTask();

        logger.debug("Close serial stream");
        try {
            out.close();
            serialPort.close();
            buffer.stop();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
            logger.warn("Could not fully shut down heat pump driver", e);
        }

        logger.debug("Disconnected");
    }

    @Override
    public byte get() throws StiebelHeatPumpException {
        return buffer.get();
    }

    @Override
    public short getShort() throws StiebelHeatPumpException {
        return buffer.getShort();
    }

    @Override
    public void get(byte[] data) throws StiebelHeatPumpException {
        buffer.get(data);
    }

    @Override
    public void mark() {
        buffer.mark();
    }

    @Override
    public void reset() {
        buffer.reset();
    }

    @Override
    public void write(byte[] data) {
        try {
            logger.debug("Send request message : {}", DataParser.bytesToHex(data));
            out.write(data);
            out.flush();

        } catch (IOException e) {
            throw new RuntimeException("Could not write", e);
        }
    }

    @Override
    public void write(byte data) {
        try {
            logger.trace(String.format("Send %02X", data));
            out.write(data);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("Could not write", e);
        }
    }

    /**
     * Sets the serial port parameters to xxxxbps-8N1
     *
     * @param baudrate
     *            used to initialize the serial connection
     */
    protected void setSerialPortParameters(int baudrate) throws IOException {

        try {
            // Set serial port to xxxbps-8N1
            serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException ex) {
            throw new IOException("Unsupported serial port parameter for serial port");
        }
    }
}
