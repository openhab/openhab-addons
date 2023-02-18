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
import org.openhab.binding.tacmi.internal.message.Message;
import org.openhab.binding.tacmi.internal.message.MessageType;

/**
 * This class carries all relevant data for the POD
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class PodData {
    protected final byte podId;
    protected final MessageType messageType;
    protected @Nullable Message message;

    /**
     * Create new AnalogValue with specified value and type
     */
    public PodData(PodIdentifier pi, byte node) {
        this.podId = pi.podId;
        this.messageType = pi.messageType;
    }
}
