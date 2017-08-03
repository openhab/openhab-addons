package org.openhab.binding.bluetoothsmart.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;

class IntegerTypeChannelHandler extends SingleChannelHandler<Integer, DecimalType> {

    IntegerTypeChannelHandler(BluetoothSmartHandler handler, String channelID, boolean persistent) {
        super(handler, channelID, persistent);
    }

    IntegerTypeChannelHandler(BluetoothSmartHandler handler, String channelID) {
        super(handler, channelID);
    }

    @Override Integer convert(DecimalType value) {
        return value != null ? value.intValue() : null;
    }

    @Override DecimalType convert(Integer value) {
        return new DecimalType(value);
    }
}
