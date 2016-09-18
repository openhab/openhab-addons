/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.communicator.server;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcResponse;
import org.openhab.binding.homematic.internal.communicator.parser.DeleteDevicesParser;
import org.openhab.binding.homematic.internal.communicator.parser.EventParser;
import org.openhab.binding.homematic.internal.communicator.parser.NewDevicesParser;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fi.iki.elonen.NanoHTTPD;

/**
 * Reads a XML-RPC message and handles the method call.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class XmlRpcServer implements RpcServer {
    private final static Logger logger = LoggerFactory.getLogger(XmlRpcServer.class);
    private final static boolean TRACE_ENABLED = logger.isTraceEnabled();

    private static final String XML_EMPTY_STRING = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value></value></param></params></methodResponse>";
    private static final String XML_EMPTY_ARRAY = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value><array><data></data></array></value></param></params></methodResponse>";
    private static final String XML_EMPTY_EVENT_LIST = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value><array><data><value>event</value></data></array></value></param></params></methodResponse>";

    private XmlRpcHTTPD xmlRpcHTTPD;
    private RpcEventListener listener;
    private HomematicConfig config;

    public XmlRpcServer(RpcEventListener listener, HomematicConfig config) {
        this.listener = listener;
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws IOException {
        logger.debug("Initializing XML-RPC server at port {}", config.getCallbackPort());

        xmlRpcHTTPD = new XmlRpcHTTPD(config.getCallbackPort());
        xmlRpcHTTPD.start(0, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (xmlRpcHTTPD != null) {
            logger.debug("Stopping XML-RPC server");
            xmlRpcHTTPD.stop();
        }
    }

    /**
     * A XML-RPC server based on a tiny HTTP server.
     *
     * @author Gerhard Riegler
     * @since 1.9.0
     */
    private class XmlRpcHTTPD extends NanoHTTPD {

        public XmlRpcHTTPD(int port) {
            super(port);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Response serve(IHTTPSession session) {
            try {
                Map<String, String> request = new HashMap<String, String>();
                session.parseBody(request);

                if (TRACE_ENABLED) {
                    logger.trace("Server original XmlRpcMessage:\n{}", request.get("postData"));
                }

                XmlRpcResponse response = new XmlRpcResponse(
                        new ByteArrayInputStream(request.get("postData").getBytes("ISO-8859-1")));
                if (TRACE_ENABLED) {
                    logger.trace("Server parsed XmlRpcMessage:\n{}", response);
                }
                String returnValue = handleMethodCall(response.getMethodName(), response.getResponseData());
                if (TRACE_ENABLED) {
                    logger.trace("Server XmlRpcResponse:\n{}", returnValue);
                }
                return newFixedLengthResponse(returnValue);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                return newFixedLengthResponse(XML_EMPTY_STRING);
            }
        }

        /**
         * Returns a valid result of the method called by the Homematic gateway.
         */
        private String handleMethodCall(String methodName, Object[] responseData) throws IOException {
            if (RPC_METHODNAME_EVENT.equals(methodName)) {
                return handleEvent(responseData);
            } else if (RPC_METHODNAME_LIST_DEVICES.equals(methodName)
                    || RPC_METHODNAME_UPDATE_DEVICE.equals(methodName)) {
                return XML_EMPTY_ARRAY;
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
                return XML_EMPTY_EVENT_LIST;
            } else {
                logger.warn("Unknown method called by Homematic gateway: " + methodName);
                return XML_EMPTY_EVENT_LIST;
            }
        }

        /**
         * Creates a XMLRPC message with the supported method names.
         */
        private String handleListMethods() {
            XmlRpcRequest msg = new XmlRpcRequest(null, XmlRpcRequest.TYPE.RESPONSE);
            List<String> events = new ArrayList<String>();
            events.add(RPC_METHODNAME_SYSTEM_MULTICALL);
            events.add(RPC_METHODNAME_EVENT);
            events.add(RPC_METHODNAME_DELETE_DEVICES);
            events.add(RPC_METHODNAME_NEW_DEVICES);
            msg.addArg(events);
            return msg.toString();
        }

        /**
         * Populates the extracted event to the listener.
         */
        @SuppressWarnings("finally")
        private String handleEvent(Object[] message) throws IOException {
            try {
                EventParser eventParser = new EventParser();
                HmDatapointInfo dpInfo = eventParser.parse(message);
                listener.eventReceived(dpInfo, eventParser.getValue());
            } finally {
                return XML_EMPTY_STRING;
            }
        }

        /**
         * Calls the listener when a devices has been detected.
         */
        @SuppressWarnings("finally")
        private String handleNewDevice(Object[] message) throws IOException {
            try {
                NewDevicesParser ndParser = new NewDevicesParser();
                List<String> adresses = ndParser.parse(message);
                listener.newDevices(adresses);
            } finally {
                return XML_EMPTY_ARRAY;
            }
        }

        /**
         * Calls the listener when devices has been deleted.
         */
        @SuppressWarnings("finally")
        private String handleDeleteDevice(Object[] message) throws IOException {
            try {
                DeleteDevicesParser ddParser = new DeleteDevicesParser();
                List<String> adresses = ddParser.parse(message);
                listener.deleteDevices(adresses);
            } finally {
                return XML_EMPTY_ARRAY;
            }
        }

    }

}
