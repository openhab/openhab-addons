/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.THING_TYPE_ETHM1;
import static org.openhab.binding.satel.internal.config.Ethm1Config.HOST;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.satel.internal.config.Ethm1Config;
import org.openhab.binding.satel.internal.protocol.Ethm1Module;
import org.openhab.binding.satel.internal.protocol.SatelModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Ethm1BridgeHandler} is a bridge handler for ETHM-1 communication module.
 * All {@link SatelThingHandler}s use it to receive events and execute commands.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class Ethm1BridgeHandler extends SatelBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_ETHM1);

    private final Logger logger = LoggerFactory.getLogger(Ethm1BridgeHandler.class);

    public Ethm1BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler");

        Ethm1Config config = getConfigAs(Ethm1Config.class);
        if (StringUtils.isNotBlank(config.getHost())) {
            SatelModule satelModule = new Ethm1Module(config.getHost(), config.getPort(), config.getTimeout(),
                    config.getEncryptionKey());
            super.initialize(satelModule);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to Satel ETHM-1 module. IP address or host name not set.");
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // The bridge IP address to be used for checks
        String host = (String) getThing().getConfiguration().get(HOST);
        Collection<ConfigStatusMessage> configStatusMessages;

        // Check whether an IP address is provided
        if (StringUtils.isBlank(host)) {
            configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(HOST)
                    .withMessageKeySuffix("hostEmpty").withArguments(HOST).build());
        } else {
            configStatusMessages = Collections.emptyList();
        }

        return configStatusMessages;
    }

}
