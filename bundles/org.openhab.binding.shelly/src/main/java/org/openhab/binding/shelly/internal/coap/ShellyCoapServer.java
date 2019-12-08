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
package org.openhab.binding.shelly.internal.coap;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.COIOT_PORT;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.lang.Validate;
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
 * The {@link ShellyCoapServer} implements the UDP listener and status event processor (for /cit/s messages)
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyCoapServer {
    private final Logger logger = LoggerFactory.getLogger(ShellyCoapServer.class);

    private @Nullable CoapEndpoint statusEndpoint;
    private @Nullable UdpMulticastConnector statusConnector;
    private @Nullable CoapServer server;
    boolean started = false;
    private final Set<ShellyCoapListener> coapListeners = new CopyOnWriteArraySet<>();

    @SuppressWarnings("null")
    @NonNullByDefault
    protected class ShellyStatusListener extends CoapResource {

        private ShellyCoapServer listener;

        public ShellyStatusListener(String uri, ShellyCoapServer listener) {
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
                        listener.processResponse(createResponse(request));
                        break;
                    default:
                        super.handleRequest(exchange);
                }
            }
        }

    }

    public void addListener(ShellyCoapListener listener) {
        coapListeners.add(listener);
    }

    public void removeListener(ShellyCoapListener listener) {
        coapListeners.remove(listener);
    }

    @SuppressWarnings("null")
    synchronized void init(String localIp) throws UnknownHostException {
        if (server == null) {
            logger.debug("Initializing CoIoT listener (local IP={}", localIp);
            NetworkConfig nc = NetworkConfig.getStandard();
            InetAddress localAddr = InetAddress.getByName(localIp);
            InetSocketAddress localPort = new InetSocketAddress(COIOT_PORT);

            // Join the Multicast group on the selected network interface
            statusConnector = new UdpMulticastConnector(localAddr, localPort, CoAP.MULTICAST_IPV4); // bind UDP listener
            statusEndpoint = new CoapEndpoint.Builder().setNetworkConfig(nc).setConnector(statusConnector).build();
            // statusEndpoint.addInterceptor(this);

            server = new CoapServer(NetworkConfig.getStandard(), COIOT_PORT);
            Validate.notNull(server);
            server.addEndpoint(statusEndpoint);
            CoapResource cit = new ShellyStatusListener("cit", this);
            CoapResource s = new ShellyStatusListener("s", this);
            cit.add(s);
            server.add(cit);
        }
    }

    public void start() {
        if (!started) {
            logger.debug("Start CoIoT Listener");
            started = true;
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
    public void stop() {
        if (started) {
            if (server != null) {
                server.stop();
            }
            if (statusEndpoint != null) {
                statusEndpoint.stop();
            }
        }
    }

    public void dispose() {
        stop();
    }

}
