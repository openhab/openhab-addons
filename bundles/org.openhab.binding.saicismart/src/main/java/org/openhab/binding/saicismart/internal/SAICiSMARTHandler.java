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

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.*;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bn.coders.IASN1PreparedElement;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.saicismart.internal.asn1.v3_0.MP_DispatcherBody;
import org.openhab.binding.saicismart.internal.asn1.v3_0.MP_DispatcherHeader;
import org.openhab.binding.saicismart.internal.asn1.v3_0.Message;
import org.openhab.binding.saicismart.internal.asn1.v3_0.MessageCoder;
import org.openhab.binding.saicismart.internal.asn1.v3_0.OTA_ChrgMangDataResp;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SAICiSMARTHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class SAICiSMARTHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SAICiSMARTHandler.class);

    private @Nullable SAICiSMARTVehicleConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;

    public SAICiSMARTHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands supported yet
    }

    protected @Nullable SAICiSMARTBridgeHandler getBridgeHandler() {
        return (SAICiSMARTBridgeHandler) super.getBridge().getHandler();
    }

    @Override
    public void initialize() {
        config = getConfigAs(SAICiSMARTVehicleConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                Message<IASN1PreparedElement> chargingStatusMessage = new Message<>(new MP_DispatcherHeader(),
                        new byte[16], new MP_DispatcherBody(), null);
                fillReserved(chargingStatusMessage);

                chargingStatusMessage.getBody().setApplicationID("516");
                chargingStatusMessage.getBody().setTestFlag(2);
                chargingStatusMessage.getBody().setVin(config.vin);
                chargingStatusMessage.getBody().setUid(getBridgeHandler().getUid());
                chargingStatusMessage.getBody().setToken(getBridgeHandler().getToken());
                chargingStatusMessage.getBody().setMessageID(5);
                chargingStatusMessage.getBody().setEventCreationTime((int) Instant.now().getEpochSecond());
                chargingStatusMessage.getBody().setApplicationDataProtocolVersion(768);
                chargingStatusMessage.getBody().setEventID(0);

                String chargingStatusRequestMessage = new MessageCoder<>(IASN1PreparedElement.class)
                        .encodeRequest(chargingStatusMessage);

                String chargingStatusResponse = getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                        "https://tap-eu.soimt.com/TAP.Web/ota.mpv30");

                Message<OTA_ChrgMangDataResp> chargingStatusResponseMessage = new MessageCoder<>(
                        OTA_ChrgMangDataResp.class).decodeResponse(chargingStatusResponse);

                // we get an eventId back...
                chargingStatusMessage.getBody().setEventID(chargingStatusResponseMessage.getBody().getEventID());
                // ... use that to request the data again, until we have it
                // TODO: check for real errors (result!=0 and/or errorMessagePresent)
                while (chargingStatusResponseMessage.getApplicationData() == null) {

                    fillReserved(chargingStatusMessage);

                    chargingStatusRequestMessage = new MessageCoder<>(IASN1PreparedElement.class)
                            .encodeRequest(chargingStatusMessage);

                    chargingStatusResponse = getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                            "https://tap-eu.soimt.com/TAP.Web/ota.mpv30");

                    chargingStatusResponseMessage = new MessageCoder<>(OTA_ChrgMangDataResp.class)
                            .decodeResponse(chargingStatusResponse);

                }

                updateState(CHANNEL_SOC, new QuantityType<>(
                        chargingStatusResponseMessage.getApplicationData().getBmsPackSOCDsp() / 10.d, Units.PERCENT));
                updateState(CHANNEL_MILAGE, new QuantityType<>(
                        chargingStatusResponseMessage.getApplicationData().getChargeStatus().getMileage() / 10.d,
                        MetricPrefix.KILO(SIUnits.METRE)));
                updateState(CHANNEL_RANGE_ELECTRIC,
                        new QuantityType<>(chargingStatusResponseMessage.getApplicationData().getChargeStatus().getFuelRangeElec() / 10.d,
                                MetricPrefix.KILO(SIUnits.METRE)));

                updateStatus(ThingStatus.ONLINE);
            } catch (URISyntaxException | ExecutionException | InterruptedException | TimeoutException e) {
                updateStatus(ThingStatus.OFFLINE);
                logger.error("Could not get vehicle data for {}", config.vin, e);
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    private static void fillReserved(Message<IASN1PreparedElement> chargingStatusMessage) {
        System.arraycopy(((new Random(System.currentTimeMillis())).nextLong() + "1111111111111111").getBytes(), 0,
                chargingStatusMessage.getReserved(), 0, 16);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }
}
