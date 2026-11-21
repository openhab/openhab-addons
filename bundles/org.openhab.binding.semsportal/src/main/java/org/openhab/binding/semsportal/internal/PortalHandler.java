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
package org.openhab.binding.semsportal.internal;

import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.CLIENT_TYPES;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.semsportal.internal.dto.BaseResponse;
import org.openhab.binding.semsportal.internal.dto.CountingResponse;
import org.openhab.binding.semsportal.internal.dto.FlowResponse;
import org.openhab.binding.semsportal.internal.dto.Inverter;
import org.openhab.binding.semsportal.internal.dto.InverterStatusResponse;
import org.openhab.binding.semsportal.internal.dto.LoginRequest;
import org.openhab.binding.semsportal.internal.dto.LoginResponse;
import org.openhab.binding.semsportal.internal.dto.ProductionRequest;
import org.openhab.binding.semsportal.internal.dto.ProductionResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSToken;
import org.openhab.binding.semsportal.internal.dto.Station;
import org.openhab.binding.semsportal.internal.dto.StationListRequest;
import org.openhab.binding.semsportal.internal.dto.StationListResponse;
import org.openhab.binding.semsportal.internal.dto.StationStatus;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PortalHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Iwan Bron - Initial contribution
 * @author Julio Vilmar Gesser - Migrated to the SEMS+ API
 */
