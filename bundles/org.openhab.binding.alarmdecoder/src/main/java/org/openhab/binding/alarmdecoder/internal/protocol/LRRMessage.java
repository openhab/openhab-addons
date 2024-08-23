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
package org.openhab.binding.alarmdecoder.internal.protocol;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LRRMessage} class represents a parsed LRR message.
 * Based partly on code from the OH1 alarmdecoder binding by Bernd Pfrommer and Lucky Mallari.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public class LRRMessage extends ADMessage {

    // Example: !LRR:012,1,CID_1441,ff
    // or: !LRR:000,1,ARM_AWAY

    /** Event data contains user number or zone number for the event */
    public final String eventData;

    /** Partition event applies to. 0 means all partitions. */
    public final int partition;

    /** CID message for event as defined in SIA DC-05-1999.09 standard */
    public final String cidMessage;

    /** Report code */
    public final String reportCode;

    public LRRMessage(String message) throws IllegalArgumentException {
        super(message);

        String[] topLevel = message.split(":");
        if (topLevel.length != 2) {
            throw new IllegalArgumentException("multiple colons in LRR message");
        }

        List<String> parts = splitMsg(topLevel[1]);

        // Apparently the 4th part of the LRR message may not be included depending on version
        if (parts.size() < 3 || parts.size() > 4) {
            throw new IllegalArgumentException("Invalid number of parts in LRR message");
        }

        eventData = parts.get(0);
        cidMessage = parts.get(2);
        reportCode = parts.size() == 4 ? parts.get(3) : "";

        try {
            partition = Integer.parseInt(parts.get(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("LRR msg contains invalid number: " + e.getMessage(), e);
        }
    }
}
