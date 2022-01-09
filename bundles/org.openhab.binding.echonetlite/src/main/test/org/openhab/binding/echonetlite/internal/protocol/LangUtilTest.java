package org.openhab.binding.echonetlite.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.echonetlite.internal.LangUtil.constantToVariable;

import org.junit.jupiter.api.Test;

class LangUtilTest {

    @Test
    void shouldConvertConstantToVariable() {
        assertEquals("operationStatus", constantToVariable("OPERATION_STATUS"));
    }
}