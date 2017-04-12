
/*
 * Copyright (C) 2010 Archie L. Cobbs. All rights reserved.
 *
 * $Id: TransmitBinaryOptionHandler.java 46 2011-10-16 23:06:39Z archie.cobbs $
 */

package gnu.io.rfc2217;

import org.apache.commons.net.telnet.TelnetOptionHandler;

/**
 * Handler for the telnet {@code TRANSMIT-BINARY} option defined by RFC 856.
 *
 * @see <a href="http://tools.ietf.org/html/rfc856">RFC 856</a>
 */
public class TransmitBinaryOptionHandler extends TelnetOptionHandler {

    public static final int TRANSMIT_BINARY_OPTION = 0;

    public TransmitBinaryOptionHandler(boolean initlocal, boolean initremote, boolean acceptlocal, boolean acceptremote) {
        super(TRANSMIT_BINARY_OPTION, initlocal, initremote, acceptlocal, acceptremote);
    }

    @Override
    public int[] answerSubnegotiation(int[] data, int length) {
        return null;
    }

    @Override
    public int[] startSubnegotiationLocal() {
        return null;
    }

    @Override
    public int[] startSubnegotiationRemote() {
        return null;
    }
}

