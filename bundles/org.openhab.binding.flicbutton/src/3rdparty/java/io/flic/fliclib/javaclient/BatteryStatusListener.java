package io.flic.fliclib.javaclient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Battery status listener.
 *
 * Add this listener to a {@link FlicClient} by executing {@link FlicClient#addBatteryStatusListener(BatteryStatusListener)}.
 */
public class BatteryStatusListener {
    private static AtomicInteger nextId = new AtomicInteger();
    int listenerId = nextId.getAndIncrement();

    private Bdaddr bdaddr;
    Callbacks callbacks;

    public BatteryStatusListener(Bdaddr bdaddr, Callbacks callbacks) {
        if (bdaddr == null) {
            throw new IllegalArgumentException("bdaddr is null");
        }
        if (callbacks == null) {
            throw new IllegalArgumentException("callbacks is null");
        }
        this.bdaddr = bdaddr;
        this.callbacks = callbacks;
    }

    public Bdaddr getBdaddr() {
        return bdaddr;
    }

    public abstract static class Callbacks {
        /**
         * This will be called when the battery status has been updated.
         * It will also be called immediately after the battery status listener has been created.
         * If the button stays connected, this method will be called approximately every three hours.
         *
         * @param bdaddr            Bluetooth device address
         * @param batteryPercentage A number between 0 and 100 for the battery level. Will be -1 if unknown.
         * @param timestamp         Standard UNIX timestamp, in seconds, for the event.
         */
        public abstract void onBatteryStatus(Bdaddr bdaddr, int batteryPercentage, long timestamp) throws IOException;
    }
}