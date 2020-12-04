package io.flic.fliclib.javaclient;

import io.flic.fliclib.javaclient.enums.BdAddrType;
import io.flic.fliclib.javaclient.enums.BluetoothControllerState;

import java.io.IOException;

/**
 * GetInfoResponseCallback.
 *
 * Used in {@link FlicClient#getInfo(GetInfoResponseCallback)}.
 */
public abstract class GetInfoResponseCallback {
    public abstract void onGetInfoResponse(BluetoothControllerState bluetoothControllerState, Bdaddr myBdAddr,
                                           BdAddrType myBdAddrType, int maxPendingConnections,
                                           int maxConcurrentlyConnectedButtons, int currentPendingConnections,
                                           boolean currentlyNoSpaceForNewConnection,
                                           Bdaddr[] verifiedButtons) throws IOException;
}
