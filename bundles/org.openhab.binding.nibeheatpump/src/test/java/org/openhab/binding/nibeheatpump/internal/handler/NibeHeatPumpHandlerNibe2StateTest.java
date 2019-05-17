package org.openhab.binding.nibeheatpump.internal.handler;

import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NibeHeatPumpHandlerNibe2StateTest {
    private NibeHeatPumpHandler product; // the class under test
    private Method m;
    private static String METHOD_NAME = "convertNibeValueToState";
    private Class[] parameterTypes;
    private Object[] parameters;

    private int fCoilAddress;

    private int fValue;

    private String fFormat;

    private String fExpected;

    @Parameterized.Parameters(name = "{index}: f({0}, {1})={3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { 47028, 0xFF, "%d", "-1" },
                { 47028, (byte)0xFF, "%d", "-1" },
                { 43009, 0x011F, "%.1f", "28.7" },
                { 40004, 0xFFFF, "%.1f", "-0.1" },
                { 40004, (short)0xFFFF, "%.1f", "-0.1" },
                { 43416, 0xFFFFFFFF, "%d", "-1" },
                { 47418, 0x004B, "%d", "75" },
                { 43514, 0x0007, "%d", "7" },
                { 47291, 0xFFFF, "%d", "65535" },
                { 43230, 0xFFFFFFFF, "%.1f", "429496729.5" },
                { 43614, 0xFFFFFFFF, "%d", "4294967295" }
        });
    }

    public NibeHeatPumpHandlerNibe2StateTest(final int coilAddress, final int value, final String format, final String expected) {
        this.fCoilAddress = coilAddress;
        this.fValue = value;
        this.fFormat = format;
        this.fExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        product = new NibeHeatPumpHandler(null, PumpModel.F1X45);
        parameterTypes = new Class[3];
        parameterTypes[0] = VariableInformation.class;
        parameterTypes[1] = int.class;
        parameterTypes[2] = String.class;
        m = product.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
        m.setAccessible(true);
        parameters = new Object[3];
    }

    @Test
    public void convertNibeValueToStateTest() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, fCoilAddress);
        parameters[0] = varInfo;
        parameters[1] = fValue;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals(fExpected, state.format(fFormat));
    }
}
