/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Message providing information about special alarm events
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
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
