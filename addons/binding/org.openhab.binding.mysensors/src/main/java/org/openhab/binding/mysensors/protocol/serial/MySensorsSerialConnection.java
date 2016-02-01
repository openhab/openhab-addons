package org.openhab.binding.mysensors.protocol.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.internal.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * @author Tim Oberföll
 *
 *         Connection to the serial interface where the MySensors Gateway is conncted
 */
public class MySensorsSerialConnection extends MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsSerialConnection.class);

    private String serialPort = "";
    private int baudRate = 115200;
    public int sendDelay = 0;

    // private NRSerialPort serialConnection = null;
    private SerialPort serialConnection = null;

    private MySensorsSerialWriter mysConWriter = null;

    public MySensorsSerialConnection(String serialPort, int baudRate, int sendDelay) {
        super();

        this.serialPort = serialPort;
        this.baudRate = baudRate;
        this.sendDelay = sendDelay;

    }

    @Override
    public boolean connect() {
        logger.debug("Connecting to {} [baudRate:{}]", serialPort, baudRate);

        // serialConnection = new NRSerialPort(serialPort, baudRate);
        // if (serialConnection.connect()) {
        // logger.debug("Successfully connected to serial port.");
        // connected = true;
        // mysConWriter = new MySensorsSerialWriter(serialConnection, this, sendDelay);
        // } else {
        // logger.error("Can't connect to serial port. Wrong port?");
        // }

        try {
            CommPortIdentifier serialPortIdentifier = CommPortIdentifier.getPortIdentifier(serialPort);
            if (serialPortIdentifier != null) {
                if (!serialPortIdentifier.isCurrentlyOwned()) {
                    CommPort c = serialPortIdentifier.open(this.getClass().getName(), 2000);
                    if (c != null && c instanceof SerialPort) {
                        serialConnection = (SerialPort) c;
                        serialConnection.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                                SerialPort.PARITY_NONE);
                        connected = true;
                        mysConWriter = new MySensorsSerialWriter(serialConnection, this, sendDelay);
                    } else {
                        logger.error("com port is not an instance of serial port");
                    }
                } else {
                    logger.error("port " + serialPort + " is already in use");
                }
            }
        } catch (Exception e) {
            logger.error(
                    "failed to connect to port: " + serialPort + " " + e.getClass() + ", message: " + e.getMessage());
            e.getStackTrace();
        }

        return connected;
    }

    /**
     * Thread that holds the serial connection and listens to messages
     * send from the MySensors network via serial to the controller
     */
    @Override
    public void run() {

        mysConWriter.startWriter();

        BufferedReader buffRead = null;
        try {
            buffRead = new BufferedReader(new InputStreamReader(serialConnection.getInputStream()));
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String line = null;

        while (!stopReader) {
            // Is there something to read?
            // String line = buffRead.readLine();
            try {
                line = buffRead.readLine();
                logger.debug(line);
                MySensorsMessage msg = MySensorsMessageParser.parse(line);
                if (msg != null) {
                    MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
                    for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                        mySensorsEventListener.statusUpdateReceived(event);
                    }
                }
            } catch (Exception e) {
                // FIXME this exception has to be fixed, is not normal to have exception: Underlying input stream
                // returned zero bytes
                logger.error("exception on reading from serial port, message: {}", e.getMessage());
            }

        }

    }

    @Override
    public void disconnect() {
        logger.debug("Shutting down serial connection!");

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
        }

        stopReader();

        // if (serialConnection != null && serialConnection.isConnected()) {
        // serialConnection.disconnect();
        // }

        if (serialConnection != null) {
            serialConnection.close();
        }
    }

}
