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
package org.openhab.binding.souliss.internal.discovery;

import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.SoulissProtocolConstants;
import org.openhab.binding.souliss.internal.handler.SoulissGatewayHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link org.openhab.binding.souliss.internal.SoulissHandlerFactory} is responsible for creating
 * things and thingGeneric handlers.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissGatewayDiscovery extends AbstractDiscoveryService
        implements DiscoverResult, DiscoveryService, ThingHandlerService {
    private @Nullable ScheduledFuture<?> discoveryJob = null;
    private final Logger logger = LoggerFactory.getLogger(SoulissGatewayDiscovery.class);

    private @Nullable SoulissDiscoverJob soulissDiscoverRunnableClass;
    private @Nullable SoulissGatewayHandler soulissGwHandler;

    public SoulissGatewayDiscovery() {
        super(SoulissBindingConstants.SUPPORTED_THING_TYPES_UIDS, SoulissBindingConstants.DISCOVERY_TIMEOUT_IN_SECONDS,
                false);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    /**
     * The {@link gatewayDetected} callback used to create the Gateway
     */
    @Override
    public void gatewayDetected(InetAddress addr, String id) {
        logger.debug("Souliss gateway found: {} ", addr.getHostName());

        String label = "Souliss Gateway " + (Byte.parseByte(id) & 0xFF);
        Map<String, Object> properties = new TreeMap<>();
        properties.put(SoulissBindingConstants.CONFIG_IP_ADDRESS, addr.getHostAddress());
        var gatewayUID = new ThingUID(SoulissBindingConstants.GATEWAY_THING_TYPE,
                Integer.toString((Byte.parseByte(id) & 0xFF)));
        var discoveryResult = DiscoveryResultBuilder.create(gatewayUID).withLabel(label)
                .withRepresentationProperty(SoulissBindingConstants.CONFIG_IP_ADDRESS).withProperties(properties)
                .build();
        thingDiscovered(discoveryResult);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting Scan Service");

        // create discovery class
        if (soulissDiscoverRunnableClass == null) {
            soulissDiscoverRunnableClass = new SoulissDiscoverJob(this.soulissGwHandler);

            // send command for gw struct (typicals).. must be not soo much quick..
            discoveryJob = scheduler.scheduleWithFixedDelay(soulissDiscoverRunnableClass, 2,
                    SoulissBindingConstants.DISCOVERY_RESEND_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
            logger.debug("Start Discovery Job");
        }
    }

    @Override
    protected synchronized void stopScan() {
        ScheduledFuture<?> localDiscoveryJob = this.discoveryJob;
        if (localDiscoveryJob != null) {
            localDiscoveryJob.cancel(false);
            soulissDiscoverRunnableClass = null;
            logger.debug("Discovery Job Stopped");
        }
        super.stopScan();
    }

    @Override
    public void thingDetectedActionMessages(String topicNumber, String sTopicVariant) {
        ThingUID thingUID = null;
        var label = "";
        DiscoveryResult discoveryResult;
        String sNodeID = topicNumber + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + sTopicVariant;

        var localGwHandler = this.soulissGwHandler;
        if (localGwHandler != null) {
            var gatewayUID = localGwHandler.getThing().getUID();
            thingUID = new ThingUID(SoulissBindingConstants.TOPICS_THING_TYPE, gatewayUID, sNodeID);
            label = "Topic. Number: " + topicNumber + ", Variant: " + sTopicVariant;

            discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                    .withProperty("number", topicNumber).withProperty("variant", sTopicVariant)
                    .withRepresentationProperty("number").withBridge(gatewayUID).build();
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    public void thingDetectedTypicals(byte lastByteGatewayIP, byte typical, byte node, byte slot) {
        ThingUID thingUID = null;
        var label = "";
        DiscoveryResult discoveryResult;
        var gwHandler = this.soulissGwHandler;
        if ((gwHandler != null) && (lastByteGatewayIP == (byte) Integer
                .parseInt(gwHandler.getGwConfig().gatewayLanAddress.split("\\.")[3]))) {
            String sNodeId = node + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + slot;

            ThingUID gatewayUID = gwHandler.getThing().getUID();
            var nodeLabel = "node";
            var slotLabel = "slot";

            switch (typical) {
                case SoulissProtocolConstants.SOULISS_T11:
                    thingUID = new ThingUID(SoulissBindingConstants.T11_THING_TYPE, gatewayUID, sNodeId);
                    label = "T11: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T12:
                    thingUID = new ThingUID(SoulissBindingConstants.T12_THING_TYPE, gatewayUID, sNodeId);
                    label = "T12: " + nodeLabel + " " + " " + node + " " + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T13:
                    thingUID = new ThingUID(SoulissBindingConstants.T13_THING_TYPE, gatewayUID, sNodeId);
                    label = "T13: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T14:
                    thingUID = new ThingUID(SoulissBindingConstants.T14_THING_TYPE, gatewayUID, sNodeId);
                    label = "T14: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T16:
                    thingUID = new ThingUID(SoulissBindingConstants.T16_THING_TYPE, gatewayUID, sNodeId);
                    label = "T16: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T18:
                    thingUID = new ThingUID(SoulissBindingConstants.T18_THING_TYPE, gatewayUID, sNodeId);
                    label = "T18: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T19:
                    thingUID = new ThingUID(SoulissBindingConstants.T19_THING_TYPE, gatewayUID, sNodeId);
                    label = "T19: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T1A:
                    thingUID = new ThingUID(SoulissBindingConstants.T1A_THING_TYPE, gatewayUID, sNodeId);
                    label = "T1A: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T21:
                    thingUID = new ThingUID(SoulissBindingConstants.T21_THING_TYPE, gatewayUID, sNodeId);
                    label = "T21: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T22:
                    thingUID = new ThingUID(SoulissBindingConstants.T22_THING_TYPE, gatewayUID, sNodeId);
                    label = "T22: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T41_ANTITHEFT_MAIN:
                    thingUID = new ThingUID(SoulissBindingConstants.T41_THING_TYPE, gatewayUID, sNodeId);
                    label = "T41: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T42_ANTITHEFT_PEER:
                    thingUID = new ThingUID(SoulissBindingConstants.T42_THING_TYPE, gatewayUID, sNodeId);
                    label = "T42: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T31:
                    thingUID = new ThingUID(SoulissBindingConstants.T31_THING_TYPE, gatewayUID, sNodeId);
                    label = "T31: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T52_TEMPERATURE_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T52_THING_TYPE, gatewayUID, sNodeId);
                    label = "T52: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T53_HUMIDITY_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T53_THING_TYPE, gatewayUID, sNodeId);
                    label = "T53: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T54_LUX_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T54_THING_TYPE, gatewayUID, sNodeId);
                    label = "T54: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T55_VOLTAGE_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T55_THING_TYPE, gatewayUID, sNodeId);
                    label = "T55: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T56_CURRENT_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T56_THING_TYPE, gatewayUID, sNodeId);
                    label = "T56: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T57_POWER_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T57_THING_TYPE, gatewayUID, sNodeId);
                    label = "T57: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T61:
                    thingUID = new ThingUID(SoulissBindingConstants.T61_THING_TYPE, gatewayUID, sNodeId);
                    label = "T61: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T62_TEMPERATURE_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T62_THING_TYPE, gatewayUID, sNodeId);
                    label = "T62: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T63_HUMIDITY_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T63_THING_TYPE, gatewayUID, sNodeId);
                    label = "T63: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T64_LUX_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T64_THING_TYPE, gatewayUID, sNodeId);
                    label = "T64: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T65_VOLTAGE_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T65_THING_TYPE, gatewayUID, sNodeId);
                    label = "T65: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T66_CURRENT_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T66_THING_TYPE, gatewayUID, sNodeId);
                    label = "T66: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                case SoulissProtocolConstants.SOULISS_T67_POWER_SENSOR:
                    thingUID = new ThingUID(SoulissBindingConstants.T67_THING_TYPE, gatewayUID, sNodeId);
                    label = "T67: " + nodeLabel + " " + node + " " + slotLabel + " " + slot;
                    break;
                default: {
                    logger.debug("no supported things found ...");
                }
            }
            if (thingUID != null) {
                label = "[" + gwHandler.getThing().getUID().getAsString() + "] " + label;
                var uniqueId = "N" + Byte.toString(node) + "S" + Byte.toString(slot);
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                        .withProperty(SoulissBindingConstants.PROPERTY_NODE, node)
                        .withProperty(SoulissBindingConstants.PROPERTY_SLOT, slot)
                        .withProperty(SoulissBindingConstants.PROPERTY_UNIQUEID, uniqueId)
                        .withRepresentationProperty(SoulissBindingConstants.PROPERTY_UNIQUEID)
                        .withBridge(gwHandler.getThing().getUID()).build();
                thingDiscovered(discoveryResult);
                gwHandler.setThereIsAThingDetection();
            }
        }
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof SoulissGatewayHandler localGwHandler) {
            this.soulissGwHandler = localGwHandler;
            localGwHandler.discoverResult = this;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return soulissGwHandler;
    }
}
