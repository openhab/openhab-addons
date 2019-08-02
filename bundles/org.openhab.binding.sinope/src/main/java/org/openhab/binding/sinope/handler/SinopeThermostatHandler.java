/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sinope.handler;

import java.io.IOException;
import java.net.UnknownHostException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.sinope.SinopeBindingConstants;
import org.openhab.binding.sinope.internal.config.SinopeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.tulip.sinope.core.SinopeDataReadRequest;
import ca.tulip.sinope.core.SinopeDataWriteRequest;
import ca.tulip.sinope.core.appdata.SinopeHeatLevelData;
import ca.tulip.sinope.core.appdata.SinopeOutTempData;
import ca.tulip.sinope.core.appdata.SinopeRoomTempData;
import ca.tulip.sinope.core.appdata.SinopeSetPointModeData;
import ca.tulip.sinope.core.appdata.SinopeSetPointTempData;
import ca.tulip.sinope.core.internal.SinopeDataAnswer;
import ca.tulip.sinope.util.ByteUtil;

/**
 * The {@link SinopeThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeThermostatHandler extends BaseThingHandler {

    private static final int DATA_ANSWER = 0x0A;

    private Logger logger = LoggerFactory.getLogger(SinopeThermostatHandler.class);

    private SinopeGatewayHandler gatewayHandler;

    private byte[] deviceId;

    public SinopeThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        try {
            if (SinopeBindingConstants.CHANNEL_SETTEMP.equals(channelUID.getId()) && command instanceof QuantityType) {
                setSetpointTemp(((QuantityType<?>) command).floatValue());
            }
            if (SinopeBindingConstants.CHANNEL_SETMODE.equals(channelUID.getId()) && command instanceof DecimalType) {
                setSetpointMode(((DecimalType) command).intValue());
            }
        } catch (IOException e) {
            logger.debug("Cannot handle command for channel {} because of {}", channelUID.getId(),
                    e.getLocalizedMessage());
            this.gatewayHandler.setCommunicationError(true);
        }
    }

    public void setSetpointTemp(float temp) throws UnknownHostException, IOException {
        int newTemp = (int) (temp * 100.0);

        this.gatewayHandler.stopPoll();
        try {
            if (this.gatewayHandler.connectToBridge()) {
                logger.debug("Connected to bridge");

                SinopeDataWriteRequest req = new SinopeDataWriteRequest(this.gatewayHandler.newSeq(), deviceId,
                        new SinopeSetPointTempData());
                ((SinopeSetPointTempData) req.getAppData()).setSetPointTemp(newTemp);

                SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);

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
            this.gatewayHandler.schedulePoll();
        }

    }

    public void setSetpointMode(int mode) throws UnknownHostException, IOException {
        this.gatewayHandler.stopPoll();
        try {
            if (this.gatewayHandler.connectToBridge()) {
                logger.debug("Connected to bridge");

                SinopeDataWriteRequest req = new SinopeDataWriteRequest(this.gatewayHandler.newSeq(), deviceId,
                        new SinopeSetPointModeData());
                ((SinopeSetPointModeData) req.getAppData()).setSetPointMode((byte) mode);

                SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);

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
            this.gatewayHandler.schedulePoll();
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
        if (this.deviceId != null) {
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
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.gatewayHandler.newSeq(), deviceId,
                new SinopeRoomTempData());
        SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);
        double temp = ((SinopeRoomTempData) answ.getAppData()).getRoomTemp() / 100.0;
        logger.debug("Room temp is : {} C", temp);
        return temp;
    }

    private double readOutsideTemp() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.gatewayHandler.newSeq(), deviceId,
                new SinopeOutTempData());
        logger.debug("Reading outside temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);
        double temp = ((SinopeOutTempData) answ.getAppData()).getOutTemp() / 100.0;
        logger.debug("Outside temp is : {} C", temp);
        return temp;
    }

    private double readSetpointTemp() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.gatewayHandler.newSeq(), deviceId,
                new SinopeSetPointTempData());
        logger.debug("Reading Set Point temp for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);
        double temp = ((SinopeSetPointTempData) answ.getAppData()).getSetPointTemp() / 100.0;
        logger.debug("Setpoint temp is : {} C", temp);
        return temp;
    }

    private int readSetpointMode() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.gatewayHandler.newSeq(), deviceId,
                new SinopeSetPointModeData());
        logger.debug("Reading Set Point mode for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);
        int mode = ((SinopeSetPointModeData) answ.getAppData()).getSetPointMode();
        logger.debug("Setpoint mode is : {}", mode);
        return mode;
    }

    private int readHeatLevel() throws UnknownHostException, IOException {
        SinopeDataReadRequest req = new SinopeDataReadRequest(this.gatewayHandler.newSeq(), deviceId,
                new SinopeHeatLevelData());
        logger.debug("Reading Heat Level for device id: {}", ByteUtil.toString(deviceId));
        SinopeDataAnswer answ = (SinopeDataAnswer) this.gatewayHandler.execute(req);
        int level = ((SinopeHeatLevelData) answ.getAppData()).getHeatLevel();
        logger.debug("Heating level is  : {}", level);
        return level;
    }

    private void updateDeviceId() {
        String sDeviceId = (String) getConfig().get(SinopeBindingConstants.CONFIG_PROPERTY_DEVICE_ID);
        this.deviceId = SinopeConfig.convert(sDeviceId);
        if (this.deviceId == null) {
            logger.debug("Invalid Device id, cannot convert id: {}", sDeviceId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid Device id");
            return;
        }
        Bridge bridge = this.getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        if (this.gatewayHandler == null && ThingStatus.ONLINE.equals(bridge.getStatus())) {
            updateSinopeGatewayHandler(bridge);
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private synchronized void updateSinopeGatewayHandler(Bridge bridge) {
        ThingHandler handler = bridge.getHandler();
        if (handler instanceof SinopeGatewayHandler) {
            this.gatewayHandler = (SinopeGatewayHandler) handler;
            this.gatewayHandler.registerThermostatHandler(this);
        }
    }
}
