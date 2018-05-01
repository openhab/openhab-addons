/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal.packets;

/**
 * The {@link VelbusScanPacket} represents a Velbus packet that can be used to
 * check if a Velbus module exists on the given address and to request this module's
 * basic properties.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusScanPacket extends VelbusPacket {

    public VelbusScanPacket(byte address) {
        super(address, PRIO_LOW, true);
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[0];
    }

}
