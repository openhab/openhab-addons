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
package org.openhab.binding.carnet.internal.handler;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.CarNetTextResources;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiErrorDTO;
import org.openhab.binding.carnet.internal.api.CarNetApiErrorDTO.CNErrorMessage2Details;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNOperationList.CarNetOperationList;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CNPairingInfo.CarNetPairingInfo;
import org.openhab.binding.carnet.internal.api.CarNetApiGSonDTO.CarNetServiceAvailability;
import org.openhab.binding.carnet.internal.api.CarNetApiResult;
import org.openhab.binding.carnet.internal.api.CarNetPendingRequest;
import org.openhab.binding.carnet.internal.api.brand.CarNetBrandApiAudi;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.openhab.binding.carnet.internal.config.CarNetVehicleConfiguration;
import org.openhab.binding.carnet.internal.provider.CarNetChannelTypeProvider;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper;
import org.openhab.binding.carnet.internal.provider.CarNetIChanneldMapper.ChannelIdMapEntry;
import org.openhab.binding.carnet.internal.services.CarNetVehicleBaseService;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceCarFinder;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceCharger;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceClimater;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceDestinations;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceRLU;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceStatus;
import org.openhab.binding.carnet.internal.services.CarNetVehicleServiceTripData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetVehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 *
 */
@NonNullByDefault
public class CarNetVehicleHandler extends BaseThingHandler implements CarNetDeviceListener {
    private final Logger logger = LoggerFactory.getLogger(CarNetVehicleHandler.class);
    private final CarNetTextResources resources;
    private final CarNetIChanneldMapper idMapper;
    private final Map<String, Object> channelData = new HashMap<>();
    private final CarNetChannelTypeProvider channelTypeProvider;
    private final ZoneId zoneId;

    public String thingId = "";
    private CarNetApiBase api = new CarNetBrandApiAudi();
    private @Nullable CarNetAccountHandler accountHandler;
    private @Nullable ScheduledFuture<?> pollingJob;
    private int updateCounter = 0;
    private int skipCount = 1;
    private boolean forceUpdate;
    private boolean channelsCreated = false;
    private boolean testData = false;

    private Map<String, CarNetVehicleBaseService> services = new LinkedHashMap<>();
    private CarNetServiceAvailability serviceAvailability = new CarNetServiceAvailability();
    private CarNetCombinedConfig config = new CarNetCombinedConfig();

