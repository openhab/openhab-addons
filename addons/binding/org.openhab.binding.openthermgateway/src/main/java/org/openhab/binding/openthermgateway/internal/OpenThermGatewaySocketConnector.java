package org.openhab.binding.openthermgateway.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private OpenThermGatewayCallback callback;
    private String ipaddress;
    private int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private boolean stopping;
    private Message previousMessage;

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
        stopping = false;

        try {
            callback.log(LogLevel.Debug, "Starting OpenThermGatewaySocketConnector");

            callback.connecting();

            socket = new Socket(this.ipaddress, this.port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            callback.connected();

            callback.log(LogLevel.Debug, "OpenThermGatewaySocketConnector connected");

            // PS=0 causes the OTGW to report every message it receives and transmits
            sendCommand(CommandType.PrintSummary, "0");

            while (!stopping && !Thread.interrupted()) {
                String message = reader.readLine();
                handleMessage(message);
            }

            callback.log(LogLevel.Debug, "Stopping OpenThermGatewaySocketConnector");

            reader.close();
            writer.close();
            socket.close();
        } catch (UnknownHostException e) {
            callback.log(LogLevel.Error, "An error occured in OpenThermGatewaySocketConnector", e);
        } catch (IOException e) {
            callback.log(LogLevel.Error, "An error occured in OpenThermGatewaySocketConnector", e);
        }

        socket = null;
        callback.disconnected();
    }

    @Override
    public boolean isConnected() {
        return (socket != null && socket.isConnected());
    }

    @Override
    public void sendCommand(CommandType commandType, String message) {
        if (Command.commands.containsKey(commandType)) {
            Command command = Command.commands.get(commandType);

            String msg = command.getMessage(message);

            callback.log(LogLevel.Debug, "Sending message: %s", msg);
            writer.println(msg);
        } else {
            callback.log(LogLevel.Warning, "No command found for commandType %s", commandType.toString());
        }
    }

    private void handleMessage(String message) {
        callback.log(LogLevel.Debug, "Received message: %s", message);

        Message msg = Message.parse(message);

        if (msg != null) {
            if (msg.getCode().equals("B")
                    && (msg.getMessageType() == MessageType.ReadAck || msg.getMessageType() == MessageType.WriteAck)) {
                receiveMessage(msg);
            }
        }
    }

    private void receiveMessage(Message message) {
        if (message != null && callback != null) {
            callback.receiveMessage(message);
        }
    }
}