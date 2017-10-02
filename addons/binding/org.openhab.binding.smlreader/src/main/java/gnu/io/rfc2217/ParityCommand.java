/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.*;

/**
 * RFC 2217 {@code SET-PARITY} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 * @author jserv
 */
public class ParityCommand extends ComPortCommand {

    private int parity;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *            NullPointerException if {@code bytes} is null
     *            IllegalArgumentException if {@code bytes} has length != 3
     *            IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *            IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_PARITY} (client or server)
     *            IllegalArgumentException if {@code bytes[2]} is not a valid RFC 2217 parity value
     */
    public ParityCommand(int[] bytes) {
        super("SET-PARITY", SET_PARITY, bytes);
        this.parity = bytes[2];
        switch (this.parity) {
            case PARITY_REQUEST:
            case PARITY_NONE:
            case PARITY_ODD:
            case PARITY_EVEN:
            case PARITY_MARK:
            case PARITY_SPACE:
                break;
            default:
                throw new IllegalArgumentException("invalid parity value " + this.parity);
        }
    }

    /**
     * Encoding constructor.
     *
     * @param parity parity value
     * @param client true for the client-to-server command, false for the server-to-client command
     *            IllegalArgumentException if {@code parity} is not a valid RFC 2217 parity value
     */
    public ParityCommand(boolean client, int parity) {
        this(new int[] { COM_PORT_OPTION, client ? SET_PARITY : SET_PARITY + SERVER_OFFSET, parity });
    }

    @Override
    public String toString() {
        String desc;
        switch (this.parity) {
            case PARITY_REQUEST:
                desc = "REQUEST";
                break;
            case PARITY_NONE:
                desc = "NONE";
                break;
            case PARITY_ODD:
                desc = "ODD";
                break;
            case PARITY_EVEN:
                desc = "EVEN";
                break;
            case PARITY_MARK:
                desc = "MARK";
                break;
            case PARITY_SPACE:
                desc = "SPACE";
                break;
            default:
                desc = "?";
                break;
        }
        return this.getName() + " " + desc;
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseParity(this);
    }

    public int getParity() {
        return this.parity;
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
