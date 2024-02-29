package org.openhab.binding.octoprint.internal.services;

import org.eclipse.jetty.client.api.Response;
import org.openhab.binding.octoprint.internal.OctoPrintHandler;
import org.openhab.binding.octoprint.internal.models.OctopiServer;

import java.util.HashMap;

public class PollRequestService {
    final HttpRequestService requestService;
    HashMap<String, PollRequestInformation> requests;
    PollRequestService(OctopiServer octopiServer) {
        requestService = new HttpRequestService(octopiServer);
    }

    public void addPollRequest(String channelUID, String route, String jsonKey) {
        requests.putIfAbsent(channelUID, new PollRequestInformation(channelUID, route, jsonKey));
    }

    public void poll() {
        for (var entry: requests.entrySet()) {
            Response res = requestService.getRequest(entry.getValue().route);
            if (res.getStatus() == 200) {
                update
            }
        }
    }
}

