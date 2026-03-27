/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.twilio.internal.handler;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.twilio.internal.api.TwilioApiClient;
import org.openhab.binding.twilio.internal.api.TwilioApiException;
import org.openhab.binding.twilio.internal.config.TwilioAccountConfiguration;
import org.openhab.binding.twilio.internal.discovery.TwilioPhoneDiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TwilioAccountHandler} is the bridge handler for a Twilio account.
 * It manages the API client and validates credentials.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TwilioAccountHandler.class);

    private final HttpClient httpClient;
    private @Nullable TwilioApiClient apiClient;
    private TwilioAccountConfiguration config = new TwilioAccountConfiguration();

    public TwilioAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Bridge has no channels
    }

    @Override
    public void initialize() {
        config = getConfigAs(TwilioAccountConfiguration.class);

        String accountSid = config.accountSid;
        if (accountSid == null || accountSid.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account SID is required");
            return;
        }

        String authToken = config.authToken;
        if (authToken == null || authToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Auth Token is required");
            return;
        }

        apiClient = new TwilioApiClient(httpClient, accountSid, authToken);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.submit(this::asyncValidateAccount);
    }

    @Override
    public void dispose() {
        apiClient = null;
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TwilioPhoneDiscoveryService.class);
    }

    /**
     * Returns the API client for child things to use.
     *
     * @return the {@link TwilioApiClient}, or null if not initialized
     */
    public @Nullable TwilioApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Returns the account configuration.
     *
     * @return the configuration
     */
    public TwilioAccountConfiguration getAccountConfig() {
        return config;
    }

    private void asyncValidateAccount() {
        TwilioApiClient client = apiClient;
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API client not initialized");
            return;
        }

        try {
            if (client.validateAccount()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Account is not active");
            }
        } catch (TwilioApiException e) {
            if (e.isConfigurationError()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            logger.debug("Failed to validate Twilio account: {}", e.getMessage());
        }
    }
}
