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
package org.openhab.binding.tacmi.internal.coe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tacmi.internal.message.AnalogMessage;
import org.openhab.binding.tacmi.internal.message.DigitalMessage;
import org.openhab.binding.tacmi.internal.message.MessageType;
import org.openhab.core.thing.ChannelUID;

/**
 * This class carries all relevant data for the POD
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class PodDataOutgoing extends PodData {

    protected long lastSent;
    protected final ChannelUID[] channeUIDs;
    protected final boolean[] initialized;
    private boolean allValuesInitialized;

    /**
     * Create new AnalogValue with specified value and type
     */
    public PodDataOutgoing(PodIdentifier pi, byte node) {
        super(pi, node);
        boolean analog = pi.messageType == MessageType.ANALOG;
        int valueCount = analog ? 4 : 16;
        this.channeUIDs = new ChannelUID[valueCount];
        this.initialized = new boolean[valueCount];
        this.allValuesInitialized = false;
        this.message = analog ? new AnalogMessage(node, pi.podId) : new DigitalMessage(node, pi.podId);
        this.lastSent = System.currentTimeMillis();
    }

    /**
     * checks if all (in use) values have been set to a value - used to prevent sending of unintended values via CoE
     */
    public boolean isAllValuesInitialized() {
        if (this.allValuesInitialized) {
            return true;
        }
        boolean allInitialized = true;
        for (int idx = 0; idx < this.initialized.length; idx++) {
            if (this.channeUIDs[idx] != null && !this.initialized[idx]) {
                return false;
            }
        }
        if (!allInitialized) {
            return false;
        }
        this.allValuesInitialized = true;
        return true;
    }

    public String getUninitializedChannelNames() {
        if (this.allValuesInitialized) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < this.initialized.length; idx++) {
            ChannelUID ct = this.channeUIDs[idx];
            if (ct != null && !this.initialized[idx]) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(ct.getId());
            }
        }
        return sb.toString();
    }
}
