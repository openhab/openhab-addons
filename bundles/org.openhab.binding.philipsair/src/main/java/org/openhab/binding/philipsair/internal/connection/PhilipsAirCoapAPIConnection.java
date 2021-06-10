/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.eclipse.californium.core.network.config.NetworkConfigDefaultHandler;
import org.eclipse.californium.elements.exception.ConnectorException;
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
public class PhilipsAirCoapAPIConnection extends PhilipsAirAPIConnection {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCoapAPIConnection.class);
    private static final String RESOURCE_PATH_STATUS = "/sys/dev/status";
    private static final String RESOURCE_PATH_SYNC = "/sys/dev/sync";
    private static final String RESOURCE_PATH_CONTROL = "/sys/dev/control";
    private static final int COAP_PORT = 5683;
    private static final long TIMEOUT = 5000;

    private final Gson gson = new Gson();
    private ExpiringCache<String> coapStatus = new ExpiringCache<>(10, this::refreshData);
    private String host = "";
    private CoapClient client = new CoapClient();
    private long counter = 1;
    private boolean hasSync = false;

    private String refreshData() {
        try {
            logger.debug("Refreshing data for {}", host);
            String uri = getUriString(host, COAP_PORT, RESOURCE_PATH_STATUS);
            if (!hasSync) {
                counter = getSync(counter);
            }
            String rawResponse = get(client, uri, Type.CON, !hasSync);
            if (!rawResponse.isBlank()) {
                hasSync = true;
                counter = getCounter(rawResponse);
                String decrypted = PhilipsAirCoapCipher.decryptMsg(rawResponse);
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
            String encryped = PhilipsAirCoapCipher.encryptedMsg(commandValue, controlCounter);
            String response = post(client, host, COAP_PORT, RESOURCE_PATH_CONTROL, encryped);
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
     */
    private static NetworkConfigDefaultHandler DEFAULTS = new NetworkConfigDefaultHandler() {

        @Override
        public void applyDefaults(NetworkConfig config) {
            // config.setInt(Keys.MULTICAST_BASE_MID, 65000);
            // config.setInt(Keys., 65000);
        }
    };

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

    private String observe(CoapClient client, String uri, Type type, boolean sendPing)
            throws ConnectorException, IOException {
        client.setURI(uri);
        logger.debug("OBSERVE {}", uri);
        client.setURI(uri);

        Request request = Request.newGet();
        request.setType(type);
        request.setObserve();
        CoapObserveRelation relation4 = client.observeAndWait(new CoapHandler() {

            @Override
            public void onLoad(CoapResponse response) {
                String content = response.getResponseText();
                logger.debug("-CO04----------");
                logger.debug("{}", content);
                String resp = PhilipsAirCoapCipher.decryptMsg(content.trim());
                logger.debug("Decryped response: {}", resp);
                logger.info("Response {}", resp);
            }

            @Override
            public void onError() {
                logger.debug("-Failed Observe--------");
            }
        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            logger.debug("Stopped");
        }
        // dummy
        // logger.debug("response fro observe {}", relation4.getCurrent().getResponseText());
        relation4.reactiveCancel();
        // return PhilipsAirCoapCipher.decryptMsg(
        // "0000CCDE6BCB393105422EAEF552A739E2737A0114FF1C9B95426E0EB291907C5833E9C4377EE2329AB344954707A9401976006AD9A1EDBB4C4395492567C0BF92530941E81331E6FC25A4284F611CF6F4E22C01AC516A35575C317BB268438F48D97FE052A6C0B96E28BFED37A70767B0E08D723F5C807C083AA7FE2A97532DF81E19A670164608B22BAD113A7DF1B0330E67F67111AF3B1152F8562D3940ACDBAEB97A0A91125014BDB7104DFEE4A0BFADB780C125F7E3E02260030473C5814323240B89AB42A1380444FCA9DA4A2DD11177DF048F8C2F89EE146BED1A3423FC5B1167F5443E68ECF0E63E4DA19260186824765C64045F3012D8D8116454E0EBB276DA9C008819DF675E281F4D3AE542784C17A8B959013588DAAEBD18B958DEF941BD9B0799C121D0ECBBACEC0A058704DD4E59460EA02C491D14D660B5D6C8FC54244AC1013F7221232EA11A898515330F77FCF4F9AB3253925BB4854A2FEADDB7142CD7FDC11908D4DF08A8734CAF20B20A757A0C6FA712A65A30241C6E984D8FBD5A10116CE7E30898F0E61B5CF5D23326F7ADA89E147CDC44E29ED937C927D1DE19D8E54395F4F2E919DC638B2474EACA65626E3AB4259B4EDF3D16F4D6694117808D1331F04643E50AC8E1E32FFA6E558034B446A2F9ADE43745C66C8B03F94B7D9DA045A2361DA673BC8FC5F925C9BB6621C8E04497263E63D041C133E868FAB22389572B65814916531EAD89863E8642B5A16FE6F3C71AE1CA8D213FE4E3F534877F0040978EFAAA08B6A60BFDA252B91E962AFC1EAD4E760CB76551F8BAC8A32118BD587C641EFBEB648B2532D769");

        return "";
    }
}
