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
 * RFC 2217 {@code SET-DATASIZE} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 * @author jserv
 */
public class DataSizeCommand extends ComPortCommand {

    private int dataSize;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *            NullPointerException if {@code bytes} is null
     *            IllegalArgumentException if {@code bytes} has length != 3
     *            IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *            IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#SET_DATASIZE} (client or server)
     *            IllegalArgumentException if {@code bytes[2]} is not a valid RFC 2217 data size value
     */
    public DataSizeCommand(int[] bytes) {
        super("SET-DATASIZE", SET_DATASIZE, bytes);
        this.dataSize = bytes[2];
        switch (this.dataSize) {
            case DATASIZE_REQUEST:
            case DATASIZE_5:
            case DATASIZE_6:
            case DATASIZE_7:
            case DATASIZE_8:
                break;
            default:
                throw new IllegalArgumentException("invalid data size value " + this.dataSize);
        }
    }

    /**
     * Encoding constructor.
     *
     * @param dataSize data size value
     * @param client true for the client-to-server command, false for the server-to-client command
     *            IllegalArgumentException if {@code dataSize} is not a valid RFC 2217 data size value
     */
    public DataSizeCommand(boolean client, int dataSize) {
        this(new int[] { COM_PORT_OPTION, client ? SET_DATASIZE : SET_DATASIZE + SERVER_OFFSET, dataSize });
    }

    @Override
    public String toString() {
        String desc;
        switch (this.dataSize) {
            case DATASIZE_REQUEST:
                desc = "REQUEST";
                break;
            case DATASIZE_5:
                desc = "5";
                break;
            case DATASIZE_6:
                desc = "6";
                break;
            case DATASIZE_7:
                desc = "7";
                break;
            case DATASIZE_8:
                desc = "8";
                break;
            default:
                desc = "?";
                break;
        }
        return this.getName() + " " + desc;
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseDataSize(this);
    }

    public int getDataSize() {
        return this.dataSize;
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
