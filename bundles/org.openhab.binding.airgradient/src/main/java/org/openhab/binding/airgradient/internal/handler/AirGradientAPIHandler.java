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

import static org.openhab.binding.airgradient.internal.AirGradientBindingConstants.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.airgradient.internal.config.AirGradientAPIConfiguration;
import org.openhab.binding.airgradient.internal.discovery.AirGradientLocationDiscoveryService;
import org.openhab.binding.airgradient.internal.model.Measure;
import org.openhab.binding.airgradient.internal.prometheus.PrometheusMetric;
import org.openhab.binding.airgradient.internal.prometheus.PrometheusTextParser;
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
import com.google.gson.reflect.TypeToken;

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
            logger.debug("Received command {} for channel {}, but the API is read only", command.toString(),
                    channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        AirGradientAPIConfiguration config = getConfiguration();

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            try {
                ContentResponse response = httpClient.GET(generatePingUrl());
                if (response.getStatus() == 200) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            response.getContentAsString());
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 5, config.refreshInterval, TimeUnit.SECONDS);
    }

    protected void pollingCode() {
        List<Measure> measures = getMeasures();
        Map<String, Measure> measureMap = measures.stream()
                .collect(Collectors.toMap((m) -> m.getLocationId(), (m) -> m));

        for (Thing t : getThing().getThings()) {
            AirGradientLocationHandler handler = (AirGradientLocationHandler) t.getHandler();
            if (handler != null) {
                String locationId = handler.getLocationId();
                @Nullable
                Measure measure = measureMap.get(locationId);
                if (measure != null) {
                    handler.setMeasurment(locationId, measure);
                }
            }
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
            AirGradientLocationHandler handler = (AirGradientLocationHandler) t.getHandler();
            if (handler != null) {
                results.add(handler.getLocationId());
            }
        }

        return results;
    }

    /**
     * Return list of measures from AirGradient API.
     *
     * @return list of measures
     */
    public List<Measure> getMeasures() {
        try {
            ContentResponse response = httpClient.GET(generateMeasuresUrl());
            String contentType = response.getMediaType();
            logger.debug("Got measurements with status {}: {} ({})", response.getStatus(),
                    response.getContentAsString(), contentType);
            if (response.getStatus() == 200) {
                updateStatus(ThingStatus.ONLINE);
                String stringResponse = response.getContentAsString().trim();

                if (CONTENTTYPE_JSON.equals(contentType)) {
                    return parseJson(stringResponse);
                } else if (CONTENTTYPE_TEXT.equals(contentType)) {
                    return parsePrometheus(stringResponse);
                }

            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, response.getContentAsString());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return Collections.emptyList();
    }

    private List<Measure> parsePrometheus(String stringResponse) {
        List<PrometheusMetric> metrics = PrometheusTextParser.parse(stringResponse);
        Measure measure = new Measure();

        for (PrometheusMetric metric : metrics) {
            if (metric.getMetricName().equals("pm02")) {
                measure.pm02 = metric.getValue();
            } else if (metric.getMetricName().equals("rco2")) {
                measure.rco2 = metric.getValue();
            } else if (metric.getMetricName().equals("atmp")) {
                measure.atmp = metric.getValue();
            } else if (metric.getMetricName().equals("rhum")) {
                measure.rhum = metric.getValue();
            } else if (metric.getMetricName().equals("tvoc")) {
                measure.tvoc = metric.getValue();
            } else if (metric.getMetricName().equals("nox")) {
                measure.noxIndex = metric.getValue();
            }

            if (metric.getLabels().containsKey("id")) {
                String id = metric.getLabels().get("id");
                measure.serialno = id;
                measure.locationId = id;
                measure.locationName = id;
            }
        }

        return Arrays.asList(measure);
    }

    private List<Measure> parseJson(String stringResponse) {
        List<@Nullable Measure> measures = null;
        if (stringResponse.startsWith("[")) {
            // Array of measures, like returned from the AirGradients API
            Type measuresType = new TypeToken<List<@Nullable Measure>>() {
            }.getType();
            measures = gson.fromJson(stringResponse, measuresType);
        } else if (stringResponse.startsWith("{")) {
            // Single measure e.g. if you read directly from the device
            Type measureType = new TypeToken<Measure>() {
            }.getType();
            Measure measure = gson.fromJson(stringResponse, measureType);
            measures = new ArrayList<@Nullable Measure>(1);
            measures.add(measure);
        }

        if (measures != null) {
            List<@Nullable Measure> nullableMeasuresWithoutNulls = measures.stream().filter(Objects::nonNull).toList();
            List<Measure> measuresWithoutNulls = new ArrayList<Measure>(nullableMeasuresWithoutNulls.size());
            for (@Nullable
            Measure m : nullableMeasuresWithoutNulls) {
                if (m != null) {
                    measuresWithoutNulls.add(m);
                }
            }

            return measuresWithoutNulls;
        }

        return Collections.emptyList();
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> p = pollingJob;
        if (p != null) {
            p.cancel(true);
            pollingJob = null;
        }
    }

    private @Nullable String generatePingUrl() {
        AirGradientAPIConfiguration config = getConfiguration();
        if (hasCloudUrl(config)) {
            return config.hostname + PING_PATH;
        } else {
            return config.hostname;
        }
    }

    private @Nullable String generateMeasuresUrl() {
        AirGradientAPIConfiguration config = getConfiguration();
        if (hasCloudUrl(config)) {
            return config.hostname + String.format(CURRENT_MEASURES_PATH, config.token);
        } else {
            return config.hostname;
        }
    }

    /**
     * Returns true if this is a URL against the cloud.
     *
     * @return true if this is a URL against the cloud API
     */
    private boolean hasCloudUrl(AirGradientAPIConfiguration config) {
        URI url = URI.create(config.hostname);
        return url.getPath().equals("/");
    }

    private AirGradientAPIConfiguration getConfiguration() {
        return getConfigAs(AirGradientAPIConfiguration.class);
    }

    // Discovery

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        logger.debug("Getting supported services");
        return Set.of(AirGradientLocationDiscoveryService.class);
    }
}
