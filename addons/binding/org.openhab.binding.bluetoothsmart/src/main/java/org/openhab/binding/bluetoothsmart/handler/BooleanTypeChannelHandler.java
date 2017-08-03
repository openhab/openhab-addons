package org.openhab.binding.bluetoothsmart.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;

class BooleanTypeChannelHandler extends SingleChannelHandler<Boolean, OnOffType> {

    BooleanTypeChannelHandler(BluetoothSmartHandler handler, String channelID, boolean persistent) {
        super(handler, channelID, persistent);
    }

    BooleanTypeChannelHandler(BluetoothSmartHandler handler, String channelID) {
        super(handler, channelID);
    }

    @Override Boolean convert(OnOffType value) {
        return value != null && value == OnOffType.ON;
    }

    @Override OnOffType convert(Boolean value) {
        return value != null && value ? OnOffType.ON : OnOffType.OFF;
    }
}
