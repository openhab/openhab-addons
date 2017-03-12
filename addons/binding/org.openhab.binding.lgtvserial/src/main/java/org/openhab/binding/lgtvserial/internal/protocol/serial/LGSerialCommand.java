package org.openhab.binding.lgtvserial.internal.protocol.serial;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.ChannelUID;

/**
 * This interface represents an LG serial command.
 *
 * @author Richard Lavoie
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
