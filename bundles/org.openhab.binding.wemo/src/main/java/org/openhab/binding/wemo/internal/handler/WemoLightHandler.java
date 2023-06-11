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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoLightHandler} is the handler for a WeMo light, responsible for handling commands and state updates for the
 * different channels of a WeMo light.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class WemoLightHandler extends WemoBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoLightHandler.class);

    private final Object jobLock = new Object();

    private @Nullable WemoBridgeHandler wemoBridgeHandler;

    private @Nullable String wemoLightID;

    private int currentBrightness;

    /**
     * Set dimming stepsize to 5%
     */
    private static final int DIM_STEPSIZE = 5;

    /**
     * The default refresh initial delay in Seconds.
     */
    private static final int DEFAULT_REFRESH_INITIAL_DELAY = 15;

    private @Nullable ScheduledFuture<?> pollingJob;

    public WemoLightHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpcaller) {
        super(thing, upnpIOService, wemoHttpcaller);

        logger.debug("Creating a WemoLightHandler for thing '{}'", getThing().getUID());
    }

    @Override
    public void initialize() {
        super.initialize();
        // initialize() is only called if the required parameter 'deviceID' is available
        wemoLightID = (String) getConfig().get(DEVICE_ID);

        final Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            addSubscription(BRIDGEEVENT);
            pollingJob = scheduler.scheduleWithFixedDelay(this::poll, DEFAULT_REFRESH_INITIAL_DELAY,
                    DEFAULT_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            ScheduledFuture<?> job = this.pollingJob;
            if (job != null && !job.isCancelled()) {
                job.cancel(true);
            }
            this.pollingJob = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("WemoLightHandler disposed.");

        ScheduledFuture<?> job = this.pollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
        }
        this.pollingJob = null;
        super.dispose();
    }

    private synchronized @Nullable WemoBridgeHandler getWemoBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warn("Required bridge not defined for device {}.", wemoLightID);
            return null;
        }
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof WemoBridgeHandler) {
            this.wemoBridgeHandler = (WemoBridgeHandler) handler;
        } else {
            logger.debug("No available bridge handler found for {} bridge {} .", wemoLightID, bridge.getUID());
            return null;
        }
        return this.wemoBridgeHandler;
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
                getDeviceState();
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
                getDeviceState();
            } catch (Exception e) {
                logger.debug("Exception during poll", e);
            }
        } else {
            Configuration configuration = getConfig();
            configuration.get(DEVICE_ID);

            WemoBridgeHandler wemoBridge = getWemoBridgeHandler();
            if (wemoBridge == null) {
                logger.debug("wemoBridgeHandler not found, cannot handle command");
                return;
            }
            String devUDN = "uuid:" + wemoBridge.getThing().getConfiguration().get(UDN).toString();
            logger.trace("WeMo Bridge to send command to : {}", devUDN);

            String value = null;
            String capability = null;
            switch (channelUID.getId()) {
                case CHANNEL_BRIGHTNESS:
                    capability = "10008";
                    if (command instanceof PercentType) {
                        int newBrightness = ((PercentType) command).intValue();
                        logger.trace("wemoLight received Value {}", newBrightness);
                        int value1 = Math.round(newBrightness * 255 / 100);
                        value = value1 + ":0";
                        currentBrightness = newBrightness;
                    } else if (command instanceof OnOffType) {
                        switch (command.toString()) {
                            case "ON":
                                value = "255:0";
                                break;
                            case "OFF":
                                value = "0:0";
                                break;
                        }
                    } else if (command instanceof IncreaseDecreaseType) {
                        int newBrightness;
                        switch (command.toString()) {
                            case "INCREASE":
                                currentBrightness = currentBrightness + DIM_STEPSIZE;
                                newBrightness = Math.round(currentBrightness * 255 / 100);
                                if (newBrightness > 255) {
                                    newBrightness = 255;
                                }
                                value = newBrightness + ":0";
                                break;
                            case "DECREASE":
                                currentBrightness = currentBrightness - DIM_STEPSIZE;
                                newBrightness = Math.round(currentBrightness * 255 / 100);
                                if (newBrightness < 0) {
                                    newBrightness = 0;
                                }
                                value = newBrightness + ":0";
                                break;
                        }
                    }
                    break;
                case CHANNEL_STATE:
                    capability = "10006";
                    switch (command.toString()) {
                        case "ON":
                            value = "1";
                            break;
                        case "OFF":
                            value = "0";
                            break;
                    }
                    break;
            }
            try {
                if (capability != null && value != null) {
                    String soapHeader = "\"urn:Belkin:service:bridge:1#SetDeviceStatus\"";
                    String content = "<?xml version=\"1.0\"?>"
                            + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                            + "<s:Body>" + "<u:SetDeviceStatus xmlns:u=\"urn:Belkin:service:bridge:1\">"
                            + "<DeviceStatusList>"
                            + "&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;DeviceStatus&gt;&lt;DeviceID&gt;"
                            + wemoLightID
                            + "&lt;/DeviceID&gt;&lt;IsGroupAction&gt;NO&lt;/IsGroupAction&gt;&lt;CapabilityID&gt;"
                            + capability + "&lt;/CapabilityID&gt;&lt;CapabilityValue&gt;" + value
                            + "&lt;/CapabilityValue&gt;&lt;/DeviceStatus&gt;" + "</DeviceStatusList>"
                            + "</u:SetDeviceStatus>" + "</s:Body>" + "</s:Envelope>";

                    wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                    if ("10008".equals(capability)) {
                        OnOffType binaryState = null;
                        binaryState = "0".equals(value) ? OnOffType.OFF : OnOffType.ON;
                        updateState(CHANNEL_STATE, binaryState);
                    }
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (Exception e) {
                logger.warn("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    }

    @Override
    public @Nullable String getUDN() {
        WemoBridgeHandler wemoBridge = getWemoBridgeHandler();
        if (wemoBridge == null) {
            logger.debug("wemoBridgeHandler not found");
            return null;
        }
        return (String) wemoBridge.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link getDeviceState} is used for polling the actual state of a WeMo Light and updating the according
     * channel states.
     */
    public void getDeviceState() {
        logger.debug("Request actual state for LightID '{}'", wemoLightID);
        String wemoURL = getWemoURL(BRIDGEACTION);
        if (wemoURL == null) {
            logger.debug("Failed to get actual state for device '{}': URL cannot be created", getThing().getUID());
            return;
        }
        try {
            String soapHeader = "\"urn:Belkin:service:bridge:1#GetDeviceStatus\"";
            String content = "<?xml version=\"1.0\"?>"
                    + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                    + "<s:Body>" + "<u:GetDeviceStatus xmlns:u=\"urn:Belkin:service:bridge:1\">" + "<DeviceIDs>"
                    + wemoLightID + "</DeviceIDs>" + "</u:GetDeviceStatus>" + "</s:Body>" + "</s:Envelope>";

            String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
            wemoCallResponse = unescapeXml(wemoCallResponse);
            String response = substringBetween(wemoCallResponse, "<CapabilityValue>", "</CapabilityValue>");
            logger.trace("wemoNewLightState = {}", response);
            String[] splitResponse = response.split(",");
            if (splitResponse[0] != null) {
                OnOffType binaryState = null;
                binaryState = "0".equals(splitResponse[0]) ? OnOffType.OFF : OnOffType.ON;
                updateState(CHANNEL_STATE, binaryState);
            }
            if (splitResponse[1] != null) {
                String splitBrightness[] = splitResponse[1].split(":");
                if (splitBrightness[0] != null) {
                    int newBrightnessValue = Integer.valueOf(splitBrightness[0]);
                    int newBrightness = Math.round(newBrightnessValue * 100 / 255);
                    logger.trace("newBrightness = {}", newBrightness);
                    State newBrightnessState = new PercentType(newBrightness);
                    updateState(CHANNEL_BRIGHTNESS, newBrightnessState);
                    currentBrightness = newBrightness;
                }
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Could not retrieve new Wemo light state for '{}':", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.trace("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });
        String capabilityId = substringBetween(value, "<CapabilityId>", "</CapabilityId>");
        String newValue = substringBetween(value, "<Value>", "</Value>");
        switch (capabilityId) {
            case "10006":
                OnOffType binaryState = null;
                binaryState = "0".equals(newValue) ? OnOffType.OFF : OnOffType.ON;
                updateState(CHANNEL_STATE, binaryState);
                break;
            case "10008":
                String splitValue[] = newValue.split(":");
                if (splitValue[0] != null) {
                    int newBrightnessValue = Integer.valueOf(splitValue[0]);
                    int newBrightness = Math.round(newBrightnessValue * 100 / 255);
                    State newBrightnessState = new PercentType(newBrightness);
                    updateState(CHANNEL_BRIGHTNESS, newBrightnessState);
                    currentBrightness = newBrightness;
                }
                break;
        }
    }
}
