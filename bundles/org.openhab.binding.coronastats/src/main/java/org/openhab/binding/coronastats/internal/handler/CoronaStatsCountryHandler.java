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
package org.openhab.binding.coronastats.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.coronastats.internal.config.CoronaStatsCountryConfiguration;
import org.openhab.binding.coronastats.internal.dto.CoronaStats;
import org.openhab.binding.coronastats.internal.dto.CoronaStatsCountry;
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
 * The {@link CoronaStatsCountryHandler} is the handler for country thing
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class CoronaStatsCountryHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(CoronaStatsCountryHandler.class);

    private CoronaStatsCountryConfiguration thingConfig = new CoronaStatsCountryConfiguration();

    public CoronaStatsCountryHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingConfig = getConfigAs(CoronaStatsCountryConfiguration.class);
        logger.debug("Initializing Corona Stats country handler for country code {}", thingConfig.getCountryCode());

        if (thingConfig.isValid()) {
            CoronaStatsWorldHandler handler = getBridgeHandler();
            if (handler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge handler missing");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No valid country code given.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("CoronaStats country handler disposes.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
        }
    }

    private void refresh() {
        CoronaStatsWorldHandler handler = getBridgeHandler();
        if (handler != null) {
            CoronaStats coronaStats = handler.getCoronaStats();
            if (coronaStats != null) {
                notifyOnUpdate(coronaStats);
            }
        }
    }

    private @Nullable CoronaStatsWorldHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof CoronaStatsWorldHandler worldHandler) {
                return worldHandler;
            }
        }

        return null;
    }

    public void notifyOnUpdate(CoronaStats coronaStats) {
        CoronaStatsCountry country = coronaStats.getCountry(thingConfig.getCountryCode());
        if (country == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Country not found");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
        updateProperties(country.getProperties());

        country.getChannelsStateMap().forEach(this::updateState);
    }
}
