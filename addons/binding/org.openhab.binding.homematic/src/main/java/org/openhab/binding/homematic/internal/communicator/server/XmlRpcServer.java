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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcResponse;
import org.openhab.binding.homematic.internal.communicator.parser.DeleteDevicesParser;
import org.openhab.binding.homematic.internal.communicator.parser.EventParser;
import org.openhab.binding.homematic.internal.communicator.parser.NewDevicesParser;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Reads a XML-RPC message and handles the method call.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class XmlRpcServer implements RpcServer {
    private final static Logger logger = LoggerFactory.getLogger(XmlRpcServer.class);

    private static final String XML_EMPTY_STRING = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value></value></param></params></methodResponse>";
    private static final String XML_EMPTY_ARRAY = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value><array><data></data></array></value></param></params></methodResponse>";
    private static final String XML_EMPTY_EVENT_LIST = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value><array><data><value>event</value></data></array></value></param></params></methodResponse>";

    private Server xmlRpcHTTPD;
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
        logger.debug("Initializing XML-RPC server at port {}", config.getXmlCallbackPort());

        xmlRpcHTTPD = new Server(config.getXmlCallbackPort());
        xmlRpcHTTPD.setHandler(new ResponseHandler());

        try {
            xmlRpcHTTPD.start();
            if (logger.isTraceEnabled()) {
                xmlRpcHTTPD.dumpStdErr();
            }
        } catch (Exception e) {
            throw new IOException("Jetty start failed", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        if (xmlRpcHTTPD != null) {
            logger.debug("Stopping XML-RPC server");
            try {
                xmlRpcHTTPD.stop();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Response handler for Jetty implementing a XML-RPC server
     *
     * @author Martin Herbst
     */
    private class ResponseHandler extends AbstractHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            response.setContentType("text/xml;charset=ISO-8859-1");
            response.setStatus(HttpServletResponse.SC_OK);
            final PrintWriter respWriter = response.getWriter();
            try {
                XmlRpcResponse xmlResponse = new XmlRpcResponse(request.getInputStream(), config.getEncoding());
                if (logger.isTraceEnabled()) {
                    logger.trace("Server parsed XmlRpcMessage:\n{}", xmlResponse);
                }
                final String returnValue = handleMethodCall(xmlResponse.getMethodName(), xmlResponse.getResponseData());
                if (logger.isTraceEnabled()) {
                    logger.trace("Server XmlRpcResponse:\n{}", returnValue);
                }
                respWriter.println(returnValue);
            } catch (SAXException | ParserConfigurationException ex) {
                logger.error(ex.getMessage(), ex);
                respWriter.println(XML_EMPTY_STRING);
            }
            baseRequest.setHandled(true);
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
            } else if (RPC_METHODNAME_SET_CONFIG_READY.equals(methodName)) {
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