@NonNullByDefault
public class PortalHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(PortalHandler.class);
    // url for the login request, to get a valid session token
    private static final String LOGIN_URL = "https://semsplus.goodwe.com/web/sems/sems-user/api/v1/auth/cross-login";
    // The other requests go to the gateway of the region of the account, which the login response tells. These are the
    // paths on that gateway: to list the power stations, and to get the output, the inverters, the energy counters of
    // an inverter, and the income of a power station
    private static final String LIST_PATH = "/sems-plant/api/portal/stations/page";
    private static final String FLOW_PATH = "/sems-plant/api/stations/flow";
    private static final String DEVICES_PATH = "/sems-plant/api/stations/device/all-status";
    private static final String COUNTING_PATH = "/sems-plant/api/equipments/%s/telecounting";
    private static final String PRODUCTION_PATH = "/sems-plant/api/stations/production";
    // the portal only serves requests that look like they come from its web application
    private static final String ORIGIN = "https://semsplus.goodwe.com";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";
    // the token holds the credential information for the portal, the signature proves that it is used right now
    private static final String HTTP_HEADER_TOKEN = "token";
    private static final String HTTP_HEADER_SIGNATURE = "X-Signature";
    private static final int REQUEST_TIMEOUT_MS = 10_000;
    // the status codes of an inverter that is producing
    private static final Set<Integer> NORMAL_STATUSES = Set.of(1, 5);
    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // The portal sums the income of any period of the type 'year': this one starts before the first installations
    private static final String INSTALLATION_START = "2000-01-01 00:00:00";
    private static final ThingStatusInfo ONLINE = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

    // used to parse json from / to the SEMS portal API
    private final Gson gson;
    private final HttpClient httpClient;

    // configuration as provided by the openhab framework: initialize with defaults to prevent compiler check errors
    private SEMSPortalConfiguration config = new SEMSPortalConfiguration();
    // null as long as we are not logged in
    private volatile @Nullable SEMSToken sessionToken;
    // A login replaces the previous session of the same client type of the account, so logins are done one at a time
    private final Object loginLock = new Object();
    private volatile @Nullable ScheduledFuture<?> loginJob;
    // A request that is still running when the handler is disposed (or initialized again) must not restore the session
    // or publish a status from before: the generation changes with the lifecycle, and the results of an older one are
    // dropped. It only changes together with the session, with the stateLock held.
    private final Object stateLock = new Object();
    private volatile int generation;
    private volatile boolean disposed;

    public PortalHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        httpClient = httpClientFactory.getCommonHttpClient();
        gson = new GsonBuilder().create();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("No supported commands. Ignoring command {} for channel {}", command, channelUID);
        return;
    }

    @Override
    public void initialize() {
        config = getConfigAs(SEMSPortalConfiguration.class);
        if (!CLIENT_TYPES.contains(config.clientType)) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                    "Client type must be one of " + String.join(", ", CLIENT_TYPES));
            return;
        }
        disposed = false;
        int started = startLifecycle();
        updateStatus(ThingStatus.UNKNOWN);

        loginJob = scheduler.schedule(() -> {
            try {
                renewSession(started, null);
            } catch (CommunicationException e) {
                logger.debug("Unable to login to SEMS portal: {}", e.getMessage()); // the status tells why
            } catch (RuntimeException e) {
                logger.debug("Unexpected error when logging in to SEMS portal", e);
                publish(started, offline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error when loggin in. Check your username and password"));
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        disposed = true;
        ScheduledFuture<?> job = loginJob;
        if (job != null) {
            job.cancel(true);
            loginJob = null;
        }
        startLifecycle();
        super.dispose();
    }

    /**
     * Starts a new generation, without a session: what the requests of the previous one get is dropped.
     *
     * @return the new generation
     */
    private int startLifecycle() {
        synchronized (stateLock) {
            sessionToken = null;
            return ++generation;
        }
    }

    /**
     * Keeps the session of a login, unless the handler was disposed or initialized again since the login started.
     */
    private boolean commitSession(int started, SEMSToken token) {
        synchronized (stateLock) {
            if (started != generation) {
                return false;
            }
            sessionToken = token;
            return true;
        }
    }

    /**
     * Logs in. Only to be called with the loginLock held. It does not call the framework: the caller publishes the
     * status that results from the login.
     *
     * @param started the generation that the login is for
     * @return the status of the portal after the login
     */
    private ThingStatusInfo requestLogin(int started) {
        sessionToken = null;
        LoginRequest request = new LoginRequest(config.username, SEMSSecurity.hashPassword(config.password));
        String response = send(HttpMethod.POST, LOGIN_URL, new SEMSToken(config.clientType), Map.of(),
                gson.toJson(request));
        if (response == null) {
            return offline(ThingStatusDetail.COMMUNICATION_ERROR, "Invalid response from SEMS portal");
        }

        LoginResponse loginResponse;
        try {
            loginResponse = gson.fromJson(response, LoginResponse.class);
        } catch (JsonSyntaxException e) {
            return offline(ThingStatusDetail.COMMUNICATION_ERROR, "Unable to parse response from SEMS portal");
        }
        if (loginResponse == null) {
            return offline(ThingStatusDetail.COMMUNICATION_ERROR, "Invalid response from SEMS portal");
        }
        SEMSToken token = loginResponse.getToken();
        if (loginResponse.isOk() && token != null && token.getApi() != null) {
            if (!commitSession(started, token)) {
                return offline(ThingStatusDetail.COMMUNICATION_ERROR, "The bridge was stopped while logging in");
            }
            logger.debug("Successfuly logged in to SEMS portal");
            return ONLINE;
        }
        logger.debug("Unable to login to SEMS portal. Response code {} message {}", loginResponse.getCode(),
                loginResponse.getMsg());
        if (BaseResponse.INVALID.equals(loginResponse.getCode())) {
            return offline(ThingStatusDetail.CONFIGURATION_ERROR, "Check username / password");
        }
        return offline(ThingStatusDetail.COMMUNICATION_ERROR,
                "Unable to login to SEMS portal: " + describe(loginResponse));
    }

    private ThingStatusInfo offline(ThingStatusDetail detail, String description) {
        return new ThingStatusInfo(ThingStatus.OFFLINE, detail, description);
    }

    /**
     * Publishes the status, unless the handler was disposed or initialized again since the generation started.
     */
    private void publish(int started, ThingStatusInfo status) {
        if (disposed || started != generation) {
            return;
        }
        if (status.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(status.getStatusDetail(), status.getDescription());
        }
    }

    /**
     * Sends a request to the portal.
     *
     * @param method the HTTP method
     * @param url the url to send the request to
     * @param token the session token to identify with
     * @param params the query parameters
     * @param payload the body of the request, if it has one
     * @return the body of the response, null if there was no (successful) response
     */
    @Nullable
    String send(HttpMethod method, String url, SEMSToken token, Map<String, String> params, @Nullable String payload) {
        try {
            Request request = httpClient.newRequest(url).method(method).timeout(REQUEST_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
            request.header(HttpHeader.USER_AGENT, USER_AGENT).header(HttpHeader.ORIGIN, ORIGIN)
                    .header(HttpHeader.REFERER, ORIGIN + "/").header(HttpHeader.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(HTTP_HEADER_TOKEN, gson.toJson(token)).header(HTTP_HEADER_SIGNATURE,
                            SEMSSecurity.signature(token.getUid(), token.getToken(), System.currentTimeMillis()));
            params.forEach(request::param);
            if (payload != null) {
                request.content(new StringContentProvider(payload, StandardCharsets.UTF_8), MediaType.APPLICATION_JSON);
            }
            request.getHeaders().remove(HttpHeader.ACCEPT_ENCODING);
            ContentResponse response = request.send();
            if (!LOGIN_URL.equals(url)) {
                logger.trace("received response: {}", response.getContentAsString());
            }
            if (!HttpStatus.isSuccess(response.getStatus())) {
                logger.debug("Received HTTP error {} when sending to url {}", response.getStatus(), url);
                return null;
            }
            return response.getContentAsString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException | RuntimeException e) {
            logger.debug("{} when sending to url {}", e.getClass().getSimpleName(), url, e);
        }
        return null;
    }

    /**
     * Sends a request to the gateway of the account, and parses the response. If the session is not valid (anymore),
     * it logs in again and repeats the request once.
     *
     * @param started the generation of the operation that sends the request: nothing is sent, and no login is done,
     *            for an operation of a previous generation
     */
    private <T extends BaseResponse> T call(int started, Class<T> type, HttpMethod method, String path,
            Map<String, String> params, @Nullable Object body) throws CommunicationException {
        SEMSToken token = getSession(started);
        T response = request(type, method, token, path, params, body);
        if (response.isSessionInvalid()) {
            logger.debug("Session is invalidated. Trying again.");
            token = renewSession(started, token);
            response = request(type, method, token, path, params, body);
        }
        return response;
    }

    private <T extends BaseResponse> T request(Class<T> type, HttpMethod method, SEMSToken token, String path,
            Map<String, String> params, @Nullable Object body) throws CommunicationException {
        String gateway = token.getApi();
        if (gateway == null) {
            throw new CommunicationException("No gateway received from portal");
        }
        String response = send(method, gateway + path, token, params, body == null ? null : gson.toJson(body));
        if (response == null) {
            throw new CommunicationException("No response received from portal");
        }
        @Nullable
        T parsed;
        try {
            parsed = gson.fromJson(response, type);
        } catch (JsonSyntaxException e) {
            throw new CommunicationException("Portal reponse not understood", e);
        }
        if (parsed == null) {
            throw new CommunicationException("Portal reponse not understood");
        }
        return parsed;
    }

    /**
     * A bridge that was stopped, or initialized again, has nothing to do for the operations that started before.
     */
    private void checkActive(int started) throws CommunicationException {
        if (disposed || started != generation) {
            throw new CommunicationException("The bridge is stopped");
        }
    }

    private SEMSToken getSession(int started) throws CommunicationException {
        checkActive(started);
        SEMSToken token = sessionToken;
        return token != null ? token : renewSession(started, null);
    }

    /**
     * Logs in, unless another request already did after it found the given token invalid. A login makes the previous
     * session token invalid, so logging in twice would invalidate the session that the first login just got.
     *
     * @param started the generation of the operation that needs the session
     */
    private SEMSToken renewSession(int started, @Nullable SEMSToken invalidToken) throws CommunicationException {
        ThingStatusInfo status;
        // The request is part of the critical section: the logins must not overlap. The framework is called outside it.
        synchronized (loginLock) {
            checkActive(started);
            SEMSToken current = sessionToken;
            if (current != null && (invalidToken == null || !invalidToken.getToken().equals(current.getToken()))) {
                return current;
            }
            status = requestLogin(started);
        }
        publish(started, status);
        SEMSToken token = sessionToken;
        if (token == null) {
            throw new CommunicationException("Unable to login to SEMS portal");
        }
        return token;
    }

    private void checkOk(BaseResponse response) throws CommunicationException {
        if (!response.isOk()) {
            throw new CommunicationException("Unknown status code received from SEMS portal: " + describe(response));
        }
    }

    private String describe(BaseResponse response) {
        String message = response.getMsg();
        return response.getCode() + (message == null || message.isBlank() ? "" : " - " + message);
    }

    public @Nullable StationStatus getStationStatus(String stationUUID)
            throws CommunicationException, ConfigurationException {
        ThingStatusInfo bridgeStatus = getThing().getStatusInfo();
        if (sessionToken == null && bridgeStatus.getStatus() == ThingStatus.OFFLINE
                && bridgeStatus.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR) {
            logger.debug("Not logged in because of configuration error. Not updating.");
            return null;
        }
        int started = generation;
        Map<String, String> station = Map.of("stationId", stationUUID);
        FlowResponse flow = call(started, FlowResponse.class, HttpMethod.GET, FLOW_PATH, station, null);
        if (flow.isNoAccess()) {
            throw new ConfigurationException("No access to this power station. Please check your station ID");
        }
        checkOk(flow);
        List<Inverter> inverters = requestInverters(started, stationUUID);

        List<@Nullable Double> dayCounters = new ArrayList<>();
        List<@Nullable Double> monthCounters = new ArrayList<>();
        List<@Nullable Double> overallCounters = new ArrayList<>();
        boolean operational = false;
        for (Inverter inverter : inverters) {
            operational |= NORMAL_STATUSES.contains(inverter.getStatus());
            CountingResponse counters = call(started, CountingResponse.class, HttpMethod.GET,
                    String.format(COUNTING_PATH, inverter.getSerialNumber()),
                    Map.of("deviceType", "INVERTER", "pwId", stationUUID), null);
            checkOk(counters);
            dayCounters.add(counters.getValue(CountingResponse.TODAY));
            monthCounters.add(counters.getValue(CountingResponse.MONTH));
            overallCounters.add(counters.getValue(CountingResponse.TOTAL));
        }
        Double dayTotal = sum(dayCounters);
        Double monthTotal = sum(monthCounters);
        Double overallTotal = sum(overallCounters);

        // The station is reachable. The income that follows is optional, and a login that fails while it is requested
        // must stay visible: this status is published before.
        publish(started, ONLINE);

        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Double dayIncome = getIncome(started, stationUUID, "day", today.atStartOfDay().format(PERIOD_FORMAT),
                today.atTime(23, 59, 59).format(PERIOD_FORMAT));
        Double totalIncome = getIncome(started, stationUUID, "year", INSTALLATION_START,
                today.atTime(23, 59, 59).format(PERIOD_FORMAT));

        Double currentOutput = flow.getCurrentOutput();
        return new StationStatus(currentOutput == null ? null : currentOutput * 1000, dayTotal, monthTotal,
                overallTotal, dayIncome, totalIncome, operational, toDateTime(flow.getRefreshTime()));
    }

    private List<Inverter> requestInverters(int started, String stationUUID) throws CommunicationException {
        InverterStatusResponse response = call(started, InverterStatusResponse.class, HttpMethod.GET, DEVICES_PATH,
                Map.of("stationId", stationUUID), null);
        checkOk(response);
        return response.getInverters();
    }

    /**
     * @param stationUUID the UUID of the power station
     * @return the inverters of the power station, empty if they cannot be retrieved
     */
    public List<Inverter> getInverters(String stationUUID) {
        try {
            return requestInverters(generation, stationUUID);
        } catch (CommunicationException e) {
            logger.debug("Unable to retrieve the inverters of the station: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * @param values the value of each inverter, null when the inverter has none
     * @return the sum, null unless every inverter has a value: a total without an inverter would drop and then jump
     *         back when its value returns
     */
    private @Nullable Double sum(List<@Nullable Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        double total = 0;
        for (Double value : values) {
            if (value == null) {
                return null;
            }
            total += value;
        }
        return total;
    }

    /**
     * The income is optional: the portal might not know it, for instance if no tariffs are set. That is not a reason
     * to fail the update.
     */
    private @Nullable Double getIncome(int started, String stationUUID, String dimension, String start, String end) {
        try {
            ProductionResponse response = call(started, ProductionResponse.class, HttpMethod.POST, PRODUCTION_PATH,
                    Map.of(), new ProductionRequest(stationUUID, dimension, start, end));
            if (response.isOk()) {
                return response.getIncome();
            }
            logger.debug("Unable to retrieve income. Response code {} message {}", response.getCode(),
                    response.getMsg());
        } catch (CommunicationException e) {
            logger.debug("Unable to retrieve income: {}", e.getMessage());
        }
        return null;
    }

    private @Nullable ZonedDateTime toDateTime(@Nullable String refreshTime) {
        if (refreshTime == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(refreshTime).atZone(ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            logger.debug("Unable to parse the time of the last update: {}", refreshTime);
            return null;
        }
    }

    public long getUpdateInterval() {
        return config.interval;
    }

    public List<Station> getAllStations() {
        int started = generation;
        try {
            StationListResponse listResponse = call(started, StationListResponse.class, HttpMethod.POST, LIST_PATH,
                    Map.of(), new StationListRequest());
            if (listResponse.isOk()) {
                logger.debug("Received list of {} stations from SEMS portal", listResponse.getStations().size());
                publish(started, ONLINE);
                return listResponse.getStations();
            }
            logger.debug("Received error response with code {} and message {} from SEMS portal", listResponse.getCode(),
                    listResponse.getMsg());
        } catch (CommunicationException e) {
            logger.debug("Unable to list the stations of the SEMS portal: {}", e.getMessage());
        }
        return Collections.emptyList();
    }
}
