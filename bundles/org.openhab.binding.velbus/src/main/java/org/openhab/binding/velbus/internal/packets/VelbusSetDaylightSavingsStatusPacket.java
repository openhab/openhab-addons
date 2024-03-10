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
package org.openhab.binding.velbus.internal.packets;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.COMMAND_DAYLIGHT_SAVING_STATUS;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusSetDaylightSavingsStatusPacket} represents a Velbus packet that can be used to
 * set the daylight saving status of the given Velbus module.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusSetDaylightSavingsStatusPacket extends VelbusPacket {
    private ZonedDateTime zonedDateTime;

    public VelbusSetDaylightSavingsStatusPacket(byte address, ZonedDateTime zonedDateTime) {
        super(address, PRIO_LOW);

        this.zonedDateTime = zonedDateTime;
    }

    public boolean isDaylightSavings() {
        return zonedDateTime.getZone().getRules().isDaylightSavings(zonedDateTime.toInstant());
    }

    @Override
    protected byte[] getDataBytes() {
        return new byte[] { COMMAND_DAYLIGHT_SAVING_STATUS, (byte) (isDaylightSavings() ? 0x01 : 0x00) };
    }
}
