/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openhab.io.semp.internal.SEMPConstants.SSDPDiscoveryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SEMP upnp Gateway
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
public class SEMPUpnpServer extends Thread {
    private final Logger logger = LoggerFactory.getLogger(SEMPUpnpServer.class);

    // jUPNP shares port 1900, but since this is multicast, we can also bind to it
    private static final int UPNP_PORT_RECV = 1900;
    private static final String MULTI_ADDR = "239.255.255.250";
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private boolean running;
    private String discoPath;
    private InetAddress address;
    private String discoveryIp;
    private String discoveryUUID;
    private int webPort;
    private String discoString = "HTTP/1.1 200 OK\r\n" + "HOST: %s:%d\r\n" + "EXT:\r\n"
            + "CACHE-CONTROL: max-age=1800\r\n" + "LOCATION: %s\r\n"
            + "SERVER: Linux/2.6.32 UPnP/1.0 SMA SSDP Server/1.0.0\r\n";

    public SEMPUpnpServer(String discoPath, int webPort, String discoveryIP, String discoveryUUID) {
        this.running = true;
        this.discoPath = discoPath;
        this.webPort = webPort;
        this.discoveryIp = discoveryIP;
        this.discoveryUUID = discoveryUUID;

        logger.debug("Starting send and receive executor");
        NotifyRunnable notifyRunnable = new NotifyRunnable();
        executor.scheduleWithFixedDelay(notifyRunnable, 15, SEMPConstants.SSDP_VALIDITY_PERIOD / 6, TimeUnit.SECONDS);

    }

    public void shutdown() {
        this.running = false;
        executor.shutdown();
        sendNotify(false);
    }

