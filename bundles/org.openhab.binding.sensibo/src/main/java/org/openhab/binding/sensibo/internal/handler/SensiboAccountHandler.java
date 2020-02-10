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
package org.openhab.binding.sensibo.internal.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sensibo.internal.SensiboCommunicationException;
import org.openhab.binding.sensibo.internal.client.RequestLogger;
import org.openhab.binding.sensibo.internal.config.SensiboAccountConfiguration;
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;
import org.openhab.binding.sensibo.internal.dto.deletetimer.DeleteTimerRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.GetPodsDetailsRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetails;
import org.openhab.binding.sensibo.internal.dto.pods.GetPodsRequest;
import org.openhab.binding.sensibo.internal.dto.pods.Pod;
import org.openhab.binding.sensibo.internal.dto.setacstateproperty.SetAcStatePropertyReponse;
import org.openhab.binding.sensibo.internal.dto.setacstateproperty.SetAcStatePropertyRequest;
import org.openhab.binding.sensibo.internal.dto.settimer.SetTimerReponse;
import org.openhab.binding.sensibo.internal.dto.settimer.SetTimerRequest;
import org.openhab.binding.sensibo.internal.model.AcState;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link SensiboAccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboAccountHandler extends BaseBridgeHandler {
    public static String API_ENDPOINT = "https://home.sensibo.com/api";
    private static final int MIN_TIME_BETWEEEN_MODEL_UPDATES_MS = 30_000;
    private final Logger logger = LoggerFactory.getLogger(SensiboAccountHandler.class);
    private final HttpClient httpClient;
    private RequestLogger requestLogger;
    private Gson gson;
    private SensiboModel model = new SensiboModel(0);
    private @Nullable ScheduledFuture<?> statusFuture;
    private @NonNullByDefault({}) SensiboAccountConfiguration config;

    public SensiboAccountHandler(final Bridge bridge, final HttpClient httpClient, final BundleContext context) {
        super(bridge);
        this.httpClient = httpClient;

        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(final @NonNullByDefault({}) JsonWriter out, final ZonedDateTime value)
                    throws IOException {
                out.value(value.toString());
            }

            @Override
            public ZonedDateTime read(final @NonNullByDefault({}) JsonReader in) throws IOException {
                return ZonedDateTime.parse(in.nextString());
            }
        }).setLenient().setPrettyPrinting().create();

        requestLogger = new RequestLogger(bridge.getUID().getId(), gson);
    }

    private boolean allowModelUpdate() {
        final long diffMsSinceLastUpdate = System.currentTimeMillis() - model.getLastUpdated();
        return diffMsSinceLastUpdate > MIN_TIME_BETWEEEN_MODEL_UPDATES_MS;
    }

    public SensiboModel getModel() {
        return model;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // Ignore commands as none are supported
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensiboAccountConfiguration.class);
        logger.debug("Initializing Sensibo Account bridge using config {}", config);
        scheduler.execute(() -> {
            try {
                model = refreshModel();
                updateStatus(ThingStatus.ONLINE);
                initPolling();
                logger.debug("Initialization of Sensibo account completed successfully for {}", config);
            } catch (final SensiboCommunicationException e) {
                logger.info("Error initializing Sensibo data: {}", e.getMessage());
                model = new SensiboModel(0); // Empty model
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error fetching initial data: " + e.getMessage());
                // Reschedule init
                scheduler.schedule(this::initialize, 30, TimeUnit.SECONDS);
            }
        });
    }

    @Override
    public void dispose() {
        stopPolling();
        super.dispose();
    }

    /**
     * starts this things polling future
     */
    private void initPolling() {
        stopPolling();
        statusFuture = scheduler.scheduleWithFixedDelay(this::updateModelFromServerAndUpdateThingStatus,
                config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
    }

    protected SensiboModel refreshModel() throws SensiboCommunicationException {
        final SensiboModel model = new SensiboModel(System.currentTimeMillis());

        final GetPodsRequest getPodsRequest = new GetPodsRequest();
        final List<Pod> pods = sendRequest(buildGetPodsRequest(getPodsRequest), getPodsRequest,
                new TypeToken<ArrayList<Pod>>() {
                }.getType());

        for (final Pod pod : pods) {
            final GetPodsDetailsRequest getPodsDetailsRequest = new GetPodsDetailsRequest(pod.id);

            final PodDetails podDetails = sendRequest(buildGetPodDetailsRequest(getPodsDetailsRequest),
                    getPodsDetailsRequest, new TypeToken<PodDetails>() {
                    }.getType());

            model.addPod(new SensiboSky(podDetails));
        }

        return model;
    }

    private <T> T sendRequest(final Request request, final AbstractRequest req, final Type responseType)
            throws SensiboCommunicationException {
        try {
            final ContentResponse contentResponse = request.send();
            final String responseJson = contentResponse.getContentAsString();
            if (contentResponse.getStatus() == HttpStatus.OK_200) {
                final JsonParser parser = new JsonParser();
                final JsonObject o = parser.parse(responseJson).getAsJsonObject();
                final String overallStatus = o.get("status").getAsString();
                if ("success".equals(overallStatus)) {
                    return gson.fromJson(o.get("result"), responseType);
                } else {
                    throw new SensiboCommunicationException(req, overallStatus);
                }
            } else if (contentResponse.getStatus() == HttpStatus.FORBIDDEN_403) {
                throw new SensiboCommunicationException("Invalid API key");
            } else {
                throw new SensiboCommunicationException(
                        "Error sending request to Sensibo server. Server responded with " + contentResponse.getStatus()
                                + " and payload " + responseJson);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SensiboCommunicationException(
                    String.format("Error sending request to Sensibo server: %s", e.getMessage()));
        }
    }

    /**
     * Stops this thing's polling future
     */
    @SuppressWarnings("null")
    private void stopPolling() {
        if (statusFuture != null && !statusFuture.isCancelled()) {
            statusFuture.cancel(true);
            statusFuture = null;
        }
    }

    public void updateModelFromServerAndUpdateThingStatus() {
        if (allowModelUpdate()) {
            try {
                model = refreshModel();
                updateThingStatuses();
                updateStatus(ThingStatus.ONLINE);
            } catch (SensiboCommunicationException e) {
                logger.debug("Error updating Sensibo model do to {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }
    }

    private void updateThingStatuses() {
        final List<Thing> subThings = getThing().getThings();
        for (final Thing thing : subThings) {
            final ThingHandler handler = thing.getHandler();
            if (handler != null) {
                final SensiboBaseThingHandler mHandler = (SensiboBaseThingHandler) handler;
                mHandler.updateState(model);
            }
        }
    }

    private Request buildGetPodsRequest(final GetPodsRequest req) {
        return buildRequest(req);
    }

    private Request buildGetPodDetailsRequest(final GetPodsDetailsRequest getPodsDetailsRequest) {
        final Request req = buildRequest(getPodsDetailsRequest);
        req.param("fields", "*");

        return req;
    }

    private Request buildSetAcStatePropertyRequest(SetAcStatePropertyRequest setAcStateRequest) {
        return buildRequest(setAcStateRequest);
    }

    private Request buildSetTimerRequest(SetTimerRequest setTimerRequest) {
        return buildRequest(setTimerRequest);
    }

    private Request buildDeleteTimerRequest(DeleteTimerRequest deleteTimerRequest) {
        return buildRequest(deleteTimerRequest);
    }

    private Request buildRequest(final AbstractRequest req) {
        Request request = httpClient.newRequest(API_ENDPOINT + req.getRequestUrl()).param("apiKey", config.apiKey)
                .method(req.getMethod());

        if (!req.getMethod().contentEquals(HttpMethod.GET.asString())) { // POST, PATCH
            final String reqJson = gson.toJson(req);
            request = request.content(new BytesContentProvider(reqJson.getBytes(StandardCharsets.UTF_8)),
                    "application/json");
        }

        requestLogger.listenTo(request);

        return request;
    }

    public void updateSensiboSkyAcState(final String macAddress, String property, Object value,
            SensiboBaseThingHandler handler) {
        model.findSensiboSkyByMacAddress(macAddress).ifPresent(pod -> {
            try {
                SetAcStatePropertyRequest setAcStatePropertyRequest = new SetAcStatePropertyRequest(pod.getId(),
                        property, value);
                Request request = buildSetAcStatePropertyRequest(setAcStatePropertyRequest);
                SetAcStatePropertyReponse response = sendRequest(request, setAcStatePropertyRequest,
                        new TypeToken<SetAcStatePropertyReponse>() {
                        }.getType());

                model.updateAcState(macAddress, new AcState(response.acState));
                handler.updateState(model);
            } catch (SensiboCommunicationException e) {
                logger.debug("Error setting ac state for {}", macAddress, e);
            }
        });
    }

    public void updateSensiboSkyTimer(final String macAddress, @Nullable Integer secondsFromNow) {
        model.findSensiboSkyByMacAddress(macAddress).ifPresent(pod -> {
            try {
                if (secondsFromNow != null && secondsFromNow >= 60) {
                    org.openhab.binding.sensibo.internal.dto.poddetails.AcState offState = new org.openhab.binding.sensibo.internal.dto.poddetails.AcState(
                            pod.getAcState().get());
                    offState.on = false;

                    SetTimerRequest setTimerRequest = new SetTimerRequest(pod.getId(), secondsFromNow / 60, offState);
                    Request request = buildSetTimerRequest(setTimerRequest);
                    // No data in response
                    sendRequest(request, setTimerRequest, new TypeToken<SetTimerReponse>() {
                    }.getType());
                } else {
                    DeleteTimerRequest setTimerRequest = new DeleteTimerRequest(pod.getId());
                    Request request = buildDeleteTimerRequest(setTimerRequest);
                    // No data in response
                    sendRequest(request, setTimerRequest, new TypeToken<SetTimerReponse>() {
                    }.getType());
                }
            } catch (SensiboCommunicationException e) {
                logger.debug("Error setting timer for {}", macAddress, e);
            }
        });
    }
}
