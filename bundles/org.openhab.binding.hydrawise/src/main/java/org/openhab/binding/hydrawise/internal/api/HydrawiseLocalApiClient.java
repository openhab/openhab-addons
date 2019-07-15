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
package org.openhab.binding.hydrawise.internal.api;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.hydrawise.internal.api.model.LocalScheduleResponse;
import org.openhab.binding.hydrawise.internal.api.model.SetZoneResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class HydrawiseLocalApiClient {
    private final Logger logger = LoggerFactory.getLogger(HydrawiseLocalApiClient.class);

    /**
     * tcpdump -i any host 192.168.90.85 -s 65535 -w /tmp/out.pcap
     * local commands
     * /set_manual_data.php?period_id=998&action=suspend&relay_id=544997&relay=3&custom=1525417199&_=1525410369032
     * HTTP/1.1
     * http://192.168.90.85/get_sched_json.php?hours=720&cache=1&_=1525410369027
     *
     * 1525417199 looks like an epoch time
     */
    private static final String GET_LOCAL_DATA_URL = "%s/get_sched_json.php?hours=720";
    private static final String SET_LOCAL_DATA_URL = "%s/set_manual_data.php?period_id=998";

    private static final int TIMEOUT = 30;
    private HttpClient httpClient;
    private Gson gson = new Gson();
    private String localSetURL;
    private String localGetURL;

    /**
     * Initializes a Client to talk with the network local Hydrawise API
     *
     * @param host
     * @param username
     * @param password
     */
    public HydrawiseLocalApiClient(String host, String username, String password) {
        String url = "http://" + host;
        localSetURL = String.format(SET_LOCAL_DATA_URL, url);
        localGetURL = String.format(GET_LOCAL_DATA_URL, url);
        httpClient = new HttpClient();
        AuthenticationStore auth = httpClient.getAuthenticationStore();
        URI uri = URI.create(url);
        auth.addAuthenticationResult(new BasicAuthentication.BasicResult(uri, username, password));
    }

    public void stopClient() {
        if (httpClient != null && httpClient.isStarted()) {
            try {
                httpClient.getAuthenticationStore().clearAuthentications();
                httpClient.stop();
            } catch (Exception e) {
                logger.error("Could not stop http client", e);
            }
        }
    }

    /**
     * Retrieves the {@link LocalScheduleResponse} for the controller
     *
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     */
    public LocalScheduleResponse getLocalSchedule()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        String json = doGet(localGetURL);
        LocalScheduleResponse response = gson.fromJson(json, LocalScheduleResponse.class);
        return response;
    }

    /**
     *
     * @param number
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String stopRelay(int number)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("stop").relayNumber(number).toString());
    }

    /**
     *
     * @param number
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runRelay(int number)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("run").relayNumber(number).toString());
    }

    /**
     *
     * @param seconds
     * @param number
     * @return
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
     *
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String stopAllRelays()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("stopall").toString());
    }

    /**
     *
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runAllRelays()
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("runall").toString());
    }

    /**
     *
     * @param seconds
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    public String runAllRelays(int seconds)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        return relayCommand(new HydrawiseZoneCommandBuilder(localSetURL).action("runall").duration(seconds).toString());
    }

    /**
     *
     * @param url
     * @return
     * @throws HydrawiseConnectionException
     * @throws HydrawiseAuthenticationException
     * @throws HydrawiseCommandException
     */
    private String relayCommand(String url)
            throws HydrawiseConnectionException, HydrawiseAuthenticationException, HydrawiseCommandException {
        String json = doGet(url);
        SetZoneResponse response = gson.fromJson(json, SetZoneResponse.class);
        if (response.getMessageType().equals("error")) {
            throw new HydrawiseCommandException(response.getMessage());
        }
        return response.getMessage();
    }

    private String doGet(String url) throws HydrawiseConnectionException, HydrawiseAuthenticationException {
        logger.debug("Getting {}", url);
        ContentResponse response;
        try {
            if (!httpClient.isStarted()) {
                httpClient.start();
            }
            response = httpClient.newRequest(url).method(HttpMethod.GET).timeout(TIMEOUT, TimeUnit.SECONDS).send();
        } catch (Exception e) {
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
