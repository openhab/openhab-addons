package org.openhab.binding.paradoxalarm.internal.communication.messages;

import org.openhab.binding.paradoxalarm.internal.communication.IRequest;

public interface Command {
    IRequest getRequest(int id);
}
