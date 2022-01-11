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
package org.openhab.binding.homematic.internal.communicator.server;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.binding.homematic.internal.communicator.parser.DeleteDevicesParser;
import org.openhab.binding.homematic.internal.communicator.parser.EventParser;
import org.openhab.binding.homematic.internal.communicator.parser.NewDevicesParser;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common RPC response methods.
 *
 * @author Gerhard Riegler - Initial contribution
 */

public abstract class RpcResponseHandler<T> {
    private final Logger logger = LoggerFactory.getLogger(RpcResponseHandler.class);

    private RpcEventListener listener;

    public RpcResponseHandler(RpcEventListener listener) {
        this.listener = listener;
    }

    /**
     * Returns a valid result of the method called by the Homematic gateway.
     */
    public T handleMethodCall(String methodName, Object[] responseData) throws IOException {
        if (RPC_METHODNAME_EVENT.equals(methodName)) {
            return handleEvent(responseData);
        } else if (RPC_METHODNAME_LIST_DEVICES.equals(methodName) || RPC_METHODNAME_UPDATE_DEVICE.equals(methodName)) {
            return getEmptyArrayResult();
        } else if (RPC_METHODNAME_DELETE_DEVICES.equals(methodName)) {
            return handleDeleteDevice(responseData);
        } else if (RPC_METHODNAME_NEW_DEVICES.equals(methodName)) {
            return handleNewDevice(responseData);
        } else if (RPC_METHODNAME_SYSTEM_LISTMETHODS.equals(methodName)) {
            RpcRequest<T> msg = createRpcRequest();
            msg.addArg(getListMethods());
            return msg.createMessage();
        } else if (RPC_METHODNAME_SYSTEM_MULTICALL.equals(methodName)) {
            for (Object o : (Object[]) responseData[0]) {
                Map<?, ?> call = (Map<?, ?>) o;
                if (call != null) {
                    String method = Objects.toString(call.get("methodName"), "");
                    Object[] data = (Object[]) call.get("params");
                    handleMethodCall(method, data);
                }
            }
            return getEmptyEventListResult();
        } else if (RPC_METHODNAME_SET_CONFIG_READY.equals(methodName)) {
            return getEmptyEventListResult();
        } else {
            logger.warn("Unknown method called by Homematic gateway: {}", methodName);
            return getEmptyEventListResult();
        }
    }

    /**
     * Creates a BINRPC message with the supported method names.
     */
    private List<String> getListMethods() {
        List<String> events = new ArrayList<>();
        events.add(RPC_METHODNAME_SYSTEM_MULTICALL);
        events.add(RPC_METHODNAME_EVENT);
        events.add(RPC_METHODNAME_DELETE_DEVICES);
        events.add(RPC_METHODNAME_NEW_DEVICES);
        return events;
    }

    /**
     * Populates the extracted event to the listener.
     */
    private T handleEvent(Object[] message) throws IOException {
        EventParser eventParser = new EventParser();
        HmDatapointInfo dpInfo = eventParser.parse(message);
        listener.eventReceived(dpInfo, eventParser.getValue());
        return getEmptyStringResult();
    }

    /**
     * Calls the listener when a devices has been detected.
     */
    private T handleNewDevice(Object[] message) throws IOException {
        NewDevicesParser ndParser = new NewDevicesParser();
        List<String> adresses = ndParser.parse(message);
        listener.newDevices(adresses);
        return getEmptyArrayResult();
    }

    /**
     * Calls the listener when devices has been deleted.
     */
    private T handleDeleteDevice(Object[] message) throws IOException {
        DeleteDevicesParser ddParser = new DeleteDevicesParser();
        List<String> adresses = ddParser.parse(message);
        listener.deleteDevices(adresses);
        return getEmptyArrayResult();
    }

    /**
     * Returns a predefined result for an empty string.
     */
    protected abstract T getEmptyStringResult();

    /**
     * Returns a predefined result for an empty array.
     */
    protected abstract T getEmptyArrayResult();

    /**
     * Returns a predefined result for an empty event list.
     */
    protected abstract T getEmptyEventListResult();

    /**
     * Creates a typed RpcRequest.
     */
    protected abstract RpcRequest<T> createRpcRequest();
}
