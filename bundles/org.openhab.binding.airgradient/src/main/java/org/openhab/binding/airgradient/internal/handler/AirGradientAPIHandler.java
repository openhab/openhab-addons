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
package org.openhab.binding.airgradient.internal.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.airgradient.internal.communication.AirGradientCommunicationException;
import org.openhab.binding.airgradient.internal.communication.RemoteAPIController;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;
import org.openhab.binding.airgradient.internal.discovery.AirGradientLocationDiscoveryService;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AirGradientAPIHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientAPIHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(AirGradientAPIHandler.class);

    private @Nullable ScheduledFuture<?> pollingJob;
    private final HttpClient httpClient;
    private final Gson gson;
    private final Set<PollEventListener> pollListeners = new HashSet<>(1);

    private @NonNullByDefault({}) RemoteAPIController apiController = null;
    private @NonNullByDefault({}) AirGradientAPIConfiguration apiConfig = null;

    public AirGradientAPIHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            pollingCode();
        } else {
            // This is read only
            logger.warn("Received command {} for channel {}, but the API is read only", command.toString(),
                    channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        apiConfig = getConfigAs(AirGradientAPIConfiguration.class);
        if (!apiConfig.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Need to set hostname to a valid URL. Refresh interval needs to be a positive integer.");
            return;
        }

        apiController = new RemoteAPIController(httpClient, gson, apiConfig);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, apiConfig.refreshInterval,
                TimeUnit.SECONDS);
    }

    private static String getMeasureId(Measure measure) {
        String id = measure.getLocationId();
        if (id.isEmpty()) {
            // Local devices don't have location ID.
            id = measure.getSerialNo();
        }

        return id;
    }

    protected void pollingCode() {
        try {
            List<Measure> measures = apiController.getMeasures();
            updateStatus(ThingStatus.ONLINE);
            triggerPollEvent(measures);

            Map<String, Measure> measureMap = measures.stream()
                    .collect(Collectors.toMap((m) -> getMeasureId(m), (m) -> m));

            for (Thing t : getThing().getThings()) {
                if (t.getHandler() instanceof AirGradientLocationHandler handler) {
                    String locationId = handler.getLocationId();
                    @Nullable
                    Measure measure = measureMap.get(locationId);
                    if (measure != null) {
                        handler.setMeasurment(measure);
                    } else {
                        logger.debug("Could not find measures for location {}", locationId);
                    }
                }
            }
        } catch (AirGradientCommunicationException agce) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, agce.getMessage());
        }
    }

    /**
     * Return location ids we already have things for.
     * 
     * @return location ids we already have things for.
     */
    public List<String> getRegisteredLocationIds() {
        List<Thing> things = getThing().getThings();
        List<String> results = new ArrayList<>(things.size());
        for (Thing t : things) {
            if (t.getHandler() instanceof AirGradientLocationHandler handler) {
                results.add(handler.getLocationId());
            }
        }

        return results;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    protected void setConfiguration(AirGradientAPIConfiguration config) {
        this.apiConfig = config;
    }

    protected void setApiController(RemoteAPIController apiController) {
        this.apiController = apiController;
    }

    public RemoteAPIController getApiController() {
        return apiController;
    }

    // Event listening

    public void addPollEventListener(PollEventListener listener) {
        pollListeners.add(listener);
    }

    public void removePollEventListener(PollEventListener listener) {
        pollListeners.remove(listener);
    }

    public void triggerPollEvent(List<Measure> measures) {
        for (PollEventListener listener : pollListeners) {
            listener.pollEvent(measures);
        }
    }

    // Discovery

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AirGradientLocationDiscoveryService.class);
    }
}
