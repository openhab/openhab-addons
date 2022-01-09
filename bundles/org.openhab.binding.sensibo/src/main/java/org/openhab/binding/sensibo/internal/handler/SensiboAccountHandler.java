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
package org.openhab.binding.sensibo.internal.handler;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import org.openhab.binding.sensibo.internal.SensiboCommunicationException;
import org.openhab.binding.sensibo.internal.SensiboConfigurationException;
import org.openhab.binding.sensibo.internal.SensiboException;
import org.openhab.binding.sensibo.internal.client.RequestLogger;
import org.openhab.binding.sensibo.internal.config.SensiboAccountConfiguration;
import org.openhab.binding.sensibo.internal.dto.AbstractRequest;
import org.openhab.binding.sensibo.internal.dto.deletetimer.DeleteTimerReponse;
import org.openhab.binding.sensibo.internal.dto.deletetimer.DeleteTimerRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.AcStateDTO;
import org.openhab.binding.sensibo.internal.dto.poddetails.GetPodsDetailsRequest;
import org.openhab.binding.sensibo.internal.dto.poddetails.PodDetailsDTO;
import org.openhab.binding.sensibo.internal.dto.pods.GetPodsRequest;
import org.openhab.binding.sensibo.internal.dto.pods.PodDTO;
import org.openhab.binding.sensibo.internal.dto.setacstateproperty.SetAcStatePropertyReponse;
import org.openhab.binding.sensibo.internal.dto.setacstateproperty.SetAcStatePropertyRequest;
import org.openhab.binding.sensibo.internal.dto.settimer.SetTimerReponse;
import org.openhab.binding.sensibo.internal.dto.settimer.SetTimerRequest;
import org.openhab.binding.sensibo.internal.model.AcState;
import org.openhab.binding.sensibo.internal.model.SensiboModel;
import org.openhab.binding.sensibo.internal.model.SensiboSky;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
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
    private static final int MIN_TIME_BETWEEEN_MODEL_UPDATES_MS = 30_000;
    private static final int SECONDS_IN_MINUTE = 60;
    public static String API_ENDPOINT = "https://home.sensibo.com/api";
    private final Logger logger = LoggerFactory.getLogger(SensiboAccountHandler.class);
    private final HttpClient httpClient;
    private final RequestLogger requestLogger;
    private final Gson gson;
    private SensiboModel model = new SensiboModel(0);
    private Optional<ScheduledFuture<?>> statusFuture = Optional.empty();
    private @NonNullByDefault({}) SensiboAccountConfiguration config;

    public SensiboAccountHandler(final Bridge bridge, final HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;

        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new TypeAdapter<ZonedDateTime>() {
            @Override
            public void write(JsonWriter out, @Nullable ZonedDateTime value) throws IOException {
                if (value != null) {
                    out.value(value.toString());
                }
            }

            @Override
            public @Nullable ZonedDateTime read(final JsonReader in) throws IOException {
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

    public SensiboAccountConfiguration loadConfigSafely() throws SensiboConfigurationException {
        SensiboAccountConfiguration loadedConfig = getConfigAs(SensiboAccountConfiguration.class);
        if (loadedConfig == null) {
            throw new SensiboConfigurationException("Could not load Sensibo account configuration");
        }

        return loadedConfig;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::initializeInternal);
    }

    private void initializeInternal() {
        try {
            config = loadConfigSafely();
            logger.debug("Initializing Sensibo Account bridge using config {}", config);
            model = refreshModel();
            updateStatus(ThingStatus.ONLINE);
            initPolling();
            logger.debug("Initialization of Sensibo account completed successfully for {}", config);
        } catch (final SensiboConfigurationException e) {
            logger.info("Error initializing Sensibo data: {}", e.getMessage());
            model = new SensiboModel(0); // Empty model
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Error fetching initial data: " + e.getMessage());
        } catch (final SensiboException e) {
            logger.info("Error initializing Sensibo data: {}", e.getMessage());
            model = new SensiboModel(0); // Empty model
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error fetching initial data: " + e.getMessage());
            // Reschedule init
            scheduler.schedule(this::initializeInternal, 30, TimeUnit.SECONDS);
        }
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
        statusFuture = Optional.of(scheduler.scheduleWithFixedDelay(this::updateModelFromServerAndUpdateThingStatus,
                config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS));
    }

    protected SensiboModel refreshModel() throws SensiboException {
        final SensiboModel updatedModel = new SensiboModel(System.currentTimeMillis());

        final GetPodsRequest getPodsRequest = new GetPodsRequest();
        final List<PodDTO> pods = sendRequest(buildRequest(getPodsRequest), getPodsRequest,
                new TypeToken<ArrayList<PodDTO>>() {
                }.getType());

        for (final PodDTO pod : pods) {
            final GetPodsDetailsRequest getPodsDetailsRequest = new GetPodsDetailsRequest(pod.id);

            final PodDetailsDTO podDetails = sendRequest(buildGetPodDetailsRequest(getPodsDetailsRequest),
                    getPodsDetailsRequest, new TypeToken<PodDetailsDTO>() {
                    }.getType());

            updatedModel.addPod(new SensiboSky(podDetails));
        }

        return updatedModel;
    }

    private <T> T sendRequest(final Request request, final AbstractRequest req, final Type responseType)
            throws SensiboException {
        try {
            final ContentResponse contentResponse = request.send();
            final String responseJson = contentResponse.getContentAsString();
            if (contentResponse.getStatus() == HttpStatus.OK_200) {
                final JsonObject o = JsonParser.parseString(responseJson).getAsJsonObject();
                final String overallStatus = o.get("status").getAsString();
                if ("success".equals(overallStatus)) {
                    return gson.fromJson(o.get("result"), responseType);
                } else {
                    throw new SensiboCommunicationException(req, overallStatus);
                }
            } else if (contentResponse.getStatus() == HttpStatus.FORBIDDEN_403) {
                throw new SensiboConfigurationException("Invalid API key");
            } else {
                throw new SensiboCommunicationException(
                        "Error sending request to Sensibo server. Server responded with " + contentResponse.getStatus()
                                + " and payload " + responseJson);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new SensiboCommunicationException(
                    String.format("Error sending request to Sensibo server: %s", e.getMessage()), e);
        }
    }

    /**
     * Stops this thing's polling future
     */
    private void stopPolling() {
        statusFuture.ifPresent(future -> {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
            statusFuture = Optional.empty();
        });
    }

    public void updateModelFromServerAndUpdateThingStatus() {
        if (allowModelUpdate()) {
            try {
                model = refreshModel();
                updateThingStatuses();
                updateStatus(ThingStatus.ONLINE);
            } catch (SensiboConfigurationException e) {
                logger.debug("Error updating Sensibo model do to {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            } catch (SensiboException e) {
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

    private Request buildGetPodDetailsRequest(final GetPodsDetailsRequest getPodsDetailsRequest) {
        final Request req = buildRequest(getPodsDetailsRequest);
        req.param("fields", "*");

        return req;
    }

    private Request buildRequest(final AbstractRequest req) {
        Request request = httpClient.newRequest(API_ENDPOINT + req.getRequestUrl()).param("apiKey", config.apiKey)
                .method(req.getMethod());

        if (!req.getMethod().contentEquals(HttpMethod.GET.asString())) { // POST, PATCH
            final String reqJson = gson.toJson(req);
            request = request.content(new BytesContentProvider(reqJson.getBytes(StandardCharsets.UTF_8)),
                    "application/json");
        }

        requestLogger.listenTo(request, new String[] { config.apiKey });

        return request;
    }

    public void updateSensiboSkyAcState(final String macAddress, String property, Object value,
            SensiboBaseThingHandler handler) {
        model.findSensiboSkyByMacAddress(macAddress).ifPresent(pod -> {
            try {
                SetAcStatePropertyRequest setAcStatePropertyRequest = new SetAcStatePropertyRequest(pod.getId(),
                        property, value);
                Request request = buildRequest(setAcStatePropertyRequest);
                SetAcStatePropertyReponse response = sendRequest(request, setAcStatePropertyRequest,
                        new TypeToken<SetAcStatePropertyReponse>() {
                        }.getType());

                model.updateAcState(macAddress, new AcState(response.acState));
                handler.updateState(model);
            } catch (SensiboException e) {
                logger.debug("Error setting ac state for {}", macAddress, e);
            }
        });
    }

    public void updateSensiboSkyTimer(final String macAddress, @Nullable Integer secondsFromNow) {
        model.findSensiboSkyByMacAddress(macAddress).ifPresent(pod -> {
            try {
                if (secondsFromNow != null && secondsFromNow >= SECONDS_IN_MINUTE) {
                    AcStateDTO offState = new AcStateDTO(pod.getAcState().get());
                    offState.on = false;

                    SetTimerRequest setTimerRequest = new SetTimerRequest(pod.getId(),
                            secondsFromNow / SECONDS_IN_MINUTE, offState);
                    Request request = buildRequest(setTimerRequest);
                    // No data in response
                    sendRequest(request, setTimerRequest, new TypeToken<SetTimerReponse>() {
                    }.getType());
                } else {
                    DeleteTimerRequest setTimerRequest = new DeleteTimerRequest(pod.getId());
                    Request request = buildRequest(setTimerRequest);
                    // No data in response
                    sendRequest(request, setTimerRequest, new TypeToken<DeleteTimerReponse>() {
                    }.getType());
                }
            } catch (SensiboException e) {
                logger.debug("Error setting timer for {}", macAddress, e);
            }
        });
    }
}
