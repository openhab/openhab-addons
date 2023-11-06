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
package org.openhab.binding.digiplex.internal.communication.events;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.digiplex.internal.communication.DigiplexMessageHandler;

/**
 * Represents generic event received from PRT3 module.
 *
 * It is created when no specific handler is found for the received event.
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
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
