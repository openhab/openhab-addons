/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Callback;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcRequest;
import org.openhab.binding.homematic.internal.communicator.message.XmlRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Reads a XML-RPC message and handles the method call.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class XmlRpcServer implements RpcServer {
    private final Logger logger = LoggerFactory.getLogger(XmlRpcServer.class);

    private static final String XML_EMPTY_STRING = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value></value></param></params></methodResponse>";
    private static final String XML_EMPTY_ARRAY = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value><array><data></data></array></value></param></params></methodResponse>";
    private static final String XML_EMPTY_EVENT_LIST = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<methodResponse><params><param><value><array><data><value>event</value></data></array></value></param></params></methodResponse>";

    private @Nullable Server xmlRpcHTTPD;
    private HomematicConfig config;
    private RpcResponseHandler<String> rpcResponseHander;
    private final ResponseHandler jettyResponseHandler = new ResponseHandler();

    public XmlRpcServer(RpcEventListener listener, HomematicConfig config) {
        this.config = config;
        this.rpcResponseHander = new RpcResponseHandler<>(listener) {

            @Override
            protected String getEmptyStringResult() {
                return XML_EMPTY_STRING;
            }

            @Override
            protected String getEmptyEventListResult() {
                return XML_EMPTY_EVENT_LIST;
            }

            @Override
            protected String getEmptyArrayResult() {
                return XML_EMPTY_ARRAY;
            }

            @Override
            protected RpcRequest<String> createRpcRequest() {
                return new XmlRpcRequest(null, XmlRpcRequest.TYPE.RESPONSE);
            }
        };
    }

    @Override
    public void start() throws IOException {
        logger.debug("Initializing XML-RPC server at port {}", config.getXmlCallbackPort());

        InetSocketAddress callbackAddress = new InetSocketAddress(config.getXmlCallbackPort());
        Server xmlRpcHTTPD = new Server(callbackAddress);
        xmlRpcHTTPD.setHandler(jettyResponseHandler);

        this.xmlRpcHTTPD = xmlRpcHTTPD;

        try {
            xmlRpcHTTPD.start();
            if (logger.isTraceEnabled()) {
                xmlRpcHTTPD.dumpStdErr();
            }
        } catch (Exception e) {
            throw new IOException("Jetty start failed", e);
        }
    }

    @Override
    public void shutdown() {
        Server xmlRpcHTTPD = this.xmlRpcHTTPD;
        if (xmlRpcHTTPD != null) {
            logger.debug("Stopping XML-RPC server");
            try {
                xmlRpcHTTPD.stop();
            } catch (Exception ex) {
                logger.error("{}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Response handler for Jetty implementing a XML-RPC server
     * This class extends the Jetty AbstractHandler that is not annotated with @NonNullByDefault, so we have to disable
     * null annotations for this class.
     *
     * @author Martin Herbst
     */
    @NonNullByDefault({})
    private class ResponseHandler extends Handler.Abstract.NonBlocking {
        @Override
        public boolean handle(Request request, Response response, Callback callback) {
            response.setStatus(200);
            response.getHeaders().put("Content-Type", "text/xml;charset=ISO-8859-1");
            String returnValue;
            try {
                XmlRpcResponse xmlResponse = new XmlRpcResponse(Request.asInputStream(request), config.getEncoding());
                if (logger.isTraceEnabled()) {
                    logger.trace("Server parsed XmlRpcMessage:\n{}", xmlResponse);
                }
                returnValue = rpcResponseHander.handleMethodCall(xmlResponse.getMethodName(),
                        xmlResponse.getResponseData());
                if (logger.isTraceEnabled()) {
                    logger.trace("Server XmlRpcResponse:\n{}", returnValue);
                }
            } catch (SAXException | ParserConfigurationException | IOException ex) {
                logger.error("{}", ex.getMessage(), ex);
                returnValue = XML_EMPTY_STRING;
            }
            Content.Sink.write(response, true, returnValue + System.lineSeparator(), callback);
            return true;
        }
    }
}
