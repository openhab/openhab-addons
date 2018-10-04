/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.openhab.binding.energenie.internal.api.JsonGateway;
import org.openhab.binding.energenie.internal.api.JsonResponseUtil;
import org.openhab.binding.energenie.internal.api.JsonSubdevice;
import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulHttpResponseException;
import org.openhab.binding.energenie.internal.exceptions.UnsuccessfulJsonResponseException;
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

    // Actions
    public static final String ACTION_LIST = "list";
    public static final String ACTION_SHOW = "show";
    public static final String ACTION_SHOW_FIRMWARE_INFORMATION = "firmware_information";

    // Common constants used for request creation
    public static final String DEVICE_ID_KEY = "id";
    public static final String SUBDEVICE_INCLUDE_USAGE_DATA = "include_usage_data";
    public static final String DEVICE_FIRMWARE_INFORMATION = "running_firmware_version_name";

    private EnergenieApiConfiguration configuration;
    private RestClient restClient;

    private Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(EnergenieApiManagerImpl.class);

    public EnergenieApiManagerImpl(EnergenieApiConfiguration configuration, RestClient restClient) {
        this.configuration = configuration;
        this.restClient = restClient;
    }

    @Override
    public EnergenieApiConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public JsonGateway[] listGateways()
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        JsonObject result = execute(CONTROLLER_DEVICES, ACTION_LIST);
        // The result may come as a JsonObject or as an JsonObcect[] with a single element
        if (result != null && result.has("data") && result.get("data") instanceof JsonObject) {
            JsonGateway res = JsonResponseUtil.getObject(result, JsonGateway.class);
            return new JsonGateway[] { res };
        } else {
            return JsonResponseUtil.getObject(result, JsonGateway[].class);
        }
    }

    @Override
    public JsonSubdevice[] listSubdevices()
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_LIST);
        return JsonResponseUtil.getObject(result, JsonSubdevice[].class);
    }

    @Override
    public JsonSubdevice showSubdeviceInfo(int id)
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_ID_KEY, id);
        // With this parameter we exclude the historical usage data ("1" - to include, "0" - to exclude)
        // The historical data is from no importance to the user,
        // it consists of the number of events for each hour from the last 24 hours.
        parameters.put(SUBDEVICE_INCLUDE_USAGE_DATA, 0);
        JsonObject result = execute(CONTROLLER_SUBDEVICES, ACTION_SHOW, parameters);
        return JsonResponseUtil.getObject(result, JsonSubdevice.class);
    }

    @Override
    public String getFirmwareInformation(int id)
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(DEVICE_ID_KEY, id);
        JsonObject result = execute(CONTROLLER_DEVICES, ACTION_SHOW_FIRMWARE_INFORMATION, parameters);
        String firmwareVersion = null;
        if (result != null) {
            JsonObject data = result.get(JsonResponseConstants.DATA_KEY).getAsJsonObject();
            firmwareVersion = data.getAsJsonPrimitive(DEVICE_FIRMWARE_INFORMATION).getAsString();
        }
        return firmwareVersion;
    }

    private JsonObject execute(String controller, String action)
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {
        return execute(controller, action, new HashMap<>());
    }

    private JsonObject execute(String controller, String action, Map<String, Object> content)
            throws IOException, UnsuccessfulJsonResponseException, UnsuccessfulHttpResponseException {

        Properties httpHeaders = new Properties();

        // Set basic authentication header
        String authData = configuration.getUserName() + ":" + configuration.getPassword();

        String basicAuthentication = "Basic " + Base64.getEncoder().encodeToString(authData.getBytes());
        basicAuthentication = basicAuthentication.trim();

        httpHeaders.setProperty("Authorization", basicAuthentication);

        // Convert request the content to JSON
        String jsonContent = gson.toJson(content);
        InputStream requestContent = IOUtils.toInputStream(jsonContent);
        ContentResponse contentResponse = null;
        String responseBody = null;

        try {
            // all requests are sent using POST method, as it is recommended from the API documentation
            String requestPath = controller + "/" + action;
            contentResponse = restClient.sendRequest(requestPath, RestClient.DEFAULT_HTTP_METHOD, httpHeaders,
                    requestContent, RestClient.CONTENT_TYPE);

            byte[] rawResponse = contentResponse.getContent();

            String encoding = contentResponse.getEncoding() != null
                    ? contentResponse.getEncoding().replaceAll("\"", "").trim()
                    : DEFAULT_RESPONSE_ENCODING;

            if (!Charset.availableCharsets().keySet().contains(encoding)) {
                logger.warn("Unsupported content encoding : {}. Fall back to the default encoding: {}", encoding,
                        DEFAULT_RESPONSE_ENCODING);
                encoding = DEFAULT_RESPONSE_ENCODING;
            }
            responseBody = new String(rawResponse, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("Content encoding is not supported: {}", e.getMessage(), e);
            String failedUrl = restClient.getBaseURL() + "/" + controller + "/" + action;
            throw new UnsupportedEncodingException(failedUrl);

        } catch (IOException e) {
            logger.error("Request execution failed: {}", e.getMessage(), e);
            String failedUrl = restClient.getBaseURL() + "/" + controller + "/" + action;
            throw new IOException(failedUrl);
        }

        if (!responseBody.isEmpty()) {
            int statusCode = contentResponse.getStatus();
            if (statusCode != HttpStatus.OK_200) {
                String statusLine = statusCode + " " + contentResponse.getReason();
                logger.debug("HTTP request was not successfull: {}", statusLine);
                throw new UnsuccessfulHttpResponseException(contentResponse);
            } else {
                JsonObject jsonResponse = null;
                try {
                    jsonResponse = JsonResponseUtil.responseStringtoJsonObject(responseBody);
                } catch (JsonParseException e) {
                    logger.error("An error occurred while trying to parse the JSON response: {}", jsonResponse, e);
                    return null;
                }
                if (JsonResponseUtil.isRequestSuccessful(jsonResponse)) {
                    return jsonResponse;
                } else {
                    throw new UnsuccessfulJsonResponseException(jsonResponse);
                }
            }
        }
        return null;
    }

}
