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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;
import static org.openhab.binding.wemo.internal.WemoUtil.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_CROCKPOT);

    private final Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<>());

    public WemoCrockpotHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);

        logger.debug("Creating a WemoCrockpotHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String localHost = getHost();
        if (localHost.isEmpty()) {
            logger.error("Failed to send command '{}' for device '{}': IP address missing", command,
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-ip");
            return;
        }
        String wemoURL = getWemoURL(localHost, BASICACTION);
        if (wemoURL == null) {
            logger.error("Failed to send command '{}' for device '{}': URL cannot be created", command,
                    getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-url");
            return;
        }
        String mode = "0";
        String time = null;

        if (command instanceof RefreshType) {
            updateWemoState();
        } else if (CHANNEL_COOKMODE.equals(channelUID.getId())) {
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
                String content = "<?xml version=\"1.0\"?>"
                        + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                        + "<s:Body>" + "<u:SetCrockpotState xmlns:u=\"urn:Belkin:service:basicevent:1\">" + "<mode>"
                        + mode + "</mode>" + "<time>" + time + "</time>" + "</u:SetCrockpotState>" + "</s:Body>"
                        + "</s:Envelope>";
                String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null && logger.isTraceEnabled()) {
                    logger.trace("wemoCall to URL '{}' for device '{}'", wemoURL, getThing().getUID());
                    logger.trace("wemoCall with soapHeader '{}' for device '{}'", soapHeader, getThing().getUID());
                    logger.trace("wemoCall with content '{}' for device '{}'", content, getThing().getUID());
                    logger.trace("wemoCall with response '{}' for device '{}'", wemoCallResponse, getThing().getUID());
                }
            } catch (RuntimeException e) {
                logger.debug("Failed to send command '{}' for device '{}':", command, getThing().getUID(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            updateStatus(ThingStatus.ONLINE);
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
    @Override
    protected void updateWemoState() {
        String localHost = getHost();
        if (localHost.isEmpty()) {
            logger.error("Failed to get actual state for device '{}': IP address missing", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-ip");
            return;
        }
        String actionService = BASICEVENT;
        String wemoURL = getWemoURL(localHost, actionService);
        if (wemoURL == null) {
            logger.error("Failed to get actual state for device '{}': URL cannot be created", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/config-status.error.missing-url");
            return;
        }
        try {
            String action = "GetCrockpotState";
            String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
            String content = createStateRequestContent(action, actionService);
            String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            if (wemoCallResponse != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("wemoCall to URL '{}' for device '{}'", wemoURL, getThing().getUID());
                    logger.trace("wemoCall with soapHeader '{}' for device '{}'", soapHeader, getThing().getUID());
                    logger.trace("wemoCall with content '{}' for device '{}'", content, getThing().getUID());
                    logger.trace("wemoCall with response '{}' for device '{}'", wemoCallResponse, getThing().getUID());
                }
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
                        updateState(CHANNEL_WARMCOOKTIME, warmTime);
                        break;
                    case "51":
                        newMode = new StringType("LOW");
                        State lowTime = DecimalType.valueOf(time);
                        updateState(CHANNEL_LOWCOOKTIME, lowTime);
                        break;
                    case "52":
                        newMode = new StringType("HIGH");
                        State highTime = DecimalType.valueOf(time);
                        updateState(CHANNEL_HIGHCOOKTIME, highTime);
                        break;
                }
                updateState(CHANNEL_COOKMODE, newMode);
                updateState(CHANNEL_COOKEDTIME, newCoockedTime);
            }
        } catch (RuntimeException e) {
            logger.debug("Failed to get actual state for device '{}': {}", getThing().getUID(), e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public synchronized void addSubscription() {
        // We cannot subscribe to service events for Crockpot device.
    }

    @Override
    public synchronized void removeSubscription() {
        // We cannot subscribe to service events for Crockpot device.
    }
}
