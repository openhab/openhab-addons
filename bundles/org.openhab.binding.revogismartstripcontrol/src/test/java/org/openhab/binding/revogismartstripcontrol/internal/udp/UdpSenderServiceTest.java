package org.openhab.binding.revogismartstripcontrol.internal.udp;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class UdpSenderServiceTest {

    private final DatagramSocketWrapper datagramSocketWrapper = mock(DatagramSocketWrapper.class);

    private final UdpSenderService udpSenderService = new UdpSenderService(datagramSocketWrapper);

    private int numberOfInterfaces = 0;

    @Before
    public void setUp() throws Exception {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp()) {
                numberOfInterfaces += (int) networkInterface.getInterfaceAddresses().stream()
                        .filter(interfaceAddress -> interfaceAddress.getBroadcast() != null)
                        .map(InterfaceAddress::getBroadcast).count();
            }
        }
    }

    @Test
    public void testTimeout() throws IOException {
        // given
        doThrow(new SocketTimeoutException()).when(datagramSocketWrapper).receiveAnswer(any());

        // when
        List<String> list = udpSenderService.broadcastUpdDatagram("send something");

        // then
        assertThat(list.isEmpty(), Matchers.is(true));
        verify(datagramSocketWrapper, times(numberOfInterfaces * 2)).receiveAnswer(any());
    }

    @Test
    public void testOneAnswer() throws IOException {
        // given
        byte[] receivedBuf = "valid answer".getBytes();
        doAnswer(new Answer<DatagramSocket>() {
            @Override
            public DatagramSocket answer(final InvocationOnMock invocation) throws Throwable {
                DatagramPacket datagramPacket = new DatagramPacket(receivedBuf, receivedBuf.length);
                return null;
            }
        }).doThrow(new SocketTimeoutException()).when(datagramSocketWrapper).receiveAnswer(any());
        when(datagramSocketWrapper.receiveAnswer(any(DatagramPacket.class))).thenReturn()
        every { datagramSocketWrapper.receiveAnswer(any()) } answers {
            firstArg<DatagramPacket>().data = receivedBuf
        } andThenThrows SocketTimeoutException().fillInStackTrace()


        // when
        List<String> list = udpSenderService.broadcastUpdDatagram("send something");

        // then
        assertThat(list).contains("valid answer")
        verify(exactly = 1 + 2 * numberOfInterfaces) {datagramSocketWrapper.receiveAnswer(any())}
    }*/


}