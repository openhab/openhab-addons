package org.openhab.binding.amazondashbutton.internal.discovery;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.amazondashbutton.internal.ArpRequestTracker;
import org.openhab.binding.amazondashbutton.internal.ArpRequestTracker.ArpRequestHandler;
import org.openhab.binding.amazondashbutton.internal.PcapUtil;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.ArpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AmazonDashButtonDiscoveryService} is responsible for discovering Amazon Dash Buttons. It does so by
 * capturing ARP requests from all available network devices.
 *
 * While scanning the user has to press the button in order to send an ARP request packet. The
 * {@link AmazonDashButtonDiscoveryService} captures this packet and checks the device'sMAC address which sent the
 * request against a static list of vendor prefixes ({@link #isAmazonVendor(String)}).
 *
 * If an Amazon MAC address is detected a {@link DiscoveryResult} is built and passed to
 * {@link #thingDiscovered(DiscoveryResult)}.
 *
 * @author Oliver Libutzki - Initial contribution
 *
 */
public class AmazonDashButtonDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(AmazonDashButtonDiscoveryService.class);

    private List<ArpRequestTracker> arpRequestListeners = new CopyOnWriteArrayList<ArpRequestTracker>();

    public AmazonDashButtonDiscoveryService() {
        super(Collections.singleton(DASH_BUTTON_THING_TYPE), DISCOVER_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {

        List<PcapNetworkInterface> pcapNetworkInterfaces = PcapUtil.getAllNetworkInterfaces();

        for (final PcapNetworkInterface pcapNetworkInterface : pcapNetworkInterfaces) {
            ArpRequestTracker arpRequestListener = new ArpRequestTracker(pcapNetworkInterface);
            arpRequestListeners.add(arpRequestListener);
            arpRequestListener.startCapturing(new ArpRequestHandler() {

                @Override
                public void handleArpRequest(ArpPacket arpPacket) {
                    String macAdress = arpPacket.getHeader().getSrcHardwareAddr().toString();
                    if (isAmazonVendor(macAdress)) {
                        ThingUID dashButtonThing = new ThingUID(DASH_BUTTON_THING_TYPE, macAdress.replace(":", "-"));
                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(dashButtonThing)
                                .withLabel("Dash Button").withRepresentationProperty(macAdress)
                                .withProperty(PROPERTY_MAC_ADDRESS, macAdress)
                                .withProperty(PROPERTY_NETWORK_INTERFACE_NAME, pcapNetworkInterface.getName())
                                .withProperty(PROPERTY_PACKET_INTERVAL, 5000).build();
                        thingDiscovered(discoveryResult);
                    }
                }
            });
        }

    }

    @Override
    protected synchronized void stopScan() {
        for (ArpRequestTracker arpRequestListener : arpRequestListeners) {
            arpRequestListener.stopCapturing();
        }
        arpRequestListeners.clear();
        super.stopScan();
    }

    private boolean isAmazonVendor(String macAddress) {
        List<String> possibleVendorPrefixes = new ArrayList<String>();
        possibleVendorPrefixes.add("44:65:0D");
        possibleVendorPrefixes.add("50:F5:DA");
        possibleVendorPrefixes.add("84:D6:D0");
        possibleVendorPrefixes.add("34:D2:70");
        possibleVendorPrefixes.add("F0:D2:F1");
        possibleVendorPrefixes.add("88:71:E5");
        possibleVendorPrefixes.add("74:C2:46");
        // This is an Amazon MAC address, but it has been used by my Fire TV...
        // possibleVendorPrefixes.add("F0:27:2D");
        possibleVendorPrefixes.add("0C:47:C9");
        possibleVendorPrefixes.add("A0:02:DC");
        possibleVendorPrefixes.add("74:75:48");
        possibleVendorPrefixes.add("AC:63:BE");

        String vendorPrefix = macAddress.substring(0, 8).toUpperCase();
        return possibleVendorPrefixes.contains(vendorPrefix);
    }

}
