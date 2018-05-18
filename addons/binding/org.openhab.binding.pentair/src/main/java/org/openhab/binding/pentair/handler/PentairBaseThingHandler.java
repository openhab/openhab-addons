/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pentair.handler;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.pentair.internal.PentairPacket;

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
