/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgtvserial.internal.protocol.serial;

import java.io.IOException;

import org.openhab.core.thing.ChannelUID;

/**
 * This interface represents an LG serial command.
 *
 * @author Richard Lavoie - Initial contribution
 */
public interface LGSerialCommand {

    /**
     * Parse the response string into a response object. If null is returned, it means the result cannot be used to
     * update a linked item state. This is useful to have one way commands.
     *
     * @param response Response to parse
     * @return Response object
     */
    LGSerialResponse parseResponse(String response);

    /**
     * This method is used to send the serial protocol command to the communicator.
     *
     * @param channel Channel related to the command
     * @param comm Communicator linked to the serial port
     * @param data Data related to this command to send over the wire. If null, this means we should send an FF state
     *            read command.
     * @throws IOException
     */
    void execute(ChannelUID channel, LGSerialCommunicator comm, Object data) throws IOException;
}
