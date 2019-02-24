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
package org.openhab.binding.nadreceiver.internal;

/**
 * The {@link NadReceiverConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Marc Chételat - Initial contribution
 */
public class NadReceiverConfiguration {

    public static final String HOST_NAME = "hostname";
    public static final String PORT = "port";
    public static final String HEART_BEAT_INTERVAL = "heartbeatInterval";
    public static final String HEART_RECONNECT_INTERVAL = "reconnectInterval";
    public static final String MAX_SOURCES = "maxSources";

    public String hostname;
    public int port;
    public int heartbeatInterval;
    public int reconnectInterval;
    public int maxSources;
}
