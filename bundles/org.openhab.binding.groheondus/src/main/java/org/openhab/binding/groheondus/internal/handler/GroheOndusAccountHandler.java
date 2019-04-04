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
package org.openhab.binding.groheondus.internal.handler;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.grohe.ondus.api.OndusService;
import org.openhab.binding.groheondus.internal.GroheOndusAccountConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Schmidt and Arne Wohlert - Initial contribution
 */
@NonNullByDefault
public class GroheOndusAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(GroheOndusAccountHandler.class);

    private @Nullable OndusService ondusService;

    public GroheOndusAccountHandler(Bridge bridge) {
        super(bridge);
    }

    public OndusService getService() {
        OndusService ret = this.ondusService;
        if (ret == null) {
            throw new IllegalStateException("OndusService requested, which is null (UNINITIALIZED)");
        }
        return ret;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do for bridge
    }

    @Override
    public void dispose() {
        super.dispose();
        if (ondusService != null) {
            ondusService = null;
        }
    }

    @Override
    public void initialize() {
        GroheOndusAccountConfiguration config = getConfigAs(GroheOndusAccountConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);
        try {
            ondusService = OndusService.login(config.username, config.password);
            updateStatus(ThingStatus.ONLINE);

            scheduler.submit(() -> getThing().getThings().forEach(thing -> {
                GroheOndusBaseHandler thingHandler = (GroheOndusBaseHandler) thing.getHandler();
                if (thingHandler != null) {
                    thingHandler.updateChannels();
                }
            }));
        } catch (LoginException e) {
            logger.debug("Grohe api login failed", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Login failed");
        } catch (IOException e) {
            logger.debug("Communication error while logging into the grohe api", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
