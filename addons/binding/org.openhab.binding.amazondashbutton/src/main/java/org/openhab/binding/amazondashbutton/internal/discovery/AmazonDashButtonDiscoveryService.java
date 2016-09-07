package org.openhab.binding.amazondashbutton.internal.discovery;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.DASH_BUTTON_THING_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmazonDashButtonDiscoveryService extends AbstractDiscoveryService {

    private static final int READ_TIMEOUT = 10; // [ms]
    private static final int SNAPLEN = 65536; // [bytes]
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private static final Logger logger = LoggerFactory.getLogger(AmazonDashButtonDiscoveryService.class);

    public AmazonDashButtonDiscoveryService() {
        super(Collections.singleton(DASH_BUTTON_THING_TYPE), DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        try {
            List<PcapNetworkInterface> pcapNetworkInterfaces = Pcaps.findAllDevs();
            PacketListener listener = getPacketListener();
            for (PcapNetworkInterface pcapNetworkInterface : pcapNetworkInterfaces) {
                PcapHandle handle = pcapNetworkInterface.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
                try {
                    handle.setFilter("arp", BpfCompileMode.OPTIMIZE);
                    handle.loop(-1, listener);
                } finally {
                    if (handle != null && handle.isOpen()) {
                        handle.close();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while scanning for Amazon Dash buttons", e);
        }

    }

    private PacketListener getPacketListener() {
        return new PacketListener() {
            @Override
            public void gotPacket(Packet packet) {
                if (packet.contains(ArpPacket.class)) {
                    ArpPacket arp = packet.get(ArpPacket.class);
                    if (arp.getHeader().getOperation().equals(ArpOperation.REQUEST)) {
                        String hardwareAddress = arp.getHeader().getSrcHardwareAddr().toString();
                        if (isAmazonVendor(hardwareAddress)) {
                            ThingUID dashButtonThing = new ThingUID(DASH_BUTTON_THING_TYPE, hardwareAddress);
                            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(dashButtonThing)
                                    .withLabel("Dash Button").build();
                            thingDiscovered(discoveryResult);
                            System.out.println("RESOLVED: " + arp.getHeader().getSrcHardwareAddr() + " --> "
                                    + arp.getHeader().getSrcProtocolAddr());

                            System.out.println(packet);
                        }
                    }
                }
            }
        };
    }

    private static boolean isAmazonVendor(String macAddress) {
        List<String> possibleVendorPrefixes = new ArrayList<String>();
        possibleVendorPrefixes.add("44:65:0D");
        possibleVendorPrefixes.add("50:F5:DA");
        possibleVendorPrefixes.add("84:D6:D0");
        possibleVendorPrefixes.add("34:D2:70");
        possibleVendorPrefixes.add("F0:D2:F1");
        possibleVendorPrefixes.add("88:71:E5");
        possibleVendorPrefixes.add("74:C2:46");
        possibleVendorPrefixes.add("F0:27:2D");
        possibleVendorPrefixes.add("0C:47:C9");
        possibleVendorPrefixes.add("A0:02:DC");
        possibleVendorPrefixes.add("74:75:48");
        possibleVendorPrefixes.add("AC:63:BE");

        String vendorPrefix = macAddress.substring(0, 8).toUpperCase();
        return possibleVendorPrefixes.contains(vendorPrefix);
    }

}
