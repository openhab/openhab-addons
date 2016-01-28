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
    public int sendDelay = 0;

    private NRSerialPort serialConnection = null;

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

        serialConnection = new NRSerialPort(serialPort, baudRate);
        if (serialConnection.connect()) {
            connected = true;
            logger.debug("Successfully connected to serial port.");
            mysConWriter = new MySensorsSerialWriter(serialConnection, this, sendDelay);
        } else {
            logger.error("Can't connect to serial port. Wrong port?");
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

        InputStreamReader ins = new InputStreamReader(serialConnection.getInputStream());
        BufferedReader buffRead = new BufferedReader(ins);

        while (!stopReader) {
            try {
                // Is there something to read?
                String line = buffRead.readLine();
                if (line != null) {
                    logger.debug(line);
                    MySensorsMessage msg = MySensorsMessageParser.parse(line);
                    if (msg != null) {
                        MySensorsStatusUpdateEvent event = new MySensorsStatusUpdateEvent(msg);
                        for (MySensorsUpdateListener mySensorsEventListener : updateListeners) {
                            mySensorsEventListener.statusUpdateReceived(event);
                        }
                    }
                }
            } catch (IOException e) {
                // Strange behavior...
                logger.error("exception on reading from serial port, message: {}", e.getMessage());
            }
        }

    }

    @Override
    public void disconnect() {
        logger.debug("Shutting down serial connection!");

        stopReader();
        serialConnection.disconnect();
        mysConWriter.stopWriting();
    }

}
