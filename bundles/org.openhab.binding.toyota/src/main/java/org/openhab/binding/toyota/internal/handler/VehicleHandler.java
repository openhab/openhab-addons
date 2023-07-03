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
package org.openhab.binding.toyota.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.toyota.internal.config.VehicleConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are sent
 * to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private @NonNullByDefault({}) VehicleConfiguration configuration;
    private @NonNullByDefault({}) MyTBridgeHandler bridgeHandler;

    public VehicleHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.trace("Initializing the Toyota Vehicle handler for {}", getThing().getUID());

        Bridge bridge = getBridge();
        initializeBridge(bridge == null ? null : bridge.getHandler(), bridge == null ? null : bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {} for thing {}", bridgeStatusInfo, getThing().getUID());

        Bridge bridge = getBridge();
        initializeBridge(bridge == null ? null : bridge.getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeBridge(@Nullable ThingHandler thingHandler, @Nullable ThingStatus bridgeStatus) {
        logger.debug("initializeBridge {} for thing {}", bridgeStatus, getThing().getUID());

        if (thingHandler != null && bridgeStatus != null) {
            bridgeHandler = (MyTBridgeHandler) thingHandler;
            if (bridgeStatus == ThingStatus.ONLINE) {
                configuration = getConfigAs(VehicleConfiguration.class);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelID = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
        }
    }
}
