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
package org.openhab.binding.bmwcardata.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bmwcardata.internal.stream.BrokerSettings;

/**
 * The {@link AccountConfiguration} class contains the configuration parameters of the account bridge.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class AccountConfiguration {

    /** The binding connects to the BMW streaming broker itself. */
    public static final String STREAM_MODE_DIRECT = "direct";
    /** The binding receives the stream from a local broker fed by the bmw-mqtt-bridge. */
    public static final String STREAM_MODE_BRIDGE = "bridge";

    public String clientId = "";
    public boolean streaming = true;
    public String streamMode = STREAM_MODE_DIRECT;
    /** Republishes the direct stream to the local broker in the topic format of the bmw-mqtt-bridge. */
    public boolean mirrorToBroker = false;
    /** Publishes the mirrored stream messages retained, like MQTT_RETAIN of the bridge, which is off by default. */
    public boolean mirrorRetain = false;

    /** Local MQTT broker, used in bridge mode and for mirroring the direct stream. */
    public String brokerHost = "";
    public int brokerPort = 1883;
    public boolean brokerSecure = false;
    public String brokerUsername = "";
    public String brokerPassword = "";
    /** Topic prefix configured as LOCAL_PREFIX in the bridge. */
    public String topicPrefix = BrokerSettings.DEFAULT_TOPIC_PREFIX;

    /** Hours between REST refreshes of all vehicles, 0 disables the periodic refresh. */
    public int refreshInterval = 6;
    /** Maximum number of REST requests in 24 hours, limited to BMW's limit by the request budget. */
    public int requestBudget = 40;

    public boolean isBridgeMode() {
        return STREAM_MODE_BRIDGE.equals(streamMode);
    }

    /**
     * @return whether the direct stream is mirrored to the local broker; mirroring is ignored in bridge mode, as it
     *         would republish what the bridge already published
     */
    public boolean isMirrorActive() {
        return streaming && mirrorToBroker && !isBridgeMode();
    }

    /**
     * @return whether the local broker is used, for the bridge stream or the mirror
     */
    public boolean usesBroker() {
        return streaming && (isBridgeMode() || mirrorToBroker);
    }

    public BrokerSettings brokerSettings() {
        return new BrokerSettings(brokerHost.strip(), brokerPort, brokerSecure, brokerUsername, brokerPassword,
                BrokerSettings.normalizeTopicPrefix(topicPrefix));
    }
}
