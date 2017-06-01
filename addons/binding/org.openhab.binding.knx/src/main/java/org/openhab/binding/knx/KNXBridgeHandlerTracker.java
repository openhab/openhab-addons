package org.openhab.binding.knx;

import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;

public interface KNXBridgeHandlerTracker {

    void onBridgeAdded(KNXBridgeBaseThingHandler handler);

    void onBridgeRemoved(KNXBridgeBaseThingHandler handler);

}
