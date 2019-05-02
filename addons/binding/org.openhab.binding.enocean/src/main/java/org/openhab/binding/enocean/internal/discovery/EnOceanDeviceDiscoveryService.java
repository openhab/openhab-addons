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
package org.openhab.binding.enocean.internal.discovery;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.eep.Base.UTEResponse;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.handler.EnOceanBridgeHandler;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.ESP3Packet;
import org.openhab.binding.enocean.internal.transceiver.ESP3PacketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnOceanDeviceDiscoveryService} is used to discover Enocean devices and to accept teach in requests.
 *
 * @author Daniel Weber - Initial contribution
 */

public class EnOceanDeviceDiscoveryService extends AbstractDiscoveryService implements ESP3PacketListener {
    private final Logger logger = LoggerFactory.getLogger(EnOceanDeviceDiscoveryService.class);

    private EnOceanBridgeHandler bridgeHandler;

    public EnOceanDeviceDiscoveryService(EnOceanBridgeHandler bridgeHandler) {
        super(null, 60, false);
        this.bridgeHandler = bridgeHandler;
    }

    /**
     * Called on component activation.
     */
    public void activate() {
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected void startScan() {
        if (bridgeHandler == null) {
            return;
        }

        logger.info("Starting EnOcean discovery and accepting teach in requests");
        bridgeHandler.startDiscovery(this);
    }

    @Override
    public synchronized void stopScan() {
        if (bridgeHandler == null) {
            return;
        }

        logger.info("Stopping EnOcean discovery scan");
        bridgeHandler.stopDiscovery();
        super.stopScan();
    }

    @Override
    public Set<@NonNull ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void espPacketReceived(ESP3Packet packet) {
        ERP1Message msg = (ERP1Message) packet;

        logger.info("EnOcean Package discovered, RORG {}, payload {}, additional {}", msg.getRORG().name(),
                HexUtils.bytesToHex(msg.getPayload()), HexUtils.bytesToHex(msg.getOptionalPayload()));

        EEP eep = EEPFactory.buildEEPFromTeachInERP1(msg);
        if (eep == null) {
            return;
        }

        String enoceanId = HexUtils.bytesToHex(eep.getSenderId());
        ThingTypeUID thingTypeUID = eep.getThingTypeUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeHandler.getThing().getUID(), enoceanId);

        int senderIdOffset = 0;
        boolean broadcastMessages = true;

        // check for bidirectional communication => do not use broadcast in this case
        if (msg.getRORG() == RORG.UTE && (msg.getPayload(1, 1)[0]
                & UTEResponse.CommunicationType_MASK) == UTEResponse.CommunicationType_MASK) {
            broadcastMessages = false;
        }

        // if ute => send response if needed
        if (msg.getRORG() == RORG.UTE && (msg.getPayload(1, 1)[0] & UTEResponse.ResponseNeeded_MASK) == 0) {
            logger.info("Sending UTE response to {}", enoceanId);
            senderIdOffset = sendTeachInResponse(msg, enoceanId);
        }

        // if 4BS teach in variation 3 => send response
        if ((eep instanceof _4BSMessage) && ((_4BSMessage) eep).isTeachInVariation3Supported()) {
            logger.info("Sending 4BS teach in variation 3 response to {}", enoceanId);
            senderIdOffset = sendTeachInResponse(msg, enoceanId);
        }

        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID)
                .withRepresentationProperty(enoceanId).withBridge(bridgeHandler.getThing().getUID());

        eep.addConfigPropertiesTo(discoveryResultBuilder);
        discoveryResultBuilder.withProperty(PARAMETER_BROADCASTMESSAGES, broadcastMessages);
        discoveryResultBuilder.withProperty(PARAMETER_ENOCEANID, enoceanId);

        if (senderIdOffset > 0) {
            // advance config with new device id
            discoveryResultBuilder.withProperty(PARAMETER_SENDERIDOFFSET, senderIdOffset);
        }

        thingDiscovered(discoveryResultBuilder.build());

        // As we only support sensors to be teached in, we do not need to send a teach in response => 4bs
        // bidirectional teach in proc is not supported yet
        // this is true except for UTE teach in => we always have to send a response here
    }

    private int sendTeachInResponse(ERP1Message msg, String enoceanId) {
        int offset;
        // get new sender Id
        offset = bridgeHandler.getNextSenderId(enoceanId);
        if (offset > 0) {
            byte[] newSenderId = bridgeHandler.getBaseId();
            newSenderId[3] += offset;

            // send response
            EEP response = EEPFactory.buildResponseEEPFromTeachInERP1(msg, newSenderId);
            if (response != null) {
                bridgeHandler.sendMessage(response.getERP1Message(), null);
                logger.info("Teach in response for {} with new senderId {} (= offset {}) sent", enoceanId,
                        HexUtils.bytesToHex(newSenderId), offset);
            } else {
                logger.warn("Teach in response for enoceanId {} not supported!", enoceanId);
            }
        }
        return offset;
    }

    @Override
    public long getSenderIdToListenTo() {
        // we just want teach in msg, so return zero here
        return 0;
    }

}
