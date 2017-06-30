package org.openhab.binding.snapcast.internal.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.snapcast.internal.rpc.JsonRpcEventClient;
import org.openhab.binding.snapcast.internal.types.Group;

public class SnapcastGroupController {

    private final JsonRpcEventClient connection;
    private final String id;
    private final Map<String, Group> groupMap;
    private final List<SnapgroupUpdateListener> updateListeners = new ArrayList<>();

    public SnapcastGroupController(JsonRpcEventClient connection, String id, Map<String, Group> groupMap,
            List<SnapgroupUpdateListener> updateListeners) {

        this.connection = connection;
        this.id = id;
        this.groupMap = groupMap;

        // Copy update listener, this prevents unique client update listener to be export to the
        // "Global updatelisterners" and not causing all clients to update everytime a single
        // client i supdated.
        updateListeners.forEach(u -> this.updateListeners.add(u));
    }

    private Map<String, Object> createParamsObject() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return params;
    }

    public SnapcastGroupController stream(final String streamId) throws IOException, InterruptedException {
        final Map<String, Object> params = createParamsObject();
        params.put("stream_id", streamId);
        connection.sendRequestAndReadResponse("Client.SetStream", params, Boolean.class);
        group().setStreamId(streamId);
        notifyUpdateListeners();
        return this;
    }

    public String stream() {
        return group().getStreamId();
    }

    void notifyUpdateListeners() {
        updateListeners.forEach(u -> u.updateGroup(this));
    }

    private Group group() {
        return groupMap.get(id);
    }

    @SuppressWarnings("unused")
    public void addUpdateListener(final SnapgroupUpdateListener updateListener) {
        updateListeners.add(updateListener);
    }

}
