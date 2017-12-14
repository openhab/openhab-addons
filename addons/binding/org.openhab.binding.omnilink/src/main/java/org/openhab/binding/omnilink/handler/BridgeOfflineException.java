package org.openhab.binding.omnilink.handler;

/**
 *
 * @author Craig Hamilton
 *
 */
public class BridgeOfflineException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -9081729691518514097L;

    public BridgeOfflineException(Exception e) {
        super(e);
    }

}
