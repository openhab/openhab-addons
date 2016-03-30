package org.openhab.binding.mysensors.protocol.serial;

import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
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
    private MySensorsSerialReader mysConReader = null;

    public MySensorsSerialConnection(String serialPort, int baudRate, int sendDelay) {
        super();

        this.serialPort = serialPort;
        this.baudRate = baudRate;
        this.sendDelay = sendDelay;

    }

    public MySensorsSerialConnection(String serialPort, int baudRate) {
        super();

        this.serialPort = serialPort;
        this.baudRate = baudRate;

    }

    @Override
    public boolean connect() {
        logger.debug("Connecting to {} [baudRate:{}]", serialPort, baudRate);

        serialConnection = new NRSerialPort(serialPort, baudRate);
        if (serialConnection.connect()) {
            logger.debug("Successfully connected to serial port.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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

}
