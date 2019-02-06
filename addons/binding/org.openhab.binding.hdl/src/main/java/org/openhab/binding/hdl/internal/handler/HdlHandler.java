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
package org.openhab.binding.hdl.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.hdl.HdlBindingConstants;
import org.openhab.binding.hdl.internal.device.CommandType;
import org.openhab.binding.hdl.internal.device.Device;
import org.openhab.binding.hdl.internal.device.MDT0601;
import org.openhab.binding.hdl.internal.device.ML01;
import org.openhab.binding.hdl.internal.device.MPL848FH;
import org.openhab.binding.hdl.internal.device.MR1216;
import org.openhab.binding.hdl.internal.device.MRDA06;
import org.openhab.binding.hdl.internal.device.MS08Mn2C;
import org.openhab.binding.hdl.internal.device.MS122C;
import org.openhab.binding.hdl.internal.device.MS24;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * Inspired by MAX! binding,
 *
 * @author stigla - Initial contribution
 */
public class HdlHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(HdlHandler.class);
    private HdlBridgeHandler bridgeHandler;

    private String hdldeviceSerial;
    private int subNet;
    private int deviceID;
    private int refreshRate;
    private ScheduledFuture<?> refreshJob;

    public HdlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            Configuration config = getThing().getConfiguration();
            subNet = ((BigDecimal) config.get(HdlBindingConstants.PROPERTY_SUBNET)).intValueExact();
            deviceID = ((BigDecimal) config.get(HdlBindingConstants.PROPERTY_DEVICEID)).intValueExact();

            try {
                refreshRate = ((BigDecimal) config.get(HdlBindingConstants.PROPERTY_REFRESHRATE)).intValueExact();
            } catch (Exception e) {
                refreshRate = 0;
            }

            hdldeviceSerial = Integer.toString(subNet * 1000 + deviceID);

            if (hdldeviceSerial != null) {
                logger.debug("Initialized Hdl! device handler for {}.", hdldeviceSerial);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Initialized HDL device missing Subnet or DeviceID configuration");
            }

            HdlBridgeHandler hdlBridge = getHdlBridgeHandler();

            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                sendUpdatePackets(hdlBridge);
                if (refreshRate != 0) {
                    if (refreshJob == null || refreshJob.isCancelled()) {
                        refreshJob = scheduler.scheduleWithFixedDelay(refreshRunnable, 1, refreshRate,
                                TimeUnit.SECONDS);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during initialize : {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        logger.debug("Disposing Hdl! device {} {}.", getThing().getUID(), hdldeviceSerial);

        if (bridgeHandler != null) {
            logger.trace("Clear HDL! device {} {} from bridge.", getThing().getUID(), hdldeviceSerial);
            bridgeHandler.clearDeviceList();
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
        }

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }

        logger.debug("Disposed HDL! device {} {}.", getThing().getUID(), hdldeviceSerial);
        super.dispose();
    }

    private Runnable refreshRunnable = () -> {
        try {
            HdlPacket p = new HdlPacket();
            p.setTargetSubnetID(subNet);
            p.setTargetDeviceId(deviceID);

            switch (getThing().getThingTypeUID().getAsString()) {
                case "hdl:MS08Mn_2C":
                case "hdl:MS12_2C":
                    p.setCommandType(CommandType.Read_Sensors_Status);
                    logger.debug("For Thing Type: {} with device id: {} with Refresh Interval: {} command is sent.",
                            getThing().getThingTypeUID().getAsString(), deviceID, refreshRate);
                    break;
                case "hdl:MPL8_48_FH":
                    p.setCommandType(CommandType.Read_Floor_Heating_Status_DLP);
                    logger.debug("For Thing Type: {} with device id: {} with Refresh Interval: {} command is sent.",
                            getThing().getThingTypeUID().getAsString(), deviceID, refreshRate);
                    break;
                /*
                 * case "hdl:MRDA06":
                 * case "hdl:MDT0601_233":
                 * case "hdl:MR1216_233":
                 * p.setCommandType(CommandType.Read_Status_of_Channels);
                 * logger.debug("For Thing Type: {} command: Refresh is sent.",
                 * getThing().getThingTypeUID().getAsString());
                 * break;
                 */
                default:
                    logger.debug("For Thing Type: {} command: Refresh interval is not supported.",
                            getThing().getThingTypeUID().getAsString());
                    refreshJob.cancel(true);
                    return;
            }

            try {
                bridgeHandler.sendPacket(p);
            } catch (IOException e) {
                logger.error("Could not send msg to bridge, got error msg: {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.debug("An exception occurred while refreshing the hdl item: '{}'", e.getMessage());
            updateStatus(ThingStatus.OFFLINE);
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandler#thingUpdated
     * (org.eclipse.smarthome.core.thing.Thing)
     */
    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
    }

    /*
     *
     * @param configurationParameter
     */

    private synchronized HdlBridgeHandler getHdlBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Required bridge not defined for device {}.", hdldeviceSerial);
                updateStatus(ThingStatus.OFFLINE);
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof HdlBridgeHandler) {
                this.bridgeHandler = (HdlBridgeHandler) handler;
                this.bridgeHandler.registerDeviceStatusListener(this);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.debug("No available bridge handler found for {} bridge {} .", hdldeviceSerial, bridge.getUID());
                updateStatus(ThingStatus.OFFLINE);
                return null;
            }
        }
        return this.bridgeHandler;
    }

    private void sendUpdatePackets(HdlBridgeHandler hdlBridge) {
        Collection<HdlPacket> hdlPacketList = new ArrayList<HdlPacket>();

        HdlPacket p = new HdlPacket();
        p.setTargetSubnetID(subNet);
        p.setTargetDeviceId(deviceID);

        switch (getThing().getThingTypeUID().getAsString()) {
            case "hdl:MS08Mn_2C":
            case "hdl:MS12_2C":
                p.setCommandType(CommandType.Read_Sensors_Status);
                hdlPacketList.add(p);
                logger.debug("For Thing Type: {} command: Refresh is sent.",
                        getThing().getThingTypeUID().getAsString());
                break;
            case "hdl:MRDA06":
            case "hdl:MDT0601_233":
            case "hdl:MR1216_233":
                p.setCommandType(CommandType.Read_Status_of_Channels);
                hdlPacketList.add(p);
                logger.debug("For Thing Type: {} command: Refresh is sent.",
                        getThing().getThingTypeUID().getAsString());
                break;
            case "hdl:MPL8_48_FH":
                p.setCommandType(CommandType.Read_Floor_Heating_Status_DLP);
                hdlPacketList.add(p);
                logger.debug("For Thing Type: {} command: Refresh is sent.",
                        getThing().getThingTypeUID().getAsString());
                break;
            case "hdl:MW02_231":
                p.setCommandType(CommandType.Read_Status_of_Curtain_Switch);
                p.setData(new byte[] { (byte) 1 });
                hdlPacketList.add(p);

                HdlPacket p2 = new HdlPacket();
                p2.setTargetSubnetID(subNet);
                p2.setTargetDeviceId(deviceID);
                p2.setCommandType(CommandType.Read_Status_of_Curtain_Switch);
                p2.setData(new byte[] { (byte) 2 });
                hdlPacketList.add(p2);

                logger.debug("For Thing Type: {} command: Refresh is sent.",
                        getThing().getThingTypeUID().getAsString());
                break;
            default:
                logger.debug("For Thing Type: {} command: Refresh not supported.",
                        getThing().getThingTypeUID().getAsString());
                return;
        }

        try {
            for (Iterator<HdlPacket> i = hdlPacketList.iterator(); i.hasNext();) {
                HdlPacket item = i.next();
                hdlBridge.sendPacket(item);
            }
        } catch (IOException e) {
            logger.error("Could not send msg to bridge, got error msg: {}", e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        HdlBridgeHandler hdlBridge = getHdlBridgeHandler();
        boolean sendCommand = false;
        if (hdlBridge == null) {
            logger.warn("HDL bridge handler not found. Cannot handle command without bridge.");
            return;
        }
        if (hdldeviceSerial == null) {
            logger.warn("Serial number missing. Can't send command to device '{}'", getThing());
            return;
        }

        HdlPacket p = new HdlPacket();

        Device chDevice = hdlBridge.getDevice(hdldeviceSerial);

        if (command instanceof RefreshType) {
            sendUpdatePackets(hdlBridge);
        } else {
            switch (channelUID.getId()) {
                case HdlBindingConstants.CHANNEL_FHMODE:
                    sendCommand = true;
                    int modenr;
                    // What Command to send:
                    switch (command.toString().toLowerCase()) {
                        case "normal":
                            modenr = 1;
                            break;
                        case "day":
                            modenr = 2;
                            break;
                        case "night":
                            modenr = 3;
                            break;
                        case "away":
                            modenr = 4;
                            break;
                        case "timer":
                            modenr = 5;
                            break;
                        default:
                            modenr = 0;
                    }

                    // Get status from existing values
                    int tempTypenr;
                    int statusNr;

                    if (((MPL848FH) chDevice).getFloorHeatingTemperaturType() != null) {
                        String tempType = ((MPL848FH) chDevice).getFloorHeatingTemperaturType();

                        if (tempType == "C") {
                            tempTypenr = 0;
                        } else {
                            tempTypenr = 1;
                        }
                    } else {
                        tempTypenr = 0;
                        sendCommand = false;
                    }

                    if (((MPL848FH) chDevice).getFloorHeatingStatus() != null) {
                        OnOffType fhStatus = ((MPL848FH) chDevice).getFloorHeatingStatus();

                        if (fhStatus == OnOffType.OFF) {
                            statusNr = 0;
                        } else {
                            statusNr = 1;
                        }

                    } else {
                        sendCommand = false;
                        statusNr = 0;
                    }

                    int setNormalTemp;
                    if (((MPL848FH) chDevice).getFloorHeatingSetNormalTemperatur() != null) {
                        setNormalTemp = ((MPL848FH) chDevice).getFloorHeatingSetNormalTemperatur().intValue();
                    } else {
                        sendCommand = false;
                        setNormalTemp = 0;
                    }

                    int setDayTemp;
                    if (((MPL848FH) chDevice).getFloorHeatingSetDayTemperatur() != null) {
                        setDayTemp = ((MPL848FH) chDevice).getFloorHeatingSetDayTemperatur().intValue();
                    } else {
                        sendCommand = false;
                        setDayTemp = 0;
                    }

                    int setNightTemp;
                    if (((MPL848FH) chDevice).getFloorHeatingSetNightTemperatur() != null) {
                        setNightTemp = ((MPL848FH) chDevice).getFloorHeatingSetNightTemperatur().intValue();
                    } else {
                        sendCommand = false;
                        setNightTemp = 0;
                    }

                    int setAwayTemp;
                    if (((MPL848FH) chDevice).getFloorHeatingSetAwayTemperatur() != null) {
                        setAwayTemp = ((MPL848FH) chDevice).getFloorHeatingSetAwayTemperatur().intValue();
                    } else {
                        sendCommand = false;
                        setAwayTemp = 0;
                    }

                    p.setData(new byte[] { (byte) tempTypenr, (byte) statusNr, (byte) modenr, (byte) setNormalTemp,
                            (byte) setDayTemp, (byte) setNightTemp, (byte) setAwayTemp });

                    p.setCommandType(CommandType.Control_Floor_Heating_Status_DLP);

                    break;
                case HdlBindingConstants.CHANNEL_SHUTTER1CONTROL:
                case HdlBindingConstants.CHANNEL_SHUTTER2CONTROL:
                    String stringCommand = command.toString();
                    p.setCommandType(CommandType.Curtain_Switch_Control);
                    int curtainNr = HdlBindingConstants.CurtainNr.valueOf(channelUID.getId()).getValue();
                    switch (stringCommand) {
                        case "Off":
                        case "OFF":
                        case "DOWN":
                            p.setData(new byte[] { (byte) curtainNr, (byte) 1 });
                            sendCommand = true;
                            break;
                        case "ON":
                        case "UP":
                            p.setData(new byte[] { (byte) curtainNr, (byte) 2 });
                            sendCommand = true;
                            break;
                        case "STOP":
                            p.setData(new byte[] { (byte) curtainNr, (byte) 0 });
                            sendCommand = true;
                            break;
                    }

                    break;
                case HdlBindingConstants.CHANNEL_DIMCHANNEL1:
                case HdlBindingConstants.CHANNEL_DIMCHANNEL2:
                case HdlBindingConstants.CHANNEL_DIMCHANNEL3:
                case HdlBindingConstants.CHANNEL_DIMCHANNEL4:
                case HdlBindingConstants.CHANNEL_DIMCHANNEL5:
                case HdlBindingConstants.CHANNEL_DIMCHANNEL6:
                    if (command instanceof PercentType) {
                        PercentType dimValue = (PercentType) command;
                        p.setCommandType(CommandType.Single_Channel_Control);
                        int channelNr = HdlBindingConstants.DimChannelNr.valueOf(channelUID.getId()).getValue();
                        p.setData(new byte[] { (byte) channelNr, (byte) dimValue.intValue(), 0, 0 });
                        sendCommand = true;
                    }
                    break;
                case HdlBindingConstants.CHANNEL_RELAYCH1:
                case HdlBindingConstants.CHANNEL_RELAYCH2:
                case HdlBindingConstants.CHANNEL_RELAYCH3:
                case HdlBindingConstants.CHANNEL_RELAYCH4:
                case HdlBindingConstants.CHANNEL_RELAYCH5:
                case HdlBindingConstants.CHANNEL_RELAYCH6:
                case HdlBindingConstants.CHANNEL_RELAYCH7:
                case HdlBindingConstants.CHANNEL_RELAYCH8:
                case HdlBindingConstants.CHANNEL_RELAYCH9:
                case HdlBindingConstants.CHANNEL_RELAYCH10:
                case HdlBindingConstants.CHANNEL_RELAYCH11:
                case HdlBindingConstants.CHANNEL_RELAYCH12:
                    if (command instanceof OnOffType) {
                        p.setCommandType(CommandType.Single_Channel_Control);
                        int relayValue = 0;
                        if (command.equals(OnOffType.ON)) {
                            relayValue = 100;
                        }
                        int channelNr = HdlBindingConstants.RelayChannelNr.valueOf(channelUID.getId()).getValue();
                        p.setData(new byte[] { (byte) channelNr, (byte) relayValue, 0, 0 });
                        sendCommand = true;
                    }
                    break;
                case HdlBindingConstants.CHANNEL_UVSWITCH1:
                case HdlBindingConstants.CHANNEL_UVSWITCH2:
                case HdlBindingConstants.CHANNEL_UVSWITCH3:
                case HdlBindingConstants.CHANNEL_UVSWITCH4:
                case HdlBindingConstants.CHANNEL_UVSWITCH5:
                case HdlBindingConstants.CHANNEL_UVSWITCH6:
                case HdlBindingConstants.CHANNEL_UVSWITCH200:
                case HdlBindingConstants.CHANNEL_UVSWITCH201:
                case HdlBindingConstants.CHANNEL_UVSWITCH202:
                case HdlBindingConstants.CHANNEL_UVSWITCH203:
                case HdlBindingConstants.CHANNEL_UVSWITCH204:
                case HdlBindingConstants.CHANNEL_UVSWITCH205:
                case HdlBindingConstants.CHANNEL_UVSWITCH206:
                case HdlBindingConstants.CHANNEL_UVSWITCH207:
                case HdlBindingConstants.CHANNEL_UVSWITCH208:
                case HdlBindingConstants.CHANNEL_UVSWITCH209:
                case HdlBindingConstants.CHANNEL_UVSWITCH210:
                case HdlBindingConstants.CHANNEL_UVSWITCH211:
                case HdlBindingConstants.CHANNEL_UVSWITCH212:
                case HdlBindingConstants.CHANNEL_UVSWITCH213:
                case HdlBindingConstants.CHANNEL_UVSWITCH214:
                case HdlBindingConstants.CHANNEL_UVSWITCH215:
                case HdlBindingConstants.CHANNEL_UVSWITCH216:
                case HdlBindingConstants.CHANNEL_UVSWITCH217:
                case HdlBindingConstants.CHANNEL_UVSWITCH218:
                case HdlBindingConstants.CHANNEL_UVSWITCH219:
                case HdlBindingConstants.CHANNEL_UVSWITCH220:
                case HdlBindingConstants.CHANNEL_UVSWITCH221:
                case HdlBindingConstants.CHANNEL_UVSWITCH222:
                case HdlBindingConstants.CHANNEL_UVSWITCH223:
                case HdlBindingConstants.CHANNEL_UVSWITCH224:
                case HdlBindingConstants.CHANNEL_UVSWITCH225:
                case HdlBindingConstants.CHANNEL_UVSWITCH226:
                case HdlBindingConstants.CHANNEL_UVSWITCH227:
                case HdlBindingConstants.CHANNEL_UVSWITCH228:
                case HdlBindingConstants.CHANNEL_UVSWITCH229:
                case HdlBindingConstants.CHANNEL_UVSWITCH230:
                case HdlBindingConstants.CHANNEL_UVSWITCH231:
                case HdlBindingConstants.CHANNEL_UVSWITCH232:
                case HdlBindingConstants.CHANNEL_UVSWITCH233:
                case HdlBindingConstants.CHANNEL_UVSWITCH234:
                case HdlBindingConstants.CHANNEL_UVSWITCH235:
                case HdlBindingConstants.CHANNEL_UVSWITCH236:
                case HdlBindingConstants.CHANNEL_UVSWITCH237:
                case HdlBindingConstants.CHANNEL_UVSWITCH238:
                case HdlBindingConstants.CHANNEL_UVSWITCH239:
                case HdlBindingConstants.CHANNEL_UVSWITCH240:
                    if (command instanceof OnOffType) {
                        p.setCommandType(CommandType.UV_Switch_Control);
                        int uvswitchValue = 0;
                        if (command.equals(OnOffType.ON)) {
                            uvswitchValue = 255;
                        }
                        int uvswitchNr = HdlBindingConstants.UVSwitchNr.valueOf(channelUID.getId()).getValue();
                        p.setData(new byte[] { (byte) uvswitchNr, (byte) uvswitchValue });
                        sendCommand = true;
                    }
                    break;
                default:
                    logger.error("For Channel: {} Command: {} Not supported.", channelUID, command);
                    return;
            }
        }

        if (sendCommand) {
            p.setTargetSubnetID(subNet);
            p.setTargetDeviceId(deviceID);

            try {
                hdlBridge.sendPacket(p);
            } catch (IOException e) {
                logger.error("Could not send msg to bridge, got error msg: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, Device device) {
        if (device.getSerialNr().equals(hdldeviceSerial)) {
            if (device.isUpdated()) {
                logger.debug("Updating states of {} {} id: {}", device.getType(), device.getSerialNr(),
                        getThing().getUID());
                switch (device.getType()) {
                    case MS08Mn_2C:
                        if (((MS08Mn2C) device).getTemperatureValue() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_TEMPERATUR),
                                    ((MS08Mn2C) device).getTemperatureValue());
                        }
                        if (((MS08Mn2C) device).getBrightnessValue() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_BRIGHTNESS),
                                    ((MS08Mn2C) device).getBrightnessValue());
                        }
                        if (((MS08Mn2C) device).getMotionSensorValue() != null) {
                            StopMoveType fromDevice = ((MS08Mn2C) device).getMotionSensorValue();
                            OnOffType sendToUpdate = OnOffType.OFF;
                            if (fromDevice.equals(StopMoveType.MOVE)) {
                                sendToUpdate = OnOffType.ON;
                            }
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_MOTIONSSENSOR),
                                    sendToUpdate);
                        }
                        if (((MS08Mn2C) device).getDryContact1Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT1),
                                    ((MS08Mn2C) device).getDryContact1Value());
                        }
                        if (((MS08Mn2C) device).getDryContact2Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT2),
                                    ((MS08Mn2C) device).getDryContact2Value());
                        }
                        break;
                    case MS12_2C:
                        if (((MS122C) device).getTemperatureValue() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_TEMPERATUR),
                                    ((MS122C) device).getTemperatureValue());
                        }
                        if (((MS122C) device).getBrightnessValue() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_BRIGHTNESS),
                                    ((MS122C) device).getBrightnessValue());
                        }
                        if (((MS122C) device).getMotionSensorValue() != null) {
                            StopMoveType fromDevice = ((MS122C) device).getMotionSensorValue();
                            OnOffType sendToUpdate = OnOffType.OFF;
                            if (fromDevice.equals(StopMoveType.MOVE)) {
                                sendToUpdate = OnOffType.ON;
                            }
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_MOTIONSSENSOR),
                                    sendToUpdate);
                        }
                        if (((MS122C) device).getSonicValue() != null) {
                            StopMoveType fromDevice = ((MS122C) device).getSonicValue();
                            OnOffType sendToUpdate = OnOffType.OFF;
                            if (fromDevice.equals(StopMoveType.MOVE)) {
                                sendToUpdate = OnOffType.ON;
                            }
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_SONIC),
                                    sendToUpdate);
                        }
                        if (((MS122C) device).getDryContact1Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT1),
                                    ((MS122C) device).getDryContact1Value());
                        }
                        if (((MS122C) device).getDryContact2Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT2),
                                    ((MS122C) device).getDryContact2Value());
                        }
                        if (((MS122C) device).getRelayCh01State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH1),
                                    ((MS122C) device).getRelayCh01State());
                        }
                        if (((MS122C) device).getRelayCh02State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH2),
                                    ((MS122C) device).getRelayCh02State());
                        }
                        break;
                    case MPL8_48_FH:
                        if (((MPL848FH) device).getTemperatureValue() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_TEMPERATUR),
                                    ((MPL848FH) device).getTemperatureValue());
                        }
                        if (((MPL848FH) device).getFloorHeatingSetNormalTemperatur() != null) {
                            updateState(
                                    new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_FHNORMALTEMPSET),
                                    ((MPL848FH) device).getFloorHeatingSetNormalTemperatur());
                        }
                        if (((MPL848FH) device).getFloorHeatingSetAwayTemperatur() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_FHAWAYTEMPSET),
                                    ((MPL848FH) device).getFloorHeatingSetAwayTemperatur());
                        }
                        if (((MPL848FH) device).getFloorHeatingSetDayTemperatur() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_FHDAYTEMPSET),
                                    ((MPL848FH) device).getFloorHeatingSetDayTemperatur());
                        }
                        if (((MPL848FH) device).getFloorHeatingSetNightTemperatur() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_FHNIGHTTEMPSET),
                                    ((MPL848FH) device).getFloorHeatingSetNightTemperatur());
                        }
                        if (((MPL848FH) device).getFloorHeatingCurrentTemperatur() != null) {
                            updateState(
                                    new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_FHCURRENTTEMPSET),
                                    ((MPL848FH) device).getFloorHeatingCurrentTemperatur());
                        }
                        if (((MPL848FH) device).getFloorHeatingMode() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_FHMODE),
                                    new StringType(((MPL848FH) device).getFloorHeatingMode().toString()));
                        }
                        if (((MPL848FH) device).getACAutoTemperatur() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACAUTOTEMPSET),
                                    ((MPL848FH) device).getACAutoTemperatur());
                        }
                        if (((MPL848FH) device).getACCoolingTemperatur() != null) {
                            updateState(
                                    new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACCOOLINGTEMPSET),
                                    ((MPL848FH) device).getACCoolingTemperatur());
                        }
                        if (((MPL848FH) device).getACCurrentTemperatur() != null) {
                            updateState(
                                    new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACCURRENTTEMPSET),
                                    ((MPL848FH) device).getACCurrentTemperatur());
                        }
                        if (((MPL848FH) device).getACDryTemperatur() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACDRYTEMPSET),
                                    ((MPL848FH) device).getACDryTemperatur());
                        }
                        if (((MPL848FH) device).getACHeatTemperatur() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACHEATTEMPSET),
                                    ((MPL848FH) device).getACHeatTemperatur());
                        }
                        if (((MPL848FH) device).getACFanSpeed() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACFANSPEED),
                                    new StringType(((MPL848FH) device).getACFanSpeed()));
                        }
                        if (((MPL848FH) device).getACMode() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_ACMODE),
                                    new StringType(((MPL848FH) device).getACMode()));
                        }
                        if (((MPL848FH) device).getUVSwitch1() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH1),
                                    ((MPL848FH) device).getUVSwitch1());
                        }
                        if (((MPL848FH) device).getUVSwitch2() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH2),
                                    ((MPL848FH) device).getUVSwitch2());
                        }
                        if (((MPL848FH) device).getUVSwitch3() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH3),
                                    ((MPL848FH) device).getUVSwitch3());
                        }
                        if (((MPL848FH) device).getUVSwitch4() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH4),
                                    ((MPL848FH) device).getUVSwitch4());
                        }
                        if (((MPL848FH) device).getUVSwitch5() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH5),
                                    ((MPL848FH) device).getUVSwitch5());
                        }
                        if (((MPL848FH) device).getUVSwitch6() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH6),
                                    ((MPL848FH) device).getUVSwitch6());
                        }
                        break;
                    case MDT0601_233:
                        if (((MDT0601) device).getDimChannel1State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL1),
                                    ((MDT0601) device).getDimChannel1State());
                        }
                        if (((MDT0601) device).getDimChannel2State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL2),
                                    ((MDT0601) device).getDimChannel2State());
                        }
                        if (((MDT0601) device).getDimChannel3State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL3),
                                    ((MDT0601) device).getDimChannel3State());
                        }
                        if (((MDT0601) device).getDimChannel4State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL4),
                                    ((MDT0601) device).getDimChannel4State());
                        }
                        if (((MDT0601) device).getDimChannel5State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL5),
                                    ((MDT0601) device).getDimChannel5State());
                        }
                        if (((MDT0601) device).getDimChannel6State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL6),
                                    ((MDT0601) device).getDimChannel6State());
                        }
                        break;
                    case MRDA06:
                        if (((MRDA06) device).getDimChannel1State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL1),
                                    ((MRDA06) device).getDimChannel1State());
                        }
                        if (((MRDA06) device).getDimChannel2State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL2),
                                    ((MRDA06) device).getDimChannel2State());
                        }
                        if (((MRDA06) device).getDimChannel3State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL3),
                                    ((MRDA06) device).getDimChannel3State());
                        }
                        if (((MRDA06) device).getDimChannel4State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL4),
                                    ((MRDA06) device).getDimChannel4State());
                        }
                        if (((MRDA06) device).getDimChannel5State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL5),
                                    ((MRDA06) device).getDimChannel5State());
                        }
                        if (((MRDA06) device).getDimChannel6State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DIMCHANNEL6),
                                    ((MRDA06) device).getDimChannel6State());
                        }
                        break;
                    case MR1216_233:
                        if (((MR1216) device).getRelayCh01State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH1),
                                    ((MR1216) device).getRelayCh01State());
                        }
                        if (((MR1216) device).getRelayCh02State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH2),
                                    ((MR1216) device).getRelayCh02State());
                        }
                        if (((MR1216) device).getRelayCh03State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH3),
                                    ((MR1216) device).getRelayCh03State());
                        }
                        if (((MR1216) device).getRelayCh04State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH4),
                                    ((MR1216) device).getRelayCh04State());
                        }
                        if (((MR1216) device).getRelayCh05State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH5),
                                    ((MR1216) device).getRelayCh05State());
                        }
                        if (((MR1216) device).getRelayCh06State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH6),
                                    ((MR1216) device).getRelayCh06State());
                        }
                        if (((MR1216) device).getRelayCh07State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH7),
                                    ((MR1216) device).getRelayCh07State());
                        }
                        if (((MR1216) device).getRelayCh08State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH8),
                                    ((MR1216) device).getRelayCh08State());
                        }
                        if (((MR1216) device).getRelayCh09State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH9),
                                    ((MR1216) device).getRelayCh09State());
                        }
                        if (((MR1216) device).getRelayCh10State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH10),
                                    ((MR1216) device).getRelayCh10State());
                        }
                        if (((MR1216) device).getRelayCh11State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH11),
                                    ((MR1216) device).getRelayCh11State());
                        }
                        if (((MR1216) device).getRelayCh12State() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_RELAYCH12),
                                    ((MR1216) device).getRelayCh12State());
                        }
                        break;
                    case ML01:
                        if (((ML01) device).getUVSwitch200() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH200),
                                    ((ML01) device).getUVSwitch200());
                        }
                        if (((ML01) device).getUVSwitch201() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH201),
                                    ((ML01) device).getUVSwitch201());
                        }
                        if (((ML01) device).getUVSwitch202() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH202),
                                    ((ML01) device).getUVSwitch202());
                        }
                        if (((ML01) device).getUVSwitch203() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH203),
                                    ((ML01) device).getUVSwitch203());
                        }
                        if (((ML01) device).getUVSwitch204() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH204),
                                    ((ML01) device).getUVSwitch204());
                        }
                        if (((ML01) device).getUVSwitch205() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH205),
                                    ((ML01) device).getUVSwitch205());
                        }
                        if (((ML01) device).getUVSwitch206() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH206),
                                    ((ML01) device).getUVSwitch206());
                        }
                        if (((ML01) device).getUVSwitch207() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH207),
                                    ((ML01) device).getUVSwitch207());
                        }
                        if (((ML01) device).getUVSwitch208() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH208),
                                    ((ML01) device).getUVSwitch208());
                        }
                        if (((ML01) device).getUVSwitch209() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH209),
                                    ((ML01) device).getUVSwitch209());
                        }
                        if (((ML01) device).getUVSwitch210() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH210),
                                    ((ML01) device).getUVSwitch210());
                        }
                        if (((ML01) device).getUVSwitch211() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH211),
                                    ((ML01) device).getUVSwitch211());
                        }
                        if (((ML01) device).getUVSwitch212() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH212),
                                    ((ML01) device).getUVSwitch212());
                        }
                        if (((ML01) device).getUVSwitch213() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH213),
                                    ((ML01) device).getUVSwitch213());
                        }
                        if (((ML01) device).getUVSwitch214() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH214),
                                    ((ML01) device).getUVSwitch214());
                        }
                        if (((ML01) device).getUVSwitch215() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH215),
                                    ((ML01) device).getUVSwitch215());
                        }
                        if (((ML01) device).getUVSwitch216() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH216),
                                    ((ML01) device).getUVSwitch216());
                        }
                        if (((ML01) device).getUVSwitch217() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH217),
                                    ((ML01) device).getUVSwitch217());
                        }
                        if (((ML01) device).getUVSwitch218() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH218),
                                    ((ML01) device).getUVSwitch218());
                        }
                        if (((ML01) device).getUVSwitch219() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH219),
                                    ((ML01) device).getUVSwitch219());
                        }
                        if (((ML01) device).getUVSwitch220() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH220),
                                    ((ML01) device).getUVSwitch220());
                        }
                        if (((ML01) device).getUVSwitch221() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH221),
                                    ((ML01) device).getUVSwitch221());
                        }
                        if (((ML01) device).getUVSwitch222() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH222),
                                    ((ML01) device).getUVSwitch222());
                        }
                        if (((ML01) device).getUVSwitch223() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH223),
                                    ((ML01) device).getUVSwitch223());
                        }
                        if (((ML01) device).getUVSwitch224() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH224),
                                    ((ML01) device).getUVSwitch224());
                        }
                        if (((ML01) device).getUVSwitch225() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH225),
                                    ((ML01) device).getUVSwitch225());
                        }
                        if (((ML01) device).getUVSwitch226() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH226),
                                    ((ML01) device).getUVSwitch226());
                        }
                        if (((ML01) device).getUVSwitch227() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH227),
                                    ((ML01) device).getUVSwitch227());
                        }
                        if (((ML01) device).getUVSwitch228() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH228),
                                    ((ML01) device).getUVSwitch228());
                        }
                        if (((ML01) device).getUVSwitch229() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH229),
                                    ((ML01) device).getUVSwitch229());
                        }
                        if (((ML01) device).getUVSwitch230() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH230),
                                    ((ML01) device).getUVSwitch230());
                        }
                        if (((ML01) device).getUVSwitch231() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH231),
                                    ((ML01) device).getUVSwitch231());
                        }
                        if (((ML01) device).getUVSwitch232() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH232),
                                    ((ML01) device).getUVSwitch232());
                        }
                        if (((ML01) device).getUVSwitch233() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH233),
                                    ((ML01) device).getUVSwitch233());
                        }
                        if (((ML01) device).getUVSwitch234() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH234),
                                    ((ML01) device).getUVSwitch234());
                        }
                        if (((ML01) device).getUVSwitch235() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH235),
                                    ((ML01) device).getUVSwitch235());
                        }
                        if (((ML01) device).getUVSwitch236() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH236),
                                    ((ML01) device).getUVSwitch236());
                        }
                        if (((ML01) device).getUVSwitch237() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH237),
                                    ((ML01) device).getUVSwitch237());
                        }
                        if (((ML01) device).getUVSwitch238() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH238),
                                    ((ML01) device).getUVSwitch238());
                        }
                        if (((ML01) device).getUVSwitch239() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH239),
                                    ((ML01) device).getUVSwitch239());
                        }
                        if (((ML01) device).getUVSwitch240() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_UVSWITCH240),
                                    ((ML01) device).getUVSwitch240());
                        }
                        break;
                    case MS24:
                        if (((MS24) device).getDryContact1Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT1),
                                    ((MS24) device).getDryContact1Value());
                        }
                        if (((MS24) device).getDryContact2Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT2),
                                    ((MS24) device).getDryContact2Value());
                        }
                        if (((MS24) device).getDryContact3Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT3),
                                    ((MS24) device).getDryContact3Value());
                        }
                        if (((MS24) device).getDryContact4Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT4),
                                    ((MS24) device).getDryContact4Value());
                        }
                        if (((MS24) device).getDryContact5Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT5),
                                    ((MS24) device).getDryContact5Value());
                        }
                        if (((MS24) device).getDryContact6Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT6),
                                    ((MS24) device).getDryContact6Value());
                        }
                        if (((MS24) device).getDryContact7Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT7),
                                    ((MS24) device).getDryContact7Value());
                        }
                        if (((MS24) device).getDryContact8Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT8),
                                    ((MS24) device).getDryContact8Value());
                        }
                        if (((MS24) device).getDryContact9Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT9),
                                    ((MS24) device).getDryContact9Value());
                        }
                        if (((MS24) device).getDryContact10Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT10),
                                    ((MS24) device).getDryContact10Value());
                        }
                        if (((MS24) device).getDryContact11Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT11),
                                    ((MS24) device).getDryContact11Value());
                        }
                        if (((MS24) device).getDryContact12Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT12),
                                    ((MS24) device).getDryContact12Value());
                        }
                        if (((MS24) device).getDryContact13Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT13),
                                    ((MS24) device).getDryContact13Value());
                        }
                        if (((MS24) device).getDryContact14Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT14),
                                    ((MS24) device).getDryContact14Value());
                        }
                        if (((MS24) device).getDryContact15Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT15),
                                    ((MS24) device).getDryContact15Value());
                        }
                        if (((MS24) device).getDryContact16Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT16),
                                    ((MS24) device).getDryContact16Value());
                        }
                        if (((MS24) device).getDryContact17Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT17),
                                    ((MS24) device).getDryContact17Value());
                        }
                        if (((MS24) device).getDryContact18Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT18),
                                    ((MS24) device).getDryContact18Value());
                        }
                        if (((MS24) device).getDryContact19Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT19),
                                    ((MS24) device).getDryContact19Value());
                        }
                        if (((MS24) device).getDryContact20Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT20),
                                    ((MS24) device).getDryContact20Value());
                        }
                        if (((MS24) device).getDryContact21Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT21),
                                    ((MS24) device).getDryContact21Value());
                        }
                        if (((MS24) device).getDryContact22Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT22),
                                    ((MS24) device).getDryContact22Value());
                        }
                        if (((MS24) device).getDryContact23Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT23),
                                    ((MS24) device).getDryContact23Value());
                        }
                        if (((MS24) device).getDryContact24Value() != null) {
                            updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_DRYCONTACT24),
                                    ((MS24) device).getDryContact24Value());
                        }
                        break;
                    // case MW02_231:
                    // if (((MW02) device).getStopMoveShutter1Status() != null) {
                    // updateState(new ChannelUID(getThing().getUID(), HdlBindingConstants.CHANNEL_SHUTTER1CONTROL),
                    // ((MW02) device).getStopMoveShutter1Status());
                    // }
                    // break;
                    default:
                        logger.debug("Device Type: {} unhandled", device.getType());
                        break;
                }
                device.setUpdated(false);
            } else {
                logger.debug("No changes for {} {} id: {}", device.getType(), device.getSerialNr(),
                        getThing().getUID());
            }
        }
    }

    @Override
    public void onDeviceRemoved(HdlBridgeHandler bridge, Device device) {
        if (device.getSerialNr().equals(hdldeviceSerial)) {
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
            // forceRefresh = true;
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        // forceRefresh = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.BaseThingHandler#bridgeStatusChanged(org.eclipse.smarthome.core.thing.
     * ThingStatusInfo)
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus().toString(),
                getThing().getUID().toString());
        if (!bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Set the Configurable properties for this device
     *
     * @param device
     */
    private void setDeviceConfiguration(Device device) {
        try {
            logger.debug("HDL! {} {} configuration update", device.getType().toString(), device.getSerialNr());
            Configuration configuration = editConfiguration();

            // Add additional device config entries
            for (Map.Entry<String, Object> entry : device.getProperties().entrySet()) {
                configuration.put(entry.getKey(), entry.getValue());
            }
            updateConfiguration(configuration);
            logger.debug("Config updated: {}", configuration.getProperties());
            // configSet = true;
        } catch (Exception e) {
            logger.debug("Exception occurred during configuration edit: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onDeviceConfigUpdate(Bridge bridge, Device device) {
        if (device.getSerialNr().equals(hdldeviceSerial)) {
            setDeviceConfiguration(device);
        }
    }
}
