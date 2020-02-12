package org.openhab.binding.revogismartstripcontrol.internal.api;

import org.junit.Test;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SwitchServiceTest {

    private UdpSenderService udpSenderService = mock(UdpSenderService.class);
    private SwitchService switchService = new SwitchService(udpSenderService);

    @Test
    public void getStatusSuccesfully() {
        // given
        ArrayList<String> response = new ArrayList<>(Collections.singleton("V3{\"response\":20,\"code\":200}"));
        when(udpSenderService.broadcastUpdDatagram("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}")).thenReturn(response);

        // when
        SwitchResponse switchResponse = switchService.switchPort("serial", 1, 1);

        // then
        assertEquals(new SwitchResponse(20, 200), switchResponse);
    }

    @Test
    public void invalidUdpResponse() {
        // given
        ArrayList<String> response = new ArrayList<>(Collections.singleton("something invalid"));
        when(udpSenderService.broadcastUpdDatagram("V3{\"sn\":\"serial\", \"cmd\": 20, \"port\": 1, \"state\": 1}")).thenReturn(response);

        // when
        SwitchResponse switchResponse = switchService.switchPort("serial", 1, 1);

        // then
        assertEquals(new SwitchResponse(0, 503), switchResponse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getExceptionOnWrongState() {
        switchService.switchPort("serial", 1, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getExceptionOnWrongPort() {
        switchService.switchPort("serial", -1, 1);
    }
}