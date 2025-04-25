package org.smslib.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Extracted from SMSLib
 */
@NonNullByDefault
public class OutboundBinaryMessage extends OutboundMessage {
    private static final long serialVersionUID = 1L;

    public OutboundBinaryMessage() {
    }

    public OutboundBinaryMessage(MsIsdn originatorAddress, MsIsdn recipientAddress, byte[] data) {
        super(originatorAddress, recipientAddress, new Payload(data));
        setEncoding(Encoding.Enc8);
    }
}
