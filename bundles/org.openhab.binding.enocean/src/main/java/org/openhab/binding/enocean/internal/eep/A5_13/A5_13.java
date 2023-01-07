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
package org.openhab.binding.enocean.internal.eep.A5_13;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 * Base class for A5-13 EEP message handling
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public abstract class A5_13 extends _4BSMessage {
    public A5_13(ERP1Message packet) {
        super(packet);
    }

    protected static final int PARTONE = 0x10;
    protected static final int PARTTWO = 0x20;

    protected int getMessageIdentifier() {
        return getDB0Value() & 0xF0;
    }

    protected boolean isPartOne() {
        return getMessageIdentifier() == PARTONE;
    }

    protected boolean isPartTwo() {
        return getMessageIdentifier() == PARTTWO;
    }
}
