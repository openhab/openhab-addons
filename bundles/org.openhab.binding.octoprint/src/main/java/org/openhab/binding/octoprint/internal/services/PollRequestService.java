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

package org.openhab.binding.octoprint.internal.services;

import java.util.HashMap;

import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.octoprint.internal.OctoPrintHandler;
import org.openhab.binding.octoprint.internal.models.OctopiServer;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The {@link PollRequestService}.TODO
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class PollRequestService {
    private final Logger logger = LoggerFactory.getLogger(PollRequestService.class);

    final HttpRequestService requestService;
    final OctoPrintHandler octoPrintHandler;
    HashMap<String, PollRequestInformation> requests = new HashMap<>();

    public PollRequestService(OctopiServer octopiServer, OctoPrintHandler octoPrintHandler) {
        requestService = new HttpRequestService(octopiServer);
        this.octoPrintHandler = octoPrintHandler;
    }

    public void addPollRequest(String channelUID, String route, String jsonKey, State type) {
        requests.putIfAbsent(channelUID, new PollRequestInformation(channelUID, route, jsonKey, type));
        logger.debug("added {} into poll requests as: [{}, {}, {}, {}]", channelUID, channelUID, route, jsonKey,
                type.toString());
    }

    public void poll() {
        Gson gson = new Gson();
        for (var entry : requests.entrySet()) {
            String channelUID = entry.getKey();
            PollRequestInformation pollRequestInformation = entry.getValue();

            ContentResponse res = requestService.getRequest(pollRequestInformation.route);
            JsonObject json = JsonParser.parseString(res.getContentAsString()).getAsJsonObject();
            var updatedValue = json.get(pollRequestInformation.jsonKey);
            if (res.getStatus() == 200) {
                if (pollRequestInformation.type instanceof StringType) {
                    octoPrintHandler.updateChannel(channelUID, StringType.valueOf(updatedValue.getAsString()));
                    logger.debug("Updated Channel {} to state {}", channelUID, updatedValue.getAsString());
                }
            }
        }
    }

    public void dispose() {
        requestService.dispose();
    }
}
