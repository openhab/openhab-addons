/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.toon.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.toon.internal.api.Agreement;
import org.openhab.binding.toon.internal.api.ToonConnectionException;
import org.openhab.binding.toon.internal.api.ToonState;
import org.openhab.binding.toon.internal.config.ToonBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link ToonApiClient} class is capable of retrieving the current states and change the setpoint and thermostat
 * program. It is based on the legacy api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class ToonApiClient {
    private Logger logger = LoggerFactory.getLogger(ToonApiClient.class);

    private static String TOON_HOST = "https://toonopafstand.eneco.nl";
    private static String TOON_LOGIN_PATH = "/toonMobileBackendWeb/client/login";
    private static String TOON_LOGOUT_PATH = "/toonMobileBackendWeb/client/auth/logout";
    private static String TOON_START_PATH = "/toonMobileBackendWeb/client/auth/start";
    private static String TOON_UPDATE_PATH = "/toonMobileBackendWeb/client/auth/retrieveToonState";
    private static String TOON_TEMPSET_PATH = "/toonMobileBackendWeb/client/auth/setPoint";
    private static String TOON_CHANGE_SCHEME_PATH = "/toonMobileBackendWeb/client/auth/schemeState";
    private static String TOON_SWITCH_PLUG_PATH = "/toonMobileBackendWeb/client/auth/smartplug/setTarget";

    protected Client client = ClientBuilder.newClient();
    protected WebTarget toonTarget = client.target(TOON_HOST);
    protected WebTarget updateTarget = client.target(TOON_HOST);

    private String clientId;
    private String clientIdChecksum;

    private final JsonParser jsonParser;
    private final Gson gson;
    private final ToonBridgeConfiguration configuration;

    public ToonApiClient(ToonBridgeConfiguration configuration) {
        this.configuration = configuration;
        this.jsonParser = new JsonParser();
        this.gson = createGsonBuilder().create();
    }

    public void login() throws ToonConnectionException {
        logger.debug("login start");

        if (configuration == null || StringUtils.isEmpty(configuration.username)) {
            throw new ToonConnectionException("Username not provided");
        }
        if (StringUtils.isEmpty(configuration.password)) {
            throw new ToonConnectionException("Password not provided");
        }

        Form form = new Form();
        form.param("username", configuration.username);
        form.param("password", configuration.password);

        Response response = toonTarget.path(TOON_LOGIN_PATH).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        JsonObject json = validateResponse(response);
        // logger.debug("json {}", json);

        clientId = json.get("clientId").getAsString();
        clientIdChecksum = json.get("clientIdChecksum").getAsString();

        form = new Form();
        JsonObject agreement = json.getAsJsonArray("agreements").get(0).getAsJsonObject();
        form.param("agreementId", agreement.get("agreementId").getAsString());
        form.param("agreementIdChecksum", agreement.get("agreementIdChecksum").getAsString());
        form.param("clientId", clientId);
        form.param("clientIdChecksum", clientIdChecksum);
        form.param("random", UUID.randomUUID().toString());

        response = toonTarget.path(TOON_START_PATH).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        validateResponse(response);

        logger.debug("login ok");
    }

    public List<Agreement> getAgreements() throws ToonConnectionException {
        logger.debug("getAgreements start");

        if (configuration == null) {
            throw new ToonConnectionException("Configuration is missing or corrupted");
        } else if (StringUtils.isEmpty(configuration.username)) {
            throw new ToonConnectionException("Username not provided");
        } else if (StringUtils.isEmpty(configuration.password)) {
            throw new ToonConnectionException("Password not provided");
        }

        Form form = new Form();
        form.param("username", configuration.username);
        form.param("password", configuration.password);

        Response response = toonTarget.path(TOON_LOGIN_PATH).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        JsonObject json = validateResponse(response);
        logger.debug("json {}", json);
        JsonArray agreements = json.get("agreements").getAsJsonArray();
        if (agreements != null) {
            return Arrays.asList(gson.fromJson(agreements, Agreement[].class));
        }
        return Collections.emptyList();
    }

    private JsonObject validateResponse(Response response) throws ToonConnectionException {
        if (response.getStatus() != 200) {
            logger.debug("response status {}", response.getStatus());
            clientId = clientIdChecksum = null;
            throw new ToonConnectionException("invalid api response status: " + response.getStatus());
        }
        if (!response.hasEntity()) {
            logger.debug("empty response from api");
            return new JsonObject();
        }
        JsonObject json = jsonParser.parse(response.readEntity(String.class)).getAsJsonObject();
        if (!json.get("success").getAsBoolean()) {
            logger.debug("validateResponse {}", json);
            clientId = clientIdChecksum = null;
            throw new ToonConnectionException(json.get("reason").getAsString());
        }
        return json;
    }

    private ToonState getToonState() throws ToonConnectionException {
        logger.debug("get ToonState");
        if (clientId == null) {
            logger.debug("not logged in");
            return null;
        }
        Form form = new Form();
        form.param("clientId", clientId);
        form.param("clientIdChecksum", clientIdChecksum);
        form.param("random", UUID.randomUUID().toString());

        Response response = client.target(TOON_HOST).path(TOON_UPDATE_PATH).queryParam("clientId", clientId)
                .queryParam("clientIdChecksum", clientIdChecksum).request(MediaType.APPLICATION_JSON_TYPE).get();

        JsonObject json = validateResponse(response);
        logger.debug("toon state: {}", json);
        return gson.fromJson(json, ToonState.class);
    }

    public void logout() {
        logger.debug("logout");

        if (clientId == null) {
            logger.debug("not logged in");
            return;
        }

        Form form = new Form();
        form.param("clientId", clientId);
        form.param("clientIdChecksum", clientIdChecksum);
        form.param("random", UUID.randomUUID().toString());

        clientId = clientIdChecksum = null;

        toonTarget.path(TOON_LOGOUT_PATH).request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

    }

    public void setSetpoint(int value) throws ToonConnectionException {
        logger.debug("setSetpoint {}", value);

        if (clientId == null) {
            logger.debug("not logged in");
            return;
        }

        Response response = client.target(TOON_HOST).path(TOON_TEMPSET_PATH).queryParam("clientId", clientId)
                .queryParam("clientIdChecksum", clientIdChecksum).queryParam("value", String.format("%d", value))
                .queryParam("random", UUID.randomUUID().toString()).request(MediaType.APPLICATION_JSON_TYPE).get();

        validateResponse(response);
    }

    public void setSetpointMode(int value) throws ToonConnectionException {
        logger.debug("setSetpointMode {}", value);

        if (clientId == null) {
            logger.debug("not logged in");
            return;
        }
        Response response = client.target(TOON_HOST).path(TOON_CHANGE_SCHEME_PATH).queryParam("clientId", clientId)
                .queryParam("clientIdChecksum", clientIdChecksum).queryParam("state", "2")
                .queryParam("temperatureState", value).queryParam("random", UUID.randomUUID().toString())
                .request(MediaType.APPLICATION_JSON_TYPE).get();

        validateResponse(response);
    }

    public void setPlugState(int value, String uuid) throws ToonConnectionException {
        logger.debug("setPlugState {} {}", value, uuid);

        if (clientId == null) {
            logger.debug("not logged in");
            return;
        }
        Response response = client.target(TOON_HOST).path(TOON_SWITCH_PLUG_PATH).queryParam("clientId", clientId)
                .queryParam("clientIdChecksum", clientIdChecksum).queryParam("devUuid", uuid).queryParam("state", value)
                .queryParam("random", UUID.randomUUID().toString()).request(MediaType.APPLICATION_JSON_TYPE).get();

        validateResponse(response);
    }

    public ToonState collect() throws ToonConnectionException {
        try {
            if (clientId == null) {
                login();
            }
            return getToonState();
        } catch (ToonConnectionException e) {
            clientId = clientIdChecksum = null;
            throw e;
        } catch (Exception e) {
            clientId = clientIdChecksum = null;
            throw new ToonConnectionException(e.getMessage(), e);
        }
    }

    private GsonBuilder createGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
        return gsonBuilder;
    }

}
