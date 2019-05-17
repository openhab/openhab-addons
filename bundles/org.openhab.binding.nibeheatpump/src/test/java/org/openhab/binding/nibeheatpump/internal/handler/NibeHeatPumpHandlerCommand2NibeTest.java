package org.openhab.binding.nibeheatpump.internal.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;
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
public class NibeHeatPumpHandlerCommand2NibeTest {
    private NibeHeatPumpHandler product; // the class under test
    private Method m;
    private static String METHOD_NAME = "convertCommandToNibeValue";
    private Class[] parameterTypes;
    private Object[] parameters;

    private int fCoilAddress;

    private Command fCommand;

    private int fExpected;

    @Parameterized.Parameters(name = "{index}: f({0}, {1})={2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { 47028, new DecimalType("-1"), (byte)0xFF },
                { 43009, new DecimalType("28.7"), 0x011F },
                { 40004, new DecimalType("-0.1"), (short)0xFFFF },
                { 47418, new DecimalType("75"), 0x004B },
                { 43514, new DecimalType("7"), 0x0007 },
                { 47291, new DecimalType("65535"), 0xFFFF },
                { 43230, new DecimalType("429496729.5"), 0xFFFFFFFF },
                { 43614, new DecimalType("4294967295"), 0xFFFFFFFF },
        });
    }

    public NibeHeatPumpHandlerCommand2NibeTest(final int coilAddress, final Command command, final int expected) {
        this.fCoilAddress = coilAddress;
        this.fCommand = command;
        this.fExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        product = new NibeHeatPumpHandler(null, PumpModel.F1X45);
        parameterTypes = new Class[2];
        parameterTypes[0] = VariableInformation.class;
        parameterTypes[1] = Command.class;
        m = product.getClass().getDeclaredMethod(METHOD_NAME, parameterTypes);
        m.setAccessible(true);
        parameters = new Object[2];
    }

    @Test
    public void convertNibeValueToStateTest() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, fCoilAddress);
        parameters[0] = varInfo;
        parameters[1] = fCommand;
        int value = (int) m.invoke(product, parameters);

        assertEquals(fExpected, value);
    }
}
