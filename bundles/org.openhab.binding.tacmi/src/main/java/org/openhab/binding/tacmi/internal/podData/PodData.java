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
package org.openhab.binding.tacmi.internal.podData;

import org.openhab.binding.tacmi.internal.message.Message;
import org.openhab.binding.tacmi.internal.message.MessageType;

/**
 * This class carries all relevant data for the POD
 *
 * @author Christian Niessner - Initial contribution
 */
public final class PodData {
    public final byte podId;
    public final MessageType messageType;
    public boolean dirty;
    public Message message;
    public long lastSent;

    /**
     * Create new AnalogValue with specified value and type
     */
    public PodData(byte podId, MessageType messageType) {
        this.podId = podId;
        this.messageType = messageType;
    }

}
