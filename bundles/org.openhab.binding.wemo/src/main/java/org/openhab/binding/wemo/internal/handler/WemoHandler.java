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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;
import static org.openhab.binding.wemo.internal.WemoUtil.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Kai Kreuzer - some refactoring for performance and simplification
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Mihir Patil - Added standby switch
 * @author Jacob Laursen - Refactoring
 */
@NonNullByDefault
public abstract class WemoHandler extends WemoBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoHandler.class);

    private final Object jobLock = new Object();

    private @Nullable ScheduledFuture<?> pollingJob;

    public WemoHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);

        logger.debug("Creating a WemoHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();

        addSubscription(BASICEVENT);
        if (THING_TYPE_INSIGHT.equals(thing.getThingTypeUID())) {
            addSubscription(INSIGHTEVENT);
        }
        pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, DEFAULT_REFRESH_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("WemoHandler disposed for thing {}", getThing().getUID());

        ScheduledFuture<?> job = this.pollingJob;
        if (job != null) {
            job.cancel(true);
        }
        this.pollingJob = null;
        super.dispose();
    }

    private void poll() {
        synchronized (jobLock) {
            if (pollingJob == null) {
                return;
            }
            try {
                logger.debug("Polling job");
                // Check if the Wemo device is set in the UPnP service registry
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("UPnP device {} not yet registered", getUDN());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "@text/config-status.pending.device-not-registered [\"" + getUDN() + "\"]");
                    return;
                }
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String wemoURL = getWemoURL(BASICACTION);
        if (wemoURL == null) {
            logger.debug("Failed to send command '{}' for device '{}': URL cannot be created", command,
                    getThing().getUID());
            return;
        }
        if (command instanceof RefreshType) {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        } else if (CHANNEL_STATE.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                try {
                    boolean binaryState = OnOffType.ON.equals(command) ? true : false;
                    String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";
                    String content = createBinaryStateContent(binaryState);
                    wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    logger.warn("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                            e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        }
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
     */
    protected void updateWemoState() {
        String actionService = BASICACTION;
        String action = "GetBinaryState";
        String variable = "BinaryState";
        String value = null;
        if ("insight".equals(getThing().getThingTypeUID().getId())) {
            action = "GetInsightParams";
            variable = "InsightParams";
            actionService = INSIGHTACTION;
        }
        String wemoURL = getWemoURL(actionService);
        if (wemoURL == null) {
            logger.debug("Failed to get actual state for device '{}': URL cannot be created", getThing().getUID());
            return;
        }
        String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        String content = createStateRequestContent(action, actionService);
        try {
            String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            if ("InsightParams".equals(variable)) {
                value = substringBetween(wemoCallResponse, "<InsightParams>", "</InsightParams>");
            } else {
                value = substringBetween(wemoCallResponse, "<BinaryState>", "</BinaryState>");
            }
            if (value.length() != 0) {
                logger.trace("New state '{}' for device '{}' received", value, getThing().getUID());
                this.onValueReceived(variable, value, actionService + "1");
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.warn("Failed to get actual state for device '{}': {}", getThing().getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
