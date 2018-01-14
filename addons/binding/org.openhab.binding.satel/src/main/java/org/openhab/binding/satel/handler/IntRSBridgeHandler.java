/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.THING_TYPE_INTRS;
import static org.openhab.binding.satel.internal.config.IntRSConfig.PORT;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.satel.internal.config.IntRSConfig;
import org.openhab.binding.satel.internal.protocol.IntRSModule;
import org.openhab.binding.satel.internal.protocol.SatelModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IntRSBridgeHandler} is a bridge handler for INT-RS communication module.
 * All {@link SatelThingHandler}s use it to receive events and execute commands.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class IntRSBridgeHandler extends SatelBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_INTRS);

    private final Logger logger = LoggerFactory.getLogger(IntRSBridgeHandler.class);

    public IntRSBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler");

        IntRSConfig config = getConfigAs(IntRSConfig.class);
        if (StringUtils.isNotBlank(config.getPort())) {
            SatelModule satelModule = new IntRSModule(config.getPort(), config.getTimeout());
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
        if (StringUtils.isBlank(port)) {
            configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(PORT)
                    .withMessageKeySuffix("portEmpty").withArguments(PORT).build());
        } else {
            configStatusMessages = Collections.emptyList();
        }

        return configStatusMessages;
    }

}
