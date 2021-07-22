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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.CarPosition;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.JwtToken;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleDetails;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.VehicleStatus;
import org.openhab.binding.connectedcar.internal.api.brand.BrandApiProperties;
import org.openhab.binding.connectedcar.internal.api.brand.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetPendingRequest;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.binding.connectedcar.internal.config.VehicleConfiguration;
import org.openhab.core.library.types.PointType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ApiBase} implements some functions across all brand apis
 *
 * @author Markus Michels - Initial contribution
 *
 */
@NonNullByDefault
public class ApiBase implements BrandAuthenticator, ApiBrandInterface {
    private static final String SUCCESSFUL = API_REQUEST_SUCCESSFUL;
    private static final String UNSUPPORTED = API_REQUEST_UNSUPPORTED;

    private final Logger logger = LoggerFactory.getLogger(ApiBase.class);

    protected final Gson gson = new Gson();

    protected String thingId = "";
    protected CombinedConfig config = new CombinedConfig();
    protected ApiHttpClient http = new ApiHttpClient();
    protected TokenManager tokenManager = new TokenManager();
    protected @Nullable ApiEventListener eventListener;
    protected Map<String, CarNetPendingRequest> pendingRequests = new ConcurrentHashMap<>();

    protected boolean initialzed = false;

    public ApiBase() {
    }

    public ApiBase(ApiHttpClient httpClient, TokenManager tokenManager, @Nullable ApiEventListener eventListener) {
        this.http = httpClient;
        this.tokenManager = tokenManager;
        this.eventListener = eventListener;
    }

    /**
     * Simple initialization, in fact used by Account Handler
     *
     * @param configIn
     * @throws ApiException
     */
    @Override
    public void initialize(CombinedConfig configIn) throws ApiException {
        config = configIn;
        setConfig(config); // derive from account config
        tokenManager.refreshTokens(config);
        initialzed = true;
        thingId = config.account.brand;
    }

    @Override
    public void setConfig(CombinedConfig config) {
        // config.api = getProperties();
        this.config = config;
        http.setConfig(this.config);
    }

    @Override
    public String getApiUrl() throws ApiException {
        return "";
    }

    public String getServiceIdEx(String serviceId) {
        return serviceId;
    }

    public boolean isRemoteServiceAvailable(String serviceId) {
        return true;
    }

    /**
     * VIN-based initialization. Initialized the API itself then does the VIN-related initialization
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
    public BrandApiProperties getProperties() {
        return new BrandApiProperties();
    }

    @Override
    public @Nullable BrandApiProperties getProperties2() {
        return null;
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
    public void checkPendingRequests() {
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

    public String getVehicleRequets() throws ApiException {
        return "";
    }

    public CarPosition getVehiclePosition() throws ApiException {
        return new CarPosition();
    }

    public CarPosition getStoredPosition() throws ApiException {
        return new CarPosition();
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
        } catch (ApiException e) {
            ApiResult res = e.getApiResult();
            if (e.isSecurityException() || res.isHttpUnauthorized()) {
                json = loadJson(function);
            } else if (e.getApiResult().isRedirect()) {
                // Handle redirect
                String newLocation = res.getLocation();
                logger.debug("{}: Handle HTTP Redirect -> {}", config.vehicle.vin, newLocation);
                json = http.get(newLocation, vin, fillAppHeaders()).response;
            }

            if ((json == null) || json.isEmpty()) {
                logger.debug("{}: API call {} failed: {}", config.vehicle.vin, function, e.toString());
                throw e;
            }
        } catch (RuntimeException e) {
            logger.debug("{}: API call {} failed", config.vehicle.vin, function, e);
            throw new ApiException("API call failes: RuntimeException", e);
        }

        if (classOfT.isInstance(json)) {
            // special case on target class == String (return raw info)
            return wrap(classOfT).cast(json);
        }
        return fromJson(gson, json, classOfT);
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
        return tokenManager.createAccessToken(config);
    }

    protected String createIdToken() throws ApiException {
        return tokenManager.createIdToken(config);
    }

    protected String createSecurityToken(String service, String action) throws ApiException {
        return tokenManager.createSecurityToken(config, service, action);
    }

    public void setConfig(VehicleConfiguration config) {
        this.config.vehicle = config;
        http.setConfig(this.config);
    }

    @Override
    public ApiHttpClient getHttp() {
        return this.http;
    }

    @Override
    public Map<String, CarNetPendingRequest> getPendingRequests() {
        return pendingRequests;
    }
}
