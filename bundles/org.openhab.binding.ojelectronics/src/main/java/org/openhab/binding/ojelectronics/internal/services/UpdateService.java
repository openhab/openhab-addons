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
package org.openhab.binding.ojelectronics.internal.services;

import java.util.List;

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
import org.openhab.binding.ojelectronics.internal.models.Thermostat;
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

    private final Gson gson = OJGSonBuilder.getGSon();
    private final Logger logger = LoggerFactory.getLogger(UpdateService.class);

    private final String sessionId;
    private final HttpClient httpClient;
    private final OJElectronicsBridgeConfiguration configuration;

    public UpdateService(OJElectronicsBridgeConfiguration configuration, HttpClient httpClient, String sessionId) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.sessionId = sessionId;
    }

    /**
     * Sends all changes of all {@link ThermostatHandler} to the API
     *
     * @param things
     */
    public void updateAllThermostats(List<Thing> things) {
        things.stream().filter(thing -> thing.getHandler() instanceof ThermostatHandler)
                .map(thing -> (ThermostatHandler) thing.getHandler())
                .map(handler -> handler.tryHandleAndGetUpdatedThermostat()).forEach(this::updateThermostat);
    }

    private void updateThermostat(@Nullable Thermostat thermostat) {
        if (thermostat == null) {
            return;
        }
        Request request = httpClient.POST(configuration.apiUrl + "/Thermostat/UpdateThermostat")
                .param("sessionid", sessionId).header(HttpHeader.CONTENT_TYPE, "application/json")
                .content(new StringContentProvider(
                        gson.toJson(new UpdateThermostatRequestModel(thermostat).withApiKey(configuration.apiKey))));

        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(@Nullable Result result) {
                if (result != null) {
                    logger.trace("onComplete {}", result);
                    if (result.isFailed()) {
                        logger.warn("updateThermostat failed {}", thermostat);
                    }
                    SimpleResponseModel responseModel = gson.fromJson(getContentAsString(), SimpleResponseModel.class);
                    if (responseModel == null) {
                        logger.warn("updateThermostat failed with empty result {}", thermostat);
                    } else if (responseModel.errorCode != 0) {
                        logger.warn("updateThermostat failed with errorCode {} {}", responseModel.errorCode,
                                thermostat);
                    }
                }
            }
        });
    }
}
