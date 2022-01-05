package org.openhab.binding.echonetlite.internal.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.echonetlite.internal.LangUtil.constantToVariable;

class LangUtilTest {

    @Test
    void shouldConvertConstantToVariable() {
        assertEquals("operationStatus", constantToVariable("OPERATION_STATUS"));
    }
}