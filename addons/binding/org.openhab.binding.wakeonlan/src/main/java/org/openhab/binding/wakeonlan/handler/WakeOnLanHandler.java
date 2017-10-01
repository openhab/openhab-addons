/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wakeonlan.handler;

import static org.openhab.binding.wakeonlan.WakeOnLanBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.PatternSyntaxException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.wakeonlan.internal.WakeOnLanConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WakeOnLanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * This is the main handler for openhab2 Wake On LAN binding.
 *
 * @author Ganesh Ingle - Initial contribution
 */
public class WakeOnLanHandler extends BaseThingHandler {

    private Logger classLogger = LoggerFactory.getLogger(WakeOnLanHandler.class);
    private Logger thingLogger = null;
    public static final String DFLT_BROADCAST_ADDRESS = "255.255.255.255";
    public static final Integer DFLT_UDP_WOL_PORT = 9;
    protected String targetIP = DFLT_BROADCAST_ADDRESS;
    protected InetAddress targetBroadcastIP = null;
    protected String targetMACHex = null;
    protected boolean invalidCfg = false;
    protected Integer targetUDPPort = DFLT_UDP_WOL_PORT;
    protected String outgoingInterface = null;
    protected Boolean sendOnAllInterfaces = false;
    protected Boolean setBroadcastFlag = false;
    protected Timer statusUpdator = null;
    protected String errorMsg = "";
    protected byte[] magicPacket = null;
    protected String sendOnInterface = null;
    protected boolean setSO_BROADCAST = true;

    public WakeOnLanHandler(Thing thing) {
        super(thing);
    }

    // re-initialize fields after config update => dispose() => initialize()
    protected void initFields() {
        targetIP = DFLT_BROADCAST_ADDRESS;
        targetUDPPort = DFLT_UDP_WOL_PORT;
        targetMACHex = null;
        targetBroadcastIP = null;
        invalidCfg = false;
        errorMsg = "";
        // macBytes = null;
        magicPacket = null;
        sendOnInterface = null;
        sendOnAllInterfaces = false;
        thingLogger = null;
        setSO_BROADCAST = true;
    }

