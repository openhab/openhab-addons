package org.openhab.binding.bluetoothsmart.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.sputnikdev.bluetooth.URL;

public interface ChannelHandler {

    void init();

    void handleCommand(ChannelUID channelUID, Command command);

    void handleUpdate(ChannelUID channelUID, State newState);

    void dispose();

    URL getURL();

}
