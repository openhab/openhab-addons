/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
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
import org.openhab.core.cache.ExpiringCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link PhilipsAirCoapAPIConnection} is responsible for handling commands, for the 2019 and newer models
 * communicating
 * using the coap protocol.
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

    private final Gson gson = new Gson();
    private ExpiringCache<String> coapStatus = new ExpiringCache<>(20000, this::refreshData);
    private String host = "";
    private CoapClient client = new CoapClient();
    private long counter = 1;
    private boolean hasSync = false;
    private long syncCounter = 0;
    private int attempt = -1;
    private @Nullable CoapObserveRelation observe = null;

    private String refreshData() {
        try {
            logger.debug("Refreshing data for {}", host);

            final CoapObserveRelation reuseObserve = this.observe;
            if (reuseObserve != null && !reuseObserve.isCanceled()) {
                if (attempt < 2) {
                    logger.debug("Send COAP ping to {}:{}", client.getURI(), client.ping(TIMEOUT));
                    logger.debug("Reregister #{},{}", attempt, reuseObserve.reregister());
                    attempt += 1;
                    return "";
                } else {
                    logger.debug("Cancel request #{}", attempt);
                    reuseObserve.proactiveCancel();
                }
            }
            attempt = 0;

            String uri = getUriString(host, COAP_PORT, RESOURCE_PATH_STATUS);
            client.setURI(uri);
            if (!hasSync) {
                counter = getSync(counter);
                logger.debug("Counter for {}: {}", host, counter);
            }
            client.setURI(uri);

            Request request = Request.newGet();
            request.setURI(uri);
            request.setType(Type.CON);
            // request.setType(Type.NON);

            request.setObserve();
            logger.debug("Send COAP ping to {}:{}", client.getURI(), client.ping(TIMEOUT));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // pass
            }

            logger.debug("Start Observe request {}", uri);
            observe = client.observeAndWait(request, new CoapHandler() {

                @Override
                public void onLoad(@Nullable CoapResponse response) {
                    processCoapResponse(uri, response);
                }

                @Override
                public void onError() {
                    logger.debug("Error for {}", uri);
                }
            });

            logger.debug("Finished refreshdata for {}", host);
            return processCoapResponse(uri, observe.getCurrent());

        } catch (ConnectorException | IOException e) {
            logger.debug("Error while refreshing {}: {}", host, e.getMessage());
        }
        return "";
    }

    private String processCoapResponse(String uri, @Nullable CoapResponse response) {
        if (response != null) {
            if (!response.isSuccess()) {
                logger.debug("Response is success: {}", response.isSuccess());
            }
            logger.trace("Response is advanced: {}", response.advanced());
            String content = response.getResponseText();
            if (content != null) {
                String resp = processResponse(content.trim(), uri);
                logger.info("Response {}", resp);
                if (resp.length() > 2) {
                    coapStatus.putValue(resp);
                    return resp;
                }
            } else {
                logger.debug("Response content null for {}", response.advanced());
            }
        } else {
            logger.debug("Response is null for {}", uri);
        }
        return "";
    }

    private String processResponse(String rawResponse, String uri) {
        if (!rawResponse.isBlank()) {
            hasSync = true;
            logger.trace("Raw Response from {}: {}", uri, rawResponse);

            counter = getCounter(rawResponse);
            String decrypted = PhilipsAirCoapCipher.decryptMsg(rawResponse, logger);
            logger.debug("Response from {}: {}", uri, decrypted);
            JsonElement airResponse = JsonParser.parseString(decrypted);
            if (airResponse.isJsonObject() && airResponse.getAsJsonObject().has("state")) {
                JsonElement stateObj = airResponse.getAsJsonObject().get("state");
                if (stateObj.isJsonObject() && stateObj.getAsJsonObject().getAsJsonObject().has("reported")) {
                    return stateObj.getAsJsonObject().get("reported").toString();
                } else {
                    logger.debug("Response does not contain 'reported' element");
                }
            } else {
                logger.debug("Response does not contain 'state' element");
            }
        }
        syncCounter += 1;
        if (syncCounter > 3) {
            hasSync = false;
            syncCounter = 0;
        }
        logger.debug("No response for {}", uri);
        return "";
    }

    private String refreshDataOld() {
        try {
            logger.debug("Refreshing data for {}", host);
            String uri = getUriString(host, COAP_PORT, RESOURCE_PATH_STATUS);
            if (!hasSync) {
                counter = getSync(counter);
                logger.debug("Counter {}", counter);
            }
            if (!hasSync) {

                logger.debug("Send COAP ping to {}:{}", client.getURI(), client.ping(TIMEOUT));
            }
            String rawResponse = get(client, uri, Type.CON, !hasSync);
            if (!rawResponse.isBlank()) {
                hasSync = true;
                logger.debug("Rawesponse from {}: {}", uri, rawResponse);

                counter = getCounter(rawResponse);
                String decrypted = PhilipsAirCoapCipher.decryptMsg(rawResponse, logger);
                logger.debug("Response from {}: {}", uri, decrypted);
                JsonElement airResponse = JsonParser.parseString(decrypted);
                if (airResponse.isJsonObject() && airResponse.getAsJsonObject().has("state")) {
                    JsonElement stateObj = airResponse.getAsJsonObject().get("state");
                    if (stateObj.isJsonObject() && stateObj.getAsJsonObject().getAsJsonObject().has("reported")) {
                        return stateObj.getAsJsonObject().get("reported").toString();
                    } else {
                        logger.debug("Response does not contain 'reported' element");
                    }
                } else {
                    logger.debug("Response does not contain 'state' element");
                }
            } else {
                hasSync = false;
                logger.debug("No response for {}", uri);
                // return observe(client, uri, Type.NON, !hasSync);
                return "";
            }
        } catch (ConnectorException | IOException e) {
            logger.debug("Error while refreshing {}: {}", host, e.getMessage());
        }
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

    public PhilipsAirCoapAPIConnection(PhilipsAirConfiguration config) {
        super(config);
        if (!config.getHost().isEmpty()) {
            host = config.getHost();
        } else {
            logger.info("Host is empty, cannot start COAP connection");
        }
        if (config.getRefreshInterval() < 10) {
            logger.info("Refresh interval<10 not supported");
        }
        NetworkConfig netConfig = NetworkConfig.createStandardWithoutFile();
        NetworkConfig.getStandard().setString(NetworkConfig.Keys.DEDUPLICATOR, NetworkConfig.Keys.NO_DEDUPLICATOR);
        netConfig.setString(NetworkConfig.Keys.DEDUPLICATOR, NetworkConfig.Keys.NO_DEDUPLICATOR);
        CoapEndpoint endpoint = new CoapEndpoint.Builder().setNetworkConfig(netConfig).build();
        /*
         * MessageInterceptor interceptor = new MessageInterceptor() {
         * 
         * @Override
         * public void sendResponse(@Nullable Response response) {
         * String content = response.getPayloadString();
         * System.out.println("-CO04----------");
         * System.out.println(content);
         * }
         * 
         * @Override
         * public void sendRequest(@Nullable Request request) {
         * String content = request.getPayloadString();
         * System.out.println("-REQO04----------");
         * System.out.println(content);
         * }
         * 
         * @Override
         * public void sendEmptyMessage(@Nullable EmptyMessage message) {
         * // TODO Auto-generated method stub
         * }
         * 
         * @Override
         * public void receiveResponse(@Nullable Response response) {
         * String content = response.getPayloadString();
         * System.out.println("-RECECO04----------");
         * System.out.println(content);
         * System.out.println(Utils.prettyPrint(response));
         * }
         * 
         * @Override
         * public void receiveRequest(@Nullable Request request) {
         * // TODO Auto-generated method stub
         * }
         * 
         * @Override
         * public void receiveEmptyMessage(@Nullable EmptyMessage message) {
         * // TODO Auto-generated method stub
         * }
         * };
         * endpoint.addInterceptor(interceptor);
         */
        client.setEndpoint(endpoint);
        client.setTimeout(TIMEOUT);
        logger.debug("PhilipsAirCoapAPIConnection initialized using host {}", host);
    }

    @Override
    public PhilipsAirConfiguration getConfig() {
        return this.config;
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierDataDTO getAirPurifierStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(coapStatus.getValue(), PhilipsAirPurifierDataDTO.class);
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierDeviceDTO getAirPurifierDevice(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(coapStatus.getValue(), PhilipsAirPurifierDeviceDTO.class);
    }

    @Override
    public synchronized @Nullable PhilipsAirPurifierFiltersDTO getAirPurifierFiltersStatus(String host)
            throws JsonSyntaxException, PhilipsAirAPIException {
        return gson.fromJson(coapStatus.getValue(), PhilipsAirPurifierFiltersDTO.class);
    }

    @Override
    public @Nullable PhilipsAirPurifierDataDTO sendCommand(String parameter, PhilipsAirPurifierWritableDataDTO value) {
        try {
            long controlCounter = getSync(counter);
            logger.debug("ControlCounter from sync={}", controlCounter);
            JsonObject cmd = (JsonObject) gson.toJsonTree(value);
            cmd.addProperty("CommandType", "app");
            cmd.addProperty("DeviceId", "");
            cmd.addProperty("EnduserId", "1");
            PhilipsAirPurifierStateDTO state = new PhilipsAirPurifierStateDTO();
            state.setDesired(cmd);
            PhilipsAirPurifierStatusDTO fullCmd = new PhilipsAirPurifierStatusDTO();
            fullCmd.setState(state);
            String commandValue = gson.toJson(fullCmd).toString();
            controlCounter++;
            logger.info("Sending command {}", commandValue);
            String encryped = PhilipsAirCoapCipher.encryptedMsg(commandValue, controlCounter, logger);
            String response = encryped == null ? "Encrypted message failed"
                    : post(client, host, COAP_PORT, RESOURCE_PATH_CONTROL, encryped);
            if (response.contentEquals("{\"status\":\"success\"}")) {
                // Sleep for a bit, otherwise we won't get the new value in the response
                Thread.sleep(1000);
                coapStatus.refreshValue();
                return gson.fromJson(coapStatus.getValue(), PhilipsAirPurifierDataDTO.class);
            } else {
                logger.debug("Command failed. Response: {}", response);
            }
        } catch (JsonSyntaxException | ConnectorException | InterruptedException | IOException e) {
            logger.info("Error sending command '{}': {}", gson.toJson(value), e.getMessage(), e);
        }
        return null;
    }

    private long getSync(long counter) throws ConnectorException, IOException {
        String controlCounterResponse = post(client, host, COAP_PORT, RESOURCE_PATH_SYNC,
                String.format("%08X", counter));
        return getCounter(controlCounterResponse);
    }

    private static String getUriString(String server, int port, String resourcePath) {
        return "coap://" + server + ":" + port + resourcePath;
    }

    /**
     * Special network configuration defaults handler.
     *
     * private static NetworkConfigDefaultHandler DEFAULTS = new NetworkConfigDefaultHandler() {
     *
     * @Override
     *           public void applyDefaults(NetworkConfig config) {
     *           // config.setInt(Keys.MULTICAST_BASE_MID, 65000);
     *           // config.setInt(Keys., 65000);
     *           }
     *           };
     */

    private String get(CoapClient client, String uri, Type type, boolean sendPing)
            throws ConnectorException, IOException {
        logger.trace("Getting {}", uri);
        client.setURI(uri);
        if (sendPing) {
            logger.debug("Send COAP ping to {}:{}", client.getURI(), client.ping(TIMEOUT));
            ;
        }
        Request request = Request.newGet();
        request.setType(type);
        request.setObserve();

        CoapResponse response = client.advanced(request);
        if (response != null) {
            logger.trace("Response from {}: {}", response.advanced().getSourceContext().getPeerAddress(),
                    Utils.prettyPrint(response));
            return response.getResponseText();
        } else {
            logger.debug("No response received for {}.", uri);
        }
        return "";
    }

    private String post(CoapClient client, String server, int port, String resourcePath, String body)
            throws ConnectorException, IOException {
        String uri = "coap://" + server + ":" + port + resourcePath;
        client.setURI(uri);
        Request request = Request.newPost();
        request.setPayload(body);
        CoapResponse response = client.advanced(request);
        if (response != null) {
            logger.trace("POST {} -> Response: {}", uri, Utils.prettyPrint(response));
            logger.debug("POST {} -> Response: {}", uri, response.getResponseText());
            return response.getResponseText();
        } else {
            logger.debug("POST {}  -> No response received.", uri);
        }
        return "";
    }
}
