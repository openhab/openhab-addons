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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDeviceControl;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDeviceCurfewList;
import org.openhab.binding.surepetcare.internal.data.SurePetcareDeviceStatus;
import org.openhab.binding.surepetcare.internal.data.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.data.SurePetcareLoginCredentials;
import org.openhab.binding.surepetcare.internal.data.SurePetcareLoginResponse;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.data.SurePetcarePetStatus;
import org.openhab.binding.surepetcare.internal.data.SurePetcareTag;
import org.openhab.binding.surepetcare.internal.data.SurePetcareTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link SurePetcareAPIHelper} is a helper class to abstract the Sure Petcare API. It handles authentication and
 * all JSON API calls. If an API call fails it automatically refreshes the authentication token and retries.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareAPIHelper {

    private static final String HTTP_REQUEST_METHOD_POST = "POST";
    private static final String HTTP_REQUEST_METHOD_PUT = "PUT";
    private static final String HTTP_REQUEST_METHOD_GET = "GET";

    private final Logger logger = LoggerFactory.getLogger(SurePetcareAPIHelper.class);

    private static final String API_USER_AGENT = "Mozilla/5.0 (Linux; Android 7.0; SM-G930F Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/64.0.3282.137 Mobile Safari/537.36";

    private static final String API_URL = "https://app.api.surehub.io/api";
    private static final String TOPOLOGY_URL = API_URL + "/me/start";
    private static final String PET_BASE_URL = API_URL + "/pet";
    private static final String PET_STATUS_URL = API_URL + "/pet/?with[]=status";
    private static final String DEVICE_BASE_URL = API_URL + "/device";
    private static final String LOGIN_URL = API_URL + "/auth/login";

    public static final int DEFAULT_DEVICE_ID = 12344711;

    private String authenticationToken = "";
    private String username = "";
    private String password = "";
    private boolean online = false;

    private SurePetcareTopology topologyCache = new SurePetcareTopology();

    /**
     * This method uses the provided username and password to obtain an authentication token used for subsequent API
     * calls.
     *
     * @param username The Sure Petcare username (email address) to be used
     * @param password The password
     * @throws AuthenticationException
     */
    public synchronized void login(String username, String password) throws AuthenticationException {
        try {
            URL object = new URL(LOGIN_URL);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();
            setConnectionHeaders(con);
            con.setRequestMethod(HTTP_REQUEST_METHOD_POST);
            con.setDoInput(true);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(SurePetcareConstants.GSON
                    .toJson(new SurePetcareLoginCredentials(username, password, getDeviceId().toString())));
            wr.flush();

            StringBuilder sb = new StringBuilder();
            int httpResult = con.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                @NonNull
                SurePetcareLoginResponse response = SurePetcareConstants.GSON.fromJson(sb.toString(),
                        SurePetcareLoginResponse.class);

                authenticationToken = response.getToken();
                this.username = username;
                this.password = password;
                online = true;

                logger.debug("Login successful, token: {}", authenticationToken);
            } else {
                logger.debug("HTTP Response Code: {}", con.getResponseCode());
                logger.debug("HTTP Response Msg: {}", con.getResponseMessage());
                throw new AuthenticationException(
                        "HTTP response " + con.getResponseCode() + " - " + con.getResponseMessage());
            }
        } catch (IOException e) {
            logger.debug("Exception caught during login: {}", e.getMessage());
            throw new AuthenticationException(e);
        }
    }

    /**
     * Refreshes the whole topology, i.e. all devices, pets etc. through a call to the Sure Petcare API. The APi call is
     * quite resource intensive and should be used very infrequently.
     */
    public synchronized void updateTopologyCache() {
        try {
            topologyCache = SurePetcareConstants.GSON.fromJson(getDataFromApi(TOPOLOGY_URL), SurePetcareTopology.class);
        } catch (JsonSyntaxException | SurePetcareApiException e) {
            logger.warn("Exception caught during topology cache update: {}", e.getMessage());
        }
    }

    /**
     * Refreshes the pet information. This API call can be used more frequently.
     * Unlike for the "position" API endpoint, there is none for the "status" (activity/feeding).
     * We also dont need to specify a "petId" in the call, so we just need to call the API once.
     */
    public synchronized void updatePetStatus() {
        try {
            String url = PET_STATUS_URL;
            topologyCache.setPets(
                    Arrays.asList(SurePetcareConstants.GSON.fromJson(getDataFromApi(url), SurePetcarePet[].class)));
        } catch (JsonSyntaxException | SurePetcareApiException e) {
            logger.warn("Exception caught during pet status update: {}", e.getMessage());
        }
    }

    /**
     * Returns the whole topology.
     *
     * @return the topology
     */
    public final SurePetcareTopology getTopology() {
        return topologyCache;
    }

    /**
     * Returns a household object if one exists with the given id, otherwise null.
     *
     * @param id the household id
     * @return the household with the given id
     */
    public final @Nullable SurePetcareHousehold getHousehold(String id) {
        return topologyCache.getHouseholdById(id);
    }

    /**
     * Returns a device object if one exists with the given id, otherwise null.
     *
     * @param id the device id
     * @return the device with the given id
     */
    public final @Nullable SurePetcareDevice getDevice(String id) {
        return topologyCache.getDeviceById(id);
    }

    /**
     * Returns a pet object if one exists with the given id, otherwise null.
     *
     * @param id the pet id
     * @return the pet with the given id
     */
    public final @Nullable SurePetcarePet getPet(String id) {
        return topologyCache.getPetById(id);
    }

    /**
     * Returns a tag object if one exists with the given id, otherwise null.
     *
     * @param id the tag id
     * @return the tag with the given id
     */
    public final @Nullable SurePetcareTag getTag(String id) {
        return topologyCache.getTagById(id);
    }

    /**
     * Returns the status object if a pet exists with the given id, otherwise null.
     *
     * @param id the pet id
     * @return the status of the pet with the given id
     */
    public final @Nullable SurePetcarePetStatus getPetStatus(String id) {
        SurePetcarePet pet = topologyCache.getPetById(id);
        return pet == null ? null : pet.getPetStatus();
    }

    /**
     * Updates the pet location through an API call to the Sure Petcare API.
     *
     * @param pet the pet
     * @param newLocationId the id of the new location
     * @throws SurePetcareApiException
     */
    public synchronized void setPetLocation(SurePetcarePet pet, Integer newLocationId) throws SurePetcareApiException {
        pet.getPetStatus().getActivity().setWhere(newLocationId);
        pet.getPetStatus().getActivity().setSince(new Date());
        String url = PET_BASE_URL + "/" + pet.getId().toString() + "/position";
        setDataThroughApi(url, HTTP_REQUEST_METHOD_POST, pet.getPetStatus().getActivity());
    }

    /**
     * Updates the device locking mode through an API call to the Sure Petcare API.
     *
     * @param device the device
     * @param newLockingModeId the id of the new locking mode
     * @throws SurePetcareApiException
     */
    public synchronized void setDeviceLockingMode(SurePetcareDevice device, Integer newLockingModeId)
            throws SurePetcareApiException {
        // post new JSON control structure to API
        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        control.setLockingModeId(newLockingModeId);
        String ctrlurl = DEVICE_BASE_URL + "/" + device.getId().toString() + "/control";
        setDataThroughApi(ctrlurl, HTTP_REQUEST_METHOD_PUT, control);

        // now we're fetching the new state back for the cache
        String devurl = DEVICE_BASE_URL + "/" + device.getId().toString() + "/status";
        SurePetcareDeviceStatus newStatus = SurePetcareConstants.GSON.fromJson(getDataFromApi(devurl),
                SurePetcareDeviceStatus.class);
        device.getStatus().assign(newStatus);
    }

    /**
     * Updates the device led mode through an API call to the Sure Petcare API.
     *
     * @param device the device
     * @param newLedModeId the id of the new led mode
     * @throws SurePetcareApiException
     */
    public synchronized void setDeviceLedMode(SurePetcareDevice device, Integer newLedModeId)
            throws SurePetcareApiException {
        // post new JSON control structure to API
        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        control.setLedModeId(newLedModeId);
        String ctrlurl = DEVICE_BASE_URL + "/" + device.getId().toString() + "/control";
        setDataThroughApi(ctrlurl, HTTP_REQUEST_METHOD_PUT, control);

        // now we're fetching the new state back for the cache
        String devurl = DEVICE_BASE_URL + "/" + device.getId().toString() + "/status";
        SurePetcareDeviceStatus newStatus = SurePetcareConstants.GSON.fromJson(getDataFromApi(devurl),
                SurePetcareDeviceStatus.class);
        device.getStatus().assign(newStatus);
    }

    /**
     * Updates all curfews through an API call to the Sure Petcare API.
     *
     * @param device the device
     * @param curfewList the list of curfews
     * @throws SurePetcareApiException
     */
    public void setCurfews(SurePetcareDevice device, SurePetcareDeviceCurfewList curfewList)
            throws SurePetcareApiException {
        // post new JSON control structure to API
        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        control.setCurfewList(curfewList.compact());
        String ctrlurl = DEVICE_BASE_URL + "/" + device.getId().toString() + "/control";
        setDataThroughApi(ctrlurl, HTTP_REQUEST_METHOD_PUT, control);

        // now we're fetching the new state back for the cache
        String devurl = DEVICE_BASE_URL + "/" + device.getId().toString() + "/control";
        SurePetcareDeviceControl newControl = SurePetcareConstants.GSON.fromJson(getDataFromApi(devurl),
                SurePetcareDeviceControl.class);
        newControl.setCurfewList(newControl.getCurfewList().order());
        device.setControl(newControl);
    }

    /**
     * @return true, if the API is connected and successfully authenticated.
     */
    public final boolean isOnline() {
        return online;
    }

    /**
     * Returns a unique device id used during the authentication process with the Sure Petcare API. The id is derived
     * from the local MAC address or hostname.
     *
     * @return a unique device id
     */
    public final Integer getDeviceId() {
        try {
            return getDeviceId(NetworkInterface.getNetworkInterfaces(), InetAddress.getLocalHost());
        } catch (UnknownHostException | SocketException e) {
            logger.warn("unable to discover mac or hostname, assigning default device id {}", DEFAULT_DEVICE_ID);
            return DEFAULT_DEVICE_ID;
        }
    }

    /**
     * Returns a unique device id used during the authentication process with the Sure Petcare API. The id is derived
     * from the local MAC address or hostname provided as arguments
     *
     * @param interfaces a list of interface of this host
     * @param localHostAddress the ip address of the localhost
     * @return a unique device id
     */
    public final int getDeviceId(Enumeration<NetworkInterface> interfaces, InetAddress localHostAddress) {
        int decimal = DEFAULT_DEVICE_ID;
        try {
            if (interfaces.hasMoreElements()) {
                NetworkInterface netif = interfaces.nextElement();

                byte[] mac = netif.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02x", mac[i]));
                    }
                    String hex = sb.toString();
                    decimal = Math.abs((int) (Long.parseUnsignedLong(hex, 16) % Integer.MAX_VALUE));
                    logger.debug("current MAC address: {}, device id: {}", hex, decimal);
                } else {
                    String hostname = localHostAddress.getHostName();
                    decimal = hostname.hashCode();
                    logger.debug("current hostname: {}, device id: {}", hostname, decimal);
                }
            } else {
                String hostname = localHostAddress.getHostName();
                decimal = hostname.hashCode();
                logger.debug("current hostname: {}, device id: {}", hostname, decimal);
            }
        } catch (SocketException e) {
            logger.debug("Socket Exception: {}", e.getMessage());
        }
        return decimal;
    }

    /**
     * Sets a set of required HTTP headers for the JSON API calls.
     *
     * @param con the HTTP connection
     * @throws ProtocolException
     */
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

    /**
     * Sends a given object as a JSON payload to the API.
     *
     * @param url the URL
     * @param requestMethod the request method (POST, PUT etc.)
     * @param payload an object used for the payload
     * @throws SurePetcareApiException
     */
    private void setDataThroughApi(String url, String requestMethod, Object payload) throws SurePetcareApiException {
        String jsonPayload = SurePetcareConstants.GSON.toJson(payload);
        postDataThroughAPI(url, requestMethod, jsonPayload);
    }

    /**
     * Returns the result of a GET API call as a string.
     *
     * @param url the URL
     * @return a JSON string with the API result
     * @throws SurePetcareApiException
     */
    private String getResultFromApi(String url) throws SurePetcareApiException {
        boolean success = false;
        String responseData = "";
        while (!success) {
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                setConnectionHeaders(con);
                con.setRequestMethod(HTTP_REQUEST_METHOD_GET);

                StringBuilder sb = new StringBuilder();
                int httpResult = con.getResponseCode();
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
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
                    logger.debug("HTTP Response Code: {}", con.getResponseCode());
                    logger.debug("HTTP Response Msg: {}", con.getResponseMessage());
                    if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        // authentication token has expired, login again and retry
                        login(username, password);
                    } else {
                        throw new SurePetcareApiException();
                    }
                }
            } catch (IOException | AuthenticationException e) {
                logger.debug("Exception caught during API execution: {}", e.getMessage());
                throw new SurePetcareApiException(e);
            }
        }
        return responseData;
    }

    /**
     * Uses the given request method to send a JSON string to an API.
     *
     * @param url the URL
     * @param requestMethod the required request method (POST, PUT etc.)
     * @param jsonPayload the JSON string
     * @throws SurePetcareApiException
     */
    private void postDataThroughAPI(String url, String requestMethod, String jsonPayload)
            throws SurePetcareApiException {
        boolean success = false;
        logger.debug("postDataThroughAPI URL: {}", url);
        logger.debug("postDataThroughAPI Payload: {}", jsonPayload);
        while (!success) {
            try {
                URL object = new URL(url);
                HttpURLConnection con = (HttpURLConnection) object.openConnection();
                setConnectionHeaders(con);
                con.setRequestMethod(requestMethod);
                con.getOutputStream().write(jsonPayload.getBytes());
                int httpResult = con.getResponseCode();
                if ((httpResult == HttpURLConnection.HTTP_OK) || (httpResult == HttpURLConnection.HTTP_CREATED)) {
                    logger.debug("API execution successful");
                    success = true;
                } else {
                    logger.debug("HTTP Response Code: {}", con.getResponseCode());
                    logger.debug("HTTP Response Msg: {}", con.getResponseMessage());
                    if (con.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        // authentication token has expired, login again and retry
                        login(username, password);
                    } else {
                        throw new SurePetcareApiException();
                    }
                }
            } catch (IOException | AuthenticationException e) {
                logger.debug("Exception caught during API execution: {}", e.getMessage());
                throw new SurePetcareApiException(e);
            }
        }
    }

}