    @Override
    public void initialize() {
        classLogger.debug("Initializing Wake On LAN OH2 handler for thing '{}'", getThing().getLabel());
        initFields();
        if (thingLogger == null || !getThing().getLabel().equals(thingLogger.getName())) {
            thingLogger = LoggerFactory.getLogger(getThing().getLabel());
        }
        thingLogger.debug("Initializing Wake On LAN OH2 handler");
        // updateState(CHANNEL_STATUS, new StringType("INITIALIZING"));
        if (statusUpdator != null) {
            statusUpdator.cancel();
        }
        statusUpdator = new Timer(true);
        WakeOnLanConfiguration config = getConfigAs(WakeOnLanConfiguration.class);
        if (config.targetIP != null && config.targetIP.trim().length() > 0) {
            classLogger.debug("WOL Broadcast IP = {}", config.targetIP);
            thingLogger.debug("WOL Broadcast IP = {}", config.targetIP);
            targetIP = config.targetIP.trim();
            try {
                if (targetIP.matches("([0-9]{1,3}[.]){3}[0-9]{1,3}")) {
                    targetBroadcastIP = InetAddress.getByName(targetIP);
                } else {
                    invalidCfg = true;
                    errorMsg = "Invalid WOL Broadcast IP " + config.targetIP;
                }
            } catch (PatternSyntaxException | UnknownHostException | SecurityException e) {
                classLogger.warn("Error validating targetBroadcastIP. ", e);
                thingLogger.warn("Error validating targetBroadcastIP. ", e);
                invalidCfg = true;
                errorMsg = "Invalid WOL Broadcast IP " + config.targetIP + ". " + e.toString();
            }
        }
        if (config.targetMAC != null && config.targetMAC.trim().length() > 0) {
            targetMACHex = config.targetMAC.trim();
            if (!targetMACHex.matches("([0-9a-fA-F]{2}[: -]?){5}[0-9a-fA-F]{2}")) {
                errorMsg = "INVALID MAC " + config.targetMAC;
                invalidCfg = true;
            } else {
                try {
                    String maxHex = targetMACHex.replaceAll("[ ]|[:]|[-]", "");
                    magicPacket = fillMagicBytes(getMacBytes(maxHex));
                    classLogger.debug("WOL MAC {}", config.targetMAC);
                    thingLogger.debug("WOL MAC {}", config.targetMAC);
                } catch (NumberFormatException e) {
                    invalidCfg = true;
                    errorMsg = "INALID MAC " + config.targetMAC + ". " + e.toString();
                    classLogger.warn("Error validating targetMAC. ", e);
                    thingLogger.warn("Error validating targetMAC. ", e);
                }
            }
        } else {
            invalidCfg = true;
            errorMsg = "No MAC configured";
        }

        if (config.targetUDPPort != null && config.targetUDPPort >= 1 && config.targetUDPPort <= 65535) {
            targetUDPPort = config.targetUDPPort;
            classLogger.debug("WOL UDP Port = {}", config.targetUDPPort);
            thingLogger.debug("WOL UDP Port = {}", config.targetUDPPort);
        } else if (config.targetUDPPort != null) {
            invalidCfg = true;
            errorMsg = "Invalid WOL UDP Port " + config.targetUDPPort;
        }

        if (config.sendOnInterface != null && config.sendOnInterface.trim().length() > 0) {
            sendOnInterface = config.sendOnInterface;
            NetworkInterface nif = null;
            try {
                nif = NetworkInterface.getByName(sendOnInterface);
            } catch (SocketException e) {
                classLogger.warn("Error getting N/W interface. ", e);
                thingLogger.warn("Error getting N/W interface. ", e);
            }
            if (nif == null) {
                errorMsg = "Invalid WOL N/W Interface " + config.sendOnInterface;
                invalidCfg = true;
            } else {
                classLogger.debug("WOL N/W Interface = {}", config.sendOnInterface);
                thingLogger.debug("WOL N/W Interface = {}", config.sendOnInterface);
            }
        }

        if (config.sendOnAllInterfaces != null) {
            sendOnAllInterfaces = config.sendOnAllInterfaces;
            classLogger.debug("Send on ALL N/W Interfaces = {}", config.sendOnAllInterfaces);
            thingLogger.debug("Send on ALL N/W Interfaces = {}", config.sendOnAllInterfaces);
        }

        if (config.setSO_BROADCAST != null) {
            setSO_BROADCAST = config.setSO_BROADCAST;
            classLogger.debug("Set SO_BROADCAST = {}", config.setSO_BROADCAST);
            thingLogger.debug("Set SO_BROADCAST = {}", config.setSO_BROADCAST);
        }

        if (invalidCfg) {
            try {
                updateThingStatusDelayed(ThingStatus.OFFLINE, 800);
                updateStateDelayed(CHANNEL_STATUS, new StringType(errorMsg), 1000);
            } catch (InterruptedException e) {
                return;
            }
            classLogger.warn("Config error while initializing Wake On LAN OH2 handler for thing '{}'. {}",
                    getThing().getLabel(), errorMsg);
            thingLogger.warn("Config error while initializing Wake On LAN OH2 handler, {}", errorMsg);
        } else {
            // Throws callback missing error occasionally
            // super.initialize();
            try {
                updateThingStatusDelayed(ThingStatus.ONLINE, 800);
                updateStateDelayed(CHANNEL_STATUS, new StringType("Ready"), 1000);
            } catch (InterruptedException e) {
                return;
            }

            classLogger.debug("Initialized Wake On LAN OH2 handler for thing '{}'", getThing().getLabel());
            thingLogger.debug("Initialized Wake On LAN OH2 handler", getThing().getLabel());
        }
    }

    @Override
    public void dispose() {
        if (statusUpdator != null) {
            statusUpdator.cancel();
            statusUpdator = null;
        }
        // updateState(CHANNEL_STATUS, new StringType("DISPOSED"));
        super.dispose();
    }

