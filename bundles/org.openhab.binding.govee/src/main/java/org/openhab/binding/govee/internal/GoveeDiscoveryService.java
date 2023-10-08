/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.govee.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.govee.internal.model.DiscoveryMessage;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Discovers Govee Devices
 *
 * Scan approach:
 * 1. Determines all local network interfaces
 * 2. Send a multicast message on each interface to the Govee multicast address 239.255.255.250 at port 4001
 * 3. Retrieve the list of devices
 *
 * Based on the description at https://app-h5.govee.com/user-manual/wlan-guide
 *
 * A typical scan response looks as follows
 *
 * {
 * "msg":{
 * "cmd":"scan",
 * "data":{
 * "ip":"192.168.1.23",
 * "device":"1F:80:C5:32:32:36:72:4E",
 * "sku":"Hxxxx",
 * "bleVersionHard":"3.01.01",
 * "bleVersionSoft":"1.03.01",
 * "wifiVersionHard":"1.00.10",
 * "wifiVersionSoft":"1.02.03"
 * }
 * }
 * }
 *
 * Note that it uses the same port for receiving data like when receiving devices status updates.
 *
 * @see GoveeHandler
 *
 * @author Stefan Höhn - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.govee")
public class GoveeDiscoveryService extends AbstractDiscoveryService {

    public static boolean discoveryActive = false;

    private final Logger logger = LoggerFactory.getLogger(GoveeDiscoveryService.class);

    private static final String DISCOVERY_MULTICAST_ADDRESS = "239.255.255.250";
    private static final int DISCOVERY_PORT = 4001;
    private static final int DISCOVERY_RESPONSE_PORT = 4002;
    private static final int INTERFACE_TIMEOUT_SEC = 5;
    private static final int MILLIS_PER_SEC = 1000;

    private static final String DISCOVER_REQUEST = "{\"msg\": {\"cmd\": \"scan\", \"data\": {\"account_topic\": \"reserve\"}}}";

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(GoveeBindingConstants.THING_TYPE_LIGHT);

