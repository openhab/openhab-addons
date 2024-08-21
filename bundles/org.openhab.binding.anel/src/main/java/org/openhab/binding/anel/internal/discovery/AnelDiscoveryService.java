/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.anel.internal.discovery;

import java.io.IOException;
import java.net.BindException;
import java.nio.channels.ClosedByInterruptException;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.anel.internal.AnelUdpConnector;
import org.openhab.binding.anel.internal.IAnelConstants;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for ANEL devices.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.anel")
public class AnelDiscoveryService extends AbstractDiscoveryService {

    private static final String PASSWORD = "anel";
    private static final String USER = "user7";
    private static final int[][] DISCOVERY_PORTS = { { 750, 770 }, { 7500, 7700 }, { 7750, 7770 } };
    private static final Set<String> BROADCAST_ADDRESSES = new TreeSet<>(NetUtil.getAllBroadcastAddresses());

    private static final int DISCOVER_DEVICE_TIMEOUT_SECONDS = 2;

    /** #BroadcastAddresses * DiscoverDeviceTimeout * (3 * #DiscoveryPorts) */
    private static final int DISCOVER_TIMEOUT_SECONDS = BROADCAST_ADDRESSES.size() * DISCOVER_DEVICE_TIMEOUT_SECONDS
            * (3 * DISCOVERY_PORTS.length);

    private final Logger logger = LoggerFactory.getLogger(AnelDiscoveryService.class);

    private @Nullable Thread scanningThread = null;

    public AnelDiscoveryService() throws IllegalArgumentException {
        super(IAnelConstants.SUPPORTED_THING_TYPES_UIDS, DISCOVER_TIMEOUT_SECONDS);
        logger.debug(
                "Anel NET-PwrCtrl discovery service instantiated for broadcast addresses {} with a timeout of {} seconds.",
                BROADCAST_ADDRESSES, DISCOVER_TIMEOUT_SECONDS);
    }

    @Override
    protected void startScan() {
        /*
         * Start scan in background thread, otherwise progress is not shown in the web UI.
         * Do not use the scheduler, otherwise further threads (for handling discovered things) are not started
         * immediately but only after the scan is complete.
         */
        final Thread thread = new NamedThreadFactory(IAnelConstants.BINDING_ID, true).newThread(this::doScan);
        thread.start();
        scanningThread = thread;
    }

    private void doScan() {
        logger.debug("Starting scan of Anel devices via UDP broadcast messages...");

        try {
            for (final String broadcastAddress : BROADCAST_ADDRESSES) {

                // for each available broadcast network address try factory default ports first
                scan(broadcastAddress, IAnelConstants.DEFAULT_SEND_PORT, IAnelConstants.DEFAULT_RECEIVE_PORT);

                // try reasonable ports...
                for (int[] ports : DISCOVERY_PORTS) {
                    int sendPort = ports[0];
                    int receivePort = ports[1];

                    // ...and continue if a device was found, maybe there is yet another device on the next port
                    while (scan(broadcastAddress, sendPort, receivePort) || sendPort == ports[0]) {
                        sendPort++;
                        receivePort++;
                    }
                }
            }
        } catch (InterruptedException | ClosedByInterruptException e) {
            return; // OH shutdown or scan was aborted
        } catch (Exception e) {
            logger.warn("Unexpected exception during anel device scan", e);
        } finally {
            scanningThread = null;
        }
        logger.debug("Scan finished.");
    }

    /* @return Whether or not a device was found for the given broadcast address and port. */
    private boolean scan(String broadcastAddress, int sendPort, int receivePort)
            throws IOException, InterruptedException {
        logger.debug("Scanning {}:{}...", broadcastAddress, sendPort);
        final AnelUdpConnector udpConnector = new AnelUdpConnector(broadcastAddress, receivePort, sendPort, scheduler);

        try {
            final boolean[] deviceDiscovered = new boolean[] { false };
            udpConnector.connect(status -> {
                // avoid the same device to be discovered multiple times for multiple responses
                if (!deviceDiscovered[0]) {
                    boolean discoverDevice = true;
                    synchronized (this) {
                        if (deviceDiscovered[0]) {
                            discoverDevice = false; // already discovered by another thread
                        } else {
                            deviceDiscovered[0] = true; // we discover the device!
                        }
                    }
                    if (discoverDevice) {
                        // discover device outside synchronized-block
                        deviceDiscovered(status, sendPort, receivePort);
                    }
                }
            }, false);

            udpConnector.send(IAnelConstants.BROADCAST_DISCOVERY_MSG);

            // answer expected within 50-600ms on a regular network; wait up to 2sec just to make sure
            for (int delay = 0; delay < 10 && !deviceDiscovered[0]; delay++) {
                Thread.sleep(100 * DISCOVER_DEVICE_TIMEOUT_SECONDS); // wait 10 x 200ms = 2sec
            }

            return deviceDiscovered[0];
        } catch (BindException e) {
            // most likely socket is already in use, ignore this exception.
            logger.debug(
                    "Invalid address {} or one of the ports {} or {} is already in use. Skipping scan of these ports.",
                    broadcastAddress, sendPort, receivePort);
        } finally {
            udpConnector.disconnect();
        }
        return false;
    }

    @Override
    protected synchronized void stopScan() {
        final Thread thread = scanningThread;
        if (thread != null) {
            thread.interrupt();
        }
        super.stopScan();
    }

    private void deviceDiscovered(String status, int sendPort, int receivePort) {
        final String[] segments = status.split(":");
        if (segments.length >= 16) {
            final String name = segments[1].trim();
            final String ip = segments[2];
            final String macAddress = segments[5];
            final String deviceType = segments.length > 17 ? segments[17] : null;
            final ThingTypeUID thingTypeUid = getThingTypeUid(deviceType, segments);
            final ThingUID thingUid = new ThingUID(thingTypeUid + AbstractUID.SEPARATOR + macAddress.replace(".", ""));

            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUid) //
                    .withThingType(thingTypeUid) //
                    .withProperty("hostname", ip) // AnelConfiguration.hostname
                    .withProperty("user", USER) // AnelConfiguration.user
                    .withProperty("password", PASSWORD) // AnelConfiguration.password
                    .withProperty("udpSendPort", sendPort) // AnelConfiguration.udpSendPort
                    .withProperty("udpReceivePort", receivePort) // AnelConfiguration.udbReceivePort
                    .withProperty(IAnelConstants.UNIQUE_PROPERTY_NAME, macAddress) //
                    .withLabel(name) //
                    .withRepresentationProperty(IAnelConstants.UNIQUE_PROPERTY_NAME) //
                    .build();

            thingDiscovered(discoveryResult);
        }
    }

    private ThingTypeUID getThingTypeUid(@Nullable String deviceType, String[] segments) {
        // device type is contained since firmware 6.0
        if (deviceType != null && !deviceType.isEmpty()) {
            final char deviceTypeChar = deviceType.charAt(0);
            final ThingTypeUID thingTypeUID = IAnelConstants.DEVICE_TYPE_TO_THING_TYPE.get(deviceTypeChar);
            if (thingTypeUID != null) {
                return thingTypeUID;
            }
        }

        if (segments.length < 20) {
            // no information given, we should be save with return the simple firmware thing type
            return IAnelConstants.THING_TYPE_ANEL_SIMPLE;
        } else {
            // more than 20 segments must include IO ports, hence it's an advanced firmware
            return IAnelConstants.THING_TYPE_ANEL_ADVANCED;
        }
    }
}
