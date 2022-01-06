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
package org.openhab.binding.draytonwiser.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.draytonwiser.internal.DraytonWiserRefreshListener;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApi;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.discovery.DraytonWiserDiscoveryService;
import org.openhab.binding.draytonwiser.internal.model.DeviceDTO;
import org.openhab.binding.draytonwiser.internal.model.DomainDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeatHubHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Moved api and helper code to separate classes
 */
@NonNullByDefault
public class HeatHubHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(HeatHubHandler.class);
    private final DraytonWiserApi api;
    private final ExpiringCache<Boolean> refreshCache = new ExpiringCache<>(3, this::actualRefresh);

    private boolean updateProperties;
    private @Nullable DraytonWiserRefreshListener discoveryService;
    private @Nullable ScheduledFuture<?> refreshJob;

    public HeatHubHandler(final Bridge thing, final HttpClient httpClient) {
        super(thing);
        api = new DraytonWiserApi(httpClient);
    }

    public DraytonWiserApi getApi() {
        return api;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(DraytonWiserDiscoveryService.class);
    }

    public void setDiscoveryService(final DraytonWiserRefreshListener discoveryService) {
        this.discoveryService = discoveryService;
        refreshCache.invalidateValue();
        refresh();
    }

    public void unsetDiscoveryService() {
        discoveryService = null;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Drayton Wiser Heat Hub handler");
        final HeatHubConfiguration configuration = getConfigAs(HeatHubConfiguration.class);
        api.setConfiguration(configuration);
        updateProperties = true;
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, configuration.refresh, TimeUnit.SECONDS);
        updateStatus(ThingStatus.UNKNOWN);
    }

    public void refresh() {
        refreshCache.getValue();
    }

    private @Nullable Boolean actualRefresh() {
        try {
            if (ThingHandlerHelper.isHandlerInitialized(this)) {
                logger.debug("Refreshing devices");
                final DomainDTO domain = api.getDomain();

                if (domain == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "No data received");
                } else {
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    final DraytonWiserDTO draytonWiserDTO = new DraytonWiserDTO(domain);

                    updateProperties(draytonWiserDTO);
                    notifyListeners(draytonWiserDTO);
                }
                logger.debug("Finished refreshing devices");
            }
        } catch (final RuntimeException | DraytonWiserApiException e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
        return Boolean.TRUE;
    }

    @Override
    public void childHandlerInitialized(final ThingHandler childHandler, final Thing childThing) {
        refresh();
    }

    private void updateProperties(final DraytonWiserDTO draytonWiseDTO) {
        if (updateProperties) {
            final DeviceDTO device = draytonWiseDTO.getExtendedDeviceProperties(0);

            if (device != null) {
                final Map<String, String> properties = editProperties();
                DraytonWiserPropertyHelper.setGeneralDeviceProperties(device, properties);
                updateProperties(properties);
                updateProperties = false;
            }
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> future = refreshJob;

        if (future != null) {
            future.cancel(true);
        }
    }

    private void notifyListeners(final DraytonWiserDTO domain) {
        final DraytonWiserRefreshListener discoveryListener = discoveryService;

        if (discoveryListener != null) {
            discoveryListener.onRefresh(domain);
        }
        getThing().getThings().stream().map(Thing::getHandler)
                .filter(handler -> handler instanceof DraytonWiserRefreshListener)
                .map(DraytonWiserRefreshListener.class::cast).forEach(listener -> listener.onRefresh(domain));
    }
}
