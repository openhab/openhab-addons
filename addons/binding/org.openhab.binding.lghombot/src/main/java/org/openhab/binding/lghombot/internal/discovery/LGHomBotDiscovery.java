/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lghombot.internal.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.net.CidrAddress;
import org.eclipse.smarthome.core.net.NetUtil;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.lghombot.internal.LGHomBotBindingConstants;
import org.openhab.binding.lghombot.internal.LGHomBotConfiguration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery class for the LG HomBot line. Right now we try to do http requests to all IPs on port 6260.
 * If we get a connection and correct answer we set the IP as result.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.lghombot")
public class LGHomBotDiscovery extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(LGHomBotDiscovery.class);

    /**
     * Port number HomBot uses
     */
    private static final int HOMBOT_PORT = 6260;

    /**
     * HTTP read timeout (in ms) - allows us to shutdown the listening every TIMEOUT
     */
    private static final int TIMEOUT = 500;

    /**
     * Timeout in seconds of the complete scan
     */
    private static final int FULL_SCAN_TIMEOUT = 30;

    /**
     * Timeout in seconds of the complete scan
     */
    private static final int SCAN_THREADS = 10;

    /**
     * Whether we are currently scanning or not
     */
    private boolean scanning;

    private int octet;
    private int ipMask;
    private CidrAddress baseIp;

    /**
     * The {@link ExecutorService} to run the listening threads on.
     */
    private ExecutorService executorService;

    /**
     * Constructs the discovery class using the thing IDs that we can discover.
     */
    public LGHomBotDiscovery() {
        super(LGHomBotBindingConstants.SUPPORTED_THING_TYPES_UIDS, FULL_SCAN_TIMEOUT, false);
    }

    private void setupBaseIp(CidrAddress adr) {
        byte[] octets = adr.getAddress().getAddress();
        ipMask = 0xFFFFFFFF << (32 - adr.getPrefix());
        octets[0] &= ipMask >> 24;
        octets[1] &= ipMask >> 16;
        octets[2] &= ipMask >> 8;
        octets[3] &= ipMask;
        try {
            InetAddress iAdr = InetAddress.getByAddress(octets);
            baseIp = new CidrAddress(iAdr, (short) adr.getPrefix());
        } catch (UnknownHostException e) {
            logger.debug("Could not build net ip address, exception: {}", e);
        }
        octet = 0;
    }

    private synchronized String getNextIPAddress() {
        octet++;
        octet &= ~ipMask;
        byte[] octets = baseIp.getAddress().getAddress();
        octets[2] += (octet >> 8);
        octets[3] += octet;
        String address = null;
        try {
            InetAddress iAdr = null;
            iAdr = InetAddress.getByAddress(octets);
            address = iAdr.getHostAddress();
        } catch (UnknownHostException e) {
            logger.debug("Could not find next ip address, exception: {}", e);
        }
        return address;
    }

    /**
     * {@inheritDoc}
     *
     * Starts the scan. This discovery will:
     * <ul>
     * <li>Request all the network interfaces</li>
     * <li>For each network interface, create a listening thread using {@link #executorService}</li>
     * <li>Each listening thread will open up a {@link MulticastSocket} using {@link #SDDP_ADDR} and {@link #SDDP_PORT}
     * and
     * will receive any {@link DatagramPacket} that comes in</li>
     * <li>The {@link DatagramPacket} is then investigated to see if is a SDDP packet and will create a new thing from
     * it</li>
     * </ul>
     * The process will continue until {@link #stopScan()} is called.
     */
    @Override
    protected void startScan() {
        if (executorService != null) {
            stopScan();
        }

        executorService = Executors.newFixedThreadPool(SCAN_THREADS);

        scanning = true;
        List<CidrAddress> l = NetUtil.getAllInterfaceAddresses().stream()
                .filter(a -> a.getAddress() instanceof Inet4Address).map(a -> a).collect(Collectors.toList());
        setupBaseIp(l.get(0));
        for (int i = 1; i < 255; i++) {

            executorService.execute(() -> {
                String ipAdd = getNextIPAddress();
                String url = "http://" + ipAdd + ":" + HOMBOT_PORT + "/status.txt";
                String message = null;

                if (scanning) {
                    try {
                        message = HttpUtil.executeUrl("GET", url, TIMEOUT);
                        if (message != null && message.length() > 0) {
                            messageReceive(message, ipAdd);
                        }
                    } catch (IOException e) {
                        // Ignore, this is the expected behavior.
                    }
                }

            });
        }
        // executorService.awaitTermination(FULL_SCAN_TIMEOUT, TimeUnit.SECONDS);

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
     * JSON_NICKNAME=""
     * CLREC_CURRENTBUMPING="29441"
     * CLREC_LAST_CLEAN="2018/08/30/11/00/00.826531"
     * </pre>
     *
     * First parse the first string to see that it's a HomBot, then parse nickname, server version & firmware version.
     * We then create our thing from it.
     *
     * @param message   possibly null, possibly empty message
     * @param ipAddress current probed ip address
     */
    private void messageReceive(String message, String ipAddress) {

        if (!message.startsWith("JSON_ROBOT_STATE=")) {
            return;
        }

        String host = "a4_24_56_8f_2c_5b";
        String model = "HomBot";
        String nickName = "HOMBOT";
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

        if (ipAddress != null) {
            ThingTypeUID typeId = LGHomBotBindingConstants.THING_TYPE_LGHOMBOT;
            ThingUID uid = new ThingUID(typeId, host);

            Map<String, Object> properties = new HashMap<>(3);
            properties.put(LGHomBotConfiguration.IP_ADDRESS, ipAddress);
            properties.put("Firmware V", fwVersion);
            properties.put("Server V", srvVersion);
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withLabel(model + " (" + nickName + ")").build();
            thingDiscovered(result);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Stops the discovery scan. We set {@link #scanning} to false (allowing the listening threads to end naturally
     * within {@link #TIMEOUT) * {@link #SCAN_THREADS} time then shutdown the {@link #executorService}
     */
    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        if (executorService == null) {
            return;
        }

        scanning = false;

        try {
            executorService.awaitTermination(TIMEOUT * SCAN_THREADS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.debug("HomBot scacn interrupted, exception: {}", e);
        }
        executorService.shutdown();
        executorService = null;
    }
}
