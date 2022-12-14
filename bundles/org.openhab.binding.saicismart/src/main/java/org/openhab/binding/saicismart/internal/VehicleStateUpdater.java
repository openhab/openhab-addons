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

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_AUXILIARY_BATTERY_VOLTAGE;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_CHARGING;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_ENGINE;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_HEADING;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_LOCATION;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_MILAGE;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_RANGE_ELECTRIC;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_SOC;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_SPEED;

import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.openhab.binding.saicismart.internal.asn1.Util;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import net.heberling.ismart.asn1.v2_1.MP_DispatcherBody;
import net.heberling.ismart.asn1.v2_1.MP_DispatcherHeader;
import net.heberling.ismart.asn1.v2_1.Message;
import net.heberling.ismart.asn1.v2_1.MessageCoder;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusReq;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusResp25857;

/**
 *
 * @author Markus Heberling - Initial contribution
 */
class VehicleStateUpdater implements Callable<Boolean> {
    private final Logger logger = LoggerFactory.getLogger(VehicleStateUpdater.class);

    private final SAICiSMARTHandler saiCiSMARTHandler;

    public VehicleStateUpdater(SAICiSMARTHandler saiCiSMARTHandler) {
        this.saiCiSMARTHandler = saiCiSMARTHandler;
    }

    @Override
    public Boolean call() {
        try {
            Message<OTA_RVMVehicleStatusReq> chargingStatusMessage = new Message<>(new MP_DispatcherHeader(),
                    new byte[16], new MP_DispatcherBody(), new OTA_RVMVehicleStatusReq());
            Util.fillReserved(chargingStatusMessage.getReserved());

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

                if (chargingStatusResponseMessage.getBody().isErrorMessagePresent()) {
                    if (chargingStatusResponseMessage.getBody().getResult() == 2) {
                        saiCiSMARTHandler.getBridgeHandler().relogin();
                    }
                    throw new TimeoutException(new String(chargingStatusResponseMessage.getBody().getErrorMessage()));
                }

                chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
                chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());

                Util.fillReserved(chargingStatusMessage.getReserved());

                chargingStatusRequestMessage = new MessageCoder<>(OTA_RVMVehicleStatusReq.class)
                        .encodeRequest(chargingStatusMessage);

                chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                        "https://tap-eu.soimt.com/TAP.Web/ota.mpv21");

                chargingStatusResponseMessage = new MessageCoder<>(OTA_RVMVehicleStatusResp25857.class)
                        .decodeResponse(chargingStatusResponse);

            }

            logger.info("Got message: {}",
                    new GsonBuilder().setPrettyPrinting().create().toJson(chargingStatusResponseMessage));

            boolean engineRunning = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                    .getEngineStatus() == 1;
            boolean isCharging = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                    .isExtendedData2Present()
                    && chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                            .getExtendedData2() >= 1;
            saiCiSMARTHandler.updateState(CHANNEL_ENGINE, OnOffType.from(engineRunning));
            saiCiSMARTHandler.updateState(CHANNEL_CHARGING, OnOffType.from(isCharging));

            if (chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().isExtendedData1Present()
                    && !engineRunning && !isCharging) {
                // if the engine is running or we are charging, we will use the more precise data from the charging API
                saiCiSMARTHandler.updateState(CHANNEL_SOC, new QuantityType<>(
                        chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getExtendedData1(),
                        Units.PERCENT));
                saiCiSMARTHandler.updateState(CHANNEL_POWER, new QuantityType<>(0, MetricPrefix.KILO(Units.WATT)));
            }

            saiCiSMARTHandler.updateState(CHANNEL_AUXILIARY_BATTERY_VOLTAGE, new QuantityType<>(
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getBatteryVoltage()
                            / 10.d,
                    Units.VOLT));

            saiCiSMARTHandler.updateState(CHANNEL_SPEED, new QuantityType<>(
                    chargingStatusResponseMessage.getApplicationData().getGpsPosition().getWayPoint().getSpeed() / 10.d,
                    SIUnits.KILOMETRE_PER_HOUR));
            saiCiSMARTHandler.updateState(CHANNEL_HEADING, new QuantityType<>(
                    chargingStatusResponseMessage.getApplicationData().getGpsPosition().getWayPoint().getHeading(),
                    Units.DEGREE_ANGLE));
            saiCiSMARTHandler.updateState(CHANNEL_LOCATION,
                    new PointType(
                            new DecimalType(chargingStatusResponseMessage.getApplicationData().getGpsPosition()
                                    .getWayPoint().getPosition().getLatitude() / 1000000d),
                            new DecimalType(chargingStatusResponseMessage.getApplicationData().getGpsPosition()
                                    .getWayPoint().getPosition().getLongitude() / 1000000d),
                            new DecimalType(chargingStatusResponseMessage.getApplicationData().getGpsPosition()
                                    .getWayPoint().getPosition().getAltitude())));

            saiCiSMARTHandler.updateState(CHANNEL_MILAGE, new QuantityType<>(
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getMileage() / 10.d,
                    MetricPrefix.KILO(SIUnits.METRE)));
            saiCiSMARTHandler
                    .updateState(CHANNEL_RANGE_ELECTRIC,
                            new QuantityType<>(chargingStatusResponseMessage.getApplicationData()
                                    .getBasicVehicleStatus().getFuelRangeElec() / 10.d,
                                    MetricPrefix.KILO(SIUnits.METRE)));

            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_FRONT_LEFT,
                    new QuantityType<>(chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                            .getFrontLeftTyrePressure() * 4 / 100.d, Units.BAR));
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_FRONT_RIGHT,
                    new QuantityType<>(chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                            .getFrontRrightTyrePressure() * 4 / 100.d, Units.BAR));
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_REAR_LEFT,
                    new QuantityType<>(chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                            .getRearLeftTyrePressure() * 4 / 100.d, Units.BAR));
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_REAR_RIGHT,
                    new QuantityType<>(chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                            .getRearRightTyrePressure() * 4 / 100.d, Units.BAR));

            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_DOOR_DRIVER,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getDriverDoor()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_DOOR_PASSENGER,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getPassengerDoor()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_DOOR_REAR_LEFT,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRearLeftDoor()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_DOOR_REAR_RIGHT,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRearRightDoor()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);

            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_WINDOW_DRIVER,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getDriverWindow()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_WINDOW_PASSENGER,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getPassengerWindow()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_WINDOW_REAR_LEFT,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRearLeftWindow()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_WINDOW_REAR_RIGHT,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRearRightWindow()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_WINDOW_SUN_ROOF,
                    chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getSunroofStatus()
                            ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED);

            saiCiSMARTHandler.updateStatus(ThingStatus.ONLINE);
            return engineRunning || isCharging;
        } catch (URISyntaxException | ExecutionException | InterruptedException | TimeoutException e) {
            saiCiSMARTHandler.updateStatus(ThingStatus.OFFLINE);
            logger.error("Could not get vehicle data for {}", saiCiSMARTHandler.config.vin, e);
            return false;
        }
    }
}
