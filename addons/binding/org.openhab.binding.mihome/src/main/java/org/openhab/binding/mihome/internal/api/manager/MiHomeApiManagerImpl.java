/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.api.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.mihome.internal.api.JSONResponseHandler;
import org.openhab.binding.mihome.internal.api.constants.DeviceConstants;
import org.openhab.binding.mihome.internal.api.constants.DeviceTypesConstants;
import org.openhab.binding.mihome.internal.rest.RestClient;
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
public class MiHomeApiManagerImpl implements MiHomeApiManager {

    // MiHome controllers IDs
    public static final String CONTROLLER_DEVICES = "devices";
    public static final String CONTROLLER_SUBDEVICES = "subdevices";
    public static final String CONTROLLER_USERS = "users";

    // Actions
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_LIST = "list";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_SHOW = "show";
    public static final String ACTION_UPDATE = "update";
    /**
     * This is only internal Mi|Home firmware upgrade. There is no information about the device firmware description.
     */
    public static final String ACTION_UPDATE_FIRMWARE = "update_latest_firmware";

    private MiHomeApiConfiguration configuration;
    private RestClient restClient;
    private FailingRequestHandler failingRequestHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public MiHomeApiManagerImpl(MiHomeApiConfiguration configuration, RestClient restClient,
            FailingRequestHandler requestHandler) {
        this.configuration = configuration;
        this.restClient = restClient;
        this.failingRequestHandler = requestHandler;
    }

    @Override
    public JsonObject registerGateway(String label, String authCode) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.DEVICE_TYPE_KEY, DeviceTypesConstants.GATEWAY_TYPE);
        parameters.put(DeviceConstants.DEVICE_LABEL_KEY, label);
        parameters.put(DeviceConstants.GATEWAY_AUTH_CODE_KEY, authCode);

        return execute(CONTROLLER_DEVICES, ACTION_CREATE, parameters);
    }

    @Override
    public JsonObject listGateways() {
        return execute(CONTROLLER_DEVICES, ACTION_LIST);

    }

    @Override
    public JsonObject unregisterGateway(int id) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.DEVICE_ID_KEY, id);

        return execute(CONTROLLER_DEVICES, ACTION_DELETE, parameters);
    }

    @Override
    public JsonObject upgradeGatewayFirmware(int id) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.DEVICE_ID_KEY, id);

        return execute(CONTROLLER_DEVICES, ACTION_UPDATE_FIRMWARE, parameters);
    }

    @Override
    public JsonObject listSubdevices() {
        return execute(CONTROLLER_SUBDEVICES, ACTION_LIST);
    }

    @Override
    public JsonObject registerSubdevice(int gatewayID, String deviceType) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.SUBDEVICE_PARENT_ID_KEY, gatewayID);
        parameters.put(DeviceConstants.DEVICE_TYPE_KEY, deviceType);
        return execute(CONTROLLER_SUBDEVICES, ACTION_CREATE, parameters);

    }

    @Override
    public JsonObject showSubdeviceInfo(int id) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.DEVICE_ID_KEY, id);
        // With this parameter we exclude the historical usage data ("1" - to include, "0" - to exclude)
        // The historical data is from no importance to the user,
        // it consists of the number of events for each hour from the last 24 hours.
        parameters.put(DeviceConstants.SUBDEVICE_INCLUDE_USAGE_DATA, 0);
        return execute(CONTROLLER_SUBDEVICES, ACTION_SHOW, parameters);
    }

    @Override
    public JsonObject updateSubdevice(int id, String label) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.DEVICE_ID_KEY, id);
        parameters.put(DeviceConstants.DEVICE_LABEL_KEY, label);
        return execute(CONTROLLER_SUBDEVICES, ACTION_UPDATE, parameters);
    }

    @Override
    public JsonObject unregisterSubdevice(int id) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(DeviceConstants.DEVICE_ID_KEY, id);
        return execute(CONTROLLER_SUBDEVICES, ACTION_DELETE, parameters);
    }

    private JsonObject execute(String controller, String action) {
        return execute(controller, action, new HashMap<String, Object>());
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
            contentResponse = restClient.sendRequest(controller + "/" + action, RestClient.DEFAULT_HTTP_METHOD,
                    httpHeaders, requestContent, RestClient.CONTENT_TYPE);

            byte[] rawResponse = contentResponse.getContent();
            String encoding = contentResponse.getEncoding() != null
                    ? contentResponse.getEncoding().replaceAll("\"", "").trim() : "UTF-8";
            responseBody = new String(rawResponse, encoding);
        } catch (IOException e1) {
            logger.error("Request execution failed: ", e1);
            String failedUrl = restClient.getBaseURL() + controller + "/" + action;
            failingRequestHandler.handleIOException(failedUrl, e1);
            return null;
        }

        logger.trace(responseBody);

        if (!responseBody.isEmpty()) {
            int statusCode = contentResponse.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                String statusLine = statusCode + " " + contentResponse.getReason();
                logger.debug("HTTP request was not successfull: {}", statusLine);
                failingRequestHandler.handleFailingHttpRequest(contentResponse);
            } else {
                JsonObject jsonResponse = null;
                try {
                    jsonResponse = JSONResponseHandler.responseStringtoJsonObject(responseBody);
                } catch (JsonParseException e) {
                    logger.error("An JsonParseException occurred by parsing JSON response: " + jsonResponse, e);
                    return null;
                }
                if (JSONResponseHandler.isRequestSuccessful(jsonResponse)) {
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
