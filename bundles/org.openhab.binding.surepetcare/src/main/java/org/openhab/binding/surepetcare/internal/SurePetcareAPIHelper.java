/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.surepetcare.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.data.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.data.SurePetcareLoginCredentials;
import org.openhab.binding.surepetcare.internal.data.SurePetcareLoginResponse;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetLocation;
import org.openhab.binding.surepetcare.internal.data.SurePetcareTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Helper class to retrieve Sure Petcare data from the API.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareAPIHelper {

    private final Logger logger = LoggerFactory.getLogger(SurePetcareAPIHelper.class);

    private static final String API_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930F Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36";

    private static final String TOPOLOGY_URL = "https://app.api.surehub.io/api/me/start";
    private static final String PET_BASE_URL = "https://app.api.surehub.io/api/pet";
    private static final String LOGIN_URL = "https://app.api.surehub.io/api/auth/login";

    private Gson gson = new Gson();
    private String authenticationToken = "";
    private String username = "";
    private String password = "";
    private boolean online = false;

    private SurePetcareTopology topologyCache = new SurePetcareTopology();

    public synchronized void login(String username, String password)
            throws JsonSyntaxException, AuthenticationException {
        try {
            URL object = new URL(LOGIN_URL);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            setConnectionHeaders(con);
            con.setRequestMethod("POST");
            con.setDoInput(true);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(gson.toJson(
                    new SurePetcareLoginCredentials(username, password, SurePetcareUtils.getDeviceId().toString())));
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                @NonNull
                SurePetcareLoginResponse response = gson.fromJson(sb.toString(), SurePetcareLoginResponse.class);

                authenticationToken = response.getToken();
                this.username = username;
                this.password = password;
                online = true;

                logger.debug("Login successful, token: {}", authenticationToken);
            } else {
                logger.warn("HTTP Response Code: {}", con.getResponseCode());
                logger.warn("HTTP Response Msg: {}", con.getResponseMessage());
                throw new AuthenticationException(
                        "HTTP response " + con.getResponseCode() + " - " + con.getResponseMessage());
            }
        } catch (Exception e) {
            logger.warn("Exception caught during login: {}", e.getMessage());
            throw new AuthenticationException(e);
        }
    }

    public void updateTopologyCache() {
        try {
            synchronized (topologyCache) {
                topologyCache = gson.fromJson(getDataFromApi(TOPOLOGY_URL), SurePetcareTopology.class);
            }
        } catch (JsonSyntaxException | SurePetcareApiException e) {
            logger.warn("Exception caught during topology cache update: {}", e.getMessage());
        }
    }

    public void updatePetLocations() {
        try {
            synchronized (topologyCache) {
                for (SurePetcarePet pet : topologyCache.getPets()) {
                    String url = PET_BASE_URL + "/" + pet.getId().toString() + "/position";
                    pet.setPosition(gson.fromJson(getDataFromApi(url), SurePetcarePetLocation.class));
                }
            }
        } catch (JsonSyntaxException | SurePetcareApiException e) {
            logger.warn("Exception caught during topology cache update: {}", e.getMessage());
        }
    }

    public SurePetcareTopology retrieveTopology() {
        return topologyCache;
    }

    public @Nullable SurePetcareHousehold retrieveHousehold(String id) {
        return topologyCache.getHouseholdById(id);
    }

    public @Nullable SurePetcareDevice retrieveDevice(String id) {
        return topologyCache.getDeviceById(id);
    }

    public @Nullable SurePetcarePet retrievePet(String id) {
        return topologyCache.getPetById(id);
    }

    public @Nullable SurePetcarePetLocation retrievePetLocation(String id) {
        SurePetcarePet pet = topologyCache.getPetById(id);
        if (pet != null) {
            return pet.getLocation();
        } else {
            return null;
        }
    }

    private void setConnectionHeaders(HttpURLConnection con) throws ProtocolException {
        // headers
        con.setRequestProperty("Connection", "keep-alive");
        con.setRequestProperty("Origin", "https://surepetcare.io");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("User-Agent", API_USER_AGENT);
        con.setRequestProperty("Referer", "https://surepetcare.io/");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Accept", "application/json, text/plain, */*");
        con.setRequestProperty("X-Requested-With", "com.sureflap.surepetcare");
        con.setRequestProperty("Authorization", "Bearer " + authenticationToken);
        con.setDoOutput(true);
    }

    /**
     * Return the "data" element of the API result as a JsonElement.
     *
     * @param url The URL of the API call.
     * @return The "data" element of the API result.
     * @throws SurePetcareApiException
     */
    private JsonElement getDataFromApi(String url) throws SurePetcareApiException {
        String apiResult = getResultFromApi(url);
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject) parser.parse(apiResult);
        return object.get("data");
    }

    private String getResultFromApi(String url) throws SurePetcareApiException {
        boolean success = false;
        String responseData = "";
        while (!success) {
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                setConnectionHeaders(con);
                con.setRequestMethod("GET");

                StringBuilder sb = new StringBuilder();
                int httpResult = con.getResponseCode();
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    br.close();
                    logger.debug("API execution successful");
                    responseData = sb.toString();
                    logger.debug("Response data: {}", responseData);

                    success = true;
                } else {
                    logger.warn("HTTP Response Code: {}", con.getResponseCode());
                    logger.warn("HTTP Response Msg: {}", con.getResponseMessage());
                    if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        // authentication token has expired, login again and retry
                        login(username, password);
                    } else {
                        throw new SurePetcareApiException();
                    }
                }
            } catch (Exception e) {
                logger.error("Exception caught during API execution: {}", e.getMessage());
                throw new SurePetcareApiException(e);
            }
        }
        return responseData;
    }

    public boolean isOnline() {
        return online;
    }

}
