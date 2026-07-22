package org.openhab.binding.transitapp.internal.handler;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.transitapp.internal.config.TransitAppBridgeConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TransitAppBridgeHandler} is responsible for handling the lifecycle
 * and connection validation of the Transit API Bridge.
 */
@NonNullByDefault
public class TransitAppBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(TransitAppBridgeHandler.class);
    private final HttpClient httpClient;

    public TransitAppBridgeHandler(Bridge bridge) {
        super(bridge);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        logger.debug("TransitAppBridgeHandler constructed for thing: {}", bridge.getUID());
    }

    @Override
    public void initialize() {
        logger.info("Initializing TransitApp Bridge handler for thing: {}", thing.getUID());

        TransitAppBridgeConfiguration config = getConfigAs(TransitAppBridgeConfiguration.class);
        String apiKey = config.apiKey;

        if (apiKey.trim().isEmpty()) {
            logger.error("API Key is missing or empty for bridge: {}", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API Key is missing");
            return;
        }

        logger.debug("API Key loaded successfully (length: {}, starts with: {}...). Verifying connection...",
                apiKey.length(), apiKey.substring(0, Math.min(4, apiKey.length())));

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Verifying API Key...");

        String verificationUrl = "https://external.transitapp.com/v4/public/stop_departures?global_stop_id=test";
        logger.trace("Building HTTP request for verification URL: {}", verificationUrl);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(verificationUrl)).header("apiKey", apiKey).GET()
                .build();

        logger.debug("Sending asynchronous HTTP request to Transit API for bridge validation...");
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(response -> {
            int statusCode = response.statusCode();
            String body = response.body();

            logger.debug("Received HTTP response from Transit API. Status code: {}, Body length: {}", statusCode,
                    body != null ? body.length() : 0);
            logger.trace("Full HTTP response body: {}", body);

            if (statusCode >= 200 && statusCode < 300) {
                logger.info("Transit API connection verified successfully! Status code: {}. Bridge is now ONLINE.",
                        statusCode);
                updateStatus(ThingStatus.ONLINE);
            } else if (statusCode == 401 || statusCode == 403) {
                logger.error("API Authentication failed with status code {}: {}", statusCode, body);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "API Authentication Failed (Status: " + statusCode + ")");
            } else if (statusCode >= 400 && statusCode < 500) {
                logger.error("Transit API returned client error status {}: {}", statusCode, body);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "API Client Error: " + statusCode);
            } else {
                logger.error("Transit API returned server error status {}: {}", statusCode, body);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "API Server Error: " + statusCode);
            }
        }).exceptionally(e -> {
            logger.error("Failed to connect to Transit API: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection failed: " + e.getMessage());
            return null;
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand called for bridge channel: {} with command: {}", channelUID, command);
    }

    public TransitAppBridgeConfiguration getConfigAsBridge() {
        return getConfigAs(TransitAppBridgeConfiguration.class);
    }
}
