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
package org.openhab.binding.tibber.internal.history;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link TibberHistory} fetches historical energy data (consumption, cost and production)
 * from the Tibber GraphQL API and notifies registered {@link TibberHistoryListener}s when done.
 * <p>
 * Each TibberHandler gets its own instance with a dedicated sequential executor so that
 * multiple Tibber homes never block each other's history fetches.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Add history channel group
 */
@NonNullByDefault
public class TibberHistory {

    /**
     * The {@link TimeWindow} enum defines the four time windows supported by the history feature.
     * It maps Tibber API resolution names to channel prefixes, entry counts, and time spans.
     */
    public enum TimeWindow {
        ANNUAL() {
            @Override
            public String channelPrefix() {
                return "yearly";
            }

            @Override
            public int getFetchCount() {
                return 1;
            }

            @Override
            public int getFullFetchPageSize() {
                return 50;
            }

            @Override
            public int daysInWindow() {
                return 365;
            }
        },

        MONTHLY() {
            @Override
            public String channelPrefix() {
                return "monthly";
            }

            @Override
            public int getFetchCount() {
                return 12;
            }

            @Override
            public int getFullFetchPageSize() {
                return 50;
            }

            @Override
            public int daysInWindow() {
                return getFetchCount() * 31;
            }
        },

        WEEKLY() {
            @Override
            public String channelPrefix() {
                return "weekly";
            }

            @Override
            public int getFetchCount() {
                return 52;
            }

            @Override
            public int getFullFetchPageSize() {
                return 100;
            }

            @Override
            public int daysInWindow() {
                return getFetchCount() * 7;
            }
        },

        DAILY() {
            @Override
            public String channelPrefix() {
                return "daily";
            }

            @Override
            public int getFetchCount() {
                return 31;
            }

            @Override
            public int getFullFetchPageSize() {
                return 100;
            }

            @Override
            public int daysInWindow() {
                return getFetchCount();
            }
        };

        public String channelPrefix() {
            return "";
        }

        public int getFetchCount() {
            return 0;
        }

        /**
         * Page size used when performing a full paginated fetch
         * (see {@link TibberHistory#updateHistory(TimeWindow, boolean)} with {@code fullUpdate=true}).
         * Larger than {@link #getFetchCount()} to minimise the number of API round-trips
         * while staying well within Tibber API timeout limits.
         */
        public int getFullFetchPageSize() {
            return 50;
        }

        public int daysInWindow() {
            return 0;
        }

        @Override
        public String toString() {
            return name();
        }
    }

    /**
     * Immutable value object that pairs a {@link TimeWindow} with its update mode.
     * Using a separate record avoids storing mutable state on the {@link TimeWindow} enum singletons,
     * which would be unsafe when multiple Tibber homes (multiple handler instances) issue concurrent requests.
     */
    public record HistoryRequest(TimeWindow window, boolean fullUpdate) {
        @Override
        public String toString() {
            return window.name() + " (" + (fullUpdate ? "full" : "partial") + ")";
        }
    }

    private final Logger logger = LoggerFactory.getLogger(TibberHistory.class);
    private final List<String> queryTemplates = List.of(CONSUMPTION_QUERY_RESOURCE_PATH,
            PRODUCTION_QUERY_RESOURCE_PATH);
    private final List<TibberHistoryListener> listeners = new ArrayList<>();
    private final List<HistoryRequest> workingList = new ArrayList<>();
    private final Storage<String> store;
    private final TibberHandler handler;
    private final String homeid;

    /**
     * Per-instance sequential executor. Using a per-instance pool ensures that multiple
     * Tibber homes (multiple TibberHandler instances) never block each other's history fetches.
     */
    private final ScheduledExecutorService historyScheduler;

    private boolean disposed = true;

    /**
     * Creates a new TibberHistory instance.
     * Must be called from {@code TibberHandler.initialize()} (not from the constructor) to
     * ensure OSGi @Reference injection of StorageService has already completed.
     *
     * @param storageService OSGi StorageService for persistent caching of history data
     * @param homeid Tibber home ID used as storage key prefix
     * @param handler the owning TibberHandler; used to borrow its HTTP request builder
     */
    public TibberHistory(StorageService storageService, String homeid, TibberHandler handler) {
        this.handler = handler;
        this.homeid = homeid;
        this.store = storageService.getStorage(TibberHistory.class.getName() + "-" + homeid);
        this.historyScheduler = ThreadPoolManager.getPoolBasedSequentialScheduledExecutorService("TibberHistory",
                homeid);
        this.listeners.add(handler);
    }

    /**
     * Marks this instance as disposed or active.
     * When disposed, the working queue is cleared and no new fetches are started.
     *
     * @param disposed true to dispose, false to reactivate
     */
    public void dispose(boolean disposed) {
        this.disposed = disposed;
        if (disposed) {
            synchronized (workingList) {
                workingList.clear();
            }
        }
    }

    /**
     * Enqueues an update for the given time window.
     * If the same window is already queued the request is ignored.
     *
     * @param window the time window to update
     * @param fullUpdate {@code true} for a full paginated fetch (replaces stored data);
     *            {@code false} for a partial fetch (merges into stored data)
     */
    public void updateHistory(TimeWindow window, boolean fullUpdate) {
        synchronized (workingList) {
            boolean alreadyQueued = workingList.stream().anyMatch(r -> r.window() == window);
            if (alreadyQueued) {
                logger.info("{} already requested", window);
                return;
            }
            HistoryRequest request = new HistoryRequest(window, fullUpdate);
            logger.info("Queuing history request: {}", request);
            workingList.add(request);
            historyScheduler.execute(this::getHistory);
        }
    }

