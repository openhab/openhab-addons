package io.flic.fliclib.javaclient;

import io.flic.fliclib.javaclient.enums.BluetoothControllerState;

import java.io.IOException;

/**
 * GeneralCallbacks.
 *
 * See the protocol specification for further details.
 */
public class GeneralCallbacks {
    public void onNewVerifiedButton(Bdaddr bdaddr) throws IOException {

    }
    public void onNoSpaceForNewConnection(int maxConcurrentlyConnectedButtons) throws IOException {

    }
    public void onGotSpaceForNewConnection(int maxConcurrentlyConnectedButtons) throws IOException {

    }
    public void onBluetoothControllerStateChange(BluetoothControllerState state) throws IOException {

    }
    public void onButtonDeleted(Bdaddr bdaddr, boolean deletedByThisClient) throws IOException {

    }
}
