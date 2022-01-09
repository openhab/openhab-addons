package org.openhab.binding.lgthinq.handler;


import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lgthinq.internal.LGThinqBindingConstants;
import org.openhab.binding.lgthinq.internal.LGThinqConfiguration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BridgeHandlerTest {

    @org.junit.jupiter.api.Test
    void initialize() {
        Bridge fakeThing = mock(Bridge.class);
        LGThinqBindingConstants.THINQ_CONNECTION_DATA_FILE = "/tmp/token.json";
        BridgeHandler b = new BridgeHandler(fakeThing);
        BridgeHandler spyBridge = spy(b);
        doReturn(new LGThinqConfiguration("nemer.daud@gmail.com", "@Apto94&J4V4", "BR", "pt-BR"))
                .when(spyBridge).getConfigAs(any(Class.class));
        spyBridge.initialize();
    }
}