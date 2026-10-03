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
package org.openhab.binding.philipsair.internal.connection;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.config.CoapConfig;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.interceptors.MessageInterceptor;
import org.eclipse.californium.elements.config.Configuration;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierFiltersDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierStateDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierStatusDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierWritableDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PhilipsAirCoapAPIConnection} is responsible for handling commands, for the 2019 and newer models
 * communicating using the CoAP protocol.
 * <p>
 * The device pushes its status through a CoAP observe relation. Each valid notification is passed to the listener;
 * {@link #ensureConnected()} is called periodically to (re)establish the relation when notifications stop.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class PhilipsAirCoapAPIConnection extends PhilipsAirAPIConnection {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCoapAPIConnection.class);
    private static final String RESOURCE_PATH_STATUS = "/sys/dev/status";
    private static final String RESOURCE_PATH_SYNC = "/sys/dev/sync";
    private static final String RESOURCE_PATH_CONTROL = "/sys/dev/control";
    private static final int COAP_PORT = 5683;
    private static final long TIMEOUT = 25000;
    private static final long PING_TIMEOUT = 5000;
    private static final int MAX_MID = 0xFFFF;
    private static final long MIN_PING_AFTER_MS = 30000;
    private static final long MIN_STALE_AFTER_MS = 60000;

    private final Gson gson = new Gson();
    private final long pingAfterMs;
    private final long staleAfterMs;

    private String host = "";
    private final CoapClient client = new CoapClient();
    // pings the device with a shorter timeout than the commands, as it is done from the periodic connection check
    private final CoapClient pingClient = new CoapClient();
    private final CoapEndpoint endpoint;
    private volatile long counter = 1;
    // commands are only serialized with each other, so they never wait for the observe relation to be (re)established
    private final Object commandLock = new Object();
    private boolean hasSync = false;
    private long syncCounter = 0;
    private volatile @Nullable Consumer<PhilipsAirAPIConnection> listener;
    private volatile @Nullable CoapObserveRelation observe = null;
    private volatile @Nullable String lastJson = null;
    // the scheme of the last status, commands are sent in the classic scheme until the device reported its status
    private volatile CoapProfile profile = CoapProfile.CLASSIC;
    // epoch ms of the last valid JSON or answer to a ping
    private volatile long lastContact = 0L;
    // set when the device did not answer, as it may have lost the observe relation when it comes back
    private volatile boolean deviceMissed = false;
    // The highest MID of the notifications and of the observe requests sent. Written by the Californium thread.
    private volatile int mid;

    /**
     * @param config the thing configuration
     * @param listener called with this connection whenever the device reported a new status
     */
    public PhilipsAirCoapAPIConnection(PhilipsAirConfiguration config, Consumer<PhilipsAirAPIConnection> listener) {
        super(config);
        this.listener = listener;
        CoapConfig.register();

        if (!config.getHost().isEmpty()) {
            host = config.getHost();
        } else {
            logger.debug("Host is empty, cannot start COAP connection");
        }
        long refreshIntervalMs = TimeUnit.SECONDS.toMillis(config.getRefreshInterval());
        pingAfterMs = Math.max(refreshIntervalMs, MIN_PING_AFTER_MS);
        staleAfterMs = Math.max(2 * refreshIntervalMs, MIN_STALE_AFTER_MS);

        // a copy, as the standard configuration is shared with the other bindings using Californium
        Configuration netConfig = new Configuration(Configuration.getStandard())
                .set(CoapConfig.DEDUPLICATOR, CoapConfig.NO_DEDUPLICATOR)
                .set(CoapConfig.ACK_TIMEOUT, 20, TimeUnit.SECONDS)
                .set(CoapConfig.EXCHANGE_LIFETIME, 65, TimeUnit.SECONDS);

        endpoint = new CoapEndpoint.Builder().setConfiguration(netConfig).build();
        if (logger.isTraceEnabled()) {
            MessageInterceptor interceptor = new CoapMessageLogger();
            endpoint.addInterceptor(interceptor);
        }
        client.setEndpoint(endpoint);
        client.setTimeout(TIMEOUT);
        pingClient.setEndpoint(endpoint);
        pingClient.setTimeout(PING_TIMEOUT);
        logger.debug("PhilipsAirCoapAPIConnection initialized using host {}", host);
    }

    /**
     * Establishes the observe relation, or checks the device when it has not been heard from recently. The relation
     * itself is asynchronous; only the counter sync before a new relation and the ping wait for the device.
     */
    @Override
    public synchronized void ensureConnected() {
        if (listener == null) {
            return;
        }
        final CoapObserveRelation currentObserve = this.observe;
        if (currentObserve != null && !currentObserve.isCanceled()) {
            final long sinceLastContact = System.currentTimeMillis() - lastContact;
            if (sinceLastContact < pingAfterMs) {
                return;
            }
            // A device in standby only pushes changes and does not answer a status request, so the silence is only a
            // failure if the device does not answer a ping either
            if (!pingDevice()) {
                // Californium cancels the relation when the device does not answer its registration, which restarts it
                deviceMissed = true;
                logger.debug("No contact with {} for {}ms", host, sinceLastContact);
                return;
            }
            if (!deviceMissed) {
                return;
            }
            // the device may have lost the relation while it was away
            deviceMissed = false;
            logger.debug("{} answers again, restarting observe", host);
            currentObserve.proactiveCancel();
            this.observe = null;
        }
        startObserve();
    }

    private void startObserve() {
        try {
            String uri = getUriString(host, COAP_PORT, RESOURCE_PATH_STATUS);
            if (!hasSync) {
                counter = getSync(counter);
                logger.debug("Counter for {}: {}", host, counter);
                if (listener == null) {
                    return; // disposed while waiting for the sync response
                }
            }
            client.setURI(uri);

            Request request = Request.newGet();
            request.setURI(uri);
            request.setType(Type.CON);
            request.setObserve();
            // Workaround for the deduplication of the device: once it sent a notification, the request continues
            // after the highest MID seen, instead of the MID Californium would assign. A MID is never used twice.
            int lastMid = this.mid;
            if (lastMid > 0) {
                int requestMid = lastMid % MAX_MID + 1;
                request.setMID(requestMid);
                this.mid = requestMid;
                logger.debug("Observe request MID {}, following the MID {}", requestMid, lastMid);
            }

            logger.debug("Start Observe request {}", uri);
            CoapObserveRelation newObserve = client.observe(request, new CoapHandler() {
                @Override
                public void onLoad(@Nullable CoapResponse response) {
                    processCoapResponse(uri, response);
                }

                @Override
                public void onError() {
                    logger.debug("Error for {}", uri);
                }
            });
            this.observe = newObserve;
            // dispose() clears the listener before cancelling the relation, so either it sees this relation or the
            // relation is cancelled here
            if (listener == null) {
                newObserve.proactiveCancel();
            }
        } catch (ConnectorException | IOException | IllegalStateException e) {
            logger.debug("Error while starting observe for {}: {}", host, e.getMessage());
        }
    }

    /**
     * Checks that the device is reachable with a counter sync, which it also answers in standby. A device only pushes
     * its status when something changed, so its last status is still valid as long as it answers.
     *
     * @return true if the device answered
     */
    boolean pingDevice() {
        String response = requestSync();
        if (response == null || response.length() < 8) {
            logger.debug("No answer to the ping of {}", host);
            return false;
        }
        try {
            counter = Long.parseUnsignedLong(response.substring(0, 8), 16);
        } catch (NumberFormatException e) {
            logger.debug("Invalid answer to the ping of {}: '{}'", host, response);
            return false;
        }
        lastContact = System.currentTimeMillis();
        logger.debug("{} answered the ping, counter {}", host, counter);
        return true;
    }

    /**
     * @return the answer of the device to the counter sync, or null if it did not answer
     */
    @Nullable
    String requestSync() {
        try {
            String response = post(pingClient, host, COAP_PORT, RESOURCE_PATH_SYNC, String.format("%08X", counter));
            return response.isEmpty() ? null : response;
        } catch (ConnectorException | IOException e) {
            logger.debug("Error while pinging {}: {}", host, e.getMessage());
            return null;
        } catch (RuntimeException e) {
            // Californium wraps the interruption while waiting for the response
            if (e.getCause() instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                return null;
            }
            throw e;
        }
    }

    private void processCoapResponse(String uri, @Nullable CoapResponse response) {
        if (response != null) {
            if (!response.isSuccess()) {
                logger.debug("Response is not success: {}", response.getCode());
            }
            logger.trace("Response is advanced: {}", response.advanced());
            String content = response.getResponseText();
            if (content != null) {
                if (processNotification(content, uri)) {
                    int notificationMid = response.advanced().getMID();
                    // a MID is only replaced by a later one, so restarts do not use the MID of an earlier request again
                    if (((notificationMid - this.mid) & MAX_MID) < MAX_MID / 2) {
                        this.mid = notificationMid;
                    }
                }
            } else {
                logger.debug("Response content null for {}", response.advanced());
            }
        } else {
            logger.debug("Response is null for {}", uri);
        }
    }

    /**
     * Processes the (encrypted) content of a status notification and notifies the listener if it holds a valid status.
     *
     * @return true if the notification held a valid status
     */
    boolean processNotification(String content, String uri) {
        String resp = processResponse(content.trim(), uri);
        if (resp.length() <= 2) {
            return false;
        }
        logger.debug("Status from {}: {}", host, resp);
        lastJson = resp;
        lastContact = System.currentTimeMillis();
        // the relation delivers, so the device did not lose it
        deviceMissed = false;
        Consumer<PhilipsAirAPIConnection> listener = this.listener;
        if (listener != null) {
            listener.accept(this);
        }
        return true;
    }

    private String processResponse(String rawResponse, String uri) {
        String decrypted = rawResponse.isBlank() ? "" : PhilipsAirCoapCipher.decryptMsg(rawResponse, logger);
        if (!decrypted.isEmpty()) {
            logger.trace("Raw Response from {}: {}", uri, rawResponse);
            logger.trace("Decrypted response from {}: {}", uri, decrypted);
            try {
                JsonElement airResponse = JsonParser.parseString(decrypted);
                if (airResponse.isJsonObject() && airResponse.getAsJsonObject().has("state")) {
                    JsonElement stateObj = airResponse.getAsJsonObject().get("state");
                    if (stateObj.isJsonObject()
                            && stateObj.getAsJsonObject().get("reported") instanceof JsonObject reported) {
                        counter = getCounter(rawResponse);
                        hasSync = true;
                        // the sync is only renewed after consecutive invalid responses
                        syncCounter = 0;
                        CoapProfile resolved = CoapProfile.resolve(config.getDeviceProfile(), reported);
                        if (resolved != profile) {
                            logger.info("Using the {} profile for {} (configured: {})", resolved, host,
                                    config.getDeviceProfile());
                            profile = resolved;
                        }
                        return resolved.toClassic(reported).toString();
                    } else {
                        logger.debug("Response does not contain 'reported' element");
                    }
                } else {
                    logger.debug("Response does not contain 'state' element");
                }
            } catch (JsonSyntaxException e) {
                logger.debug("Error parsing JSON response: {}", decrypted, e);
            }
        }
        syncCounter += 1;
        if (syncCounter > 3) {
            hasSync = false;
            syncCounter = 0;
        }
        logger.debug("No valid response for {}", uri);
        return "";
    }

    private long getCounter(String rawResponse) {
        if (rawResponse.length() >= 8) {
            String counterStr = rawResponse.substring(0, 8);
            try {
                counter = Long.parseUnsignedLong(counterStr, 16);
                logger.trace("Current counter: {}->{}", counterStr, counter);
            } catch (NumberFormatException e) {
                logger.debug("Error decoding '{}' to a number", counterStr);
            }
        } else {
            logger.debug("Error getting counter from response: '{}'", rawResponse);
        }
        return counter;
    }

    /**
     * @return the last reported status, or null if the device did not report a status recently
     */
    private @Nullable String currentStatus() {
        String json = lastJson;
        return json != null && System.currentTimeMillis() - lastContact < staleAfterMs ? json : null;
    }

    @Override
    public boolean isPushingStatus() {
        return true;
    }

    @Override
    public @Nullable CoapProfile getDeviceProfile() {
        return profile;
    }

    @Override
    public void dispose() {
        listener = null;
        CoapObserveRelation observe = this.observe;
        if (observe != null) {
            observe.proactiveCancel();
            this.observe = null;
        }
        client.shutdown();
        pingClient.shutdown();
        endpoint.destroy();
        logger.debug("PhilipsAirCoapAPIConnection for {} disposed", host);
    }

    @Override
    public @Nullable PhilipsAirPurifierDataDTO getAirPurifierStatus(String host) throws JsonSyntaxException {
        String json = currentStatus();
        return json != null ? gson.fromJson(json, PhilipsAirPurifierDataDTO.class) : null;
    }

    @Override
    public @Nullable PhilipsAirPurifierDeviceDTO getAirPurifierDevice(String host) throws JsonSyntaxException {
        String json = currentStatus();
        return json != null ? gson.fromJson(json, PhilipsAirPurifierDeviceDTO.class) : null;
    }

    @Override
    public @Nullable PhilipsAirPurifierFiltersDTO getAirPurifierFiltersStatus(String host) throws JsonSyntaxException {
        String json = currentStatus();
        return json != null ? gson.fromJson(json, PhilipsAirPurifierFiltersDTO.class) : null;
    }

    /**
     * Sends the command to the device. No state is returned, as the device pushes the new state through the observe
     * relation.
     */
    @Override
    public @Nullable PhilipsAirPurifierDataDTO sendCommand(String parameter, PhilipsAirPurifierWritableDataDTO value) {
        JsonObject desired = profile.toDevice((JsonObject) gson.toJsonTree(value));
        if (desired.isEmpty()) {
            logger.debug("Command '{}' is not supported by {}", gson.toJson(value), host);
            return null;
        }
        try {
            synchronized (commandLock) {
                String encrypted = prepareCommand(desired);
                if (encrypted == null) {
                    logger.debug("Could not encrypt command '{}'", gson.toJson(value));
                    return null;
                }
                String response = post(client, host, COAP_PORT, RESOURCE_PATH_CONTROL, encrypted);
                if (!"{\"status\":\"success\"}".equals(response)) {
                    logger.debug("Command failed. Response: {}", response);
                }
            }
        } catch (JsonSyntaxException | ConnectorException | IOException e) {
            logger.debug("Error sending command '{}': {}", gson.toJson(value), e.getMessage());
        }
        return null;
    }

    private @Nullable String prepareCommand(JsonObject desired) throws ConnectorException, IOException {
        long controlCounter = getSync(counter);
        logger.debug("ControlCounter from sync={}", controlCounter);
        desired.addProperty("CommandType", "app");
        desired.addProperty("DeviceId", "");
        desired.addProperty("EnduserId", "1");
        PhilipsAirPurifierStateDTO state = new PhilipsAirPurifierStateDTO();
        state.setDesired(desired);
        PhilipsAirPurifierStatusDTO fullCmd = new PhilipsAirPurifierStatusDTO();
        fullCmd.setState(state);
        String commandValue = gson.toJson(fullCmd);
        controlCounter++;
        logger.debug("Sending command {}", commandValue);
        return PhilipsAirCoapCipher.encryptedMsg(commandValue, controlCounter, logger);
    }

    private long getSync(long currentCounter) throws ConnectorException, IOException {
        String controlCounterResponse = post(client, host, COAP_PORT, RESOURCE_PATH_SYNC,
                String.format("%08X", currentCounter));
        return getCounter(controlCounterResponse);
    }

    private static String getUriString(String server, int port, String resourcePath) {
        return "coap://" + server + ":" + port + resourcePath;
    }

    private String post(CoapClient client, String server, int port, String resourcePath, String body)
            throws ConnectorException, IOException {
        String uri = getUriString(server, port, resourcePath);
        // the URI is set on the request, as the shared client URI is also changed when (re)starting the observe
        Request request = Request.newPost();
        request.setURI(uri);
        request.setPayload(body);
        CoapResponse response = client.advanced(request);
        if (response != null) {
            logger.trace("POST {} -> Response: {}", uri, Utils.prettyPrint(response));
            logger.debug("POST {} -> Response: {}", uri, response.getResponseText());
            return response.getResponseText();
        } else {
            logger.debug("POST {} -> No response received.", uri);
        }
        return "";
    }
}
