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
package org.openhab.binding.groupepsa.internal.bridge;

import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.THING_TYPE_BRIDGE;
import static org.openhab.binding.groupepsa.internal.GroupePSABindingConstants.VendorConstants;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.groupepsa.internal.discovery.GroupePSADiscoveryService;
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
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link GroupePSABridgeHandler} is responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSABridgeHandler extends BaseBridgeHandler {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);
    private static final long DEFAULT_POLLING_INTERVAL_M = TimeUnit.HOURS.toMinutes(1);

    private final OAuthFactory oAuthFactory;

    private @Nullable OAuthClientService oAuthService;
    private @Nullable ScheduledFuture<?> groupepsaBridgePollingJob;
    private final HttpClient httpClient;

    private String vendor = "";
    private @Nullable VendorConstants vendorConstants;
    private String userName = "";
    private String password = "";
    private String clientId = "";
    private String clientSecret = "";

    private @Nullable GroupePSAConnectApi groupePSAApi;

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
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/comm-error-query-vehicles-failed");
            }
        } catch (GroupePSACommunicationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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

        if (vendor.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-vendor");
        } else if (userName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-username");
        } else if (password.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-password");
        } else if (clientId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/conf-error-no-clientid");
        } else if (clientSecret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error-no-clientsecret");
        } else if (pollingIntervalM < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/conf-error-invalid-polling-interval");
        } else {
            VendorConstants localVendorConstants = VendorConstants.valueOf(vendor);
            vendorConstants = localVendorConstants;

            oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), localVendorConstants.url,
                    null, clientId, clientSecret, localVendorConstants.scope, true);

            groupePSAApi = new GroupePSAConnectApi(httpClient, this, clientId, localVendorConstants.realm);

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
            if (nextE != null) {
                e = nextE;
            }
        } while (nextE != null);
        return e;
    }

    public String authenticate() throws GroupePSACommunicationException {
        OAuthClientService localOAuthService = oAuthService;
        VendorConstants localVendorConstants = vendorConstants;
        if (localOAuthService == null) {
            throw new GroupePSACommunicationException("OAuth service is unexpectedly null");
        }
        if (localVendorConstants == null) {
            throw new GroupePSACommunicationException("Vendor constants are unexpectedly null");
        }
        try {
            AccessTokenResponse result = localOAuthService.getAccessTokenResponse();
            if (result == null) {
                result = localOAuthService.getAccessTokenByResourceOwnerPasswordCredentials(userName, password,
                        localVendorConstants.scope);
            }
            return result.getAccessToken();
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new GroupePSACommunicationException("Unable to authenticate: " + getRootCause(e).getMessage(), e);
        }
    }

    public GroupePSAConnectApi getAPI() throws GroupePSACommunicationException {
        GroupePSAConnectApi localGroupePSAApi = groupePSAApi;
        if (localGroupePSAApi == null) {
            throw new GroupePSACommunicationException("groupePSAApi is unexpectedly null");
        } else {
            return localGroupePSAApi;
        }
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

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(GroupePSADiscoveryService.class);
    }
}
