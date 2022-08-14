/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openhab.binding.sleepiq.api.BedNotFoundException;
import org.openhab.binding.sleepiq.api.Configuration;
import org.openhab.binding.sleepiq.api.LoginException;
import org.openhab.binding.sleepiq.api.SleepIQ;
import org.openhab.binding.sleepiq.api.UnauthorizedException;
import org.openhab.binding.sleepiq.api.filter.LoggingFilter;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.model.BedsResponse;
import org.openhab.binding.sleepiq.api.model.Failure;
import org.openhab.binding.sleepiq.api.model.FamilyStatus;
import org.openhab.binding.sleepiq.api.model.LoginInfo;
import org.openhab.binding.sleepiq.api.model.LoginRequest;
import org.openhab.binding.sleepiq.api.model.PauseMode;
import org.openhab.binding.sleepiq.api.model.Sleeper;
import org.openhab.binding.sleepiq.api.model.SleepersResponse;
import org.openhab.binding.sleepiq.internal.GsonProvider;

public class SleepIQImpl extends AbstractClient implements SleepIQ {
    protected static final String PARAM_KEY = "_k";

    protected static final String DATA_BED_ID = "bedId";

    protected final Configuration config;

    private volatile LoginInfo loginInfo;

    private final ClientBuilder clientBuilder;

    public SleepIQImpl(Configuration config, ClientBuilder clientBuilder) {
        this.config = config;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public LoginInfo login() throws LoginException {
        if (loginInfo == null) {
            synchronized (this) {
                if (loginInfo == null) {
                    Response response = getClient().target(config.getBaseUri()).path(Endpoints.login())
                            .request(MediaType.APPLICATION_JSON_TYPE).put(Entity.json(new LoginRequest()
                                    .withLogin(config.getUsername()).withPassword(config.getPassword())));

                    if (isUnauthorized(response)) {
                        throw new UnauthorizedException(response.readEntity(Failure.class));
                    }

                    if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                        throw new LoginException(response.readEntity(Failure.class));
                    }

                    // add the received cookies to all future requests
                    getClient().register(new ClientRequestFilter() {
                        @Override
                        public void filter(ClientRequestContext requestContext) throws IOException {
                            List<Object> cookies = response.getCookies().values().stream()
                                    .map(newCookie -> newCookie.toCookie()).collect(Collectors.toList());
                            requestContext.getHeaders().put("Cookie", cookies);
                        }
                    });

                    loginInfo = response.readEntity(LoginInfo.class);
                }
            }
        }

        return loginInfo;
    }

    @Override
    public List<Bed> getBeds() {
        return getSessionResponse(this::getBedsResponse).readEntity(BedsResponse.class).getBeds();
    }

    protected Response getBedsResponse(Map<String, Object> data) throws LoginException {
        LoginInfo login = login();
        return getClient().target(config.getBaseUri()).path(Endpoints.bed()).queryParam(PARAM_KEY, login.getKey())
                .request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    @Override
    public List<Sleeper> getSleepers() {
        return getSessionResponse(this::getSleepersResponse).readEntity(SleepersResponse.class).getSleepers();
    }

    protected Response getSleepersResponse(Map<String, Object> data) throws LoginException {
        LoginInfo login = login();
        return getClient().target(config.getBaseUri()).path(Endpoints.sleeper()).queryParam(PARAM_KEY, login.getKey())
                .request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    @Override
    public FamilyStatus getFamilyStatus() {
        return getSessionResponse(this::getFamilyStatusResponse).readEntity(FamilyStatus.class);
    }

    protected Response getFamilyStatusResponse(Map<String, Object> data) throws LoginException {
        LoginInfo login = login();
        return getClient().target(config.getBaseUri()).path(Endpoints.bed()).path(Endpoints.familyStatus())
                .queryParam(PARAM_KEY, login.getKey()).request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    @Override
    public PauseMode getPauseMode(String bedId) throws BedNotFoundException {
        Map<String, Object> data = new HashMap<>();
        data.put(DATA_BED_ID, bedId);

        Response response = getSessionResponse(this::getPauseModeResponse, data);

        if (!Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new BedNotFoundException(response.readEntity(Failure.class));
        }

        return response.readEntity(PauseMode.class);
    }

    protected Response getPauseModeResponse(Map<String, Object> data) throws LoginException {
        LoginInfo login = login();
        return getClient().target(config.getBaseUri()).path(Endpoints.bed()).path(data.get(DATA_BED_ID).toString())
                .path(Endpoints.pauseMode()).queryParam(PARAM_KEY, login.getKey())
                .request(MediaType.APPLICATION_JSON_TYPE).get();
    }

    protected boolean isUnauthorized(Response response) {
        return Status.UNAUTHORIZED.getStatusCode() == response.getStatusInfo().getStatusCode();
    }

    protected synchronized void resetLogin() {
        loginInfo = null;
    }

    protected Response getSessionResponse(Request request) {
        return getSessionResponse(request, Collections.emptyMap());
    }

    protected Response getSessionResponse(Request request, Map<String, Object> data) {
        try {
            Response response = request.execute(data);

            if (isUnauthorized(response)) {
                // session timed out
                response.close();
                resetLogin();
                response = request.execute(data);
            }

            return response;
        } catch (LoginException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected Client createClient() {
        // setup Gson (de)serialization
        GsonProvider<Object> gsonProvider = new GsonProvider<>(getGson());
        clientBuilder.register(gsonProvider);

        // turn on logging if requested
        if (config.isLogging()) {
            clientBuilder.register(new LoggingFilter(Logger.getLogger(SleepIQImpl.class.getName()), true));
        }

        return clientBuilder.build();
    }

    @FunctionalInterface
    public static interface Request {
        public Response execute(Map<String, Object> data) throws LoginException;
    }
}
