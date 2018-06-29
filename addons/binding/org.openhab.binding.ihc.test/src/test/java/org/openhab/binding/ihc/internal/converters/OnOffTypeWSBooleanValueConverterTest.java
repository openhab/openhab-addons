package org.openhab.binding.ihc.internal.converters;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila
 */
public class OnOffTypeWSBooleanValueConverterTest {

    @Test
    public void TestOn() {
        final boolean inverted = false;
        WSBooleanValue val = new WSBooleanValue(12345);

        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(true, val.isValue());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void TestOff() {
        final boolean inverted = false;

        WSBooleanValue val = new WSBooleanValue(12345);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(false, val.isValue());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.OFF, type);
    }

    @Test
    public void TestOnInverted() {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345);
        val = convertFromOHType(val, OnOffType.ON, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(false, val.isValue());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.ON, type);
    }

    @Test
    public void TestOffInverted() {
        final boolean inverted = true;

        WSBooleanValue val = new WSBooleanValue(12345);
        val = convertFromOHType(val, OnOffType.OFF, new ConverterAdditionalInfo(null, inverted));
        assertEquals(12345, val.getResourceID());
        assertEquals(true, val.isValue());

        OnOffType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, inverted));
        assertEquals(OnOffType.OFF, type);
    }

    private WSBooleanValue convertFromOHType(WSBooleanValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (WSBooleanValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private OnOffType convertFromResourceValue(WSBooleanValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                OnOffType.class);
        return (OnOffType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
