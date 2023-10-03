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

package org.openhab.binding.goveelan.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
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
import org.openhab.binding.goveelan.internal.model.DiscoveryMessage;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.framework.BundleContext;
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
 * @see GoveeLanHandler
 *
 * @author Stefan Höhn - Initial Contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.goveelan")
public class GoveeLanDiscoveryService extends AbstractDiscoveryService {

    public static boolean discoveryActive = false;

    private final Logger logger = LoggerFactory.getLogger(GoveeLanDiscoveryService.class);

    private static final String DISCOVERY_MULTICAST_ADDRESS = "239.255.255.250";
    private static final int DISCOVERY_PORT = 4001;
    private static final int DISCOVERY_RESPONSE_PORT = 4002;
    private static final int INTERFACE_TIMEOUT_SEC = 5;
    private static final int MILLIS_PER_SEC = 1000;

    private static final String DISCOVER_REQUEST = "{\"msg\": {\"cmd\": \"scan\", \"data\": {\"account_topic\": \"reserve\"}}}";

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(GoveeLanBindingConstants.THING_TYPE_LIGHT);

    @Activate
    public GoveeLanDiscoveryService(@Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    // for test purposes only
    public GoveeLanDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 0, false);
    }

    @Override
    protected void startScan() {
        logger.debug("starting Scan");
        BundleContext bundleContext = FrameworkUtil.getBundle(GoveeLanDiscoveryService.class).getBundleContext();

        try {
            discoveryActive = true;

            // check if the status receiver is currently running, stop it and wait for that.
            // note that it restarts itself as soon as we are done.
            while (GoveeLanHandler.isRefreshJobRunning()) {
                GoveeLanHandler.stopRefreshStatusJob();
                Thread.sleep(1000);
            }

            InetAddress multicastAddress = InetAddress.getByName(DISCOVERY_MULTICAST_ADDRESS);

            getLocalNetworkInterfaces().forEach(localNetworkInterface -> {
                logger.debug("Discovering Govee Devices on {} ...", localNetworkInterface);
                try (MulticastSocket socket = new MulticastSocket();
                        MulticastSocket rSocket = new MulticastSocket(DISCOVERY_RESPONSE_PORT)) {

                    socket.setSoTimeout(INTERFACE_TIMEOUT_SEC * MILLIS_PER_SEC);
                    rSocket.setSoTimeout(INTERFACE_TIMEOUT_SEC * MILLIS_PER_SEC);
                    socket.setBroadcast(true);
                    socket.setTimeToLive(2);

                    byte[] requestData = DISCOVER_REQUEST.getBytes();
                    DatagramPacket request = new DatagramPacket(requestData, requestData.length, multicastAddress,
                            DISCOVERY_PORT);
                    socket.send(request);

                    do {
                        byte[] rxbuf = new byte[10240];
                        DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                        rSocket.setReuseAddress(true);
                        rSocket.receive(packet);

                        String response = new String(packet.getData()).trim();
                        logger.trace("Govee Device Response: {}", response);

                        Map<String, Object> properties = getDeviceProperties(response);
                        final String sku = properties.get(GoveeLanConfiguration.DEVICETYPE).toString();
                        final String skuLabel = "discovery.goveelan.goveeLight." + sku;
                        String productName = i18nProvider.getText(bundleContext.getBundle(), skuLabel, sku,
                                Locale.getDefault());
                        properties.put(GoveeLanConfiguration.PRODUCTNAME, (productName != null) ? productName : sku);
                        ThingUID thingUid = new ThingUID(GoveeLanBindingConstants.THING_TYPE_LIGHT,
                                properties.get(GoveeLanConfiguration.MAC_ADDRESS).toString().replace(":", "_"));

                        DiscoveryResultBuilder discoveryResult = DiscoveryResultBuilder.create(thingUid)
                                .withProperties(properties)
                                .withRepresentationProperty(GoveeLanConfiguration.MAC_ADDRESS).withLabel(
                                        "Govee " + productName + " " + properties.get(GoveeLanConfiguration.DEVICETYPE)
                                                + " (" + properties.get(GoveeLanConfiguration.IPADDRESS) + ")");

                        thingDiscovered(discoveryResult.build());
                    } while (true); // left by SocketTimeoutException
                } catch (SocketTimeoutException ste) {
                    // done with scanning
                } catch (IOException e) {
                    logger.warn("Discovery with IO exception: {}", e.getMessage());
                }
            });
        } catch (UnknownHostException e) {
            logger.warn("Discovery failed: {}", e.getMessage());
        } catch (InterruptedException e) {
        } finally {
            discoveryActive = false;
        }
    }

    public Map<String, Object> getDeviceProperties(String response) {
        Gson gson = new Gson();

        DiscoveryMessage message = gson.fromJson(response, DiscoveryMessage.class);
        String ipAddress = message.msg().data().ip();
        String deviceType = message.msg().data().sku();
        String macAddress = message.msg().data().device();

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(GoveeLanConfiguration.IPADDRESS, ipAddress);
        properties.put(GoveeLanConfiguration.DEVICETYPE, deviceType);
        properties.put(GoveeLanConfiguration.MAC_ADDRESS, macAddress);

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
