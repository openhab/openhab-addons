package org.openhab.binding.omnilink.handler;

public class BridgeOfflineException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -9081729691518514097L;

    public BridgeOfflineException(Exception e) {
        super(e);
    }

}
