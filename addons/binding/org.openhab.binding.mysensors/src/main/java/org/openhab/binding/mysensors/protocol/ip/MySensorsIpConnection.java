package org.openhab.binding.mysensors.protocol.ip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.openhab.binding.mysensors.handler.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.handler.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.internal.MySensorsMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsIpConnection extends MySensorsBridgeConnection {

    private Logger logger = LoggerFactory.getLogger(MySensorsIpConnection.class);

    private String ipAddress = "";
    private int tcpPort = 0;
    public int sendDelay = 0;

    private BufferedReader buffRead = null;
    private Socket sock = null;

    private MySensorsIpWriter mysConWriter = null;

    public MySensorsIpConnection(String ipAddress, int tcpPort, int sendDelay) {
        super();
        this.ipAddress = ipAddress;
        this.tcpPort = tcpPort;
        this.sendDelay = sendDelay;
    }

    @Override
    public boolean connect() {
        logger.debug("Connecting to bridge ...");

        try {
            sock = new Socket(ipAddress, tcpPort);
            buffRead = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            mysConWriter = new MySensorsIpWriter(sock, this, sendDelay);
        } catch (UnknownHostException e) {
            logger.error("Error while trying to connect to: " + ipAddress + ":" + tcpPort);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Error while trying to connect InputStreamReader");
            e.printStackTrace();
        }

        if (buffRead != null) {
            connected = true;
            logger.debug("Connection to ethernet gateway successful!");
        } else {
            logger.error("Something went wrong!");
        }

        return connected;
    }

    @Override
    public void run() {
        mysConWriter.startWriter();
        try {
            if (buffRead != null) {
                while (!stopReader) {
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
                }
            }

        } catch (IOException e) {
            logger.error("exception on reading from socket, message: {}", e.getMessage());
        }
    }

    @Override
    public void disconnect() {

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
        }

        stopReader();

        // Shut down socket
        try {

            if (buffRead != null) {
                buffRead.close();
            }

            if (sock != null && sock.isConnected()) {
                sock.close();
            }
        } catch (IOException e) {
            logger.error("cannot disconnect from socket, message: {}", e.getMessage());
        }

    }
}
