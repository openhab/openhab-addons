package org.openhab.binding.openthermgateway.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private OpenThermGatewayCallback callback;
    private String ipaddress;
    private int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private boolean stopping;
    private Message previousMessage;

    // TODO: logging doesnt work, runs on different thread, use callback ?
    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewaySocketConnector.class);

    public OpenThermGatewaySocketConnector(OpenThermGatewayCallback callback, String ipaddress, int port) {
        this.callback = callback;
        this.ipaddress = ipaddress;
        this.port = port;
    }

    public void stop() {
        stopping = true;
    }

    @Override
    public void run() {
        try {
            logger.debug("Starting OpenThermGatewaySocketConnector");

            callback.connecting();

            socket = new Socket(this.ipaddress, this.port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            callback.connected();
            logger.debug("OpenThermGatewaySocketConnector connected");

            sendCommand(CommandType.PrintSummary, "0");

            String message;

            while (!stopping && !Thread.interrupted()) {
                message = reader.readLine();
                handleMessage(message);
            }

            logger.debug("Stopping OpenThermGatewaySocketConnector");

            reader.close();
            writer.close();
            socket.close();
        } catch (UnknownHostException e) {
            logger.error("An error occured in OpenThermGatewaySocketConnector", e);
        } catch (IOException e) {
            logger.error("An error occured in OpenThermGatewaySocketConnector", e);
        }

        callback.disconnected();
    }

    @Override
    public void sendCommand(CommandType commandType, String message) {
        if (Command.commands.containsKey(commandType)) {
            Command command = Command.commands.get(commandType);

            String msg = command.getMessage(message);

            logger.debug("Sending command {0}", msg);
            writer.println(msg);
        } else {
            logger.debug("No command found for commandType {0}", commandType.toString());
        }
    }

    private void handleMessage(String message) {
        Message msg = Message.parse(message);

        if (msg != null) {
            if (msg.getCode().equals("B")) {
                // Dont handle messages received from the thermostat
                receiveMessage(msg);
            }
        }
        // if (msg != null) {
        // if (msg.overrides(previousMessage)) {
        // previousMessage = null;
        // receiveMessage(msg);
        // } else {
        // receiveMessage(previousMessage);
        // previousMessage = msg;
        // }
        // }
    }

    private void receiveMessage(Message message) {
        if (message != null) {
            logger.debug(message.toString());
            callback.receiveMessage(message);
        }
    }
}