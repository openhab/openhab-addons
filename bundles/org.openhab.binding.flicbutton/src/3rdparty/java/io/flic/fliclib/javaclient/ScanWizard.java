package io.flic.fliclib.javaclient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import io.flic.fliclib.javaclient.enums.ScanWizardResult;

/**
 * Scan wizard class.
 *
 * This class will scan for a new button and pair it automatically.
 * There are internal timeouts that make sure operations don't take too long time.
 *
 * Inherit this class and override the methods.
 * Then add this scan wizard to a {@link FlicClient} using {@link FlicClient#addScanWizard(ScanWizard)} to start it.
 * You can cancel by calling {@link FlicClient#cancelScanWizard(ScanWizard)}.
 */
public abstract class ScanWizard {
    private static AtomicInteger nextId = new AtomicInteger();
    int scanWizardId = nextId.getAndIncrement();
    Bdaddr bdaddr;
    String name;

    /**
     * This will be called once if a private button is found.
     *
     * Tell the user to hold down the button for 7 seconds in order to make it public.
     *
     */
    public abstract void onFoundPrivateButton() throws IOException;
    
    /**
     * This will be called once a public button is found.
     *
     * Now a connection attempt will be made to the device in order to pair and verify it.
     *
     * @param bdaddr Bluetooth Device Address
     * @param name Advertising name
     */
    public abstract void onFoundPublicButton(Bdaddr bdaddr, String name) throws IOException;
    
    /**
     * This will be called once the bluetooth connection has been established.
     *
     * Now a pair attempt will be made.
     *
     * @param bdaddr Bluetooth Device Address
     * @param name Advertising name
     */
    public abstract void onButtonConnected(Bdaddr bdaddr, String name) throws IOException;
    
    /**
     * Scan wizard completed.
     *
     * If the result is success, you can now create a connection channel to the button.
     *
     * The ScanWizard is now detached from the FlicClient and can now be recycled.
     *
     * @param result Result of the scan wizard
     * @param bdaddr Bluetooth Device Address or null, depending on if {@link #onFoundPublicButton} has been called or not
     * @param name Advertising name or null, depending on if {@link #onFoundPublicButton} has been called or not
     */
    public abstract void onCompleted(ScanWizardResult result, Bdaddr bdaddr, String name) throws IOException;
}
