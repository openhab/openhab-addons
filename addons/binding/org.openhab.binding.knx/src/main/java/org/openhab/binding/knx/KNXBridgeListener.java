package org.openhab.binding.knx;

import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;

/**
 * The {@link KNXBridgeListener} is an interface that needs to be
 * implemented by classes that want to listen to Bridge connectivity
 *
 * @author Karel Goderis - Initial contribution
 */
public interface KNXBridgeListener {

    /**
     * 
     * Called when the connection with the KNX bridge is lost
     * 
     * @param bridge
     */
    public void onBridgeDisconnected(KNXBridgeBaseThingHandler bridge);

    /**
     * Called when the connection with the KNX bridge is established
     * 
     * @param bridge
     */
    public void onBridgeConnected(KNXBridgeBaseThingHandler bridge);

}
