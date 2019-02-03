/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * The {@link WemoHandler} is responsible for handling commands, which are
 * sent to one of the channels and to update their states.
 *
 * @author Hans-Jörg Merk - Initial contribution; Added support for WeMo Insight energy measurement
 * @author Kai Kreuzer - some refactoring for performance and simplification
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Mihir Patil - Added standby switch
 */

public class WemoHandler extends AbstractWemoHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(WemoHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream
            .of(THING_TYPE_SOCKET, THING_TYPE_INSIGHT, THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION)
            .collect(Collectors.toSet());

    private Map<String, Boolean> subscriptionState = new HashMap<String, Boolean>();

    private final Map<String, String> stateMap = Collections.synchronizedMap(new HashMap<String, String>());

    // protected static final int SUBSCRIPTION_DURATION = WemoBindingConstants.SUBSCRIPTION_DURATION;

    private UpnpIOService service;

    /**
     * The default refresh interval in Seconds.
     */
    private final int DEFAULT_REFRESH_INTERVAL = 120;

    private ScheduledFuture<?> refreshJob;

    private final Runnable refreshRunnable = new Runnable() {

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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    public WemoHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemohttpCaller) {
        super(thing);

        this.wemoHttpCaller = wemohttpCaller;

        logger.debug("Creating a WemoHandler for thing '{}'", getThing().getUID());

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
            logger.debug("Initializing WemoHandler for UDN '{}'", configuration.get("udn"));
            onSubscription();
            onUpdate();
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoHandler. UDN not set.");
        }
    }

    @Override
    public void dispose() {
        logger.debug("WeMoHandler disposed.");

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
        } else if (channelUID.getId().equals(CHANNEL_STATE)) {
            if (command instanceof OnOffType) {
                try {
                    String binaryState = null;

                    if (command.equals(OnOffType.ON)) {
                        binaryState = "1";
                    } else if (command.equals(OnOffType.OFF)) {
                        binaryState = "0";
                    }

                    String soapHeader = "\"urn:Belkin:service:basicevent:1#SetBinaryState\"";

                    String content = "<?xml version=\"1.0\"?>"
                            + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                            + "<s:Body>" + "<u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">"
                            + "<BinaryState>" + binaryState + "</BinaryState>" + "</u:SetBinaryState>" + "</s:Body>"
                            + "</s:Envelope>";

                    String wemoURL = getWemoURL("basicevent");

                    if (wemoURL != null) {
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

        if (getThing().getThingTypeUID().getId().equals("insight")) {
            String insightParams = stateMap.get("InsightParams");

            if (insightParams != null) {
                String[] splitInsightParams = insightParams.split("\\|");

                if (splitInsightParams[0] != null) {
                    OnOffType binaryState = null;
                    binaryState = splitInsightParams[0].equals("0") ? OnOffType.OFF : OnOffType.ON;
                    if (binaryState != null) {
                        logger.trace("New InsightParam binaryState '{}' for device '{}' received", binaryState,
                                getThing().getUID());
                        updateState(CHANNEL_STATE, binaryState);
                    }
                }

                long lastChangedAt = 0;
                try {
                    lastChangedAt = Long.parseLong(splitInsightParams[1]) * 1000; // convert s to ms
                } catch (NumberFormatException e) {
                    logger.error("Unable to parse lastChangedAt value '{}' for device '{}'; expected long",
                            splitInsightParams[1], getThing().getUID());
                }
                ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastChangedAt),
                        TimeZone.getDefault().toZoneId());

                State lastChangedAtState = new DateTimeType(zoned);
                if (lastChangedAt != 0) {
                    logger.trace("New InsightParam lastChangedAt '{}' for device '{}' received", lastChangedAtState,
                            getThing().getUID());
                    updateState(CHANNEL_LASTCHANGEDAT, lastChangedAtState);
                }

                State lastOnFor = DecimalType.valueOf(splitInsightParams[2]);
                if (lastOnFor != null) {
                    logger.trace("New InsightParam lastOnFor '{}' for device '{}' received", lastOnFor,
                            getThing().getUID());
                    updateState(CHANNEL_LASTONFOR, lastOnFor);
                }

                State onToday = DecimalType.valueOf(splitInsightParams[3]);
                if (onToday != null) {
                    logger.trace("New InsightParam onToday '{}' for device '{}' received", onToday,
                            getThing().getUID());
                    updateState(CHANNEL_ONTODAY, onToday);
                }

                State onTotal = DecimalType.valueOf(splitInsightParams[4]);
                if (onTotal != null) {
                    logger.trace("New InsightParam onTotal '{}' for device '{}' received", onTotal,
                            getThing().getUID());
                    updateState(CHANNEL_ONTOTAL, onTotal);
                }

                State timespan = DecimalType.valueOf(splitInsightParams[5]);
                if (timespan != null) {
                    logger.trace("New InsightParam timespan '{}' for device '{}' received", timespan,
                            getThing().getUID());
                    updateState(CHANNEL_TIMESPAN, timespan);
                }

                State averagePower = DecimalType.valueOf(splitInsightParams[6]); // natively given in W
                if (averagePower != null) {
                    logger.trace("New InsightParam averagePower '{}' for device '{}' received", averagePower,
                            getThing().getUID());
                    updateState(CHANNEL_AVERAGEPOWER, averagePower);
                }

                BigDecimal currentMW = new BigDecimal(splitInsightParams[7]);
                State currentPower = new DecimalType(currentMW.divide(new BigDecimal(1000), RoundingMode.HALF_UP)); // recalculate
                // mW to W
                if (currentPower != null) {
                    logger.trace("New InsightParam currentPower '{}' for device '{}' received", currentPower,
                            getThing().getUID());
                    updateState(CHANNEL_CURRENTPOWER, currentPower);
                }

                BigDecimal energyTodayMWMin = new BigDecimal(splitInsightParams[8]);
                // recalculate mW-mins to Wh
                State energyToday = new DecimalType(
                        energyTodayMWMin.divide(new BigDecimal(60000), RoundingMode.HALF_UP));
                if (energyToday != null) {
                    logger.trace("New InsightParam energyToday '{}' for device '{}' received", energyToday,
                            getThing().getUID());
                    updateState(CHANNEL_ENERGYTODAY, energyToday);
                }

                BigDecimal energyTotalMWMin = new BigDecimal(splitInsightParams[9]);
                // recalculate mW-mins to Wh
                State energyTotal = new DecimalType(
                        energyTotalMWMin.divide(new BigDecimal(60000), RoundingMode.HALF_UP));
                if (energyTotal != null) {
                    logger.trace("New InsightParam energyTotal '{}' for device '{}' received", energyTotal,
                            getThing().getUID());
                    updateState(CHANNEL_ENERGYTOTAL, energyTotal);
                }

                BigDecimal standByLimitMW = new BigDecimal(splitInsightParams[10]);
                State standByLimit = new DecimalType(standByLimitMW.divide(new BigDecimal(1000), RoundingMode.HALF_UP)); // recalculate
                // mW to W
                if (standByLimit != null) {
                    logger.trace("New InsightParam standByLimit '{}' for device '{}' received", standByLimit,
                            getThing().getUID());
                    updateState(CHANNEL_STANDBYLIMIT, standByLimit);
                }
                if (currentMW.divide(new BigDecimal(1000), RoundingMode.HALF_UP).intValue() > standByLimitMW.divide(new BigDecimal(1000), RoundingMode.HALF_UP).intValue()){
                    updateState(CHANNEL_ONSTANDBY, OnOffType.OFF);
                }
                else{
                    updateState(CHANNEL_ONSTANDBY, OnOffType.ON);
                }
            }
        } else {
            State state = stateMap.get("BinaryState").equals("0") ? OnOffType.OFF : OnOffType.ON;

            logger.debug("State '{}' for device '{}' received", state, getThing().getUID());

            if (state != null) {
                if (getThing().getThingTypeUID().getId().equals("motion")) {
                    updateState(CHANNEL_MOTIONDETECTION, state);
                    if (state.equals(OnOffType.ON)) {
                        State lastMotionDetected = new DateTimeType();
                        updateState(CHANNEL_LASTMOTIONDETECTED, lastMotionDetected);
                    }
                } else {
                    updateState(CHANNEL_STATE, state);
                }
            }
        }
    }

    private synchronized void onSubscription() {
        if (service.isRegistered(this)) {
            logger.debug("Checking WeMo GENA subscription for '{}'", this);

            ThingTypeUID thingTypeUID = thing.getThingTypeUID();
            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) == null) || !subscriptionState.get(subscription).booleanValue()) {
                logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(), subscription);
                service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                subscriptionState.put(subscription, true);
            }

            if (thingTypeUID.equals(THING_TYPE_INSIGHT)) {
                subscription = "insight1";
                if ((subscriptionState.get(subscription) == null)
                        || !subscriptionState.get(subscription).booleanValue()) {
                    logger.debug("Setting up GENA subscription {}: Subscribing to service {}...", getUDN(),
                            subscription);
                    service.addSubscription(this, subscription, SUBSCRIPTION_DURATION);
                    subscriptionState.put(subscription, true);
                }
            }
        } else {
            logger.debug("Setting up WeMo GENA subscription for '{}' FAILED - service.isRegistered(this) is FALSE",
                    this);
        }
    }

    private synchronized void removeSubscription() {
        logger.debug("Removing WeMo GENA subscription for '{}'", this);

        if (service.isRegistered(this)) {
            ThingTypeUID thingTypeUID = thing.getThingTypeUID();
            String subscription = "basicevent1";

            if ((subscriptionState.get(subscription) != null) && subscriptionState.get(subscription).booleanValue()) {
                logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                service.removeSubscription(this, subscription);
            }

            if (thingTypeUID.equals(THING_TYPE_INSIGHT)) {
                subscription = "insight1";
                if ((subscriptionState.get(subscription) != null)
                        && subscriptionState.get(subscription).booleanValue()) {
                    logger.debug("WeMo {}: Unsubscribing from service {}...", getUDN(), subscription);
                    service.removeSubscription(this, subscription);
                }
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
            refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 0, refreshInterval, TimeUnit.SECONDS);
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
        String variable = "BinaryState";
        String actionService = "basicevent";
        String value = null;

        if (getThing().getThingTypeUID().getId().equals("insight")) {
            action = "GetInsightParams";
            variable = "InsightParams";
            actionService = "insight";
        }

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
                    if (variable.equals("InsightParams")) {
                        value = StringUtils.substringBetween(wemoCallResponse, "<InsightParams>", "</InsightParams>");
                    } else {
                        value = StringUtils.substringBetween(wemoCallResponse, "<BinaryState>", "</BinaryState>");
                    }
                    if (value != null) {
                        logger.trace("New state '{}' for device '{}' received", value, getThing().getUID());
                        this.onValueReceived(variable, value, actionService + "1");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to get actual state for device '{}': {}", getThing().getUID(), e.getMessage());
        }
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

    @Override
    public void onStatusChanged(boolean status) {
    }

}
