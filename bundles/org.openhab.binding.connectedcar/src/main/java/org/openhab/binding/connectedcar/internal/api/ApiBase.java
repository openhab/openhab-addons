/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.GeoPosition;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.JwtToken;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.OAuthToken;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.binding.connectedcar.internal.config.ThingConfiguration;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.openhab.core.library.types.PointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ApiBase} implements some functions across all brand apis
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiBase extends ApiRequestQueue implements ApiBrandInterface, BrandAuthenticator {
    private static final String SUCCESSFUL = API_REQUEST_SUCCESSFUL;
    private static final String UNSUPPORTED = API_REQUEST_UNSUPPORTED;

    private final Logger logger = LoggerFactory.getLogger(ApiBase.class);
    protected final Gson gson = new Gson();

    protected @Nullable ThingHandlerInterface handler;
    protected String thingId = "";
    protected CombinedConfig config = new CombinedConfig();
    protected ApiHttpClient http = new ApiHttpClient();
    protected IdentityManager tokenManager = new IdentityManager();
    protected @Nullable ApiEventListener eventListener;

    protected boolean initialzed = false;

    public ApiBase() {
    }

    public ApiBase(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        this.handler = handler;
        this.config = handler.getCombinedConfig();
        this.http = httpClient;
        this.tokenManager = tokenManager;
        this.eventListener = eventListener;
    }

    @Override
    public void initialize(CombinedConfig configIn) throws ApiException {
        setConfig(configIn); // derive from account config
        thingId = config.account.brand;
        initialzed = tokenManager.refreshTokens(config);
    }

    /**
     * VIN-based initialization. Initialized the API itself then does the
     * VIN-related initialization
     *
     * @param vin Vehicle ID (VIN)
     * @param configIn Combined config, which gets updated and will be returned
     * @return Updated config
     * @throws ApiException
     */
    @Override
    public CombinedConfig initialize(String vin, CombinedConfig configIn) throws ApiException {
        initialize(configIn);
        getImageUrls();

        return config;
    }

    @Override
    public boolean isInitialized() {
        return initialzed;
    }

    @Override
    public void setConfig(CombinedConfig config) {
        // config.api = getProperties();
        this.config = config;
        http.setConfig(this.config);
        thingId = config.vehicle.vin;
        setupRequestQueue(thingId, eventListener);
    }

    @Override
    public String getApiUrl() throws ApiException {
        return "";
    }

    public String getServiceIdEx(String serviceId) {
        return serviceId;
    }

    @Override
    public boolean refreshTokens() throws ApiException {
        return tokenManager.refreshTokens(config);
    }

    @Override
    public boolean isAccessTokenValid() throws ApiException {
        return tokenManager.isAccessTokenValid(config);
    }

    protected <T> T callApi(String uri, String function, Class<T> classOfT) throws ApiException {
        return callApi("", uri, fillAppHeaders(), function, classOfT);
    }

    protected <T> T callApi(String vin, String uri, Map<String, String> headers, String function, Class<T> classOfT)
            throws ApiException {
        String json = "";
        try {
            ApiResult res = http.get(uri, vin, headers);
            json = res.response;
            if (res.isRedirect()) {
                // Handle redirect
                String newLocation = res.getLocation();
                logger.debug("{}: Handle HTTP Redirect -> {}", config.vehicle.vin, newLocation);
                json = http.get(newLocation, vin, fillAppHeaders()).response;
            }

            if (classOfT.isInstance(json)) {
                // special case on target class == String (return raw info)
                return wrap(classOfT).cast(json);
            }
            try {
                return fromJson(gson, json, classOfT);
            } catch (ApiException e) {
                throw new ApiException("Error parsing JSON", res, e);
            }
        } catch (ApiException e) {
            ApiResult res = e.getApiResult();
            if (e.isSecurityException() || res.isHttpUnauthorized()) {
                json = loadJson(function);
            }

            if ((json == null) || json.isEmpty()) {
                logger.debug("{}: API call {} failed: {}", config.vehicle.vin, function, e.toString());
                throw e;
            }

            if (classOfT.isInstance(json)) {
                // special case on target class == String (return raw info)
                return wrap(classOfT).cast(json);
            }
            try {
                return fromJson(gson, json, classOfT);
            } catch (ApiException e2) {
                throw new ApiException("Error parsing JSON", res, e);
            }
        } catch (RuntimeException e) {
            logger.debug("{}: API call {} failed", config.vehicle.vin, function, e);
            throw new ApiException("API call failes: RuntimeException", e);
        }
    }

    protected JwtToken decodeJwt(String token) throws ApiException {
        Base64.Decoder decoder = Base64.getDecoder();
        String[] chunks = token.split("\\.");
        // Header is chunks[0], payload chunks[1]
        String payload = new String(decoder.decode(chunks[1]));
        return fromJson(gson, payload, JwtToken.class);
    }

    protected @Nullable String loadJson(String filename) {
        if (filename.isEmpty()) {
            return null;
        }
        try {
            StringBuffer result = new StringBuffer();
            String path = System.getProperty("user.dir") + "/userdata/";
            File myObj = new File(path + filename + ".json");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                result.append(line);
            }
            myReader.close();
            return result.toString();
        } catch (IOException e) {
        }
        return null;
    }

    protected Map<String, String> fillAppHeaders(String token) throws ApiException {
        return http.fillAppHeaders(new HashMap<>(), token);
    }

    protected Map<String, String> fillAppHeaders() throws ApiException {
        return fillAppHeaders(createAccessToken());
    }

    protected String createAccessToken() throws ApiException {
        logger.trace("{}: createAccessToken for {}/{}", config.getLogId(), config.tokenSetId, config.api.clientId);
        return tokenManager.createAccessToken(config);
    }

    protected String createAccessToken2() throws ApiException {
        CombinedConfig cfg = config;
        CombinedConfig previousConfig = cfg.previousConfig;
        if (previousConfig == null) {
            throw new ApiException("No previous config found");
        }
        logger.trace("{}: createAccessToken2 setConfig from {}/{} to {}/{}", previousConfig.vehicle.vin,
                config.tokenSetId, config.api.clientId, previousConfig.tokenSetId, previousConfig.api.clientId);
        setConfig(previousConfig);
        http.setConfig(previousConfig);
        ApiBase auth = (ApiBase) previousConfig.authenticator;
        if (auth == null) {
            throw new ApiException("No previous authenticator found");
        }
        auth.setConfig(previousConfig);
        logger.trace("{}: createAccessToken2 ", config.getLogId());
        logger.trace("{}: createAccessToken2 for {}/{}", previousConfig.vehicle.vin, previousConfig.tokenSetId,
                previousConfig.api.clientId);
        String token = tokenManager.createAccessToken(previousConfig);
        auth.setConfig(cfg);
        setConfig(cfg);
        http.setConfig(cfg);
        return token;
    }

    protected String createIdToken() throws ApiException {
        return tokenManager.createIdToken(config);
    }

    protected String createSecurityToken(String service, String action) throws ApiException {
        return tokenManager.createSecurityToken(config, service, action);
    }

    protected String getWcAccessToken() throws ApiException {
        return tokenManager.getWcAccessToken(config);
    }

    public void setConfig(ThingConfiguration config) {
        this.config.vehicle = config;
        http.setConfig(this.config);
    }

    public ApiHttpClient getHttp() {
        return this.http;
    }

    @Override
    public String getLoginUrl(IdentityOAuthFlow oauth) throws ApiException {
        return "";
    }

    @Override
    public ApiIdentity login(String loginUrl, IdentityOAuthFlow oauth) throws ApiException {
        throw new ApiException("BrandAuthenticator is missing");
    }

    @Override
    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException {
        throw new ApiException("BrandAuthenticator is missing");
    }

    @Override
    public OAuthToken refreshToken(ApiIdentity token) throws ApiException {
        BrandAuthenticator a = config.authenticator;
        if (a != null) {
            return a.refreshToken(token);
        }
        throw new ApiException("BrandAuthenticator.refreshToken() is missing");
    }

    @Override
    public ApiBrandProperties getProperties() throws ApiException {
        return new ApiBrandProperties();
    }

    @Override
    public @Nullable ApiBrandProperties getProperties2() {
        return null;
    }

    public boolean isRemoteServiceAvailable(String serviceId) {
        return true;
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        return new ArrayList<>();
    }

    @Override
    public VehicleDetails getVehicleDetails(String vin) throws ApiException {
        return new VehicleDetails();
    }

    public VehicleStatus getVehicleStatus() throws ApiException {
        return new VehicleStatus();
    }

    @Override
    public String refreshVehicleStatus() throws ApiException {
        return SUCCESSFUL;
    }

    @Override
    public String controlLock(boolean lock) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlEngine(boolean start) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlClimater(boolean start, String heaterSource) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlClimaterTemp(double tempC, String heaterSource) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlPreHeating(boolean start, int duration) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlVentilation(boolean start, int duration) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlHonkFlash(boolean honk, PointType position, int duration) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlWindowHeating(boolean start) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlCharger(boolean start) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlMaxCharge(int maxCurrent) throws ApiException {
        return UNSUPPORTED;
    }

    @Override
    public String controlTargetChgLevel(int targetLevel) throws ApiException {
        return UNSUPPORTED;
    }

    public String getHomeReguionUrl() {
        return "";
    }

    public String[] getImageUrls() throws ApiException {
        // Default: No image URLs (will be overwritten by brand specific API)
        return config.vstatus.imageUrls;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws ApiException {
        return url; // default: no modification
    }

    @Override
    public IdentityOAuthFlow updateSigninParameters(IdentityOAuthFlow oauth) throws ApiException {
        return oauth; // default: no modification
    }

    public String getVehicleRequets() throws ApiException {
        return "";
    }

    public GeoPosition getVehiclePosition() throws ApiException {
        return new GeoPosition();
    }

    public GeoPosition getStoredPosition() throws ApiException {
        return new GeoPosition();
    }

    public String getProperty(String property) {
        ThingHandlerInterface h = handler;
        if (h != null) {
            return h.getProperty(property);
        }
        throw new IllegalArgumentException("Handler not initialized!");
    }

    public void fillProperty(String property, String value) {
        ThingHandlerInterface h = handler;
        if (h != null) {
            h.fillProperty(property, value);
        } else {
            throw new IllegalArgumentException("Handler not initialized!");
        }
    }
}
