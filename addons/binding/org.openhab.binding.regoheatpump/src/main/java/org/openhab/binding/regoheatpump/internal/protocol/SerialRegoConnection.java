package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

public class SerialRegoConnection implements RegoConnection {
    private final Logger logger = LoggerFactory.getLogger(SerialRegoConnection.class);
    private final int baudRate;
    private final String portName;
    private NRSerialPort serialPort;

    public SerialRegoConnection(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }

    @Override
    public void connect() throws IOException {
        if (isPortNameExist(portName)) {
            serialPort = new NRSerialPort(portName, baudRate);
            if (serialPort.connect() == false) {
                throw new IOException("Failed to connect on port " + portName);
            }

            logger.debug("Connected to {}", portName);

        } else {
            throw new IOException("Serial port with name " + portName + " does not exist. Available port names: "
                    + NRSerialPort.getAvailableSerialPorts());
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isConnected();
    }

    @Override
    public void close() {
        if (serialPort != null) {
            serialPort.disconnect();
            serialPort = null;
        }
    }

    @Override
    public void write(byte[] data) throws IOException {
        final OutputStream stream = serialPort.getOutputStream();
        stream.write(data);
        stream.flush();
    }

    @Override
    public int read() throws IOException {
        return serialPort.getInputStream().read();
    }

    private boolean isPortNameExist(String portName) {
        return NRSerialPort.getAvailableSerialPorts().contains(portName);
    }
}
