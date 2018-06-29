package org.openhab.binding.ihc.internal.converters;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila
 */
public class OnOffTypeWSIntegerValueConverterTest {

    @Test
    public void TestOn() {
        final boolean inverted = false;
        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);

        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void TestOff() {
        final boolean inverted = false;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.OFF, type);
    }

    @Test
    public void TestOnInverted() {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(0, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.ON, type);

    }

    @Test
    public void TestOffInverted() {
        final boolean inverted = true;

        WSIntegerValue val = new WSIntegerValue(12345, 0, 0, 1);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(1, val.getInteger());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.OFF, type);
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private OnOffType convertFromResourceValue(WSIntegerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (OnOffType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
