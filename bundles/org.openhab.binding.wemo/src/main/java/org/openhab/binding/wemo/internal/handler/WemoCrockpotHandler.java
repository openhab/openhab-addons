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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoCrockpotHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution;
 */
@NonNullByDefault
public class WemoCrockpotHandler extends WemoBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoCrockpotHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_CROCKPOT);

    private final Object jobLock = new Object();

    private final Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    private @Nullable ScheduledFuture<?> pollingJob;

    public WemoCrockpotHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);

        logger.debug("Creating a WemoCrockpotHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();
        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.debug("Initializing WemoCrockpotHandler for UDN '{}'", configuration.get(UDN));
            addSubscription(BASICEVENT);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, 0, DEFAULT_REFRESH_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/config-status.error.missing-udn");
        }
    }

    @Override
    public void dispose() {
        logger.debug("WeMoCrockpotHandler disposed.");
        ScheduledFuture<?> job = this.pollingJob;
        if (job != null && !job.isCancelled()) {
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
        String mode = "0";
        String time = null;

        if (command instanceof RefreshType) {
            updateWemoState();
        } else if (CHANNEL_COOK_MODE.equals(channelUID.getId())) {
            String commandString = command.toString();
            switch (commandString) {
                case "OFF":
                    mode = "0";
                    time = "0";
                    break;
                case "WARM":
                    mode = "50";
                    break;
                case "LOW":
                    mode = "51";
                    break;
                case "HIGH":
                    mode = "52";
                    break;
            }
            try {
                String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";
                String content = """
                        <?xml version="1.0"?>\
                        <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">\
                        <s:Body>\
                        <u:SetCrockpotState xmlns:u="urn:Belkin:service:basicevent:1">\
                        <mode>\
                        """
                        + mode + "</mode>" + "<time>" + time + "</time>" + "</u:SetCrockpotState>" + "</s:Body>"
                        + "</s:Envelope>";
                wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                logger.debug("Failed to send command '{}' for device '{}':", command, getThing().getUID(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'", variable, value, service,
                this.getThing().getUID());

        updateStatus(ThingStatus.ONLINE);
        if (variable != null && value != null) {
            this.stateMap.put(variable, value);
        }
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
     */
    protected void updateWemoState() {
        String actionService = BASICEVENT;
        String wemoURL = getWemoURL(actionService);
        if (wemoURL == null) {
            logger.warn("Failed to get actual state for device '{}': URL cannot be created", getThing().getUID());
            return;
        }
        try {
            String action = "GetCrockpotState";
            String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
            String content = createStateRequestContent(action, actionService);
            String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            String mode = substringBetween(wemoCallResponse, "<mode>", "</mode>");
            String time = substringBetween(wemoCallResponse, "<time>", "</time>");
            String coockedTime = substringBetween(wemoCallResponse, "<coockedTime>", "</coockedTime>");

            State newMode = new StringType(mode);
            State newCoockedTime = DecimalType.valueOf(coockedTime);
            switch (mode) {
                case "0":
                    newMode = new StringType("OFF");
                    break;
                case "50":
                    newMode = new StringType("WARM");
                    State warmTime = DecimalType.valueOf(time);
                    updateState(CHANNEL_WARM_COOK_TIME, warmTime);
                    break;
                case "51":
                    newMode = new StringType("LOW");
                    State lowTime = DecimalType.valueOf(time);
                    updateState(CHANNEL_LOW_COOK_TIME, lowTime);
                    break;
                case "52":
                    newMode = new StringType("HIGH");
                    State highTime = DecimalType.valueOf(time);
                    updateState(CHANNEL_HIGHCOOKTIME, highTime);
                    break;
            }
            updateState(CHANNEL_COOK_MODE, newMode);
            updateState(CHANNEL_COOKED_TIME, newCoockedTime);
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.debug("Failed to get actual state for device '{}': {}", getThing().getUID(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
