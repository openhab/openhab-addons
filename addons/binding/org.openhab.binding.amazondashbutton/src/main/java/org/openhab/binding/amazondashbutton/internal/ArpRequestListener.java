package org.openhab.binding.amazondashbutton.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpOperation;

/**
 *
 * @author Oliver Libutzki <oliver@libutzki.de> - Initial contribution
 *
 */
public class ArpRequestListener {

    public interface ArpRequestHandler {
        public void handleArpRequest(ArpPacket arpPacket);
    }

    private static final int READ_TIMEOUT = 10; // [ms]
    private static final int SNAPLEN = 65536; // [bytes]

    private final PcapNetworkInterface pcapNetworkInterface;

    private PcapHandle pcapHandle;

    public ArpRequestListener(PcapNetworkInterface pcapNetworkInterface) {
        this.pcapNetworkInterface = pcapNetworkInterface;
    }

    public void startListener(final ArpRequestHandler arpRequestHandler) {
        startListener(arpRequestHandler, null);
    }

    public void startListener(final ArpRequestHandler arpRequestHandler, final String macAddress) {
        if (pcapHandle != null) {
            if (pcapHandle.isOpen()) {
                throw new IllegalStateException("There is an open pcap handle.");
            } else {
                pcapHandle.close();
            }
        }
        try {
            pcapHandle = pcapNetworkInterface.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
            StringBuilder filterBuilder = new StringBuilder("arp");
            if (macAddress != null) {
                filterBuilder.append(" and ether src " + macAddress);
            }
            pcapHandle.setFilter(filterBuilder.toString(), BpfCompileMode.OPTIMIZE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    pcapHandle.loop(-1, new PacketListener() {

                        @Override
                        public void gotPacket(Packet packet) {
                            if (packet.contains(ArpPacket.class)) {
                                ArpPacket arpPacket = packet.get(ArpPacket.class);
                                if (arpPacket.getHeader().getOperation().equals(ArpOperation.REQUEST)) {
                                    arpRequestHandler.handleArpRequest(arpPacket);
                                }
                            }
                        }
                    });
                } finally {
                    if (pcapHandle != null && pcapHandle.isOpen()) {
                        pcapHandle.close();
                        pcapHandle = null;
                    }
                }
                return null;
            }
        });
    }

    public void stopListener() {
        if (pcapHandle != null) {
            if (pcapHandle.isOpen()) {
                try {
                    pcapHandle.breakLoop();
                } catch (NotOpenException e) {
                    // Just ignore
                }
            } else {
                pcapHandle = null;
            }
        }
    }
}
