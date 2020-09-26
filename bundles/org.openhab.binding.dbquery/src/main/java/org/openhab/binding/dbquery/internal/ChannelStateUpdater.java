package org.openhab.binding.dbquery.internal;

import org.openhab.core.thing.Channel;
import org.openhab.core.types.State;

public interface ChannelStateUpdater {
    void updateChannelState(Channel channelUID, State value);
}
