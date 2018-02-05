/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.rfc2217;

import org.apache.commons.net.telnet.TelnetOptionHandler;

/**
 * Handler for the telnet {@code TRANSMIT-BINARY} option defined by RFC 856.
 *
 * @see <a href="http://tools.ietf.org/html/rfc856">RFC 856</a>
 * @author jserv
 */
public class TransmitBinaryOptionHandler extends TelnetOptionHandler {

    public static final int TRANSMIT_BINARY_OPTION = 0;

    public TransmitBinaryOptionHandler(boolean initlocal, boolean initremote, boolean acceptlocal,
            boolean acceptremote) {
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
