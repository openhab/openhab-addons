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
package org.openhab.binding.generacmobilelink.internal.handler;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.util.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openhab.binding.generacmobilelink.internal.GeneracMobileLinkBindingConstants;
import org.openhab.binding.generacmobilelink.internal.config.GeneracMobileLinkAccountConfiguration;
import org.openhab.binding.generacmobilelink.internal.config.GeneracMobileLinkGeneratorConfiguration;
import org.openhab.binding.generacmobilelink.internal.discovery.GeneracMobileLinkDiscoveryService;
import org.openhab.binding.generacmobilelink.internal.dto.Apparatus;
import org.openhab.binding.generacmobilelink.internal.dto.ApparatusDetail;
import org.openhab.binding.generacmobilelink.internal.dto.SelfAssertedResponse;
import org.openhab.binding.generacmobilelink.internal.dto.SignInConfig;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link GeneracMobileLinkAccountHandler} is responsible for connecting to the MobileLink cloud service and
 * discovering generator things
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class GeneracMobileLinkAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(GeneracMobileLinkAccountHandler.class);
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    private static final String API_BASE = "https://app.mobilelinkgen.com/api";
    private static final String LOGIN_BASE = "https://generacconnectivity.b2clogin.com/generacconnectivity.onmicrosoft.com/B2C_1A_MobileLink_SignIn";
    private static final Pattern SETTINGS_PATTERN = Pattern.compile("^var SETTINGS = (.*);$", Pattern.MULTILINE);
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type,
                    jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .create();
    private HttpClient httpClient;
    private GeneracMobileLinkDiscoveryService discoveryService;
    private Map<String, Apparatus> apparatusesCache = new HashMap<String, Apparatus>();
    private int refreshIntervalSeconds = 60;
    private boolean loggedIn;

    private @Nullable Future<?> pollFuture;

    public GeneracMobileLinkAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory,
            GeneracMobileLinkDiscoveryService discoveryService) {
        super(bridge);
        this.discoveryService = discoveryService;
        httpClient = httpClientFactory.createHttpClient(GeneracMobileLinkBindingConstants.BINDING_ID);
        httpClient.setFollowRedirects(true);
        // We have to send a very large amount of cookies which exceeds the default buffer size
        httpClient.setRequestBufferSize(16348);
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new IllegalStateException("Error starting custom HttpClient", e);
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        stopOrRestartPoll(true);
    }

    @Override
    public void dispose() {
        stopOrRestartPoll(false);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("Could not stop HttpClient", e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            try {
                updateGeneratorThings();
            } catch (IOException | SessionExpiredException e) {
                logger.debug("Could refresh things", e);
            }
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        logger.debug("childHandlerInitialized {}", childThing.getUID());
        String id = childThing.getConfiguration().as(GeneracMobileLinkGeneratorConfiguration.class).generatorId;
        Apparatus apparatus = apparatusesCache.get(id);
        if (apparatus == null) {
            logger.debug("No device for id {}", id);
            return;
        }
        try {
            updateGeneratorThing(childHandler, apparatus);
        } catch (IOException | SessionExpiredException e) {
            logger.debug("Could not initialize child", e);
        }
    }

    private synchronized void stopOrRestartPoll(boolean restart) {
        Future<?> pollFuture = this.pollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
            this.pollFuture = null;
        }
        if (restart) {
            this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, refreshIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    private void poll() {
        try {
            if (!loggedIn) {
                login();
            }
            loggedIn = true;
            updateGeneratorThings();
        } catch (IOException e) {
            logger.debug("Could not update devices", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing.generacmobilelink.account.offline.communication-error.io-exception");
        } catch (SessionExpiredException e) {
            logger.debug("Session expired", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing.generacmobilelink.account.offline.communication-error.session-expired");
            loggedIn = false;
        } catch (InvalidCredentialsException e) {
            logger.debug("Credentials Invalid", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing.generacmobilelink.account.offline.configuration-error.invalid-credentials");
            loggedIn = false;
            // we don't want to continue polling with bad credentials
            stopOrRestartPoll(false);
        }
    }

    private void updateGeneratorThings() throws IOException, SessionExpiredException {
        Apparatus[] apparatuses = getEndpoint(Apparatus[].class, "/v2/Apparatus/list");
        if (apparatuses == null) {
            logger.debug("Could not decode apparatuses response");
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        for (Apparatus apparatus : apparatuses) {
            if (apparatus.type != 0) {
                logger.debug("Unknown apparatus type {} {}", apparatus.type, apparatus.name);
                continue;
            }

            String id = String.valueOf(apparatus.apparatusId);
            apparatusesCache.put(id, apparatus);

            Optional<Thing> thing = getThing().getThings().stream().filter(
                    t -> t.getConfiguration().as(GeneracMobileLinkGeneratorConfiguration.class).generatorId.equals(id))
                    .findFirst();
            if (thing.isEmpty()) {
                discoveryService.generatorDiscovered(apparatus, getThing().getUID());
            } else {
                ThingHandler handler = thing.get().getHandler();
                if (handler != null) {
                    updateGeneratorThing(handler, apparatus);
                }
            }
        }
    }

    private void updateGeneratorThing(ThingHandler handler, Apparatus apparatus)
            throws IOException, SessionExpiredException {
        ApparatusDetail detail = getEndpoint(ApparatusDetail.class, "/v1/Apparatus/details/" + apparatus.apparatusId);
        if (detail != null) {
            ((GeneracMobileLinkGeneratorHandler) handler).updateGeneratorStatus(apparatus, detail);
        } else {
            logger.debug("Could not decode apparatuses detail response");
        }
    }

    private @Nullable <T> T getEndpoint(Class<T> clazz, String endpoint) throws IOException, SessionExpiredException {
        try {
            ContentResponse response = httpClient.newRequest(API_BASE + endpoint).send();
            if (response.getStatus() == 204) {
                // no data
                return null;
            }
            if (response.getStatus() != 200) {
                throw new SessionExpiredException("API returned status code: " + response.getStatus());
            }
            String data = response.getContentAsString();
            logger.debug("getEndpoint {}", data);
            return GSON.fromJson(data, clazz);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Attempts to login through a Microsoft Azure implicit grant oauth flow
     *
     * @throws IOException if there is a problem communicating or parsing the responses
     * @throws InvalidCredentialsException If Azure rejects the login credentials.
     */
    private synchronized void login() throws IOException, InvalidCredentialsException {
        logger.debug("Attempting login");
        GeneracMobileLinkAccountConfiguration config = getConfigAs(GeneracMobileLinkAccountConfiguration.class);
        refreshIntervalSeconds = config.refreshInterval;
        try {
            ContentResponse signInResponse = httpClient.newRequest(API_BASE + "/Auth/SignIn?email=" + config.username)
                    .send();

            String responseData = signInResponse.getContentAsString();
            logger.trace("response data: {}", responseData);

            // If we are immediately returned a submit form, it means our cookies are still valid with the identity
            // provider and we can just try and submit to the API service
            if (submitPage(responseData)) {
                return;
            }

            // Azure wants us to login again, look for the SETTINGS javascript in the page
            Matcher matcher = SETTINGS_PATTERN.matcher(responseData);
            if (!matcher.find()) {
                throw new IOException("Could not find settings string");
            }

            String parseSettings = matcher.group(1);
            logger.debug("parseSettings: {}", parseSettings);
            SignInConfig signInConfig = GSON.fromJson(parseSettings, SignInConfig.class);

            if (signInConfig == null) {
                throw new IOException("Could not parse settings string");
            }

            Fields fields = new Fields();
            fields.put("request_type", "RESPONSE");
            fields.put("signInName", config.username);
            fields.put("password", config.password);

            Request selfAssertedRequest = httpClient.POST(LOGIN_BASE + "/SelfAsserted")
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).header("X-Csrf-Token", signInConfig.csrf)
                    .param("tx", "StateProperties=" + signInConfig.transId).param("p", "B2C_1A_SignUpOrSigninOnline")
                    .content(new FormContentProvider(fields));

            ContentResponse selfAssertedResponse = selfAssertedRequest.send();

            logger.debug("selfAssertedRequest response {}", selfAssertedResponse.getStatus());

            if (selfAssertedResponse.getStatus() != 200) {
                throw new IOException("SelfAsserted: Bad response status: " + selfAssertedResponse.getStatus());
            }

            SelfAssertedResponse sa = GSON.fromJson(selfAssertedResponse.getContentAsString(),
                    SelfAssertedResponse.class);

            if (sa == null) {
                throw new IOException("SelfAsserted Could not parse response JSON");
            }

            if (!"200".equals(sa.status)) {
                throw new InvalidCredentialsException("Invalid Credentials: " + sa.message);
            }

            Request confirmedRequest = httpClient.newRequest(LOGIN_BASE + "/api/CombinedSigninAndSignup/confirmed")
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).param("csrf_token", signInConfig.csrf)
                    .param("tx", "StateProperties=" + signInConfig.transId).param("p", "B2C_1A_SignUpOrSigninOnline");

            ContentResponse confirmedResponse = confirmedRequest.send();

            if (confirmedResponse.getStatus() != 200) {
                throw new IOException("CombinedSigninAndSignup bad response: " + confirmedResponse.getStatus());
            }

            String loginString = confirmedResponse.getContentAsString();
            logger.trace("confirmedResponse: {}", loginString);
            if (!submitPage(loginString)) {
                throw new IOException("Error parsing HTML submit form");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (ExecutionException | TimeoutException | JsonSyntaxException e) {
            throw new IOException(e);
        }
    }

    /**
     * Attempts to submit a HTML form from Azure to the Generac API, returns false if the HTML does not match the
     * required form
     *
     * @param loginString
     * @return false if the HTML is not a form, true if submission is successful
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws JsonSyntaxException
     * @throws IOException
     */
    private boolean submitPage(String loginString)
            throws ExecutionException, TimeoutException, InterruptedException, JsonSyntaxException, IOException {
        Document loginPage = Jsoup.parse(loginString);
        Element form = loginPage.select("form").first();
        Element loginState = loginPage.select("input[name=state]").first();
        Element loginCode = loginPage.select("input[name=code]").first();

        if (form == null || loginState == null || loginCode == null) {
            logger.debug("Could not load login page");
            return false;
        }

        // url that the form will submit to
        String action = form.attr("action");

        Fields fields = new Fields();
        fields.put("state", loginState.attr("value"));
        fields.put("code", loginCode.attr("value"));

        Request loginRequest = httpClient.POST(action).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .content(new FormContentProvider(fields));

        ContentResponse loginResponse = loginRequest.send();
        if (logger.isTraceEnabled()) {
            logger.trace("login response {} {}", loginResponse.getStatus(), loginResponse.getContentAsString());
        } else {
            logger.debug("login response status {}", loginResponse.getStatus());
        }
        if (loginResponse.getStatus() != 200) {
            throw new IOException("Bad api login resposne: " + loginResponse.getStatus());
        }
        return true;
    }

    private class InvalidCredentialsException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    private class SessionExpiredException extends Exception {
        private static final long serialVersionUID = 1L;

        public SessionExpiredException(String message) {
            super(message);
        }
    }
}
