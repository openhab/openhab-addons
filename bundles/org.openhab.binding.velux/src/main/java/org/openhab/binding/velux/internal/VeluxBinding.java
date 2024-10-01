/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.velux.internal;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.config.VeluxBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * <B>Class for Velux binding which validates the bridge configuration parameters.</B>
 *
 * <ul>
 * <li>{@link #VeluxBinding constructor}</li>
 * <li>{@link #checked }</li>
 * </ul>
 *
 * @author Guenther Schreiner - Initial contribution
 * @author Joachim Sauer (@Saua) - fix for isBulkRetrievalEnabled, isSequentialEnforced
 */
@NonNullByDefault
public class VeluxBinding extends VeluxBridgeConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /***
     *** Startup methods
     ***/

    /**
     * Constructor
     *
     * initializes the interface towards the Velux bridge. Furthermore, the checked configuration can be retrieved by
     * the method {@link #checked checked}.
     *
     * @param uncheckedConfiguration
     *            The configuration of type {@link VeluxBridgeConfiguration}
     *            which shall be checked.
     */
    public VeluxBinding(@Nullable VeluxBridgeConfiguration uncheckedConfiguration) {
        logger.trace("VeluxBinding(constructor) called.");
        if (logger.isTraceEnabled()) {
            for (Field field : VeluxBridgeConfiguration.class.getFields()) {
                String fName = field.getName();
                if ((fName.length() > 0) && Character.isUpperCase(fName.charAt(0))) {
                    logger.trace("VeluxBinding(): FYI: a potential configuration string is '{}'.", fName);
                }
            }
        }
        if (uncheckedConfiguration == null) {
            logger.debug("No configuration found, using default values.");
        } else {
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_PROTOCOL);
            if (!uncheckedConfiguration.protocol.isBlank()) {
                this.protocol = uncheckedConfiguration.protocol;
            }
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_IPADDRESS);
            if (!uncheckedConfiguration.ipAddress.isBlank()) {
                this.ipAddress = uncheckedConfiguration.ipAddress;
            }
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_TCPPORT);
            if ((uncheckedConfiguration.tcpPort > 0) && (uncheckedConfiguration.tcpPort <= 65535)) {
                this.tcpPort = uncheckedConfiguration.tcpPort;
            }
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_PASSWORD);
            if (!uncheckedConfiguration.password.isBlank()) {
                this.password = uncheckedConfiguration.password;
            }
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_TIMEOUT_MSECS);
            if ((uncheckedConfiguration.timeoutMsecs >= 500) && (uncheckedConfiguration.timeoutMsecs <= 5000)) {
                this.timeoutMsecs = uncheckedConfiguration.timeoutMsecs;
            }
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_RETRIES);
            if ((uncheckedConfiguration.retries >= 0) && (uncheckedConfiguration.retries <= 10)) {
                this.retries = uncheckedConfiguration.retries;
            }
            logger.trace("VeluxBinding(): checking {}.", VeluxBridgeConfiguration.BRIDGE_REFRESH_MSECS);
            if ((uncheckedConfiguration.refreshMSecs >= 1000) && (uncheckedConfiguration.refreshMSecs <= 60000)) {
                this.refreshMSecs = uncheckedConfiguration.refreshMSecs;
            }
            this.isBulkRetrievalEnabled = uncheckedConfiguration.isBulkRetrievalEnabled;
            this.isSequentialEnforced = uncheckedConfiguration.isSequentialEnforced;
            this.isProtocolTraceEnabled = uncheckedConfiguration.isProtocolTraceEnabled;
        }
        logger.trace("VeluxBinding(constructor) done.");
    }

    /**
     * Access method returning a validated configuration.
     *
     * @return bridgeConfiguration of type {@link VeluxBridgeConfiguration
     *         VeluxBridgeConfiguration}.
     */
    public VeluxBridgeConfiguration checked() {
        logger.trace("checked() called.");
        // @formatter:off
        logger.debug("{}Config[{}={},{}={},{}={},{}={},{}={},{}={},{}={},{}={},{}={},{}={}]",
                VeluxBindingConstants.BINDING_ID,
                VeluxBridgeConfiguration.BRIDGE_PROTOCOL, protocol,
                VeluxBridgeConfiguration.BRIDGE_IPADDRESS, this.ipAddress,
                VeluxBridgeConfiguration.BRIDGE_TCPPORT, tcpPort,
                VeluxBridgeConfiguration.BRIDGE_PASSWORD, password.replaceAll(".", "*"),
                VeluxBridgeConfiguration.BRIDGE_TIMEOUT_MSECS, timeoutMsecs,
                VeluxBridgeConfiguration.BRIDGE_RETRIES, retries,
                VeluxBridgeConfiguration.BRIDGE_REFRESH_MSECS, refreshMSecs,
                VeluxBridgeConfiguration.BRIDGE_IS_BULK_RETRIEVAL_ENABLED, isBulkRetrievalEnabled,
                VeluxBridgeConfiguration.BRIDGE_IS_SEQUENTIAL_ENFORCED, isSequentialEnforced,
                VeluxBridgeConfiguration.BRIDGE_PROTOCOL_TRACE_ENABLED, isProtocolTraceEnabled);
        // @formatter:off
        logger.trace("checked() done.");
        return this;
    }
}
