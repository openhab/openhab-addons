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
package org.openhab.binding.groupepsa.internal.bridge;

import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.VendorConstants;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.groupepsa.internal.rest.api.GroupePSAConnectApi;
import org.openhab.binding.groupepsa.internal.rest.api.dto.Vehicle;
import org.openhab.binding.groupepsa.internal.rest.api.dto.VehicleStatus;
import org.openhab.binding.groupepsa.internal.rest.exceptions.GroupePSACommunicationException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GroupePSABridgeHandler} is responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSABridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(GroupePSABridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final long DEFAULT_POLLING_INTERVAL_M = TimeUnit.HOURS.toMinutes(1);

    private final OAuthFactory oAuthFactory;

    private @NonNullByDefault({}) OAuthClientService oAuthService;
    private @Nullable ScheduledFuture<?> groupepsaBridgePollingJob;
    private final HttpClient httpClient;

    private @NonNullByDefault({}) String vendor;
    private @NonNullByDefault({}) VendorConstants vendorConstants;
    private @NonNullByDefault({}) String userName;
    private @NonNullByDefault({}) String password;
    private @NonNullByDefault({}) String clientId;
    private @NonNullByDefault({}) String clientSecret;

    private @NonNullByDefault({}) GroupePSAConnectApi groupePSAApi;

    public GroupePSABridgeHandler(Bridge bridge, OAuthFactory oAuthFactory, HttpClient httpClient) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
        this.httpClient = httpClient;
    }

    private void pollGroupePSAs() {
        try {
            List<Vehicle> vehicles = getVehicles();
            if (vehicles != null) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Found {} vehicles", vehicles.size());
                for (Vehicle vehicle : vehicles) {
                    logger.trace("Vehicle: {}", vehicle);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error-query-vehicles-failed");
                logger.warn("Unable to fetch vehicles");
            }
        } catch (GroupePSACommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/comm-error-query-vehicles-failed");
            logger.warn("Unable to fetch vehicles: {}", e.getMessage());
        }
    }

    @Override
    public void dispose() {
        stopGroupePSABridgePolling();
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
    }

    @Override
    public void initialize() {
        GroupePSABridgeConfiguration bridgeConfiguration = getConfigAs(GroupePSABridgeConfiguration.class);

        vendor = bridgeConfiguration.getVendor();
        userName = bridgeConfiguration.getUserName();
        password = bridgeConfiguration.getPassword();
        clientId = bridgeConfiguration.getClientId();
        clientSecret = bridgeConfiguration.getClientSecret();

        final Integer pollingIntervalM = bridgeConfiguration.getPollingInterval();

        if (vendor == null || vendor.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-vendor");
        } else if (userName == null || userName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-username");
        } else if (password == null || password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-password");
        } else if (clientId == null || clientId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-clientid");
        } else if (clientSecret == null || clientSecret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error-no-clientsecret");
        } else if (pollingIntervalM != null && pollingIntervalM < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error-invalid-polling-interval");
        } else {

            vendorConstants = VendorConstants.valueOf(vendor);

            oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                    vendorConstants.OAUTH_URL, null, clientId, clientSecret, vendorConstants.OAUTH_SCOPE, true);

            groupePSAApi = new GroupePSAConnectApi(httpClient, this, clientId, vendorConstants.OAUTH_REALM);

            startGroupePSABridgePolling(pollingIntervalM);

            updateStatus(ThingStatus.UNKNOWN);
        }
    }

    private void startGroupePSABridgePolling(@Nullable Integer pollingIntervalM) {
        if (groupepsaBridgePollingJob == null) {
            final long pollingIntervalToUse = pollingIntervalM == null ? DEFAULT_POLLING_INTERVAL_M : pollingIntervalM;
            groupepsaBridgePollingJob = scheduler.scheduleWithFixedDelay(() -> pollGroupePSAs(), 1,
                    TimeUnit.MINUTES.toSeconds(pollingIntervalToUse), TimeUnit.SECONDS);
        }
    }

    private void stopGroupePSABridgePolling() {
        final ScheduledFuture<?> job = groupepsaBridgePollingJob;
        if (job != null) {
            job.cancel(true);
            groupepsaBridgePollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    static Throwable getRootCause(Throwable e) {
        Throwable nextE;
        do {
            nextE = e.getCause();
            if (nextE != null)
                e = nextE;
        } while (nextE != null);
        return e;
    }

    public String authenticate() throws GroupePSACommunicationException {
        try {
            AccessTokenResponse result = oAuthService.getAccessTokenResponse();
            if (result == null) {
                result = oAuthService.getAccessTokenByResourceOwnerPasswordCredentials(this.userName, this.password,
                        this.vendorConstants.OAUTH_SCOPE);
            }
            return result.getAccessToken();
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new GroupePSACommunicationException("Unable to authenticate: " + getRootCause(e).getMessage(), e);
        }
    }

    public GroupePSAConnectApi getAPI() {
        return groupePSAApi;
    }

    /**
     * @return A list of vehicles
     * @throws GroupePSACommunicationException In case the query cannot be executed
     *             successfully
     */
    public @Nullable List<Vehicle> getVehicles() throws GroupePSACommunicationException {
        return getAPI().getVehicles();
    }

    /**
     * @param id The id of the mower to query
     * @return A detailed status of the mower with the specified id
     * @throws GroupePSACommunicationException In case the query cannot be executed
     *             successfully
     */
    public @Nullable VehicleStatus getVehicleStatus(String vin) throws GroupePSACommunicationException {
        return getAPI().getVehicleStatus(vin);
    }
}
