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
package org.openhab.binding.hydrawise.internal.api.local;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.hydrawise.internal.api.HydrawiseAuthenticationException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseCommandException;
import org.openhab.binding.hydrawise.internal.api.HydrawiseConnectionException;
import org.openhab.binding.hydrawise.internal.api.local.dto.LocalScheduleResponse;
import org.openhab.binding.hydrawise.internal.api.local.dto.SetZoneResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link HydrawiseLocalApiClient} communicates with a network local Hydrawise controller.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HydrawiseLocalApiClient {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseLocalApiClient.class);

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static final String GET_LOCAL_DATA_URL = "%s/get_sched_json.php?hours=720";
    private static final String SET_LOCAL_DATA_URL = "%s/set_manual_data.php?period_id=998";

    private static final int TIMEOUT_SECONDS = 30;
    private HttpClient httpClient;
    private String localSetURL = "";
    private String localGetURL = "";

    public HydrawiseLocalApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Initializes the {@link HydrawiseLocalApiClient} to talk with the network local Hydrawise API
     *
     * @param host
     * @param username
     * @param password
     */
    public HydrawiseLocalApiClient(String host, String username, String password, HttpClient httpClient) {
        this.httpClient = httpClient;
        setCredentials(host, username, password);
    }

    /**
     * Sets the local credentials and controller host
     *
     * @param host
     * @param username
     * @param password
     */
    public void setCredentials(String host, String username, String password) {
        String url = "http://" + host;
        localSetURL = String.format(SET_LOCAL_DATA_URL, url);
        localGetURL = String.format(GET_LOCAL_DATA_URL, url);
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(url);
        auth.addAuthenticationResult(new BasicAuthentication.BasicResult(uri, username, password));
    }

    /**
     * Retrieves the {@link LocalScheduleResponse} for the controller
     *
     * @return the local schedule response
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     */
    @Nullable
    public LocalScheduleResponse getLocalSchedule()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        String json = doGet(localGetURL);
        LocalScheduleResponse response = gson.fromJson(json, LocalScheduleResponse.class);
        return response;
    }

    /**
     * Stops a given relay
     *
     * @param number
     * @return Response message
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String stopRelay(int number)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("stop").relayNumber(number).toString());
    }

    /**
     * Runs a given relay for the default amount of time
     *
     * @param number
     * @return Response message
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runRelay(int number)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("run").relayNumber(number).toString());
    }

    /**
     * Runs a given relay for a specified numbers of seconds
     *
     * @param seconds
     * @param number
     * @return Response message
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runRelay(int seconds, int number)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("run").relayNumber(number)
                .duration(seconds).toString());
    }

    /**
     * Stops all relays
     *
     * @return Response message
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String stopAllRelays()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("stopall").toString());
    }

    /**
     * Run all relays for the default amount of time
     *
     * @return Response message
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runAllRelays()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("runall").toString());
    }

    /**
     * Run all relays for a given amount of seconds
     *
     * @param seconds
     * @return Response message
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runAllRelays(int seconds)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("runall").duration(seconds).toString());
    }

    private String relayCommand(String url)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        String json = doGet(url);
        SetZoneResponse response = gson.fromJson(json, SetZoneResponse.class);
        if (response.messageType.equals("error")) {
            throw new HydrawiseCommandException(response.message);
        }
        return response.message;
    }

    private String doGet(String url) throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        logger.trace("Getting {}", url);
        ContentResponse response;
        try {
            response = httpClient.newRequest(url).method(HttpMethod.GET).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new HydrawiseConnectionException(e);
        }
        if (response.getStatus() == 401) {
            throw new HydrawiseAuthenticationException();
        }
        if (response.getStatus() != 200) {
            throw new HydrawiseConnectionException("Error from controller.  Response code " + response.getStatus());
        }
        String stringResponse = response.getContentAsString();
        logger.trace("Response: {}", stringResponse);
        return stringResponse;
    }
}
