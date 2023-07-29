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
package org.openhab.binding.lifx.internal.dto;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A packet handler responsible for converting a ByteBuffer into a Packet
 * instance.
 *
 * @param <T> the generic packet type
 *
 * @author Tim Buckley - Initial contribution
 * @author Karel Goderis - Enhancement for the V2 LIFX Firmware and LAN Protocol Specification
 */
@NonNullByDefault
public interface PacketHandler<T extends Packet> {

    /**
     * Creates a packet from the given buffer.
     *
     * @param buf the buffer used for creating the packet
     * @return the packet created from the buffer
     * @throws IllegalArgumentException when an empty packet could not be created or the data in the buffer
     *             could not be parsed
     */
    public abstract T handle(ByteBuffer buf);
}
