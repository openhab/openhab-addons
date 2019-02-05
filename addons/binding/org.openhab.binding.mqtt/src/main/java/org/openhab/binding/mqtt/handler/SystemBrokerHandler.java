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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.io.transport.mqtt.MqttServiceObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttWillAndTestament;
import org.eclipse.smarthome.io.transport.mqtt.reconnect.PeriodicReconnectStrategy;

/**
 * This handler does not much except providing all information from a
 * {@link MqttBrokerConnection} via Thing properties and put the Thing
 * offline or online depending on the connection.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SystemBrokerHandler extends AbstractBrokerHandler implements MqttServiceObserver {
    // Properties
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_QOS = "qos";
    public static final String PROPERTY_RETAIN = "retain";
    public static final String PROPERTY_LAST_WILL = "lastwill";
    public static final String PROPERTY_RECONNECT_TIME = "reconnect_time_ms";
    public static final String PROPERTY_KEEP_ALIVE_TIME = "keep_alive_time_ms";
    public static final String PROPERTY_CONNECT_TIMEOUT = "connect_timeout_ms";

    protected final MqttService service;

    protected String brokerID = "";

    public SystemBrokerHandler(Bridge thing, MqttService service) {
        super(thing);
        this.service = service;
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_URL, connection.getHost() + ":" + String.valueOf(connection.getPort()));
        final String username = connection.getUser();
        final String password = connection.getPassword();
        if (username != null && password != null) {
            properties.put(PROPERTY_USERNAME, username);
            properties.put(PROPERTY_PASSWORD, password);
        }
        properties.put(PROPERTY_QOS, String.valueOf(connection.getQos()));
        properties.put(PROPERTY_RETAIN, String.valueOf(connection.isRetain()));
        final MqttWillAndTestament lastWill = connection.getLastWill();
        if (lastWill != null) {
            properties.put(PROPERTY_LAST_WILL, lastWill.toString());
        } else {
            properties.put(PROPERTY_LAST_WILL, "");
        }
        if (connection.getReconnectStrategy() instanceof PeriodicReconnectStrategy) {
            final PeriodicReconnectStrategy strategy = (PeriodicReconnectStrategy) connection.getReconnectStrategy();
            if (strategy != null) {
                properties.put(PROPERTY_RECONNECT_TIME, String.valueOf(strategy.getReconnectFrequency()));
            }
        }
        properties.put(PROPERTY_KEEP_ALIVE_TIME, String.valueOf(connection.getKeepAliveInterval()));

        updateProperties(properties);
        super.connectionStateChanged(state, error);
    }

    /**
     * The base implementation will set the connection variable to the given broker
     * if it matches the brokerID and will start to connect to the broker if there
     * is no connection established yet.
     */
    @Override
    public void brokerAdded(String connectionName, MqttBrokerConnection addedConnection) {
        if (!connectionName.equals(brokerID) || connection == addedConnection) {
            return;
        }

        this.connection = addedConnection;
        super.initialize();
    }

    @Override
    public void brokerRemoved(String connectionName, MqttBrokerConnection removedConnection) {
        final MqttBrokerConnection connection = this.connection;
        if (removedConnection == connection) {
            connection.removeConnectionObserver(this);
            this.connection = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.sharedremoved");
            return;
        }
    }

    @Override
    public void initialize() {
        this.brokerID = getThing().getConfiguration().get("brokerid").toString();
        service.addBrokersListener(this);

        connection = service.getBrokerConnection(brokerID);
        if (connection == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.notextualconfig [\"" + brokerID + "\"");
            return;
        }
        super.initialize();
    }

    @Override
    public void dispose() {
        service.removeBrokersListener(this);
        super.dispose();
    }
}
