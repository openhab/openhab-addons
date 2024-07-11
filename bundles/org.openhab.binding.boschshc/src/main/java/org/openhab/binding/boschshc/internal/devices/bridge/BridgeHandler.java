/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.eclipse.jetty.http.HttpMethod.GET;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static org.eclipse.jetty.http.HttpMethod.PUT;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.devices.BoschDeviceIdUtils;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Message;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Room;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.binding.boschshc.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.LongPollingFailedException;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;
import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;
import org.openhab.binding.boschshc.internal.services.dto.JsonRestExceptionResponse;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Representation of a connection with a Bosch Smart Home Controller bridge.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Gerd Zanker - added HttpClient with pairing support
 * @author Christian Oeing - refactorings of e.g. server registration
 * @author David Pace - Added support for custom endpoints and HTTP POST requests
 * @author Gerd Zanker - added thing discovery
 */
@NonNullByDefault
public class BridgeHandler extends BaseBridgeHandler {

    private static final String HTTP_CLIENT_NOT_INITIALIZED = "HttpClient not initialized";

    private final Logger logger = LoggerFactory.getLogger(BridgeHandler.class);

    /**
     * Handler to do long polling.
     */
    private final LongPolling longPolling;

    /**
     * HTTP client for all communications to and from the bridge.
     * <p>
     * This member is package-protected to enable mocking in unit tests.
     */
    /* package */ @Nullable
    BoschHttpClient httpClient;

    private @Nullable ScheduledFuture<?> scheduledPairing;

    /**
     * SHC thing/device discovery service instance.
     * Registered and unregistered if service is actived/deactived.
     * Used to scan for things after bridge is paired with SHC.
     */
    private @Nullable ThingDiscoveryService thingDiscoveryService;

    private final ScenarioHandler scenarioHandler;

