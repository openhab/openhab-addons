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
package org.openhab.binding.tacmi.internal.coe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tacmi.internal.message.MessageType;

/**
 * This class defines a key for POD identification
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public final class PodIdentifier {
    public final MessageType messageType;
    public final byte podId;
    public final boolean outgoing;

    /**
     * Create new AnalogValue with specified value and type
     */
    public PodIdentifier(MessageType messageType, byte podId, boolean outgoing) {
        this.messageType = messageType;
        if (podId < 0) {
            throw new ArrayIndexOutOfBoundsException(podId);
        }
        switch (messageType) {
            case ANALOG:
                if (podId < 1 || podId > 8) {
                    throw new ArrayIndexOutOfBoundsException(podId);
                }
                break;
            case DIGITAL:
                if (podId != 0 && podId != 9) {
                    throw new ArrayIndexOutOfBoundsException(podId);
                }
                break;
        }
        this.podId = podId;
        this.outgoing = outgoing;
    }

    @Override
    public int hashCode() {
        return (this.messageType.ordinal() << 8) | podId | (outgoing ? 0x10000 : 0);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof PodIdentifier)) {
            return false;
        }
        PodIdentifier po = (PodIdentifier) o;
        return this.messageType == po.messageType && this.podId == po.podId && this.outgoing == po.outgoing;
    }
}
