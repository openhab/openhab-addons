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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserJson.Country;
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserJson.Language;
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserJson.State;
import org.openhab.binding.radiobrowser.internal.api.RadioBrowserJson.Station;
import org.openhab.core.library.types.StringType;
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
    private String language = "";
    private String countryCode = "";
    private String state = "";
    private String genre = "";
    private Map<String, Station> stationMap = new HashMap<>();
    public Map<String, Country> countryMap = new HashMap<>();

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

    private List<StateOption> getStates() throws ApiException {
        try {
            Country localCountry = countryMap.get(countryCode);
            if (localCountry == null) {
                return new ArrayList<>();
            }
            String returnContent = sendGetRequest("/json/states/"
                    + URLEncoder.encode(localCountry.name, "UTF-8").replace("+", "%20") + "/?hidebroken=true");
            State[] states = gson.fromJson(returnContent, State[].class);
            if (states == null) {
                throw new ApiException("Could not get states");
            }
            List<StateOption> stateOptions = new ArrayList<>();
            for (State state : states) {
                stateOptions.add(new StateOption(state.name, state.name));
            }
            stateOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            stateOptions.add(0, new StateOption("ALL", "Show All States"));
            return stateOptions;
        } catch (JsonSyntaxException | UnsupportedEncodingException e) {
            throw new ApiException("Server did not reply with a valid json");
        }
    }

    private List<StateOption> getCountries() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/countries?hidebroken=true");
            Country[] countries = gson.fromJson(returnContent, Country[].class);
            if (countries == null) {
                throw new ApiException("Could not get countries");
            }
            List<StateOption> countryOptions = new ArrayList<>();
            for (Country country : countries) {
                countryMap.put(country.countryCode, country);
                if (country.stationcount > 4) {
                    countryOptions.add(
                            new StateOption(country.countryCode, country.name + " (" + country.stationcount + ")"));
                }
            }
            countryOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            countryOptions.add(0, new StateOption("ALL", "Show All Countries"));
            return countryOptions;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Server did not reply with a valid json");
        }
    }

    private List<StateOption> getLanguages() throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/languages?hidebroken=true");
            Language[] languages = gson.fromJson(returnContent, Language[].class);
            if (languages == null) {
                throw new ApiException("Could not get languages");
            }
            List<StateOption> languageOptions = new ArrayList<>();
            languageOptions.add(new StateOption("ALL", "Show All Languages"));
            for (Language language : languages) {
                if (language.stationcount >= handler.config.languageCount) {
                    languageOptions
                            .add(new StateOption(language.name, language.name + " (" + language.stationcount + ")"));
                }
            }
            languageOptions.sort(Comparator.comparing(o -> "0".equals(o.getValue()) ? "" : o.getLabel()));
            return languageOptions;
        } catch (JsonSyntaxException e) {
            throw new ApiException("Server did not reply with a valid json");
        }
    }

    private void searchStations(String arguments) throws ApiException {
        stationMap.clear();
        try {
            String returnContent = sendGetRequest("/json/stations/search" + arguments);
            Station[] stations = gson.fromJson(returnContent, Station[].class);
            if (stations == null) {
                throw new ApiException("Could not get stations");
            }
            List<StateOption> stationOptions = new ArrayList<>();
            for (Station station : stations) {
                stationMap.put(station.name, station);
                stationOptions.add(new StateOption(station.name, station.name));
            }
            handler.stateDescriptionProvider
                    .setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_STATION), stationOptions);
            if (stationMap.isEmpty()) {
                handler.setChannelState(CHANNEL_STATION, new StringType(stationMap.size() + " matches found"));
            } else {
                handler.setChannelState(CHANNEL_STATION,
                        new StringType(stationMap.size() + " stations, click to select"));
            }
        } catch (JsonSyntaxException e) {
            throw new ApiException("Server did not reply with a valid json");
        } catch (IllegalArgumentException e) {
            // occurs when there are 0 matches
            handler.setChannelState(CHANNEL_STATION, new StringType(stationMap.size() + " matches found"));
        }
    }

    public void updateStations() throws ApiException {
        searchStations(updateFilter());
    }

    private Station searchStationUUID(String uuid) throws ApiException {
        try {
            String returnContent = sendGetRequest("/json/stations/" + uuid);
            Station[] stations = gson.fromJson(returnContent, Station[].class);
            if (stations == null) {
                throw new ApiException("Could not find requested station, missing from list and is not a valid UUID.");
            }
            return stations[0];
        } catch (JsonSyntaxException e) {
            throw new ApiException("Server did not reply with a valid json");
        }
    }

    public void selectStation(String name) throws ApiException {
        Station station = stationMap.get(name);
        if (station == null) {
            // missing from the MAP so its not from state options, try looking for UUID.
            station = searchStationUUID(name);
        }
        handler.setChannelState(CHANNEL_NAME, new StringType(name));
        handler.setChannelState(CHANNEL_ICON, new StringType(station.favicon));
        handler.setChannelState(CHANNEL_STREAM, new StringType(station.url));
        logger.debug("Selected stationUUID:{}", station.stationuuid);
        if (handler.config.clicks) {
            sendGetRequest("/json/url/" + station.stationuuid);
        }
    }

    private String updateFilter() {
        String filter = "?" + handler.config.filters;
        if (!language.isEmpty()) {
            filter = filter + "&language=" + language;
        }
        if (!countryCode.isEmpty()) {
            filter = filter + "&countrycode=" + countryCode;
        }
        if (!genre.isEmpty()) {
            filter = filter + "&tag=" + genre;
        }
        if (!state.isEmpty()) {
            try {
                filter = filter + "&state=" + URLEncoder.encode(state, "UTF-8").replace("+", "%20");
            } catch (UnsupportedEncodingException e) {
                logger.warn("State contains bad characters?:{}", state);
            }
        }
        return filter;
    }

    public void setLanguage(String language) throws ApiException {
        if ("ALL".equals(language)) {
            this.language = "";
        } else {
            this.language = language;
        }
        searchStations(updateFilter());
    }

    public void setCountry(String countryCode) throws ApiException {
        this.state = "";
        handler.setChannelState(CHANNEL_STATE, new StringType());
        if ("ALL".equals(countryCode)) {
            this.countryCode = "";
            handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_STATE),
                    new ArrayList<>());
        } else {
            this.countryCode = countryCode;
            handler.stateDescriptionProvider.setStateOptions(new ChannelUID(handler.getThing().getUID(), CHANNEL_STATE),
                    getStates());
        }
        searchStations(updateFilter());
    }

    public void setState(String state) throws ApiException {
        if ("ALL".equals(state)) {
            this.state = "";
        } else {
            this.state = state;
        }
        searchStations(updateFilter());
    }

    public void setGenre(String genre) throws ApiException {
        if ("ALL".equals(genre)) {
            this.genre = "";
        } else {
            this.genre = genre;
        }
        searchStations(updateFilter());
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
