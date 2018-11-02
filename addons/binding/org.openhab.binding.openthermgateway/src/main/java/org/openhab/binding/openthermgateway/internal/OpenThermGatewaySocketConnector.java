package org.openhab.binding.openthermgateway.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.openthermgateway.handler.TypeConverter;

public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private OpenThermGatewayCallback callback;
    private String ipaddress;
    private int port;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    private volatile boolean stopping;
    private boolean connected;
    private Message previousMessage;

    public OpenThermGatewaySocketConnector(OpenThermGatewayCallback callback, String ipaddress, int port) {
        this.callback = callback;
        this.ipaddress = ipaddress;
        this.port = port;
    }

    @Override
    public synchronized void stop() {
        callback.log(LogLevel.Debug, "Stopping OpenThermGatewaySocketConnector");
        stopping = true;
    }

    @Override
    public void run() {
        stopping = false;
        connected = false;

        try {
            callback.log(LogLevel.Debug,
                    String.format("Connecting OpenThermGatewaySocketConnector to %s:%s", this.ipaddress, this.port));

            callback.connecting();

            socket = new Socket(this.ipaddress, this.port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            connected = true;

            callback.connected();

            callback.log(LogLevel.Debug, "OpenThermGatewaySocketConnector connected");

            sendCommand(GatewayCommand.parse(GatewayCommandCode.PrintReport, "A"));
            // Set the OTGW to report every message it receives and transmits
            sendCommand(GatewayCommand.parse(GatewayCommandCode.PrintSummary, "0"));

            while (!stopping && !Thread.currentThread().isInterrupted()) {
                String message = reader.readLine();
                handleMessage(message);
            }

            callback.log(LogLevel.Debug, "Stopping OpenThermGatewaySocketConnector");

        } catch (Exception e) {
            callback.log(LogLevel.Error, "An error occured in OpenThermGatewaySocketConnector", e);
        } finally {

            if (writer != null) {
                writer.flush();
                writer.close();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }

            connected = false;

            callback.log(LogLevel.Debug, "OpenThermGatewaySocketConnector disconnected");
            callback.disconnected();
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void sendCommand(GatewayCommand command) {
        String msg = command.toFullString();

        if (connected) {
            callback.log(LogLevel.Debug, "Sending message: %s", msg);
            writer.printf("%s\r\n", msg);
        } else {
            callback.log(LogLevel.Debug,
                    "Unable to send message: %s. OpenThermGatewaySocketConnector is not connected.", msg);
        }
    }

    private void handleMessage(String message) {
        if (message == null) {
            return;
        }

        Message msg = Message.parse(message);

        if (msg == null) {
            callback.log(LogLevel.Debug, "Received message: %s, (unknown)", message);
            return;
        } else {
            callback.log(LogLevel.Debug, String.format("Received message: %s, %d %s %s", message, msg.getID(),
                    msg.getCode(), msg.getMessageType().toString()));
        }

        if (DataItemGroup.dataItemGroups.containsKey(msg.getID())) {
            DataItem[] dataItems = DataItemGroup.dataItemGroups.get(msg.getID());

            for (int i = 0; i < dataItems.length; i++) {
                DataItem dataItem = dataItems[i];

                State state = null;

                switch (dataItem.getDataType()) {
                    case Flags:
                        state = TypeConverter.toOnOffType(msg.getBit(dataItem.getByteType(), dataItem.getBitPos()));
                        break;
                    case Uint8:
                    case Uint16:
                        state = TypeConverter.toDecimalType(msg.getUInt(dataItem.getByteType()));
                        break;
                    case Int8:
                    case Int16:
                        state = TypeConverter.toDecimalType(msg.getInt(dataItem.getByteType()));
                        break;
                    case Float:
                        state = TypeConverter.toDecimalType(msg.getFloat());
                        break;
                    case DoWToD:
                        break;
                }
                callback.log(LogLevel.Trace,
                        String.format("  Data %d: %d %s %s %s", i, dataItem.getID(), dataItem.getSubject(),
                                dataItem.getDataType().toString(), state == null ? "" : state.toString()));

            }
        }

        if (msg.getMessageType() == MessageType.ReadAck || msg.getMessageType() == MessageType.WriteData) {
            receiveMessage(msg);
        }
    }

    private void receiveMessage(Message message) {
        if (message != null && callback != null) {
            callback.receiveMessage(message);
        }
    }
}