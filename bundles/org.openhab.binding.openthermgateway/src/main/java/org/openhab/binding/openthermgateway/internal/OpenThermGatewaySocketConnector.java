/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenThermGatewaySocketConnector} is responsible for handling the socket connection
 *
 * @author Arjen Korevaar - Initial contribution
 * @author Arjan Mels - Improved robustness by re-sending commands, handling all message types (not only Boiler)
 */
@NonNullByDefault
public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private static final int COMMAND_RESPONSE_TIME_MILLISECONDS = 100;
    private static final int COMMAND_TIMEOUT_MILLISECONDS = 5000;

    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewaySocketConnector.class);

    private final OpenThermGatewayCallback callback;
    private final String ipaddress;
    private final int port;

    private @Nullable PrintWriter writer;

    private volatile boolean stopping;
    private boolean connected;

    private Map<String, Entry<Long, GatewayCommand>> pendingCommands = new ConcurrentHashMap<>();

    public OpenThermGatewaySocketConnector(OpenThermGatewayCallback callback, String ipaddress, int port) {
        this.callback = callback;
        this.ipaddress = ipaddress;
        this.port = port;
    }

    @Override
    public void run() {
        stopping = false;
        connected = false;

        logger.debug("Connecting OpenThermGatewaySocketConnector to {}:{}", this.ipaddress, this.port);

        callback.connecting();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(this.ipaddress, this.port), COMMAND_TIMEOUT_MILLISECONDS);
            socket.setSoTimeout(COMMAND_TIMEOUT_MILLISECONDS);

            connected = true;

            callback.connected();

            logger.debug("OpenThermGatewaySocketConnector connected");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter wrt = new PrintWriter(socket.getOutputStream(), true)) {
                // Make writer accessible on class level
                writer = wrt;

                sendCommand(GatewayCommand.parse(GatewayCommandCode.PrintReport, "A"));
                // Set the OTGW to report every message it receives and transmits
                sendCommand(GatewayCommand.parse(GatewayCommandCode.PrintSummary, "0"));

                while (!stopping && !Thread.currentThread().isInterrupted()) {
                    @Nullable
                    String message = reader.readLine();

                    if (message != null) {
                        handleMessage(message);
                    } else {
                        logger.debug("Connection closed by OpenTherm Gateway");
                        break;
                    }
                }

                logger.debug("Stopping OpenThermGatewaySocketConnector");
            } finally {
                connected = false;

                logger.debug("OpenThermGatewaySocketConnector disconnected");
                callback.disconnected();
            }
        } catch (IOException ex) {
            logger.warn("Unable to connect to the OpenTherm Gateway.", ex);
            callback.disconnected();
        }
    }

    @Override
    public void stop() {
        logger.debug("Stopping OpenThermGatewaySocketConnector");
        stopping = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void sendCommand(GatewayCommand command) {
        @Nullable
        PrintWriter wrtr = writer;

        String msg = command.toFullString();

        pendingCommands.put(command.getCode(),
                new AbstractMap.SimpleImmutableEntry<>(System.currentTimeMillis(), command));

        if (connected) {
            logger.debug("Sending message: {}", msg);
            if (wrtr != null) {
                wrtr.print(msg + "\r\n");
                wrtr.flush();
            }
        } else {
            logger.debug("Unable to send message: {}. OpenThermGatewaySocketConnector is not connected.", msg);
        }
    }

    private void handleMessage(String message) {
        if (message.length() > 2 && message.charAt(2) == ':') {
            String code = message.substring(0, 2);
            String value = message.substring(3);

            logger.debug("Received command confirmation: {}: {}", code, value);
            pendingCommands.remove(code);
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Entry<Long, GatewayCommand> timeAndCommand : pendingCommands.values()) {
            long responseTime = timeAndCommand.getKey() + COMMAND_RESPONSE_TIME_MILLISECONDS;
            long timeoutTime = timeAndCommand.getKey() + COMMAND_TIMEOUT_MILLISECONDS;

            if (currentTime > responseTime && currentTime <= timeoutTime) {
                logger.debug("Resending command: {}", timeAndCommand.getValue());
                sendCommand(timeAndCommand.getValue());
            } else if (currentTime > timeoutTime) {
                pendingCommands.remove(timeAndCommand.getValue().getCode());
            }
        }

        Message msg = Message.parse(message);

        if (msg == null) {
            logger.trace("Received message: {}, (unknown)", message);
            return;
        } else {
            logger.trace("Received message: {}, {} {} {}", message, msg.getID(), msg.getCode(), msg.getMessageType());
        }

        if (DataItemGroup.dataItemGroups.containsKey(msg.getID())) {
            DataItem[] dataItems = DataItemGroup.dataItemGroups.get(msg.getID());

            for (DataItem dataItem : dataItems) {
                State state = null;

                switch (dataItem.getDataType()) {
                    case FLAGS:
                        state = OnOffType.from(msg.getBit(dataItem.getByteType(), dataItem.getBitPos()));
                        break;
                    case UINT8:
                    case UINT16:
                        state = new DecimalType(msg.getUInt(dataItem.getByteType()));
                        break;
                    case INT8:
                    case INT16:
                        state = new DecimalType(msg.getInt(dataItem.getByteType()));
                        break;
                    case FLOAT:
                        state = new DecimalType(msg.getFloat());
                        break;
                    case DOWTOD:
                        break;
                }

                logger.trace("  Data: {} {} {} {}", dataItem.getID(), dataItem.getSubject(), dataItem.getDataType(),
                        state == null ? "" : state);
            }
        }

        if (msg.getMessageType() == MessageType.READACK || msg.getMessageType() == MessageType.WRITEDATA
                || msg.getID() == 0 || msg.getID() == 1) {
            receiveMessage(msg);
        }
    }

    private void receiveMessage(Message message) {
        callback.receiveMessage(message);
    }
}
