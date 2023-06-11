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
package org.openhab.binding.velux.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VeluxBridgeConfiguration} is a wrapper for
 * configuration settings needed to access the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider}
 * device.
 * <p>
 * It contains the factory default values as well.
 * <ul>
 * <li>{@link VeluxBridgeConfiguration#protocol protocol} protocol type
 * (one of http or https or slip),</li>
 * <li>{@link VeluxBridgeConfiguration#ipAddress ipAddress} bridge IP address,</li>
 * <li>{@link VeluxBridgeConfiguration#tcpPort tcpPort} bridge TCP port,</li>
 * <li>{@link VeluxBridgeConfiguration#password password} bridge password,</li>
 * <li>{@link VeluxBridgeConfiguration#timeoutMsecs timeoutMsecs} communication timeout in milliseconds,</li>
 * <li>{@link VeluxBridgeConfiguration#retries retries} number of retries (with exponential backoff algorithm),</li>
 * <li>{@link VeluxBridgeConfiguration#refreshMSecs refreshMSecs} refreshMSecs interval for retrieval of bridge
 * information.</li>
 * <li>{@link VeluxBridgeConfiguration#isBulkRetrievalEnabled isBulkRetrievalEnabled} flag to use bulk product</LI>
 * <li>{@link VeluxBridgeConfiguration#isSequentialEnforced isSequentialEnforced} flag to enforce sequential control on
 * actuators.</LI>
 * <li>{@link VeluxBridgeConfiguration#isProtocolTraceEnabled isProtocolTraceEnabled} flag to enable protocol logging
 * (via loglevel INFO).</li>
 * </ul>
 * <p>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBridgeConfiguration {
    public static final String BRIDGE_PROTOCOL = "protocol";
    public static final String BRIDGE_IPADDRESS = "ipAddress";
    public static final String BRIDGE_TCPPORT = "tcpPort";
    public static final String BRIDGE_PASSWORD = "password";
    public static final String BRIDGE_TIMEOUT_MSECS = "timeoutMsecs";
    public static final String BRIDGE_RETRIES = "retries";
    public static final String BRIDGE_REFRESH_MSECS = "refreshMsecs";
    public static final String BRIDGE_IS_BULK_RETRIEVAL_ENABLED = "isBulkRetrievalEnabled";
    public static final String BRIDGE_IS_SEQUENTIAL_ENFORCED = "isSequentialEnforced";
    public static final String BRIDGE_PROTOCOL_TRACE_ENABLED = "isProtocolTraceEnabled";

    /*
     * Value to flag any changes towards the getter.
     */
    public boolean hasChanged = true;

    /*
     * Default values - should not be modified
     */
    public String protocol = "slip";
    public String ipAddress = "192.168.1.1";
    public int tcpPort = 51200;
    public String password = "velux123";
    public int timeoutMsecs = 1000; // one second
    public int retries = 5;
    public long refreshMSecs = 10000L; // 10 seconds
    public boolean isBulkRetrievalEnabled = true;
    public boolean isSequentialEnforced = false;
    public boolean isProtocolTraceEnabled = false;
}