    protected void updateStateDelayed(final String channel, final State state, long delayMs)
            throws InterruptedException {
        try {
            statusUpdator.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateState(channel, state);
                }
            }, delayMs);
        } catch (IllegalStateException e) {
            statusUpdator = new Timer(true);
            statusUpdator.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateState(channel, state);
                }

            }, delayMs);
        }
    }

    protected void updateThingStatusDelayed(final ThingStatus thingStatus, long delayMs) throws InterruptedException {
        try {
            statusUpdator.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateStatus(thingStatus);
                }
            }, delayMs);
        } catch (IllegalStateException e) {
            statusUpdator = new Timer(true);
            statusUpdator.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateStatus(thingStatus);
                }

            }, delayMs);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == null || channelUID == null) {
            return;
        }
        if (command == RefreshType.REFRESH) {
            return;
        }
        if (!(command instanceof OnOffType)) {
            String strCmd = command.toString();
            if (strCmd.equalsIgnoreCase("on") || strCmd.equalsIgnoreCase("1")) {
                command = OnOffType.ON;
            }
        }
        if ((CHANNEL_WAKEUP.equals(channelUID.getId()) || CHANNEL_STATUS.equals(channelUID.getId()))
                && command == OnOffType.ON) {
            try {
                if (!invalidCfg) {
                    try {
                        sendWolPacket();
                        updateState(CHANNEL_STATUS, new StringType("Packet Sent"));
                        updateStateDelayed(CHANNEL_STATUS, new StringType("Ready"), 5000);
                    } catch (InterruptedException e) {
                        return;
                    } catch (IOException | SecurityException | java.nio.channels.IllegalBlockingModeException
                            | IllegalArgumentException e) {
                        classLogger.warn("Failed to send Wake on LAN packet to {} , MAC={}",
                                targetBroadcastIP.getHostAddress() + ":" + targetUDPPort, targetMACHex);
                        classLogger.warn("Failed to send Wake on LAN packet. ", e);
                        thingLogger.warn("Failed to send Wake on LAN packet to {} , MAC={}",
                                targetBroadcastIP.getHostAddress() + ":" + targetUDPPort, targetMACHex);
                        thingLogger.warn("Failed to send Wake on LAN packet. ", e);
                        updateState(CHANNEL_STATUS, new StringType(e.toString()));
                        // Let user copy paste error message somewhere for diagnostics
                        updateStateDelayed(CHANNEL_STATUS, new StringType("Ready"), 60000);
                    }
                } else {
                    updateState(CHANNEL_STATUS, new StringType("Fix Config Error First"));
                    updateStateDelayed(CHANNEL_STATUS, new StringType(errorMsg), 5000);
                }
                updateStateDelayed(channelUID.getId(), OnOffType.OFF, 1000);
            } catch (InterruptedException e) {
                return;
            }
        } else {
            if (classLogger.isWarnEnabled()) {
                classLogger.warn("Unsupported command {} on channel {}", command.toFullString(), channelUID.getId());
            }
        }
    }

    protected void sendWolPacket() throws IOException, SecurityException, PortUnreachableException, SocketException,
            IOException, SecurityException, PortUnreachableException, java.nio.channels.IllegalBlockingModeException,
            IllegalArgumentException {
        if (sendOnAllInterfaces == true) {
            sendWolPacketOnAllInterfaces();
        } else if (sendOnInterface != null) {
            sendWolPacketOnInterface(null, null, sendOnInterface);
        } else {
            sendWolPacketSimple();
        }
    }

    protected void sendWolPacketSimple() throws IOException {
        DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, targetBroadcastIP, targetUDPPort);
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(setSO_BROADCAST);
        socket.send(packet);
        if (thingLogger.isInfoEnabled() || classLogger.isDebugEnabled()) {
            String localIP = socket.getLocalAddress() != null ? socket.getLocalAddress().getHostAddress() : "NULL";
            String msg = "WOL Packet sent from local IP " + localIP + " to target IP:PORT "
                    + targetBroadcastIP.getHostAddress() + ":" + targetUDPPort + ", target MAC " + targetMACHex
                    + ", with SO_BROADCAST = " + setSO_BROADCAST;
            thingLogger.info("{}", msg);
            classLogger.debug("{}", msg);
        }
        socket.close();
    }

    protected void sendWolPacketOnAllInterfaces() throws IOException {
        String messages = null;
        Boolean partialSuccess = false;
        Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
        while (nifs.hasMoreElements()) {
            NetworkInterface nif = nifs.nextElement();
            try {
                InetAddress validIP = hasValidIP(nif);
                if (validIP != null) {
                    sendWolPacketOnInterface(nif, validIP, nif.getName());
                    partialSuccess = true;
                }
            } catch (IOException e) {
                thingLogger.debug("Error while sending WOL packet on interface {} : {}", nif.getName(), e.toString());
                thingLogger.debug("Stacktrace . ", e);
                classLogger.debug("Error while sending WOL packet on interface {} : {}", nif.getName(), e.toString());
                classLogger.debug("Stacktrace . ", e);
                if (messages == null) {
                    messages = e.toString();
                } else {
                    messages = messages + "; " + e.toString();
                }
            }
        }
        if (!partialSuccess) {
            throw new IOException(messages);
        }
    }

    private InetAddress hasValidIP(NetworkInterface nif) {
        Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
        InetAddress selectedAddress = null;
        while (nifAddresses.hasMoreElements()) {
            InetAddress ip = nifAddresses.nextElement();
            if (!ip.isAnyLocalAddress() && !ip.isLinkLocalAddress() && !ip.isLoopbackAddress()
                    && !ip.isMulticastAddress()) {
                if (ip.getHostAddress().length() <= 15) {
                    selectedAddress = ip;
                }
                // else { // TODO add IPv6 option later }
            }
        }
        return selectedAddress;
    }

    protected void sendWolPacketOnInterface(NetworkInterface nif, InetAddress selectedAddress, String sendOnInterface)
            throws IOException, SecurityException, PortUnreachableException,
            java.nio.channels.IllegalBlockingModeException, IllegalArgumentException {
        if (nif == null) {
            nif = NetworkInterface.getByName(sendOnInterface);
        }
        if (nif == null) {
            throw new IOException("Error getting the network interface " + sendOnInterface);
        }
        classLogger.debug("Trying to send WOL packet on interface {}", nif.getName());
        thingLogger.debug("Trying to send WOL packet on interface {}", nif.getName());

        if (selectedAddress == null) {
            selectedAddress = hasValidIP(nif);
        }

        if (selectedAddress != null) {
            InetSocketAddress inetAddr = new InetSocketAddress(selectedAddress, 0);
            // socket.bind(new InetSocketAddress(nifAddresses.nextElement(), 0));
            DatagramSocket socket = new DatagramSocket(inetAddr);
            DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, targetBroadcastIP,
                    targetUDPPort);
            socket.setBroadcast(setSO_BROADCAST);
            socket.send(packet);
            classLogger.debug("Sent WOL packet on interface {}", nif.getName());
            thingLogger.debug("Sent WOL packet on interface {}", nif.getName());
            if (thingLogger.isInfoEnabled() || classLogger.isDebugEnabled()) {
                String localIP = socket.getLocalAddress() != null ? socket.getLocalAddress().getHostAddress() : "NULL";
                String msg = "WOL Packet sent from " + "local IP " + localIP + " to target IP:PORT "
                        + targetBroadcastIP.getHostAddress() + ":" + targetUDPPort + ", target MAC " + targetMACHex
                        + ", via NIC '" + nif.getName() + " (" + nif.getDisplayName() + ")', with SO_BROADCAST = "
                        + setSO_BROADCAST;
                thingLogger.info("{}", msg);
                classLogger.debug("{}", msg);
            }
            socket.close();
        } else {
            throw new IOException("Interface NOT suitable " + nif.getName());
        }
    }

    protected static byte[] fillMagicBytes(byte[] macBytes) {

        byte[] bytes = new byte[6 + 16 * macBytes.length];

        for (int i = 0; i < 6; i++) {
            bytes[i] = (byte) 0xff;
        }
        for (int i = 6; i < bytes.length; i += macBytes.length) {
            System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
        }

        return bytes;
    }

    protected byte[] getMacBytes(String macHex) throws NumberFormatException {
        byte[] bytes = new byte[6];
        byte[] hex = macHex.getBytes();
        for (int i = 0, j = 0; i < 6; i++, j += 2) {
            byte[] hexByteStr = new byte[2];
            hexByteStr[0] = hex[j];
            hexByteStr[1] = hex[j + 1];
            bytes[i] = (byte) Integer.parseInt(new String(hexByteStr), 16);
        }
        return bytes;
    }

}
