package org.openhab.binding.paradoxalarm.internal.communication;

import org.openhab.binding.paradoxalarm.internal.communication.messages.IPPacket;

public class ZoneCommandRequest extends Request {

    public ZoneCommandRequest(RequestType type, IPPacket packet, IResponseReceiver receiver) {
        super(type, packet, receiver);
    }
}
