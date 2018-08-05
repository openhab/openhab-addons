/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * @author Ganesh Ingle <ganesh.ingle@asvilabs.com>
 */

package org.openhab.binding.wakeonlan.internal;

import static org.openhab.binding.wakeonlan.WakeOnLanBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.wakeonlan.WakeOnLanConfiguration;
import org.openhab.binding.wakeonlan.WakeOnLanHandlerFactory;
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
    private Logger logger = null;
    protected String targetIP = DFLT_BROADCAST_ADDRESS;
    protected InetAddress targetBroadcastIP = null;
    protected String targetMACHex = null;
    protected boolean invalidCfg = false;
    protected Integer targetUDPPort = DFLT_UDP_WOL_PORT;
    protected String outgoingInterface = null;
    protected boolean sendOnAllInterfaces = false;
    protected boolean setBroadcastFlag = false;
    protected Timer statusUpdator = null;
    protected Timer periodicPingTimer = null;
    protected String errorMsg = "";
    protected byte[] magicPacket = null;
    protected String sendOnInterface = null;
    protected boolean setSoBroadcast = true;
    // Enable periodic ping
    protected boolean periodicPing = false;
    // Use external ping program if Java ping doesn't work
    protected boolean externalPing = false;
    protected int pingIntervalMinutes = 2;
    protected String pingHostnameOrIp = null;
    protected StringType lastStatus = null;
    protected String shutdownCommands = null;
    protected String shutdownCommandExt = null;
    protected WakeOnLanHandlerFactory factory = null;
    protected boolean pingForSomeTimeAtHigherFreq = false;
    protected String observeForSomeTimeUntilStatus = "";

    public WakeOnLanHandler(WakeOnLanHandlerFactory factory, Thing thing) {
        super(thing);
        this.factory = factory;
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
        logger = null;
        setSoBroadcast = true;
        periodicPing = true;
        externalPing = false;
        pingIntervalMinutes = 2;
        pingHostnameOrIp = null;
        shutdownCommands = null;
        shutdownCommandExt = null;
        pingForSomeTimeAtHigherFreq = false;
        observeForSomeTimeUntilStatus = "";
    }

    // @Override
    // public void initialize() {
    // try {
    // doInitialize();
    // } catch (Throwable e) {
    // logger.warn("Couldn't initialize handler", e);
    // }
    // }

    @Override
    public void initialize() {
        initFields();
        String lbl = getThing().getLabel();
        if (lbl == null) {
            lbl = BINDING_LOGGER_NAME;
        }
        if (logger == null || (!lbl.equals(logger.getName()))) {
            logger = LoggerFactory.getLogger(lbl);
        }
        logger.info("Initializing Wake On LAN handler for Thing {}", getThing().getLabel());
        if (statusUpdator != null) {
            statusUpdator.cancel();
        }
        statusUpdator = new Timer(true);
        WakeOnLanConfiguration config = getConfigAs(WakeOnLanConfiguration.class);
        if (config.targetIP != null && config.targetIP.trim().length() > 0) {
            // classLogger.debug("WOL Broadcast IP = {}", config.targetIP);
            logger.info("WOL Broadcast IP = {}", config.targetIP);
            targetIP = config.targetIP.trim();
            try {
                if (targetIP.matches("([0-9]{1,3}[.]){3}[0-9]{1,3}")) {
                    targetBroadcastIP = InetAddress.getByName(targetIP);
                } else {
                    invalidCfg = true;
                    errorMsg = "Invalid WOL Broadcast IP = " + config.targetIP;
                }
            } catch (Exception e) {
                // classLogger.warn(e.toString(), e);
                logger.warn(e.toString(), e);
                invalidCfg = true;
                errorMsg = "Invalid WOL Broadcast IP = " + config.targetIP + ". " + e.toString();
            }
        }
        if (config.targetMAC != null && config.targetMAC.trim().length() > 0) {
            targetMACHex = config.targetMAC.trim();
            if (!targetMACHex.matches("([0-9a-fA-F]{2}[: -]?){5}[0-9a-fA-F]{2}")) {
                errorMsg = "INVALID MAC = " + config.targetMAC;
                invalidCfg = true;
            } else {
                try {
                    String maxHex = targetMACHex.replaceAll("[ ]|[:]|[-]", "");
                    magicPacket = fillMagicBytes(getMacBytes(maxHex));
                    // classLogger.debug("WOL MAC {}", config.targetMAC);
                    logger.info("WOL MAC = {}", config.targetMAC);
                } catch (NumberFormatException e) {
                    invalidCfg = true;
                    errorMsg = "INALID MAC " + config.targetMAC + ". " + e.toString();
                    // classLogger.warn(e.toString(), e);
                    logger.warn(e.toString(), e);
                }
            }
        } else {
            invalidCfg = true;
            errorMsg = "No MAC configured";
        }

        if (config.targetUDPPort != null && config.targetUDPPort >= 1 && config.targetUDPPort <= 65535) {
            targetUDPPort = config.targetUDPPort;
            // classLogger.debug("WOL UDP Port = {}", config.targetUDPPort);
            logger.info("WOL UDP Port = {}", config.targetUDPPort);
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
                // classLogger.warn(e.toString(), e);
                logger.warn(e.toString(), e);
            }
            if (nif == null) {
                errorMsg = "Invalid WOL N/W Interface " + config.sendOnInterface;
                invalidCfg = true;
            } else {
                // classLogger.debug("WOL N/W Interface = {}", config.sendOnInterface);
                logger.info("WOL N/W Interface = {}", config.sendOnInterface);
            }
        } else {
            logger.info("WOL N/W Interface = Not Set");
        }

        if (config.sendOnAllInterfaces != null) {
            sendOnAllInterfaces = config.sendOnAllInterfaces;
        }
        logger.info("Send on ALL N/W Interfaces = {}", sendOnAllInterfaces);

        if (config.setSocketOptionBroadcast != null) {
            setSoBroadcast = config.setSocketOptionBroadcast;
        }
        logger.info("Set SO_BROADCAST = {}", setSoBroadcast);

        if (config.pingHostnameOrIp != null) {
            pingHostnameOrIp = config.pingHostnameOrIp;
            logger.info("Ping Hostname Or IP = {}", config.pingHostnameOrIp);
            if (config.periodicPing != null) {
                periodicPing = config.periodicPing;
            }
            logger.info("Periodic Ping = {}", config.periodicPing);
            if (periodicPing) {
                if (config.externalPing != null) {
                    externalPing = config.externalPing;
                }
                logger.info("External Ping = {}", config.externalPing);
                if (config.pingIntervalMinutes != null && config.pingIntervalMinutes >= 1) {
                    pingIntervalMinutes = config.pingIntervalMinutes;
                }
                logger.info("Ping Interval Minutes = {}", pingIntervalMinutes);
            }
        } else {
            periodicPing = false;
            logger.info("Periodic Ping = false");
            logger.info("Ping Hostname Or IP = Not Set");
        }

        if (config.shutdownCommands != null) {
            shutdownCommands = config.shutdownCommands;
            logger.info("Shutdown Using Item or Thing Commands = {}", config.shutdownCommands);
        } else {
            logger.info("Shutdown Using Item or Thing Commands = Not Set");
        }
        if (config.shutdownCommandExt != null) {
            if (pingHostnameOrIp != null) {
                shutdownCommandExt = config.shutdownCommandExt.replaceAll("[^%][%]h", pingHostnameOrIp);
            }
            logger.info("Shutdown Using External Program Or Script = {}", shutdownCommandExt);
        } else {
            logger.info("Shutdown Using External Program Or Script = Not Set");
        }

        if (invalidCfg) {
            try {
                updateThingStatusDelayed(ThingStatus.OFFLINE, 800);
                updateStateDelayed(CHANNEL_STATUS, new StringType(errorMsg), 1000);
            } catch (InterruptedException e) {
                return;
            }
            logger.warn("Config error while initializing handler for Thing {}: {}", getThing().getLabel(), errorMsg);
        }
        startOrCleanupPeriodicPing();
        if (!invalidCfg) {
            try {
                updateThingStatusDelayed(ThingStatus.ONLINE, 800);
                if (periodicPingTimer == null) {
                    updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 1000);
                }
            } catch (InterruptedException e) {
                return;
            }
            logger.info("Successfully initialized Wake On LAN handler for Thing {}", getThing().getLabel());
        }
    }

    @Override
    public void dispose() {
        if (statusUpdator != null) {
            statusUpdator.cancel();
            statusUpdator = null;
        }
        if (periodicPingTimer != null) {
            periodicPingTimer.cancel();
            periodicPingTimer = null;
        }
        super.dispose();
    }

    protected void startOrCleanupPeriodicPing() {
        if (!periodicPing || pingHostnameOrIp == null || invalidCfg) {
            if (periodicPingTimer != null) {
                periodicPingTimer.cancel();
                periodicPingTimer = null;
            }
            return;
        }
        if (periodicPingTimer == null) {
            periodicPingTimer = new Timer(true);
        }
        periodicPingTimer.schedule(new TimerTask() {
            // int exceptionCount = 0;
            long lastPingAt = 0;
            int highFreqPingCount = 0;

            @Override
            public void run() {
                if ((System.currentTimeMillis() - lastPingAt) >= pingIntervalMinutes * 60000
                        || (pingForSomeTimeAtHigherFreq
                                && (System.currentTimeMillis() - lastPingAt) >= HIGHER_FREQ_PING_INTERVAL_MILLIS)) {
                    // configuration changed
                    lastPingAt = System.currentTimeMillis();
                    highFreqPingCount++;
                    if (highFreqPingCount >= 10) {
                        pingForSomeTimeAtHigherFreq = false;
                        highFreqPingCount = 0;
                    }
                    if (!periodicPing || pingHostnameOrIp == null || invalidCfg) {
                        periodicPingTimer.cancel();
                        periodicPingTimer = null;
                        pingForSomeTimeAtHigherFreq = false;
                        observeForSomeTimeUntilStatus = "";
                        return;
                    }
                    if (pingHostnameOrIp != null) {
                        boolean isOnline = false;
                        try {
                            try {
                                if (externalPing) {
                                    isOnline = PingHelper.isHostOnline(pingHostnameOrIp, logger);
                                } else {
                                    isOnline = PingHelper.isHostOnlineJava(pingHostnameOrIp, logger);
                                }
                            } catch (UnknownHostException | NoRouteToHostException e) {
                                logger.debug("{}. Considering it as host down.", e.toString());
                                isOnline = false;
                            }
                            if (pingForSomeTimeAtHigherFreq) {
                                if ((STATUS_HOST_ONLINE.equals(observeForSomeTimeUntilStatus) && isOnline)
                                        || (STATUS_HOST_OFFLINE.equals(observeForSomeTimeUntilStatus) && !isOnline)
                                        || STATUS_UPDATING.equals(observeForSomeTimeUntilStatus)) {
                                    pingForSomeTimeAtHigherFreq = false;
                                    highFreqPingCount = 0;
                                    observeForSomeTimeUntilStatus = "";
                                    updateState(CHANNEL_STATUS,
                                            new StringType(isOnline ? STATUS_HOST_ONLINE : STATUS_HOST_OFFLINE));
                                    updateState(CHANNEL_POWER, isOnline ? OnOffType.ON : OnOffType.OFF);
                                } else {
                                    updateState(CHANNEL_STATUS, new StringType(STATUS_UPDATING));
                                }
                            } else {
                                updateState(CHANNEL_STATUS,
                                        new StringType(isOnline ? STATUS_HOST_ONLINE : STATUS_HOST_OFFLINE));
                                updateState(CHANNEL_POWER, isOnline ? OnOffType.ON : OnOffType.OFF);
                            }
                        } catch (UnsupportedOSException e) {
                            logger.error("Exception during ping check. ", e);
                            updateState(CHANNEL_STATUS, new StringType("Can't do external ping. " + e.toString()));
                            updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 60000);
                            periodicPingTimer.cancel();
                            periodicPingTimer = null;
                            pingForSomeTimeAtHigherFreq = false;
                            observeForSomeTimeUntilStatus = "";
                            return;
                        } catch (IOException e) {
                            logger.error("Exception during ping check. ", e);
                            updateState(CHANNEL_STATUS, new StringType(
                                    (externalPing ? "External" : "Java builtin ") + " ping failed. " + e.toString()));
                            updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 60000);
                            // exceptionCount++;
                            // if (exceptionCount >= 3) {
                            // periodicPingTimer.cancel();
                            // periodicPingTimer = null;
                            // return;
                            // }
                        } catch (InterruptedException e) {
                            periodicPingTimer.cancel();
                            periodicPingTimer = null;
                            pingForSomeTimeAtHigherFreq = false;
                            highFreqPingCount = 0;
                            observeForSomeTimeUntilStatus = "";
                            return;
                        }
                    }
                }
            }
        }, 1000, HIGHER_FREQ_PING_INTERVAL_MILLIS + 100);
    }

    protected void updateStateDelayed(final String channel, final State state, long delayMs) {
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
    public void handleCommand(@NonNull ChannelUID channelUIDIn, @NonNull Command commandIn) {
        Command command = commandIn;
        String effectiveChannelName = channelUIDIn.getId();
        if (command == RefreshType.REFRESH) {
            return;
        }
        if (!(command instanceof OnOffType)) {
            String cmdStr = String.valueOf(command).toLowerCase();
            if (cmdStr.equals("1") || cmdStr.equals("on")) {
                command = OnOffType.ON;
            } else if (cmdStr.equals("0") || cmdStr.equals("off")) {
                command = OnOffType.OFF;
            } else if (cmdStr.equals("shutdown") || cmdStr.equals("halt")) {
                effectiveChannelName = CHANNEL_SHUTDOWN;
                command = OnOffType.ON;
            } else if (cmdStr.startsWith("wake")) {
                effectiveChannelName = CHANNEL_WAKEUP;
                command = OnOffType.ON;
            }
        }
        if ((effectiveChannelName.equals(CHANNEL_STATUS) || effectiveChannelName.equals(CHANNEL_POWER))) {
            if (command == OnOffType.ON) {
                effectiveChannelName = CHANNEL_WAKEUP;
            } else if (command == OnOffType.OFF) {
                effectiveChannelName = CHANNEL_SHUTDOWN;
                command = OnOffType.ON;
            }
        }
        if ((CHANNEL_WAKEUP.equals(effectiveChannelName)) && command == OnOffType.ON) {
            if (!invalidCfg) {
                try {
                    if (!channelUIDIn.getId().equals(CHANNEL_WAKEUP)) {
                        updateStateDelayed(CHANNEL_WAKEUP, OnOffType.ON, 100);
                    }
                    sendWolPacket();
                    updateState(CHANNEL_STATUS, new StringType(STATUS_PACKET_SENT));
                    pingForSomeTimeAtHigherFreq = true;
                    observeForSomeTimeUntilStatus = STATUS_HOST_ONLINE;
                    if (periodicPingTimer == null) {
                        updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 5000);
                    }
                } catch (Exception e) {
                    updateState(CHANNEL_STATUS, new StringType(e.toString()));
                    // Let user copy paste error message somewhere for diagnostics
                    updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 60000);
                }
            } else {
                updateState(CHANNEL_STATUS, new StringType("Fix Config Error First"));
                updateStateDelayed(CHANNEL_STATUS, new StringType(errorMsg), 5000);
            }
            updateStateDelayed(CHANNEL_WAKEUP, OnOffType.OFF, 1000);
        } else if ((CHANNEL_SHUTDOWN.equals(effectiveChannelName) && command == OnOffType.ON)) {
            if (!channelUIDIn.getId().equals(CHANNEL_SHUTDOWN)) {
                updateStateDelayed(CHANNEL_SHUTDOWN, OnOffType.ON, 100);
            }
            if (shutdownCommands != null || shutdownCommandExt != null) {
                try {
                    if (shutdownCommands != null) {
                        factory.getOhCommandHelper().handleCommands(shutdownCommands, logger);
                    } else if (shutdownCommandExt != null) {
                        ExecUtil.executeCommandLineAndWaitResponse(shutdownCommandExt, 10000, logger);
                    }
                    pingForSomeTimeAtHigherFreq = true;
                    observeForSomeTimeUntilStatus = STATUS_HOST_OFFLINE;
                    updateState(CHANNEL_STATUS, new StringType(STATUS_SHUTDOWN_COMMANDS_SENT));
                    if (periodicPingTimer == null) {
                        updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 5000);
                    }
                } catch (IOException e) {
                    logger.warn("Unable to run external shutdown command");
                    updateState(CHANNEL_STATUS, new StringType(e.toString()));
                    // Let user copy paste error message somewhere for diagnostics
                    if (periodicPingTimer == null) {
                        updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 60000);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            } else {
                updateState(CHANNEL_STATUS, new StringType(STATUS_SHUTDOWN_COMMANDS_NOT_CONFIGURED));
                if (periodicPingTimer == null) {
                    updateStateDelayed(CHANNEL_STATUS, new StringType(STATUS_READY), 5000);
                }
            }
            updateStateDelayed(CHANNEL_SHUTDOWN, OnOffType.OFF, 1000);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Unsupported command {} of type {} on channel " + channelUIDIn.getId(), command,
                        command.getClass().getName());
            }
        }
    }

    protected void sendWolPacket() throws IOException, SecurityException, PortUnreachableException, SocketException {
        if (sendOnAllInterfaces) {
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
        socket.setBroadcast(setSoBroadcast);
        socket.send(packet);
        if (logger.isDebugEnabled()) {
            String localIP = socket.getLocalAddress() != null ? socket.getLocalAddress().getHostAddress() : "NULL";
            String msg = "WOL Packet sent from local IP " + localIP + " to target IP:PORT "
                    + targetBroadcastIP.getHostAddress() + ":" + targetUDPPort + ", target MAC " + targetMACHex
                    + ", with SO_BROADCAST = " + setSoBroadcast;
            logger.info(msg);
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
                logger.debug(e.toString(), e);
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
            }
        }
        return selectedAddress;
    }

    protected void sendWolPacketOnInterface(NetworkInterface nifIn, InetAddress selectedAddressIn,
            String sendOnInterface) throws IOException {
        NetworkInterface nif = nifIn;
        InetAddress selectedAddress = selectedAddressIn;
        if (nif == null) {
            nif = NetworkInterface.getByName(sendOnInterface);
        }
        if (nif == null) {
            throw new IOException("Error getting the network interface " + sendOnInterface);
        }
        logger.debug("Trying to send WOL packet on interface " + nif.getName());

        if (selectedAddress == null) {
            selectedAddress = hasValidIP(nif);
        }

        if (selectedAddress != null) {
            InetSocketAddress inetAddr = new InetSocketAddress(selectedAddress, 0);
            // socket.bind(new InetSocketAddress(nifAddresses.nextElement(), 0));
            DatagramSocket socket = new DatagramSocket(inetAddr);
            DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, targetBroadcastIP,
                    targetUDPPort);
            socket.setBroadcast(setSoBroadcast);
            socket.send(packet);
            logger.debug("Sent WOL packet on interface " + nif.getName());
            if (logger.isDebugEnabled()) {
                String localIP = socket.getLocalAddress() != null ? socket.getLocalAddress().getHostAddress() : "NULL";
                String msg = "WOL Packet sent from " + "local IP " + localIP + " to target IP:PORT "
                        + targetBroadcastIP.getHostAddress() + ":" + targetUDPPort + ", target MAC " + targetMACHex
                        + ", via NIC '" + nif.getName() + " (" + nif.getDisplayName() + ")', with SO_BROADCAST = "
                        + setSoBroadcast;
                logger.info(msg);
            }
            socket.close();
        } else {
            String msg = "Interface NOT suitable " + nif.getName();
            logger.debug(msg);
            throw new IOException(msg);
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
