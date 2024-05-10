package org.openhab.binding.myuplink.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.myuplink.internal.MyUplinkBindingConstants;
import org.openhab.binding.myuplink.internal.model.ChannelType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@ExtendWith(MockitoExtension.class)
public class MyUplinkDynamicThingHandlerTest {

    @Mock
    private Thing thing;
    private MyUplinkDynamicThingHandler handler;

    private final String testChannelDataTemperature = """
            {"category":"NIBEF VVM 320 E","parameterId":"40121","parameterName":"Add. heat (BT63)","parameterUnit":"°C","writable":false,"timestamp":"2024-05-10T05:35:50+00:00","value":39.0,"strVal":"39Â°C","smartHomeCategories":[],"minValue":null,"maxValue":null,"stepValue":1.0,"enumValues":[],"scaleValue":"0.1","zoneId":null}
            """;

    @BeforeEach
    public void initMock() {
        when(thing.getUID()).thenReturn(new ThingUID(MyUplinkBindingConstants.BINDING_ID, "test"));
        handler = new MyUplinkDynamicThingHandlerImpl(thing);
    }

    @Test
    public void testFromJsonDataTemperature() {
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(testChannelDataTemperature, JsonObject.class);
        json = json == null ? new JsonObject() : json;

        Channel result = handler.getOrCreateChannel("4711", json);
        assertThat(result.getAcceptedItemType(), is(ChannelType.TEMPERATURE.getAcceptedType()));
    }
}
