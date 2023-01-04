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
package org.openhab.binding.shelly.internal.api1;

import static org.openhab.binding.shelly.internal.api1.Shelly1CoapJSonDTO.COIOT_PORT;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.elements.UdpMulticastConnector;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Shelly1CoapServer} implements the UDP listener and status event processor (for /cit/s messages)
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly1CoapServer {
    private final Logger logger = LoggerFactory.getLogger(Shelly1CoapServer.class);

    boolean started = false;
    private CoapEndpoint statusEndpoint = new CoapEndpoint.Builder().build();
    private @Nullable UdpMulticastConnector statusConnector;
    private CoapServer server = new CoapServer(NetworkConfig.getStandard(), COIOT_PORT);
    private final Set<Shelly1CoapListener> coapListeners = ConcurrentHashMap.newKeySet();

    protected class ShellyStatusListener extends CoapResource {
        private Shelly1CoapServer listener;

        public ShellyStatusListener(String uri, Shelly1CoapServer listener) {
            super(uri, true);
            getAttributes().setTitle("ShellyCoapListener");
            this.listener = listener;
        }

        @Override
        public void handleRequest(@Nullable final Exchange exchange) {
            if (exchange != null) {
                Request request = exchange.getRequest();
                Code code = exchange.getRequest().getCode();
                switch (code) {
                    case CUSTOM_30:
                    case PUT: // Shelly Motion beta: incorrect, but handle the format
                        listener.processResponse(createResponse(request));
                        break;
                    default:
                        super.handleRequest(exchange);
                }
            }
        }
    }

    public synchronized void start(String localIp, int port, Shelly1CoapListener listener)
            throws UnknownHostException, SocketException {
        if (!started) {
            logger.debug("Initializing CoIoT listener (local IP={}:{})", localIp, port);
            NetworkConfig nc = NetworkConfig.getStandard();
            InetAddress localAddr = InetAddress.getByName(localIp);
            // Join the multicast group on the selected network interface, add UDP listener
            statusConnector = new UdpMulticastConnector.Builder().setLocalAddress(localAddr, port).setLocalPort(port)
                    .setOutgoingMulticastInterface(localAddr).addMulticastGroup(CoAP.MULTICAST_IPV4).build();
            statusEndpoint = new CoapEndpoint.Builder().setNetworkConfig(nc).setConnector(statusConnector).build();
            server = new CoapServer(NetworkConfig.getStandard(), port);
            server.addEndpoint(statusEndpoint);
            CoapResource cit = new ShellyStatusListener("cit", this);
            CoapResource s = new ShellyStatusListener("s", this);
            cit.add(s);
            server.add(cit);
            started = true;
        }

        if (!coapListeners.contains(listener)) {
            coapListeners.add(listener);
        }
    }

    protected void processResponse(Response response) {
        coapListeners.forEach(listener -> listener.processResponse(response));
    }

    public static Response createResponse(Request request) {
        Response response = Response.createResponse(request, ResponseCode.CONTENT);
        response.setType(request.getType());
        response.setSourceContext(request.getSourceContext());
        response.setMID(request.getMID());
        response.setOptions(request.getOptions());
        response.setPayload(request.getPayload());
        return response;
    }

    @Nullable
    public CoapEndpoint getEndpoint() {
        return statusEndpoint;
    }

    /**
     * Cancel pending requests and shutdown the client
     */
    public void stop(Shelly1CoapListener listener) {
        coapListeners.remove(listener);
        if (coapListeners.isEmpty()) {
            stop();
        }
    }

    private synchronized void stop() {
        if (started) {
            // Last listener
            server.stop();
            statusEndpoint.stop();
            coapListeners.clear();
            started = false;
            logger.debug("CoAP Listener stopped");
        }
    }

    public void dispose() {
        stop();
    }
}
