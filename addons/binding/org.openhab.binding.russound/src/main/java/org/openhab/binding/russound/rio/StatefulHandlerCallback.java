package org.openhab.binding.russound.rio;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.LoggerFactory;

public class StatefulHandlerCallback implements RioHandlerCallback {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(StatefulHandlerCallback.class);

    private final RioHandlerCallback _wrappedCallback;
    private final Map<String, State> _state = new HashMap<String, State>();

    public StatefulHandlerCallback(RioHandlerCallback wrappedCallback) {
        _wrappedCallback = wrappedCallback;
    }

    @Override
    public void statusChanged(ThingStatus status, ThingStatusDetail detail, String msg) {
        _wrappedCallback.statusChanged(status, detail, msg);

    }

    @Override
    public void stateChanged(String channelId, State state) {
        final State oldState = _state.get(channelId);

        // If both null OR the same value (enums), nothing changed
        if (oldState == state) {
            return;
        }

        // If they are equal - nothing changed
        if (oldState != null && oldState.equals(state)) {
            return;
        }

        // Something changed - save the new state and call the underlying wrapped
        _state.put(channelId, state);
        _wrappedCallback.stateChanged(channelId, state);

    }

    public void removeState(String channelId) {
        _state.remove(channelId);
    }
}
