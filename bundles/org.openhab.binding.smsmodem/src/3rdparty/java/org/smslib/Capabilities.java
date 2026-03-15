package org.smslib;

import java.util.BitSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted from SMSLib
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
