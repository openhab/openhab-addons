package org.openhab.binding.octoprint.internal.services;

public class PollRequestInformation{
    public final String channelUID;
    public final String route;
    public final String jsonKey;
    
    PollRequestInformation(String channelUID, String route, String jsonKey) {
        this.channelUID = channelUID;
        this.route = route;
        this.jsonKey = jsonKey;
    }
    
}
