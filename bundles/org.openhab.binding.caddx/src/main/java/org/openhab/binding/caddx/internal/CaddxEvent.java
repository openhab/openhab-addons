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
package org.openhab.binding.caddx.internal;

import java.util.EventObject;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event for Receiving API Messages.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final CaddxMessage caddxMessage;
    private final @Nullable Integer partition;
    private final @Nullable Integer zone;
    private final @Nullable Integer keypad;

    public CaddxEvent(CaddxMessage caddxMessage, @Nullable Integer partition, @Nullable Integer zone,
            @Nullable Integer keypad) {
        super(caddxMessage);

        this.caddxMessage = caddxMessage;
        this.partition = partition;
        this.zone = zone;
        this.keypad = keypad;
    }

    /**
     * Returns the Message event from the Caddx Alarm System.
     *
     * @return message
     */
    public CaddxMessage getCaddxMessage() {
        return caddxMessage;
    }

    public @Nullable Integer getPartition() {
        return partition;
    }

    public @Nullable Integer getZone() {
        return zone;
    }

    public @Nullable Integer getKeypad() {
        return keypad;
    }

    /**
     * Returns a string representation of a CaddxEvent.
     *
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("partition: %d, zone: %d, keypad: %d\r\n", partition, zone, keypad));
        sb.append(caddxMessage.toString());

        return sb.toString();
    }
}
