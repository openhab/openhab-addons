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
package org.openhab.binding.pentair.internal.handler;

import org.openhab.binding.pentair.internal.PentairPacket;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * Abstract class for all Pentair Things.
 *
 * @author Jeff James - Initial contribution
 *
 */
public abstract class PentairBaseThingHandler extends BaseThingHandler {
    /** ID of Thing on Pentair bus */
    protected int id;

    public PentairBaseThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Gets Pentair bus ID of Thing
     *
     * @return
     */
    public int getPentairID() {
        return id;
    }

    /**
     * Abstract function to be implemented by Thing to dispose/parse a received packet
     *
     * @param p
     */
    public abstract void processPacketFrom(PentairPacket p);
}
