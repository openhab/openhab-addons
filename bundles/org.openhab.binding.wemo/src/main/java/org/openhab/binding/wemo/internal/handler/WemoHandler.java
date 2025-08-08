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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;
import static org.openhab.binding.wemo.internal.WemoUtil.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wemo.internal.exception.MissingHostException;
import org.openhab.binding.wemo.internal.exception.WemoException;
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

    public WemoHandler(final Thing thing, final UpnpIOService upnpIOService, final HttpClient httpClient) {
        super(thing, upnpIOService, httpClient);

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
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        this.pollingJob = null;
        super.dispose();
    }

    private void poll() {
        synchronized (jobLock) {
            logger.debug("Polling job for thing {}", getThing().getUID());
            // Check if the Wemo device is set in the UPnP service registry
            if (!isUpnpDeviceRegistered()) {
                logger.debug("UPnP device {} not yet registered", getUDN());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                        "@text/config-status.pending.device-not-registered [\"" + getUDN() + "\"]");
                return;
            }
            updateWemoState();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateWemoState();
        } else if (CHANNEL_STATE.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                try {
                    boolean binaryState = OnOffType.ON.equals(command) ? true : false;
                    String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";
                    String content = createBinaryStateContent(binaryState);
                    probeAndExecuteCall(BASICACTION, soapHeader, content);
                    updateStatus(ThingStatus.ONLINE);
                } catch (MissingHostException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/config-status.error.missing-ip");
                } catch (WemoException e) {
                    logger.warn("Failed to send command '{}' for thing '{}': {}", command, getThing().getUID(),
                            e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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
        String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        String content = createStateRequestContent(action, actionService);
        try {
            String wemoCallResponse = probeAndExecuteCall(actionService, soapHeader, content);
            if ("InsightParams".equals(variable)) {
                value = substringBetween(wemoCallResponse, "<InsightParams>", "</InsightParams>");
            } else {
                value = substringBetween(wemoCallResponse, "<BinaryState>", "</BinaryState>");
            }
            if (value.length() != 0) {
                logger.trace("New state '{}' for thing '{}' received", value, getThing().getUID());
                this.onValueReceived(variable, value, actionService + "1");
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (MissingHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-ip");
        } catch (WemoException e) {
            logger.debug("Failed to get actual state for thing '{}': {}", getThing().getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
