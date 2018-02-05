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
 * Message providing information about special alarm events
 *
 * @author Robert Michalak - Initial contribution
 *
 */
public class SpecialAlarmEvent extends AbstractEvent {

    private SpecialAlarmType type;

    public SpecialAlarmEvent(int areaNo, SpecialAlarmType type) {
        super(areaNo);
        this.type = type;
    }

    public SpecialAlarmType getType() {
        return type;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleSpecialAlarmEvent(this);
    }
}
