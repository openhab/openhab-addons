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
 * RFC 2217 {@code FLOWCONTROL-SUSPEND} command.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2217">RFC 2217</a>
 * @author jserv
 */
public class FlowControlSuspendCommand extends ComPortCommand {

    /**
     * Decoding constructor.
     *
     * @param bytes encoded option starting with the {@code COM-PORT-OPTION} byte
     *            NullPointerException if {@code bytes} is null
     *            IllegalArgumentException if {@code bytes} has length != 3
     *            IllegalArgumentException if {@code bytes[0]} is not {@link RFC2217#COM_PORT_OPTION}
     *            IllegalArgumentException if {@code bytes[1]} is not {@link RFC2217#FLOWCONTROL_SUSPEND} (client or
     *            server)
     */
    public FlowControlSuspendCommand(int[] bytes) {
        super("FLOWCONTROL-SUSPEND", FLOWCONTROL_SUSPEND, bytes);
    }

    /**
     * Encoding constructor.
     *
     * @param client true for the client-to-server command, false for the server-to-client command
     */
    public FlowControlSuspendCommand(boolean client) {
        this(new int[] { COM_PORT_OPTION, client ? FLOWCONTROL_SUSPEND : FLOWCONTROL_SUSPEND + SERVER_OFFSET, });
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public void visit(ComPortCommandSwitch sw) {
        sw.caseFlowControlSuspend(this);
    }

    @Override
    int getMinPayloadLength() {
        return 0;
    }

    @Override
    int getMaxPayloadLength() {
        return 0;
    }
}