    public CarNetVehicleHandler(Thing thing, CarNetTextResources resources, ZoneId zoneId,
            CarNetIChanneldMapper idMapper, CarNetChannelTypeProvider channelTypeProvider) throws CarNetException {
        super(thing);

        this.thingId = getThing().getUID().getId();
        this.resources = resources;
        this.idMapper = idMapper;
        this.channelTypeProvider = channelTypeProvider;
        this.zoneId = zoneId;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing!");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Initializing");
        scheduler.schedule(() -> {
            // Register listener and wait for account being ONLINE
            CarNetAccountHandler handler = null;
            Bridge bridge = getBridge();
            if (bridge != null) {
                handler = (CarNetAccountHandler) bridge.getHandler();
            }
            if ((handler == null)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "Account Thing is not initialized!");
                return;
            }
            accountHandler = handler;
            this.api = handler.createApi(config);

            handler.registerListener(this);
            setupPollingJob();
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * (re-)initialize the thing
     *
     * @return true=successful
     */
    boolean initializeThing() {
        channelData.clear(); // clear any cached channels
        boolean successful = true;
        String error = "";
        try {
            CarNetAccountHandler handler = accountHandler;
            if (handler == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                        "Account Handler not initialized!");
                return false;
            }
            config = handler.getCombinedConfig();
            config.vehicle = getConfigAs(CarNetVehicleConfiguration.class);
            Map<String, String> properties = getThing().getProperties();
            skipCount = Math.max(config.vehicle.pollingInterval * 60 / POLL_INTERVAL_SEC, 2);
            channelsCreated = false;

            String vin = "";
            if (properties.containsKey(PROPERTY_VIN)) {
                vin = properties.get(PROPERTY_VIN);
            }
            if ((vin == null) || vin.isEmpty()) {
                logger.info("VIN not set (Thing properties)");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "VIN not set (Thing properties)");
                return false;
            }
            config.vehicle.vin = vin.toUpperCase();
            api.setConfig(config); // required to pass VIN to CarNetApi
            config.homeRegionUrl = api.getHomeReguionUrl();
            config.apiUrlPrefix = api.getApiUrl();
            api.setConfig(config);

            serviceAvailability = new CarNetServiceAvailability(); // init all to true
            try {
                CarNetOperationList ol = api.getOperationList();
                config.user.id = ol.userId;
                config.user.role = ol.role;
                config.user.status = ol.status;
                config.user.securityLevel = ol.securityLevel;

                serviceAvailability = api.getServiceAvailability(ol);
                CarNetServiceAvailability sa = serviceAvailability;
                logger.debug(
                        "{}: Service availability: statusData: {}, tripData: {}, destinations: {}, carFinder: {}, climater: {}, charger: {}, remoteLock: {}",
                        thingId, sa.statusData, sa.tripData, sa.destinations, sa.carFinder, sa.clima, sa.charger,
                        sa.rlu);
                logger.debug("{}: Active userId = {}, role = {} (securityLevel {}), status = {}, Pairing Code {}",
                        thingId, config.user.id, ol.role, ol.securityLevel, ol.status, config.user.pairingCode);
            } catch (CarNetException e) {
                logger.debug("{}: Available services coould not be determined, continue with default profile", thingId);
            }

            try {
                CarNetPairingInfo pi = api.getPairingStatus();
                config.user.pairingCode = pi.pairingCode;
                if (!pi.isPairingCompleted()) {
                    logger.warn("{}: Pairing is not completed, use MMI to pair with code {}", thingId, pi.pairingCode);
                }
            } catch (CarNetException e) {
                logger.debug("{}: Unable to verify pairing status: {}", thingId, e.toString());
            }

            api.setConfig(config);

            if (logger.isDebugEnabled() && testData) {
                // Get available services
                String h = null, ts = null, df = null, poi = null, hr = null;
                try {
                    h = api.getHistory();
                } catch (Exception e) {
                }
                try {
                    ts = api.getTripStats("shortTerm");
                } catch (Exception e) {
                }
                try {
                    df = api.getMyDestinationsFeed(config.user.id);
                } catch (Exception e) {
                }
                try {
                    poi = api.getPois();
                } catch (Exception e) {
                }
                try {
                    hr = api.getVehicleHealthReport();
                } catch (Exception e) {
                }

                logger.debug(
                        "{}: Additional Data\nHistory:{}\nTrip stats short: {}\nMyDestinationsFeed: {}\nPOIs: {}\nHealth Report: {}\n",
                        thingId, h, ts, df, poi, hr);
                testData = false;
            }

            // Create services
            services.clear();
            addService(serviceAvailability.statusData, CNAPI_SERVICE_VEHICLE_STATUS_REPORT,
                    new CarNetVehicleServiceStatus(this, api));
            addService(serviceAvailability.carFinder, CNAPI_SERVICE_CAR_FINDER,
                    new CarNetVehicleServiceCarFinder(this, api));
            addService(serviceAvailability.rlu, CNAPI_SERVICE_REMOTE_LOCK_UNLOCK,
                    new CarNetVehicleServiceRLU(this, api));
            addService(serviceAvailability.charger, CNAPI_SERVICE_REMOTE_BATTERY_CHARGE,
                    new CarNetVehicleServiceCharger(this, api));
            addService(serviceAvailability.clima, CNAPI_SERVICE_REMOTE_PRETRIP_CLIMATISATION,
                    new CarNetVehicleServiceClimater(this, api));
            addService(serviceAvailability.tripData, CNAPI_SERVICE_REMOTE_TRIP_STATISTICS,
                    new CarNetVehicleServiceTripData(this, api));
            addService(serviceAvailability.destinations, CNAPI_SERVICE_DESTINATIONS,
                    new CarNetVehicleServiceDestinations(this, api));

            if (!channelsCreated) {
                // General channels
                Map<String, ChannelIdMapEntry> channels = new LinkedHashMap<>();
                addChannel(channels, CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_UPDATE, ITEMT_SWITCH, null, false, false);
                addChannel(channels, CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_LOCKED, ITEMT_SWITCH, null, false, true);
                addChannel(channels, CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_MAINTREQ, ITEMT_SWITCH, null, false, true);
                addChannel(channels, CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_WINCLOSED, ITEMT_SWITCH, null, false, true);
                addChannel(channels, CHANNEL_GROUP_STATUS, CHANNEL_GENERAL_TIRESOK, ITEMT_SWITCH, null, false, true);
                addChannel(channels, CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_UPDATED, ITEMT_DATETIME, null, false, true);

                // Add channels based on service information
                for (Map.Entry<String, CarNetVehicleBaseService> s : services.entrySet()) {
                    CarNetVehicleBaseService service = s.getValue();
                    if (!service.createChannels(channels)) {
                        logger.debug("{}: Service {} is not available, disable", thingId, service.getServiceId());
                        service.disable();
                    }
                }

                logger.debug("{}: Creating {} channels", thingId, channels.size());
                ArrayList<ChannelIdMapEntry> channelList = new ArrayList<>(channels.values());

                try (FileWriter myWriter = new FileWriter("carnetChannels.MD")) {
                    String lastGroup = "";
                    for (Map.Entry<String, ChannelIdMapEntry> m : channels.entrySet()) {
                        ChannelIdMapEntry e = m.getValue();
                        String group = lastGroup.equals(e.groupName) ? "" : e.groupName;
                        String s = String.format("| %-12.12s | %-23.23s | %-20.20s | %-7s | %-85s |\n", group,
                                e.channelName, e.itemType, e.readOnly ? "yes" : "no", e.getDescription());
                        myWriter.write(s);
                        lastGroup = e.groupName;
                    }
                } catch (IOException e) {
                }

                createChannels(channelList);
                channelsCreated = true;
            }
        } catch (CarNetException e) {
            CarNetApiErrorDTO res = e.getApiResult().getApiError();
            if (res.description.contains("disabled ")) {
                // Status service in the vehicle is disabled
                String message = "Status service is disabled, check data privacy settings in MMI: " + res;
                logger.debug("{}: {}", thingId, message);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, message);
                return false;
            }

