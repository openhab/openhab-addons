/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.handler;

import static org.openhab.binding.tesla.internal.TeslaBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.measure.quantity.Temperature;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.glassfish.jersey.client.ClientProperties;
import org.openhab.binding.tesla.internal.TeslaBindingConstants;
import org.openhab.binding.tesla.internal.TeslaBindingConstants.EventKeys;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler.Authenticator;
import org.openhab.binding.tesla.internal.handler.TeslaAccountHandler.Request;
//import org.openhab.binding.tesla.internal.protocol.ChargeState;
//import org.openhab.binding.tesla.internal.protocol.ClimateState;
//import org.openhab.binding.tesla.internal.protocol.DriveState;
//import org.openhab.binding.tesla.internal.protocol.GUIState;
//import org.openhab.binding.tesla.internal.protocol.Vehicle;
//import org.openhab.binding.tesla.internal.protocol.VehicleState;
import org.openhab.binding.tesla.internal.throttler.QueueChannelThrottler;
//import org.openhab.binding.tesla.internal.throttler.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TeslaPowerwallHandler} is responsible for handling commands, which are sent
 * to one of the channels of a specific powerwall
 *
 * @author Paul Smedley - Initial contribution
 */
public class TeslaPowerwallHandler extends TeslaVehicleHandler {

    private static final int EVENT_STREAM_CONNECT_TIMEOUT = 3000;
    private static final int EVENT_STREAM_READ_TIMEOUT = 200000;
    private static final int EVENT_STREAM_PAUSE = 5000;
    private static final int EVENT_TIMESTAMP_AGE_LIMIT = 3000;
    private static final int EVENT_TIMESTAMP_MAX_DELTA = 10000;
    private static final int FAST_STATUS_REFRESH_INTERVAL = 15000;
    private static final int SLOW_STATUS_REFRESH_INTERVAL = 60000;
    private static final int EVENT_MAXIMUM_ERRORS_IN_INTERVAL = 10;
    private static final int EVENT_ERROR_INTERVAL_SECONDS = 15;
    private static final int API_SLEEP_INTERVAL_MINUTES = 20;
    private static final int MOVE_THRESHOLD_INTERVAL_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallHandler.class);

    protected WebTarget eventTarget;

    // Vehicle state variables
//    protected Powerwall powerwall;
    protected String powerwallJSON;
    protected TeslaAccountHandler account;

    protected QueueChannelThrottler stateThrottler;

    protected Client eventClient = ClientBuilder.newClient();
    protected TeslaChannelSelectorProxy teslaChannelSelectorProxy = new TeslaChannelSelectorProxy();
    protected Thread eventThread;
    protected ScheduledFuture<?> fastStateJob;
    protected ScheduledFuture<?> slowStateJob;

    private final Gson gson = new Gson();
    private final JsonParser parser = new JsonParser();

    public TeslaPowerwallHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        logger.debug("Initializing the Tesla handler for {}", getThing().getUID());
        updateStatus(ThingStatus.UNKNOWN);
        updateStatus(ThingStatus.ONLINE);
        logger.debug("We don't do anything yet but we got here!");
        account = (TeslaAccountHandler) getBridge().getHandler();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Tesla handler for {}", getThing().getUID());
        try {
            if (fastStateJob != null && !fastStateJob.isCancelled()) {
                fastStateJob.cancel(true);
                fastStateJob = null;
            }

            if (slowStateJob != null && !slowStateJob.isCancelled()) {
                slowStateJob.cancel(true);
                slowStateJob = null;
            }

            if (eventThread != null && !eventThread.isInterrupted()) {
                eventThread.interrupt();
                eventThread = null;
            }
        } finally {
        }

        eventClient.close();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand {} {}", channelUID, command);
        String channelID = channelUID.getId();
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromChannelID(channelID);

        if (command instanceof RefreshType) {
            // Request the state of all known variables. This is sub-optimal, but the requests get scheduled and
            // throttled so we are safe not to break the Tesla SLA
            //requestAllData();
        } else {
            if (selector != null) {
                try {
// do nothing                  
                } catch (IllegalArgumentException e) {
                    logger.warn(
                            "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                            channelID, command.toString());
                }
            }
        }
    }

    public void sendCommand(String command, String payLoad, WebTarget target) {
        if (command.equals(COMMAND_WAKE_UP)) {
            Request request = account.newRequest(this, command, payLoad, target);
            if (stateThrottler != null) {
                stateThrottler.submit(COMMAND_THROTTLE, request);
            }
        }
    }

    public void sendCommand(String command) {
        sendCommand(command, "{}");
    }

    public void sendCommand(String command, String payLoad) {
        if (command.equals(COMMAND_WAKE_UP)) {
            Request request = account.newRequest(this, command, payLoad, account.commandTarget);
            if (stateThrottler != null) {
                stateThrottler.submit(COMMAND_THROTTLE, request);
            }
        }
    }

    public void sendCommand(String command, WebTarget target) {
        if (command.equals(COMMAND_WAKE_UP)) {
            Request request = account.newRequest(this, command, "{}", target);
            if (stateThrottler != null) {
                stateThrottler.submit(COMMAND_THROTTLE, request);
            }
        }
    }

    public void requestData(String command, String payLoad) {
        if (command.equals(COMMAND_WAKE_UP)) {
            Request request = account.newRequest(this, command, payLoad, account.dataRequestTarget);
            if (stateThrottler != null) {
                stateThrottler.submit(DATA_THROTTLE, request);
            }
        }
    }

    @Override
    protected void updateStatus(@NonNull ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    protected void updateStatus(@NonNull ThingStatus status, @NonNull ThingStatusDetail statusDetail) {
        super.updateStatus(status, statusDetail);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public void requestData(String command) {
        requestData(command, null);
    }


}
