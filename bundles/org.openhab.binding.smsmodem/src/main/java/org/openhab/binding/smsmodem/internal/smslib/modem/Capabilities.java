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
package org.openhab.binding.smsmodem.internal.smslib.modem;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Extracted from SMSLib
 *
 * @author Gwendal ROULLEAU - Initial contribution,
 *         extracted from SMSLib, no modification
 */
@NonNullByDefault
public class Capabilities {
    BitSet caps = new BitSet();

    public enum Caps {
        CanSendMessage,
        CanSendBinaryMessage,
        CanSendUnicodeMessage,
        CanSendWapMessage,
        CanSendFlashMessage,
        CanSendPortInfo,
        CanSetSenderId,
        CanSplitMessages,
        CanRequestDeliveryStatus,
        CanQueryDeliveryStatus,
        CanQueryCreditBalance,
        CanQueryCoverage,
        CanSetValidityPeriod
    }

    public void set(Caps c) {
        this.caps.set(c.ordinal());
    }

    public BitSet getCapabilities() {
        return (BitSet) this.caps.clone();
    }

    @Override
    public String toString() {
        BitSet bs = (BitSet) getCapabilities().clone();
        StringBuffer b = new StringBuffer();
        for (Caps c : Caps.values()) {
            b.append(String.format("%-30s : ", c.toString()));
            b.append(bs.get(c.ordinal()) ? "YES" : "NO");
            b.append("\n");
        }
        return b.toString();
    }
}
