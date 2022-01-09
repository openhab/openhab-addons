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
package org.openhab.binding.enocean.internal.discovery;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.enocean.internal.eep.Base.UTEResponse;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.eep.EEP;
import org.openhab.binding.enocean.internal.eep.EEPFactory;
import org.openhab.binding.enocean.internal.handler.EnOceanBridgeHandler;
import org.openhab.binding.enocean.internal.messages.BasePacket;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.binding.enocean.internal.messages.ERP1Message.RORG;
import org.openhab.binding.enocean.internal.messages.EventMessage;
import org.openhab.binding.enocean.internal.messages.EventMessage.EventMessageType;
import org.openhab.binding.enocean.internal.messages.Responses.SMACKTeachInResponse;
import org.openhab.binding.enocean.internal.transceiver.TeachInListener;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnOceanDeviceDiscoveryService} is used to discover Enocean devices and to accept teach in requests.
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanDeviceDiscoveryService extends AbstractDiscoveryService implements TeachInListener {
    private final Logger logger = LoggerFactory.getLogger(EnOceanDeviceDiscoveryService.class);

    private EnOceanBridgeHandler bridgeHandler;
    private ThingManager thingManager;

    public EnOceanDeviceDiscoveryService(EnOceanBridgeHandler bridgeHandler, ThingManager thingManager) {
        super(null, 60, false);
        this.bridgeHandler = bridgeHandler;
        this.thingManager = thingManager;
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
    public void packetReceived(BasePacket packet) {
        ERP1Message msg = (ERP1Message) packet;

        logger.info("EnOcean Package discovered, RORG {}, payload {}, additional {}", msg.getRORG().name(),
                HexUtils.bytesToHex(msg.getPayload()), HexUtils.bytesToHex(msg.getOptionalPayload()));

        EEP eep = EEPFactory.buildEEPFromTeachInERP1(msg);
        if (eep == null) {
            logger.debug("Could not build EEP for received package");
            return;
        }

        String enoceanId = HexUtils.bytesToHex(eep.getSenderId());

        bridgeHandler.getThing().getThings().stream()
                .filter(t -> t.getConfiguration().getProperties().getOrDefault(PARAMETER_ENOCEANID, EMPTYENOCEANID)
                        .toString().equals(enoceanId))
                .findFirst().ifPresentOrElse(t -> {
                    // If repeated learn is not allowed => send teach out
                    // otherwise do nothing
                    if (bridgeHandler.sendTeachOuts()) {
                        sendTeachOutResponse(msg, enoceanId, t);
                        thingManager.setEnabled(t.getUID(), false);
                    }
                }, () -> {
                    Integer senderIdOffset = null;
                    boolean broadcastMessages = true;

                    // check for bidirectional communication => do not use broadcast in this case
                    if (msg.getRORG() == RORG.UTE && (msg.getPayload(1, 1)[0]
                            & UTEResponse.CommunicationType_MASK) == UTEResponse.CommunicationType_MASK) {
                        broadcastMessages = false;
                    }

                    if (msg.getRORG() == RORG.UTE && (msg.getPayload(1, 1)[0] & UTEResponse.ResponseNeeded_MASK) == 0) {
                        // if ute => send response if needed
                        logger.debug("Sending UTE response to {}", enoceanId);
                        senderIdOffset = sendTeachInResponse(msg, enoceanId);
                        if (senderIdOffset == null) {
                            return;
                        }
                    } else if ((eep instanceof _4BSMessage) && ((_4BSMessage) eep).isTeachInVariation3Supported()) {
                        // if 4BS teach in variation 3 => send response
                        logger.debug("Sending 4BS teach in variation 3 response to {}", enoceanId);
                        senderIdOffset = sendTeachInResponse(msg, enoceanId);
                        if (senderIdOffset == null) {
                            return;
                        }
                    }

                    createDiscoveryResult(eep, broadcastMessages, senderIdOffset);
                });
    }

    @Override
    public void eventReceived(EventMessage event) {
        if (event.getEventMessageType() == EventMessageType.SA_CONFIRM_LEARN) {
            EEP eep = EEPFactory.buildEEPFromTeachInSMACKEvent(event);
            if (eep == null) {
                return;
            }

            SMACKTeachInResponse response = EEPFactory.buildResponseFromSMACKTeachIn(event,
                    bridgeHandler.sendTeachOuts());
            if (response != null) {
                bridgeHandler.sendMessage(response, null);

                if (response.isTeachIn()) {
                    // SenderIdOffset will be determined during Thing init
                    createDiscoveryResult(eep, false, -1);
                } else if (response.isTeachOut()) {
                    // disable already teached in thing
                    bridgeHandler.getThing().getThings().stream()
                            .filter(t -> t.getConfiguration().getProperties()
                                    .getOrDefault(PARAMETER_ENOCEANID, EMPTYENOCEANID).toString()
                                    .equals(HexUtils.bytesToHex(eep.getSenderId())))
                            .findFirst().ifPresentOrElse(t -> {
                                thingManager.setEnabled(t.getUID(), false);
                                logger.info("Disable thing with id {}", t.getUID());
                            }, () -> {
                                logger.info("Thing for EnOceanId {} already deleted",
                                        HexUtils.bytesToHex(eep.getSenderId()));
                            });
                }
            }
        }
    }

    private Integer sendTeachInResponse(ERP1Message msg, String enoceanId) {
        // get new sender Id
        Integer offset = bridgeHandler.getNextSenderId(enoceanId);
        if (offset != null) {
            byte[] newSenderId = bridgeHandler.getBaseId();
            newSenderId[3] += offset;

            // send response
            EEP response = EEPFactory.buildResponseEEPFromTeachInERP1(msg, newSenderId, true);
            if (response != null) {
                bridgeHandler.sendMessage(response.getERP1Message(), null);
                logger.debug("Teach in response for {} with new senderId {} (= offset {}) sent", enoceanId,
                        HexUtils.bytesToHex(newSenderId), offset);
            } else {
                logger.warn("Teach in response for enoceanId {} not supported!", enoceanId);
            }
        } else {
            logger.warn("Could not get new SenderIdOffset");
        }
        return offset;
    }

    private void sendTeachOutResponse(ERP1Message msg, String enoceanId, Thing thing) {
        byte[] senderId = bridgeHandler.getBaseId();
        senderId[3] += (byte) thing.getConfiguration().getProperties().getOrDefault(PARAMETER_SENDERIDOFFSET, 0);

        // send response
        EEP response = EEPFactory.buildResponseEEPFromTeachInERP1(msg, senderId, false);
        if (response != null) {
            bridgeHandler.sendMessage(response.getERP1Message(), null);
            logger.debug("Teach out response for thing {} with EnOceanId {} sent", thing.getUID().getId(), enoceanId);
        } else {
            logger.warn("Teach out response for enoceanId {} not supported!", enoceanId);
        }
    }

    protected void createDiscoveryResult(EEP eep, boolean broadcastMessages, Integer senderIdOffset) {
        String enoceanId = HexUtils.bytesToHex(eep.getSenderId());
        ThingTypeUID thingTypeUID = eep.getThingTypeUID();
        ThingUID thingUID = new ThingUID(thingTypeUID, bridgeHandler.getThing().getUID(), enoceanId);

        DiscoveryResultBuilder discoveryResultBuilder = DiscoveryResultBuilder.create(thingUID)
                .withRepresentationProperty(PARAMETER_ENOCEANID).withProperty(PARAMETER_ENOCEANID, enoceanId)
                .withProperty(PARAMETER_BROADCASTMESSAGES, broadcastMessages)
                .withBridge(bridgeHandler.getThing().getUID());

        eep.addConfigPropertiesTo(discoveryResultBuilder);

        if (senderIdOffset != null) {
            // advance config with new device id
            discoveryResultBuilder.withProperty(PARAMETER_SENDERIDOFFSET, senderIdOffset);
        }

        thingDiscovered(discoveryResultBuilder.build());
    }

    @Override
    public long getEnOceanIdToListenTo() {
        // we just want teach in msg, so return zero here
        return 0;
    }
}
