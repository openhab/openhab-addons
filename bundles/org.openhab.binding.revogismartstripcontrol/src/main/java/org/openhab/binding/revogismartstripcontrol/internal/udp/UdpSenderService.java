package org.openhab.binding.revogismartstripcontrol.internal.udp;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UdpSenderService {

    private static final int MAX_TIMEOUT_COUNT = 2;
    private static final int REVOGI_PORT = 8888;

    private final Logger logger = LoggerFactory.getLogger(UdpSenderService.class);
    private final DatagramSocketWrapper datagramSocketWrapper;

    public UdpSenderService(DatagramSocketWrapper datagramSocketWrapper) {
        this.datagramSocketWrapper = datagramSocketWrapper;
    }

    public List<String> broadcastUpdDatagram(String content) {
        List<InetAddress> broadcastAddresses = new ArrayList<>();
        try {
            broadcastAddresses = getBroadcastAddresses();
        } catch (SocketException e) {
            logger.warn("Could not find broadcast addresse, got socket error {}", e.getMessage(), e);
        }
        return broadcastAddresses.stream()
                .map( address -> sendBroadcastMessage(content, address))
                .flatMap(Collection::stream)
                .collect(toList());

    }

    private List<String> sendBroadcastMessage(String content, InetAddress broadcastAddress) {
        logger.info("Using address {}", broadcastAddress);
        byte[] buf = content.getBytes(Charset.defaultCharset());
        DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, REVOGI_PORT);
        List<String> responses = new ArrayList<>();
        try {
            datagramSocketWrapper.initSocket();
            datagramSocketWrapper.sendPacket(packet);
            responses = receiveResponses();
            datagramSocketWrapper.closeSocket();
        } catch (IOException e) {
            logger.warn("Error sending message or reading anwser {}", e.getMessage(), e);
        }
        return responses;
    }

    private List<String> receiveResponses() throws IOException {
        List<String> list = new ArrayList<>();
        int timeoutCounter = 0;
        while (timeoutCounter < MAX_TIMEOUT_COUNT) {
            byte[] receivedBuf = new byte[512];
            DatagramPacket answer = new DatagramPacket(receivedBuf, receivedBuf.length);
            try {
                datagramSocketWrapper.receiveAnswer(answer);
            }
            catch (SocketTimeoutException e) {
                timeoutCounter++;
                logger.warn("Socket receive time no. {}", timeoutCounter);
                try {
                    TimeUnit.MILLISECONDS.sleep(timeoutCounter * 800L);
                } catch (InterruptedException ex) {
                    logger.warn("Interrupted sleep");
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            if (answer.getLength() > 0) {
                list.add(new String(answer.getData(), 0, answer.getLength()));
            }
        }

        return list;
    }

    private List<InetAddress> getBroadcastAddresses() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        List<InetAddress> broadcastAdresses = new ArrayList<>();
        while (networkInterfaces.hasMoreElements()) {
            broadcastAdresses.addAll(findInterfaceBroadcastAddresses(networkInterfaces));
        }
        return broadcastAdresses;
    }

    private List<InetAddress> findInterfaceBroadcastAddresses(final Enumeration<NetworkInterface> networkInterfaces) throws SocketException {
        NetworkInterface anInterface = networkInterfaces.nextElement();
        if (anInterface.isUp()) {
            List<InetAddress> addresses = anInterface.getInterfaceAddresses().stream()
                    .filter(address -> address.getBroadcast() != null)
                    .map(InterfaceAddress::getBroadcast)
                    .collect(toList());
            if (!addresses.isEmpty()) {
                return addresses;
            }
        }
        return Collections.emptyList();
    }

}