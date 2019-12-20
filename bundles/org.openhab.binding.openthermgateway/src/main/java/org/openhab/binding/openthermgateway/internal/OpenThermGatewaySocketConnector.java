/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway.internal;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.openthermgateway.handler.TypeConverter;

/**
 * The {@link OpenThermGatewaySocketConnector} is responsible for handling the socket connection
 *
 * @author Arjen Korevaar - Initial contribution
 * @author Arjan Mels - Improved robustness by re-sending commands, handling all message types (not only Boiler)
 */
@NonNullByDefault
public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private static final int COMMAND_RESPONSE_TIME = 100;
    private static final int COMMAND_TIMEOUT = 5000;

    private OpenThermGatewayCallback callback;
    private String ipaddress;
    private int port;

    private @Nullable Socket socket;
    private @Nullable BufferedReader reader;
    private @Nullable PrintWriter writer;

    private volatile boolean stopping;
    private boolean connected;

    public OpenThermGatewaySocketConnector(OpenThermGatewayCallback callback, String ipaddress, int port) {
        this.callback = callback;
        this.ipaddress = ipaddress;
        this.port = port;
    }

    @Override
    public void run() {
        stopping = false;
        connected = false;

        try {
            callback.log(LogLevel.Info,
                    String.format("Connecting OpenThermGatewaySocketConnector to %s:%s", this.ipaddress, this.port));

            callback.connecting();

            socket = new Socket();
            socket.connect(new InetSocketAddress(this.ipaddress, this.port), COMMAND_TIMEOUT);
            socket.setSoTimeout(COMMAND_TIMEOUT);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            connected = true;

            callback.connected();

            callback.log(LogLevel.Debug, "OpenThermGatewaySocketConnector connected");

            sendCommand(GatewayCommand.parse(GatewayCommandCode.PrintReport, "A"));
            // Set the OTGW to report every message it receives and transmits
            sendCommand(GatewayCommand.parse(GatewayCommandCode.PrintSummary, "0"));

            while (!stopping && !Thread.currentThread().isInterrupted()) {
                @Nullable String message = reader.readLine();

                if (message != null) {
                    handleMessage(message);
                } else {
                    callback.log(LogLevel.Info, "Connection closed by OpenTherm Gateway");
                    break;
                }
            }

            callback.log(LogLevel.Debug, "Stopping OpenThermGatewaySocketConnector");

        } catch (Exception e) {
            callback.log(LogLevel.Error, "An error occured in OpenThermGatewaySocketConnector: %s", e.getMessage());
        } finally {

            if (writer != null) {
                writer.flush();
                writer.close();
            }

            close(reader);
            close(writer);

            connected = false;

            callback.log(LogLevel.Debug, "OpenThermGatewaySocketConnector disconnected");
            callback.disconnected();
        }
    }

    @Override
    public synchronized void stop() {
        callback.log(LogLevel.Debug, "Stopping OpenThermGatewaySocketConnector");

        stopping = true;

        close(socket);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    Map<String, Entry<Long, GatewayCommand>> pendingCommands = new HashMap<>();

    @Override
    public void sendCommand(GatewayCommand command) {
        String msg = command.toFullString();

        pendingCommands.put(command.getCode(),
                new AbstractMap.SimpleImmutableEntry<>(System.currentTimeMillis(), command));

        if (connected) {
            callback.log(LogLevel.Debug, "Sending message: %s", msg);
            writer.printf("%s\r\n", msg);
        } else {
            callback.log(LogLevel.Debug,
                    "Unable to send message: %s. OpenThermGatewaySocketConnector is not connected.", msg);
        }
    }

    private void handleMessage(String message) {
        if (message.length() > 2 && message.charAt(2) == ':') {
            String code = message.substring(0, 2);
            String value = message.substring(3);
            callback.log(LogLevel.Debug, String.format("Received command confirmation: %s: %s", code, value));
            pendingCommands.remove(code);
            return;
        }

        for (Entry<Long, GatewayCommand> timeAndCommand : pendingCommands.values()) {
            if (System.currentTimeMillis() > timeAndCommand.getKey() + COMMAND_RESPONSE_TIME) {
                callback.log(LogLevel.Debug,
                        String.format("Resending command: %s", timeAndCommand.getValue().toFullString()));
                sendCommand(timeAndCommand.getValue());
            } else if (System.currentTimeMillis() > timeAndCommand.getKey() + COMMAND_TIMEOUT) {
                pendingCommands.remove(timeAndCommand.getValue().getCode());
            }
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

    private void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) { }
        }
    }
}