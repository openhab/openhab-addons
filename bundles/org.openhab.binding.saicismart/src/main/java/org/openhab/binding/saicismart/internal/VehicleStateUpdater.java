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

import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.API_ENDPOINT_V21;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_AUXILIARY_BATTERY_VOLTAGE;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_CHARGING;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_ENGINE;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_HEADING;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_LOCATION;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_ODOMETER;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_RANGE_ELECTRIC;
import static org.openhab.binding.saicismart.internal.SAICiSMARTBindingConstants.CHANNEL_SPEED;

import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.saicismart.internal.exceptions.VehicleStatusAPIException;
import org.openhab.core.library.types.DateTimeType;
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

import net.heberling.ismart.asn1.v2_1.Message;
import net.heberling.ismart.asn1.v2_1.MessageCoder;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusReq;
import net.heberling.ismart.asn1.v2_1.entity.OTA_RVMVehicleStatusResp25857;

/**
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
class VehicleStateUpdater implements Callable<OTA_RVMVehicleStatusResp25857> {
    private final Logger logger = LoggerFactory.getLogger(VehicleStateUpdater.class);

    private final SAICiSMARTHandler saiCiSMARTHandler;

    public VehicleStateUpdater(SAICiSMARTHandler saiCiSMARTHandler) {
        this.saiCiSMARTHandler = saiCiSMARTHandler;
    }

    @Override
    public OTA_RVMVehicleStatusResp25857 call() throws URISyntaxException, ExecutionException, InterruptedException,
            TimeoutException, VehicleStatusAPIException {
        MessageCoder<OTA_RVMVehicleStatusReq> otaRvmVehicleStatusRequstMessageCoder = new MessageCoder<>(
                OTA_RVMVehicleStatusReq.class);

        OTA_RVMVehicleStatusReq otaRvmVehicleStatusReq = new OTA_RVMVehicleStatusReq();
        otaRvmVehicleStatusReq.setVehStatusReqType(1);

        Message<OTA_RVMVehicleStatusReq> chargingStatusMessage = otaRvmVehicleStatusRequstMessageCoder
                .initializeMessage(saiCiSMARTHandler.getBridgeHandler().getUid(),
                        saiCiSMARTHandler.getBridgeHandler().getToken(), saiCiSMARTHandler.config.vin, "511", 25857, 1,
                        otaRvmVehicleStatusReq);

        String chargingStatusRequestMessage = otaRvmVehicleStatusRequstMessageCoder
                .encodeRequest(chargingStatusMessage);

        String chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                API_ENDPOINT_V21);

        Message<OTA_RVMVehicleStatusResp25857> chargingStatusResponseMessage = new MessageCoder<>(
                OTA_RVMVehicleStatusResp25857.class).decodeResponse(chargingStatusResponse);

        // we get an eventId back...
        chargingStatusMessage.getBody().setEventID(chargingStatusResponseMessage.getBody().getEventID());
        // ... use that to request the data again, until we have it
        while (chargingStatusResponseMessage.getApplicationData() == null) {
            if (chargingStatusResponseMessage.getBody().getResult() != 0
                    || chargingStatusResponseMessage.getBody().isErrorMessagePresent()) {
                if (chargingStatusResponseMessage.getBody().getResult() == 2) {
                    saiCiSMARTHandler.getBridgeHandler().relogin();
                }
                throw new VehicleStatusAPIException(chargingStatusResponseMessage.getBody());
            }

            chargingStatusMessage.getBody().setUid(saiCiSMARTHandler.getBridgeHandler().getUid());
            chargingStatusMessage.getBody().setToken(saiCiSMARTHandler.getBridgeHandler().getToken());

            chargingStatusRequestMessage = otaRvmVehicleStatusRequstMessageCoder.encodeRequest(chargingStatusMessage);

            chargingStatusResponse = saiCiSMARTHandler.getBridgeHandler().sendRequest(chargingStatusRequestMessage,
                    API_ENDPOINT_V21);

            chargingStatusResponseMessage = new MessageCoder<>(OTA_RVMVehicleStatusResp25857.class)
                    .decodeResponse(chargingStatusResponse);
        }

        logger.trace("Got message: {}",
                new GsonBuilder().setPrettyPrinting().create().toJson(chargingStatusResponseMessage));

        boolean engineRunning = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                .getEngineStatus() == 1;
        boolean isCharging = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                .isExtendedData2Present()
                && chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getExtendedData2() >= 1;
        saiCiSMARTHandler.updateState(CHANNEL_ENGINE, OnOffType.from(engineRunning));
        saiCiSMARTHandler.updateState(CHANNEL_CHARGING, OnOffType.from(isCharging));

        saiCiSMARTHandler.updateState(CHANNEL_AUXILIARY_BATTERY_VOLTAGE, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getBatteryVoltage() / 10.d,
                Units.VOLT));

        saiCiSMARTHandler.updateState(CHANNEL_SPEED, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getGpsPosition().getWayPoint().getSpeed() / 10.d,
                SIUnits.KILOMETRE_PER_HOUR));
        saiCiSMARTHandler.updateState(CHANNEL_HEADING,
                new QuantityType<>(
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

        saiCiSMARTHandler.updateState(CHANNEL_ODOMETER,
                new QuantityType<>(
                        chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getMileage() / 10.d,
                        MetricPrefix.KILO(SIUnits.METRE)));
        saiCiSMARTHandler.updateState(CHANNEL_RANGE_ELECTRIC, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getFuelRangeElec() / 10.d,
                MetricPrefix.KILO(SIUnits.METRE)));

        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_FRONT_LEFT, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getFrontLeftTyrePressure()
                        * 4 / 100.d,
                Units.BAR));
        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_FRONT_RIGHT, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getFrontRrightTyrePressure()
                        * 4 / 100.d,
                Units.BAR));
        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_REAR_LEFT, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRearLeftTyrePressure() * 4
                        / 100.d,
                Units.BAR));
        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_TYRE_PRESSURE_REAR_RIGHT, new QuantityType<>(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRearRightTyrePressure()
                        * 4 / 100.d,
                Units.BAR));

        Integer interiorTemperature = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                .getInteriorTemperature();
        if (interiorTemperature > -128) {
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_INTERIOR_TEMPERATURE,
                    new QuantityType<>(interiorTemperature, SIUnits.CELSIUS));
        }
        Integer exteriorTemperature = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                .getExteriorTemperature();
        if (exteriorTemperature > -128) {
            saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_EXTERIOR_TEMPERATURE,
                    new QuantityType<>(exteriorTemperature, SIUnits.CELSIUS));
        }

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

        boolean acActive = chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus()
                .getRemoteClimateStatus() > 0;
        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_SWITCH_AC, OnOffType.from(acActive));
        saiCiSMARTHandler.updateState(SAICiSMARTBindingConstants.CHANNEL_REMOTE_AC_STATUS, new DecimalType(
                chargingStatusResponseMessage.getApplicationData().getBasicVehicleStatus().getRemoteClimateStatus()));

        saiCiSMARTHandler
                .updateState(SAICiSMARTBindingConstants.CHANNEL_LAST_POSITION_UPDATE,
                        new DateTimeType(ZonedDateTime.ofInstant(
                                Instant.ofEpochSecond(chargingStatusResponseMessage.getApplicationData()
                                        .getGpsPosition().getTimestamp4Short().getSeconds()),
                                saiCiSMARTHandler.getTimeZone())));

        if (isCharging || acActive || engineRunning) {
            // update activity date
            saiCiSMARTHandler.notifyCarActivity(ZonedDateTime.now(saiCiSMARTHandler.getTimeZone()), true);
        }

        saiCiSMARTHandler.updateStatus(ThingStatus.ONLINE);
        return chargingStatusResponseMessage.getApplicationData();
    }
}
