/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.SUPPORTED_THING_TYPES;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.tplinksmarthome.internal.model.Sysinfo;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TPLinkSmartHomeDiscoveryService} detects new Smart Home Bulbs, Plugs and Switches by sending a UDP network
 * broadcast and parsing the answer into a thing.
 *
 * @author Christian Fischer - Initial contribution
 * @author Hilbrand Bouwkamp - Complete make-over, reorganized code and code cleanup.
 */
@Component(service = DiscoveryService.class, immediate = true)
public class TPLinkSmartHomeDiscoveryService extends AbstractDiscoveryService {

    private static final String BROADCAST_IP = "255.255.255.255";
    private static final int DISCOVERY_TIMEOUT_SECONDS = 20;
    private static final int UDP_PACKET_TIMEOUT = 1000;
    private static final long REFRESH_INTERVAL_MINUTES = 10;

    private final Logger logger = LoggerFactory.getLogger(TPLinkSmartHomeDiscoveryService.class);
    private final Commands commands = new Commands();

    private final DatagramPacket discoverPacket;
    private final byte[] buffer = new byte[2048];
    private DatagramSocket discoverSocket;
    private ScheduledFuture<?> discoveryJob;

    public TPLinkSmartHomeDiscoveryService() throws UnknownHostException {
        super(SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, false);
        InetAddress broadcast = InetAddress.getByName(BROADCAST_IP);
        byte[] discoverbuffer = CryptUtil.encrypt(Commands.getSysinfo());
        discoverPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcast,
                Connection.SMART_PLUG_PORT);
    }

    @Override
    protected void startBackgroundDiscovery() {
        discoveryJob = scheduler.scheduleWithFixedDelay(this::startScan, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        stopScan();
        if (discoveryJob != null && !discoveryJob.isCancelled()) {
            discoveryJob.cancel(true);
            discoveryJob = null;
        }
    }

    @Override
    protected void startScan() {
        logger.debug("Start scan for TP-Link Smart devices.");
        discoverThings();
    }

    @Override
    protected void stopScan() {
        logger.debug("Stop scan for TP-Link Smart devices.");
        closeDiscoverSocket();
        super.stopScan();
    }

    /**
     * Performs the discovery of TP-Link Smart Home devices.
     */
    private void discoverThings() {
        synchronized (this) {
            try {
                discoverSocket = sendDiscoveryPacket();
                // Runs until the socket call gets a time out and throws an exception. When a time out is triggered it
                // means no data was present and nothing new to discover.
                while (true) {
                    if (discoverSocket == null) {
                        break;
                    }
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    discoverSocket.receive(packet);
                    logger.debug("TP-Link Smart device discovery returned package with length {}", packet.getLength());
                    if (packet.getLength() > 0) {
                        detectThing(packet);
                    }
                }
            } catch (SocketTimeoutException e) {
                logger.debug("Discovering poller timeout...");
            } catch (IOException e) {
                logger.debug("Error during discovery: {}", e.getMessage());
            } finally {
                closeDiscoverSocket();
                removeOlderResults(getTimestampOfLastScan());
            }
        }
    }

    /**
     * Opens a {@link DatagramSocket} and sends a packet for discovery of TP-Link Smart Home devices.
     *
     * @return Returns the new socket
     * @throws IOException exception in case sending the packet failed
     */
    protected DatagramSocket sendDiscoveryPacket() throws IOException {
        DatagramSocket ds = new DatagramSocket(null);
        ds.setBroadcast(true);
        ds.setSoTimeout(UDP_PACKET_TIMEOUT);
        ds.send(discoverPacket);
        if (logger.isTraceEnabled()) {
            logger.trace("Discovery package sent: {}", new String(discoverPacket.getData(), StandardCharsets.UTF_8));
        }
        return ds;
    }

    /**
     * Closes the discovery socket and cleans the value. No need for synchronization as this method is called from a
     * synchronized context.
     */
    private void closeDiscoverSocket() {
        if (discoverSocket != null) {
            discoverSocket.close();
            discoverSocket = null;
        }
    }

    /**
     * Detected a device (thing) and get process the data from the device and report it discovered.
     *
     * @param packet containing data of detected device
     * @throws IOException in case decrypting of the data failed
     */
    private void detectThing(DatagramPacket packet) throws IOException {
        String ipAddress = packet.getAddress().getHostAddress();
        String rawData = CryptUtil.decrypt(packet.getData(), packet.getLength());
        Sysinfo sysinfo = commands.getSysinfoReponse(rawData);

        logger.trace("Detected TP-Link Smart Home device: {}", rawData);
        String deviceId = sysinfo.getDeviceId();
        logger.debug("TP-Link Smart Home device '{}' with id {} found on {} ", sysinfo.getAlias(), deviceId, ipAddress);
        Optional<ThingTypeUID> thingTypeUID = getThingTypeUID(sysinfo.getModel());

        if (thingTypeUID.isPresent()) {
            ThingUID thingUID = new ThingUID(thingTypeUID.get(),
                    deviceId.substring(deviceId.length() - 6, deviceId.length()));
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(sysinfo.getAlias())
                    .withProperties(PropertiesCollector.collectProperties(thingTypeUID.get(), ipAddress, sysinfo))
                    .build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Detected, but ignoring unsupported TP-Link Smart Home device model '{}'", sysinfo.getModel());
        }
    }

    /**
     * Finds the {@link ThingTypeUID} based on the model value returned by the device.
     *
     * @param model model value returned by the device
     * @return {@link ThingTypeUID} or null if device not recognized
     */
    private Optional<ThingTypeUID> getThingTypeUID(String model) {
        String modelLC = model.toLowerCase(Locale.ENGLISH);
        return SUPPORTED_THING_TYPES.stream().filter(suid -> modelLC.startsWith(suid.getId())).findFirst();
    }
}
