/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getTimestamp;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for a Smart Hose Timer BaseStation.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
public class RachioBaseStationHandler extends AbstractRachioThingHandler {
    private final Logger logger = LoggerFactory.getLogger(RachioBaseStationHandler.class);

    private @Nullable RachioBaseStation baseStation;

    public RachioBaseStationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingId = getThing().getUID().getAsString();
        String baseStationId = getThingConfigurationOrPropertyString(PROPERTY_BASE_STATION_ID);
        logger.debug("Initializing Rachio BaseStation Thing '{}', configured baseStationId='{}'", getThing().getUID(),
                baseStationId);

        if (baseStationId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Missing Rachio baseStationId. Add the BaseStation via Inbox discovery or configure the Rachio BaseStation UUID manually.");
            return;
        }
        if (!initializeCloudHandler()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        refreshBaseStation(baseStationId, true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            RachioBaseStation currentBaseStation = baseStation;
            if (currentBaseStation == null) {
                initialize();
            } else {
                refreshBaseStation(currentBaseStation.id, false);
            }
        }
    }

    private boolean refreshBaseStation(String baseStationId, boolean initialLoad) {
        RachioBridgeHandler handler = cloudHandler;
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return false;
        }

        try {
            baseStation = initialLoad ? handler.getBaseStationForInitialization(baseStationId)
                    : handler.getBaseStation(baseStationId);
            RachioBaseStation currentBaseStation = baseStation;
            if (currentBaseStation == null || currentBaseStation.id.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Configured Rachio baseStationId was not found in the account.");
                return false;
            }
            thingId = currentBaseStation.getThingName();
            logger.debug("{}: BaseStation model lookup succeeded: baseStationId='{}'", thingId, currentBaseStation.id);
            if (resetLocalThrottleRetry()) {
                logger.debug(
                        "{}: Deferred initialization succeeded for Smart Hose Timer BaseStation '{}'; Thing is ONLINE.",
                        thingId, currentBaseStation.id);
            }
            goOnline();
            return true;
        } catch (RachioApiThrottledException e) {
            long delaySeconds = initialLoad
                    ? scheduleInitializationThrottleRetry(
                            "loading Smart Hose Timer BaseStation '" + baseStationId + "'",
                            () -> refreshBaseStation(baseStationId, true), e)
                    : scheduleLocalThrottleRetry("loading Smart Hose Timer BaseStation '" + baseStationId + "'",
                            () -> refreshBaseStation(baseStationId, false));
            if (delaySeconds > 0) {
                if (initialLoad) {
                    logger.debug(
                            "{}: Deferring initialization REST request for Smart Hose Timer BaseStation '{}' due to local API bootstrap pacing; retry scheduled in {} seconds.",
                            thingId, baseStationId, delaySeconds);
                } else {
                    logger.debug(
                            "{}: Local Rachio API throttle hit while loading Smart Hose Timer BaseStation '{}'; retry scheduled in {} seconds.",
                            thingId, baseStationId, delaySeconds);
                }
            }
            return false;
        } catch (RachioApiException e) {
            String message = "Unable to load Rachio BaseStation '" + baseStationId + "': " + e.getMessage();
            logger.debug("{}: {}", thingId, message);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            return false;
        } catch (RuntimeException e) {
            String message = "Unable to initialize Rachio BaseStation '" + baseStationId + "': " + e.getMessage();
            logger.debug("{}: {}", thingId, message, e);
            updateStatus(ThingStatus.OFFLINE,
                    initialLoad ? ThingStatusDetail.CONFIGURATION_ERROR : ThingStatusDetail.COMMUNICATION_ERROR,
                    message);
            return false;
        }
    }

    @Override
    protected void postChannelData() {
        RachioBaseStation currentBaseStation = baseStation;
        if (currentBaseStation == null) {
            return;
        }
        updateChannel(CHANNEL_BASE_STATION_NAME, new StringType(currentBaseStation.getThingName()));
        updateChannel(CHANNEL_BASE_STATION_ONLINE, onlineState(currentBaseStation));
        updateChannel(CHANNEL_LAST_UPDATE, getTimestamp());
    }

    @Override
    protected void goOnline() {
        RachioBaseStation currentBaseStation = baseStation;
        if (currentBaseStation != null) {
            updateProperties(currentBaseStation.fillProperties());
        }
        postChannelData();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    protected void onBridgeOnline() {
        RachioBaseStation currentBaseStation = baseStation;
        if (currentBaseStation == null) {
            initialize();
        } else {
            refreshBaseStation(currentBaseStation.id, false);
        }
    }

    @Override
    public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
        return false;
    }

    private State onlineState(RachioBaseStation currentBaseStation) {
        if (!currentBaseStation.hasOnlineState()) {
            return UnDefType.UNDEF;
        }
        return currentBaseStation.isOnline() ? OnOffType.ON : OnOffType.OFF;
    }
}
