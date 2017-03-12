package org.openhab.binding.lgtvserial.internal.protocol.serial;

public class LGCommunicationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6762203224719713562L;

    public LGCommunicationException(String data) {
        super("Cannot interpret command " + data);
    }

}
