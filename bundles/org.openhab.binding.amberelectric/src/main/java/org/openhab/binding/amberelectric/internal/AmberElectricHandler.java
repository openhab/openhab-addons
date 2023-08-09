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
package org.openhab.binding.amberelectric.internal;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amberelectric.internal.api.CurrentPrices;
import org.openhab.binding.amberelectric.internal.api.Sites;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmberElectricHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class AmberElectricHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(AmberElectricHandler.class);

    private long refreshInterval;
    private @Nullable String siteID;
    private @Nullable String apikey;

    private @NonNullByDefault({}) AmberElectricWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public AmberElectricHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This binding is read only");
    }

    @Override
    public void initialize() {
        AmberElectricConfiguration config = getConfigAs(AmberElectricConfiguration.class);
        if (config.apikey == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key must be set");
        } else {
            webTargets = new AmberElectricWebTargets(config.site);
            refreshInterval = config.refresh;
            siteID = config.site;
            apikey = config.apikey;

            schedulePoll();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 1 second out, then every {} s", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, refreshInterval, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to AmberAPI", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to AmberAPI", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() throws IOException {

        if (siteID == null) {
            Sites sites = webTargets.getSites(apikey);
            // add error handling
            siteID = sites.siteid;
            logger.debug("Detected amber siteid is {}", sites.siteid);
        }

        CurrentPrices currentPrices = webTargets.getCurrentPrices(siteID, apikey);
        updateStatus(ThingStatus.ONLINE);
        if (currentPrices != null) {
            updateState(AmberElectricBindingConstants.CHANNEL_AMBERELECTRIC_ELECPRICE,
                    new DecimalType(currentPrices.perKwh));
            updateState(AmberElectricBindingConstants.CHANNEL_AMBERELECTRIC_NEMTIME,
                    new StringType(currentPrices.nemTime));
            updateState(AmberElectricBindingConstants.CHANNEL_AMBERELECTRIC_RENEWABLES,
                    new DecimalType(currentPrices.renewables));
            updateState(AmberElectricBindingConstants.CHANNEL_AMBERELECTRIC_SPOTPRICE,
                    new DecimalType(currentPrices.spotPerKwh));
            switch (currentPrices.spikeStatus) {
                case "none":
                    updateState(AmberElectricBindingConstants.CHANNEL_AMBERELECTRIC_SPIKE, OnOffType.OFF);
                    break;
                default:
                    updateState(AmberElectricBindingConstants.CHANNEL_AMBERELECTRIC_SPIKE, OnOffType.ON);
                    break;
            }
        }
    }
}
