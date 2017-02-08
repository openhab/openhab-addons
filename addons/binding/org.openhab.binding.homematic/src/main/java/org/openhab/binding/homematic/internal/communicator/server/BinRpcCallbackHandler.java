/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.server;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openhab.binding.homematic.internal.communicator.message.BinRpcMessage;
import org.openhab.binding.homematic.internal.communicator.parser.DeleteDevicesParser;
import org.openhab.binding.homematic.internal.communicator.parser.EventParser;
import org.openhab.binding.homematic.internal.communicator.parser.NewDevicesParser;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads a BIN-RPC message from the socket and handles the method call.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class BinRpcCallbackHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BinRpcCallbackHandler.class);

    private static final byte BIN_EMPTY_STRING[] = { 'B', 'i', 'n', 1, 0, 0, 0, 8, 0, 0, 0, 3, 0, 0, 0, 0 };
    private static final byte BIN_EMPTY_ARRAY[] = { 'B', 'i', 'n', 1, 0, 0, 0, 8, 0, 0, 1, 0, 0, 0, 0, 0 };
    private static final byte BIN_EMPTY_EVENT_LIST[] = { 'B', 'i', 'n', 1, 0, 0, 0, 21, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0,
            3, 0, 0, 0, 5, 'e', 'v', 'e', 'n', 't' };

    private Socket socket;
    private RpcEventListener listener;
    private String encoding;

    public BinRpcCallbackHandler(Socket socket, RpcEventListener listener, String encoding) {
        this.socket = socket;
        this.listener = listener;
        this.encoding = encoding;
    }

    /**
     * Reads the event from the Homematic gateway and handles the method call.
     */
    @Override
    public void run() {
        try {
            BinRpcMessage message = new BinRpcMessage(socket.getInputStream(), true, encoding);
            if (logger.isTraceEnabled()) {
                logger.trace("Event BinRpcMessage: {}", message.toString());
            }
            byte[] returnValue = handleMethodCall(message.getMethodName(), message.getResponseData());
            if (returnValue != null) {
                socket.getOutputStream().write(returnValue);
            }
        } catch (EOFException eof) {
            // ignore
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                socket.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    /**
     * Returns a valid result of the method called by the Homematic gateway.
     */
    private byte[] handleMethodCall(String methodName, Object[] responseData) throws IOException {
        if (RPC_METHODNAME_EVENT.equals(methodName)) {
            return handleEvent(responseData);
        } else if (RPC_METHODNAME_LIST_DEVICES.equals(methodName) || RPC_METHODNAME_UPDATE_DEVICE.equals(methodName)) {
            return BIN_EMPTY_ARRAY;
        } else if (RPC_METHODNAME_DELETE_DEVICES.equals(methodName)) {
            return handleDeleteDevice(responseData);
        } else if (RPC_METHODNAME_NEW_DEVICES.equals(methodName)) {
            return handleNewDevice(responseData);
        } else if (RPC_METHODNAME_SYSTEM_LISTMETHODS.equals(methodName)) {
            return handleListMethods();
        } else if (RPC_METHODNAME_SYSTEM_MULTICALL.equals(methodName)) {
            for (Object o : (Object[]) responseData[0]) {
                Map<?, ?> call = (Map<?, ?>) o;
                String method = call.get("methodName").toString();
                Object[] data = (Object[]) call.get("params");
                handleMethodCall(method, data);
            }
            return BIN_EMPTY_EVENT_LIST;
        } else {
            logger.warn("Unknown method called by Homematic gateway: " + methodName);
            return BIN_EMPTY_EVENT_LIST;
        }
    }

    /**
     * Creates a BINRPC message with the supported method names.
     */
    private byte[] handleListMethods() {
        BinRpcMessage msg = new BinRpcMessage(null, BinRpcMessage.TYPE.RESPONSE, encoding);
        List<String> events = new ArrayList<String>();
        events.add(RPC_METHODNAME_SYSTEM_MULTICALL);
        events.add(RPC_METHODNAME_EVENT);
        events.add(RPC_METHODNAME_DELETE_DEVICES);
        events.add(RPC_METHODNAME_NEW_DEVICES);
        msg.addArg(events);
        return msg.createMessage();
    }

    /**
     * Populates the extracted event to the listener.
     */
    @SuppressWarnings("finally")
    private byte[] handleEvent(Object[] message) throws IOException {
        try {
            EventParser eventParser = new EventParser();
            HmDatapointInfo dpInfo = eventParser.parse(message);
            listener.eventReceived(dpInfo, eventParser.getValue());
        } finally {
            return BIN_EMPTY_STRING;
        }
    }

    /**
     * Calls the listener when a devices has been detected.
     */
    @SuppressWarnings("finally")
    private byte[] handleNewDevice(Object[] message) throws IOException {
        try {
            NewDevicesParser ndParser = new NewDevicesParser();
            List<String> adresses = ndParser.parse(message);
            listener.newDevices(adresses);
        } finally {
            return BIN_EMPTY_ARRAY;
        }
    }

    /**
     * Calls the listener when devices has been deleted.
     */
    @SuppressWarnings("finally")
    private byte[] handleDeleteDevice(Object[] message) throws IOException {
        try {
            DeleteDevicesParser ddParser = new DeleteDevicesParser();
            List<String> adresses = ddParser.parse(message);
            listener.deleteDevices(adresses);
        } finally {
            return BIN_EMPTY_ARRAY;
        }
    }

}
