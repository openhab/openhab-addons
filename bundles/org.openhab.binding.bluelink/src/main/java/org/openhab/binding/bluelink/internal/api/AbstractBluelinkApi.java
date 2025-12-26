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
package org.openhab.binding.bluelink.internal.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.bluelink.internal.dto.Token;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Abstract base class for Bluelink/Kia Connect API implementations.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBluelinkApi<V extends IVehicle> {

    protected static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    protected static final int HTTP_TIMEOUT_SECONDS = 30;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Gson gson = new GsonBuilder().create();
    protected final HttpClient httpClient;
    protected final TimeZoneProvider timeZoneProvider;
    protected final String username;
    protected final String password;
    protected final @Nullable String pin;

    protected @Nullable String accessToken;
    protected @Nullable Instant tokenExpiry;

    protected AbstractBluelinkApi(final HttpClient httpClient, final TimeZoneProvider timeZoneProvider,
            final String username, final String password, final @Nullable String pin) {
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
        this.username = username;
        this.password = password;
        this.pin = pin;
    }

    /**
     * Authenticate with the API.
     *
     * @return true if login was successful
     * @throws BluelinkApiException if login fails
     */
    public abstract boolean login() throws BluelinkApiException;

    /**
     * Get list of enrolled vehicles.
     *
     * @return list of vehicles
     * @throws BluelinkApiException if the request fails
     */
    public abstract List<V> getVehicles() throws BluelinkApiException;

    /**
     * Get vehicle status.
     *
     * @param vehicle the vehicle to query
     * @param forceRefresh if true, force a refresh from the vehicle instead of using cached data
     * @return the vehicle status, or null if not available
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean getVehicleStatus(IVehicle vehicle, boolean forceRefresh, VehicleStatusCallback cb)
            throws BluelinkApiException;

    /**
     * Lock the vehicle doors.
     *
     * @param vehicle the vehicle to lock
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean lockVehicle(IVehicle vehicle) throws BluelinkApiException;

    /**
     * Unlock the vehicle doors.
     *
     * @param vehicle the vehicle to unlock
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean unlockVehicle(IVehicle vehicle) throws BluelinkApiException;

    /**
     * Start the climate control.
     *
     * @param vehicle the vehicle
     * @param temperature target temperature
     * @param heat enable heating
     * @param defrost enable defrost
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean climateStart(IVehicle vehicle, QuantityType<Temperature> temperature, boolean heat,
            boolean defrost, final @Nullable Integer igniOnDuration) throws BluelinkApiException;

    /**
     * Stop the climate control.
     *
     * @param vehicle the vehicle
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean climateStop(IVehicle vehicle) throws BluelinkApiException;

    /**
     * Start charging (EV only).
     *
     * @param vehicle the vehicle
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean startCharging(IVehicle vehicle) throws BluelinkApiException;

    /**
     * Stop charging (EV only).
     *
     * @param vehicle the vehicle
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean stopCharging(IVehicle vehicle) throws BluelinkApiException;

    /**
     * Set DC charge limit (EV only).
     *
     * @param vehicle the vehicle
     * @param limit charge limit percentage (0-100)
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean setChargeLimitDC(IVehicle vehicle, int limit) throws BluelinkApiException;

    /**
     * Set AC charge limit (EV only).
     *
     * @param vehicle the vehicle
     * @param limit charge limit percentage (0-100)
     * @return true if the command was sent successfully
     * @throws BluelinkApiException if the request fails
     */
    public abstract boolean setChargeLimitAC(IVehicle vehicle, int limit) throws BluelinkApiException;

    public abstract void addStandardHeaders(Request request);

    protected boolean isAuthenticated() {
        final String token = accessToken;
        final Instant expiry = tokenExpiry;
        return token != null && expiry != null && Instant.now().isBefore(expiry);
    }

    protected void ensureAuthenticated() throws BluelinkApiException {
        if (!isAuthenticated()) {
            login();
        }
    }

    protected long getTimeZoneOffset() {
        return Duration.ofSeconds(timeZoneProvider.getTimeZone().getRules().getOffset(Instant.now()).getTotalSeconds())
                .toHours();
    }

    protected <ResT> boolean doLogin(final String loginUrl, final Object loginRequest, final Class<ResT> clazz,
            final Function<ResT, @Nullable Token> tokenExtractor) throws BluelinkApiException {
        final Request request = httpClient.newRequest(loginUrl).method(HttpMethod.POST)
                .timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .content(new StringContentProvider(gson.toJson(loginRequest)), APPLICATION_JSON);
        addStandardHeaders(request);

        try {
            final ContentResponse response = request.send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.debug("Login failed with status {}: {}", response.getStatus(), response.getContentAsString());
                final String msg = "Login failed: " + response.getStatus();
                if (isRetryable(response)) {
                    throw new RetryableRequestException(msg);
                } else {
                    throw new BluelinkApiException(msg);
                }
            }

            final @Nullable ResT res = gson.fromJson(response.getContentAsString(), clazz);
            if (res == null) {
                throw new BluelinkApiException("empty response");
            }
            final Token token = tokenExtractor.apply(res);
            if (token == null || token.accessToken() == null || token.expiresIn() == null) {
                throw new BluelinkApiException("Invalid token response");
            }
            accessToken = token.accessToken();
            tokenExpiry = Instant.now().plusSeconds(Integer.parseInt(token.expiresIn()) - 60);
            logger.debug("Login successful, token valid until {}", tokenExpiry);
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException("Login interrupted", e);
        } catch (TimeoutException | ExecutionException e) {
            throw new BluelinkApiException("Login failed", e);
        }
    }

    /**
     * Send a request and parse the response,
     *
     * @param request the request object
     * @param clazz class of the parsed response
     * @param op the operation, for logging
     * @param <T> the response type
     * @return a parsed response
     * @throws BluelinkApiException on request failure
     */
    protected <T> T sendRequest(final Request request, final Class<T> clazz, final String op)
            throws BluelinkApiException {
        final @Nullable T res = sendRequestInternal(request, clazz, op);
        if (res == null) {
            logger.debug("{}: unexpected empty response", op);
            throw new BluelinkApiException(op + ": empty response");
        }
        return res;
    }

    /**
     * Send a request, ignore the response (but check HTTP status).
     *
     * @param request request to send
     * @param op the operation, for logging
     * @throws BluelinkApiException on request failure
     */
    protected void sendRequest(final Request request, final String op) throws BluelinkApiException {
        sendRequestInternal(request, Void.class, op);
    }

    /**
     * Send a request and parse the response (return null if response is empty)
     *
     * @param request the request object
     * @param responseClass class of the parsed response
     * @param op the operation, for logging
     * @param <T> response type
     * @return the parsed response, or {@code null} if response is empty
     * @throws BluelinkApiException on request failure
     */
    private <T> @Nullable T sendRequestInternal(final Request request, final Class<T> responseClass, final String op)
            throws BluelinkApiException {
        try {
            final ContentResponse response = checkStatus(request.send(), op);
            return gson.fromJson(response.getContentAsString(), responseClass);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BluelinkApiException(op + " interrupted", e);
        } catch (final TimeoutException | ExecutionException e) {
            throw new BluelinkApiException(op + " request failed", e);
        }
    }

    protected ContentResponse checkStatus(final ContentResponse response, final String op) throws BluelinkApiException {
        if (response.getStatus() != HttpStatus.OK_200) {
            logger.debug("operation failed ({}): {} - {}", op, response.getStatus(), response.getContentAsString());
            final String msg = "operation failed: %s - %d".formatted(op, response.getStatus());
            if (isRetryable(response)) {
                throw new RetryableRequestException(msg);
            } else {
                throw new BluelinkApiException(msg);
            }
        }
        return response;
    }

    protected boolean isRetryable(final ContentResponse response) {
        final int status = response.getStatus();
        return status >= 500 && status < 600;
    }
}
