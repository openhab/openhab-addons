package org.openhab.binding.bluetooth.dbusbluez.handler.events;

import org.openhab.binding.bluetooth.dbusbluez.handler.DBusBlueZEvent;

public class AdapterDiscoveringChangedEvent extends DBusBlueZEvent {

    private boolean discovering;

    public AdapterDiscoveringChangedEvent(String adapter, boolean discovering) {
        super(EventType.ADAPTER_DISCOVERING_CHANGED, adapter);
        this.discovering = discovering;
    }

    public boolean isDiscovering() {
        return discovering;
    }

}
