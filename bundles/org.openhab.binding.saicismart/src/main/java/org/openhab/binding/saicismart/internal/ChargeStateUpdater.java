/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.API_ENDPOINT_V30;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_SOC;

import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.bn.coders.IASN1PreparedElement;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.saicismart.internal.exceptions.ChargingStatusAPIException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import net.heberling.ismart.asn1.v3_0.Message;
import net.heberling.ismart.asn1.v3_0.MessageCoder;
import net.heberling.ismart.asn1.v3_0.entity.OTA_ChrgMangDataResp;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.FIELD,
        DefaultLocation.TYPE_BOUND })
class ChargeStateUpdater implements Callable<OTA_ChrgMangDataResp> {
    private final Logger logger = LoggerFactory.getLogger(ChargeStateUpdater.class);

    private final SAICiSMARTHandler saiCiSMARTHandler;

    public ChargeStateUpdater(SAICiSMARTHandler saiCiSMARTHandler) {
        this.saiCiSMARTHandler = saiCiSMARTHandler;
    }

    public OTA_ChrgMangDataResp call() throws URISyntaxException, ExecutionException, InterruptedException,
            TimeoutException, ChargingStatusAPIException {
        MessageCoder<IASN1PreparedElement> chargingStatusRequestmessageCoder = new MessageCoder<>(
                IASN1PreparedElement.class);
        Message<IASN1PreparedElement> chargingStatusMessage = chargingStatusRequestmessageCoder.initializeMessage(
                saiCiSMARTHandler.getBridgeHandler().getUid(), saiCiSMARTHandler.getBridgeHandler().getToken(),
                saiCiSMARTHandler.config.vin, "516", 768, 5, null);

        String chargingStatusRequestMessage = chargingStatusRequestmessageCoder.encodeRequest(chargingStatusMessage);

        String chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                API_ENDPOINT_V30);

        Message<OTA_ChrgMangDataResp> chargingStatusResponseMessage = new MessageCoder<>(OTA_ChrgMangDataResp.class)
                .decodeResponse(chargingStatusResponse);

        // we get an eventId back...
        chargingStatusMessage.getBody().setEventID(chargingStatusResponseMessage.getBody().getEventID());
        // ... use that to request the data again, until we have it
        while (chargingStatusResponseMessage.getApplicationData() == null) {
            if (chargingStatusResponseMessage.getBody().getResult() != 0
                    || chargingStatusResponseMessage.getBody().isErrorMessagePresent()) {
                if (chargingStatusResponseMessage.getBody().getResult() == 2) {
                    saiCiSMARTHandler.getBridgeHandler().relogin();
                }
                throw new ChargingStatusAPIException(chargingStatusResponseMessage.getBody());
            }

            chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
            chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());

            chargingStatusRequestMessage = chargingStatusRequestmessageCoder.encodeRequest(chargingStatusMessage);

            chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                    API_ENDPOINT_V30);

            chargingStatusResponseMessage = new MessageCoder<>(OTA_ChrgMangDataResp.class)
                    .decodeResponse(chargingStatusResponse);
        }
        saiCiSMARTHandler.updateState(CHANNEL_SOC,
                new DecimalType(chargingStatusResponseMessage.getApplicationData().getBmsPackSOCDsp() / 10.d));
        logger.debug("Got message: {}",
                new GsonBuilder().setPrettyPrinting().create().toJson(chargingStatusResponseMessage));

        Double power = (chargingStatusResponseMessage.getApplicationData().getBmsPackCrnt() * 0.05d - 1000.0d)
                * ((double) chargingStatusResponseMessage.getApplicationData().getBmsPackVol() * 0.25d);

        saiCiSMARTHandler.updateState(CHANNEL_POWER, new QuantityType<>(power.intValue(), Units.WATT));

        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_LAST_CHARGE_STATE_UPDATE,
                new DateTimeType(ZonedDateTime.now(saiCiSMARTHandler.getTimeZone())));

        saiCiSMARTHandler.updateStatus(ThingStatus.ONLINE);
        return chargingStatusResponseMessage.getApplicationData();
    }
}
