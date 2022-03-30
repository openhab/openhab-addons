/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants;
import org.openhab.binding.boschspexor.internal.api.model.ObservationChangeStatus;
import org.openhab.binding.boschspexor.internal.api.model.ObservationChangeStatus.StatusCode;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus.ObservationType;
import org.openhab.binding.boschspexor.internal.api.model.ObservationStatus.SensorMode;
import org.openhab.binding.boschspexor.internal.api.model.SensorType;
import org.openhab.binding.boschspexor.internal.api.model.SensorValue;
import org.openhab.binding.boschspexor.internal.api.model.Spexor;
import org.openhab.binding.boschspexor.internal.api.model.SpexorInfo;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;

/**
 * Bosch Spexor API Service
 * calls the endpoint to determine information about spexors
 *
 * @author Marc Fischer
 *
 */
@NonNullByDefault
public class SpexorAPIService {
    private final Logger logger = LoggerFactory.getLogger(SpexorAPIService.class);

    private final SpexorAuthorizationService authService;
    private ObjectMapper mapper = new ObjectMapper();

    public SpexorAPIService(SpexorAuthorizationService authService) {
        this.authService = authService;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    public List<Spexor> getSpexors() {
        Optional<Request> request = authService.newRequest(BoschSpexorBindingConstants.ENDPOINT_SPEXORS);
        if (request.isEmpty()) {
            return Collections.emptyList();
        } else {
            try {
                return send(request.get(), new TypeReference<List<Spexor>>() {
                });
            } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("failed to get '{}' : {}", request.get().getURI(), e.getMessage(), e);
                return Collections.emptyList();
            }
        }
    }

    @Nullable
    public SpexorInfo getSpexor(String id) {
        Optional<Request> request = authService.newRequest(BoschSpexorBindingConstants.ENDPOINT_SPEXOR, id);
        if (request.isEmpty()) {
            return null;
        } else {
            try {
                return send(request.get(), new TypeReference<SpexorInfo>() {
                });
            } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("failed to get '{}' : {}", request.get().getURI(), e.getMessage(), e);
                return null;
            }
        }
    }

    public Map<SensorType, SensorValue<?>> getSensorValues(String id, List<SensorType> sensors) {
        Map<@NonNull SensorType, @NonNull SensorValue<?>> values = new HashMap<SensorType, SensorValue<?>>();
        String keys = sensors.stream().map(SensorType::name).collect(Collectors.joining(","));
        Optional<Request> request = authService.newRequest(BoschSpexorBindingConstants.ENDPOINT_SPEXOR, id,
                BoschSpexorBindingConstants.ENDPOINT_SENSORVALUE, keys);
        if (request.isPresent()) {
            try {

                request.get().accept(MimeTypes.Type.APPLICATION_JSON.asString());
                ContentResponse response = request.get().send();
                logger.debug("received {} for request {} with content {}", response.getContentAsString(), request,
                        request.get().getContent());
                List<Map<String, String>> resp = mapper.readValue(response.getContent(),
                        mapper.getTypeFactory().constructCollectionType(ArrayList.class, HashMap.class));
                for (Map<String, String> map : resp) {
                    SensorType key = SensorType.valueOf(String.valueOf(map.get("key")));
                    if (SensorType.AirQualityLevel.equals(key)) {
                        SensorValue<String> value = mapper.convertValue(map, new TypeReference<SensorValue<String>>() {
                        });
                        values.put(key, value);
                    } else {
                        SensorValue<Integer> value = mapper.convertValue(map,
                                new TypeReference<SensorValue<Integer>>() {
                                });
                        values.put(key, value);
                    }
                }
            } catch (NullPointerException | InterruptedException | TimeoutException | ExecutionException
                    | IOException e) {
                logger.error("failed to get '{}' : {}", request.get().getURI(), e.getMessage(), e);
                return values;
            }
        }
        return values;
    }

    public ObservationChangeStatus setObservation(String id, ObservationType type, boolean enable) {
        Optional<Request> request = authService.newRequest(BoschSpexorBindingConstants.ENDPOINT_SPEXOR, id,
                BoschSpexorBindingConstants.ENDPOINT_OBSERVATION);
        if (request.isPresent()) {
            request.get().method(HttpMethod.PATCH);
            request.get().accept(MimeTypes.Type.APPLICATION_JSON.asString());
            request.get()
                    .content(new StringContentProvider(
                            "[{\"observationType\":\"" + type.name() + "\", \"sensorMode\":\""
                                    + (enable ? SensorMode.Activated : SensorMode.Deactivated).name() + "\"}]",
                            "UTF-8"), ContentType.APPLICATION_JSON.toString());
            try {
                List<ObservationChangeStatus> result = send(request.get(),
                        new TypeReference<List<ObservationChangeStatus>>() {
                        });
                if (result.size() > 0) {
                    for (ObservationChangeStatus observationChangeStatus : result) {
                        if (type.equals(observationChangeStatus.getObservationType())) {
                            return observationChangeStatus;
                        }
                    }
                }
            } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.error("failed to get '{}' : {}", request.get().getURI(), e.getMessage(), e);
                ObservationChangeStatus statusFailed = new ObservationChangeStatus();
                statusFailed.setObservationType(type);
                statusFailed.setStatusCode(StatusCode.FAILURE);
                statusFailed.setMessage(e.getLocalizedMessage());
                return statusFailed;
            }
        }
        ObservationChangeStatus statusFailed = new ObservationChangeStatus();
        statusFailed.setObservationType(type);
        statusFailed.setStatusCode(StatusCode.FAILURE);
        return statusFailed;
    }

    private <T> T send(Request request, TypeReference<T> clazzOfJson) throws JsonParseException, JsonMappingException,
            IOException, InterruptedException, TimeoutException, ExecutionException {
        request.accept(MimeTypes.Type.APPLICATION_JSON.asString());
        ContentResponse response = request.send();
        logger.debug("received {} for request {} with content {}", response.getContentAsString(), request,
                request.getContent());
        return mapper.readValue(response.getContent(), clazzOfJson);
    }
}
