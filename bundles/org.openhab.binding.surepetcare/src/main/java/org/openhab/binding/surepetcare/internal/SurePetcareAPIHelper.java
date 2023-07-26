/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDevice;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceControl;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceCurfewList;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareDeviceStatus;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareHousehold;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareLoginCredentials;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareLoginResponse;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePet;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePetStatus;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareTag;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareTopology;
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

    private final Logger logger = LoggerFactory.getLogger(SurePetcareAPIHelper.class);

    private static final String API_URL = "https://app.api.surehub.io/api";
    private static final String TOPOLOGY_URL = API_URL + "/me/start";
    private static final String PET_BASE_URL = API_URL + "/pet";
    private static final String PET_STATUS_URL = API_URL + "/pet/?with[]=status&with[]=photo";
    private static final String DEVICE_BASE_URL = API_URL + "/device";
    private static final String LOGIN_URL = API_URL + "/auth/login";

    public static final int DEFAULT_DEVICE_ID = 12344711;

    private String authenticationToken = "";
    private String username = "";
    private String password = "";

    private final String userAgent;

    private @NonNullByDefault({}) HttpClient httpClient;
    private SurePetcareTopology topologyCache = new SurePetcareTopology();

    public SurePetcareAPIHelper() {
        userAgent = "openHAB/" + org.openhab.core.OpenHAB.getVersion();
    }

    /**
     * Sets the httpClient object to be used for API calls to Sure Petcare.
     *
     * @param httpClient the client to be used.
     */
    public void setHttpClient(@Nullable HttpClient httpClient) {
        this.httpClient = httpClient;
    }

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
            Request request = httpClient.POST(LOGIN_URL);
            setConnectionHeaders(request);
            request.content(new StringContentProvider(SurePetcareConstants.GSON
                    .toJson(new SurePetcareLoginCredentials(username, password, getDeviceId().toString()))));
            ContentResponse response = request.send();
            if (response.getStatus() == HttpURLConnection.HTTP_OK) {
                SurePetcareLoginResponse loginResponse = SurePetcareConstants.GSON
                        .fromJson(response.getContentAsString(), SurePetcareLoginResponse.class);
                if (loginResponse != null) {
                    authenticationToken = loginResponse.getToken();
                    this.username = username;
                    this.password = password;
                    logger.debug("Login successful");
                } else {
                    throw new AuthenticationException("Invalid JSON response from login");
                }
            } else {
                logger.debug("HTTP Response Code: {}", response.getStatus());
                logger.debug("HTTP Response Msg: {}", response.getReason());
                throw new AuthenticationException(
                        "HTTP response " + response.getStatus() + " - " + response.getReason());
            }
        } catch (IOException | InterruptedException | TimeoutException | ExecutionException e) {
            throw new AuthenticationException(e);
        }
    }

    /**
     * Refreshes the whole topology, i.e. all devices, pets etc. through a call to the Sure Petcare API. The APi call is
     * quite resource intensive and should be used very infrequently.
     */
    public synchronized void updateTopologyCache() {
        try {
            SurePetcareTopology tc = SurePetcareConstants.GSON.fromJson(getDataFromApi(TOPOLOGY_URL),
                    SurePetcareTopology.class);
            if (tc != null) {
                topologyCache = tc;
            }
        } catch (JsonSyntaxException | SurePetcareApiException e) {
            logger.warn("Exception caught during topology cache update", e);
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
            topologyCache.pets = Arrays
                    .asList(SurePetcareConstants.GSON.fromJson(getDataFromApi(url), SurePetcarePet[].class));
        } catch (JsonSyntaxException | SurePetcareApiException e) {
            logger.warn("Exception caught during pet status update", e);
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
        return topologyCache.getById(topologyCache.households, id);
    }

    /**
     * Returns a device object if one exists with the given id, otherwise null.
     *
     * @param id the device id
     * @return the device with the given id
     */
    public final @Nullable SurePetcareDevice getDevice(String id) {
        return topologyCache.getById(topologyCache.devices, id);
    }

    /**
     * Returns a pet object if one exists with the given id, otherwise null.
     *
     * @param id the pet id
     * @return the pet with the given id
     */
    public final @Nullable SurePetcarePet getPet(String id) {
        return topologyCache.getById(topologyCache.pets, id);
    }

    /**
     * Returns a tag object if one exists with the given id, otherwise null.
     *
     * @param id the tag id
     * @return the tag with the given id
     */
    public final @Nullable SurePetcareTag getTag(String id) {
        return topologyCache.getById(topologyCache.tags, id);
    }

    /**
     * Returns the status object if a pet exists with the given id, otherwise null.
     *
     * @param id the pet id
     * @return the status of the pet with the given id
     */
    public final @Nullable SurePetcarePetStatus getPetStatus(String id) {
        SurePetcarePet pet = topologyCache.getById(topologyCache.pets, id);
        return pet == null ? null : pet.status;
    }

    /**
     * Updates the pet location through an API call to the Sure Petcare API.
     *
     * @param pet the pet
     * @param newLocationId the id of the new location
     * @throws SurePetcareApiException
     */
    public synchronized void setPetLocation(SurePetcarePet pet, Integer newLocationId, ZonedDateTime newSince)
            throws SurePetcareApiException {
        pet.status.activity.where = newLocationId;
        pet.status.activity.since = newSince;
        String url = PET_BASE_URL + "/" + pet.id.toString() + "/position";
        setDataThroughApi(url, HttpMethod.POST, pet.status.activity);
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
        control.lockingModeId = newLockingModeId;
        String ctrlurl = DEVICE_BASE_URL + "/" + device.id.toString() + "/control";
        setDataThroughApi(ctrlurl, HttpMethod.PUT, control);

        // now we're fetching the new state back for the cache
        String devurl = DEVICE_BASE_URL + "/" + device.id.toString() + "/status";
        SurePetcareDeviceStatus newStatus = SurePetcareConstants.GSON.fromJson(getDataFromApi(devurl),
                SurePetcareDeviceStatus.class);
        device.status.assign(newStatus);
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
        control.ledModeId = newLedModeId;
        String ctrlurl = DEVICE_BASE_URL + "/" + device.id.toString() + "/control";
        setDataThroughApi(ctrlurl, HttpMethod.PUT, control);

        // now we're fetching the new state back for the cache
        String devurl = DEVICE_BASE_URL + "/" + device.id.toString() + "/status";
        SurePetcareDeviceStatus newStatus = SurePetcareConstants.GSON.fromJson(getDataFromApi(devurl),
                SurePetcareDeviceStatus.class);
        device.status.assign(newStatus);
    }

    /**
     * Updates all curfews through an API call to the Sure Petcare API.
     *
     * @param device the device
     * @param curfewList the list of curfews
     * @throws SurePetcareApiException
     */
    public synchronized void setCurfews(SurePetcareDevice device, SurePetcareDeviceCurfewList curfewList)
            throws SurePetcareApiException {
        // post new JSON control structure to API
        SurePetcareDeviceControl control = new SurePetcareDeviceControl();
        control.curfewList = curfewList.compact();
        String ctrlurl = DEVICE_BASE_URL + "/" + device.id.toString() + "/control";
        setDataThroughApi(ctrlurl, HttpMethod.PUT, control);

        // now we're fetching the new state back for the cache
        String devurl = DEVICE_BASE_URL + "/" + device.id.toString() + "/control";
        SurePetcareDeviceControl newControl = SurePetcareConstants.GSON.fromJson(getDataFromApi(devurl),
                SurePetcareDeviceControl.class);
        if (newControl != null) {
            newControl.curfewList = newControl.curfewList.order();
        }
        device.control = newControl;
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
            logger.debug("Socket Exception", e);
        }
        return decimal;
    }

    /**
     * Sets a set of required HTTP headers for the JSON API calls.
     *
     * @param request the HTTP connection
     * @throws ProtocolException
     */
    private void setConnectionHeaders(Request request) throws ProtocolException {
        // headers
        request.header(HttpHeader.ACCEPT, "application/json, text/plain, */*");
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate");
        request.header(HttpHeader.AUTHORIZATION, "Bearer " + authenticationToken);
        request.header(HttpHeader.CONNECTION, "keep-alive");
        request.header(HttpHeader.CONTENT_TYPE, "application/json; utf-8");
        request.header(HttpHeader.USER_AGENT, userAgent);
        request.header(HttpHeader.REFERER, "https://surepetcare.io/");
        request.header("Origin", "https://surepetcare.io");
        request.header("Referer", "https://surepetcare.io");
        request.header("X-Requested-With", "com.sureflap.surepetcare");
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
        JsonObject object = (JsonObject) JsonParser.parseString(apiResult);
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
    private void setDataThroughApi(String url, HttpMethod method, Object payload) throws SurePetcareApiException {
        String jsonPayload = SurePetcareConstants.GSON.toJson(payload);
        postDataThroughAPI(url, method, jsonPayload);
    }

    /**
     * Returns the result of a GET API call as a string.
     *
     * @param url the URL
     * @return a JSON string with the API result
     * @throws SurePetcareApiException
     */
    private String getResultFromApi(String url) throws SurePetcareApiException {
        Request request = httpClient.newRequest(url).method(HttpMethod.GET);
        ContentResponse response = executeAPICall(request);
        String responseData = response.getContentAsString();
        logger.debug("API execution successful, response: {}", responseData);
        return responseData;
    }

    /**
     * Uses the given request method to send a JSON string to an API.
     *
     * @param url the URL
     * @param method the required request method (POST, PUT etc.)
     * @param jsonPayload the JSON string
     * @throws SurePetcareApiException
     */
    private void postDataThroughAPI(String url, HttpMethod method, String jsonPayload) throws SurePetcareApiException {
        logger.debug("postDataThroughAPI URL: {}", url);
        logger.debug("postDataThroughAPI Payload: {}", jsonPayload);
        Request request = httpClient.newRequest(url).method(method);
        request.content(new StringContentProvider(jsonPayload));
        executeAPICall(request);
    }

    /**
     * Uses the given request execute the API call. If it receives an HTTP_UNAUTHORIZED response, it will automatically
     * login again and retry.
     *
     * @param request the Request
     * @return the response from the API
     * @throws SurePetcareApiException
     */
    private ContentResponse executeAPICall(Request request) throws SurePetcareApiException {
        int retries = 3;
        while (retries > 0) {
            try {
                setConnectionHeaders(request);
                ContentResponse response = request.send();
                if ((response.getStatus() == HttpURLConnection.HTTP_OK)
                        || (response.getStatus() == HttpURLConnection.HTTP_CREATED)) {
                    return response;
                } else {
                    logger.debug("HTTP Response Code: {}", response.getStatus());
                    logger.debug("HTTP Response Msg: {}", response.getReason());
                    if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        // authentication token has expired, login again and retry
                        login(username, password);
                        retries--;
                    } else {
                        throw new SurePetcareApiException(
                                "Http error: " + response.getStatus() + " - " + response.getReason());
                    }
                }
            } catch (AuthenticationException | InterruptedException | ExecutionException | TimeoutException
                    | ProtocolException e) {
                throw new SurePetcareApiException("Exception caught during API execution.", e);
            }
        }
        throw new SurePetcareApiException("Can't execute API after 3 retries");
    }
}
