/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.easee.internal.handler;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.config.EaseeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EaseeUserHandler} represents a user with access to an Easee site.
 * This handler only maintains static user properties and does not have channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeUserHandler extends BaseThingHandler implements EaseeThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EaseeUserHandler.class);

    public EaseeUserHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            logger.debug("bridgeStatusChanged: ONLINE");
            if (isInitialized()) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            logger.debug("bridgeStatusChanged: NOT ONLINE");
            if (isInitialized()) {
                if (bridgeStatusInfo.getStatus() == ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize User");
        String userId = getConfig().get(THING_CONFIG_ID).toString();
        logger.debug("Easee User initialized with id: {}", userId);

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_BRIDGE);
        }
    }

    @Override
    public void dispose() {
        logger.debug("User handler disposed.");
        super.dispose();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void enqueueCommand(EaseeCommand command) {
        EaseeBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeHandler.enqueueCommand(command);
        } else {
            logger.warn("no bridge handler found");
        }
    }

    private @Nullable EaseeBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge == null ? null : ((EaseeBridgeHandler) bridge.getHandler());
    }

    @Override
    public EaseeConfiguration getBridgeConfiguration() {
        EaseeBridgeHandler bridgeHandler = getBridgeHandler();
        return bridgeHandler == null ? new EaseeConfiguration() : bridgeHandler.getBridgeConfiguration();
    }
}
