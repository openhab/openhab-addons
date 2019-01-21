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

import static org.mockito.Mockito.*;

import java.util.Collections;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;

/**
 * We need an extended MqttAsyncClientEx to overwrite the connection state.
 *
 * In respect to the success flags the operations publish, subscribe, unsubscribe, connect,
 * and disconnect immediately succeed or fail.
 *
 * @author David Graeff - Initial contribution
 */
public class MqttAsyncClientEx extends MqttAsyncClient {
    public MqttBrokerConnectionEx connection;

    public MqttAsyncClientEx(String serverURI, String clientId, MqttClientPersistence dataStore,
            MqttBrokerConnectionEx connection) throws MqttException {
        super(serverURI, clientId, dataStore);
        this.connection = connection;
    }

    IMqttToken getToken(Object userContext, IMqttActionListener callback, String topic) {
        IMqttToken t = mock(IMqttToken.class);
        doReturn(userContext).when(t).getUserContext();
        doReturn(true).when(t).isComplete();
        doReturn(Collections.singletonList(topic).toArray(new String[1])).when(t).getTopics();
        doReturn(MqttAsyncClientEx.this).when(t).getClient();
        doReturn(callback).when(t).getActionCallback();
        doReturn(null).when(t).getException();
        return t;
    }

    IMqttDeliveryToken getDeliveryToken(Object userContext, IMqttActionListener callback, String topic) {
        IMqttDeliveryToken t = mock(IMqttDeliveryToken.class);
        doReturn(userContext).when(t).getUserContext();
        doReturn(true).when(t).isComplete();
        doReturn(Collections.singletonList(topic).toArray(new String[1])).when(t).getTopics();
        doReturn(MqttAsyncClientEx.this).when(t).getClient();
        doReturn(callback).when(t).getActionCallback();
        doReturn(null).when(t).getException();
        return t;
    }

    @Override
    public boolean isConnected() {
        return connection.connectionStateOverwrite == MqttConnectionState.CONNECTED;
    }

    @Override
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos, boolean retained, Object userContext,
            IMqttActionListener callback) throws MqttException, MqttPersistenceException {

        if (connection.publishSuccess) {
            callback.onSuccess(getToken(userContext, callback, topic));
        } else {
            callback.onFailure(getToken(userContext, callback, topic), new MqttException(0));
        }
        return getDeliveryToken(userContext, callback, topic);
    }

    @Override
    public IMqttToken subscribe(String topic, int qos, Object userContext, IMqttActionListener callback)
            throws MqttException {
        if (connection.publishSuccess) {
            callback.onSuccess(getToken(userContext, callback, topic));
        } else {
            callback.onFailure(getToken(userContext, callback, topic), new MqttException(0));
        }
        return getToken(userContext, callback, topic);
    }

    @Override
    public IMqttToken unsubscribe(String topic, Object userContext, IMqttActionListener callback) throws MqttException {
        if (connection.unsubscribeSuccess) {
            callback.onSuccess(getToken(userContext, callback, topic));
        } else {
            callback.onFailure(getToken(userContext, callback, topic), new MqttException(0));
        }
        return getToken(userContext, callback, topic);
    }

    @Override
    public IMqttToken disconnect(long quiesceTimeout, Object userContext, IMqttActionListener callback)
            throws MqttException {
        connection.connectionStateOverwrite = MqttConnectionState.DISCONNECTED;
        if (connection.disconnectSuccess) {
            callback.onSuccess(getToken(userContext, callback, null));
        } else {
            callback.onFailure(getToken(userContext, callback, null), new MqttException(0));
        }
        return getToken(userContext, callback, null);
    }

    @Override
    public IMqttToken connect(MqttConnectOptions options, Object userContext, IMqttActionListener callback)
            throws MqttException, MqttSecurityException {
        if (!connection.connectTimeout) {
            connection.connectionStateOverwrite = MqttConnectionState.CONNECTED;
            if (connection.connectSuccess) {
                callback.onSuccess(getToken(userContext, callback, null));
            } else {
                callback.onFailure(getToken(userContext, callback, null), new MqttException(0));
            }
        } else {
            connection.connectionStateOverwrite = MqttConnectionState.DISCONNECTED;
        }
        return getToken(userContext, callback, null);
    }
}
