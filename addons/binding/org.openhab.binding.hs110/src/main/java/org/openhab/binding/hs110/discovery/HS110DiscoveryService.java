package org.openhab.binding.hs110.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.hs110.HS110BindingConstants;
import org.openhab.binding.hs110.internal.HS110;
import org.openhab.binding.hs110.internal.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HS110DiscoveryService extends AbstractDiscoveryService {

    private Logger log = LoggerFactory.getLogger(HS110DiscoveryService.class);

    ///// Network
    private byte[] discoverbuffer = Util.encryptBytes(HS110.Command.SYSINFO.value);
    final private DatagramPacket discoverPacket;
    private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;
    private byte[] buffer = new byte[1024];
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    private final InetAddress broadcast;

    public HS110DiscoveryService() throws IllegalArgumentException, UnknownHostException {

        super(HS110BindingConstants.SUPPORTED_THING_TYPES_UIDS, 2, false);
        byte[] addr = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        broadcast = InetAddress.getByAddress(addr);
        discoverPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcast, 9999);

    }

    @Override
    protected void startScan() {

        try {
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setBroadcast(true);
            datagramSocket.send(discoverPacket);
            StringBuilder sb = new StringBuilder();
            for (byte b : discoverPacket.getData()) {
                sb.append(b);
            }
            log.debug(sb.toString());
            log.debug("Discovery package sent");

            while (true) {
                datagramSocket.receive(packet);
                log.debug("Discovery returned package with length {}", packet.getLength());
                detectThing(packet);
                packet.setLength(buffer.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void detectThing(DatagramPacket packet) throws IOException {
        String data = Util
                .decrypt(new ByteArrayInputStream(Arrays.copyOfRange(packet.getData(), 0, packet.getLength())), true);

        log.debug("Detecting HS110 by data: {}", data);

        String inetAddress = packet.getAddress().getHostAddress();
        Integer port = packet.getPort();
        String id = HS110.parseDeviceId(data);
        log.debug("HS110 with id {} found on {} ", id, inetAddress);
        // TEST if thing already exists
        ThingUID thingUID = new ThingUID(HS110BindingConstants.THING_TYPE_HS110, id);
        String label = "HS110 at " + inetAddress;
        Map<String, Object> properties = new TreeMap<>();
        properties.put(HS110BindingConstants.CONFIG_IP, inetAddress);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

}
