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
package org.openhab.binding.cloudrain.internal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.AbstractTypedContentProvider;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.cloudrain.internal.api.model.AuthParams;
import org.openhab.binding.cloudrain.internal.api.model.CloudrainAPIError;
import org.openhab.binding.cloudrain.internal.api.model.CommandParams;
import org.openhab.binding.cloudrain.internal.api.model.Controller;
import org.openhab.binding.cloudrain.internal.api.model.ControllerResult;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.IrrigationResult;
import org.openhab.binding.cloudrain.internal.api.model.Token;
import org.openhab.binding.cloudrain.internal.api.model.Zone;
import org.openhab.binding.cloudrain.internal.api.model.ZoneResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A {@link CloudrainAPI} implementation to access the Cloudrain Developer API V1 as documented in
 * {@link https://developer.cloudrain.com/documentation/api-documentation}. Instantiated objects of this class should be
 * considered stateful as they store the access token obtained from an authentication request and managed subsequent
 * token refresh and re-authentication. If authentication details change during usage of the API a new initialization
 * and authentication may be performed.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainAPIv1Impl implements CloudrainAPI {

    private final Logger logger = LoggerFactory.getLogger(CloudrainAPIv1Impl.class);

    private Gson gson = new Gson();

    private static final String API_ERROR_UNEXPECTED_API_RESPONSE = "Unexpected API Response. Status: {}, Message: {}";
    private static final String API_ERROR_TOKEN_REQUIRED = "No valid API Token available.";
    private static final String API_ERROR_NO_VALID_TOKEN_RECEIVED = "Unable to obtain a valid Token from API authentication";
    private static final String API_ERROR_HTTP_CLIENT_START = "HTTP Client is not starting correclty. Details: {}";
    private static final String API_ERROR_DEBUG_RESPONSE = "Endpoint: '{}', status: '{}', resonse (excerpt): '{}'. Trace for full response.";
    private static final String API_ERROR_TRACE_RESPONSE = "Full Cloudrain API response: '{}'";

    private static final String API_HEADER_ACCEPT_ENCODING = "gzip, deflate, br";

    private static final String API_FIELD_CLIENT_ID = "client_id";
    private static final String API_FIELD_CLIENT_SECRET = "client_secret";
    private static final String API_FIELD_GRANT_TYPE = "grant_type";
    private static final String API_FIELD_USER_NAME = "username";
    private static final String API_FIELD_PW = "password";
    private static final String API_FIELD_SCOPE = "scope";
    private static final String API_FIELD_REFRESH_TOKEN = "refresh_token";
    private static final String API_FIELD_SCOPE_VALUE = "read start_irrigation";
    private static final String API_FIELD_GRANT_TYPE_PW = "password";
    private static final String API_FIELD_GRANT_TYPE_REFRESH = "refresh_token";

    private static final String API_CONTENT_TYPE_JSON = "application/json";
    private static final String API_CONTENT_TYPE_WWW_FORM = "application/x-www-form-urlencoded;charset=UTF-8";

    private static final String API_URL_CLOUDRAIN = "https://api.cloudrain.com/v1";
    private static final String API_URL_TOKEN = API_URL_CLOUDRAIN + "/token";
    private static final String API_URL_CONTROLLERS = API_URL_CLOUDRAIN + "/controllers";
    private static final String API_URL_IRRIGATIONS = API_URL_CLOUDRAIN + "/irrigations";
    private static final String API_URL_ZONES = API_URL_CLOUDRAIN + "/zones";
    private static final String API_URL_ZONE_COMMAND = API_URL_ZONES + "/%s/irrigation";

    private static final Set<Integer> VALID_RETURN_STATUS_LIST = Set.of(200, 201, 202, 204);

    /**
     * A reference to the {@link HttpClient} fir the API communication
     */
    private HttpClient httpClient;

    /**
     * The {@link AuthParams} containing the authentication attributes. Stored for re-authenticating in case of expired
     * access and refresh tokens
     */
    private @Nullable AuthParams authParams;

    /**
     * The {@link Token} containing the access and refresh token obtained from authenticating at the Cloudrain API.
     * Stored to handle expired tokens without consumer interaction.
     */
    private @Nullable Token token;

    /**
     * Creates an instance of this API implementation class
     *
     * @param httpClient the {@link HttpClient} for the HTTP connections
     */
    public CloudrainAPIv1Impl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void authenticate(AuthParams params) throws CloudrainAPIException {
        // refreshing a token without passing a refresh token performs an authentication
        refreshToken(params, null);
    }

    /**
     * Refreshes an access token using the refresh token.
     *
     * @param token the {@link Token} with expired access token, but valid refresh token
     * @throws CloudrainAPIException the exception in case of errors
     */
    public void refreshToken(AuthParams params, @Nullable String refreshToken) throws CloudrainAPIException {
        try {
            // call method handling authentication and refresh API calls
            Token newToken = getTokenFromAPI(params, refreshToken);
            // initialize, check and store the received token
            this.token = initializeNewToken(newToken);
            // store the authentication parameters for later refreshing if required
            this.authParams = params;
        } catch (CloudrainAPIException ex) {
            this.token = null;
            this.authParams = null;
            throw ex;
        }
    }

    /**
     * This method calls the /token API end point for authentication and token refresh
     *
     * @param params the authentication parameters
     * @param refreshToken Attempts a refresh if a refresh token is passed. Authentication otherwise
     * @throws CloudrainAPIException
     */
    private @Nullable Token getTokenFromAPI(AuthParams params, @Nullable String refreshToken)
            throws CloudrainAPIException {
        // prepare the input parameters
        Fields fields = new Fields();
        fields.add(API_FIELD_CLIENT_ID, params.getClientId());
        fields.add(API_FIELD_CLIENT_SECRET, params.getClientSecret());
        if (refreshToken != null) {
            fields.add(API_FIELD_GRANT_TYPE, API_FIELD_GRANT_TYPE_REFRESH);
            fields.add(API_FIELD_REFRESH_TOKEN, refreshToken);
        } else {
            fields.add(API_FIELD_GRANT_TYPE, API_FIELD_GRANT_TYPE_PW);
            fields.add(API_FIELD_USER_NAME, params.getUser());
            fields.add(API_FIELD_PW, params.getPassword());
            fields.add(API_FIELD_SCOPE, API_FIELD_SCOPE_VALUE);
        }
        // send the request
        Token newToken = sendApiRequest(HttpMethod.POST, API_URL_TOKEN, fields, Token.class);
        return newToken;
    }

    /**
     * Executes an API request with the given method, URL, content and expected result object.
     *
     * @param <T> The class of the expected result object
     * @param method The HTTP method for the request
     * @param url the URL to be contacted
     * @param content the content to be passed into the request which may wither be Fields or content for a JSON body
     * @param result the expected result object. The method will attempt to parse JSON results into this objects using
     *            {@link GSON}
     * @param token a valid {@link Token} to authorize the request. Null if the request is intended to obtain a token.
     * @return the result object of a class as stated in the input parameters
     * @throws CloudrainAPIException the exception in case of a communication or other error
     */
    private @Nullable <T> T sendApiRequest(HttpMethod method, String url, @Nullable Object content,
            @Nullable Class<T> resultClass) throws CloudrainAPIException {
        try {
            // validate whether the HttpClient is running
            validateHttpClient();
            // prepare the content
            AbstractTypedContentProvider contentProvider = null;
            String contentType = API_CONTENT_TYPE_JSON;
            if (content != null) {
                if (content instanceof Fields) {
                    contentProvider = new FormContentProvider((Fields) content);
                    contentType = API_CONTENT_TYPE_WWW_FORM;
                } else {
                    contentProvider = new StringContentProvider(gson.toJson(content));
                }
            }
            // prepare the request
            Request request = httpClient.newRequest(url).method(method).header(HttpHeader.CONTENT_TYPE, contentType)
                    .header(HttpHeader.ACCEPT, API_CONTENT_TYPE_JSON)
                    .header(HttpHeader.ACCEPT_ENCODING, API_HEADER_ACCEPT_ENCODING);
            request.content(contentProvider);

            // set the authorization header unless this is not the authentication step
            if (!API_URL_TOKEN.equals(url)) {
                if (!isTokenValid()) {
                    refreshToken();
                }
                Token validToken = token;
                if (validToken != null) {
                    String tokenData = validToken.getTokenType() + " " + validToken.getAccessToken();
                    request.header(HttpHeader.AUTHORIZATION, tokenData);
                } else {
                    throw new CloudrainAPIException(API_ERROR_TOKEN_REQUIRED);
                }
            }
            // send the request
            ContentResponse contentResponse = request.send();
            String resonse = contentResponse.getContentAsString();
            int status = contentResponse.getStatus();

            // check the status
            if (!VALID_RETURN_STATUS_LIST.contains(status)) {
                // try to parse the error message from the API
                CloudrainAPIError error = gson.fromJson(resonse, CloudrainAPIError.class);
                if (error == null) {
                    error = new CloudrainAPIError();
                }
                error.setFullResponse(resonse);
                logger.debug(API_ERROR_DEBUG_RESPONSE, url, status, error.getPartialResponse(30));
                logger.trace(API_ERROR_TRACE_RESPONSE, error.getFullResponse());
                throw new CloudrainAPIException(API_ERROR_UNEXPECTED_API_RESPONSE, error);
            }
            // process the output in case it is expected
            if (resultClass != null) {
                return (T) gson.fromJson(resonse, resultClass);
            } else {
                return null;
            }
        } catch (CloudrainAPIException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudrainAPIException(ex);
        }
    }

    @Override
    public List<Controller> getControllers() throws CloudrainAPIException {
        List<Controller> result = new ArrayList<Controller>();
        ControllerResult apiResult = sendApiRequest(HttpMethod.GET, API_URL_CONTROLLERS, null, ControllerResult.class);
        if (apiResult != null) {
            result = apiResult.getControllerList();
        }
        return result;
    }

    @Override
    public @Nullable Zone getZone(String zoneId) throws CloudrainAPIException {
        String url = API_URL_ZONES + "/" + zoneId;
        Zone result = sendApiRequest(HttpMethod.GET, url, null, Zone.class);
        return result;
    }

    @Override
    public List<Zone> getZones() throws CloudrainAPIException {
        List<Zone> result = new ArrayList<Zone>();
        ZoneResult apiResult = sendApiRequest(HttpMethod.GET, API_URL_ZONES, null, ZoneResult.class);
        if (apiResult != null) {
            result = apiResult.getZoneList();
        }
        return result;
    }

    @Override
    public List<Irrigation> getIrrigations() throws CloudrainAPIException {
        List<Irrigation> result = new ArrayList<Irrigation>();
        IrrigationResult apiResult = sendApiRequest(HttpMethod.GET, API_URL_IRRIGATIONS, null, IrrigationResult.class);
        if (apiResult != null) {
            result = apiResult.getIrrigationList();
        }
        return result;
    }

    @Override
    public @Nullable Irrigation getIrrigation(String zoneId) throws CloudrainAPIException {
        String url = String.format(API_URL_ZONE_COMMAND, zoneId);
        IrrigationResult result = sendApiRequest(HttpMethod.GET, url, null, IrrigationResult.class);
        if (result != null) {
            return result.getFirstEntry();
        }
        return null;
    }

    @Override
    public void startIrrigation(String zoneId, int duration) throws CloudrainAPIException {
        String url = String.format(API_URL_ZONE_COMMAND, zoneId);
        sendApiRequest(HttpMethod.PUT, url, new CommandParams(duration), null);
    }

    @Override
    public void adjustIrrigation(String zoneId, int duration) throws CloudrainAPIException {
        String url = String.format(API_URL_ZONE_COMMAND, zoneId);
        sendApiRequest(HttpMethod.PATCH, url, new CommandParams(duration), null);
    }

    @Override
    public void stopIrrigation(String zoneId) throws CloudrainAPIException {
        String url = String.format(API_URL_ZONE_COMMAND, zoneId);
        sendApiRequest(HttpMethod.DELETE, url, null, null);
    }

    @Override
    public void initialize(CloudrainAPIConfig config) throws CloudrainAPIException {
        try {
            if (httpClient.isRunning()) {
                httpClient.stop();
            }
            httpClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);
            httpClient.start();
        } catch (Exception e) {
            logger.warn(API_ERROR_HTTP_CLIENT_START, e.getMessage());
            throw new CloudrainAPIException(e);
        }
    }

    /**
     * Returns the API token received after authentication
     *
     * @return the API token received after authentication
     */
    public @Nullable Token getToken() {
        return token;
    }

    /**
     * Sets an API token to be used by the API
     *
     * @param an API token to be used by the API
     */
    public void setToken(Token token) {
        this.token = token;
    }

    /**
     * Initializes a newly received token from the API and checks its validity
     *
     * @param newToken the token to initialize
     * @return the initialized token in case of success
     * @throws CloudrainAPIException in case of problems with the validity of the token
     */
    private Token initializeNewToken(@Nullable Token newToken) throws CloudrainAPIException {
        if (newToken != null) {
            newToken.initialize();
        }
        // only a valid token is considered a positive result
        if (newToken == null || !newToken.isTokenValid()) {
            throw new CloudrainAPIException(API_ERROR_NO_VALID_TOKEN_RECEIVED);
        }
        return newToken;
    }

    /**
     * Validates the {@link Token} member variable. Checks existance of the token member variable instance and validity
     * of its access and refresh token
     *
     * @return true if the token is valid. False otherwise
     */
    private boolean isTokenValid() throws CloudrainAPIException {
        Token theToken = this.token;
        if (theToken != null) {
            return theToken.isAccessTokenValid() && theToken.isRefreshTokenValid();
        }
        return false;
    }

    /**
     * Refreshes the {@link Token} member variable. Handles refreshing the token in case of expired access token or
     * re-authenticating in case of expired refresh token.
     *
     * @throws CloudrainAPIException the exception in case of an error
     */
    private void refreshToken() throws CloudrainAPIException {
        Token theToken = this.token;
        AuthParams params = authParams;
        if (theToken != null && params != null) {
            if (!theToken.isAccessTokenValid()) {
                if (theToken.isRefreshTokenValid()) {
                    refreshToken(params, theToken.getRefreshToken());
                } else {
                    authenticate(params);
                }
            }
        }
        // Final check: authenticate and refreshToken store the resulting token in tbe token member
        theToken = this.token;
        if (theToken == null || !theToken.isAccessTokenValid()) {
            throw new CloudrainAPIException(API_ERROR_TOKEN_REQUIRED);
        }
    }

    /**
     * Validates the {@link HttpClient} member variable. Handles re-starting the client in case it is required.
     *
     * @throws CloudrainAPIException the exception in case of an error
     */
    private void validateHttpClient() throws CloudrainAPIException {
        if (!httpClient.isStarted()) {
            try {
                httpClient.start();
            } catch (Exception e) {
                logger.warn(API_ERROR_HTTP_CLIENT_START, e.getMessage());
                throw new CloudrainAPIException(e);
            }
        }
    }
}
