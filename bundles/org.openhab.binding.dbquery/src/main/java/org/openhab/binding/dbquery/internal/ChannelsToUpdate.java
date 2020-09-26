package org.openhab.binding.dbquery.internal;

import java.util.List;

import org.openhab.core.thing.Channel;

public interface ChannelsToUpdate {
    List<Channel> getChannels();
}
