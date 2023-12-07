package org.openhab.binding.tacmi.internal.coe;

import org.openhab.binding.tacmi.internal.message.MessageType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TACmiHandlerTest {

    @DataProvider(name = "outputIndices")
    public Object[][] provideOutputIndices() {
        return new Object[][] { { 1, true, 0 }, { 2, true, 1 }, { 3, true, 2 }, { 4, true, 3 }, { 5, true, 0 },
                { 32, true, 3 }, { 1, false, 0 }, { 2, false, 1 }, { 16, false, 15 }, { 17, false, 0 },
                { 32, false, 15 } };
    }

    @Test(dataProvider = "outputIndices")
    public void testGetOutputIndexHappy(int aIndex, boolean aAnalog, int aExpected) {
        final Thing thing = new ThingImpl(new ThingTypeUID("test:test"), "test");
        final TACmiHandler sut = new TACmiHandler(thing);

        final int outputIdx = sut.getOutputIndex(aIndex, aAnalog);

        Assert.assertEquals(outputIdx, aExpected);
    }

    // This is actual target data
    @DataProvider(name = "podIds")
    public Object[][] providePodIds() {
        return new Object[][] { { MessageType.ANALOG, 1, (byte) 1 }, { MessageType.ANALOG, 2, (byte) 1 },
                { MessageType.ANALOG, 3, (byte) 1 }, { MessageType.ANALOG, 4, (byte) 1 },
                { MessageType.ANALOG, 5, (byte) 2 }, { MessageType.ANALOG, 32, (byte) 8 },
                { MessageType.DIGITAL, 1, (byte) 0 }, { MessageType.DIGITAL, 16, (byte) 0 },
                { MessageType.DIGITAL, 17, (byte) 9 }, { MessageType.DIGITAL, 32, (byte) 9 } };
    }

    @Test(dataProvider = "podIds")
    public void testGetPodIdHappy(final MessageType aMT, final int aOutput, final byte aExpected) {
        final Thing thing = new ThingImpl(new ThingTypeUID("test:test"), "test");
        final TACmiHandler sut = new TACmiHandler(thing);

        final byte podId = sut.getPodId(aMT, aOutput);

        Assert.assertEquals(podId, aExpected);
    }
}
