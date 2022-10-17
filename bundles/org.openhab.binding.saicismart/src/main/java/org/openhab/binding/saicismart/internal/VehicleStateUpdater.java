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
package org.openhab.binding.saicismart.internal;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.openhab.binding.saicismart.internal.asn1.v2_1.MP_DispatcherBody;
import org.openhab.binding.saicismart.internal.asn1.v2_1.MP_DispatcherHeader;
import org.openhab.binding.saicismart.internal.asn1.v2_1.Message;
import org.openhab.binding.saicismart.internal.asn1.v2_1.MessageCoder;
import org.openhab.binding.saicismart.internal.asn1.v2_1.OTA_RVMVehicleStatusReq;
import org.openhab.binding.saicismart.internal.asn1.v2_1.OTA_RVMVehicleStatusResp25857;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
class VehicleStateUpdater implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(VehicleStateUpdater.class);

    private final SAICiSMARTHandler saiCiSMARTHandler;

    public VehicleStateUpdater(SAICiSMARTHandler saiCiSMARTHandler) {
        this.saiCiSMARTHandler = saiCiSMARTHandler;
    }

    @Override
    public void run() {
        try {
            Message<OTA_RVMVehicleStatusReq> chargingStatusMessage = new Message<>(new MP_DispatcherHeader(),
                    new byte[16], new MP_DispatcherBody(), new OTA_RVMVehicleStatusReq());
            fillReserved(chargingStatusMessage);

            chargingStatusMessage.getBody().setApplicationID("511");
            chargingStatusMessage.getBody().setTestFlag(2);
            chargingStatusMessage.getBody().setVin(saiCiSMARTHandler.config.vin);
            chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
            chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());
            chargingStatusMessage.getBody().setMessageID(1);
            chargingStatusMessage.getBody().setEventCreationTime((int) Instant.now().getEpochSecond());
            chargingStatusMessage.getBody().setApplicationDataProtocolVersion(25857);
            chargingStatusMessage.getBody().setEventID(0);

            chargingStatusMessage.getApplicationData().setVehStatusReqType(1);

            String chargingStatusRequestMessage = new MessageCoder<>(OTA_RVMVehicleStatusReq.class)
                    .encodeRequest(chargingStatusMessage);

            String chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler()
                    .sendRequest(chargingStatusRequestMessage, "https://tap-eu.soimt.com/TAP.Web/ota.mpv21");

            Message<OTA_RVMVehicleStatusResp25857> chargingStatusResponseMessage = new MessageCoder<>(
                    OTA_RVMVehicleStatusResp25857.class).decodeResponse(chargingStatusResponse);

            // we get an eventId back...
            chargingStatusMessage.getBody().setEventID(chargingStatusResponseMessage.getBody().getEventID());
            // ... use that to request the data again, until we have it
            // TODO: check for real errors (result!=0 and/or errorMessagePresent)
            while (chargingStatusResponseMessage.getApplicationData() == null) {

                chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
                chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());

                fillReserved(chargingStatusMessage);

                chargingStatusRequestMessage = new MessageCoder<>(OTA_RVMVehicleStatusReq.class)
                        .encodeRequest(chargingStatusMessage);

                chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                        "https://tap-eu.soimt.com/TAP.Web/ota.mpv21");

                chargingStatusResponseMessage = new MessageCoder<>(OTA_RVMVehicleStatusResp25857.class)
                        .decodeResponse(chargingStatusResponse);

            }

            // saiCiSMARTHandler.updateState(CHANNEL_SOC, new QuantityType<>(
            // chargingStatusResponseMessage.getApplicationData().getBmsPackSOCDsp() / 10.d, Units.PERCENT));
            // saiCiSMARTHandler.updateState(CHANNEL_MILAGE, new QuantityType<>(
            // chargingStatusResponseMessage.getApplicationData().getChargeStatus().getMileage() / 10.d,
            // MetricPrefix.KILO(SIUnits.METRE)));
            // saiCiSMARTHandler.updateState(CHANNEL_RANGE_ELECTRIC, new QuantityType<>(
            // chargingStatusResponseMessage.getApplicationData().getChargeStatus().getFuelRangeElec() / 10.d,
            // MetricPrefix.KILO(SIUnits.METRE)));
            logger.info("Got message: {}", new GsonBuilder().setPrettyPrinting().create()
                    .toJson(chargingStatusResponseMessage.getApplicationData()));
            saiCiSMARTHandler.updateStatus(ThingStatus.ONLINE);
        } catch (URISyntaxException | ExecutionException | InterruptedException | TimeoutException e) {
            saiCiSMARTHandler.updateStatus(ThingStatus.OFFLINE);
            logger.error("Could not get vehicle data for {}", saiCiSMARTHandler.config.vin, e);
        }
    }

    public static void fillReserved(Message<?> chargingStatusMessage) {
        System.arraycopy(((new Random(System.currentTimeMillis())).nextLong() + "1111111111111111").getBytes(), 0,
                chargingStatusMessage.getReserved(), 0, 16);
    }
}
