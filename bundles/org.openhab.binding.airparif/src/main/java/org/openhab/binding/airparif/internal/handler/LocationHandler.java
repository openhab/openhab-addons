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
package org.openhab.binding.airparif.internal.handler;

import static org.openhab.binding.airparif.internal.AirParifBindingConstants.GROUP_POLLENS;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.AirParifApi.Pollen;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollensResponse;
import org.openhab.binding.airparif.internal.api.AirParifDto.Route;
import org.openhab.binding.airparif.internal.api.PollenAlertLevel;
import org.openhab.binding.airparif.internal.config.LocationConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LocationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LocationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(LocationHandler.class);

    private @Nullable LocationConfiguration config;

    public LocationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(LocationConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::getConcentrations);
    }

    public void setPollens(PollensResponse pollens) {
        LocationConfiguration local = config;
        if (local != null) {
            Map<Pollen, PollenAlertLevel> alerts = pollens.getDepartment(local.department);
            alerts.forEach((pollen, level) -> {
                updateState(GROUP_POLLENS + "#" + pollen.name().toLowerCase(), new DecimalType(level.ordinal()));
            });
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void getConcentrations() {
        AirParifBridgeHandler apiHandler = getApiBridgeHandler();
        LocationConfiguration local = config;
        if (apiHandler != null && local != null) {
            Route route = apiHandler.getConcentrations(local.location);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    private @Nullable AirParifBridgeHandler getApiBridgeHandler() {
        Bridge bridge = this.getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            if (bridge.getHandler() instanceof AirParifBridgeHandler airParifBridgeHandler) {
                return airParifBridgeHandler;
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/incorrect-bridge");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        return null;
    }
}
