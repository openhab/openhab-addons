/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal.services;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.ojelectronics.internal.ThermostatHandler;
import org.openhab.binding.ojelectronics.internal.common.OJGSonBuilder;
import org.openhab.binding.ojelectronics.internal.config.OJElectronicsBridgeConfiguration;
import org.openhab.binding.ojelectronics.internal.models.SimpleResponseModel;
import org.openhab.binding.ojelectronics.internal.models.thermostat.ThermostatModel;
import org.openhab.binding.ojelectronics.internal.models.thermostat.UpdateThermostatRequestModel;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles the update of the devices of a session
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
public final class UpdateService {
    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private final Gson gson = OJGSonBuilder.getGSon();
    private final Logger logger = LoggerFactory.getLogger(UpdateService.class);

    private final HttpClient httpClient;
    private final OJElectronicsBridgeConfiguration configuration;
    private final Runnable unauthorized;
    private final Consumer<@Nullable String> connectionLost;

    public UpdateService(OJElectronicsBridgeConfiguration configuration, HttpClient httpClient,
            Consumer<@Nullable String> connectionLost, Runnable unauthorized) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.unauthorized = unauthorized;
        this.connectionLost = connectionLost;
    }

    /**
     * Sends all changes of all {@link ThermostatHandler} to the API
     *
     * @param things
     */
    public void updateAllThermostats(List<Thing> things) {
        new SignInService(configuration, httpClient).signIn((sessionId) -> updateAllThermostats(things, sessionId),
                connectionLost, unauthorized);
    }

    private void updateAllThermostats(List<Thing> things, String sessionId) {
        things.stream().filter(thing -> thing.getHandler() instanceof ThermostatHandler)
                .map(thing -> (ThermostatHandler) thing.getHandler())
                .map(handler -> handler.tryHandleAndGetUpdatedThermostat())
                .forEach((thermostat) -> updateThermostat(thermostat, sessionId));
    }

    private void updateThermostat(@Nullable ThermostatModel thermostat, String sessionId) {
        if (thermostat == null) {
            return;
        }
        String jsonPayload = gson.toJson(new UpdateThermostatRequestModel(thermostat).withApiKey(configuration.apiKey));
        Request request = httpClient.POST(configuration.getRestApiUrl() + "/Thermostat/UpdateThermostat")
                .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).param("sessionid", sessionId)
                .header(HttpHeader.CONTENT_TYPE, "application/json").content(new StringContentProvider(jsonPayload));
        logger.trace("updateThermostat payload for themostat with serial {} is {}", thermostat.serialNumber,
                jsonPayload);

        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (result != null) {
                    logger.trace("onComplete Http Status {} {}", result.getResponse().getStatus(), result);
                    if (result.isFailed()) {
                        logger.warn("updateThermostat failed for themostat with serial {}", thermostat.serialNumber);
                        return;
                    }
                    SimpleResponseModel responseModel = Objects
                            .requireNonNull(gson.fromJson(getContentAsString(), SimpleResponseModel.class));
                    if (responseModel.errorCode != 0) {
                        logger.warn("updateThermostat failed with errorCode {} for thermostat with serial {}",
                                responseModel.errorCode, thermostat.serialNumber);
                    }
                }
            }
        });
    }
}
