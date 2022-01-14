/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lghombot.internal.discovery;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghombot.internal.LGHomBotBindingConstants;
import org.openhab.binding.lghombot.internal.LGHomBotConfiguration;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.net.CidrAddress;
import org.openhab.core.net.NetUtil;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery class for the LG HomBot line. Right now we try to do http requests to all IPs on port 6260.
 * If we get a connection and correct answer we set the IP as result.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
@Component(service = { DiscoveryService.class, LGHomBotDiscovery.class }, configurationPid = "discovery.lghombot")
public class LGHomBotDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(LGHomBotDiscovery.class);

    /**
     * HTTP read timeout (in milliseconds) - allows us to shutdown the listening every TIMEOUT
     */
    private static final int TIMEOUT_MS = 500;

    /**
     * Timeout in seconds of the complete scan
     */
    private static final int FULL_SCAN_TIMEOUT_SECONDS = 30;

    /**
     * Total number of concurrent threads during scanning.
     */
    private static final int SCAN_THREADS = 10;

    /**
     * Whether we are currently scanning or not
     */
    private boolean scanning;

    private int octet;
    private int ipMask;
    private int addressCount;
    private @Nullable CidrAddress baseIp;

    /**
     * The {@link ExecutorService} to run the listening threads on.
     */
    private @Nullable ExecutorService executorService;

    /**
     * Constructs the discovery class using the thing IDs that we can discover.
     */
    public LGHomBotDiscovery() {
        super(LGHomBotBindingConstants.SUPPORTED_THING_TYPES_UIDS, FULL_SCAN_TIMEOUT_SECONDS, false);
    }

    private void setupBaseIp(CidrAddress adr) {
        byte[] octets = adr.getAddress().getAddress();
        addressCount = (1 << (32 - adr.getPrefix())) - 2;
        ipMask = 0xFFFFFFFF << (32 - adr.getPrefix());
        octets[0] &= ipMask >> 24;
        octets[1] &= ipMask >> 16;
        octets[2] &= ipMask >> 8;
        octets[3] &= ipMask;
        try {
            InetAddress iAdr = InetAddress.getByAddress(octets);
            baseIp = new CidrAddress(iAdr, (short) adr.getPrefix());
        } catch (UnknownHostException e) {
            logger.debug("Could not build net ip address.", e);
        }
        octet = 0;
    }

    private synchronized String getNextIPAddress(CidrAddress adr) {
        octet++;
        octet &= ~ipMask;
        byte[] octets = adr.getAddress().getAddress();
        octets[2] += (octet >> 8);
        octets[3] += octet;
        String address = "";
        try {
            InetAddress iAdr = null;
            iAdr = InetAddress.getByAddress(octets);
            address = iAdr.getHostAddress();
        } catch (UnknownHostException e) {
            logger.debug("Could not find next ip address.", e);
        }
        return address;
    }

    /**
     * {@inheritDoc}
     *
     * Starts the scan. This discovery will:
     * <ul>
     * <li>Request this hosts first IPV4 address.</li>
     * <li>Send a HTTP request on port 6260 to all IPs on the subnet.</li>
     * <li>The response is then investigated to see if is an answer from a HomBot lg.srv</li>
     * </ul>
     * The process will continue until all addresses are checked, timeout or {@link #stopScan()} is called.
     */
    @Override
    protected void startScan() {
        if (executorService != null) {
            stopScan();
        }

        CidrAddress localAdr = getLocalIP4Address();
        if (localAdr == null) {
            stopScan();
            return;
        }
        setupBaseIp(localAdr);
        CidrAddress baseAdr = baseIp;
        scanning = true;
        ExecutorService localExecutorService = Executors.newFixedThreadPool(SCAN_THREADS);
        executorService = localExecutorService;
        for (int i = 0; i < addressCount; i++) {

            localExecutorService.execute(() -> {
                if (scanning && baseAdr != null) {
                    String ipAdd = getNextIPAddress(baseAdr);
                    String url = "http://" + ipAdd + ":" + LGHomBotBindingConstants.DEFAULT_HOMBOT_PORT + "/status.txt";

                    try {
                        String message = HttpUtil.executeUrl("GET", url, TIMEOUT_MS);
                        if (message != null && !message.isEmpty()) {
                            messageReceive(message, ipAdd);
                        }
                    } catch (IOException e) {
                        // Ignore, this is the expected behavior.
                    }
                }

            });
        }
    }

    /**
     * Tries to find valid IP4 address.
     *
     * @return An IP4 address or null if none is found.
     */
    private @Nullable CidrAddress getLocalIP4Address() {
        List<CidrAddress> adrList = NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> a.getAddress() instanceof Inet4Address).collect(Collectors.toList());

        for (CidrAddress adr : adrList) {
            // Don't return a "fake" DHCP lease.
            if (!adr.toString().startsWith("169.254.")) {
                return adr;
            }
        }
        return null;
    }

    /**
     * lgsrv message has the following format
     *
     * <pre>
     * JSON_ROBOT_STATE="CHARGING"
     * JSON_BATTPERC="100"
     * LGSRV_VERSION="lg.srv, V2.51 compiled 18.11.2016, by fx2"
     * LGSRV_SUMCMD="0"
     * LGSRV_SUMCMDSEC="0.000000"
     * LGSRV_NUMHTTP="929"
     * LGSRV_MEMUSAGE="0.387 MB"
     * CPU_IDLE="67.92"
     * CPU_USER="19.49"
     * CPU_SYS="12.57"
     * CPU_NICE="0.00"
     * JSON_TURBO="false"
     * JSON_REPEAT="false"
     * JSON_MODE="ZZ"
     * JSON_VERSION="16552"
     * JSON_NICKNAME="HOMBOT"
     * CLREC_CURRENTBUMPING="29441"
     * CLREC_LAST_CLEAN="2018/08/30/11/00/00.826531"
     * </pre>
     *
     * First parse the first string to see that it's a HomBot, then parse nickname, server version & firmware version.
     * We then create our thing from it.
     *
     * @param message a response from a lgsrv to be parsed
     * @param ipAddress current probed ip address
     */
    private void messageReceive(String message, String ipAddress) {
        if (!message.startsWith("JSON_ROBOT_STATE=")) {
            return;
        }

        String model = "HomBot";
        String nickName = "";
        String srvVersion = "0";
        String fwVersion = "0";

        for (String msg : message.split("\\r?\\n")) {
            int idx = msg.indexOf('=');
            if (idx > 0) {
                String name = msg.substring(0, idx);

                if (name.equalsIgnoreCase("JSON_NICKNAME")) {
                    nickName = msg.substring(idx + 1).trim().replaceAll("\"", "");
                } else if (name.equalsIgnoreCase("JSON_VERSION")) {
                    fwVersion = msg.substring(idx + 1).trim().replaceAll("\"", "");
                } else if (name.equalsIgnoreCase("LGSRV_VERSION")) {
                    srvVersion = msg.substring(idx + 1).trim().replaceAll("\"", "");
                }
            }

        }

        if (!ipAddress.isEmpty()) {
            if (nickName.isEmpty()) {
                nickName = "HOMBOT1";
            }
            ThingTypeUID typeId = LGHomBotBindingConstants.THING_TYPE_LGHOMBOT;
            ThingUID uid = new ThingUID(typeId, nickName);

            Map<String, Object> properties = new HashMap<>(3);
            properties.put(LGHomBotConfiguration.IP_ADDRESS, ipAddress);
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, fwVersion);
            properties.put("server", srvVersion);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(model + " (" + nickName + ")").build();
            thingDiscovered(result);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Stops the discovery scan. We set {@link #scanning} to false (allowing the listening threads to end naturally
     * within {@link #TIMEOUT_MS) * {@link #SCAN_THREADS} time then shutdown the {@link #executorService}
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        ExecutorService localExecutorService = executorService;
        if (localExecutorService != null) {
            scanning = false;
            try {
                localExecutorService.awaitTermination(TIMEOUT_MS * SCAN_THREADS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.debug("Stop scan interrupted.", e);
            }
            localExecutorService.shutdown();
            executorService = null;
        }
    }
}
