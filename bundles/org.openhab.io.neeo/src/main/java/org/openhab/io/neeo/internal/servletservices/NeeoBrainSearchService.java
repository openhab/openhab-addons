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
package org.openhab.io.neeo.internal.servletservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.TokenSearch;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.openhab.io.neeo.internal.models.TokenScore;
import org.openhab.io.neeo.internal.models.TokenScoreResult;
import org.openhab.io.neeo.internal.serialization.NeeoBrainDeviceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * The implementation of {@link ServletService} that will handle device search requests from the NEEO Brain
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoBrainSearchService extends DefaultServletService {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoBrainSearchService.class);

    /** The gson used to for json manipulation */
    private final Gson gson;

    /** The context. */
    private final ServiceContext context;

    /** The last search results */
    private final Map<Integer, NeeoThingUID> lastSearchResults = new ConcurrentHashMap<>();

    /**
     * Constructs the service from the given {@link ServiceContext}.
     *
     * @param context the non-null {@link ServiceContext}
     */
    public NeeoBrainSearchService(ServiceContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        this.context = context;

        final GsonBuilder gsonBuilder = NeeoUtil.createGsonBuilder();
        gsonBuilder.registerTypeAdapter(NeeoDevice.class, new NeeoBrainDeviceSerializer());

        gson = gsonBuilder.create();
    }

    /**
     * Returns true if the path starts with "db"
     *
     * @see DefaultServletService#canHandleRoute(String[])
     */
    @Override
    public boolean canHandleRoute(String[] paths) {
        return paths.length >= 1 && paths[0].equalsIgnoreCase("db");
    }

    /**
     * Handles the get request. If the path is "/db/search", will do a search via
     * {@link #doSearch(String, HttpServletResponse)}. Otherwise we assume it's a request for device details (via
     * {@link #doQuery(String, HttpServletResponse)}
     *
     * As of 52.15 - "/db/adapterdefinition/{id}" get's the latest device details
     *
     * @see DefaultServletService#handleGet(HttpServletRequest, String[], HttpServletResponse)
     */
    @Override
    public void handleGet(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");
        if (paths.length < 2) {
            throw new IllegalArgumentException("paths must have atleast 2 elements: " + String.join("", paths));
        }

        final String path = paths[1].toLowerCase();

        if (path.equalsIgnoreCase("search")) {
            String queryString = req.getQueryString();
            if (queryString != null) {
                doSearch(queryString, resp);
            }
        } else if (path.equalsIgnoreCase("adapterdefinition") && paths.length >= 3) {
            doAdapterDefinition(paths[2], resp);
        } else {
            doQuery(path, resp);
        }
    }

    /**
     * Does the search of all things and returns the results
     *
     * @param queryString the non-null, possibly empty query string
     * @param resp the non-null response to write to
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void doSearch(String queryString, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(queryString, "queryString cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");

        final int idx = queryString.indexOf("=");

        if (idx >= 0 && idx + 1 < queryString.length()) {
            final String search = NeeoUtil.decodeURIComponent(queryString.substring(idx + 1));

            final List<JsonObject> ja = new ArrayList<>();
            search(search).stream().sorted(Comparator.comparing(TokenScoreResult<NeeoDevice>::getScore).reversed())
                    .forEach(item -> {
                        final JsonObject jo = (JsonObject) gson.toJsonTree(item);

                        // transfer id from tokenscoreresult to neeodevice (as per NEEO API)
                        final int id = jo.getAsJsonPrimitive("id").getAsInt();
                        jo.remove("id");
                        jo.getAsJsonObject("item").addProperty("id", id);
                        ja.add(jo);
                    });

            final String itemStr = gson.toJson(ja);
            logger.debug("Search '{}', response: {}", search, itemStr);
            NeeoUtil.write(resp, itemStr);
        }
    }

    /**
     * Does a query for the NEEO device definition
     *
     * @param id the non-empty (last) search identifier
     * @param resp the non-null response to write to
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void doAdapterDefinition(String id, HttpServletResponse resp) throws IOException {
        NeeoThingUID thingUID;
        try {
            thingUID = new NeeoThingUID(id);
        } catch (IllegalArgumentException e) {
            logger.debug("Not a valid thingUID: {}", id);
            NeeoUtil.write(resp, "{}");
            return;
        }

        final NeeoDevice device = context.getDefinitions().getDevice(thingUID);

        if (device == null) {
            logger.debug("Called with index position {} but nothing was found", id);
            NeeoUtil.write(resp, "{}");
        } else {
            final String jos = gson.toJson(device);
            NeeoUtil.write(resp, jos);

            logger.debug("Query '{}', response: {}", id, jos);
        }
    }

    /**
     * Does a query for the NEEO device definition
     *
     * @param id the non-empty (last) search identifier
     * @param resp the non-null response to write to
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void doQuery(String id, HttpServletResponse resp) throws IOException {
        NeeoUtil.requireNotEmpty(id, "id cannot be empty");
        Objects.requireNonNull(resp, "resp cannot be null");

        NeeoDevice device = null;

        int idx = -1;
        try {
            idx = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            logger.debug("Device ID was not a number: {}", id);
            idx = -1;
        }

        if (idx >= 0) {
            final NeeoThingUID thingUID = lastSearchResults.get(idx);

            if (thingUID != null) {
                device = context.getDefinitions().getDevice(thingUID);
            }
        }

        if (device == null) {
            logger.debug("Called with index position {} but nothing was found", id);
            NeeoUtil.write(resp, "{}");
        } else {
            final JsonObject jo = (JsonObject) gson.toJsonTree(device);
            jo.addProperty("id", idx);

            final String jos = jo.toString();
            NeeoUtil.write(resp, jos);

            logger.debug("Query '{}', response: {}", idx, jos);
        }
    }

    /**
     * Performs the actual search of things for the given query
     *
     * @param queryString the non-null, possibly empty query string
     * @return the non-null, possibly empty list of {@link TokenScoreResult}
     */
    private List<TokenScoreResult<NeeoDevice>> search(String queryString) {
        Objects.requireNonNull(queryString, "queryString cannot be null");
        final TokenSearch tokenSearch = new TokenSearch(context, NeeoConstants.SEARCH_MATCHFACTOR);
        final TokenSearch.Result searchResult = tokenSearch.search(queryString);

        final List<TokenScoreResult<NeeoDevice>> searchItems = new ArrayList<>();

        for (TokenScore<NeeoDevice> ts : searchResult.getDevices()) {
            final NeeoDevice device = ts.getItem();
            final TokenScoreResult<NeeoDevice> result = new TokenScoreResult<>(device, searchItems.size(),
                    ts.getScore(), searchResult.getMaxScore());

            searchItems.add(result);
        }

        final Map<Integer, NeeoThingUID> results = new HashMap<>();
        for (TokenScoreResult<NeeoDevice> tsr : searchItems) {
            results.put(tsr.getId(), tsr.getItem().getUid());
        }

        // this isn't really thread safe but close enough for me
        lastSearchResults.clear();
        lastSearchResults.putAll(results);

        return searchItems;
    }
}