    /**
     * Returns the currently stored series for the given time window directly from the persistent store.
     *
     * @param window the time window
     * @return the stored series (may be empty if nothing has been fetched yet)
     */
    public TibberHistorySeries getStoredSeries(TimeWindow window) {
        return new TibberHistorySeries(store.get(homeid + "-" + window.name()));
    }

    /**
     * Worker method that processes the head of the working queue.
     * Fetches consumption and production data for the current window via paginated GraphQL queries,
     * stores the result, and notifies all listeners.
     */
    private void getHistory() {
        HistoryRequest localRequest;
        synchronized (workingList) {
            if (workingList.isEmpty()) {
                logger.debug("History queue is empty — nothing to fetch");
                return;
            }
            localRequest = workingList.get(0);
            logger.info("Processing {} ({} request(s) in queue)", localRequest, workingList.size());
        }

        // Snapshot the request fields — no further access to workingList needed until removal
        final TimeWindow window = localRequest.window();
        final boolean isPaginated = localRequest.fullUpdate();

        // Notify listeners that a fetch is starting (series == null signals "in progress")
        listeners.forEach(listener -> listener.historyUpdated(localRequest, null));

        // fullUpdate: paginate through all available history pages, starting from a fresh series.
        // partialUpdate: fetch only the most recent page and merge into the existing stored series.
        int pageSize = isPaginated ? window.getFullFetchPageSize() : window.getFetchCount();
        TibberHistorySeries series = isPaginated ? new TibberHistorySeries(null) : getStoredSeries(window);

        logger.info("Starting {} fetch for {} (pageSize={}, paginated={})", isPaginated ? "full paginated" : "partial",
                window, pageSize, isPaginated);

        for (String templatePath : queryTemplates) {
            logger.debug("Fetching {} for template {}", window, templatePath);
            String cursor = EMPTY_VALUE;
            boolean hasNext = false;
            int retryCounter = 0;
            int pageCount = 0;

            do {
                String queryTemplate = handler.getTemplate(templatePath);
                String query = String.format(queryTemplate, homeid, window.name(), pageSize, cursor);
                String response = executeRequest(query);

                if (response != null) {
                    retryCounter = 0; // reset on success
                    JsonObject jsonResponse = (JsonObject) JsonParser.parseString(response);
                    String[] jsonPath = CONSUMPTION_QUERY_RESOURCE_PATH.equals(templatePath)
                            ? HISTORY_CONSUMPTION_JSON_PATH
                            : HISTORY_PRODUCTION_JSON_PATH;
                    JsonObject dataObject = getJsonObjectSafely(jsonResponse, jsonPath);
                    if (dataObject != null) {
                        JsonArray edges = dataObject.getAsJsonArray("edges");
                        series.addData(edges);
                        pageCount++;

                        JsonObject pageInfo = dataObject.getAsJsonObject("pageInfo");
                        hasNext = pageInfo.get("hasPreviousPage").getAsBoolean();
                        if (hasNext) {
                            cursor = "before: \\\"" + pageInfo.get("startCursor").getAsString() + "\\\"";
                        } else {
                            cursor = EMPTY_VALUE;
                        }
                        logger.debug("page={} hasNext={} totalFetched={}", pageCount, hasNext, series.size());
                    } else {
                        logger.warn("Unexpected response structure for template {}", templatePath);
                        hasNext = false;
                    }
                } else {
                    retryCounter++;
                    hasNext = retryCounter < 3; // retry up to 3 times on null response
                    logger.warn("No response for template {} - retry {}/3", templatePath, retryCounter);
                }

                // Sleep on retry (with exponential backoff) or between paginated pages (fixed 200ms courtesy delay)
                if (retryCounter > 0 || (hasNext && isPaginated)) {
                    int sleepMs = retryCounter > 0 ? dynamicRetryTimeMs(retryCounter) : 200;
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("History fetch interrupted: {}", e.getMessage());
                        return;
                    }
                }
            } while (hasNext && isPaginated && !disposed);

            logger.info("Completed template {} for {} after {} page(s), {} total entries", templatePath, window,
                    pageCount, series.size());
        }

        // Persist the merged result
        store.put(homeid + "-" + window.name(), series.toString());

        // Remove from queue and notify listeners with the completed series
        synchronized (workingList) {
            workingList.remove(0);
            int remaining = workingList.size();
            logger.info("Completed {}. {} request(s) remaining in queue.", window, remaining);
        }

        listeners.forEach(listener -> listener.historyUpdated(localRequest, series));
    }

    private @Nullable String executeRequest(String body) {
        Request request = handler.getRequest();
        String content = String.format(QUERY_CONTAINER, body);
        logger.debug("History query: {}", content);
        request.content(new StringContentProvider(content, "utf-8"));
        try {
            ContentResponse cr = request.timeout(10, TimeUnit.SECONDS).send();
            int status = cr.getStatus();
            String responseBody = cr.getContentAsString();
            if (status == HttpStatus.OK_200) {
                logger.debug("History response ({}): {}", status, responseBody);
                return responseBody;
            } else {
                logger.warn("History API returned HTTP {}: {}", status, responseBody);
            }
        } catch (Exception e) {
            logger.warn("History request failed: {}", e.getMessage());
        }
        return null;
    }

    private @Nullable JsonObject getJsonObjectSafely(JsonObject root, String[] path) {
        JsonObject current = root;
        for (String key : path) {
            if (current.has(key) && current.get(key).isJsonObject()) {
                current = current.getAsJsonObject(key);
            } else {
                logger.warn("Expected JSON key '{}' not found in history response", key);
                return null;
            }
        }
        return current;
    }

    private static int dynamicRetryTimeMs(int retryCount) {
        int exponentialBackoff = Math.min(1024, (int) Math.pow(2.0, retryCount));
        return exponentialBackoff * 1000 + (int) (Math.random() * 1000);
    }
}
