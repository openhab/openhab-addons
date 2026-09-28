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
    private static final int MAX_REREGISTER_ATTEMPTS = 6;
    private static final long MIN_REREGISTER_AFTER_MS = 30000;
    private static final long MIN_STALE_AFTER_MS = 60000;

    private final Gson gson = new Gson();
    private final long reregisterAfterMs;
    private final long staleAfterMs;

    private String host = "";
    private final CoapClient client = new CoapClient();
    private final CoapEndpoint endpoint;
    private volatile long counter = 1;
    // commands are only serialized with each other, so they never wait for the observe relation to be (re)established
    private final Object commandLock = new Object();
    private boolean hasSync = false;
    private long syncCounter = 0;
    private volatile int attempt = 0;
    private volatile @Nullable Consumer<PhilipsAirAPIConnection> listener;
    private volatile @Nullable CoapObserveRelation observe = null;
    private volatile @Nullable String lastJson = null;
    // the scheme of the last status, commands are sent in the classic scheme until the device reported its status
    private volatile CoapKeyScheme keyScheme = CoapKeyScheme.CLASSIC;
    private volatile long lastUpdated = 0L; // epoch ms of last valid JSON
    private int mid;

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
        reregisterAfterMs = Math.max(refreshIntervalMs, MIN_REREGISTER_AFTER_MS);
        staleAfterMs = Math.max(2 * refreshIntervalMs, MIN_STALE_AFTER_MS);

        Configuration netConfig = Configuration.getStandard().set(CoapConfig.DEDUPLICATOR, CoapConfig.NO_DEDUPLICATOR)
                .set(CoapConfig.ACK_TIMEOUT, 20, TimeUnit.SECONDS)
                .set(CoapConfig.EXCHANGE_LIFETIME, 65, TimeUnit.SECONDS);

        endpoint = new CoapEndpoint.Builder().setConfiguration(netConfig).build();
        if (logger.isTraceEnabled()) {
            MessageInterceptor interceptor = new CoapMessageLogger();
            endpoint.addInterceptor(interceptor);
        }
        client.setEndpoint(endpoint);
        client.setTimeout(TIMEOUT);
        logger.debug("PhilipsAirCoapAPIConnection initialized using host {}", host);
    }

    /**
     * Establishes the observe relation, or re-registers it when the device has not sent a notification recently. The
     * relation itself is asynchronous; only the counter sync before a new relation waits for the device.
     */
    @Override
    public synchronized void ensureConnected() {
        if (listener == null) {
            return;
        }
        final long sinceLastUpdate = System.currentTimeMillis() - lastUpdated;
        final CoapObserveRelation currentObserve = this.observe;
        if (currentObserve != null && !currentObserve.isCanceled()) {
            if (sinceLastUpdate < reregisterAfterMs) {
                return;
            }
            if (attempt < MAX_REREGISTER_ATTEMPTS) {
                attempt++;
                logger.debug("No update from {} for {}ms, re-register #{}: {}", host, sinceLastUpdate, attempt,
                        currentObserve.reregister());
                return;
            }
            logger.debug("No update from {} after {} re-registrations, restarting observe", host, attempt);
            currentObserve.proactiveCancel();
            this.observe = null;
        }
        attempt = 0;
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
            if (this.mid > 0 && Math.abs(this.mid - request.getMID()) > 100) {
                logger.debug("Different MIDs in request and responses: {} &  {}", request.getMID(), this.mid + 1);
                request.setMID(this.mid + 1);
            } else {
                logger.debug("MIDs in sync:  {}", this.mid + 1);
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

    private void processCoapResponse(String uri, @Nullable CoapResponse response) {
        if (response != null) {
            if (!response.isSuccess()) {
                logger.debug("Response is not success: {}", response.getCode());
            }
            logger.trace("Response is advanced: {}", response.advanced());
            String content = response.getResponseText();
            if (content != null) {
                if (processNotification(content, uri)) {
                    this.mid = response.advanced().getMID();
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
        lastUpdated = System.currentTimeMillis();
        attempt = 0;
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
                        CoapKeyScheme scheme = CoapKeyScheme.detect(reported);
                        keyScheme = scheme;
                        return scheme.toClassic(reported).toString();
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
        return json != null && System.currentTimeMillis() - lastUpdated < staleAfterMs ? json : null;
    }

    @Override
    public boolean isPushingStatus() {
        return true;
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
        JsonObject desired = keyScheme.toDevice((JsonObject) gson.toJsonTree(value));
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
