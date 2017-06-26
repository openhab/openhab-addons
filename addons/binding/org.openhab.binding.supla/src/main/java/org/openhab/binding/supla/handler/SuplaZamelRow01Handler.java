/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.handler;

import static java.lang.String.format;
import static org.openhab.binding.supla.SuplaBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SuplaZamelRow01Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class SuplaZamelRow01Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SuplaZamelRow01Handler.class);
    private SuplaCloudBridgeHandler bridgeHandler;

    public SuplaZamelRow01Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(SWITCH_CHANNEL)) {
            try {
                bridgeHandler.switchCommand((OnOffType) command, thing);
            } catch (RuntimeException e) {
                // TODO can do more generic
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        bridgeHandler = getBridgeHandler();
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    private synchronized SuplaCloudBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new RuntimeException("Required bridge not defined for device {}.");
        } else {
            return getBridgeHandler(bridge);
        }

    }

    private synchronized SuplaCloudBridgeHandler getBridgeHandler(Bridge bridge) {
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof SuplaCloudBridgeHandler) {
            return (SuplaCloudBridgeHandler) handler;
        } else {
            throw new RuntimeException(format("No available bridge handler found yet. Bridge: %s.", bridge.getUID()));
        }
    }
}
