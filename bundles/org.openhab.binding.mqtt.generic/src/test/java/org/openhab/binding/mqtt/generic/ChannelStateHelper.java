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
package org.openhab.binding.mqtt.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;

/**
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class ChannelStateHelper {
    public static void setConnection(ChannelState cs, MqttBrokerConnection connection) {
        cs.setConnection(connection);
    }
}
