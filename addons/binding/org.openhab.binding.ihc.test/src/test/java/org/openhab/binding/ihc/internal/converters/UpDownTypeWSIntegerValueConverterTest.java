package org.openhab.binding.ihc.internal.converters;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila
 */
public class UpDownTypeWSIntegerValueConverterTest {

    @Test
    public void TestOpen() {
        final boolean inverted = false;
        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);

        val = convertFromOHType(val, UpDownType.UP, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.UP, type);
    }

    @Test
    public void TestClosed() {
        final boolean inverted = false;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, UpDownType.DOWN, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.DOWN, type);
    }

    @Test
    public void TestOpenInverted() {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, UpDownType.UP, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.UP, type);

    }

    @Test
    public void TestClosedInverted() {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, UpDownType.DOWN, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        UpDownType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(UpDownType.DOWN, type);
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                UpDownType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private UpDownType convertFromResourceValue(WSIntegerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                UpDownType.class);
        return (UpDownType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
