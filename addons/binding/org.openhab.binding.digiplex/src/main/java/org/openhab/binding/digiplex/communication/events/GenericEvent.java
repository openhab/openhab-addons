/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication.events;

import org.openhab.binding.digiplex.communication.DigiplexMessageHandler;

/**
 * Represents generic event received from PRT3 module.
 *
 * It is created when no specific handler is found for the received event.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class GenericEvent extends AbstractEvent {

    private int eventGroup;
    private int eventNumber;

    public GenericEvent(int eventGroup, int eventNumber, int areaNumber) {
        super(areaNumber);
        this.eventGroup = eventGroup;
        this.eventNumber = eventNumber;
    }

    public int getEventGroup() {
        return eventGroup;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleGenericEvent(this);
    }
}
