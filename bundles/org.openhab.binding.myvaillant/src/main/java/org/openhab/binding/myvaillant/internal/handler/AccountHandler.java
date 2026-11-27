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
package org.openhab.binding.myvaillant.internal.handler;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.myvaillant.internal.api.MyVaillantApi;
import org.openhab.binding.myvaillant.internal.api.MyVaillantAuthenticationException;
import org.openhab.binding.myvaillant.internal.api.MyVaillantException;
import org.openhab.binding.myvaillant.internal.api.MyVaillantQuotaExceededException;
import org.openhab.binding.myvaillant.internal.config.AccountConfiguration;
import org.openhab.binding.myvaillant.internal.discovery.AccountDiscoveryService;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

/**
 * Bridge for a myVAILLANT account. It logs in, owns the API client, sends the requests of the systems of the account
 * and pauses all requests while Vaillant's call volume quota is exceeded.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {

    private static final Duration RECONNECT_DELAY = Duration.ofMinutes(5);
    private static final Set<String> BRANDS = Set.of("vaillant", "sdbg", "bulex", "glow-worm", "demirdokum");
    private static final Set<String> BRANDS_WITH_COUNTRY = Set.of("vaillant", "sdbg");
    /** The country becomes part of the identity provider's URL, e.g. {@code germany} or {@code north-macedonia} */
    private static final Pattern COUNTRY = Pattern.compile("[a-z]+(-[a-z]+)*");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final HttpClientFactory httpClientFactory;
    private final TimeZoneProvider timeZoneProvider;
    private final Storage<String> requestCountStorage;

    private volatile AccountConfiguration config = new AccountConfiguration();
    private @Nullable HttpClient httpClient;
    private volatile @Nullable MyVaillantApi api;
    private @Nullable ScheduledFuture<?> connectFuture;

    /** A request with the API client of the account */
    @FunctionalInterface
    interface ApiRequest<T> {
        T execute(MyVaillantApi api) throws MyVaillantException, InterruptedException;
    }

    /** A request without result */
    @FunctionalInterface
    interface ApiCommand {
        void execute(MyVaillantApi api) throws MyVaillantException, InterruptedException;
    }

    /**
     * @param requestCountStorage keeps the number of requests sent today per account across restarts, as
     *            {@code yyyy-MM-dd count}
     */
    public AccountHandler(Bridge bridge, HttpClientFactory httpClientFactory, TimeZoneProvider timeZoneProvider,
            Storage<String> requestCountStorage) {
        super(bridge);
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.requestCountStorage = requestCountStorage;
    }

    @Override
    public void initialize() {
        AccountConfiguration configuration = getConfigAs(AccountConfiguration.class);
        config = configuration;
        String brand = configuration.brand;
        if (configuration.username.isBlank() || configuration.password.isBlank()) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "@text/offline.conf-error.no-credentials");
            return;
        }
        if (!BRANDS.contains(brand)) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.unknown-brand [\"" + brand + "\"]");
            return;
        }
        String country = null;
        if (BRANDS_WITH_COUNTRY.contains(brand)) {
            country = configuration.country.trim().toLowerCase(Locale.ROOT);
            if (country.isEmpty()) {
                updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.no-country [\"" + brand + "\"]");
                return;
            }
            // Anything else would end up in the URL of the identity provider
            if (!COUNTRY.matcher(country).matches()) {
                updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.conf-error.invalid-country [\"" + configuration.country + "\"]");
                return;
            }
        }

        // An own client, as the login needs a cookie store of its own: Keycloak keeps the login session in cookies, in
        // the
        // shared client's cookie store the sessions of several accounts would mix
        HttpClient client = httpClientFactory.createHttpClient(BINDING_ID);
        try {
            client.start();
        } catch (Exception e) {
            logger.debug("Could not start the HTTP client: {}", e.getMessage());
            updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "@text/offline.comm-error.http-client");
            return;
        }
        httpClient = client;
        // Vaillant's quota counts all requests of the day, also those before a restart of openHAB or the binding
        int requestsToday = getRequestsToday(requestCountStorage.get(getThing().getUID().getAsString()),
                LocalDate.now(timeZoneProvider.getTimeZone()));
        api = new MyVaillantApi(client, timeZoneProvider, configuration.username, configuration.password, brand,
                country, requestsToday, this::updateRequestCount);
        updateState(CHANNEL_API_REQUESTS, new DecimalType(requestsToday));

        updateStatus(ThingStatus.UNKNOWN);
        scheduleConnect(Duration.ZERO);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> future = connectFuture;
        if (future != null) {
            future.cancel(true);
            connectFuture = null;
        }
        api = null;
        HttpClient client = httpClient;
        httpClient = null;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.debug("Could not stop the HTTP client: {}", e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The request count is published after each request
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(AccountDiscoveryService.class);
    }

    /** Configuration of the account, its update settings and defaults apply to all systems of the account */
    public AccountConfiguration getAccountConfiguration() {
        return config;
    }

    /** Whether the account is online, so its systems can send requests */
    boolean isOnline() {
        return api != null && getThing().getStatus() == ThingStatus.ONLINE;
    }

    /**
     * Sends a request with the API client of the account.
     *
     * @throws MyVaillantException if the account is not online or the request failed, failures of the whole account are
     *             passed to {@link #handleAccountFailure(MyVaillantException)} by the caller
     */
    <T> T request(ApiRequest<T> request) throws MyVaillantException, InterruptedException {
        return request.execute(getOnlineApi());
    }

    /**
     * Sends a request without result with the API client of the account.
     *
     * @see #request(ApiRequest)
     */
    void send(ApiCommand command) throws MyVaillantException, InterruptedException {
        command.execute(getOnlineApi());
    }

    private MyVaillantApi getOnlineApi() throws MyVaillantException {
        MyVaillantApi currentApi = api;
        if (currentApi == null || getThing().getStatus() != ThingStatus.ONLINE) {
            throw new MyVaillantException("The account is not online");
        }
        return currentApi;
    }

    /** The homes of the account, one per system */
    public JsonArray getHomes() throws MyVaillantException, InterruptedException {
        return request(MyVaillantApi::getHomes);
    }

    /**
     * Failures that concern the whole account: wrong credentials take the account offline until its configuration
     * changes, an exceeded quota until the quota is replenished. Other failures are handled by the caller.
     *
     * @return whether the failure concerned the account
     */
    public boolean handleAccountFailure(MyVaillantException e) {
        if (e instanceof MyVaillantAuthenticationException) {
            updateStatus(ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return true;
        }
        if (e instanceof MyVaillantQuotaExceededException quotaException) {
            Instant until = quotaException.getBlockedUntil();
            logger.debug("Call volume quota exceeded, pausing requests until {}", until);
            updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "@text/offline.comm-error.quota-exceeded [\""
                    + TIME.format(until.atZone(timeZoneProvider.getTimeZone())) + "\"]");
            scheduleConnect(Duration.between(Instant.now(), until));
            return true;
        }
        return false;
    }

    private synchronized void scheduleConnect(Duration delay) {
        ScheduledFuture<?> future = connectFuture;
        if (future != null) {
            future.cancel(false);
        }
        connectFuture = scheduler.schedule(this::connect, Math.max(0, delay.toSeconds()), TimeUnit.SECONDS);
    }

    private void connect() {
        MyVaillantApi currentApi = api;
        if (currentApi == null) {
            return;
        }
        try {
            currentApi.ensureLoggedIn();
            // The account may have been disposed or reinitialized meanwhile
            if (currentApi.equals(api)) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (MyVaillantException e) {
            if (!currentApi.equals(api) || handleAccountFailure(e)) {
                return;
            }
            logger.debug("Login to myVAILLANT failed: {}", e.getMessage());
            updateStatus(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            scheduleConnect(RECONNECT_DELAY);
        } catch (InterruptedException e) {
            // The account is being disposed
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void handleRemoval() {
        requestCountStorage.remove(getThing().getUID().getAsString());
        super.handleRemoval();
    }

    private void updateRequestCount(LocalDate day, int count) {
        if (api != null) {
            updateState(CHANNEL_API_REQUESTS, new DecimalType(count));
            requestCountStorage.put(getThing().getUID().getAsString(), day + " " + count);
        }
    }

    /**
     * @param stored stored request count as {@code yyyy-MM-dd count}
     * @return the stored count if it is from today, else 0
     */
    static int getRequestsToday(@Nullable String stored, LocalDate today) {
        if (stored == null) {
            return 0;
        }
        String[] parts = stored.trim().split(" ");
        try {
            return parts.length == 2 && LocalDate.parse(parts[0]).equals(today)
                    ? Math.max(0, Integer.parseInt(parts[1]))
                    : 0;
        } catch (DateTimeParseException | NumberFormatException e) {
            return 0;
        }
    }
}
