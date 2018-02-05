/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.*;

/**
 * RFC 2217 {@code SET-LINESTATE-MASK} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 * @author jserv
 */
public class LineStateMaskCommand extends ComPortCommand {

    private int lineStateMask;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *            NullPointerException if {@code bytes} is null
     *            IllegalArgumentException if {@code bytes} has length != 3
     *            IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *            IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_LINESTATE_MASK} (client or
     *            server)
     */
    public LineStateMaskCommand(int[] bytes) {
        super("SET-LINESTATE-MASK", SET_LINESTATE_MASK, bytes);
        this.lineStateMask = bytes[2];
    }

    /**
     * Encoding constructor.
     *
     * @param lineStateMask line state mask value
     * @param client true for the client-to-server command, false for the server-to-client command
     */
    public LineStateMaskCommand(boolean client, int lineStateMask) {
        this(new int[] { COM_PORT_OPTION, client ? SET_LINESTATE_MASK : SET_LINESTATE_MASK + SERVER_OFFSET,
                lineStateMask });
    }

    @Override
    public String toString() {
        return this.getName() + " " + Util.decodeBits(this.lineStateMask, Util.LINE_STATE_BITS);
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseLineStateMask(this);
    }

    public int getLineStateMask() {
        return this.lineStateMask;
    }

    @Override
    int getMinPayloadLength() {
        return 1;
    }

    @Override
    int getMaxPayloadLength() {
        return 1;
    }
}
