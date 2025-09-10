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
package org.openhab.binding.homekit.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.config.HomekitBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomekitBridgeHandler} is responsible for marshalling communications with HomeKit device servers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBridgeHandler.class);

    private @Nullable HomekitBridgeConfiguration config;

    public HomekitBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        // TODO initialize the overall HomeKit user credentials
        // TODO initialise mDNS discovery of HomeKit device servers
        // TODO set state to ONLINE if successful
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Not used - Bridge has no channels
    }
}
