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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.THING_TYPE_INTRS;
import static org.openhab.binding.satel.internal.config.IntRSConfig.PORT;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.config.IntRSConfig;
import org.openhab.binding.satel.internal.protocol.IntRSModule;
import org.openhab.binding.satel.internal.protocol.SatelModule;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IntRSBridgeHandler} is a bridge handler for INT-RS communication module.
 * All {@link SatelThingHandler}s use it to receive events and execute commands.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntRSBridgeHandler extends SatelBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_INTRS);

    private final Logger logger = LoggerFactory.getLogger(IntRSBridgeHandler.class);

    private final SerialPortManager serialPortManager;

    public IntRSBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler");

        final IntRSConfig config = getConfigAs(IntRSConfig.class);
        final String port = config.getPort();
        if (!port.isBlank()) {
            SatelModule satelModule = new IntRSModule(port, serialPortManager, config.getTimeout(),
                    config.hasExtCommandsSupport());
            super.initialize(satelModule);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Satel INT-RS module. Serial port is not set.");
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // The bridge serial port to be used for checks
        final String port = (String) getThing().getConfiguration().get(PORT);
        Collection<ConfigStatusMessage> configStatusMessages;

        // Check whether a serial port is provided
        if (port.isBlank()) {
            configStatusMessages = List.of(ConfigStatusMessage.Builder.error(PORT).withMessageKeySuffix("portEmpty")
                    .withArguments(PORT).build());
        } else {
            configStatusMessages = Collections.emptyList();
        }

        return configStatusMessages;
    }
}
