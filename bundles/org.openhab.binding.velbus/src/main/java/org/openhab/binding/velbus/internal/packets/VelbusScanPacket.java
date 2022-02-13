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
package org.openhab.binding.velbus.internal.packets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusScanPacket} represents a Velbus packet that can be used to
 * check if a Velbus module exists on the given address and to request this module's
 * basic properties.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusScanPacket extends VelbusPacket {

    public VelbusScanPacket(byte address) {
        super(address, PRIO_LOW, true);
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[0];
    }
}
