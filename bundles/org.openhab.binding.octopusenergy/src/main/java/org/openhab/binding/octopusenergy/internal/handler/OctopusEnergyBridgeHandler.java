/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.handler;

import static org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyApiHelper;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyConfiguration;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyDiscoveryService;
import org.openhab.binding.octopusenergy.internal.dto.ElectricityMeterPoint;
import org.openhab.binding.octopusenergy.internal.dto.GasMeterPoint;
import org.openhab.binding.octopusenergy.internal.exception.ApiException;
import org.openhab.binding.octopusenergy.internal.exception.AuthenticationException;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OctopusEnergyBridgeHandler} is responsible for handling the bridge things created to use the Octopus
 * Energy API.
 *
 * It also spawns a background polling thread to update the data at a specified time interval.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public class OctopusEnergyBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(OctopusEnergyBridgeHandler.class);

    private final OctopusEnergyApiHelper apiHelper;
    private @Nullable ScheduledFuture<?> pollingJob;

    private ZonedDateTime lastRefreshTime = UNDEFINED_TIME;

    public OctopusEnergyBridgeHandler(Bridge bridge, OctopusEnergyApiHelper apiHelper) {
        super(bridge);
        this.apiHelper = apiHelper;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Octopus Energy bridge handler.");
        OctopusEnergyConfiguration config = getConfigAs(OctopusEnergyConfiguration.class);

        if (config.accountNumber.equals("") || config.apiKey.equals("")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-account-number-or-api-key");
            return;
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            try {
                logger.debug("Login to API with account number: {}", config.accountNumber);
                apiHelper.setAccountNumber(config.accountNumber);
                apiHelper.setApiKey(config.apiKey);
                refresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (AuthenticationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.conf-error-authentication");
                return;
            } catch (ApiException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-general");
            }
        }

        ScheduledFuture<?> job = pollingJob;
        if (job == null || job.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    refresh();
                    if ((getThing().getStatus() == ThingStatus.OFFLINE) && (getThing().getStatusInfo()
                            .getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)) {
                        // if previous status was COMMUNICATION_ERROR, we now reestablished the comms
                        updateStatus(ThingStatus.ONLINE);
                    }
                } catch (ApiException e) {
                    logger.warn("Exception from API - {}", getThing().getUID(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-general");
                }
            }, config.refreshInterval, config.refreshInterval, TimeUnit.MINUTES);
            logger.debug("Bridge topology polling job every {} minutes", config.refreshInterval);
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(OctopusEnergyDiscoveryService.class);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            pollingJob = null;
            logger.debug("Stopped topology background polling process");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThing();
        } else {
            switch (channelUID.getId()) {
                case CHANNEL_BRIDGE_REFRESH:
                    if (OnOffType.ON.equals(command)) {
                        try {
                            refresh();
                        } catch (AuthenticationException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/offline.conf-error-authentication");
                            return;
                        } catch (ApiException e) {
                            logger.warn("Exception from API - {}, {}", getThing().getUID(), e.getMessage());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/offline.comm-error-general");
                        }
                        updateState(CHANNEL_BRIDGE_REFRESH, OnOffType.OFF);
                    }
                    break;
            }
        }
    }

    public ThingUID getUID() {
        return thing.getUID();
    }

    public Iterable<ElectricityMeterPoint> listElectricityMeterPoints() {
        return apiHelper.getAccounts().getElectricityMeterPoints();
    }

    public Iterable<GasMeterPoint> listGasMeterPoints() {
        return apiHelper.getAccounts().getGasMeterPoints();
    }

    protected void updateThing() {
        if (lastRefreshTime.isEqual(UNDEFINED_TIME)) {
            updateState(CHANNEL_BRIDGE_LAST_REFRESH_TIME, UnDefType.UNDEF);
        } else {
            updateState(CHANNEL_BRIDGE_LAST_REFRESH_TIME, new DateTimeType(lastRefreshTime));
        }
        updateState(CHANNEL_BRIDGE_REFRESH, OnOffType.OFF);
    }

    protected synchronized void refresh() throws ApiException {
        logger.debug("Refreshing cache from API");
        apiHelper.updateAccounts();
        apiHelper.updateElectricityConsumption();
        apiHelper.updateElectricityPrices();
        lastRefreshTime = ZonedDateTime.now();
        updateState(CHANNEL_BRIDGE_LAST_REFRESH_TIME, new DateTimeType(lastRefreshTime));

        logger.debug("Updating {} connected things", getThing().getThings().size());
        for (Thing th : getThing().getThings()) {
            String identifier = th.getUID().getId();
            Map<String, String> properties = null;
            ThingHandler handler = th.getHandler();
            if (handler instanceof OctopusEnergyElectricityMeterPointHandler) {
                ((OctopusEnergyElectricityMeterPointHandler) handler).updateThing();
                ElectricityMeterPoint mp;
                try {
                    mp = apiHelper.getAccounts().getElectricityMeterPoint(identifier);
                    properties = mp.getThingProperties();
                    ((OctopusEnergyElectricityMeterPointHandler) handler).updateProperties(properties);
                } catch (RecordNotFoundException e) {
                    logger.warn("No Meter Point found with MPAN - {}", identifier);
                }
            }
            if (handler instanceof OctopusEnergyGasMeterPointHandler) {
                ((OctopusEnergyGasMeterPointHandler) handler).updateThing();
                GasMeterPoint mp;
                try {
                    mp = apiHelper.getAccounts().getGasMeterPoint(identifier);
                    properties = mp.getThingProperties();
                    ((OctopusEnergyGasMeterPointHandler) handler).updateProperties(properties);
                } catch (RecordNotFoundException e) {
                    logger.warn("No Meter Point found with MPRN - {}", identifier);
                }
            }
        }
    }
}