    @Activate
    public GoveeDiscoveryService(@Reference TranslationProvider i18nProvider) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
        this.i18nProvider = i18nProvider;
    }

    // for test purposes only
    public GoveeDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
    }

    @Override
    protected void startScan() {
        logger.debug("starting Scan");

        try {
            discoveryActive = true;

            // check if the status receiver is currently running, stop it and wait for that.
            // note that it restarts itself as soon as we are done.
            while (GoveeHandler.isRefreshJobRunning()) {
                GoveeHandler.stopRefreshStatusJob();
                Thread.sleep(1000);
                logger.debug("Waiting for device status request finish its task to be able to start discovery");
            }

            InetAddress broadcastAddress = InetAddress.getByName(DISCOVERY_MULTICAST_ADDRESS);
            final InetSocketAddress socketAddress = new InetSocketAddress(broadcastAddress, DISCOVERY_RESPONSE_PORT);

            getLocalNetworkInterfaces().forEach(localNetworkInterface -> {
                logger.debug("Discovering Govee Devices on {} ...", localNetworkInterface);

                try (MulticastSocket sendSocket = new MulticastSocket(socketAddress);
                        MulticastSocket receiveSocket = new MulticastSocket(DISCOVERY_RESPONSE_PORT)) {
                    sendSocket.setSoTimeout(INTERFACE_TIMEOUT_SEC * MILLIS_PER_SEC);
                    sendSocket.setReuseAddress(true);
                    sendSocket.setBroadcast(true);
                    sendSocket.setTimeToLive(2);
                    sendSocket.joinGroup(new InetSocketAddress(broadcastAddress, DISCOVERY_RESPONSE_PORT),
                            localNetworkInterface);
                    receiveSocket.setReuseAddress(true);
                    sendBroadcastToDiscoverThing(sendSocket, receiveSocket, broadcastAddress);
                } catch (IOException e) {
                    logger.warn("Discovery with IO exception: {}", e.getMessage());
                }
            });
        } catch (InterruptedException ie) {
            // don't care
        } catch (UnknownHostException e) {
            logger.warn("Discovery with UnknownHostException exception: {}", e.getMessage());
        } finally {
            discoveryActive = false;
        }
    }

    private void sendBroadcastToDiscoverThing(MulticastSocket sendSocket, MulticastSocket receiveSocket,
            InetAddress broadcastAddress) throws IOException {
        byte[] requestData = DISCOVER_REQUEST.getBytes();

        DatagramPacket request = new DatagramPacket(requestData, requestData.length, broadcastAddress, DISCOVERY_PORT);

        try {
            sendSocket.send(request);
        } catch (SocketTimeoutException ste) {
            // done with scanning
        }

        receiveSocket.setSoTimeout(INTERFACE_TIMEOUT_SEC * MILLIS_PER_SEC);
        receiveSocket.setReuseAddress(true);
        do {
            byte[] rxbuf = new byte[10240];
            DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
            receiveSocket.receive(packet);

            String response = new String(packet.getData()).trim();
            logger.trace("Govee Device Response: {}", response);

            final Map<String, Object> properties = getDeviceProperties(response);
            final Object product = properties.get(GoveeBindingConstants.PRODUCT_NAME);
            final String productName = (product != null) ? product.toString() : "unknown";
            final Object mac = properties.get(GoveeBindingConstants.MAC_ADDRESS);
            final String macAddress = (mac != null) ? mac.toString() : "unknown";

            ThingUID thingUid = new ThingUID(GoveeBindingConstants.THING_TYPE_LIGHT, macAddress.replace(":", "_"));

            DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUid).withProperties(properties)
                    .withRepresentationProperty(GoveeBindingConstants.MAC_ADDRESS)
                    .withLabel("Govee " + productName + " " + properties.get(GoveeBindingConstants.DEVICE_TYPE) + " ("
                            + properties.get(GoveeBindingConstants.IP_ADDRESS) + ")");

            thingDiscovered(discoveryResult.build());
        } while (true); // left by SocketTimeoutException
    }

    public Map<String, Object> getDeviceProperties(String response) {
        Bundle bundle = FrameworkUtil.getBundle(GoveeDiscoveryService.class);
        Gson gson = new Gson();

        DiscoveryMessage message = gson.fromJson(response, DiscoveryMessage.class);
        String ipAddress = "";
        String sku = "";
        String macAddress = "";
        String productName = "";

        if (message != null) {
            ipAddress = message.msg().data().ip();
            sku = message.msg().data().sku();
            macAddress = message.msg().data().device();

            if (ipAddress.isEmpty()) {
                ipAddress = "unknown";
                logger.warn("Empty IP Address received during discovery - device may not work");
            }

            productName = "unknown";
            if (!sku.isEmpty()) {
                final String skuLabel = "discovery.govee-light." + sku;
                if (bundle != null) {
                    productName = i18nProvider.getText(bundle, skuLabel, sku, Locale.getDefault());
                }
            } else {
                sku = "unknown";
                productName = "unknown";
                logger.warn("Empty SKU (product name) received during discovery - device may not work");
            }

            if (macAddress.isEmpty()) {
                macAddress = "unknown";
                logger.warn("Empty Mac Address received during discovery - device may not work");
            }
        }

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(GoveeBindingConstants.IP_ADDRESS, ipAddress);
        properties.put(GoveeBindingConstants.DEVICE_TYPE, sku);
        properties.put(GoveeBindingConstants.MAC_ADDRESS, macAddress);
        properties.put(GoveeBindingConstants.PRODUCT_NAME, (productName != null) ? productName : sku);

        return properties;
    }

    private List<NetworkInterface> getLocalNetworkInterfaces() {
        List<NetworkInterface> result = new LinkedList<>();
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                try {
                    if (networkInterface.isUp() && !networkInterface.isLoopback()
                            && !networkInterface.isPointToPoint()) {
                        result.add(networkInterface);
                    }
                } catch (SocketException exception) {
                    // ignore
                }
            }
        } catch (SocketException exception) {
            return Collections.emptyList();
        }
        return result;
    }

    public static boolean isDiscoveryActive() {
        return discoveryActive;
    }
}
