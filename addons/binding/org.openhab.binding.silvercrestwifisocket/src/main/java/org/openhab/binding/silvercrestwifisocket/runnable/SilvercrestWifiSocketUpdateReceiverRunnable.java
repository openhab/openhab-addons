package org.openhab.binding.silvercrestwifisocket.runnable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.openhab.binding.silvercrestwifisocket.exceptions.PacketIntegrityErrorException;
import org.openhab.binding.silvercrestwifisocket.handler.SilvercrestWifiSocketMediator;
import org.openhab.binding.silvercrestwifisocket.utils.WifiSocketPacketConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Thread is responsible to receive all Wifi Socket messages and redirect them to
 * {@link SilvercrestWifiSocketMediator}.
 *
 * @author Jaime Vaz - Initial contribution
 *
 */
public class SilvercrestWifiSocketUpdateReceiverRunnable implements Runnable {

    private static final int TIMEOUT_TO_DATAGRAM_RECEPTION = 10000;

    private static final Logger LOG = LoggerFactory.getLogger(SilvercrestWifiSocketUpdateReceiverRunnable.class);

    private final DatagramSocket datagramSocket;
    private final SilvercrestWifiSocketMediator mediator;
    private final WifiSocketPacketConverter packetConverter = new WifiSocketPacketConverter();

    private boolean shutdown;

    /**
     * Constructor of the receiver runnable thread.
     *
     * @param mediator the {@link SilvercrestWifiSocketMediator}
     * @param listeningPort the listening UDP port
     * @throws SocketException is some problem occurs opening the socket.
     */
    public SilvercrestWifiSocketUpdateReceiverRunnable(final SilvercrestWifiSocketMediator mediator,
            final int listeningPort) throws SocketException {
        // Create a socket to listen on the port.
        this.datagramSocket = new DatagramSocket(listeningPort);
        this.datagramSocket.setSoTimeout(TIMEOUT_TO_DATAGRAM_RECEPTION);
        this.mediator = mediator;
        this.shutdown = false;
    }

    @Override
    public void run() {
        // Now loop forever, waiting to receive packets and redirect them to mediator.
        while (!this.shutdown) {

            // Create a buffer to read datagrams into. If a
            // packet is larger than this buffer, the
            // excess will simply be discarded!
            byte[] buffer = new byte[2048];

            // Create a packet to receive data into the buffer
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // Wait to receive a datagram
            try {
                this.datagramSocket.receive(packet);

                LOG.debug("Received packet from: {}. Will process the packet...", packet.getAddress().getHostAddress());

                // Do mediator something with it
                this.mediator.processReceivedPacket(this.packetConverter.decryptResponsePacket(packet));

                LOG.debug("Message delivered with success to mediator.");

            } catch (SocketTimeoutException e) {
                LOG.trace("Socket Timeout receiving packet.");
            } catch (IOException e) {
                LOG.debug("One exception has occurred: {} ", e.getMessage());
            } catch (PacketIntegrityErrorException e) {
                LOG.error("Packet has one integrity error: {}", e.getMessage());
            }
            // Reset the length of the packet before reusing it.
            packet.setLength(buffer.length);
        }
    }

    /**
     * Gracefully shutdown thread. Worst case takes TIMEOUT_TO_DATAGRAM_RECEPTION to shutdown.
     */
    public void shutdown() {
        this.shutdown = true;
    }

}
