package org.openhab.binding.supla.internal.supla.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openhab.binding.supla.SuplaTest;
import org.openhab.binding.supla.internal.http.HttpExecutor;
import org.openhab.binding.supla.internal.http.JsonBody;
import org.openhab.binding.supla.internal.http.Request;
import org.openhab.binding.supla.internal.http.Response;
import org.openhab.binding.supla.internal.mappers.JsonMapper;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaFunction;
import org.openhab.binding.supla.internal.supla.entities.SuplaType;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SuplaChannelManagerTest extends SuplaTest {
    private SuplaChannelManager manager;

    @Mock
    private HttpExecutor httpExecutor;
    @Mock
    private JsonMapper jsonMapper;

    @Before
    public void init() {
        manager = new SuplaChannelManager(httpExecutor, jsonMapper);
    }

    @Test
    public void shouldRunPatchMethodToTurnOnChannel() {

        // given
        final SuplaChannel channel = new SuplaChannel(10, 11, "cpa", new SuplaType(1, "xxx"), new SuplaFunction(3, "yyy"));
        final Request request = new Request("/channels/10");
        final JsonBody body = new JsonBody(ImmutableMap.<String, String>builder().put("action", "turn-on").build(), jsonMapper);

        when(httpExecutor.patch(request, body)).thenReturn(new Response(200, "resp"));

        // when
        manager.turnOn(channel);

        // then
        verify(httpExecutor).patch(request, body);
    }

    @Test
    public void shouldRunPatchMethodToTurnOffChannel() {

        // given
        final SuplaChannel channel = new SuplaChannel(10, 11, "cpa", new SuplaType(1, "xxx"), new SuplaFunction(3, "yyy"));
        final Request request = new Request("/channels/10");
        final JsonBody body = new JsonBody(ImmutableMap.<String, String>builder().put("action", "turn-off").build(), jsonMapper);

        when(httpExecutor.patch(request, body)).thenReturn(new Response(200, "resp"));

        // when
        manager.turnOff(channel);

        // then
        verify(httpExecutor).patch(request, body);
    }
}
