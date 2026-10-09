/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
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
    private static final Duration LOGIN_RETRY_DELAY = Duration.ofMinutes(15);

    private static final String API_BASE = "https://app.mobilelinkgen.com/api";
    private static final String AUTH_BASE = "https://auth.ecobee.com";
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type,
                    jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .create();
    private HttpClient httpClient;
    private GeneracMobileLinkDiscoveryService discoveryService;
    private Map<String, Apparatus> apparatusesCache = new HashMap<>();
    private int refreshIntervalSeconds = 60;
    private boolean loggedIn;
    private Instant nextLoginAttempt = Instant.MIN;

    private @Nullable Future<?> pollFuture;

    public GeneracMobileLinkAccountHandler(Bridge bridge, HttpClientFactory httpClientFactory,
            GeneracMobileLinkDiscoveryService discoveryService) {
        super(bridge);
        this.discoveryService = discoveryService;
        httpClient = httpClientFactory.createHttpClient(GeneracMobileLinkBindingConstants.BINDING_ID);
        httpClient.setFollowRedirects(true);
        // We have to send a very large amount of cookies which exceeds the default buffer size
        httpClient.setRequestBufferSize(16348);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        loggedIn = false;
        nextLoginAttempt = Instant.MIN;
        try {
            httpClient.start();
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Could not start HttpClient: " + e.getMessage());
            return;
        }
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
                if (Instant.now().isBefore(nextLoginAttempt)) {
                    return;
                }
                login();
            }
            loggedIn = true;
            updateGeneratorThings();
            nextLoginAttempt = Instant.MIN;
        } catch (LoginBlockedException e) {
            logger.debug("Login blocked", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing.generacmobilelink.account.offline.communication-error.login-blocked");
            stopOrRestartPoll(false);
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
     * Logs in through the Auth0 login pages. We fill in the email and password forms, and the MobileLink backend
     * handles the rest of the OAuth flow and sets the session cookie used by the API.
     *
     * @throws IOException if there is a problem communicating or parsing the responses
     * @throws InvalidCredentialsException if Auth0 rejects the login credentials
     * @throws LoginBlockedException if Auth0 requires a step we can't complete, like a captcha or MFA
     */
    private synchronized void login() throws IOException, InvalidCredentialsException, LoginBlockedException {
        logger.debug("Attempting login");
        GeneracMobileLinkAccountConfiguration config = getConfigAs(GeneracMobileLinkAccountConfiguration.class);
        refreshIntervalSeconds = config.refreshInterval;
        try {
            ContentResponse response = httpClient.newRequest(API_BASE + "/Auth/Auth0/SignIn")
                    .param("userEmail", config.username).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).send();
            nextLoginAttempt = Instant.now().plus(LOGIN_RETRY_DELAY);
            if (response.getStatus() != 200) {
                throw new IOException("Unexpected sign in response status: " + response.getStatus());
            }
            boolean passwordSent = false;
            for (int step = 0; step < 3; step++) {
                Document page = Jsoup.parse(response.getContentAsString());
                if (page.selectFirst("[data-captcha-provider], input[name=captcha]") != null) {
                    throw new LoginBlockedException("Auth0 is requesting a captcha");
                }
                Element form = page.selectFirst("form:has(input[name=state])");
                if (form == null) {
                    // no form means Auth0 redirected back to the app
                    return;
                }
                boolean passwordStep = form.selectFirst("input[name=password]") != null;
                if (passwordStep && passwordSent) {
                    throw new InvalidCredentialsException("Auth0 asked for the password again");
                }
                if (!passwordStep && form.selectFirst("input[name=username]") == null) {
                    throw new LoginBlockedException("Unsupported login step, MFA may be enabled on the account");
                }
                Fields fields = new Fields();
                form.select("input[name]").forEach(input -> fields.put(input.attr("name"), input.attr("value")));
                fields.put("username", config.username);
                fields.put("action", "default");
                if (passwordStep) {
                    fields.put("password", config.password);
                } else {
                    fields.put("js-available", "true");
                }
                String stepPath = passwordStep ? "/u/login/password" : "/u/login/identifier";
                response = httpClient.POST(AUTH_BASE + stepPath).param("state", form.select("input[name=state]").val())
                        .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).content(new FormContentProvider(fields))
                        .send();
                logger.debug("Login {} response status {}", stepPath, response.getStatus());
                // Auth0 returns 400 when it rejects the credentials
                if (response.getStatus() == 400) {
                    Document errorPage = Jsoup.parse(response.getContentAsString());
                    if (errorPage.selectFirst("[data-captcha-provider], input[name=captcha]") != null) {
                        throw new LoginBlockedException("Auth0 is requesting a captcha");
                    }
                    throw new InvalidCredentialsException("Auth0 rejected the credentials: "
                            + errorPage.select("[id^=error-element], #prompt-alert, .ulp-input-error-message").text());
                }
                if (response.getStatus() != 200) {
                    throw new IOException("Unexpected login response status: " + response.getStatus());
                }
                passwordSent |= passwordStep;
            }
            throw new IOException("Login did not complete");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException(e);
        }
    }

    private class InvalidCredentialsException extends Exception {
        private static final long serialVersionUID = 1L;

        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    private class LoginBlockedException extends Exception {
        private static final long serialVersionUID = 1L;

        public LoginBlockedException(String message) {
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
