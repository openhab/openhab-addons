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
package org.openhab.binding.sinope.handler;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.openhab.binding.sinope.internal.core.SinopeDataReadRequest;
import org.openhab.binding.sinope.internal.core.SinopeDataWriteRequest;
import org.openhab.binding.sinope.internal.core.appdata.SinopeHeatLevelData;
import org.openhab.binding.sinope.internal.core.appdata.SinopeOutTempData;
import org.openhab.binding.sinope.internal.core.appdata.SinopeRoomTempData;
import org.openhab.binding.sinope.internal.core.appdata.SinopeSetPointModeData;
import org.openhab.binding.sinope.internal.core.appdata.SinopeSetPointTempData;
import org.openhab.binding.sinope.internal.core.base.SinopeDataAnswer;
import org.openhab.binding.sinope.internal.util.ByteUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SinopeThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pascal Larin - Initial contribution
 */
@NonNullByDefault
public class SinopeThermostatHandler extends BaseThingHandler {

    private static final int DATA_ANSWER = 0x0A;

    private Logger logger = LoggerFactory.getLogger(SinopeThermostatHandler.class);

    private byte[] deviceId = new byte[0];

    public SinopeThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getSinopeGatewayHandler() != null) {
            try {
                if (SinopeBindingConstants.CHANNEL_SETTEMP.equals(channelUID.getId())
                        && command instanceof QuantityType quantityCommand) {
                    setSetpointTemp(quantityCommand.floatValue());
                }
                if (SinopeBindingConstants.CHANNEL_SETMODE.equals(channelUID.getId())
                        && command instanceof DecimalType decimalCommand) {
                    setSetpointMode(decimalCommand.intValue());
                }
            } catch (IOException e) {
                logger.debug("Cannot handle command for channel {} because of {}", channelUID.getId(),
                        e.getLocalizedMessage());
                getSinopeGatewayHandler().setCommunicationError(true);
            }
        }
    }

    private void setSetpointTemp(float temp) throws UnknownHostException, IOException {
        int newTemp = (int) (temp * 100.0);

        getSinopeGatewayHandler().stopPoll();
        try {
            if (getSinopeGatewayHandler().connectToBridge()) {
                logger.debug("Connected to bridge");

                SinopeDataWriteRequest req = new SinopeDataWriteRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                        new SinopeSetPointTempData());
                ((SinopeSetPointTempData) req.getAppData()).setSetPointTemp(newTemp);

                SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);

                if (answ.getStatus() == DATA_ANSWER) {
                    logger.debug("Setpoint temp is now: {} C", temp);
                } else {
                    logger.debug("Cannot Setpoint temp, status: {}", answ.getStatus());
                }
            } else {
                logger.debug("Could not connect to bridge to update Setpoint Temp");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot connect to bridge");
            }
        } finally {
            getSinopeGatewayHandler().schedulePoll();
        }
    }

    private void setSetpointMode(int mode) throws UnknownHostException, IOException {
        getSinopeGatewayHandler().stopPoll();
        try {
            if (getSinopeGatewayHandler().connectToBridge()) {
                logger.debug("Connected to bridge");

                SinopeDataWriteRequest req = new SinopeDataWriteRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                        new SinopeSetPointModeData());
                ((SinopeSetPointModeData) req.getAppData()).setSetPointMode((byte) mode);

                SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);

                if (answ.getStatus() == DATA_ANSWER) {
                    logger.debug("Setpoint mode is now : {}", mode);
                } else {
                    logger.debug("Cannot Setpoint mode, status: {}", answ.getStatus());
                }
            } else {
                logger.debug("Could not connect to bridge to update Setpoint Temp");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Cannot connect to bridge");
            }
        } finally {
            getSinopeGatewayHandler().schedulePoll();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        updateDeviceId();
    }

    @Override
    public void initialize() {
        logger.debug("initializeThing thing {}", getThing().getUID());
        updateDeviceId();
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        super.updateConfiguration(configuration);
        updateDeviceId();
    }

    public void updateOutsideTemp(double temp) {
        updateState(SinopeBindingConstants.CHANNEL_OUTTEMP, new QuantityType<>(temp, SIUnits.CELSIUS));
    }

    public void updateRoomTemp(double temp) {
        updateState(SinopeBindingConstants.CHANNEL_INTEMP, new QuantityType<>(temp, SIUnits.CELSIUS));
    }

    public void updateSetPointTemp(double temp) {
        updateState(SinopeBindingConstants.CHANNEL_SETTEMP, new QuantityType<>(temp, SIUnits.CELSIUS));
    }

    public void updateSetPointMode(int mode) {
        updateState(SinopeBindingConstants.CHANNEL_SETMODE, new DecimalType(mode));
    }

    public void updateHeatingLevel(int heatingLevel) {
        updateState(SinopeBindingConstants.CHANNEL_HEATINGLEVEL, new DecimalType(heatingLevel));
    }

    public void update() throws UnknownHostException, IOException {
        if (this.deviceId.length > 0 && getSinopeGatewayHandler() != null) {
            if (isLinked(SinopeBindingConstants.CHANNEL_OUTTEMP)) {
                this.updateOutsideTemp(readOutsideTemp());
            }
            if (isLinked(SinopeBindingConstants.CHANNEL_INTEMP)) {
                this.updateRoomTemp(readRoomTemp());
            }
            if (isLinked(SinopeBindingConstants.CHANNEL_SETTEMP)) {
                this.updateSetPointTemp(readSetpointTemp());
            }
            if (isLinked(SinopeBindingConstants.CHANNEL_SETMODE)) {
                this.updateSetPointMode(readSetpointMode());
            }
            if (isLinked(SinopeBindingConstants.CHANNEL_HEATINGLEVEL)) {
                this.updateHeatingLevel(readHeatLevel());
            }
        } else {
            logger.error("Device id is null for Thing UID: {}", getThing().getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private double readRoomTemp() throws UnknownHostException, IOException {
        logger.debug("Reading room temp for device id : {}", ByteUtil.toString(deviceId));
        SinopeDataReadRequest req = new SinopeDataReadRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeRoomTempData());
        SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);
        double temp = ((SinopeRoomTempData) answ.getAppData()).getRoomTemp() / 100.0;
        logger.debug("Room temp is : {} C", temp);
        return temp;
    }

    private double readOutsideTemp() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeOutTempData());
        logger.debug("Reading outside temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);
        double temp = ((SinopeOutTempData) answ.getAppData()).getOutTemp() / 100.0;
        logger.debug("Outside temp is : {} C", temp);
        return temp;
    }

    private double readSetpointTemp() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeSetPointTempData());
        logger.debug("Reading Set Point temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);
        double temp = ((SinopeSetPointTempData) answ.getAppData()).getSetPointTemp() / 100.0;
        logger.debug("Setpoint temp is : {} C", temp);
        return temp;
    }

    private int readSetpointMode() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeSetPointModeData());
        logger.debug("Reading Set Point mode for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);
        int mode = ((SinopeSetPointModeData) answ.getAppData()).getSetPointMode();
        logger.debug("Setpoint mode is : {}", mode);
        return mode;
    }

    private int readHeatLevel() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(getSinopeGatewayHandler().newSeq(), deviceId,
                new SinopeHeatLevelData());
        logger.debug("Reading Heat Level for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) getSinopeGatewayHandler().execute(req);
        int level = ((SinopeHeatLevelData) answ.getAppData()).getHeatLevel();
        logger.debug("Heating level is  : {}", level);
        return level;
    }

    private synchronized void updateDeviceId() {
        String sDeviceId = (String) getConfig().get(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID);
        this.deviceId = SinopeConfig.convert(sDeviceId);
        if (this.deviceId.length == 0) {
            logger.debug("Invalid Device id, cannot convert id: {}", sDeviceId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid Device id");
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        SinopeGatewayHandler handler = getSinopeGatewayHandler();
        if (handler != null) {
            handler.registerThermostatHandler(this);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private @Nullable SinopeGatewayHandler getSinopeGatewayHandler() {
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            return (SinopeGatewayHandler) bridge.getHandler();
        } else {
            return null;
        }
    }
}
