package org.smslib.driver;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.CommunicationException;
import org.smslib.Modem;

/**
 * Manage communications with a serial modem
 * Extracted from SMSLib
 */
@NonNullByDefault
public class JSerialModemDriver extends AbstractModemDriver {

    private static final int ONE_STOP_BIT = 1;
    static final public int NO_PARITY = 0;
    static final public int FLOW_CONTROL_RTS_ENABLED = 0x00000001;
    static final public int FLOW_CONTROL_CTS_ENABLED = 0x00000010;

    static Logger logger = LoggerFactory.getLogger(JSerialModemDriver.class);

    String portName;

    int baudRate;

    private final SerialPortManager serialPortManager;

    @Nullable
    SerialPort serialPort;

    public JSerialModemDriver(SerialPortManager serialPortManager, Modem modem, String port, int baudRate) {
        super(modem);
        this.portName = port;
        this.baudRate = baudRate;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void openPort() throws CommunicationException {
        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(portName);
        if (portIdentifier == null) {
            throw new CommunicationException("SMSModem cannot use serial port " + portName);
        }
        try {
            SerialPort openedSerialPort = portIdentifier.open("org.openhab.binding.smsmodem", 2000);
            openedSerialPort.setSerialPortParams(baudRate, 8, ONE_STOP_BIT, NO_PARITY);
            openedSerialPort.setFlowControlMode(FLOW_CONTROL_RTS_ENABLED | FLOW_CONTROL_CTS_ENABLED);
            this.in = openedSerialPort.getInputStream();
            this.out = openedSerialPort.getOutputStream();
            serialPort = openedSerialPort;
            this.pollReader = new PollReader(this, getPortInfo());
            this.pollReader.setDaemon(true);
            this.pollReader.start();

        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            throw new CommunicationException("Cannot open port", e);
        }
    }

    @Override
    public void closePort() {
        try {
            logger.debug("Closing comm port: {}", getPortInfo());
            this.pollReader.cancel();
            try {
                this.pollReader.join();
            } catch (InterruptedException ex) {
                logger.debug("PollReader closing exception", ex);
            }
            if (in != null) {
                this.in.close();
                this.in = null;
            }
            if (out != null) {
                this.out.close();
                this.out = null;
            }
            final SerialPort finalSerialPort = serialPort;
            if (finalSerialPort != null) {
                finalSerialPort.close();
                serialPort = null;
            }
        } catch (IOException e) {
            logger.debug("Closing port exception", e);
        }
    }

    @Override
    public String getPortInfo() {
        return this.portName + ":" + this.baudRate;
    }
}
