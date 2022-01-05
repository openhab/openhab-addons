package org.openhab.binding.echonetlite.internal.protocol;

import org.junit.jupiter.api.Test;
import org.openhab.binding.echonetlite.internal.StateCodec;
import org.openhab.core.types.State;

import java.nio.ByteBuffer;

import static org.openhab.binding.echonetlite.internal.LangUtil.b;

class StateCodecTest
{
    @Test
    void shouldCalculateCumulativeOperatingTime()
    {
        final byte[] array = { b(0x42), b(0x00), b(0x00), b(0x1f), b(0x87) };
        final ByteBuffer buffer = ByteBuffer.wrap(array);
        final State state = StateCodec.OperatingTimeDecode.INSTANCE.decodeState(buffer);
        System.out.println(state);
    }
}