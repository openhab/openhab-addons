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
 * Message providing miscellaneous area informations
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class AreaEvent extends AbstractEvent {

    private AreaEventType type;

    public AreaEvent(AreaEventType type, int areaNo) {
        super(areaNo);
        this.type = type;
    }

    public AreaEventType getType() {
        return type;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleAreaEvent(this);
    }
}
