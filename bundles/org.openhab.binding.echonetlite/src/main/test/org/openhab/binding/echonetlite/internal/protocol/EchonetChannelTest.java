package org.openhab.binding.echonetlite.internal.protocol;

import org.junit.jupiter.api.Test;
import org.openhab.binding.echonetlite.internal.EchonetChannel;

import java.io.IOException;

class EchonetChannelTest
{
    @Test
    void name() throws IOException
    {
        EchonetChannel ch1 = new EchonetChannel();
        ch1.close();
        EchonetChannel ch2 = new EchonetChannel();
    }
}