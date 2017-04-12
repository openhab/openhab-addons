
/*
 * Copyright (C) 2010 Archie L. Cobbs. All rights reserved.
 *
 * $Id: LineStateMaskCommand.java 39 2011-03-22 17:21:53Z archie.cobbs $
 */

package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.COM_PORT_OPTION;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;
import static gnu.io.rfc2217.RFC2217.SET_LINESTATE_MASK;

/**
 * RFC 2217 {@code SET-LINESTATE-MASK} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public class LineStateMaskCommand extends ComPortCommand {

    private int lineStateMask;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length != 3
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_LINESTATE_MASK} (client or server)
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
        this(new int[] {
            COM_PORT_OPTION,
            client ? SET_LINESTATE_MASK : SET_LINESTATE_MASK + SERVER_OFFSET,
            lineStateMask
        });
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

