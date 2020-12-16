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
package org.openhab.binding.generacmobilelink.internal.handler;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.generacmobilelink.internal.GeneracMobileLinkBindingConstants;
import org.openhab.binding.generacmobilelink.internal.config.GeneracMobileLinkAccountConfiguration;
import org.openhab.binding.generacmobilelink.internal.discovery.GeneracMobileLinkDiscoveryService;
import org.openhab.binding.generacmobilelink.internal.dto.GeneratorStatusDTO;
import org.openhab.binding.generacmobilelink.internal.dto.GeneratorStatusResponseDTO;
import org.openhab.binding.generacmobilelink.internal.dto.LoginRequestDTO;
import org.openhab.binding.generacmobilelink.internal.dto.LoginResponseDTO;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
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
            localPollFuture.cancel(false);
        }
    }

    private void restartPoll() {
        stopPoll();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshIntervalSeconds, TimeUnit.SECONDS);
    }

    private void poll() {
        // if our token is null we need to login
        if (authToken == null) {
            logger.debug("login");
            login();
        }

        // if we now have a token, get our data
        if (authToken != null) {
            getStatuses(true);
        }
    }

    private void login() {
        try {
            GeneracMobileLinkAccountConfiguration config = getConfigAs(GeneracMobileLinkAccountConfiguration.class);
            refreshIntervalSeconds = config.refreshInterval;
            ContentResponse contentResponse = httpClient.newRequest(BASE_URL + "/Users/login").method(HttpMethod.POST)
                    .content(
                            new StringContentProvider(
                                    gson.toJson(new LoginRequestDTO(SHARED_KEY, config.username, config.password))),
                            "application/json")
                    .timeout(10, TimeUnit.SECONDS).send();
            int statusCode = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.trace("LoginResponse - status: {} content: {}", statusCode, content);
            switch (statusCode) {
                case HttpStatus.OK_200:
                    LoginResponseDTO loginResponse = gson.fromJson(content, LoginResponseDTO.class);
                    if (loginResponse != null) {
                        authToken = loginResponse.authToken;
                        updateStatus(ThingStatus.ONLINE);
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Invalid Response Body");
                    }
                    break;
                case HttpStatus.UNAUTHORIZED_401:
                    // the server responds with a 500 error in some cases when credentials are not correct
                case HttpStatus.INTERNAL_SERVER_ERROR_500:
                    // do not continue to poll with bad credentials since this requires user intervention
                    stopPoll();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Unauthorized - Check Credentials");
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Invalid Response Code " + statusCode);
            }
        } catch (ExecutionException e) {
            // MobileLink response will trigger a Jetty "Authentication challenge without WWW-Authenticate header"
            // ExecutionException if the password is not right, this still does not looked to be fixed in jetty
            // see https://github.com/eclipse/jetty.project/issues/1555
            String message = e.getMessage();
            if (message != null && message.contains("Authentication challenge without WWW-Authenticate header")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Unauthorized - Check Credentials");
                stopPoll();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        } catch (InterruptedException | TimeoutException e) {
            logger.debug("Could not login", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private void getStatuses(boolean retry) {
        try {
            ContentResponse contentResponse = httpClient.newRequest(BASE_URL + "/Generator/GeneratorStatus")
                    .method(HttpMethod.GET).timeout(10, TimeUnit.SECONDS).header("AuthToken", authToken).send();
            int httpStatus = contentResponse.getStatus();
            String content = contentResponse.getContentAsString();
            logger.trace("GeneratorStatusResponse - status: {} content: {}", httpStatus, content);
            switch (httpStatus) {
                case HttpStatus.OK_200:
                    generators = gson.fromJson(content, GeneratorStatusResponseDTO.class);
                    updateGeneratorThings();
                    if (getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                    break;
                case HttpStatus.UNAUTHORIZED_401:
                    authToken = null;
                    restartPoll();
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Invalid Return Code " + httpStatus);
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.debug("Could not get statuses ", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        } catch (TimeoutException e) {
            logger.debug("Could not get statuses ", e);
            // the API seems to time out on this call frequently, although recovers after trying again
            if (retry) {
                logger.debug("Retying status request");
                getStatuses(false);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        }
    }

    private void updateGeneratorThings() {
        GeneratorStatusResponseDTO generatorsLocal = generators;
        if (generatorsLocal != null) {
            generatorsLocal.forEach(generator -> {
                Thing thing = getThing().getThing(new ThingUID(GeneracMobileLinkBindingConstants.THING_TYPE_GENERATOR,
                        getThing().getUID(), String.valueOf(generator.gensetID)));
                if (thing == null) {
                    generatorDiscovered(generator);
                } else {
                    ThingHandler handler = thing.getHandler();
                    if (handler != null) {
                        ((GeneracMobileLinkGeneratorHandler) handler).updateGeneratorStatus(generator);
                    }
                }
            });
        }
    }

    private void generatorDiscovered(GeneratorStatusDTO generator) {
        DiscoveryResult result = DiscoveryResultBuilder
                .create(new ThingUID(GeneracMobileLinkBindingConstants.THING_TYPE_GENERATOR, getThing().getUID(),
                        String.valueOf(generator.gensetID)))
                .withLabel("MobileLink Generator " + generator.generatorName)
                .withProperty("generatorId", String.valueOf(generator.gensetID))
                .withRepresentationProperty("generatorId").withBridge(getThing().getUID()).build();
        discoveryService.generatorDiscovered(result);
    }
}
