
/*
 * Copyright (C) 2010 Archie L. Cobbs. All rights reserved.
 *
 * $Id: StopSizeCommand.java 39 2011-03-22 17:21:53Z archie.cobbs $
 */

package gnu.io.rfc2217;

import static gnu.io.rfc2217.RFC2217.COM_PORT_OPTION;
import static gnu.io.rfc2217.RFC2217.SERVER_OFFSET;
import static gnu.io.rfc2217.RFC2217.SET_STOPSIZE;
import static gnu.io.rfc2217.RFC2217.STOPSIZE_1;
import static gnu.io.rfc2217.RFC2217.STOPSIZE_1_5;
import static gnu.io.rfc2217.RFC2217.STOPSIZE_2;
import static gnu.io.rfc2217.RFC2217.STOPSIZE_REQUEST;

/**
 * RFC 2217 {@code SET-STOPSIZE} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 */
public class StopSizeCommand extends ComPortCommand {

    private int stopSize;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *   NullPointerException if {@code bytes} is null
     *   IllegalArgumentException if {@code bytes} has length != 3
     *   IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *   IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_STOPSIZE} (client or server)
     *   IllegalArgumentException if {@code bytes[2]} is not a valid RFC 2217 stop size value
     */
    public StopSizeCommand(int[] bytes) {
        super("SET-STOPSIZE", SET_STOPSIZE, bytes);
        this.stopSize = bytes[2];
        switch (this.stopSize) {
        case STOPSIZE_REQUEST:
        case STOPSIZE_1:
        case STOPSIZE_2:
        case STOPSIZE_1_5:
            break;
        default:
            throw new IllegalArgumentException("invalid stop size value " + this.stopSize);
        }
    }

    /**
     * Encoding constructor.
     *
     * @param stopSize stop size value
     * @param client true for the client-to-server command, false for the server-to-client command
     *   IllegalArgumentException if {@code stopSize} is not a valid RFC 2217 stop size value
     */
    public StopSizeCommand(boolean client, int stopSize) {
        this(new int[] {
            COM_PORT_OPTION,
            client ? SET_STOPSIZE : SET_STOPSIZE + SERVER_OFFSET,
            stopSize
        });
    }

    @Override
    public String toString() {
        String desc;
        switch (this.stopSize) {
        case STOPSIZE_REQUEST:
            desc = "REQUEST";
            break;
        case STOPSIZE_1:
            desc = "1";
            break;
        case STOPSIZE_2:
            desc = "2";
            break;
        case STOPSIZE_1_5:
            desc = "1.5";
            break;
        default:
            desc = "?";
            break;
        }
        return this.getName() + " " + desc;
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseStopSize(this);
    }

    public int getStopSize() {
        return this.stopSize;
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

