/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.pentair.internal.PentairPacket;

/**
 * Abstract class for all Pentair Things.
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
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

    public void writePacket(byte[] packet) {
        writePacket(packet, -1, 0);
    }

    public boolean writePacket(byte[] packet, int response, int retries) {
        PentairPacket p = new PentairPacket(packet);

        return writePacket(p, response, retries);
    }

    public boolean writePacket(PentairPacket p, int response, int retries) {
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            return false;
        }

        PentairBaseBridgeHandler bbh = (PentairBaseBridgeHandler) bridge.getHandler();
        if (bbh == null) {
            return false;
        }

        return bbh.writePacket(p, response, retries);
    }

    public void delay300() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Abstract function to be implemented by Thing to parse a received packet
     *
     * @param p
     */
    public abstract void processPacketFrom(PentairPacket p);
}
