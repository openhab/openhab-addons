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
package org.openhab.binding.generacmobilelink.internal.handler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.generacmobilelink.internal.GeneracMobileLinkBindingConstants;
import org.openhab.binding.generacmobilelink.internal.config.GeneracMobileLinkAccountConfiguration;
import org.openhab.binding.generacmobilelink.internal.discovery.GeneracMobileLinkDiscoveryService;
import org.openhab.binding.generacmobilelink.internal.dto.ErrorResponseDTO;
import org.openhab.binding.generacmobilelink.internal.dto.GeneratorStatusDTO;
import org.openhab.binding.generacmobilelink.internal.dto.GeneratorStatusResponseDTO;
import org.openhab.binding.generacmobilelink.internal.dto.LoginRequestDTO;
import org.openhab.binding.generacmobilelink.internal.dto.LoginResponseDTO;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link GeneracMobileLinkAccountHandler} is responsible for connecting to the MobileLink cloud service and
 * discovering generator things
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneracMobileLinkAccountHandler extends BaseBridgeHandler {
    private static final String BASE_URL = "https://api.mobilelinkgen.com";
    private static final String SHARED_KEY = "GeneseeDepot13";
    private final Logger logger = LoggerFactory.getLogger(GeneracMobileLinkAccountHandler.class);
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    private @Nullable Future<?> pollFuture;
    private @Nullable String authToken;
    private @Nullable GeneratorStatusResponseDTO generators;
    private GeneracMobileLinkDiscoveryService discoveryService;
    private HttpClient httpClient;
    private int refreshIntervalSeconds = 60;

    public GeneracMobileLinkAccountHandler(Bridge bridge, HttpClient httpClient,
            GeneracMobileLinkDiscoveryService discoveryService) {
        super(bridge);
        this.httpClient = httpClient;
        this.discoveryService = discoveryService;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        authToken = null;
        restartPoll();
    }

    @Override
    public void dispose() {
        stopPoll();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateGeneratorThings();
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        GeneratorStatusResponseDTO generatorsLocal = generators;
        if (generatorsLocal != null) {
            Optional<GeneratorStatusDTO> generatorOpt = generatorsLocal.stream()
                    .filter(g -> String.valueOf(g.gensetID).equals(childThing.getUID().getId())).findFirst();
            if (generatorOpt.isPresent()) {
                ((GeneracMobileLinkGeneratorHandler) childHandler).updateGeneratorStatus(generatorOpt.get());
            }
        }
    }

    private void stopPoll() {
        Future<?> localPollFuture = pollFuture;
        if (localPollFuture != null) {
            localPollFuture.cancel(true);
        }
    }

    private void restartPoll() {
        stopPoll();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            if (authToken == null) {
                logger.debug("Attempting Login");
                login();
            }
            getStatuses(true);
        } catch (InterruptedException e) {
        }
    }

    private synchronized void login() throws InterruptedException {
        GeneracMobileLinkAccountConfiguration config = getConfigAs(GeneracMobileLinkAccountConfiguration.class);
        refreshIntervalSeconds = config.refreshInterval;
        HTTPResult result = sendRequest(BASE_URL + "/Users/login", HttpMethod.POST, null,
                new StringContentProvider(
                        gson.toJson(new LoginRequestDTO(SHARED_KEY, config.username, config.password))),
                "application/json");
        if (result.responseCode == HttpStatus.OK_200) {
            LoginResponseDTO loginResponse = gson.fromJson(result.content, LoginResponseDTO.class);
            if (loginResponse != null) {
                authToken = loginResponse.authToken;
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            handleErrorResponse(result);
            if (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
                // bad credentials, stop trying to login
                stopPoll();
            }
        }
    }

    private void getStatuses(boolean retry) throws InterruptedException {
        if (authToken == null) {
            return;
        }
        HTTPResult result = sendRequest(BASE_URL + "/Generator/GeneratorStatus", HttpMethod.GET, authToken, null, null);
        if (result.responseCode == HttpStatus.OK_200) {
            generators = gson.fromJson(result.content, GeneratorStatusResponseDTO.class);
            updateGeneratorThings();
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            if (retry) {
                logger.debug("Retrying status request");
                getStatuses(false);
            } else {
                handleErrorResponse(result);
            }
        }
    }

    private HTTPResult sendRequest(String url, HttpMethod method, @Nullable String token,
            @Nullable ContentProvider content, @Nullable String contentType) throws InterruptedException {
        try {
            Request request = httpClient.newRequest(url).method(method).timeout(10, TimeUnit.SECONDS);
            if (token != null) {
                request = request.header("AuthToken", token);
            }
            if (content != null & contentType != null) {
                request = request.content(content, contentType);
            }
            logger.trace("Sending {} to {}", request.getMethod(), request.getURI());
            final CompletableFuture<HTTPResult> futureResult = new CompletableFuture<>();
            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    futureResult.complete(new HTTPResult(result.getResponse().getStatus(), getContentAsString()));
                }
            });
            HTTPResult result = futureResult.get();
            logger.trace("Response - status: {} content: {}", result.responseCode, result.content);
            return result;
        } catch (ExecutionException e) {
            return new HTTPResult(0, e.getMessage());
        }
    }

    private void handleErrorResponse(HTTPResult result) {
        switch (result.responseCode) {
            case HttpStatus.UNAUTHORIZED_401:
                // the server responds with a 500 error in some cases when credentials are not correct
            case HttpStatus.INTERNAL_SERVER_ERROR_500:
                // server returned a valid error response
                ErrorResponseDTO error = gson.fromJson(result.content, ErrorResponseDTO.class);
                if (error != null && error.errorCode > 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Unauthorized: " + result.content);
                    authToken = null;
                    break;
                }
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, result.content);
        }
    }

    private void updateGeneratorThings() {
        GeneratorStatusResponseDTO generatorsLocal = generators;
        if (generatorsLocal != null) {
            generatorsLocal.forEach(generator -> {
                Thing thing = getThing().getThing(new ThingUID(GeneracMobileLinkBindingConstants.THING_TYPE_GENERATOR,
                        getThing().getUID(), String.valueOf(generator.gensetID)));
                if (thing == null) {
                    discoveryService.generatorDiscovered(generator, getThing().getUID());
                } else {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        ((GeneracMobileLinkGeneratorHandler) handler).updateGeneratorStatus(generator);
                    }
                }
            });
        }
    }

    public static class HTTPResult {
        public @Nullable String content;
        public final int responseCode;

        public HTTPResult(int responseCode, @Nullable String content) {
            this.responseCode = responseCode;
            this.content = content;
        }
    }
}
