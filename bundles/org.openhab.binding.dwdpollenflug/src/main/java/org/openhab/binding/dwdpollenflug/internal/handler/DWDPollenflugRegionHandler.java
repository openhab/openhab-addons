/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dwdpollenflug.internal.config.DWDPollenflugRegionConfiguration;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDPollenflug;
import org.openhab.binding.dwdpollenflug.internal.dto.DWDRegion;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DWDPollenflugRegionHandler} is the handler for bridge thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugRegionHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DWDPollenflugRegionHandler.class);

    private DWDPollenflugRegionConfiguration thingConfig = new DWDPollenflugRegionConfiguration();

    public DWDPollenflugRegionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DWD Pollenflug region handler");
        thingConfig = getConfigAs(DWDPollenflugRegionConfiguration.class);

        if (thingConfig.isValid()) {
            DWDPollenflugBridgeHandler handler = getBridgeHandler();
            if (handler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge handler missing");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No valid region id given.");
        }
    }

    private @Nullable DWDPollenflugBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof DWDPollenflugBridgeHandler) {
                DWDPollenflugBridgeHandler bridgeHandler = (DWDPollenflugBridgeHandler) handler;
                return bridgeHandler;
            }
        }

        return null;
    }

    @Override
    public void dispose() {
        logger.debug("DWDPollenflug region handler disposes.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private void refresh() {
        DWDPollenflugBridgeHandler handler = getBridgeHandler();
        if (handler != null) {
            DWDPollenflug pollenflug = handler.getPollenflug();
            if (pollenflug != null) {
                notifyOnUpdate(pollenflug);
            }
        }
    }

    public void notifyOnUpdate(DWDPollenflug pollenflug) {
        DWDRegion region = pollenflug.getRegion(thingConfig.regionID);
        if (region == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Region not found");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        updateProperties(region.getProperties());

        region.getChannelsStateMap().forEach(this::updateState);
    }
}
