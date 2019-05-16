package org.openhab.binding.nibeheatpump.internal.handler;

import static org.junit.Assert.*;

import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NibeHeatPumpHandlerTest {
    private NibeHeatPumpHandler product; // the class under test
    private Method m;
    private static String METHOD_NAME = "convertNibeValueToState";
    private Class[] parameterTypes;
    private Object[] parameters;


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
    public void convertNibeValueToStateS8Test() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 47028);

        parameters[0] = varInfo;
        parameters[1] = (int)0xFF;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("-1", state.format("%d"));
    }

    @Test
    public void convertNibeValueToStateS16Factor10Test() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 43009);

        parameters[0] = varInfo;
        parameters[1] = (int)0x011F;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("28.7", state.format("%.1f"));
    }

    @Test
    public void convertNibeValueToStateS16Factor10NegativeTest() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 40004);

        parameters[0] = varInfo;
        parameters[1] = (int)0xFFFF;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("-0.1", state.format("%.1f"));
    }

    @Test
    public void convertNibeValueToStateS32NegativeTest() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 43416);

        parameters[0] = varInfo;
        parameters[1] = (int)0xFFFFFFFF;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("-1", state.format("%d"));
    }

    @Test
    public void convertNibeValueToStateS32Test() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 43416);

        parameters[0] = varInfo;
        parameters[1] = (int)0x1B1B;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("6939", state.format("%d"));
    }

    @Test
    public void convertNibeValueToStateU8Test() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 47418);

        parameters[0] = varInfo;
        parameters[1] = (int)0x004B;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("75", state.format("%d"));
    }

    @Test
    public void convertNibeValueToStateU8PositiveTest() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 43514);

        parameters[0] = varInfo;
        parameters[1] = (int)0x0007;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("7", state.format("%d"));
    }

    @Test
    public void convertNibeValueToStateU16Test() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 47291);

        parameters[0] = varInfo;
        parameters[1] = (int)0xFFFF;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("65535", state.format("%d"));
    }

    @Test
    public void convertNibeValueToStateU32Test() throws InvocationTargetException, IllegalAccessException {
        VariableInformation varInfo = VariableInformation.getVariableInfo(PumpModel.F1X45, 43230);

        parameters[0] = varInfo;
        parameters[1] = (int)0xFFFFFFFF;
        parameters[2] = "Number";
        State state = (State) m.invoke(product, parameters);

        assertEquals("429496729.5", state.format("%.1f"));
    }
}