    @Override
    public void run() {
        MulticastSocket recvSocket = null;
        byte[] buf = new byte[1000];
        DatagramPacket recv = new DatagramPacket(buf, buf.length);
        while (running) {
            try {
                if (discoveryIp != null && discoveryIp.trim().length() > 0) {
                    address = InetAddress.getByName(discoveryIp);
                } else {
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface ni = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = ni.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress addr = addresses.nextElement();
                            if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                                address = addr;
                                logger.debug("Network address: {}", address);
                                break;
                            }
                        }
                    }
                }
                InetSocketAddress socketAddr = new InetSocketAddress(MULTI_ADDR, UPNP_PORT_RECV);
                recvSocket = new MulticastSocket(UPNP_PORT_RECV);
                recvSocket.joinGroup(socketAddr, NetworkInterface.getByInetAddress(address));
                while (running) {
                    logger.debug("UPNP is running");
                    recvSocket.receive(recv);
                    if (recv.getLength() > 0) {
                        String data = new String(recv.getData());
                        logger.debug("Got SSDP Discovery packet from {}:{}", recv.getAddress().getHostAddress(),
                                recv.getPort());
                        logger.debug("Data: {}", data);
                        if (data.startsWith("M-SEARCH * HTTP/1.1") && data.contains("ssdp:discover")) {
                            String[] dataLines = StringUtils.split(data, "\r\n");
                            String searchTarget = null;
                            for (int i = 1; i < dataLines.length; ++i) {
                                if (dataLines[i].startsWith("ST:")) {
                                    searchTarget = dataLines[i].substring(3).trim();
                                    logger.debug("searchTarget: {}", searchTarget);
                                }
                            }
                            if (searchTarget == null) {
                                return;
                            }
                            String header = String.format(discoString, MULTI_ADDR, UPNP_PORT_RECV,
                                    "http://" + address.getHostAddress().toString() + ":" + webPort + discoPath);
                            String response;
                            if ("ssdp:all".equals(searchTarget)) {
                                response = header + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_ROOTDEVICE, true)
                                        + "\r\n";
                                sendResponseDatagramPacket(response, recv);

                                response = header + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_DEVICEID_TYPE, true)
                                        + "\r\n";
                                sendResponseDatagramPacket(response, recv);

                                response = header + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_DEVICE_TYPE, true)
                                        + "\r\n";
                                sendResponseDatagramPacket(response, recv);

                                for (SEMPConstants.SSDPServiceConfig serviceConfig : SEMPConstants.SSDP_SERVICE_CONFIG_LIST) {
                                    response = header + getDiscoveryServiceNtUsn(serviceConfig, true) + "\r\n";
                                    sendResponseDatagramPacket(response, recv);
                                }
                            } else if ("upnp:rootdevice".equals(searchTarget)) {
                                response = header + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_ROOTDEVICE, true)
                                        + "\r\n";
                                sendResponseDatagramPacket(response, recv);
                            } else if (("uuid:" + discoveryUUID).equals(searchTarget)) {
                                response = header + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_DEVICEID_TYPE, true)
                                        + "\r\n";
                                sendResponseDatagramPacket(response, recv);
                            } else if (("urn:" + SEMPConstants.SEMP_DEVICE_CONFIG.urn).equals(searchTarget)) {
                                response = header + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_DEVICE_TYPE, true)
                                        + "\r\n";
                                sendResponseDatagramPacket(response, recv);
                            } else {
                                for (SEMPConstants.SSDPServiceConfig serviceConfig : SEMPConstants.SSDP_SERVICE_CONFIG_LIST) {
                                    if (("urn:" + serviceConfig.typeUrn).equals(searchTarget)) {
                                        response = header + getDiscoveryServiceNtUsn(serviceConfig, true) + "\r\n";
                                        sendResponseDatagramPacket(response, recv);
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (SocketException e) {
                logger.error("Socket error with UPNP server", e);
            } catch (IOException e) {
                logger.error("IO Error with UPNP server", e);
            } finally {
                IOUtils.closeQuietly(recvSocket);
                if (running) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    private String getDiscoveryNtUsn(SSDPDiscoveryType type, boolean useSearchTarget) {
        String targetPrefix = (useSearchTarget ? "ST" : "NT");

        switch (type) {
            case SSDP_DT_ROOTDEVICE:
                targetPrefix += ": upnp:rootdevice\r\n" + "USN: uuid:" + discoveryUUID + "::upnp:rootdevice\r\n";
                break;
            case SSDP_DT_DEVICE_TYPE:
                targetPrefix += ": urn:" + SEMPConstants.SEMP_DEVICE_CONFIG.urn + "\r\n" + "USN: uuid:" + discoveryUUID
                        + "::urn:" + SEMPConstants.SEMP_DEVICE_CONFIG.urn + "\r\n";
                break;
            case SSDP_DT_DEVICEID_TYPE:
                targetPrefix += ": uuid:" + discoveryUUID + "\r\n" + "USN: uuid:" + discoveryUUID + "\r\n";
                break;
            default:
                return "";
        }
        return targetPrefix;
    }

    private String getDiscoveryServiceNtUsn(SEMPConstants.SSDPServiceConfig svc, boolean useSearchTarget) {
        String targetPrefix = (useSearchTarget ? "ST" : "NT");
        return targetPrefix + ": urn:" + svc.typeUrn + "\r\n" + "USN: uuid:" + discoveryUUID + "::urn:" + svc.typeUrn
                + "\r\n";
    }

    private void sendResponseDatagramPacket(String msg, DatagramPacket recv) {
        DatagramSocket sendSocket = null;
        try {
            sendSocket = new DatagramSocket();
            DatagramPacket response = new DatagramPacket(msg.getBytes(), msg.length(), recv.getAddress(),
                    recv.getPort());
            logger.debug("Sending to {} : {}", recv.getAddress().getHostAddress(), msg);
            sendSocket.send(response);
        } catch (SocketException e) {
            logger.error("Socket error with UPNP server", e);
        } catch (IOException e) {
            logger.debug("Could not send UPNP response: {}", e.getMessage());
        } finally {
            IOUtils.closeQuietly(sendSocket);
            if (running) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    void sendNotify(boolean alive) {
        String msg;
        DatagramPacket dgPacket;
        StringBuilder notifyBuilder = new StringBuilder();
        MulticastSocket mcSocket = null;
        InetAddress iGgroup = null;
        try {
            iGgroup = InetAddress.getByName(SEMPConstants.SSDP_MCAST_IP);
            mcSocket = new MulticastSocket(SEMPConstants.SSDP_MCAST_PORT);
            mcSocket.joinGroup(iGgroup);

            notifyBuilder.append("NOTIFY * HTTP/1.1\r\n");
            notifyBuilder.append("HOST: " + SEMPConstants.SSDP_MCAST_IP + ":"
                    + String.valueOf(SEMPConstants.SSDP_MCAST_PORT) + "\r\n");
            if (alive) {
                notifyBuilder.append("CACHE-CONTROL: max-age = " + SEMPConstants.SSDP_VALIDITY_PERIOD + "\r\n");
                notifyBuilder.append("SERVER: " + SEMPConstants.SSDP_SERVER_TYPE + "\r\n");
                notifyBuilder.append("NTS: ssdp:alive\r\n");
                notifyBuilder.append(
                        "LOCATION: http://" + address.getHostAddress().toString() + ":" + webPort + discoPath + "\r\n");
            } else {
                notifyBuilder.append("NTS: ssdp:byebye\r\n");
            }

            msg = notifyBuilder + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_ROOTDEVICE, false) + "\r\n";
            dgPacket = new DatagramPacket(msg.getBytes(), msg.length(), iGgroup, SEMPConstants.SSDP_MCAST_PORT);
            mcSocket.send(dgPacket);
            logger.debug("Send Notify: {}", msg);

            msg = notifyBuilder + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_DEVICEID_TYPE, false) + "\r\n";
            dgPacket = new DatagramPacket(msg.getBytes(), msg.length(), iGgroup, SEMPConstants.SSDP_MCAST_PORT);
            mcSocket.send(dgPacket);
            logger.debug("Send Notify: {}", msg);

            msg = notifyBuilder + getDiscoveryNtUsn(SSDPDiscoveryType.SSDP_DT_DEVICE_TYPE, false) + "\r\n";
            dgPacket = new DatagramPacket(msg.getBytes(), msg.length(), iGgroup, SEMPConstants.SSDP_MCAST_PORT);
            mcSocket.send(dgPacket);
            logger.debug("Send Notify: {}", msg);

            for (SEMPConstants.SSDPServiceConfig serviceConfig : SEMPConstants.SSDP_SERVICE_CONFIG_LIST) {
                msg = notifyBuilder + getDiscoveryServiceNtUsn(serviceConfig, true) + "\r\n";
                dgPacket = new DatagramPacket(msg.getBytes(), msg.length(), iGgroup, SEMPConstants.SSDP_MCAST_PORT);
                mcSocket.send(dgPacket);
                logger.debug("Send Notify: {}", msg);
            }

            mcSocket.leaveGroup(iGgroup);
        } catch (IOException e) {
            logger.error("Notify error: {}", e.getMessage());
        }
    }

    private class NotifyRunnable implements Runnable {

        public NotifyRunnable() {
        }

        @Override
        public void run() {
            logger.debug("NotifyRunnable");
            sendNotify(true);
        }

    }

}
