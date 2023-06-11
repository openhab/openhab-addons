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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnectionConfig;

/**
 * Holds the configuration of a {@link BrokerHandler} Thing. Parameters are inherited
 * from {@link MqttBrokerConnectionConfig}, Additionally some
 * reconnect and security related parameters are defined here.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class BrokerHandlerConfig extends MqttBrokerConnectionConfig {
    public @Nullable Integer reconnectTime;
    public @Nullable Integer timeoutInMs;

    // For more security, the following optional parameters can be altered

    public boolean certificatepin = false;
    public boolean publickeypin = false;
    public String certificate = "";
    public String publickey = "";

    public boolean enableDiscovery = true;

    // Birth message parameters
    public @Nullable String birthTopic;
    public @Nullable String birthMessage;
    public Boolean birthRetain = true;

    // Shutdown message parameters
    public @Nullable String shutdownTopic;
    public @Nullable String shutdownMessage;
    public Boolean shutdownRetain = true;
}
