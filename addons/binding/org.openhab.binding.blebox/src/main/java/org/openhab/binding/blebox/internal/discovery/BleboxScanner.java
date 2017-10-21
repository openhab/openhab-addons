/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blebox.internal.discovery;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.openhab.binding.blebox.internal.devices.DeviceInfo;
import org.openhab.binding.blebox.internal.devices.StatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link BleboxScanner} is responsible for scan local network for Blebox devices
 *
 * @author Szymon Tokarski - Initial contribution
 * @author Russell Stephens - network scanning method from EnvisalinkBridgeDiscovery
 */
public class BleboxScanner {
    private Logger logger = LoggerFactory.getLogger(BleboxScanner.class);

    static final int TIMEOUT = 2000;
    static final int THREADS_NUMER = 50;

    private BleboxDiscovery bleboxDiscovery = null;
    private Gson gson = new Gson();

    public BleboxScanner(BleboxDiscovery bleboxDiscovery) {
        this.bleboxDiscovery = bleboxDiscovery;
    }

    /**
     * Method for devices discovery.
     */
    public synchronized void discoverDevices() {
        logger.debug("Starting Blebox Discovery.");

        SubnetUtils subnetUtils = null;
        SubnetInfo subnetInfo = null;
        long lowIP = 0;
        long highIP = 0;

        try {
            List<Inet4Address> inet4 = getInet4Addresses();
            logger.debug("discoverDevices(): ip addresses - {}", inet4.size());
            logger.debug("discoverDevices(): ip main ip - {}", inet4.get(0));

            NetworkInterface networkInterfacee = NetworkInterface.getByInetAddress(inet4.get(0));
            logger.debug("discoverDevices(): networkInterface - {}", networkInterfacee.toString());
            String hostAddress = inet4.get(0).getHostAddress();
            subnetUtils = new SubnetUtils(hostAddress + "/" + "24");
            subnetInfo = subnetUtils.getInfo();
            lowIP = convertIPToNumber(subnetInfo.getLowAddress());
            highIP = convertIPToNumber(subnetInfo.getHighAddress());
        } catch (IllegalArgumentException e) {
            logger.error("discoverDevices(): Illegal Argument Exception - {}", e.toString());
            return;
        } catch (Exception e) {
            logger.error("discoverDevices(): Error - Unable to get Subnet Information! {}", e.toString());
            return;
        }

        logger.debug("   Local IP Address: {} - {}", subnetInfo.getAddress(),
                convertIPToNumber(subnetInfo.getAddress()));
        logger.debug("   Subnet:           {} - {}", subnetInfo.getNetworkAddress(),
                convertIPToNumber(subnetInfo.getNetworkAddress()));
        logger.debug("   Network Prefix:   {}", subnetInfo.getCidrSignature().split("/")[1]);
        logger.debug("   Network Mask:     {}", subnetInfo.getNetmask());
        logger.debug("   Low IP:           {}", convertNumberToIP(lowIP));
        logger.debug("   High IP:          {}", convertNumberToIP(highIP));

        ExecutorService threadpool = Executors.newFixedThreadPool(THREADS_NUMER);
        Async async = Async.newInstance().use(threadpool);

        for (long ip = lowIP; ip <= highIP; ip++) {
            try {
                final String ipAddress = convertNumberToIP(ip);

                URIBuilder builder = new URIBuilder();
                builder.setScheme("http").setHost(ipAddress).setPath("/api/device/state");
                URI requestURL = null;
                try {
                    requestURL = builder.build();
                } catch (URISyntaxException e) {
                    logger.warn("discoverDevices(): requestURL - {}", e.toString());
                }

                final Request request = Request.Get(requestURL).connectTimeout(TIMEOUT).socketTimeout(TIMEOUT);

                Future<Content> future = async.execute(request, new FutureCallback<Content>() {
                    @Override
                    public void failed(final Exception e) {
                        logger.debug("Error: {}", e.getMessage());
                    }

                    @Override
                    public void completed(final Content content) {
                        try {
                            // Standard response for every blebox device, except gateBox
                            StatusResponse statusResp = gson.fromJson(content.asString(), StatusResponse.class);

                            if (statusResp.device != null) {
                                logger.debug("Found blebox device: {}", statusResp.device.id);
                                bleboxDiscovery.addDevice(ipAddress, statusResp.device.type, statusResp.device.id,
                                        statusResp.device.deviceName);
                            } else {
                                DeviceInfo deviceInfo = gson.fromJson(content.asString(), DeviceInfo.class);

                                if (deviceInfo != null) {
                                    logger.debug("Found blebox device: {}", deviceInfo.id);
                                    bleboxDiscovery.addDevice(ipAddress, deviceInfo.type, deviceInfo.id,
                                            deviceInfo.deviceName);
                                }
                            }
                        } catch (Exception ex) {
                            logger.debug("Error: {}", ex.getMessage());
                        }
                    }

                    @Override
                    public void cancelled() {
                    }
                });
            } catch (Exception ex) {
                logger.debug("Error: {}", ex.getMessage());
            }
        }
    }

    /**
     * Returns this host's non-loopback IPv4 addresses.
     *
     * @return
     * @throws SocketException
     */
    private static List<Inet4Address> getInet4Addresses() throws SocketException {
        List<Inet4Address> ret = new ArrayList<Inet4Address>();

        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                    ret.add((Inet4Address) inetAddress);
                }
            }
        }
        return ret;
    }

    /**
     * Convert an IP address to a number.
     *
     * @param ipAddress
     * @return
     */
    private long convertIPToNumber(String ipAddress) {
        String octets[] = ipAddress.split("\\.");

        if (octets.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }

        long ip = 0;

        for (int i = 3; i >= 0; i--) {
            long octet = Long.parseLong(octets[3 - i]);

            if (octet != (octet & 0xff)) {
                throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
            }

            ip |= octet << (i * 8);
        }
        return ip;
    }

    /**
     * Convert a number to an IP address.
     *
     * @param ip
     * @return
     */
    private String convertNumberToIP(long ip) {
        StringBuilder ipAddress = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {

            ipAddress.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                ipAddress.insert(0, '.');
            }

            ip = ip >> 8;
        }

        return ipAddress.toString();
    }
}
