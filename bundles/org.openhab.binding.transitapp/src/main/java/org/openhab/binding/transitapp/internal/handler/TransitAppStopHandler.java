package org.openhab.binding.transitapp.internal.handler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.transitapp.internal.config.TransitAppBridgeConfiguration;
import org.openhab.binding.transitapp.internal.config.TransitAppStopConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link TransitAppStopHandler} manages the lifecycle and command processing
 * of a Transit Stop Thing. It fetches departure data periodically based on the bridge refresh interval
 * from the Transit API v4 endpoint, parses the JSON payload robustly using Jackson ObjectMapper,
 * and updates the corresponding channels in openHAB with comprehensive debug, trace, warning, and error logs.
 */
@NonNullByDefault
public class TransitAppStopHandler extends BaseThingHandler {

    // Logger instance for recording runtime diagnostics, traces, warnings, and errors
    private final Logger logger = LoggerFactory.getLogger(TransitAppStopHandler.class);

    // Asynchronous HTTP client configured with a connection timeout to communicate with the Transit API v4 endpoint
    private final HttpClient httpClient;

    // Jackson ObjectMapper instance for robust, reliable JSON payload parsing
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Background scheduled future task responsible for periodic polling and refreshing of stop data
    private @Nullable ScheduledFuture<?> refreshFuture;

    /**
     * Constructs the stop handler instance for the specified thing.
     * Initializes the underlying asynchronous HTTP client with a 10-second connection timeout.
     *
     * @param thing the transit stop thing representation
     */
    public TransitAppStopHandler(Thing thing) {
        super(thing);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        logger.debug("TransitAppStopHandler successfully constructed and initialized for thing UID: {}",
                thing.getUID());
    }

