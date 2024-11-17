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
package org.openhab.binding.metofficedatahub.internal;

import static org.openhab.binding.metofficedatahub.internal.MetOfficeDataHubBindingConstants.BRIDGE_PROP_FORECAST_REQUEST_COUNT;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.metofficedatahub.internal.api.IConnectionStatusListener;
import org.openhab.binding.metofficedatahub.internal.api.IRateLimiterListener;
import org.openhab.binding.metofficedatahub.internal.api.RequestLimiter;
import org.openhab.binding.metofficedatahub.internal.api.SiteApi;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MetOfficeDataHubBridgeHandler} models the account(s) to the MetOfficeDataHub services.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class MetOfficeDataHubBridgeHandler extends BaseBridgeHandler
        implements IRateLimiterListener, IConnectionStatusListener {

    private volatile MetOfficeDataHubBridgeConfiguration config = getConfigAs(
            MetOfficeDataHubBridgeConfiguration.class);

    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    private SiteApi siteApi;
    private String bridgeId = "";

    public MetOfficeDataHubBridgeHandler(final Bridge bridge, @Reference HttpClientFactory httpClientFactory,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider,
            @Reference StorageService storageService, @Reference TimeZoneProvider timeZoneProvider) {
        super(bridge);
        bridgeId = getThing().getUID().getAsString();
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
        this.bundle = FrameworkUtil.getBundle(getClass());
        this.siteApi = new SiteApi(bridgeId, httpClientFactory, storageService, translationProvider, localeProvider,
                timeZoneProvider, scheduler);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        siteApi.registerListeners(bridgeId, this);

        config = getConfigAs(MetOfficeDataHubBridgeConfiguration.class);

        siteApi.setLimits(config.siteRateDailyLimit);
        siteApi.setApiKey(config.siteApiKey);
        siteApi.validateSiteApi();
    }

    @Override
    public void dispose() {
        siteApi.deregisterListeners(bridgeId, this);
        siteApi.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    // API Management

    public SiteApi getSiteApi() {
        return siteApi;
    }

    // Localization functionality

    public String getLocalizedText(String key, @Nullable Object @Nullable... arguments) {
        String result = translationProvider.getText(bundle, key, key, localeProvider.getLocale(), arguments);
        return Objects.nonNull(result) ? result : key;
    }

    // Implementation of IRateLimiterListener

    @Override
    public void processRateLimiterUpdated(RequestLimiter requestLimiter) {
        final Map<String, String> newProps = new HashMap<>();
        newProps.put(BRIDGE_PROP_FORECAST_REQUEST_COUNT, String.valueOf(requestLimiter.getCurrentRequestCount()));
        this.updateProperties(newProps);
    }

    // Implementation of IConnectionStatusListener

    @Override
    public void processAuthenticationResult(boolean authenticated) {
        if (!authenticated) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    getLocalizedText("bridge.error.site-specific.auth-issue"));
        } else {
            processConnected();
        }
    }

    @Override
    public void processCommunicationFailure(final @Nullable Throwable e) {
        String message = "";
        if (e != null) {
            if (e.getLocalizedMessage() != null) {
                message = e.getLocalizedMessage();
            } else if (e.getMessage() != null) {
                message = e.getMessage();
            }
        }
        if (message == null || message.isBlank()) {
            message = getLocalizedText("bridge.error.site-specific.communication-failure.unknown");
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                getLocalizedText("bridge.error.site-specific.communication-failure", message));
    }

    @Override
    public void processConnected() {
        updateStatus(ThingStatus.ONLINE);
        for (Thing thing : getThing().getThings()) {
            if (thing instanceof MetOfficeDataHubSiteHandler siteHandler) {
                if (!siteHandler.requiresPoll()) {
                    siteHandler.scheduleInitTask();
                }
            }
        }
    }
}
