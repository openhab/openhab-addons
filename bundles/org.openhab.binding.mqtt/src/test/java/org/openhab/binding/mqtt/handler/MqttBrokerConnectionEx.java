/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.handler;

import static org.mockito.Mockito.spy;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.internal.Subscription;
import org.openhab.core.io.transport.mqtt.internal.client.MqttAsyncClientWrapper;

/**
 * We need an extended MqttBrokerConnection to overwrite the protected `connectionCallbacks` with
 * an instance that takes the mocked version of `MqttBrokerConnection` and overwrite the connection state.
 *
 * We also replace the internal MqttAsyncClient with a spied one, that in respect to the success flags
 * immediately succeed or fail with publish, subscribe, unsubscribe, connect, disconnect.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class MqttBrokerConnectionEx extends MqttBrokerConnection {
    public MqttConnectionState connectionStateOverwrite = MqttConnectionState.DISCONNECTED;
    public boolean publishSuccess = true;
    public boolean subscribeSuccess = true;
    public boolean unsubscribeSuccess = true;
    public boolean disconnectSuccess = true;
    public boolean connectSuccess = true;
    public boolean connectTimeout = false;

    public MqttBrokerConnectionEx(String host, @Nullable Integer port, boolean secure, String clientId) {
        super(host, port, secure, clientId);
    }

    public void setConnectionCallback(MqttBrokerConnectionEx o) {
        connectionCallback = spy(new ConnectionCallback(o));
    }

    public Map<String, Subscription> getSubscribers() {
        return subscribers;
    }

    public ConnectionCallback getCallback() {
        return connectionCallback;
    }

    @Override
    protected MqttAsyncClientWrapper createClient() {
        return new MqttAsyncClientWrapperEx(this);
    }

    @Override
    public MqttConnectionState connectionState() {
        return connectionStateOverwrite;
    }
}
