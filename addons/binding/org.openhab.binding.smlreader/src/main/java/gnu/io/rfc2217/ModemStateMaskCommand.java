
/*
 * Copyright (C) 2010 Archie L. Cobbs. All rights reserved.
 *
 * $Id: ModemStateMaskCommand.java 39 2011-03-22 17:21:53Z archie.cobbs $
 */

package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.COM_PORT_OPTION;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;
import static gnu.io.rfc2217.RFC2217.SET_MODEMSTATE_MASK;

/**
 * RFC 2217 {@code SET-MODEMSTATE-MASK} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public class ModemStateMaskCommand extends ComPortCommand {

    private int modemStateMask;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length != 3
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_MODEMSTATE_MASK} (client or server)
     */
    public ModemStateMaskCommand(int[] bytes) {
        super("SET-MODEMSTATE-MASK", SET_MODEMSTATE_MASK, bytes);
        this.modemStateMask = bytes[2];
    }

    /**
     * Encoding constructor.
     *
     * @param modemStateMask modem state mask value
     * @param client true for the client-to-server command, false for the server-to-client command
     */
    public ModemStateMaskCommand(boolean client, int modemStateMask) {
        this(new int[] {
            COM_PORT_OPTION,
            client ? SET_MODEMSTATE_MASK : SET_MODEMSTATE_MASK + SERVER_OFFSET,
            modemStateMask
        });
    }

    @Override
    public String toString() {
        return this.getName() + " " + Util.decodeBits(this.modemStateMask, Util.MODEM_STATE_BITS);
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseModemStateMask(this);
    }

    public int getModemStateMask() {
        return this.modemStateMask;
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

