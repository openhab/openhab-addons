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
package org.openhab.binding.plivo.internal.handler;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.SERVLET_PATH;
import static org.openhab.binding.plivo.internal.PlivoBindingConstants.WEBHOOK_MEDIA;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.plivo.internal.api.PlivoApiClient;
import org.openhab.binding.plivo.internal.api.PlivoApiException;
import org.openhab.binding.plivo.internal.config.PlivoAccountConfiguration;
import org.openhab.binding.plivo.internal.discovery.PlivoPhoneDiscoveryService;
import org.openhab.binding.plivo.internal.service.PlivoCloudWebhookService;
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
 * The {@link PlivoAccountHandler} is the bridge handler for a Plivo account.
 * It manages the API client and validates credentials.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(PlivoAccountHandler.class);

    private final HttpClient httpClient;
    private final PlivoCloudWebhookService cloudWebhookService;
    private @Nullable PlivoApiClient apiClient;
    private PlivoAccountConfiguration config = new PlivoAccountConfiguration();
    private @Nullable Future<?> validateTask;

    public PlivoAccountHandler(Bridge bridge, HttpClient httpClient, PlivoCloudWebhookService cloudWebhookService) {
        super(bridge);
        this.httpClient = httpClient;
        this.cloudWebhookService = cloudWebhookService;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(PlivoAccountConfiguration.class);

        if (!config.useCloudWebhook) {
            cloudWebhookService.unregister(getThing().getUID().getAsString());
        }

        String authId = config.authId;
        if (authId == null || authId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.missing-auth-id");
            return;
        }

        String authToken = config.authToken;
        if (authToken == null || authToken.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.missing-auth-token");
            return;
        }

        apiClient = new PlivoApiClient(httpClient, authId, authToken);
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
    public void handleRemoval() {
        cloudWebhookService.unregister(getThing().getUID().getAsString());
        super.handleRemoval();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PlivoPhoneDiscoveryService.class);
    }

    /**
     * Returns the API client for child things to use.
     *
     * @return the {@link PlivoApiClient}, or null if not initialized
     */
    public @Nullable PlivoApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Returns the account configuration.
     *
     * @return the configuration
     */
    public PlivoAccountConfiguration getAccountConfig() {
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
     * @return the base webhook URL (e.g. {@code https://…/plivo/callback/plivo:phone:…}),
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
        PlivoApiClient client = apiClient;
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.api-client-not-initialized");
            return;
        }

        try {
            if (client.validateAccount()) {
                if (config.useCloudWebhook) {
                    cloudWebhookService.register(getThing().getUID().getAsString());
                }
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.account-invalid");
            }
        } catch (PlivoApiException e) {
            if (e.isConfigurationError()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
            logger.debug("Failed to validate Plivo account: {}", e.getMessage());
        }
    }
}
