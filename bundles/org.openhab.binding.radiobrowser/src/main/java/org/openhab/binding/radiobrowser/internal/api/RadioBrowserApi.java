/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.radiobrowser.internal.api;

import static org.openhab.binding.radiobrowser.internal.RadioBrowserBindingConstants.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.radiobrowser.internal.RadioBrowserHandler;
import org.openhab.binding.radiobrowser.internal.json.RadioBrowserJson.Country;
import org.openhab.binding.radiobrowser.internal.json.RadioBrowserJson.Language;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link RadioBrowserApi} Handles all http calls to and from the Radio Stations API.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RadioBrowserApi {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RadioBrowserHandler handler;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private String server = "";

    public RadioBrowserApi(RadioBrowserHandler handler, HttpClient httpClient) {
        this.handler = handler;
        this.httpClient = httpClient;
    }

    private String sendGetRequest(String url) throws ApiException {
        Request request;
        String errorReason = "";
        request = httpClient.newRequest("http://" + server + url);
        request.header("Host", server);
        request.header("User-Agent", "openHAB/RadioBrowserBinding");// api requirement
        request.header("Connection", "Keep-Alive");
        request.timeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        request.method(HttpMethod.GET);
        logger.debug("Sending GET:{}", url);

        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                return contentResponse.getContentAsString();
            } else {
                errorReason = String.format("GET request failed with %d: %s", contentResponse.getStatus(),
                        contentResponse.getContentAsString());
            }
        } catch (TimeoutException e) {
            errorReason = "TimeoutException: Server was not reachable on your network";
        } catch (ExecutionException e) {
            errorReason = String.format("ExecutionException: %s", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorReason = String.format("InterruptedException: %s", e.getMessage());
        }
        throw new ApiException(errorReason);
    }

    private List<String> getServers() throws ApiException {
        List<String> listResult = new ArrayList<>();
        try {
            // add all round robin servers one by one
            InetAddress[] list = InetAddress.getAllByName("all.api.radio-browser.info");
            for (InetAddress item : list) {
                listResult.add(item.getCanonicalHostName());
            }
            // Requirement of using the API is to spread the load and either let user select or random
            Random rand = new Random();
            server = listResult.get(rand.nextInt(listResult.size()));
        } catch (UnknownHostException e) {
            throw new ApiException("Unknown host");
        }
        return listResult;
    }

    private List<StateOption> getCountries() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/countries");
            Country[] countries = gson.fromJson(returnContent, Country[].class);
            if (countries == null) {
                throw new ApiException("Could not GET:/json/countries");
            }
            List<StateOption> countryOptions = new ArrayList<>();
            int counter = 0;
            for (Country country : countries) {
                countryOptions.add(
                        new StateOption(Integer.toString(counter++), country.name + " (" + country.stationcount + ")"));
            }
            countryOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            return countryOptions;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Server did not reply with a valid json");
        }
    }

    private List<StateOption> getLanguages() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/languages");
            Language[] languages = gson.fromJson(returnContent, Language[].class);
            if (languages == null) {
                throw new ApiException("Could not GET:/json/languages");
            }
            List<StateOption> languageOptions = new ArrayList<>();
            int counter = 0;
            for (Language language : languages) {
                languageOptions.add(new StateOption(Integer.toString(counter++),
                        language.name + " (" + language.stationcount + ")"));
            }
            languageOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            return languageOptions;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Server did not reply with a valid json");
        }
    }

    public void searchStations(String arguments) throws ApiException {
        sendGetRequest("/json/stations/search?" + arguments);
    }

    public void initialize() throws ApiException {
        getServers();
        logger.debug("Using server:{}", server);
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_COUNTRY),
                getCountries());
        handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_LANGUAGE),
                getLanguages());
    }
}
