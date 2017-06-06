package org.openhab.binding.insteonplm.handler;

/**
 * Keeps track of the insteon thing and the time at which we want to send
 * it.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class InsteonBridgeThingQEntry {
    private long expirationTime;
    private final InsteonPlmBaseThing thingHandler;

    public InsteonBridgeThingQEntry(InsteonPlmBaseThing handler, long expirationTime) {
        this.expirationTime = expirationTime;
        this.thingHandler = handler;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public InsteonPlmBaseThing getThingHandler() {
        return thingHandler;
    }

    public void setExpirationTime(long time) {
        this.expirationTime = time;
    }
}
