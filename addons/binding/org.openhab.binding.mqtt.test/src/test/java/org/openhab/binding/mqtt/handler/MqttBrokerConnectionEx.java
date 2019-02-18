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
package org.openhab.binding.mqtt.handler;

import static org.mockito.Mockito.spy;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;

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

    public MqttBrokerConnectionEx(String host, @Nullable Integer port, boolean secure, @Nullable String clientId) {
        super(host, port, secure, clientId);
    }

    public void setConnectionCallback(MqttBrokerConnectionEx o) {
        connectionCallback = spy(new ConnectionCallback(o));
    }

    @Override
    protected MqttAsyncClient createClient(String serverURI, String clientId, MqttClientPersistence dataStore)
            throws org.eclipse.paho.client.mqttv3.MqttException {
        return spy(new MqttAsyncClientEx(serverURI, clientId, dataStore, this));
    }

    @Override
    public @NonNull MqttConnectionState connectionState() {
        return connectionStateOverwrite;
    }
}
