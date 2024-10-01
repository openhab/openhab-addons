package io.flic.fliclib.javaclient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Button scanner class.
 *
 * Inherit this class and override the
 * {@link #onAdvertisementPacket(Bdaddr, String, int, boolean, boolean, boolean, boolean)} method.
 * Then add this button scanner to a {@link FlicClient} using {@link FlicClient#addScanner(ButtonScanner)} to start it.
 */
public abstract class ButtonScanner {
    private static AtomicInteger nextId = new AtomicInteger();
    int scanId = nextId.getAndIncrement();

    /**
     * This will be called for every received advertisement packet from a Flic button.
     *
     * @param bdaddr Bluetooth address
     * @param name Advertising name
     * @param rssi RSSI value in dBm
     * @param isPrivate The button is private and won't accept new connections from non-bonded clients
     * @param alreadyVerified The server has already verified this button, which means you can connect to it even if it's private
     * @param alreadyConnectedToThisDevice The button is already connected to this device
     * @param alreadyConnectedToOtherDevice The button is already connected to another device
     */
    public abstract void onAdvertisementPacket(Bdaddr bdaddr, String name, int rssi, boolean isPrivate, boolean alreadyVerified, boolean alreadyConnectedToThisDevice, boolean alreadyConnectedToOtherDevice) throws IOException;
}
