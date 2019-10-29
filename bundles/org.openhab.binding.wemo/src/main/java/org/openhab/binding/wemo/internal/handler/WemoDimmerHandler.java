package org.openhab.binding.wemo.internal.handler;

/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;

import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */

public class WemoDimmerHandler extends AbstractWemoHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoDimmerHandler.class);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DIMMER);

    private Map<String, Boolean> subscriptionState = new HashMap<String, Boolean>();

    private Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    protected final static int SUBSCRIPTION_DURATION = 600;

    private UpnpIOService service;

    private int currentBrightness;

    private int currentNightModeBrightness;

    private String currentNightModeState = null;

    /**
     * Set dimming stepsize to 5%
     */
    private static final int DIM_STEPSIZE = 5;

    /**
     * The default refresh interval in Seconds.
     */
    private int DEFAULT_REFRESH_INTERVAL = 120;

    private ScheduledFuture<?> refreshJob;

    private Runnable refreshRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (!isUpnpDeviceRegistered()) {
                    logger.debug("WeMo UPnP device {} not yet registered", getUDN());
                }

                updateWemoState();
                onSubscription();

            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        }
    };

    public WemoDimmerHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemohttpCaller) {

        super(thing);

        this.wemoHttpCaller = wemohttpCaller;

        logger.debug("Creating a WemoDimmerHandler for thing '{}'", getThing().getUID());

        if (upnpIOService != null) {
            this.service = upnpIOService;
        } else {
            logger.debug("upnpIOService not set.");
        }

    }

    @Override
    public void initialize() {

        Configuration configuration = getConfig();

        if (configuration.get("udn") != null) {
            logger.debug("Initializing WemoDimmerHandler for UDN '{}'", configuration.get("udn"));
            service.registerParticipant(this);
            onSubscription();
            onUpdate();
        } else {
            logger.debug("Cannot initalize WemoDimmerHandler. UDN not set.");
        }

    }

    @Override
    public void dispose() {
        logger.debug("WeMoDimmerHandler disposed.");

        removeSubscription();

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command '{}' received for channel '{}'", command, channelUID);

        if (command instanceof RefreshType) {
            try {
                updateWemoState();
            } catch (Exception e) {
                logger.debug("Exception during poll : {}", e);
            }
        } else {

            String action = "SetBinaryState";
            String argument = null;
            String value = null;
            String timeStamp = null;

            switch (channelUID.getId()) {
                case CHANNEL_BRIGHTNESS:
                    argument = "brightness";
                    if (command instanceof PercentType) {
                        int newBrightness = ((PercentType) command).intValue();
                        value = String.valueOf(newBrightness);
                        currentBrightness = newBrightness;
                    } else if (command instanceof OnOffType) {
                        switch (command.toString()) {
                            case "ON":
                                value = "100";
                                break;
                            case "OFF":
                                value = "0";
                                break;
                        }
                    } else if (command instanceof IncreaseDecreaseType) {
                        int newBrightness;
                        switch (command.toString()) {
                            case "INCREASE":
                                newBrightness = currentBrightness + DIM_STEPSIZE;
                                if (newBrightness > 100) {
                                    newBrightness = 100;
                                }
                                value = String.valueOf(newBrightness);
                                currentBrightness = newBrightness;
                                break;
                            case "DECREASE":
                                newBrightness = currentBrightness - DIM_STEPSIZE;
                                if (newBrightness < 0) {
                                    newBrightness = 0;
                                }
                                value = String.valueOf(newBrightness);
                                currentBrightness = newBrightness;
                                break;
                        }
                    }
                    break;
                case CHANNEL_STATE:
                    argument = "BinaryState";
                    value = command.equals(OnOffType.OFF) ? "0" : "1";
                    if (command.equals(OnOffType.OFF)) {
                        updateState(CHANNEL_TIMERSTART, OnOffType.OFF);
                    }
                    break;
                case CHANNEL_FADERCOUNTDOWNTIME:
                    argument = "Fader";
                    if (command instanceof DecimalType) {
                        int commandValue = Integer.valueOf(String.valueOf(command));
                        commandValue = commandValue * 60;
                        String commandString = String.valueOf(commandValue);
                        value = "<BinaryState></BinaryState>" + "<Duration></Duration>" + "<EndAction></EndAction>"
                                + "<brightness></brightness>" + "<fader>" + commandString + ":-1:1:0:0</fader>"
                                + "<UDN></UDN>";
                    }
                    break;
                case CHANNEL_FADERENABLED:
                    argument = "Fader";

                    if (command.equals(OnOffType.ON)) {
                        value = "<BinaryState></BinaryState>" + "<Duration></Duration>" + "<EndAction></EndAction>"
                                + "<brightness></brightness>" + "<fader>600:-1:1:0:0</fader>" + "<UDN></UDN>";

                    } else if (command.equals(OnOffType.OFF)) {
                        value = "<BinaryState></BinaryState>" + "<Duration></Duration>" + "<EndAction></EndAction>"
                                + "<brightness></brightness>" + "<fader>600:-1:0:0:0</fader>" + "<UDN></UDN>";
                    }
                    break;
                case CHANNEL_TIMERSTART:
                    argument = "Fader";

                    long ts = System.currentTimeMillis() / 1000;
                    timeStamp = String.valueOf(ts);
                    logger.info("timestamp '{}' created", timeStamp);

                    String faderSeconds = null;
                    String faderEnabled = null;

                    String[] splitFader = this.stateMap.get("fader").split(":");

                    if (splitFader[0] != null) {
                        faderSeconds = splitFader[0];
                    }
                    if (splitFader[0] != null) {
                        faderEnabled = splitFader[2];
                    }

                    if (faderSeconds != null && faderEnabled != null) {
                        if (command.equals(OnOffType.ON)) {
                            value = "<BinaryState></BinaryState>" + "<Duration></Duration>" + "<EndAction></EndAction>"
                                    + "<brightness></brightness>" + "<fader>" + faderSeconds + ":" + timeStamp + ":"
                                    + faderEnabled + ":0:0</fader>" + "<UDN></UDN>";
                            updateState(CHANNEL_STATE, OnOffType.ON);

                        } else if (command.equals(OnOffType.OFF)) {
                            value = "<BinaryState></BinaryState>" + "<Duration></Duration>" + "<EndAction></EndAction>"
                                    + "<brightness></brightness>" + "<fader>" + faderSeconds + ":-1:" + faderEnabled
                                    + ":0:0</fader>" + "<UDN></UDN>";
                        }
                    }
                    break;
                case CHANNEL_NIGHTMODE:
                    action = "ConfigureNightMode";
                    argument = "NightModeConfiguration";
                    String nightModeBrightness = String.valueOf(currentNightModeBrightness);

                    if (command.equals(OnOffType.ON)) {
                        value = "&lt;startTime&gt;0&lt;/startTime&gt; \\n&lt;nightMode&gt;1&lt;/nightMode&gt; \\n&lt;endTime&gt;23400&lt;/endTime&gt; \\n&lt;nightModeBrightness&gt;"
                                + nightModeBrightness + "&lt;/nightModeBrightness&gt; \\n";
                    } else if (command.equals(OnOffType.OFF)) {
                        value = "&lt;startTime&gt;0&lt;/startTime&gt; \\n&lt;nightMode&gt;0&lt;/nightMode&gt; \\n&lt;endTime&gt;23400&lt;/endTime&gt; \\n&lt;nightModeBrightness&gt;"
                                + nightModeBrightness + "&lt;/nightModeBrightness&gt; \\n";
                    }
                    break;
                case CHANNEL_NIGHTMODEBRIGHTNESS:
                    action = "ConfigureNightMode";
                    argument = "NightModeConfiguration";

                    if (command instanceof PercentType) {
                        int newBrightness = ((PercentType) command).intValue();
                        String newNightModeBrightness = String.valueOf(newBrightness);
                        value = "&lt;startTime&gt;0&lt;/startTime&gt; \\n&lt;nightMode&gt;" + currentNightModeState
                                + "&lt;/nightMode&gt; \\n&lt;endTime&gt;23400&lt;/endTime&gt; \\n&lt;nightModeBrightness&gt;"
                                + newNightModeBrightness + "&lt;/nightModeBrightness&gt; \\n";

                        currentNightModeBrightness = newBrightness;
                    } else if (command instanceof IncreaseDecreaseType) {
                        int newBrightness;
                        String newNightModeBrightness = null;
                        switch (command.toString()) {
                            case "INCREASE":
                                newBrightness = currentNightModeBrightness + DIM_STEPSIZE;
                                if (newBrightness > 100) {
                                    newBrightness = 100;
                                }
                                newNightModeBrightness = String.valueOf(newBrightness);
                                currentBrightness = newBrightness;
                                break;
                            case "DECREASE":
                                newBrightness = currentNightModeBrightness - DIM_STEPSIZE;
                                if (newBrightness < 0) {
                                    newBrightness = 0;
                                }
                                newNightModeBrightness = String.valueOf(newBrightness);
                                currentNightModeBrightness = newBrightness;
                                break;
                        }
                        value = "&lt;startTime&gt;0&lt;/startTime&gt; \\n&lt;nightMode&gt;" + currentNightModeState
                                + "&lt;/nightMode&gt; \\n&lt;endTime&gt;23400&lt;/endTime&gt; \\n&lt;nightModeBrightness&gt;"
                                + newNightModeBrightness + "&lt;/nightModeBrightness&gt; \\n";
                    }
                    break;
            }

            if (argument != null && value != null) {

                try {

                    String soapHeader = "\"urn:Belkin:service:basicevent:1#" + action + "\"";

                    String content = "<?xml version=\"1.0\"?>"
                            + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                            + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:basicevent:1\">" + "<"
                            + argument + ">" + value + "</" + argument + ">" + "</u:" + action + ">" + "</s:Body>"
                            + "</s:Envelope>";

                    if (channelUID.getId().equals(CHANNEL_TIMERSTART)) {
                        content = "<?xml version=\"1.0\"?>"
                                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:basicevent:1\">" + value
                                + "</u:" + action + ">" + "</s:Body>" + "</s:Envelope>";
                    }

                    String wemoURL = getWemoURL("basicevent");

                    if (wemoURL != null) {
                        logger.info("About to send content to Dimmer {}", content);
                        wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                    }
                } catch (Exception e) {
                    logger.error("Failed to send command '{}' for device '{}': {}", command, getThing().getUID(),
                            e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
                updateStatus(ThingStatus.ONLINE);

            }

        }

    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
        logger.debug("WeMo {}: Subscription to service {} {}", getUDN(), service, succeeded ? "succeeded" : "failed");
        subscriptionState.put(service, succeeded);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {

        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });

        updateStatus(ThingStatus.ONLINE);

        this.stateMap.put(variable, value);

        switch (variable) {
            case "BinaryState":
                State state = value.equals("0") ? OnOffType.OFF : OnOffType.ON;
                logger.info("State '{}' for device '{}' received", state, getThing().getUID());
                updateState(CHANNEL_STATE, state);
                if (state.equals(OnOffType.OFF)) {
                    updateState(CHANNEL_TIMERSTART, OnOffType.OFF);
                }
                break;
            case "brightness":
                logger.info("brightness '{}' for device '{}' received", value, getThing().getUID());

                int newBrightnessValue = Integer.valueOf(value);
                State newBrightnessState = new PercentType(newBrightnessValue);
                updateState(CHANNEL_BRIGHTNESS, newBrightnessState);
                currentBrightness = newBrightnessValue;
                break;
            case "fader":
                logger.info("fader '{}' for device '{}' received", value, getThing().getUID());
                String[] splitFader = value.split(":");
                if (splitFader[0] != null) {
                    int faderSeconds = Integer.valueOf(splitFader[0]);
                    State faderMinutes = new DecimalType(faderSeconds / 60);
                    logger.info("faderTime '{} minutes' for device '{}' received", faderMinutes, getThing().getUID());
                    updateState(CHANNEL_FADERCOUNTDOWNTIME, faderMinutes);
                }
                if (splitFader[1] != null) {
                    State isTimerRunning = splitFader[1].equals("-1") ? OnOffType.OFF : OnOffType.ON;
                    logger.info("isTimerRunning '{}' for device '{}' received", isTimerRunning, getThing().getUID());
                    updateState(CHANNEL_TIMERSTART, isTimerRunning);
                    if (isTimerRunning.equals(OnOffType.ON)) {
                        updateState(CHANNEL_STATE, OnOffType.ON);

                    }
                }
                if (splitFader[2] != null) {
                    State isFaderEnabled = splitFader[1].equals("0") ? OnOffType.OFF : OnOffType.ON;
                    logger.info("isFaderEnabled '{}' for device '{}' received", isFaderEnabled, getThing().getUID());
                    updateState(CHANNEL_FADERENABLED, isFaderEnabled);
                }
                break;
            case "nightMode":
                State nightModeState = value.equals("0") ? OnOffType.OFF : OnOffType.ON;
                currentNightModeState = value;
                logger.info("State '{}' for device '{}' received", nightModeState, getThing().getUID());
                updateState(CHANNEL_NIGHTMODE, nightModeState);
                break;
            case "startTime":
                State startTimeState = getDateTimeState(value);
                logger.info("State '{}' for device '{}' received", startTimeState, getThing().getUID());
                updateState(CHANNEL_STARTTIME, startTimeState);
                break;
            case "endTime":
                State endTimeState = getDateTimeState(value);
                logger.info("State '{}' for device '{}' received", endTimeState, getThing().getUID());
                updateState(CHANNEL_ENDTIME, endTimeState);
                break;
            case "nightModeBrightness":
                int nightModeBrightnessValue = Integer.valueOf(value);
                currentNightModeBrightness = nightModeBrightnessValue;
                State nightModeBrightnessState = new PercentType(nightModeBrightnessValue);
                logger.info("State '{}' for device '{}' received", nightModeBrightnessState, getThing().getUID());
                updateState(CHANNEL_NIGHTMODEBRIGHTNESS, nightModeBrightnessState);
                break;

        }
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Checking WeMo GENA subscription for '{}'", this);

            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) == null) || !subscriptionState.get(subscription).booleanValue()) {
                logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(), subscription);
                service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                subscriptionState.put(subscription, true);
            }

        } else {
            logger.debug("Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                    this);
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);

        if (service.isRegistered(this)) {
            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) != null) && subscriptionState.get(subscription).booleanValue()) {
                logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                service.removeSubscription(this, subscription);
            }

            subscriptionState = new HashMap<String, Boolean>();
            service.unregisterParticipant(this);
        }
    }

    private synchronized void onUpdate() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Configuration config = getThing().getConfiguration();
            int refreshInterval = DEFAULT_REFRESH_INTERVAL;
            Object refreshConfig = config.get("refresh");
            if (refreshConfig != null) {
                refreshInterval = ((BigDecimal) refreshConfig).intValue();
            }
            refreshJob = scheduler.scheduleAtFixedRate(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    private boolean isUpnpDeviceRegistered() {
        return service.isRegistered(this);
    }

    @Override
    public String getUDN() {
        return (String) this.getThing().getConfiguration().get(UDN);
    }

    /**
     * The {@link updateWemoState} polls the actual state of a WeMo device and
     * calls {@link onValueReceived} to update the statemap and channels..
     *
     */
    protected void updateWemoState() {

        String action = "GetBinaryState";
        String variable = null;
        String actionService = "basicevent";
        String value = null;

        String soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        String content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:"
                + action + ">" + "</s:Body>" + "</s:Envelope>";

        try {
            String wemoURL = getWemoURL(actionService);
            if (wemoURL != null) {
                String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    logger.trace("State response '{}' for device '{}' received", wemoCallResponse, getThing().getUID());
                    value = StringUtils.substringBetween(wemoCallResponse, "<BinaryState>", "</BinaryState>");
                    if (value != null) {
                        variable = "BinaryState";
                        logger.trace("New state '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<brightness>", "</brightness>");
                    if (value != null) {
                        variable = "brightness";
                        logger.trace("New brightness '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<fader>", "</fader>");
                    if (value != null) {
                        variable = "fader";
                        logger.trace("New fader value '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get actual state for device '{}': {}", getThing().getUID(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        updateStatus(ThingStatus.ONLINE);

        action = "GetNightModeConfiguration";
        variable = null;
        value = null;

        soapHeader = "\"urn:Belkin:service:" + actionService + ":1#" + action + "\"";
        content = "<?xml version=\"1.0\"?>"
                + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                + "<s:Body>" + "<u:" + action + " xmlns:u=\"urn:Belkin:service:" + actionService + ":1\">" + "</u:"
                + action + ">" + "</s:Body>" + "</s:Envelope>";

        try {
            String wemoURL = getWemoURL(actionService);
            if (wemoURL != null) {
                String wemoCallResponse = wemoHttpCaller.executeCall(wemoURL, soapHeader, content);
                if (wemoCallResponse != null) {
                    logger.info("GetNightModeConfiguration response '{}' for device '{}' received", wemoCallResponse,
                            getThing().getUID());
                    value = StringUtils.substringBetween(wemoCallResponse, "<startTime>", "</startTime>");
                    if (value != null) {
                        variable = "startTime";
                        logger.trace("New startTime '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<endTime>", "</endTime>");
                    if (value != null) {
                        variable = "endTime";
                        logger.trace("New endTime '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<nightMode>", "</nightMode>");
                    if (value != null) {
                        variable = "nightMode";
                        logger.trace("New nightMode state '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                    value = StringUtils.substringBetween(wemoCallResponse, "<nightModeBrightness>",
                            "</nightModeBrightness>");
                    if (value != null) {
                        variable = "nightModeBrightness";
                        logger.trace("New nightModeBrightness  '{}' for device '{}' received", value,
                                getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get actual NightMode state for device '{}': {}", getThing().getUID(),
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    public String getWemoURL(String actionService) {
        URL descriptorURL = service.getDescriptorURL(this);
        String wemoURL = null;
        if (descriptorURL != null) {
            String deviceURL = StringUtils.substringBefore(descriptorURL.toString(), "/setup.xml");
            wemoURL = deviceURL + "/upnp/control/" + actionService + "1";
            return wemoURL;
        }
        return null;
    }

    @SuppressWarnings("null")
    public State getDateTimeState(String attributeValue) {
        if (attributeValue != null) {
            long value = 0;
            try {
                value = Long.parseLong(attributeValue) * 1000; // convert s to ms
            } catch (NumberFormatException e) {
                logger.error("Unable to parse attributeValue '{}' for device '{}'; expected long", attributeValue,
                        getThing().getUID());
                return null;
            }
            ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value),
                    TimeZone.getDefault().toZoneId());
            State dateTimeState = new DateTimeType(zoned);
            if (dateTimeState != null) {
                logger.trace("New attribute '{}' received", dateTimeState);
                return dateTimeState;
            }
        }
        return null;
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

}
