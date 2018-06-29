package org.openhab.binding.ihc.internal.converters;

import static org.junit.Assert.assertEquals;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Type;
import org.junit.Test;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila
 */
public class DecimalTypeWSIntegerValueConverterTest {

    @Test
    public void Test() {
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);

        val = convertFromOHType(val, new DecimalType(2), new ConverterAdditionalInfo(null, false));
        assertEquals(12345, val.getResourceID());
        assertEquals(2, val.getInteger());

        DecimalType type = convertFromResourceValue(val, new ConverterAdditionalInfo(null, false));
        assertEquals(new DecimalType(2), type);
    }

    @Test(expected = NumberFormatException.class)
    public void TestMinExceed() {
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);
        val = convertFromOHType(val, new DecimalType(-101.5), new ConverterAdditionalInfo(null, false));
    }

    @Test(expected = NumberFormatException.class)
    public void TestMaxExceed() {
        WSIntegerValue val = new WSIntegerValue(12345, 0, -100, 100);
        val = convertFromOHType(val, new DecimalType(101.5), new ConverterAdditionalInfo(null, false));
    }

    private WSIntegerValue convertFromOHType(WSIntegerValue IHCvalue, Type OHval,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (WSIntegerValue) converter.convertFromOHType(OHval, IHCvalue, converterAdditionalInfo);
    }

    private DecimalType convertFromResourceValue(WSIntegerValue IHCvalue,
            ConverterAdditionalInfo converterAdditionalInfo) {
        Converter<WSResourceValue, Type> converter = ConverterFactory.getInstance().getConverter(IHCvalue.getClass(),
                DecimalType.class);
        return (DecimalType) converter.convertFromResourceValue(IHCvalue, converterAdditionalInfo);
    }
}
