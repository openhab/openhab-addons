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
 * RFC 2217 {@code NOTIFY-LINESTATE} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 * @author jserv
 */
public class NotifyLineStateCommand extends ComPortCommand {

    private int lineState;

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *            NullPointerException if {@code bytes} is null
     *            IllegalArgumentException if {@code bytes} has length != 3
     *            IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *            IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#NOTIFY_LINESTATE} (client or
     *            server)
     */
    public NotifyLineStateCommand(int[] bytes) {
        super("NOTIFY-LINESTATE", NOTIFY_LINESTATE, bytes);
        this.lineState = bytes[2];
    }

    /**
     * Encoding constructor.
     *
     * @param lineState line state value
     * @param client true for the client-to-server command, false for the server-to-client command
     */
    public NotifyLineStateCommand(boolean client, int lineState) {
        this(new int[] { COM_PORT_OPTION, client ? NOTIFY_LINESTATE : NOTIFY_LINESTATE + SERVER_OFFSET, lineState });
    }

    @Override
    public String toString() {
        return this.getName() + " " + Util.decodeBits(this.lineState, Util.LINE_STATE_BITS);
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseNotifyLineState(this);
    }

    public int getLineState() {
        return this.lineState;
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
