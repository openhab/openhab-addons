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
package org.openhab.binding.automower.internal.things;

import static org.openhab.binding.automower.internal.AutomowerBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.automower.internal.bridge.AutomowerBridgeHandler;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.StayOutZone;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutomowerStayoutZoneHandler} represents a StayoutZone of an automower as thing.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class AutomowerStayoutZoneHandler extends BaseThingHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_STAYOUTZONE);

    private final Logger logger = LoggerFactory.getLogger(AutomowerStayoutZoneHandler.class);
    private final String thingId;

    public AutomowerStayoutZoneHandler(Thing thing) {
        super(thing);
        this.thingId = this.getThing().getUID().getId();
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler == null) {
            logger.warn("No AutomowerBridgeHandler found for zoneId {}", this.thingId);
            return;
        }
        AutomowerHandler mowerHandler = automowerBridgeHandler.getAutomowerHandlerByStayoutZoneId(this.thingId);
        if (mowerHandler == null) {
            logger.warn("No AutomowerHandler found for zoneId {}", this.thingId);
            return;
        }

        /* all pre-conditions met ... */
        if (RefreshType.REFRESH == command) {
            mowerHandler.updateAutomowerState(); // refresh current state from cache
        } else if (CHANNEL_STAYOUTZONE_ENABLED.equals(channelUID.getIdWithoutGroup())) {
            if (command instanceof OnOffType cmd) {
                mowerHandler.sendAutomowerStayOutZone(this.thingId, cmd == OnOffType.ON);
            } else {
                logger.warn("Command {} not supported for channel {}", command, channelUID);
            }
        } else {
            logger.warn("Command {} not supported for channel {}", command, channelUID);
        }
    }

    @Override
    public void initialize() {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            // Adding handler to map of handlers
            automowerBridgeHandler.registerAutomowerStayoutZoneHandler(this.thingId, this);

            updateStatus(ThingStatus.ONLINE);
            logger.trace("AutomowerStayoutZoneHandler initialized for thingId {}", this.thingId);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/conf-error-no-bridge");
        }
    }

    @Nullable
    private AutomowerBridgeHandler getAutomowerBridgeHandler() {
        if (getBridge() instanceof Bridge bridge) {
            if (bridge.getHandler() instanceof AutomowerBridgeHandler bridgeHandler) {
                return bridgeHandler;
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        AutomowerBridgeHandler automowerBridgeHandler = getAutomowerBridgeHandler();
        if (automowerBridgeHandler != null) {
            automowerBridgeHandler.unregisterAutomowerStayoutZoneHandler(this.thingId);
        }
    }

    public void updateStayOutZoneChannels(StayOutZone stayOutZone) {
        updateState(CHANNEL_STAYOUTZONE_NAME, new StringType(stayOutZone.getName()));
        updateState(CHANNEL_STAYOUTZONE_ENABLED, OnOffType.from(stayOutZone.isEnabled()));
    }
}
