/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Message providing global trouble status
 *
 * @author Robert Michalak - Initial contribution
 *
 */
@NonNullByDefault
public class TroubleEvent extends AbstractEvent {

    private TroubleStatus status;
    private TroubleType type;

    public TroubleEvent(TroubleType type, TroubleStatus status, int areaNo) {
        super(areaNo);
        this.type = type;
        this.status = status;
    }

    public TroubleStatus getStatus() {
        return status;
    }

    public TroubleType getType() {
        return type;
    }

    @Override
    public void accept(DigiplexMessageHandler visitor) {
        visitor.handleTroubleEvent(this);
    }
}
