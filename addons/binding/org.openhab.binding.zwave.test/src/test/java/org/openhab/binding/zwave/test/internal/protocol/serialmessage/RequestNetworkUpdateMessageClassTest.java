package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNetworkUpdateMessageClass;

public class RequestNetworkUpdateMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 0x01, 0x04, 0x00, 0x53, 0x01, (byte) 0xA9 };

        RequestNetworkUpdateMessageClass handler = new RequestNetworkUpdateMessageClass();
        SerialMessage msg = handler.doRequest();
        msg.setCallbackId(1);

        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
