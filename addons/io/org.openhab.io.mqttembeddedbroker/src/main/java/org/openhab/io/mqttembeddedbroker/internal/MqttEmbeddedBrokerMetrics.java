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
package org.openhab.io.mqttembeddedbroker.internal;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptAcknowledgedMessage;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.server.Server;

/**
 * Informs the given listener about connected clients and maybe other
 * server metrics in the future. You need to set the server with {@link #setServer(Server)}.
 *
 * Right now this is an adapter interface for Moquettes InterceptHandler.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttEmbeddedBrokerMetrics implements InterceptHandler {
    /**
     * Metric listener interface. Implement this to get notified of currently connected clients.
     */
    public interface BrokerMetricsListener {
        void connectedClientIDs(Collection<String> clientIDs);
    }

    private final BrokerMetricsListener listener;
    private @Nullable Server server;

    public MqttEmbeddedBrokerMetrics(BrokerMetricsListener listener) {
        this.listener = listener;
    }

    /**
     * Removes the intercept handler from the server, if a server was set with {@link #setServer(Server)} before.
     */
    public void resetServer() {
        if (this.server != null) {
            this.server.removeInterceptHandler(this);
        }
        this.server = null;
    }

    /**
     * Set the Moquette server.
     *
     * @param server Moquette server
     */
    public void setServer(Server server) {
        if (this.server != null) {
            this.server.removeInterceptHandler(this);
        }
        this.server = server;
        server.addInterceptHandler(this);
    }

    @Override
    public void onUnsubscribe(@Nullable InterceptUnsubscribeMessage msg) {
    }

    @Override
    public void onSubscribe(@Nullable InterceptSubscribeMessage msg) {
    }

    @Override
    public void onPublish(@Nullable InterceptPublishMessage msg) {
    }

    @Override
    public void onMessageAcknowledged(@Nullable InterceptAcknowledgedMessage msg) {
    }

    @Override
    public void onDisconnect(@Nullable InterceptDisconnectMessage msg) {
        Server server = this.server;
        if (server != null) {
            listener.connectedClientIDs(server.getConnectionsManager().getConnectedClientIds());
        }
    }

    @Override
    public void onConnectionLost(@Nullable InterceptConnectionLostMessage msg) {
        Server server = this.server;
        if (server != null) {
            listener.connectedClientIDs(server.getConnectionsManager().getConnectedClientIds());
        }
    }

    @Override
    public void onConnect(@Nullable InterceptConnectMessage msg) {
        Server server = this.server;
        if (server != null) {
            listener.connectedClientIDs(server.getConnectionsManager().getConnectedClientIds());
        }
    }

    @Override
    public Class<?>[] getInterceptedMessageTypes() {
        return new Class<?>[] { InterceptConnectMessage.class, InterceptConnectionLostMessage.class,
                InterceptDisconnectMessage.class };
    }

    @Override
    public String getID() {
        return "collectmetrics";
    }
}
