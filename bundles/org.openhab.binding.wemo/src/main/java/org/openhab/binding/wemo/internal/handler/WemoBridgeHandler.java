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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoBridgeHandler} is the handler for a wemo bridge and connects it to
 * the framework.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class WemoBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(WemoBridgeHandler.class);

    public WemoBridgeHandler(Bridge bridge) {
        super(bridge);
        logger.debug("Creating a WemoBridgeHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WemoBridgeHandler");

        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.trace("Initializing WemoBridgeHandler for UDN '{}'", configuration.get(UDN));
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoBridgeHandler. UDN not set.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/config-status.error.missing-udn");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Not needed, all commands are handled in the {@link WemoLightHandler}
    }
}
