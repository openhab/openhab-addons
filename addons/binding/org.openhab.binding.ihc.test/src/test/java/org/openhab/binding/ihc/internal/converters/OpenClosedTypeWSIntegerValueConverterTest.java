package org.openhab.binding.ihc.internal.converters;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila
 */
public class OpenClosedTypeWSIntegerValueConverterTest {

    @Test
    public void TestOpen() {
        final boolean inverted = false;
        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);

        val = convertFromOHType(val, OpenClosedType.OPEN, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OpenClosedType.OPEN, type);
    }

    @Test
    public void TestClosed() {
        final boolean inverted = false;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OpenClosedType.CLOSED, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OpenClosedType.CLOSED, type);
    }

    @Test
    public void TestOpenInverted() {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OpenClosedType.OPEN, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OpenClosedType.OPEN, type);

    }

    @Test
    public void TestClosedInverted() {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OpenClosedType.CLOSED, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        OpenClosedType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OpenClosedType.CLOSED, type);
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OpenClosedType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private OpenClosedType convertFromResourceValue(WSIntegerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OpenClosedType.class);
        return (OpenClosedType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
