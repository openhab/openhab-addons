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

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.SERVLET_PATH;
import static org.openhab.binding.twilio.internal.TwilioBindingConstants.WEBHOOK_MEDIA;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.twilio.internal.api.TwilioApiClient;
import org.openhab.binding.twilio.internal.api.TwilioApiException;
import org.openhab.binding.twilio.internal.config.TwilioAccountConfiguration;
import org.openhab.binding.twilio.internal.discovery.TwilioPhoneDiscoveryService;
import org.openhab.binding.twilio.internal.service.TwilioCloudWebhookService;
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
    private final TwilioCloudWebhookService cloudWebhookService;
    private @Nullable TwilioApiClient apiClient;
    private TwilioAccountConfiguration config = new TwilioAccountConfiguration();
    private @Nullable Future<?> validateTask;

    public TwilioAccountHandler(Bridge bridge, HttpClient httpClient, TwilioCloudWebhookService cloudWebhookService) {
        super(bridge);
        this.httpClient = httpClient;
        this.cloudWebhookService = cloudWebhookService;
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.missing-account-sid");
            return;
        }

        String authToken = config.authToken;
        if (authToken == null || authToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.missing-auth-token");
            return;
        }

        apiClient = new TwilioApiClient(httpClient, accountSid, authToken);
        updateStatus(ThingStatus.UNKNOWN);
        validateTask = scheduler.submit(this::asyncValidateAccount);
    }

    @Override
    public void dispose() {
        Future<?> task = validateTask;
        if (task != null) {
            task.cancel(true);
            validateTask = null;
        }
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

    /**
     * Returns true if cloud webhooks are active (enabled in config and cloud URL is available).
     */
    public boolean isUsingCloudWebhooks() {
        return config.useCloudWebhook && cloudWebhookService.getBaseUrl() != null;
    }

    /**
     * Returns the webhook base URL for a phone thing, choosing cloud or publicUrl based on
     * account configuration. The returned URL can have endpoint path segments appended.
     *
     * @param thingUID the phone thing UID as a string
     * @return the base webhook URL (e.g. {@code https://…/twilio/callback/twilio:phone:…}),
     *         or {@code null} if no URL source is configured
     */
    public @Nullable String getWebhookBaseUrl(String thingUID) {
        if (config.useCloudWebhook) {
            String cloudBase = cloudWebhookService.getBaseUrl();
            if (cloudBase != null) {
                return cloudBase + "/" + thingUID;
            }
        }
        return getPublicUrlBase(thingUID);
    }

    /**
     * Returns the media serving base URL, choosing cloud or publicUrl based on account
     * configuration. Media UUIDs can be appended as sub-paths.
     *
     * @return the media base URL, or {@code null} if no URL source is configured
     */
    public @Nullable String getMediaBaseUrl() {
        if (config.useCloudWebhook) {
            String cloudBase = cloudWebhookService.getBaseUrl();
            if (cloudBase != null) {
                return cloudBase + "/" + WEBHOOK_MEDIA;
            }
        }
        String publicUrl = getNormalizedPublicUrl();
        return publicUrl != null ? publicUrl + SERVLET_PATH + "/" + WEBHOOK_MEDIA : null;
    }

    private @Nullable String getPublicUrlBase(String thingUID) {
        String publicUrl = getNormalizedPublicUrl();
        return publicUrl != null ? publicUrl + SERVLET_PATH + "/" + thingUID : null;
    }

    private @Nullable String getNormalizedPublicUrl() {
        String publicUrl = config.publicUrl;
        if (publicUrl == null || publicUrl.isBlank()) {
            return null;
        }
        if (publicUrl.endsWith("/")) {
            publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
        }
        return publicUrl;
    }

    private void asyncValidateAccount() {
        TwilioApiClient client = apiClient;
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.api-client-not-initialized");
            return;
        }

        try {
            if (client.validateAccount()) {
                if (config.useCloudWebhook) {
                    cloudWebhookService.register();
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.account-inactive");
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