    /**
     * Called by openHAB during the thing initialization lifecycle phase.
     * Validates required configuration parameters, verifies the bridge topology status,
     * and triggers the background polling mechanism.
     */
    @Override
    public void initialize() {
        logger.info("Initializing TransitApp stop handler for thing UID: {}", thing.getUID());

        TransitAppStopConfiguration config = getConfigAs(TransitAppStopConfiguration.class);

        logger.debug(
                "Validating stop configuration parameters for thing {}: globalStopId='{}', time={}, removeCancelled={}, locale='{}', shouldUpdateRealtime={}, maxNumDepartures={}, includeStopsAndShapes={}, stopDetailed={}",
                thing.getUID(), config.globalStopId, config.time, config.removeCancelled, config.locale,
                config.shouldUpdateRealtime, config.maxNumDepartures, config.includeStopsAndShapes,
                config.stopDetailed);

        // Validation check: globalStopId must not be blank
        if (config.globalStopId.trim().isEmpty()) {
            logger.error(
                    "CRITICAL CONFIGURATION ERROR: Mandatory parameter 'globalStopId' is blank or missing for stop thing: {}. Setting thing status to OFFLINE.",
                    thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'globalStopId' is mandatory and missing.");
            return;
        }

        // Validation check: locale must not be blank
        if (config.locale.trim().isEmpty()) {
            logger.error(
                    "CRITICAL CONFIGURATION ERROR: Mandatory parameter 'locale' is blank or missing for stop thing: {}. Setting thing status to OFFLINE.",
                    thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'locale' is mandatory and missing.");
            return;
        }

        // Retrieve and verify parent bridge reference
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.error(
                    "CRITICAL TOPOLOGY ERROR: Parent bridge reference is null for stop thing: {}. Please check your thing configuration and bridge linkage.",
                    thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is missing");
            return;
        }

        logger.debug("Verifying bridge status for bridge UID: {}", bridge.getUID());
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            logger.warn(
                    "BRIDGE STATUS WARNING: Associated bridge {} is currently not online (Status: {}). Stop thing {} will remain offline until the bridge becomes online.",
                    bridge.getUID(), bridge.getStatus(), thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not online");
            return;
        }

        logger.info("Stop configuration and bridge status validated successfully. Starting polling for stop: {}",
                thing.getUID());
        startPolling();
    }

    /**
     * Automatically reacts to status changes of the associated parent bridge.
     *
     * @param bridgeStatusInfo the updated status information of the bridge
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);
        logger.info("Bridge status changed for stop thing {}: New bridge status = {}", thing.getUID(),
                bridgeStatusInfo.getStatus());

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            logger.debug("Parent bridge is now ONLINE. Resuming polling mechanism for stop thing: {}", thing.getUID());
            startPolling();
        } else {
            logger.warn("Parent bridge is no longer ONLINE. Halting polling and setting stop thing {} to OFFLINE.",
                    thing.getUID());
            stopPolling();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not online");
        }
    }

    /**
     * Called when the thing is disposed or removed. Ensures background tasks are terminated safely.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing TransitAppStopHandler instance for thing UID: {}", thing.getUID());
        stopPolling();
        super.dispose();
    }

    /**
     * Starts the periodic background refresh task using the refresh interval configured on the bridge.
     */
    private synchronized void startPolling() {
        if (refreshFuture == null || refreshFuture.isCancelled()) {
            long refreshInterval = 60;
            Bridge bridge = getBridge();
            if (bridge != null) {
                ThingHandler handler = bridge.getHandler();
                if (handler instanceof TransitAppBridgeHandler) {
                    TransitAppBridgeHandler bridgeHandler = (TransitAppBridgeHandler) handler;
                    TransitAppBridgeConfiguration bridgeConfig = bridgeHandler.getConfigAsBridge();
                    refreshInterval = bridgeConfig.refreshInterval;
                }
            }

            logger.info("Starting background refresh job for stop {} with a polling interval of {} seconds.",
                    thing.getUID(), refreshInterval);
            refreshFuture = scheduler.scheduleWithFixedDelay(this::fetchStopData, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * Stops the periodic background refresh task if currently active.
     */
    private synchronized void stopPolling() {
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
            refreshFuture = null;
            logger.debug("Background refresh job successfully stopped and cancelled for stop thing: {}.",
                    thing.getUID());
        }
    }

    /**
     * Builds the request URL for the Transit API v4 endpoint, dispatches an asynchronous HTTP request,
     * and processes the server response with comprehensive trace and error logging.
     */
    private void fetchStopData() {
        TransitAppStopConfiguration config = getConfigAs(TransitAppStopConfiguration.class);

        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.warn("Aborting fetchStopData execution: Parent bridge reference is null for stop thing: {}",
                    thing.getUID());
            return;
        }

        ThingHandler handler = bridge.getHandler();
        if (!(handler instanceof TransitAppBridgeHandler)) {
            logger.error(
                    "CRITICAL ERROR: Parent bridge handler is invalid or not an instance of TransitAppBridgeHandler for bridge UID: {}",
                    bridge.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge handler is not valid");
            return;
        }

        TransitAppBridgeHandler bridgeHandler = (TransitAppBridgeHandler) handler;
        TransitAppBridgeConfiguration bridgeConfig = bridgeHandler.getConfigAsBridge();
        String apiKey = bridgeConfig.apiKey;

        if (apiKey.trim().isEmpty()) {
            logger.error(
                    "CRITICAL SECURITY/CONFIG ERROR: Bridge API key is empty or missing when fetching data for stop: {}",
                    thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge API Key is missing");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Fetching Departures...");

        StringBuilder urlBuilder = new StringBuilder("https://external.transitapp.com/v4/public/stop_departures?");
        urlBuilder.append("global_stop_id=").append(config.globalStopId);
        if (config.time > 0) {
            urlBuilder.append("&time=").append(config.time);
        }
        urlBuilder.append("&remove_cancelled=").append(config.removeCancelled);
        urlBuilder.append("&locale=").append(config.locale);
        urlBuilder.append("&should_update_realtime=").append(config.shouldUpdateRealtime);
        urlBuilder.append("&max_num_departures=").append(config.maxNumDepartures);
        urlBuilder.append("&include_stops_and_shapes=").append(config.includeStopsAndShapes);
        urlBuilder.append("&stop_detailed=").append(config.stopDetailed);

        String url = urlBuilder.toString();
        logger.info("HTTP REQUEST: Dispatching request to Transit API for stop ID '{}' using URL: {}",
                config.globalStopId, url);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("apiKey", apiKey).GET().build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            int statusCode = response.statusCode();
            String body = response.body();

            logger.debug("HTTP RESPONSE RECEIVED: Stop ID '{}' returned status code: {}, Payload body length: {}",
                    config.globalStopId, statusCode, body != null ? body.length() : 0);
            logger.trace("RAW JSON PAYLOAD TRACE [Stop ID: {}]:\n{}", config.globalStopId, body);

            if (statusCode >= 200 && statusCode < 300) {
                logger.debug("HTTP Success: Updating 'jsonResponse' channel and initiating JSON parsing.");
                if (body != null) {
                    updateState("jsonResponse", new StringType(body));
                    updateDepartureChannels(body, config.maxNumDepartures);
                }

                logger.info(
                        "SUCCESS: Data successfully fetched and processed for stop ID '{}'. Thing status set to ONLINE.",
                        config.globalStopId);
                updateStatus(ThingStatus.ONLINE);
            } else if (statusCode == 401 || statusCode == 403) {
                logger.error(
                        "SECURITY AUTHENTICATION ERROR: API key rejected by Transit API (HTTP Status {}). Please verify your API key configuration on the bridge.",
                        statusCode);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "API Authentication Failed (Status: " + statusCode + ")");
            } else if (statusCode >= 400 && statusCode < 500) {
                logger.error("CLIENT API ERROR: Transit API returned client error response (HTTP Status {}): {}",
                        statusCode, body);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "API Client Error: " + statusCode);
            } else {
                logger.error("SERVER API ERROR: Transit API returned server error response (HTTP Status {}): {}",
                        statusCode, body);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "API Server Error: " + statusCode);
            }
        }).exceptionally(e -> {
            logger.error("COMMUNICATION EXCEPTION: Failed to communicate with Transit API endpoint for stop {}: {}",
                    config.globalStopId, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection failed: " + e.getMessage());
            return null;
        });
    }

    /**
     * Parses the raw JSON response payload using Jackson ObjectMapper and updates the stop name channel
     * as well as all indexed departure channels with comprehensive trace and warning diagnostics.
     *
     * @param jsonBody the raw JSON response string from the API
     * @param maxNumDepartures maximum number of departures configured to process
     */
    private void updateDepartureChannels(String jsonBody, int maxNumDepartures) {
        try {
            logger.debug("JSON PARSING: Starting to parse response payload using Jackson ObjectMapper...");

            JsonNode root = objectMapper.readTree(jsonBody);

            // 1. Extract stop_name dynamically with fallback checks
            String parsedStopName = "-";
            JsonNode stopNameNode = root.path("stop_name");
            if (!stopNameNode.isMissingNode() && stopNameNode.isTextual()) {
                parsedStopName = stopNameNode.asText();
            } else {
                JsonNode routeSvcNode = root.path("route_services");
                if (routeSvcNode.has("stop_name")) {
                    parsedStopName = routeSvcNode.path("stop_name").asText("-");
                }
            }
            updateState("stopName", new StringType(parsedStopName));
            logger.trace("STOP NAME PARSED: Successfully extracted stop name as '{}'", parsedStopName);

            int limit = Math.min(maxNumDepartures, 10);
            logger.debug("CHANNEL PRE-FILLING: Initializing {} departure channel sets with default fallback values.",
                    limit);

            // Pre-fill channels with default fallback values
            for (int i = 1; i <= limit; i++) {
                updateState("routeLongName#" + i, new StringType("-"));
                updateState("routeShortName#" + i, new StringType("-"));
                updateState("departureTime#" + i, new DateTimeType());
                updateState("isCancelled#" + i, OnOffType.OFF);
            }

            // 2. Extract departures from route_departures -> merged_itineraries -> schedule_items
            int deptIndex = 1;
            JsonNode routeDepartures = root.path("route_departures");

            if (routeDepartures.isArray()) {
                logger.trace("JACKSON NAVIGATION: Found 'route_departures' array containing {} route entries.",
                        routeDepartures.size());

                for (JsonNode routeDeparture : routeDepartures) {
                    String routeLongName = routeDeparture.path("route_long_name").asText("-");
                    String routeShortName = routeDeparture.path("route_short_name").asText("-");

                    JsonNode mergedItineraries = routeDeparture.path("merged_itineraries");

                    if (mergedItineraries.isArray()) {
                        for (JsonNode merged : mergedItineraries) {
                            JsonNode scheduleItems = merged.path("schedule_items");

                            if (scheduleItems.isArray()) {
                                for (JsonNode item : scheduleItems) {
                                    if (deptIndex > limit) {
                                        logger.trace(
                                                "REACHED LIMIT: Maximum configured departures limit ({}) reached. Stopping processing.",
                                                limit);
                                        break;
                                    }

                                    // Skip helper objects without departure_time
                                    if (!item.has("departure_time")) {
                                        logger.trace("SKIPPING ITEM: Schedule item lacks 'departure_time' field.");
                                        continue;
                                    }

                                    long depTime = item.path("departure_time").asLong(0);
                                    boolean isCancelled = item.path("is_cancelled").asBoolean(false);

                                    updateState("routeLongName#" + deptIndex, new StringType(routeLongName));
                                    updateState("routeShortName#" + deptIndex, new StringType(routeShortName));

                                    if (depTime > 0) {
                                        ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(depTime),
                                                ZoneId.systemDefault());
                                        updateState("departureTime#" + deptIndex, new DateTimeType(zdt));
                                        logger.trace(
                                                "DEPARTURE TIME CONVERTED: Index {} -> Epoch {} converted to ZonedDateTime: {}",
                                                deptIndex, depTime, zdt);
                                    } else {
                                        logger.warn(
                                                "DEPARTURE WARNING: Invalid or zero epoch timestamp encountered for departure index {}.",
                                                deptIndex);
                                        updateState("departureTime#" + deptIndex, new DateTimeType());
                                    }

                                    updateState("isCancelled#" + deptIndex, isCancelled ? OnOffType.ON : OnOffType.OFF);

                                    logger.trace(
                                            "PARSED DEPARTURE ITEM [Index #{}] -> Route: '{}' ({}), Departure Epoch: {}, Cancelled: {}",
                                            deptIndex, routeLongName, routeShortName, depTime, isCancelled);

                                    deptIndex++;
                                }
                            }
                        }
                    }
                }
            } else {
                logger.warn(
                        "JSON PARSING WARNING: 'route_departures' root node is missing or not an array in API response payload.");
            }

            logger.info(
                    "JSON PARSING COMPLETE: Successfully updated departure channels (Total processed: {} departures).",
                    deptIndex - 1);
        } catch (Exception e) {
            logger.error("JSON PARSING CRITICAL ERROR: Failed to parse departure JSON payload via Jackson: {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Handles commands sent from openHAB to channels (e.g., UI updates or manual refreshes).
     *
     * @param channelUID the UID of the target channel
     * @param command the issued command received from openHAB
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("HANDLE COMMAND: Received command '{}' for channel UID: {}", command, channelUID);
    }
}