            successful = false;
            error = getError(e);
        } catch (RuntimeException e) {
            error = "General Error: " + getString(e.getMessage());
            logger.warn("{}: {}", thingId, error, e);
        }

        if (!error.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
        } else {
            // updateStatus(ThingStatus.ONLINE);
        }
        return successful;
    }

    /**
     * Brigde status changed
     */
    @Override
    public void stateChanged(ThingStatus status, ThingStatusDetail detail, String message) {
        forceUpdate = true;
    }

    @Override
    public void informationUpdate(@Nullable List<CarNetVehicleInformation> vehicleList) {
        forceUpdate = true;
        channelsCreated = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }

        ThingStatus s = getThing().getStatus();
        if ((s == ThingStatus.INITIALIZING) || (s == ThingStatus.UNKNOWN)) {
            logger.info("{}: Thing not yet fully initialized, command ignored", thingId);
            forceUpdate = true;
            return;
        }

        String channelId = channelUID.getIdWithoutGroup();
        String error = "";
        boolean sendOffOnError = false;
        String action = "";
        String actionStatus = "";
        boolean switchOn = (command instanceof OnOffType) && (OnOffType) command == OnOffType.ON;
        try {
            switch (channelId) {
                case CHANNEL_CONTROL_UPDATE:
                    forceUpdate = true;
                    updateState(channelUID.getId(), OnOffType.OFF);
                    break;
                case CHANNEL_CONTROL_LOCK:
                    sendOffOnError = true;
                    action = switchOn ? "lock" : "unlock";
                    actionStatus = api.controlLock(switchOn);
                    break;
                case CHANNEL_CONTROL_CLIMATER:
                    sendOffOnError = true;
                    action = switchOn ? "startClimater" : "stopClimater";
                    actionStatus = api.controlClimater(switchOn);
                    break;
                case CHANNEL_CLIMATER_TARGET_TEMP:
                    actionStatus = api.controlClimaterTemp(((DecimalType) command).doubleValue());
                    break;
                case CHANNEL_CONTROL_CHARGER:
                    sendOffOnError = true;
                    action = switchOn ? "startCharging" : "stopCharging";
                    actionStatus = api.controlCharger(switchOn);
                    break;
                case CHANNEL_CONTROL_WINHEAT:
                    sendOffOnError = true;
                    action = switchOn ? "startWindowHeat" : "stopWindowHeat";
                    actionStatus = api.controlWindowHeating(switchOn);
                    break;
                case CHANNEL_CONTROL_PREHEAT:
                    sendOffOnError = true;
                    action = switchOn ? "startPreHeat" : "stopPreHeat";
                    actionStatus = api.controlPreHeating(switchOn);
                    break;
                case CHANNEL_CONTROL_VENT:
                    sendOffOnError = true;
                    action = switchOn ? "startVentilation" : "stopVentilation";
                    actionStatus = api.controlVentilation(switchOn, 15);
                    break;
                default:
                    logger.info("{}: Channel {} is unknown, command {} ignored", thingId, channelId, command);
                    break;
            }

            updateActionStatus(action, actionStatus);
            forceUpdate = true; // update on successful command
        } catch (CarNetException e) {
            CarNetApiErrorDTO res = e.getApiResult().getApiError();
            if (res.isOpAlreadyInProgress()) {
                logger.warn("{}: \"An operation is already in progress, request was rejected!\"", thingId);
            }
            if (e.isTooManyRequests()) {
                logger.warn("{}: API reported 'Too many requests', slow down updates", thingId);
            } else {
                error = getError(e);
                logger.warn("{}: {}", thingId, error.toString());
            }
        } catch (RuntimeException e) {
            error = "General Error: " + getString(e.getMessage());
            logger.warn("{}: {}", thingId, error, e);
        }

        if (!error.isEmpty()) {
            if (sendOffOnError) {
                updateState(channelUID.getId(), OnOffType.OFF);
            }
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
        }
    }

    private void updateActionStatus(String action, String actionStatus) {
        if (!actionStatus.isEmpty()) {
            if (!action.isEmpty()) {
                updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_ACTION, getStringType(action));
            }
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_ACTION_STATUS, getStringType(actionStatus));
            boolean inProgress = CNAPI_REQUEST_IN_PROGRESS.equalsIgnoreCase(actionStatus)
                    || CNAPI_REQUEST_QUEUED.equalsIgnoreCase(actionStatus);
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_ACTION_PENDING,
                    inProgress ? OnOffType.ON : OnOffType.OFF);
        }
    }

    /**
     * This routine is called every time the Thing configuration has been changed (e.g. PaperUI)
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("{}: Thing config updated.", thingId);
        super.handleConfigurationUpdate(configurationParameters);
        initializeThing();
    }

    private boolean updateVehicleStatus(boolean skipRefresh) throws CarNetException {
        if (!skipRefresh) {
            // check for pending refresh
            boolean pending = false;
            Map<String, CarNetPendingRequest> requests = api.getPendingRequests();
            for (Map.Entry<String, CarNetPendingRequest> e : requests.entrySet()) {
                CarNetPendingRequest request = e.getValue();
                if (CNAPI_SERVICE_VEHICLE_STATUS_REPORT.equalsIgnoreCase(request.service)) {
                    pending = true;
                }
            }
            if (!pending) {
                String status = api.refreshVehicleStatus();
                logger.debug("{}: Vehicle status refresh initiated, status={}", thingId, status);
            }
        }

        boolean updated = false;
        for (Map.Entry<String, CarNetVehicleBaseService> s : services.entrySet()) {
            updated |= s.getValue().update();
        }

        if (updated) {
            updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_UPDATED, getTimestamp(zoneId));
        }
        return updated;
    }

    public void checkPendingRequests() {
        Map<String, CarNetPendingRequest> requests = api.getPendingRequests();
        if (requests.size() > 0) {
            logger.debug("{}: Checking status for {} pending requets", thingId, requests.size());
            for (Map.Entry<String, CarNetPendingRequest> e : requests.entrySet()) {
                CarNetPendingRequest request = e.getValue();
                try {
                    request.status = api.getRequestStatus(request.requestId, "");
                } catch (CarNetException ex) {
                    CarNetApiErrorDTO error = ex.getApiResult().getApiError();
                    if (error.isTechValidationError()) {
                        // Id is no longer valid
                        request.status = CNAPI_REQUEST_ERROR;
                    }
                }

                if (!CNAPI_SERVICE_VEHICLE_STATUS_REPORT.equalsIgnoreCase(request.service)) {
                    updateActionStatus("", request.status);
                }
                if (!request.isInProgress()) {
                    updateChannel(CHANNEL_GROUP_GENERAL, CHANNEL_GENERAL_UPDATED, getTimestamp(zoneId));
                    forceUpdate = true; // refresh vehicle status
                }
            }
        }
    }

    /**
     * Sets up a polling job (using the scheduler) with the given interval.
     *
     * @param initialWaitTime The delay before the first refresh. Maybe 0 to immediately
     *            initiate a refresh.
     */
    private void setupPollingJob() {
        cancelPollingJob();
        logger.debug("Setting up polling job with an interval of {} seconds", config.vehicle.pollingInterval * 60);

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            ++updateCounter;
            if ((updateCounter % API_REQUEST_CHECK_INT) == 0) {
                // Check results for pending requests, remove expires ones from the list
                checkPendingRequests();
            }

            if (forceUpdate || (updateCounter % skipCount == 0)) {
                CarNetAccountHandler handler = accountHandler;
                if ((handler != null) && (handler.getThing().getStatus() == ThingStatus.ONLINE)) {
                    String error = "";
                    try {
                        ThingStatus s = getThing().getStatus();
                        boolean initialized = true;
                        boolean offline = (s == ThingStatus.UNKNOWN) || (s == ThingStatus.OFFLINE);
                        if (offline) {
                            initialized = initializeThing();
                        }
                        if (initialized) {
                            updateVehicleStatus(!offline && forceUpdate); // on success thing must be online
                            if (getThing().getStatus() != ThingStatus.ONLINE) {
                                logger.debug("{}: Thing is now online", thingId);
                                updateStatus(ThingStatus.ONLINE);
                            }
                        }
                    } catch (CarNetException e) {
                        if (e.isTooManyRequests() || e.isHttpNotModified()) {
                            logger.debug("{}: Status update failed, ignore temporary error (HTTP {})", thingId,
                                    e.getApiResult().httpCode);
                        } else {
                            error = getError(e);
                        }
                    } catch (RuntimeException e) {
                        error = "General Error: " + getString(e.getMessage());
                        logger.warn("{}: {}", thingId, error, e);
                    }

                    if (!error.isEmpty()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                    }
                }
                forceUpdate = false;
            }

        }, 1, POLL_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    private boolean createChannels(List<ChannelIdMapEntry> channels) {
        boolean created = false;

        ThingBuilder updatedThing = editThing();
        for (ChannelIdMapEntry channelDef : channels) {
            String channelId = channelDef.channelName;
            String groupId = channelDef.groupName;
            if (groupId.isEmpty()) {
                groupId = CHANNEL_GROUP_STATUS; // default group
            }
            String itemType = channelDef.itemType.isEmpty() ? ITEMT_NUMBER : channelDef.itemType;
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
            boolean cte = this.channelTypeProvider.channelTypeExists(channelTypeUID, null);
            // check if channelTypeUID exists in the registry, if not create it
            if (!cte) {
                logger.debug("{}: Channel type {} doesn't exist, creating", thingId, channelTypeUID.getAsString());
                ChannelType ct = ChannelTypeBuilder.state(channelTypeUID, channelTypeUID.getId(), itemType)
                        .withDescription("Auto-created for " + channelTypeUID.getId()).build();
                this.channelTypeProvider.addChannelType(ct);
            }

            if (getThing().getChannel(groupId + "#" + channelId) == null) {
                // the channel does not exist yet, so let's add it
                logger.debug("{}: Auto-creating channel {}#{}, type {}", thingId, groupId, channelId, itemType);
                String label = getChannelAttribute(channelId, "label");
                String description = getChannelAttribute(channelId, "description");
                if (label.isEmpty() || channelDef.itemType.isEmpty()) {
                    label = channelDef.symbolicName;
                }
                Channel channel = ChannelBuilder
                        .create(new ChannelUID(getThing().getUID(), mkChannelId(groupId, channelId)), itemType)
                        .withType(channelTypeUID).withLabel(label).withDescription(description)
                        .withKind(ChannelKind.STATE).build();
                updatedThing.withChannel(channel);
                created = true;
            }
        }

        updateThing(updatedThing.build());
        return created;
    }

    private String getReason(CarNetApiErrorDTO error) {
        CNErrorMessage2Details details = error.details;
        if (details != null) {
            return getString(details.reason);
        }
        return "";
    }

    private String getApiStatus(String errorMessage, String errorClass) {
        if (errorMessage.contains(errorClass)) {
            // extract the error code like VSR.security.9007
            String key = API_STATUS_MSG_PREFIX
                    + substringBefore(substringAfterLast(errorMessage, API_STATUS_CLASS_SECURUTY + "."), ")").trim();
            return resources.get(key);
        }
        return "";
    }

    private String getError(CarNetException e) {
        CarNetApiResult res = e.getApiResult();
        if (res.httpCode == HttpStatus.FORBIDDEN_403) {
            logger.info("{}: API Service is not available: ", thingId);
            return "";
        }

        String reason = "";
        CarNetApiErrorDTO error = e.getApiResult().getApiError();
        if (!error.isError()) {
            return getString(e.getMessage());
        } else {
            logger.info("{}: API Call failed: {}", thingId, getString(e.getMessage()));
            reason = getReason(error);
            if (!reason.isEmpty()) {
                logger.debug("{}: {}", thingId, reason);
            }
        }
        if (error.isSecurityClass()) {
            String message = getApiStatus(error.description, API_STATUS_CLASS_SECURUTY);
            logger.debug("{}: {}({})", thingId, message, error.description);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, message);
        }

        CarNetApiResult http = e.getApiResult();
        if (!http.isHttpOk() && !http.response.isEmpty()) {
            logger.debug("{}: HTTP response: {}", thingId, http.response);
        }
        String message = "";
        if (!error.code.isEmpty()) {
            String msgId = API_STATUS_MSG_PREFIX + "." + error.code;
            message = resources.get(msgId);
            if (message.equals(msgId)) {
                // No user friendly message for this code was found, so output the raw description
                message = message + " - " + error.description;
            }
        }
        logger.debug("{}: {}", thingId, message);
        return message;
    }

    private String getChannelAttribute(String channelId, String attribute) {
        String key = "channel-type.carnet." + channelId + "." + attribute;
        String value = resources.getText(key);
        return !value.equals(key) ? value : "";
    }

    private boolean addService(boolean add, String serviceId, CarNetVehicleBaseService service) {
        if (add && !services.containsKey(serviceId)) {
            services.put(serviceId, service);
            return true;
        }
        return false;
    }

    public boolean addChannel(Map<String, ChannelIdMapEntry> channels, String group, String channel, String itemType,
            @Nullable Unit<?> unit, boolean advanced, boolean readOnly) {
        if (!channels.containsKey(mkChannelId(group, channel))) {
            logger.debug("{}: Adding channel definition for channel {}", thingId, mkChannelId(group, channel));
            channels.put(mkChannelId(group, channel), idMapper.add(group, channel, itemType, unit, advanced, readOnly));
            return true;
        }
        return false;
    }

    public boolean updateChannel(String group, String channel, State value) {
        updateState(mkChannelId(group, channel), value);
        return true;
    }

    public boolean updateChannel(String group, String channel, State value, Unit<?> unit) {
        updateState(mkChannelId(group, channel), toQuantityType((Number) value, unit));
        return true;
    }

    public boolean updateChannel(String group, String channel, State value, int digits, Unit<?> unit) {
        updateState(mkChannelId(group, channel), toQuantityType(((DecimalType) value).doubleValue(), digits, unit));
        return true;
    }

    public boolean updateChannel(Channel channel, State value) {
        updateState(channel.getUID(), value);
        return true;
    }

    /**
     * Cancels the polling job (if one was setup).
     */
    private void cancelPollingJob() {
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(false);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler has been disposed");
        cancelPollingJob();
        super.dispose();
    }

    public CarNetCombinedConfig getThingConfig() {
        return config;
    }

    public CarNetIChanneldMapper getIdMapper() {
        return idMapper;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }
}
