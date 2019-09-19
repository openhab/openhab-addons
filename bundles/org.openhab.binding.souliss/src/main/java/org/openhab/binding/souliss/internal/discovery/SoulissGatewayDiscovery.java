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
package org.openhab.binding.souliss.internal.discovery;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.discovery.SoulissDiscoverJob.DiscoverResult;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingUDPServerJob;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link soulissHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.souliss")
public class SoulissGatewayDiscovery extends AbstractDiscoveryService implements DiscoverResult {
    private Logger logger = LoggerFactory.getLogger(SoulissGatewayDiscovery.class);
    private SoulissDiscoverJob soulissDiscoverRunnableClass = null;
    private ThingUID gatewayUID;
    // private ScheduledFuture<?> schedulerFuture;
    private DatagramSocket datagramSocket;
    SoulissBindingUDPServerJob UDP_Server_RunnableClass = null;

    private ScheduledFuture<?> discoveryJob;
    private ScheduledFuture<?> UDPserverJob;

    public SoulissGatewayDiscovery() throws IllegalArgumentException, UnknownHostException {
        super(SoulissBindingConstants.SUPPORTED_THING_TYPES_UIDS, SoulissBindingConstants.DISCOVERY_TIMEOUT_IN_SECONDS,
                false);

        SoulissBindingNetworkParameters.discoverResult = this;
        // open socket
        // Version version = FrameworkUtil.getBundle(getClass()).getVersion();
        String sSymbolicName = FrameworkUtil.getBundle(getClass()).getSymbolicName();
        long sLastModified = FrameworkUtil.getBundle(getClass()).getLastModified();
        Date date = new Date(sLastModified);
        Format format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

        logger.info("Starting " + sSymbolicName + " - Version: " + format.format(date));
        logger.info("Starting Servers");

        datagramSocket = SoulissDatagramSocketFactory.getSocketDatagram();
        if (datagramSocket != null) {
            SoulissBindingNetworkParameters.setDatagramSocket(datagramSocket);

            logger.debug("Starting UDP server on Preferred Local Port (random if it is zero)");
            UDP_Server_RunnableClass = new SoulissBindingUDPServerJob(datagramSocket,
                    SoulissBindingNetworkParameters.discoverResult);

            UDPserverJob = scheduler.scheduleWithFixedDelay(UDP_Server_RunnableClass, 100,
                    SoulissBindingConstants.SERVER_CICLE_IN_MILLIS, TimeUnit.MILLISECONDS);

        } else {
            logger.debug("Error - datagramSocket is null - Server not started");
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        // if (backgroundFuture != null) {
        // return;
        // }
        // // per adesso non mi serve il discovery in background
        // // startDiscoveryService();
        //
        // backgroundFuture = scheduler.scheduleAtFixedRate(new DetectTask(), 50, 60000 * 30, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        // stopScan();
        // if (backgroundFuture != null) {
        // backgroundFuture.cancel(false);
        // backgroundFuture = null;
        // }

    }

    /**
     * The {@link gatewayDetected} used to create the Gateway
     *
     * @author Tonino Fazio - Initial contribution
     */
    @Override
    public void gatewayDetected(InetAddress addr, String id) {
        logger.debug("Souliss gateway found " + addr.getHostName() + " " + id);
        gatewayUID = new ThingUID(SoulissBindingConstants.GATEWAY_THING_TYPE, id);

        String label = "Souliss Gateway " + id;
        Map<String, Object> properties = new TreeMap<>();
        // properties.put(SoulissBindingConstants.CONFIG_ID, id);
        properties.put(SoulissBindingConstants.CONFIG_IP_ADDRESS, addr.getHostAddress());
        // SoulissBindingNetworkParameters.IPAddressOnLAN = addr.getHostAddress();
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(gatewayUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Scan Service");

        // create discovery class
        if (soulissDiscoverRunnableClass == null) {
            try {
                soulissDiscoverRunnableClass = new SoulissDiscoverJob(datagramSocket, this);
            } catch (SocketException e) {
                logger.error("Opening the souliss discovery service failed. " + e.getLocalizedMessage());
                return;
            }

        }
        // create discovery job, that run discovery class
        if (soulissDiscoverRunnableClass != null) {
            discoveryJob = scheduler.scheduleWithFixedDelay(soulissDiscoverRunnableClass, 100,
                    SoulissBindingConstants.DISCOVERY_RESEND_TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
            logger.info("Start Discovery Job");
        }

    }

    @Override
    protected synchronized void stopScan() {
        if (discoveryJob != null) {
            discoveryJob.cancel(false);
            discoveryJob = null;
            soulissDiscoverRunnableClass = null;
            logger.info("Discovery Job Stopped");
        }
        super.stopScan();
    }

    @Override
    public ThingUID getGatewayUID() {
        return gatewayUID;
    }

    @Override
    public void thingDetected_ActionMessages(String TopicNumber, String sTopicVariant) {
        ThingUID thingUID = null;
        String label = "";
        DiscoveryResult discoveryResult;
        String sNodeID = TopicNumber + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + sTopicVariant;

        thingUID = new ThingUID(SoulissBindingConstants.TOPICS_THING_TYPE, sNodeID);
        label = "Topic. Number: " + TopicNumber + ", Variant: " + sTopicVariant;

        discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public void thingDetected_Typicals(byte lastByteGatewayIP, byte typical, byte node, byte slot) {
        ThingUID thingUID = null;
        String label = "";
        DiscoveryResult discoveryResult;
        SoulissGatewayHandler gw = (SoulissGatewayHandler) (SoulissBindingNetworkParameters
                .getGateway(lastByteGatewayIP).getHandler());
        if (gw != null) {
            gatewayUID = gw.getThing().getUID();

            if (lastByteGatewayIP == Byte.parseByte(gw.IPAddressOnLAN.split("\\.")[3])) {
                String sNodeId = node + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + slot;
                switch (typical) {
                    case SoulissBindingProtocolConstants.Souliss_T11:
                        thingUID = new ThingUID(SoulissBindingConstants.T11_THING_TYPE, gatewayUID, sNodeId);
                        label = "T11: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T12:
                        thingUID = new ThingUID(SoulissBindingConstants.T12_THING_TYPE, sNodeId);
                        label = "T12: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T13:
                        thingUID = new ThingUID(SoulissBindingConstants.T13_THING_TYPE, sNodeId);
                        label = "T13: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T14:
                        thingUID = new ThingUID(SoulissBindingConstants.T14_THING_TYPE, sNodeId);
                        label = "T14: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T16:
                        thingUID = new ThingUID(SoulissBindingConstants.T16_THING_TYPE, sNodeId);
                        label = "T16: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T18:
                        thingUID = new ThingUID(SoulissBindingConstants.T18_THING_TYPE, sNodeId);
                        label = "T18: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T19:
                        thingUID = new ThingUID(SoulissBindingConstants.T19_THING_TYPE, sNodeId);
                        label = "T19: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T1A:
                        thingUID = new ThingUID(SoulissBindingConstants.T1A_THING_TYPE, sNodeId);
                        label = "T1A: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T21:
                        thingUID = new ThingUID(SoulissBindingConstants.T21_THING_TYPE, sNodeId);
                        label = "T21: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T22:
                        thingUID = new ThingUID(SoulissBindingConstants.T22_THING_TYPE, sNodeId);
                        label = "T22: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T41_Antitheft_Main:
                        thingUID = new ThingUID(SoulissBindingConstants.T41_THING_TYPE, sNodeId);
                        label = "T41: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T42_Antitheft_Peer:
                        thingUID = new ThingUID(SoulissBindingConstants.T42_THING_TYPE, sNodeId);
                        label = "T42: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T31:
                        thingUID = new ThingUID(SoulissBindingConstants.T31_THING_TYPE, sNodeId);
                        label = "T31: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T52_TemperatureSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T52_THING_TYPE, sNodeId);
                        label = "T52: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T53_HumiditySensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T53_THING_TYPE, sNodeId);
                        label = "T53: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T54_LuxSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T54_THING_TYPE, sNodeId);
                        label = "T54: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T55_VoltageSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T55_THING_TYPE, sNodeId);
                        label = "T55: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T56_CurrentSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T56_THING_TYPE, sNodeId);
                        label = "T56: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T57_PowerSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T57_THING_TYPE, sNodeId);
                        label = "T57: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T61:
                        thingUID = new ThingUID(SoulissBindingConstants.T61_THING_TYPE, sNodeId);
                        label = "T61: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T62_TemperatureSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T62_THING_TYPE, sNodeId);
                        label = "T62: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T63_HumiditySensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T63_THING_TYPE, sNodeId);
                        label = "T63: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T64_LuxSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T64_THING_TYPE, sNodeId);
                        label = "T64: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T65_VoltageSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T65_THING_TYPE, sNodeId);
                        label = "T65: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T66_CurrentSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T66_THING_TYPE, sNodeId);
                        label = "T66: node " + node + ", slot " + slot;
                        break;
                    case SoulissBindingProtocolConstants.Souliss_T67_PowerSensor:
                        thingUID = new ThingUID(SoulissBindingConstants.T67_THING_TYPE, sNodeId);
                        label = "T67: node " + node + ", slot " + slot;
                        break;
                }
                if (thingUID != null) {
                    label = "[" + gw.getThing().getUID().getAsString() + "] " + label;
                    discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                            .withBridge(gw.getThing().getUID()).build();
                    thingDiscovered(discoveryResult);
                    gw.setThereIsAThingDetection();

                }
            }

        }
    }
}
