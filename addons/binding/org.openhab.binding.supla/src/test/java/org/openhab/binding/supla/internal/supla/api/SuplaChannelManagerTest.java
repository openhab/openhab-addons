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
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;
import org.openhab.binding.supla.internal.supla.entities.SuplaFunction;
import org.openhab.binding.supla.internal.supla.entities.SuplaType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SuplaChannelManagerTest extends SuplaTest {
    private SuplaChannelManager manager;

    @Mock
    private HttpExecutor httpExecutor;
    @Mock
    private JsonMapper jsonMapper;

    private final SuplaChannel channel = new SuplaChannel(10, 11, "cpa", new SuplaType(1, "xxx"), new SuplaFunction(3, "yyy"));
    private final Request request = new Request("/channels/10");

    @Before
    public void init() {
        manager = new SuplaChannelManager(httpExecutor, jsonMapper);
    }

    @Test
    public void shouldRunPatchMethodToTurnOnChannel() {

        // given
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
        final JsonBody body = new JsonBody(ImmutableMap.<String, String>builder().put("action", "turn-off").build(), jsonMapper);

        when(httpExecutor.patch(request, body)).thenReturn(new Response(200, "resp"));

        // when
        manager.turnOff(channel);

        // then
        verify(httpExecutor).patch(request, body);
    }

    @Test
    public void shouldObtainChannelStatus() {

        // given
        final String response = "resp";
        when(httpExecutor.get(request)).thenReturn(new Response(200, response));
        when(jsonMapper.to(SuplaChannelStatus.class, response)).thenReturn(new SuplaChannelStatus(true, true));

        // when
        manager.obtainChannelStatus(channel);

        // then
        verify(httpExecutor).get(request);
        verify(jsonMapper).to(SuplaChannelStatus.class, response);
    }

    @Test
    public void shouldReturnEmptyIfThereIsNoChannelStatus() {

        // given
        when(httpExecutor.get(request)).thenReturn(new Response(503, "error"));

        // when
        final Optional<SuplaChannelStatus> status = manager.obtainChannelStatus(channel);

        // then
        verify(httpExecutor).get(request);
        assertThat(status).isEmpty();
    }
}
