/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energenie.internal.api.EnergenieDeviceTypes;
import org.openhab.binding.energenie.internal.api.JsonDevice;
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonResponseHandler;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * A helper class for executing requests to the Mi|Home REST API.
 * For more information see {@link https://mihome4u.co.uk/docs/api-documentation}
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Mihaela Memova - Added {@link FailingRequestHandler}
 */
public class EnergenieApiManagerImpl implements EnergenieApiManager {

    private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";
    // Mi|Home controllers IDs
    public static final String CONTROLLER_DEVICES = "devices";
    public static final String CONTROLLER_SUBDEVICES = "subdevices";
    public static final String CONTROLLER_USERS = "users";

    // Actions
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_LIST = "list";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_SHOW = "show";
    public static final String ACTION_UPDATE = "update";

    // Common constants used for request creation
    private static final String DEVICE_TYPE_KEY = "device_type";
    private static final String DEVICE_ID_KEY = "id";
    private static final String DEVICE_LABEL_KEY = "label";
    private static final String GATEWAY_AUTH_CODE_KEY = "auth_code";
    private static final String SUBDEVICE_PARENT_ID_KEY = "device_id";
    private static final String SUBDEVICE_INCLUDE_USAGE_DATA = "include_usage_data";

    /**
     * This is only internal Mi|Home firmware upgrade. There is no information about the device firmware description.
     */
    public static final String ACTION_UPDATE_FIRMWARE = "update_latest_firmware";

    private EnergenieApiConfiguration configuration;
    private RestClient restClient;
    private FailingRequestHandler failingRequestHandler;

    private final Logger logger = LoggerFactory.getLogger(EnergenieApiManagerImpl.class);

    public EnergenieApiManagerImpl(EnergenieApiConfiguration configuration, RestClient restClient,
            FailingRequestHandler requestHandler) {
        this.configuration = configuration;
        this.restClient = restClient;
        this.failingRequestHandler = requestHandler;
    }

    @Override
    public EnergenieApiConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public JsonGateway registerGateway(String label, String authCode) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_TYPE_KEY, EnergenieDeviceTypes.GATEWAY.toString());
        parameters.put(DEVICE_LABEL_KEY, label);
        parameters.put(GATEWAY_AUTH_CODE_KEY, authCode);

        JsonObject result = execute(CONTROLLER_DEVICES, ACTION_CREATE, parameters);
        return JsonResponseHandler.getObject(result, JsonGateway.class);
    }

    @Override
    public JsonGateway[] listGateways() {
        JsonObject result = execute(CONTROLLER_DEVICES, ACTION_LIST);
        return JsonResponseHandler.getObject(result, JsonGateway[].class);

    }

    @Override
    public JsonGateway unregisterGateway(int id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_ID_KEY, id);

        JsonObject result = execute(CONTROLLER_DEVICES, ACTION_DELETE, parameters);
        return JsonResponseHandler.getObject(result, JsonGateway.class);
    }

    @Override
    public JsonSubdevice[] listSubdevices() {
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_LIST);
        return JsonResponseHandler.getObject(result, JsonSubdevice[].class);
    }

    @Override
    public JsonDevice registerSubdevice(int gatewayID, EnergenieDeviceTypes deviceType) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(SUBDEVICE_PARENT_ID_KEY, gatewayID);
        parameters.put(DEVICE_TYPE_KEY, deviceType.toString());
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_CREATE, parameters);
        return JsonResponseHandler.getObject(result, JsonDevice.class);

    }

    @Override
    public JsonSubdevice showSubdeviceInfo(int id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_ID_KEY, id);
        // With this parameter we exclude the historical usage data ("1" - to include, "0" - to exclude)
        // The historical data is from no importance to the user,
        // it consists of the number of events for each hour from the last 24 hours.
        parameters.put(SUBDEVICE_INCLUDE_USAGE_DATA, 0);
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_SHOW, parameters);
        return JsonResponseHandler.getObject(result, JsonSubdevice.class);
    }

    @Override
    public JsonSubdevice updateSubdevice(int id, String label) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_ID_KEY, id);
        parameters.put(DEVICE_LABEL_KEY, label);
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_UPDATE, parameters);
        return JsonResponseHandler.getObject(result, JsonSubdevice.class);
    }

    @Override
    public JsonSubdevice unregisterSubdevice(int id) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_ID_KEY, id);
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_DELETE, parameters);
        return JsonResponseHandler.getObject(result, JsonSubdevice.class);
    }

    private JsonObject execute(String controller, String action) {
        return execute(controller, action, new HashMap<>());
    }

    private JsonObject execute(String controller, String action, Map<String, Object> content) {

        Properties httpHeaders = new Properties();

        // Set basic authentication header
        String authData = configuration.getUserName() + ":" + configuration.getPassword();
        String basicAuthentication = "Basic " + Base64.getEncoder().encodeToString(authData.getBytes());
        basicAuthentication = basicAuthentication.trim();
        httpHeaders.setProperty("Authorization", basicAuthentication);

        // Convert request the content to JSON
        Gson gson = new Gson();
        String jsonContent = gson.toJson(content);
        InputStream requestContent = IOUtils.toInputStream(jsonContent);
        ContentResponse contentResponse = null;
        String responseBody = null;

        try {
            // all requests are sent using POST method, as it is recommended from the API documentation
            contentResponse = restClient.sendRequest(controller + "/" + action, RestClient.DEFAULT_HTTP_METHOD,
                    httpHeaders, requestContent, RestClient.CONTENT_TYPE);

            byte[] rawResponse = contentResponse.getContent();

            String encoding = contentResponse.getEncoding() != null
                    ? contentResponse.getEncoding().replaceAll("\"", "").trim() : DEFAULT_RESPONSE_ENCODING;

            if (!Charset.availableCharsets().keySet().contains(encoding)) {
                logger.warn("Unsupported content encoding : {}. Fall back to the default encoding {}", encoding,
                        DEFAULT_RESPONSE_ENCODING);
                encoding = DEFAULT_RESPONSE_ENCODING;
            }
            responseBody = new String(rawResponse, encoding);

        } catch (UnsupportedEncodingException e) {
            logger.error("Content encoding is not supported: ", e);
            String failedUrl = restClient.getBaseURL() + controller + "/" + action;
            failingRequestHandler.handleIOException(failedUrl, e);
            return null;
        } catch (IOException e) {
            logger.error("Request execution failed: ", e);
            String failedUrl = restClient.getBaseURL() + controller + "/" + action;
            failingRequestHandler.handleIOException(failedUrl, e);
            return null;
        }

        if (!responseBody.isEmpty()) {
            int statusCode = contentResponse.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                String statusLine = statusCode + " " + contentResponse.getReason();
                logger.debug("HTTP request was not successfull: {}", statusLine);
                failingRequestHandler.handleFailingHttpRequest(contentResponse);
            } else {
                JsonObject jsonResponse = null;
                try {
                    jsonResponse = JsonResponseHandler.responseStringtoJsonObject(responseBody);
                } catch (JsonParseException e) {
                    logger.error("An error occurred while trying to parse the JSON response {}:", jsonResponse, e);
                    return null;
                }
                if (JsonResponseHandler.isRequestSuccessful(jsonResponse)) {
                    return jsonResponse;
                } else {
                    failingRequestHandler.handleFailingJsonRequest(jsonResponse);
                    return null;
                }
            }
        }
        return null;
    }
}
