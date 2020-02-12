package org.openhab.binding.revogismartstripcontrol.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.revogismartstripcontrol.internal.udp.UdpSenderService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class DiscoveryServiceTest {

    private UdpSenderService udpSenderService = mock(UdpSenderService.class);
    private DiscoveryService discoveryService = new DiscoveryService(udpSenderService);

    @Test
    public void discoverSmartStripSuccesfully() {
        // given
        DiscoveryResponse discoveryResponse = new DiscoveryResponse("1234", "reg", "sak", "Strip", "mac", "5.11");
        List<String> discoveryString = Collections.singletonList("{\"response\":0,\"data\":{\"sn\":\"1234\",\"regid\":\"reg\",\"sak\":\"sak\",\"name\":\"Strip\",\"mac\":\"mac\",\"ver\":\"5.11\"}}");
        when(udpSenderService.broadcastUpdDatagram("00sw=all,,,;")).thenReturn(discoveryString);

        // when
        List<DiscoveryResponse> discoverSmartStrips = discoveryService.discoverSmartStrips();

        // then
        assertThat(discoverSmartStrips.size(), equalTo(1));
        assertThat(discoverSmartStrips.get(0), equalTo(discoveryResponse));
    }

    @Test
    public void invalidUdpResponse() {
        // given
        List<String> discoveryString = Collections.singletonList("something invalid");
        when(udpSenderService.broadcastUpdDatagram("00sw=all,,,;")).thenReturn(discoveryString);

        // when
        List<DiscoveryResponse> discoverSmartStrips = discoveryService.discoverSmartStrips();

        // then
        assertThat(discoverSmartStrips.isEmpty(), is(true));
    }
}