/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.handler;

import static org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.digitalstrom.internal.DigitalSTROMBindingConstants;
import org.openhab.binding.digitalstrom.internal.lib.GeneralLibConstance;
import org.openhab.binding.digitalstrom.internal.lib.config.Config;
import org.openhab.binding.digitalstrom.internal.lib.listener.DeviceStatusListener;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Device;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.GeneralDeviceInformation;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceSceneSpec;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ApplicationGroup;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.ChangeableDeviceConfigEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.DeviceBinarayInputEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputModeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.SensorEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DeviceBinaryInput;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DeviceStateUpdateImpl;
import org.openhab.binding.digitalstrom.internal.providers.DsChannelTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DeviceHandler} is responsible for handling the configuration, load supported channels of a
 * digitalSTROM device and handling commands, which are sent to one of the channels. <br>
 * <br>
 * For that it uses the {@link BridgeHandler} and the {@link DeviceStateUpdate} mechanism of the {@link Device} to
 * execute the actual command and implements the {@link DeviceStatusListener} to get informed about changes from the
 * accompanying {@link Device}.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class DeviceHandler extends BaseThingHandler implements DeviceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(DeviceHandler.class);

    /**
     * Contains all supported thing types of this handler, will be filled by DsDeviceThingTypeProvider.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>();

    public static final String TWO_STAGE_SWITCH_IDENTICATOR = "2";
    public static final String THREE_STAGE_SWITCH_IDENTICATOR = "3";

    private String dSID;
    private Device device;
    private BridgeHandler dssBridgeHandler;

    private Command lastComand;
    private String currentChannel;
    private List<String> loadedSensorChannels;

    /**
     * Creates a new {@link DeviceHandler}.
     *
     * @param thing must not be null
     */
    public DeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing DeviceHandler.");
        dSID = (String) getConfig().get(DigitalSTROMBindingConstants.DEVICE_DSID);
        if (dSID != null && !dSID.isBlank()) {
            final Bridge bridge = getBridge();
            if (bridge != null) {
                bridgeStatusChanged(bridge.getStatusInfo());
            } else {
                // Set status to OFFLINE if no bridge is available e.g. because the bridge has been removed and the
                // Thing was reinitialized.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Bridge is missing!");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "dSID is missing");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed... unregister DeviceStatusListener");
        if (dSID != null) {
            if (dssBridgeHandler != null) {
                dssBridgeHandler.unregisterDeviceStatusListener(this);
            }
        }
        if (device != null) {
            device.setSensorDataRefreshPriority(Config.REFRESH_PRIORITY_NEVER, Config.REFRESH_PRIORITY_NEVER,
                    Config.REFRESH_PRIORITY_NEVER);
        }
        device = null;
    }

    @Override
    public void handleRemoval() {
        if (getDssBridgeHandler() != null) {
            this.dssBridgeHandler.childThingRemoved(dSID);
        }
        updateStatus(ThingStatus.REMOVED);
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
        if (device == null) {
            initialize();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            if (dSID != null) {
                if (getDssBridgeHandler() != null) {
                    if (device == null) {
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                                "waiting for listener registration");
                        dssBridgeHandler.registerDeviceStatusListener(this);
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No dSID is set!");
            }
        }
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        logger.debug("Set status to {}", getThing().getStatusInfo());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        BridgeHandler dssBridgeHandler = getDssBridgeHandler();
        if (dssBridgeHandler == null) {
            logger.debug("BridgeHandler not found. Cannot handle command without bridge.");
            return;
        }

        if (device == null) {
            logger.debug(
                    "Device not known on StructureManager or DeviceStatusListener is not registerd. Cannot handle command.");
            return;
        }

        if (command instanceof RefreshType) {
            try {
                SensorEnum sensorType = SensorEnum.valueOf(channelUID.getId());
                dssBridgeHandler.sendComandsToDSS(device, new DeviceStateUpdateImpl(sensorType, 1));
            } catch (IllegalArgumentException e) {
                dssBridgeHandler.sendComandsToDSS(device,
                        new DeviceStateUpdateImpl(DeviceStateUpdate.REFRESH_OUTPUT, 0));
            }
        } else if (!device.isShade()) {
            if (DsChannelTypeProvider.isOutputChannel(channelUID.getId())) {
                if (command instanceof PercentType) {
                    device.setOutputValue(
                            (short) fromPercentToValue(((PercentType) command).intValue(), device.getMaxOutputValue()));
                } else if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        device.setIsOn(true);
                    } else {
                        device.setIsOn(false);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        device.increase();
                    } else {
                        device.decrease();
                    }
                } else if (command instanceof StringType) {
                    device.setOutputValue(Short.parseShort(((StringType) command).toString()));
                }
            } else {
                logger.debug("Command sent to an unknown channel id: {}", channelUID);
            }
        } else {
            if (channelUID.getId().contains(DsChannelTypeProvider.ANGLE)) {
                if (command instanceof PercentType) {
                    device.setAnglePosition(
                            (short) fromPercentToValue(((PercentType) command).intValue(), device.getMaxSlatAngle()));
                } else if (command instanceof OnOffType) {
                    if (OnOffType.ON.equals(command)) {
                        device.setAnglePosition(device.getMaxSlatAngle());
                    } else {
                        device.setAnglePosition(device.getMinSlatAngle());
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (IncreaseDecreaseType.INCREASE.equals(command)) {
                        device.increaseSlatAngle();
                    } else {
                        device.decreaseSlatAngle();
                    }
                }
            } else if (channelUID.getId().contains(DsChannelTypeProvider.SHADE)) {
                if (command instanceof PercentType) {
                    int percent = ((PercentType) command).intValue();
                    if (!device.getHWinfo().equals("GR-KL200")) {
                        percent = 100 - percent;
                    }
                    device.setSlatPosition(fromPercentToValue(percent, device.getMaxSlatPosition()));
                    this.lastComand = command;
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.MOVE.equals(command)) {
                        handleCommand(channelUID, this.lastComand);
                    } else {
                        dssBridgeHandler.stopOutputValue(device);
                    }
                } else if (command instanceof UpDownType) {
                    if (UpDownType.UP.equals(command)) {
                        device.setIsOpen(true);
                        this.lastComand = command;
                    } else {
                        device.setIsOpen(false);
                        this.lastComand = command;
                    }
                }
            } else {
                logger.debug("Command sent to an unknown channel id: {}", channelUID);
            }
        }
    }

    private int fromPercentToValue(int percent, int max) {
        if (percent < 0 || percent == 0) {
            return 0;
        }
        if (max < 0 || max == 0) {
            return 0;
        }
        return (int) (max * ((float) percent / 100));
    }

    private synchronized BridgeHandler getDssBridgeHandler() {
        if (this.dssBridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Bride cannot be found");
                return null;
            }
            ThingHandler handler = bridge.getHandler();

            if (handler instanceof BridgeHandler) {
                dssBridgeHandler = (BridgeHandler) handler;
            } else {
                return null;
            }
        }
        return dssBridgeHandler;
    }

    private boolean sensorChannelsLoaded() {
        return loadedSensorChannels != null && !loadedSensorChannels.isEmpty();
    }

    @Override
    public synchronized void onDeviceStateChanged(DeviceStateUpdate deviceStateUpdate) {
        if (device != null) {
            if (deviceStateUpdate != null) {
                if (sensorChannelsLoaded()) {
                    if (deviceStateUpdate.isSensorUpdateType()) {
                        updateState(getSensorChannelID(deviceStateUpdate.getTypeAsSensorEnum()),
                                new DecimalType(deviceStateUpdate.getValueAsFloat()));
                        logger.debug("Update state");
                        return;
                    }
                    if (deviceStateUpdate.isBinarayInputType()) {
                        if (deviceStateUpdate.getValueAsShort() == 1) {
                            updateState(getBinaryInputChannelID(deviceStateUpdate.getTypeAsDeviceBinarayInputEnum()),
                                    OnOffType.ON);
                        } else {
                            updateState(getBinaryInputChannelID(deviceStateUpdate.getTypeAsDeviceBinarayInputEnum()),
                                    OnOffType.OFF);
                        }
                    }
                }
                if (!device.isShade()) {
                    if (currentChannel != null) {
                        switch (deviceStateUpdate.getType()) {
                            case DeviceStateUpdate.OUTPUT_DECREASE:
                            case DeviceStateUpdate.OUTPUT_INCREASE:
                            case DeviceStateUpdate.OUTPUT:
                                if (currentChannel.contains(DsChannelTypeProvider.DIMMER)) {
                                    if (deviceStateUpdate.getValueAsInteger() > 0) {
                                        updateState(currentChannel, new PercentType(fromValueToPercent(
                                                deviceStateUpdate.getValueAsInteger(), device.getMaxOutputValue())));
                                    } else {
                                        updateState(currentChannel, OnOffType.OFF);
                                    }
                                } else if (currentChannel.contains(DsChannelTypeProvider.STAGE)) {
                                    if (currentChannel.contains(TWO_STAGE_SWITCH_IDENTICATOR)) {
                                        updateState(currentChannel,
                                                new StringType(convertStageValue((short) 2, device.getOutputValue())));
                                    } else {
                                        updateState(currentChannel,
                                                new StringType(convertStageValue((short) 3, device.getOutputValue())));
                                    }
                                }
                                break;
                            case DeviceStateUpdate.ON_OFF:
                                if (currentChannel.contains(DsChannelTypeProvider.STAGE)) {
                                    onDeviceStateChanged(new DeviceStateUpdateImpl(DeviceStateUpdate.OUTPUT,
                                            device.getOutputValue()));
                                }
                                if (deviceStateUpdate.getValueAsInteger() > 0) {
                                    updateState(currentChannel, OnOffType.ON);
                                } else {
                                    updateState(currentChannel, OnOffType.OFF);
                                }
                                break;
                            default:
                                return;
                        }
                    }
                } else {
                    int percent = 0;
                    switch (deviceStateUpdate.getType()) {
                        case DeviceStateUpdate.SLAT_DECREASE:
                        case DeviceStateUpdate.SLAT_INCREASE:
                        case DeviceStateUpdate.SLATPOSITION:
                            percent = fromValueToPercent(deviceStateUpdate.getValueAsInteger(),
                                    device.getMaxSlatPosition());
                            break;
                        case DeviceStateUpdate.OPEN_CLOSE:
                            if (deviceStateUpdate.getValueAsInteger() > 0) {
                                percent = 100;
                            }
                            break;
                        case DeviceStateUpdate.OPEN_CLOSE_ANGLE:
                            if (device.isBlind() && currentChannel != null) {
                                if (deviceStateUpdate.getValueAsInteger() > 0) {
                                    updateState(currentChannel, PercentType.HUNDRED);
                                } else {
                                    updateState(currentChannel, PercentType.ZERO);
                                }
                            }
                            return;
                        case DeviceStateUpdate.SLAT_ANGLE_DECREASE:
                        case DeviceStateUpdate.SLAT_ANGLE_INCREASE:
                        case DeviceStateUpdate.SLAT_ANGLE:
                            if (device.isBlind() && currentChannel != null) {
                                updateState(currentChannel,
                                        new PercentType(fromValueToPercent(deviceStateUpdate.getValueAsInteger(),
                                                device.getMaxSlatAngle())));
                            }
                            return;
                        default:
                            return;
                    }
                    if (!device.getHWinfo().equals("GR-KL210")) {
                        percent = 100 - percent;
                    }
                    updateState(DsChannelTypeProvider.SHADE, new PercentType(percent));
                }
                logger.debug("Update state");
            }
        }
    }

    private int fromValueToPercent(int value, int max) {
        if (value <= 0 || max <= 0) {
            return 0;
        }
        int percentValue = new BigDecimal(value * ((float) 100 / max)).setScale(0, RoundingMode.HALF_UP).intValue();
        return percentValue < 0 ? 0 : percentValue > 100 ? 100 : percentValue;
    }

    @Override
    public synchronized void onDeviceRemoved(GeneralDeviceInformation device) {
        if (device instanceof Device) {
            this.device = (Device) device;
            if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (!((Device) device).isPresent()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Device is not present in the digitalSTROM-System.");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "Device is not avaible in the digitalSTROM-System.");
                }

            }
            logger.debug("Set status to {}", getThing().getStatus());
        }
    }

    @Override
    public synchronized void onDeviceAdded(GeneralDeviceInformation device) {
        if (device instanceof Device) {
            this.device = (Device) device;
            if (this.device.isPresent()) {
                ThingStatusInfo statusInfo = this.dssBridgeHandler.getThing().getStatusInfo();
                updateStatus(statusInfo.getStatus(), statusInfo.getStatusDetail(), statusInfo.getDescription());
                logger.debug("Set status to {}", getThing().getStatus());

                // load scene configurations persistently into the thing
                for (Short i : this.device.getSavedScenes()) {
                    onSceneConfigAdded(i);
                }
                logger.debug("Load saved scene specification into device");
                this.device.saveConfigSceneSpecificationIntoDevice(getThing().getProperties());

                checkDeviceInfoProperties(this.device);
                // load sensor priorities into the device and load sensor channels of the thing
                if (!this.device.isShade()) {
                    loadSensorChannels();
                    // check and load output channel of the thing
                    checkOutputChannel();
                } else if (this.device.isBlind()) {
                    // load channel for set the angle of jalousie devices
                    ApplicationGroup.Color color = ((Device) device).getFunctionalColorGroup() != null
                            ? ((Device) device).getFunctionalColorGroup().getColor()
                            : null;
                    String channelTypeID = DsChannelTypeProvider.getOutputChannelTypeID(color,
                            ((Device) device).getOutputMode(), ((Device) device).getOutputChannels());
                    loadOutputChannel(new ChannelTypeUID(BINDING_ID, channelTypeID),
                            DsChannelTypeProvider.getItemType(channelTypeID));
                }

                // load first channel values
                onDeviceStateInitial(this.device);
                return;
            }
        }
        onDeviceRemoved(device);
    }

    /**
     * Updates device info properties.
     *
     * @param device (must not be null)
     */
    private void checkDeviceInfoProperties(Device device) {
        boolean propertiesChanged = false;
        Map<String, String> properties = editProperties();
        // check device info
        if (device.getName() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_NAME, device.getName());
            propertiesChanged = true;
        }
        if (device.getDSUID() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_UID, device.getDSUID());
            propertiesChanged = true;
        }
        if (device.getHWinfo() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_HW_INFO, device.getHWinfo());
            propertiesChanged = true;
        }
        if (device.getZoneId() != -1) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_ZONE_ID, device.getZoneId() + "");
            propertiesChanged = true;
        }
        if (device.getGroups() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_GROUPS, device.getGroups().toString());
            propertiesChanged = true;
        }
        if (device.getOutputMode() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
            propertiesChanged = true;
        }
        if (device.getFunctionalColorGroup() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_FUNCTIONAL_COLOR_GROUP,
                    device.getFunctionalColorGroup().toString());
            propertiesChanged = true;
        }
        if (device.getMeterDSID() != null) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_METER_ID, device.getMeterDSID().toString());
            propertiesChanged = true;
        }
        if (!device.getBinaryInputs().isEmpty()) {
            properties.put(DigitalSTROMBindingConstants.DEVICE_BINARAY_INPUTS, getBinarayInputList());
            propertiesChanged = true;
        }
        if (propertiesChanged) {
            super.updateProperties(properties);
            propertiesChanged = false;
        }
    }

    private String getBinarayInputList() {
        List<String> binarayInputs = new ArrayList<>(device.getBinaryInputs().size());
        for (DeviceBinaryInput binInput : device.getBinaryInputs()) {
            DeviceBinarayInputEnum devBinInp = DeviceBinarayInputEnum.getdeviceBinarayInput(binInput.getInputType());
            if (devBinInp != null) {
                binarayInputs.add(devBinInp.toString().toLowerCase());
            }
        }
        return binarayInputs.toString();
    }

    private void loadSensorChannels() {
        if (device != null && device.isPresent()) {
            // load sensor priorities into the device
            boolean configChanged = false;
            Configuration config = getThing().getConfiguration();
            logger.debug("Add sensor priorities to the device");

            String activePowerPrio = Config.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY) != null) {
                activePowerPrio = config.get(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY, Config.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }
            // By devices with output mode WIPE the active power always will be read out to check, if the device is not
            // in standby any more.
            if (OutputModeEnum.WIPE.equals(device.getOutputMode())
                    && activePowerPrio.equals(Config.REFRESH_PRIORITY_NEVER)) {
                config.put(DigitalSTROMBindingConstants.ACTIVE_POWER_REFRESH_PRIORITY, Config.REFRESH_PRIORITY_LOW);
                configChanged = true;
            }

            String outputCurrentPrio = Config.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY) != null) {
                outputCurrentPrio = config.get(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.OUTPUT_CURRENT_REFRESH_PRIORITY, Config.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }

            String electricMeterPrio = Config.REFRESH_PRIORITY_NEVER;
            if (config.get(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY) != null) {
                electricMeterPrio = config.get(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY).toString();
            } else {
                config.put(DigitalSTROMBindingConstants.ELECTRIC_METER_REFRESH_PRIORITY, Config.REFRESH_PRIORITY_NEVER);
                configChanged = true;
            }

            if (configChanged) {
                super.updateConfiguration(config);
                configChanged = false;
            }

            device.setSensorDataRefreshPriority(activePowerPrio, electricMeterPrio, outputCurrentPrio);
            logger.debug(
                    "add sensor prioritys: active power = {}, output current = {}, electric meter = {} to device with id {}",
                    activePowerPrio, outputCurrentPrio, electricMeterPrio, device.getDSID());

            // check and load sensor channels of the thing
            checkSensorChannel();
        }
    }

    private boolean addLoadedSensorChannel(String sensorChannelType) {
        if (loadedSensorChannels == null) {
            loadedSensorChannels = new LinkedList<>();
        }
        if (!loadedSensorChannels.contains(sensorChannelType.toString())) {
            return loadedSensorChannels.add(sensorChannelType.toString());
        }
        return false;
    }

    private boolean removeLoadedSensorChannel(String sensorChannelType) {
        if (loadedSensorChannels == null) {
            return false;
        }
        return loadedSensorChannels.remove(sensorChannelType);
    }

    private boolean isSensorChannelLoaded(String sensorChannelType) {
        if (loadedSensorChannels == null) {
            return false;
        }
        return loadedSensorChannels.contains(sensorChannelType);
    }

    private void checkSensorChannel() {
        List<Channel> channelList = new LinkedList<>(this.getThing().getChannels());

        boolean channelListChanged = false;

        // if sensor channels with priority never are loaded delete these channels
        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel channel = channelInter.next();
                String channelID = channel.getUID().getId();
                if (channelID.startsWith(DsChannelTypeProvider.BINARY_INPUT_PRE)) {
                    DeviceBinarayInputEnum devBinInput = getBinaryInput(channelID);
                    if (device.getBinaryInput(devBinInput) != null) {
                        addLoadedSensorChannel(channelID);
                    } else {
                        logger.debug("remove {} binary input channel", channelID);
                        channelInter.remove();
                        channelListChanged = removeLoadedSensorChannel(channelID);
                    }
                } else {
                    SensorEnum sensorType = getSensorEnum(channelID);
                    if (sensorType != null) {
                        if (SensorEnum.isPowerSensor(sensorType)) {
                            if (device.checkPowerSensorRefreshPriorityNever(sensorType)) {
                                logger.debug("remove {} sensor channel", channelID);
                                channelInter.remove();
                                channelListChanged = removeLoadedSensorChannel(channelID);
                            } else {
                                addLoadedSensorChannel(channelID);
                            }
                        } else {
                            if (device.supportsSensorType(sensorType)) {
                                addLoadedSensorChannel(channelID);
                            } else {
                                logger.debug("remove {} sensor channel", channelID);
                                channelInter.remove();
                                removeLoadedSensorChannel(channelID);
                                channelListChanged = true;
                            }
                        }
                    }
                }
            }
        }
        for (SensorEnum sensorType : device.getPowerSensorTypes()) {
            if (!device.checkPowerSensorRefreshPriorityNever(sensorType)
                    && !isSensorChannelLoaded(getSensorChannelID(sensorType))) {
                logger.debug("create {} sensor channel", sensorType.toString());
                channelList.add(getSensorChannel(sensorType));
                channelListChanged = addLoadedSensorChannel(getSensorChannelID(sensorType));
            }
        }
        if (device.hasClimateSensors()) {
            for (SensorEnum sensorType : device.getClimateSensorTypes()) {
                if (!isSensorChannelLoaded(getSensorChannelID(sensorType))) {
                    logger.debug("create {} sensor channel", sensorType.toString());
                    channelList.add(getSensorChannel(sensorType));
                    channelListChanged = addLoadedSensorChannel(getSensorChannelID(sensorType));
                }
            }
        }
        if (device.isBinaryInputDevice()) {
            for (DeviceBinaryInput binInput : device.getBinaryInputs()) {
                DeviceBinarayInputEnum binInputType = DeviceBinarayInputEnum
                        .getdeviceBinarayInput(binInput.getInputType());
                if (binInputType != null && !isSensorChannelLoaded(getBinaryInputChannelID(binInputType))) {
                    logger.debug("create {} sensor channel", binInputType.toString());
                    channelList.add(getBinaryChannel(binInputType));
                    channelListChanged = addLoadedSensorChannel(getBinaryInputChannelID(binInputType));
                }
            }
        }

        if (channelListChanged) {
            logger.debug("load new channel list");
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
        }
    }

    private Channel getSensorChannel(SensorEnum sensorType) {
        return ChannelBuilder.create(getSensorChannelUID(sensorType), "Number")
                .withType(DsChannelTypeProvider.getSensorChannelUID(sensorType)).build();
    }

    private Channel getBinaryChannel(DeviceBinarayInputEnum binaryInputType) {
        return ChannelBuilder.create(getBinaryInputChannelUID(binaryInputType), "Switch")
                .withType(DsChannelTypeProvider.getBinaryInputChannelUID(binaryInputType)).build();
    }

    private void checkOutputChannel() {
        if (device == null) {
            logger.debug("Can not load a channel without a device!");
            return;
        }
        // if the device have no output channel or it is disabled all output channels will be deleted
        if (!device.isDeviceWithOutput()) {
            loadOutputChannel(null, null);
        }
        ApplicationGroup.Color color = device.getFunctionalColorGroup() != null
                ? device.getFunctionalColorGroup().getColor()
                : null;
        String channelTypeID = DsChannelTypeProvider.getOutputChannelTypeID(color, device.getOutputMode(),
                device.getOutputChannels());
        logger.debug("load channel: typeID={}, itemType={}",
                DsChannelTypeProvider.getOutputChannelTypeID(device.getFunctionalColorGroup().getColor(),
                        device.getOutputMode(), device.getOutputChannels()),
                DsChannelTypeProvider.getItemType(channelTypeID));
        if (channelTypeID != null && (currentChannel == null || !currentChannel.equals(channelTypeID))) {
            loadOutputChannel(new ChannelTypeUID(BINDING_ID, channelTypeID),
                    DsChannelTypeProvider.getItemType(channelTypeID));
        }
    }

    private void loadOutputChannel(ChannelTypeUID channelTypeUID, String acceptedItemType) {
        if (channelTypeUID == null || acceptedItemType == null) {
            return;
        }
        currentChannel = channelTypeUID.getId();

        List<Channel> channelList = new LinkedList<>(this.getThing().getChannels());
        boolean channelIsAlreadyLoaded = false;
        boolean channelListChanged = false;

        if (!channelList.isEmpty()) {
            Iterator<Channel> channelInter = channelList.iterator();
            while (channelInter.hasNext()) {
                Channel channel = channelInter.next();
                if (DsChannelTypeProvider.isOutputChannel(channel.getUID().getId())) {
                    if (!channel.getUID().getId().equals(currentChannel)
                            && !(device.isShade() && channel.getUID().getId().equals(DsChannelTypeProvider.SHADE))) {
                        channelInter.remove();
                        channelListChanged = true;
                    } else {
                        if (!channel.getUID().getId().equals(DsChannelTypeProvider.SHADE)) {
                            channelIsAlreadyLoaded = true;
                        }
                    }
                }
            }
        }

        if (!channelIsAlreadyLoaded && currentChannel != null) {
            Channel channel = ChannelBuilder
                    .create(new ChannelUID(this.getThing().getUID(), channelTypeUID.getId()), acceptedItemType)
                    .withType(channelTypeUID).build();
            channelList.add(channel);
            channelListChanged = true;
        }

        if (channelListChanged) {
            ThingBuilder thingBuilder = editThing();
            thingBuilder.withChannels(channelList);
            updateThing(thingBuilder.build());
            logger.debug("load channel: {} with item: {}", channelTypeUID.getAsString(), acceptedItemType);
        }
    }

    private ChannelUID getSensorChannelUID(SensorEnum sensorType) {
        return new ChannelUID(getThing().getUID(), getSensorChannelID(sensorType));
    }

    private String getSensorChannelID(SensorEnum sensorType) {
        return sensorType.toString().toLowerCase();
    }

    private ChannelUID getBinaryInputChannelUID(DeviceBinarayInputEnum binaryInputType) {
        return new ChannelUID(getThing().getUID(), getBinaryInputChannelID(binaryInputType));
    }

    private String getBinaryInputChannelID(DeviceBinarayInputEnum binaryInputType) {
        return DsChannelTypeProvider.BINARY_INPUT_PRE + binaryInputType.toString().toLowerCase();
    }

    private DeviceBinarayInputEnum getBinaryInput(String channelID) {
        try {
            return DeviceBinarayInputEnum
                    .valueOf(channelID.replace(DsChannelTypeProvider.BINARY_INPUT_PRE, "").toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SensorEnum getSensorEnum(String channelID) {
        try {
            return SensorEnum.valueOf(channelID.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (device != null) {
            SensorEnum sensorType = getSensorEnum(channelUID.getId());
            if (sensorType != null) {
                Float val = device.getFloatSensorValue(sensorType);
                if (val != null) {
                    updateState(channelUID, new DecimalType(val));
                }
            }
            Short val = device.getBinaryInputState(getBinaryInput(channelUID.getId()));
            if (val != null) {
                if (val == 1) {
                    updateState(channelUID, OnOffType.ON);
                } else {
                    updateState(channelUID, OnOffType.OFF);
                }
            }

            if (channelUID.getId().contains(DsChannelTypeProvider.DIMMER)) {
                if (device.isOn()) {
                    updateState(channelUID,
                            new PercentType(fromValueToPercent(device.getOutputValue(), device.getMaxOutputValue())));
                } else {
                    updateState(channelUID, new PercentType(0));
                }
                return;
            }
            if (channelUID.getId().contains(DsChannelTypeProvider.SWITCH)) {
                if (device.isOn()) {
                    updateState(channelUID, OnOffType.ON);
                } else {
                    updateState(channelUID, OnOffType.OFF);
                }
                return;
            }
            if (channelUID.getId().contains(DsChannelTypeProvider.SHADE)) {
                updateState(channelUID,
                        new PercentType(fromValueToPercent(device.getSlatPosition(), device.getMaxSlatPosition())));
                return;
            }
            if (channelUID.getId().contains(DsChannelTypeProvider.ANGLE)) {
                updateState(channelUID,
                        new PercentType(fromValueToPercent(device.getAnglePosition(), device.getMaxSlatAngle())));
                return;
            }
            if (channelUID.getId().contains(DsChannelTypeProvider.STAGE)) {
                if (channelUID.getId().contains(TWO_STAGE_SWITCH_IDENTICATOR)) {
                    updateState(channelUID, new StringType(convertStageValue((short) 2, device.getOutputValue())));
                    return;
                }
                if (channelUID.getId().contains(THREE_STAGE_SWITCH_IDENTICATOR)) {
                    updateState(channelUID, new StringType(convertStageValue((short) 3, device.getOutputValue())));
                    return;
                }
            }
        }
    }

    private String convertStageValue(short stage, short value) {
        switch (stage) {
            case 2:
                if (value < 85) {
                    return OPTION_COMBINED_BOTH_OFF;
                } else if (value >= 85 && value < 170) {
                    return OPTION_COMBINED_FIRST_ON;
                } else if (value >= 170 && value <= 255) {
                    return OPTION_COMBINED_BOTH_ON;
                }
            case 3:
                if (value < 64) {
                    return OPTION_COMBINED_BOTH_OFF;
                } else if (value >= 64 && value < 128) {
                    return OPTION_COMBINED_FIRST_ON;
                } else if (value >= 128 && value < 192) {
                    return OPTION_COMBINED_SECOND_ON;
                } else if (value >= 192 && value <= 255) {
                    return OPTION_COMBINED_BOTH_ON;
                }
        }
        return null;
    }

    private void onDeviceStateInitial(Device device) {
        if (device != null) {
            if (currentChannel != null) {
                if (isLinked(currentChannel)) {
                    channelLinked(new ChannelUID(getThing().getUID(), currentChannel));
                }
            }
            if (!device.isShade()) {
                if (loadedSensorChannels != null) {
                    for (String sensor : loadedSensorChannels) {
                        Channel channel = getThing().getChannel(sensor);
                        if (channel != null && isLinked(sensor)) {
                            channelLinked(channel.getUID());
                        }
                    }
                }
            } else {
                if (isLinked(DsChannelTypeProvider.SHADE)) {
                    channelLinked(new ChannelUID(getThing().getUID(), DsChannelTypeProvider.SHADE));
                }
            }
        }
    }

    @Override
    public synchronized void onSceneConfigAdded(short sceneId) {
        if (device != null) {
            String saveScene = "";
            DeviceSceneSpec sceneSpec = device.getSceneConfig(sceneId);
            if (sceneSpec != null) {
                saveScene = sceneSpec.toString();
            }

            Integer[] sceneValue = device.getSceneOutputValue(sceneId);
            if (sceneValue[GeneralLibConstance.SCENE_ARRAY_INDEX_VALUE] != -1) {
                saveScene = saveScene + ", sceneValue: " + sceneValue[0];
            }
            if (sceneValue[GeneralLibConstance.SCENE_ARRAY_INDEX_ANGLE] != -1) {
                saveScene = saveScene + ", sceneAngle: " + sceneValue[1];
            }
            String key = DigitalSTROMBindingConstants.DEVICE_SCENE + sceneId;
            if (!saveScene.isEmpty()) {
                logger.debug("Save scene configuration: [{}] to thing with UID {}", saveScene, getThing().getUID());
                super.updateProperty(key, saveScene);
                // persist the new property
                // super.updateThing(editThing().build());
            }
        }
    }

    @Override
    public void onDeviceConfigChanged(ChangeableDeviceConfigEnum whichConfig) {
        if (whichConfig != null) {
            switch (whichConfig) {
                case DEVICE_NAME:
                    super.updateProperty(DEVICE_NAME, device.getName());
                    break;
                case METER_DSID:
                    super.updateProperty(DEVICE_METER_ID, device.getMeterDSID().getValue());
                    break;
                case ZONE_ID:
                    super.updateProperty(DEVICE_ZONE_ID, device.getZoneId() + "");
                    break;
                case GROUPS:
                    super.updateProperty(DEVICE_GROUPS, device.getGroups().toString());
                    break;
                case FUNCTIONAL_GROUP:
                    super.updateProperty(DEVICE_FUNCTIONAL_COLOR_GROUP, device.getFunctionalColorGroup().toString());
                    checkOutputChannel();
                    break;
                case OUTPUT_MODE:
                    super.updateProperty(DEVICE_OUTPUT_MODE, device.getOutputMode().toString());
                    checkOutputChannel();
                    break;
                case BINARY_INPUTS:
                    super.updateProperty(DEVICE_BINARAY_INPUTS, getBinarayInputList());
                    checkSensorChannel();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public String getDeviceStatusListenerID() {
        return this.dSID;
    }
}
