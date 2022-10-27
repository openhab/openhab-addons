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

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_SOC;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bn.coders.IASN1PreparedElement;
import org.openhab.binding.saicismart.internal.asn1.Util;
import org.openhab.binding.saicismart.internal.asn1.v3_0.MP_DispatcherBody;
import org.openhab.binding.saicismart.internal.asn1.v3_0.MP_DispatcherHeader;
import org.openhab.binding.saicismart.internal.asn1.v3_0.Message;
import org.openhab.binding.saicismart.internal.asn1.v3_0.MessageCoder;
import org.openhab.binding.saicismart.internal.asn1.v3_0.entity.OTA_ChrgMangDataResp;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
class ChargeStateUpdater implements Callable<Boolean> {
    private final Logger logger = LoggerFactory.getLogger(ChargeStateUpdater.class);

    private final SAICiSMARTHandler saiCiSMARTHandler;

    public ChargeStateUpdater(SAICiSMARTHandler saiCiSMARTHandler) {
        this.saiCiSMARTHandler = saiCiSMARTHandler;
    }

    public Boolean call() {
        try {
            Message<IASN1PreparedElement> chargingStatusMessage = new Message<>(new MP_DispatcherHeader(), new byte[16],
                    new MP_DispatcherBody(), null);
            Util.fillReserved(chargingStatusMessage.getReserved());

            chargingStatusMessage.getBody().setApplicationID("516");
            chargingStatusMessage.getBody().setTestFlag(2);
            chargingStatusMessage.getBody().setVin(saiCiSMARTHandler.config.vin);
            chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
            chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());
            chargingStatusMessage.getBody().setMessageID(5);
            chargingStatusMessage.getBody().setEventCreationTime((int) Instant.now().getEpochSecond());
            chargingStatusMessage.getBody().setApplicationDataProtocolVersion(768);
            chargingStatusMessage.getBody().setEventID(0);

            String chargingStatusRequestMessage = new MessageCoder<>(IASN1PreparedElement.class)
                    .encodeRequest(chargingStatusMessage);

            String chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler()
                    .sendRequest(chargingStatusRequestMessage, "https://tap-eu.soimt.com/TAP.Web/ota.mpv30");

            Message<OTA_ChrgMangDataResp> chargingStatusResponseMessage = new MessageCoder<>(OTA_ChrgMangDataResp.class)
                    .decodeResponse(chargingStatusResponse);

            // we get an eventId back...
            chargingStatusMessage.getBody().setEventID(chargingStatusResponseMessage.getBody().getEventID());
            // ... use that to request the data again, until we have it
            // TODO: check for real errors (result!=0 and/or errorMessagePresent)
            while (chargingStatusResponseMessage.getApplicationData() == null) {

                if (chargingStatusResponseMessage.getBody().isErrorMessagePresent()) {
                    if (chargingStatusResponseMessage.getBody().getResult() == 2) {
                        saiCiSMARTHandler.getBridgeHandler().relogin();
                    }
                    throw new TimeoutException(new String(chargingStatusResponseMessage.getBody().getErrorMessage()));
                }

                chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
                chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());

                Util.fillReserved(chargingStatusMessage.getReserved());

                chargingStatusRequestMessage = new MessageCoder<>(IASN1PreparedElement.class)
                        .encodeRequest(chargingStatusMessage);

                chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                        "https://tap-eu.soimt.com/TAP.Web/ota.mpv30");

                chargingStatusResponseMessage = new MessageCoder<>(OTA_ChrgMangDataResp.class)
                        .decodeResponse(chargingStatusResponse);

            }

            saiCiSMARTHandler.updateState(CHANNEL_SOC, new QuantityType<>(
                    chargingStatusResponseMessage.getApplicationData().getBmsPackSOCDsp() / 10.d, Units.PERCENT));
            logger.info("Got message: {}", new GsonBuilder().setPrettyPrinting().create()
                    .toJson(chargingStatusResponseMessage.getApplicationData()));

            double power = (chargingStatusResponseMessage.getApplicationData().getBmsPackCrnt() * 0.05d - 1000.0d)
                    * ((double) chargingStatusResponseMessage.getApplicationData().getBmsPackVol() * 0.25d) / 1000d;

            saiCiSMARTHandler.updateState(CHANNEL_POWER, new QuantityType<>(power, MetricPrefix.KILO(Units.WATT)));

            saiCiSMARTHandler.updateStatus(ThingStatus.ONLINE);
        } catch (URISyntaxException | ExecutionException | InterruptedException | TimeoutException e) {
            saiCiSMARTHandler.updateStatus(ThingStatus.OFFLINE);
            logger.error("Could not get vehicle data for {}", saiCiSMARTHandler.config.vin, e);
        }
        return false;
    }
}
