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
package org.openhab.binding.threedprinter.internal.handler;

import static org.openhab.binding.threedprinter.internal.ThreedprinterBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Base class for all 3D printer handlers providing shared HTTP and scheduling logic.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractPrinterHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final HttpClient httpClient;
    protected final Gson gson = new Gson();

    private @Nullable ScheduledFuture<?> refreshJob;

    protected AbstractPrinterHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        int interval = getRefreshInterval();
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, interval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    protected abstract int getRefreshInterval();

    protected abstract void refresh();

    /**
     * Result of an {@link #httpGet(String, String)} call. A negative {@code status} means the request could not be
     * completed at all (timeout or connection failure); a non-negative {@code status} is the HTTP response code
     * actually received from the printer, with {@code body} populated only when it was a 2xx response.
     */
    protected static final class HttpGetResult {
        final int status;
        final @Nullable String body;

        private HttpGetResult(int status, @Nullable String body) {
            this.status = status;
            this.body = body;
        }
    }

    /**
     * Sends an HTTP GET and returns the result. Callers should check {@code result.body} for the response and, if
     * null, pass {@code result.status} to {@link #markHttpFailure(int)} to report a distinguishable Thing status.
     */
    protected HttpGetResult httpGet(String url, String apiKey) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            ContentResponse response = request.send();
            int status = response.getStatus();
            if (status == HttpStatus.OK_200) {
                return new HttpGetResult(status, response.getContentAsString());
            }
            logger.debug("GET {} returned HTTP {}", url, status);
            return new HttpGetResult(status, null);
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("GET {} failed: {}", url, e.getMessage());
            return new HttpGetResult(-1, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("GET {} interrupted", url);
            return new HttpGetResult(-1, null);
        }
    }

    /**
     * Sends an HTTP POST with a JSON body and returns the HTTP status code, or -1 on exception.
     */
    protected int httpPost(String url, String apiKey, String jsonBody) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.POST).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            if (!jsonBody.isEmpty()) {
                request.content(new StringContentProvider(jsonBody), "application/json");
            }
            return request.send().getStatus();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("POST {} failed: {}", url, e.getMessage());
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("POST {} interrupted", url);
            return -1;
        }
    }

    /**
     * Sends an HTTP DELETE and returns the HTTP status code, or -1 on exception.
     */
    protected int httpDelete(String url, String apiKey) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.DELETE).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            return request.send().getStatus();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("DELETE {} failed: {}", url, e.getMessage());
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("DELETE {} interrupted", url);
            return -1;
        }
    }

    /**
     * Sends an HTTP PUT with a JSON body and returns the HTTP status code, or -1 on exception.
     */
    protected int httpPut(String url, String apiKey, String jsonBody) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.PUT).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            if (!jsonBody.isEmpty()) {
                request.content(new StringContentProvider(jsonBody), "application/json");
            }
            return request.send().getStatus();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("PUT {} failed: {}", url, e.getMessage());
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("PUT {} interrupted", url);
            return -1;
        }
    }

    /**
     * Sends an HTTP GET and returns the raw response bytes, or null on failure.
     */
    protected byte @Nullable [] httpGetBytes(String url, String apiKey) {
        try {
            Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(10, TimeUnit.SECONDS);
            if (!apiKey.isBlank()) {
                request.header("X-Api-Key", apiKey);
            }
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {
                return response.getContent();
            }
            logger.debug("GET bytes {} returned HTTP {}", url, response.getStatus());
            return null;
        } catch (TimeoutException | ExecutionException e) {
            logger.debug("GET bytes {} failed: {}", url, e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.debug("GET bytes {} interrupted", url);
            return null;
        }
    }

    protected void markOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
    }

    /**
     * Updates the Thing status in response to a failed HTTP call, distinguishing a transport failure (the printer
     * could not be reached at all) from an HTTP-level error response (the printer was reached but rejected the
     * request), so an authentication failure is not presented the same way as an unreachable printer.
     *
     * @param status the status from {@link HttpGetResult#status} or one of the {@code httpPost}/{@code httpPut}/
     *            {@code httpDelete} return values; negative means a transport failure.
     */
    protected void markHttpFailure(int status) {
        if (status < 0) {
            markOffline("@text/offline.comm-error-unreachable");
        } else if (status == HttpStatus.UNAUTHORIZED_401 || status == HttpStatus.FORBIDDEN_403) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.comm-error-auth");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error-http [\"" + status + "\"]");
        }
    }

    /**
     * Updates the Thing status in response to a failed command (pause/resume/cancel/setpoint/etc.), distinguishing
     * an application-level rejection of the command from an actual communication failure. PrusaLink and OctoPrint
     * both return {@code 404}/{@code 409} when a command doesn't match the printer's current job state - e.g. a
     * stale job ID, or a cancel racing a job that already finished. Moonraker's Klippy bridge instead converts a
     * rejected G-code command (e.g. a `PAUSE` when nothing is printing) into a {@code 400} that its HTTP handler
     * forwards as-is. In all of these cases the API was reached and responded normally, so the Thing should stay
     * {@code ONLINE}; a refresh is triggered instead so the channels reconcile with the printer's actual state.
     * Transport failures and {@code 401}/{@code 403} still behave like {@link #markHttpFailure(int)}.
     */
    protected void markCommandFailure(int status) {
        if (status == HttpStatus.BAD_REQUEST_400 || status == HttpStatus.NOT_FOUND_404
                || status == HttpStatus.CONFLICT_409) {
            logger.debug("Command rejected by printer due to its current state: HTTP {}", status);
            scheduler.execute(this::refresh);
        } else {
            markHttpFailure(status);
        }
    }

    /**
     * Parses a JSON string into the given type, returning null on malformed input instead of throwing. This prevents a
     * {@link JsonSyntaxException} from propagating out of the scheduled refresh task and silently stopping all future
     * polling.
     */
    protected <T> @Nullable T fromJson(@Nullable String json, Class<T> type) {
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to parse JSON response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Resets every job-dependent channel to {@link UnDefType#UNDEF}. Handlers should call this when the printer
     * reports no active job/filename, so stale data from a previous print does not linger.
     */
    protected void clearJobState() {
        updateState(CHANNEL_JOB_NAME, UnDefType.UNDEF);
        updateState(CHANNEL_JOB_PROGRESS, UnDefType.UNDEF);
        updateState(CHANNEL_TIME_ELAPSED, UnDefType.UNDEF);
        updateState(CHANNEL_TIME_REMAINING, UnDefType.UNDEF);
        updateState(CHANNEL_JOB_PREVIEW, UnDefType.UNDEF);
    }

    /**
     * Converts a command to a whole-degree Celsius value, or null if the command type is not supported.
     */
    protected @Nullable Integer toCelsius(Command command) {
        if (command instanceof QuantityType<?> qty) {
            QuantityType<?> celsius = qty.toUnit(SIUnits.CELSIUS);
            return celsius != null ? celsius.intValue() : qty.intValue();
        }
        if (command instanceof DecimalType dec) {
            return dec.intValue();
        }
        return null;
    }

    /**
     * Converts a command to a whole-percent value, or null if the command type is not supported.
     */
    protected @Nullable Integer toPercent(Command command) {
        if (command instanceof QuantityType<?> qty) {
            QuantityType<?> percent = qty.toUnit(Units.PERCENT);
            return percent != null ? percent.intValue() : qty.intValue();
        }
        if (command instanceof DecimalType dec) {
            return dec.intValue();
        }
        return null;
    }
}