    public BridgeHandler(Bridge bridge) {
        super(bridge);
        scenarioHandler = new ScenarioHandler();

        this.longPolling = new LongPolling(this.scheduler, this::handleLongPollResult, this::handleLongPollFailure);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ThingDiscoveryService.class);
    }

    @Override
    public void initialize() {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        if (bundle != null) {
            logger.debug("Initialize {} Version {}", bundle.getSymbolicName(), bundle.getVersion());
        }

        // Read configuration
        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);

        String ipAddress = config.ipAddress.trim();
        if (ipAddress.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-empty-ip");
            return;
        }

        String password = config.password.trim();
        if (password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-empty-password");
            return;
        }

        SslContextFactory factory;
        try {
            // prepare SSL key and certificates
            factory = new BoschSslUtil(ipAddress).getSslContextFactory();
        } catch (PairingFailedException e) {
            logger.debug("Error while obtaining SSL context factory.", e);
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-ssl");
            return;
        }

        // Instantiate HttpClient with the SslContextFactory
        BoschHttpClient localHttpClient = this.httpClient = new BoschHttpClient(ipAddress, password, factory);

        // Start http client
        try {
            localHttpClient.start();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Could not create http connection to controller: %s", e.getMessage()));
            return;
        }

        // general checks are OK, therefore set the status to unknown and wait for initial access
        this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE);

        // Initialize bridge in the background.
        // Start initial access the first time
        scheduleInitialAccess(localHttpClient);
    }

    @Override
    public void dispose() {
        // Cancel scheduled pairing.
        @Nullable
        ScheduledFuture<?> localScheduledPairing = this.scheduledPairing;
        if (localScheduledPairing != null) {
            localScheduledPairing.cancel(true);
            this.scheduledPairing = null;
        }

        // Stop long polling.
        this.longPolling.stop();

        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient != null) {
            try {
                localHttpClient.stop();
            } catch (Exception e) {
                logger.debug("HttpClient failed on bridge disposal: {}", e.getMessage(), e);
            }
            this.httpClient = null;
        }

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // commands are handled by individual device handlers
        BoschHttpClient localHttpClient = httpClient;
        if (BoschSHCBindingConstants.CHANNEL_TRIGGER_SCENARIO.equals(channelUID.getId())
                && !RefreshType.REFRESH.equals(command) && localHttpClient != null) {
            scenarioHandler.triggerScenario(localHttpClient, command.toString());
        }
    }

    /**
     * Schedule the initial access.
     * Use a delay if pairing fails and next retry is scheduled.
     */
    private void scheduleInitialAccess(BoschHttpClient httpClient) {
        this.scheduledPairing = scheduler.schedule(() -> initialAccess(httpClient), 15, TimeUnit.SECONDS);
    }

    /**
     * Execute the initial access.
     * Uses the HTTP Bosch SHC client
     * to check if access if possible
     * pairs this Bosch SHC Bridge with the SHC if necessary
     * and starts the first log poll.
     * <p>
     * This method is package-protected to enable unit testing.
     */
    /* package */ void initialAccess(BoschHttpClient httpClient) {
        logger.debug("Initializing Bosch SHC Bridge: {} - HTTP client is: {}", this, httpClient);

        try {
            // check if SCH is offline
            if (!httpClient.isOnline()) {
                // update status already if access is not possible
                this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                        "@text/offline.conf-error-offline");
                // restart later initial access
                scheduleInitialAccess(httpClient);
                return;
            }

            // SHC is online
            // check if SHC access is not possible and pairing necessary
            if (!httpClient.isAccessPossible()) {
                // update status description to show pairing test
                this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                        "@text/offline.conf-error-pairing");
                if (!httpClient.doPairing()) {
                    this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "@text/offline.conf-error-pairing");
                }
                // restart initial access - needed also in case of successful pairing to check access again
                scheduleInitialAccess(httpClient);
                return;
            }

            // SHC is online and access should possible
            if (!checkBridgeAccess()) {
                this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        "@text/offline.not-reachable");
                // restart initial access
                scheduleInitialAccess(httpClient);
                return;
            }

            // do thing discovery after pairing
            final ThingDiscoveryService discovery = thingDiscoveryService;
            if (discovery != null) {
                discovery.doScan();
            }

            // start long polling loop
            this.updateStatus(ThingStatus.ONLINE);
            startLongPolling(httpClient);

        } catch (InterruptedException e) {
            this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE, "@text/offline.interrupted");
            Thread.currentThread().interrupt();
        }
    }

    private void startLongPolling(BoschHttpClient httpClient) {
        try {
            this.longPolling.start(httpClient);
        } catch (LongPollingFailedException e) {
            this.handleLongPollFailure(e);
        }
    }

    /**
     * Check the bridge access by sending an HTTP request.
     * Does not throw any exception in case the request fails.
     */
    public boolean checkBridgeAccess() throws InterruptedException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;

        if (localHttpClient == null) {
            return false;
        }

        try {
            logger.debug("Sending http request to BoschSHC to check access: {}", localHttpClient);
            String url = localHttpClient.getBoschSmartHomeUrl("devices");
            ContentResponse contentResponse = localHttpClient.createRequest(url, GET).send();

            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Access check failed with status code: {}", contentResponse.getStatus());
                return false;
            }

            // Access OK
            return true;
        } catch (TimeoutException | ExecutionException e) {
            logger.warn("Access check failed because of {}!", e.getMessage());
            return false;
        }
    }

    /**
     * Get a list of connected devices from the Smart-Home Controller
     *
     * @throws InterruptedException in case bridge is stopped
     */
    public List<Device> getDevices() throws InterruptedException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            return Collections.emptyList();
        }

        try {
            logger.trace("Sending http request to Bosch to request devices: {}", localHttpClient);
            String url = localHttpClient.getBoschSmartHomeUrl("devices");
            ContentResponse contentResponse = localHttpClient.createRequest(url, GET).send();

            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Request devices failed with status code: {}", contentResponse.getStatus());
                return Collections.emptyList();
            }

            String content = contentResponse.getContentAsString();
            logger.trace("Request devices completed with success: {} - status code: {}", content,
                    contentResponse.getStatus());

            Type collectionType = new TypeToken<ArrayList<Device>>() {
            }.getType();
            List<Device> nullableDevices = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(content, collectionType);
            return nullableDevices != null ? nullableDevices : Collections.emptyList();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Request devices failed because of {}!", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<UserDefinedState> getUserStates() throws InterruptedException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            return List.of();
        }

        try {
            logger.trace("Sending http request to Bosch to request user-defined states: {}", localHttpClient);
            String url = localHttpClient.getBoschSmartHomeUrl("userdefinedstates");
            ContentResponse contentResponse = localHttpClient.createRequest(url, GET).send();

            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Request devices failed with status code: {}", contentResponse.getStatus());
                return List.of();
            }

            String content = contentResponse.getContentAsString();
            logger.trace("Request devices completed with success: {} - status code: {}", content,
                    contentResponse.getStatus());

            Type collectionType = new TypeToken<ArrayList<UserDefinedState>>() {
            }.getType();
            List<UserDefinedState> nullableUserStates = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(content,
                    collectionType);
            return nullableUserStates != null ? nullableUserStates : Collections.emptyList();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("Request user-defined states failed because of {}!", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get a list of rooms from the Smart-Home controller
     *
     * @throws InterruptedException in case bridge is stopped
     */
    public List<Room> getRooms() throws InterruptedException {
        List<Room> emptyRooms = new ArrayList<>();
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient != null) {
            try {
                logger.trace("Sending http request to Bosch to request rooms");
                String url = localHttpClient.getBoschSmartHomeUrl("rooms");
                ContentResponse contentResponse = localHttpClient.createRequest(url, GET).send();

                // check HTTP status code
                if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                    logger.debug("Request rooms failed with status code: {}", contentResponse.getStatus());
                    return emptyRooms;
                }

                String content = contentResponse.getContentAsString();
                logger.trace("Request rooms completed with success: {} - status code: {}", content,
                        contentResponse.getStatus());

                Type collectionType = new TypeToken<ArrayList<Room>>() {
                }.getType();

                ArrayList<Room> rooms = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(content, collectionType);
                return Objects.requireNonNullElse(rooms, emptyRooms);
            } catch (TimeoutException | ExecutionException e) {
                logger.debug("Request rooms failed because of {}!", e.getMessage());
                return emptyRooms;
            }
        } else {
            return emptyRooms;
        }
    }

    /**
     * Get public information from Bosch SHC.
     */
    public PublicInformation getPublicInformation()
            throws InterruptedException, BoschSHCException, ExecutionException, TimeoutException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            throw new BoschSHCException(HTTP_CLIENT_NOT_INITIALIZED);
        }

        String url = localHttpClient.getPublicInformationUrl();
        Request request = localHttpClient.createRequest(url, GET);

        return localHttpClient.sendRequest(request, PublicInformation.class, PublicInformation::isValid, null);
    }

    public boolean registerDiscoveryListener(ThingDiscoveryService listener) {
        if (thingDiscoveryService == null) {
            thingDiscoveryService = listener;
            return true;
        }

        return false;
    }

    public boolean unregisterDiscoveryListener() {
        if (thingDiscoveryService != null) {
            thingDiscoveryService = null;
            return true;
        }

        return false;
    }

    /**
     * Bridge callback handler for the results of long polls.
     *
     * It will check the results and
     * forward the received states to the Bosch thing handlers.
     *
     * @param result Results from Long Polling
     */
    void handleLongPollResult(LongPollResult result) {
        for (BoschSHCServiceState serviceState : result.result) {
            if (serviceState instanceof DeviceServiceData deviceServiceData) {
                handleDeviceServiceData(deviceServiceData);
            } else if (serviceState instanceof UserDefinedState userDefinedState) {
                handleUserDefinedState(userDefinedState);
            } else if (serviceState instanceof Scenario scenario) {
                final Channel channel = this.getThing().getChannel(BoschSHCBindingConstants.CHANNEL_SCENARIO_TRIGGERED);
                if (channel != null && isLinked(channel.getUID())) {
                    updateState(channel.getUID(), new StringType(scenario.name));
                }
            } else if (serviceState instanceof Message message) {
                handleMessage(message);
            }
        }
    }

    private void handleMessage(Message message) {
        if (Message.SOURCE_TYPE_DEVICE.equals(message.sourceType) && message.sourceId != null) {
            forwardMessageToDevice(message, message.sourceId);
        }
    }

    private void forwardMessageToDevice(Message message, String deviceId) {
        BoschSHCHandler deviceHandler = findDeviceHandler(deviceId);
        if (deviceHandler == null) {
            return;
        }

        deviceHandler.processMessage(message);
    }

    @Nullable
    private BoschSHCHandler findDeviceHandler(String deviceIdToFind) {
        for (Thing childThing : getThing().getThings()) {
            @Nullable
            ThingHandler baseHandler = childThing.getHandler();
            if (baseHandler instanceof BoschSHCHandler handler) {
                @Nullable
                String deviceId = handler.getBoschID();

                if (deviceIdToFind.equals(deviceId)) {
                    return handler;
                }
            }
        }
        return null;
    }

    /**
     * Processes a single long poll result.
     *
     * @param deviceServiceData object representing a single long poll result
     */
    private void handleDeviceServiceData(@Nullable DeviceServiceData deviceServiceData) {
        if (deviceServiceData != null) {
            JsonElement state = obtainState(deviceServiceData);

            logger.debug("Got update for service {} of type {}: {}", deviceServiceData.id, deviceServiceData.type,
                    state);

            var updateDeviceId = deviceServiceData.deviceId;
            if (updateDeviceId == null || state == null) {
                return;
            }

            logger.debug("Got update for device {}", updateDeviceId);

            forwardStateToHandlers(deviceServiceData, state, updateDeviceId);
        }
    }

    private void handleUserDefinedState(@Nullable UserDefinedState userDefinedState) {
        if (userDefinedState != null) {
            JsonElement state = GsonUtils.DEFAULT_GSON_INSTANCE.toJsonTree(userDefinedState.isState());

            logger.debug("Got update for user-defined state {} with id {}: {}", userDefinedState.getName(),
                    userDefinedState.getId(), state);

            var stateId = userDefinedState.getId();
            if (stateId == null || state == null) {
                return;
            }

            logger.debug("Got update for user-defined state {}", userDefinedState);

            forwardStateToHandlers(userDefinedState, state, stateId);
        }
    }

    /**
     * Extracts the actual state object from the given {@link DeviceServiceData} instance.
     * <p>
     * In some special cases like the <code>BatteryLevel</code> service the {@link DeviceServiceData} object itself
     * contains the state.
     * In all other cases, the state is contained in a sub-object named <code>state</code>.
     *
     * @param deviceServiceData the {@link DeviceServiceData} object from which the state should be obtained
     * @return the state sub-object or the {@link DeviceServiceData} object itself
     */
    @Nullable
    private JsonElement obtainState(DeviceServiceData deviceServiceData) {
        // the battery level service receives no individual state object but rather requires the DeviceServiceData
        // structure
        if ("BatteryLevel".equals(deviceServiceData.id)) {
            return GsonUtils.DEFAULT_GSON_INSTANCE.toJsonTree(deviceServiceData);
        }

        return deviceServiceData.state;
    }

    /**
     * Tries to find handlers for the device with the given ID and forwards the received state to the handlers.
     *
     * @param serviceData object representing updates received in long poll results
     * @param state the received state object as JSON element
     * @param updateDeviceId the ID of the device for which the state update was received
     */
    private void forwardStateToHandlers(BoschSHCServiceState serviceData, JsonElement state, String updateDeviceId) {
        boolean handled = false;
        final String serviceId = getServiceId(serviceData);

        Bridge bridge = this.getThing();
        for (Thing childThing : bridge.getThings()) {
            // All children of this should implement BoschSHCHandler
            @Nullable
            ThingHandler baseHandler = childThing.getHandler();
            if (baseHandler instanceof BoschSHCHandler handler) {
                @Nullable
                String deviceId = handler.getBoschID();

                if (deviceId == null) {
                    continue;
                }

                logger.trace("Checking device {}, looking for {}", deviceId, updateDeviceId);

                // handled is a boolean latch that stays true once it becomes true
                // note that no short-circuiting operators are used, meaning that the method
                // calls will always be evaluated, even if the latch is already true
                handled |= notifyHandler(handler, deviceId, updateDeviceId, serviceId, state);
                handled |= notifyParentHandler(handler, deviceId, updateDeviceId, serviceId, state);
            } else {
                logger.warn("longPoll: child handler for {} does not implement Bosch SHC handler", baseHandler);
            }
        }

        if (!handled) {
            logger.debug("Could not find a thing for device ID: {}", updateDeviceId);
        }
    }

    /**
     * Notifies the given handler if its device ID exactly matches the device ID for which the update was received.
     * 
     * @param handler the handler to be notified if applicable
     * @param deviceId the device ID associated with the handler
     * @param updateDeviceId the device ID for which the update was received
     * @param serviceId the ID of the service for which the update was received
     * @param state the received state object as JSON element
     * 
     * @return <code>true</code> if the handler matched and was notified, <code>false</code> otherwise
     */
    private boolean notifyHandler(BoschSHCHandler handler, String deviceId, String updateDeviceId, String serviceId,
            JsonElement state) {
        if (updateDeviceId.equals(deviceId)) {
            logger.debug("Found handler {}, calling processUpdate() for service {} with state {}", handler, serviceId,
                    state);
            handler.processUpdate(serviceId, state);
            return true;
        }
        return false;
    }

    /**
     * If an update is received for a logical child device and the given handler is the parent device handler, the
     * parent handler is notified.
     * 
     * @param handler the handler to be notified if applicable
     * @param deviceId the device ID associated with the handler
     * @param updateDeviceId the device ID for which the update was received
     * @param serviceId the ID of the service for which the update was received
     * @param state the received state object as JSON element
     * @return <code>true</code> if the given handler was the corresponding parent handler and was notified,
     *         <code>false</code> otherwise
     */
    private boolean notifyParentHandler(BoschSHCHandler handler, String deviceId, String updateDeviceId,
            String serviceId, JsonElement state) {
        if (BoschDeviceIdUtils.isChildDeviceId(updateDeviceId)) {
            String parentDeviceId = BoschDeviceIdUtils.getParentDeviceId(updateDeviceId);
            if (parentDeviceId.equals(deviceId)) {
                logger.debug("Notifying parent handler {} about update for child device for service {} with state {}",
                        handler, serviceId, state);
                handler.processChildUpdate(updateDeviceId, serviceId, state);
                return true;
            }
        }
        return false;
    }

    private String getServiceId(BoschSHCServiceState serviceData) {
        if (serviceData instanceof UserDefinedState userState) {
            return userState.getId();
        }
        return ((DeviceServiceData) serviceData).id;
    }

    /**
     * Bridge callback handler for the failures during long polls.
     *
     * It will update the bridge status and try to access the SHC again.
     *
     * @param e error during long polling
     */
    void handleLongPollFailure(Throwable e) {
        logger.warn("Long polling failed, will try to reconnect", e);
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.long-polling-failed.http-client-null");
            return;
        }

        this.updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE,
                "@text/offline.long-polling-failed.trying-to-reconnect");
        scheduleInitialAccess(localHttpClient);
    }

    public Device getDeviceInfo(String deviceId)
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            throw new BoschSHCException(HTTP_CLIENT_NOT_INITIALIZED);
        }

        String url = localHttpClient.getBoschSmartHomeUrl(String.format("devices/%s", deviceId));
        Request request = localHttpClient.createRequest(url, GET);

        return localHttpClient.sendRequest(request, Device.class, Device::isValid,
                (Integer statusCode, String content) -> {
                    JsonRestExceptionResponse errorResponse = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(content,
                            JsonRestExceptionResponse.class);
                    if (errorResponse != null && JsonRestExceptionResponse.isValid(errorResponse)) {
                        if (errorResponse.errorCode.equals(JsonRestExceptionResponse.ENTITY_NOT_FOUND)) {
                            return new BoschSHCException("@text/offline.conf-error.invalid-device-id");
                        } else {
                            return new BoschSHCException(String.format(
                                    "Request for info of device %s failed with status code %d and error code %s",
                                    deviceId, errorResponse.statusCode, errorResponse.errorCode));
                        }
                    } else {
                        return new BoschSHCException(String.format(
                                "Request for info of device %s failed with status code %d", deviceId, statusCode));
                    }
                });
    }

    public UserDefinedState getUserStateInfo(String stateId)
            throws BoschSHCException, InterruptedException, TimeoutException, ExecutionException {
        @Nullable
        BoschHttpClient locaHttpClient = this.httpClient;
        if (locaHttpClient == null) {
            throw new BoschSHCException(HTTP_CLIENT_NOT_INITIALIZED);
        }

        String url = locaHttpClient.getBoschSmartHomeUrl(String.format("userdefinedstates/%s", stateId));
        Request request = locaHttpClient.createRequest(url, GET);

        return locaHttpClient.sendRequest(request, UserDefinedState.class, UserDefinedState::isValid,
                (Integer statusCode, String content) -> {
                    JsonRestExceptionResponse errorResponse = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(content,
                            JsonRestExceptionResponse.class);
                    if (errorResponse != null && JsonRestExceptionResponse.isValid(errorResponse)) {
                        if (errorResponse.errorCode.equals(JsonRestExceptionResponse.ENTITY_NOT_FOUND)) {
                            return new BoschSHCException("@text/offline.conf-error.invalid-state-id");
                        } else {
                            return new BoschSHCException(String.format(
                                    "Request for info of user-defined state %s failed with status code %d and error code %s",
                                    stateId, errorResponse.statusCode, errorResponse.errorCode));
                        }
                    } else {
                        return new BoschSHCException(
                                String.format("Request for info of user-defined state %s failed with status code %d",
                                        stateId, statusCode));
                    }
                });
    }

    /**
     * Query the Bosch Smart Home Controller for the state of the given device.
     * <p>
     * The URL used for retrieving the state has the following structure:
     *
     * <pre>
     * https://{IP}:8444/smarthome/devices/{deviceId}/services/{serviceName}/state
     * </pre>
     *
     * @param deviceId Id of device to get state for
     * @param stateName Name of the state to query
     * @param stateClass Class to convert the resulting JSON to
     * @return the deserialized state object, may be <code>null</code>
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws BoschSHCException
     */
    public <T extends BoschSHCServiceState> @Nullable T getState(String deviceId, String stateName, Class<T> stateClass)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            logger.warn(HTTP_CLIENT_NOT_INITIALIZED);
            return null;
        }

        String url = localHttpClient.getServiceStateUrl(stateName, deviceId, stateClass);
        logger.debug("getState(): Requesting \"{}\" from Bosch: {} via {}", stateName, deviceId, url);
        return getState(localHttpClient, url, stateClass);
    }

    /**
     * Queries the Bosch Smart Home Controller for the state using an explicit endpoint.
     *
     * @param <T> Type to which the resulting JSON should be deserialized to
     * @param endpoint The destination endpoint part of the URL
     * @param stateClass Class to convert the resulting JSON to
     * @return the deserialized state object, may be <code>null</code>
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws BoschSHCException
     */
    public <T extends BoschSHCServiceState> @Nullable T getState(String endpoint, Class<T> stateClass)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            logger.warn(HTTP_CLIENT_NOT_INITIALIZED);
            return null;
        }

        String url = localHttpClient.getBoschSmartHomeUrl(endpoint);
        logger.debug("getState(): Requesting from Bosch: {}", url);
        return getState(localHttpClient, url, stateClass);
    }

    /**
     * Sends a HTTP GET request in order to retrieve a state from the Bosch Smart Home Controller.
     *
     * @param <T> Type to which the resulting JSON should be deserialized to
     * @param httpClient HTTP client used for sending the request
     * @param url URL at which the state should be retrieved
     * @param stateClass Class to convert the resulting JSON to
     * @return the deserialized state object, may be <code>null</code>
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws BoschSHCException
     */
    protected <T extends BoschSHCServiceState> @Nullable T getState(BoschHttpClient httpClient, String url,
            Class<T> stateClass) throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        Request request = httpClient.createRequest(url, GET).header("Accept", "application/json");

        ContentResponse contentResponse = request.send();

        String content = contentResponse.getContentAsString();
        logger.debug("getState(): Request complete: [{}] - return code: {}", content, contentResponse.getStatus());

        int statusCode = contentResponse.getStatus();
        if (statusCode != 200) {
            JsonRestExceptionResponse errorResponse = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(content,
                    JsonRestExceptionResponse.class);
            if (errorResponse != null) {
                throw new BoschSHCException(
                        String.format("State request with URL %s failed with status code %d and error code %s", url,
                                errorResponse.statusCode, errorResponse.errorCode));
            } else {
                throw new BoschSHCException(
                        String.format("State request with URL %s failed with status code %d", url, statusCode));
            }
        }

        @Nullable
        T state = BoschSHCServiceState.fromJson(content, stateClass);
        if (state == null) {
            throw new BoschSHCException(String.format("Received invalid, expected type %s", stateClass.getName()));
        }
        return state;
    }

    /**
     * Sends a state change for a device to the controller
     *
     * @param deviceId Id of device to change state for
     * @param serviceName Name of service of device to change state for
     * @param state New state data to set for service
     *
     * @return Response of request
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public <T extends BoschSHCServiceState> @Nullable Response putState(String deviceId, String serviceName, T state)
            throws InterruptedException, TimeoutException, ExecutionException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            logger.warn(HTTP_CLIENT_NOT_INITIALIZED);
            return null;
        }

        // Create request
        String url = localHttpClient.getServiceStateUrl(serviceName, deviceId, state.getClass());
        Request request = localHttpClient.createRequest(url, PUT, state);

        // Send request
        return request.send();
    }

    /**
     * Sends a HTTP POST request without a request body to the given endpoint.
     *
     * @param endpoint The destination endpoint part of the URL
     * @return the HTTP response
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public @Nullable Response postAction(String endpoint)
            throws InterruptedException, TimeoutException, ExecutionException {
        return postAction(endpoint, null);
    }

    /**
     * Sends a HTTP POST request with a request body to the given endpoint.
     *
     * @param <T> Type of the request
     * @param endpoint The destination endpoint part of the URL
     * @param requestBody object representing the request body to be sent, may be <code>null</code>
     * @return the HTTP response
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public <T extends BoschSHCServiceState> @Nullable Response postAction(String endpoint, @Nullable T requestBody)
            throws InterruptedException, TimeoutException, ExecutionException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            logger.warn(HTTP_CLIENT_NOT_INITIALIZED);
            return null;
        }

        String url = localHttpClient.getBoschSmartHomeUrl(endpoint);
        Request request = localHttpClient.createRequest(url, POST, requestBody);
        return request.send();
    }

    public @Nullable DeviceServiceData getServiceData(String deviceId, String serviceName)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        @Nullable
        BoschHttpClient localHttpClient = this.httpClient;
        if (localHttpClient == null) {
            logger.warn(HTTP_CLIENT_NOT_INITIALIZED);
            return null;
        }

        String url = localHttpClient.getServiceUrl(serviceName, deviceId);
        logger.debug("getState(): Requesting \"{}\" from Bosch: {} via {}", serviceName, deviceId, url);
        return getState(localHttpClient, url, DeviceServiceData.class);
    }
}
