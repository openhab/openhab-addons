/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.handler;

import static org.openhab.binding.max.MaxBinding.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.internal.command.C_Command;
import org.openhab.binding.max.internal.command.Q_Command;
import org.openhab.binding.max.internal.command.S_ConfigCommand;
import org.openhab.binding.max.internal.command.S_ConfigCommand.ConfigCommandType;
import org.openhab.binding.max.internal.command.Z_Command;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.EcoSwitch;
import org.openhab.binding.max.internal.device.HeatingThermostat;
import org.openhab.binding.max.internal.device.ShutterContact;
import org.openhab.binding.max.internal.device.ThermostatModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDevicesHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MaxDevicesHandler extends BaseThingHandler implements DeviceStatusListener {

    private Logger logger = LoggerFactory.getLogger(MaxDevicesHandler.class);
    private MaxCubeBridgeHandler bridgeHandler;

    private String maxDeviceSerial;
    private String rfAddress;
    private boolean forceRefresh = true;
    private boolean propertiesSet = false;
    private boolean configSet = false;

    // actual refresh variables
    public static final int REFRESH_ACTUAL_MIN_RATE = 10; // minutes
    public static final int REFRESH_ACTUAL_DURATION = 120; // seconds
    private static final long COMMUNICATION_DELAY_TIME = 120;
    private int refreshActualRate = 0;
    private boolean refreshingActuals = false;
    private ScheduledFuture<?> refreshActualsJob;
    private DecimalType originalSetTemp;
    private ThermostatModeType originalMode;
    private Runnable refreshActualsRestoreRunnable = new Runnable() {
        @Override
        public void run() {
            refreshActualsRestore();
        }
    };

    public MaxDevicesHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            Configuration config = getThing().getConfiguration();
            final String configDeviceId = (String) config.get(MaxBinding.PROPERTY_SERIAL_NUMBER);

            try {
                refreshActualRate = ((BigDecimal) config.get(MaxBinding.PROPERTY_REFRESH_ACTUAL_RATE)).intValueExact();
            } catch (Exception e) {
                refreshActualRate = 0;
            }

            if (configDeviceId != null) {
                maxDeviceSerial = configDeviceId;
            }
            if (maxDeviceSerial != null) {
                logger.debug("Initialized MAX! device handler for {}.", maxDeviceSerial);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Initialized MAX! device missing serialNumber configuration");
            }
            propertiesSet = false;
            configSet = false;
            forceRefresh = true;
            getMaxCubeBridgeHandler();
        } catch (Exception e) {
            logger.debug("Exception occurred during initialize : {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing MAX! device {} {}.", getThing().getUID(), maxDeviceSerial);
        if (refreshingActuals) {
            refreshActualsRestore();
        }
        if (refreshActualsJob != null && !refreshActualsJob.isCancelled()) {
            refreshActualsJob.cancel(true);
            refreshActualsJob = null;
        }
        if (bridgeHandler != null) {
            logger.trace("Clear MAX! device {} {} from bridge.", getThing().getUID(), maxDeviceSerial);
            bridgeHandler.clearDeviceList();
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
        }
        logger.debug("Disposed MAX! device {} {}.", getThing().getUID(), maxDeviceSerial);
        super.dispose();
    }

    @Override
    public void thingUpdated(Thing thing) {
        configSet = false;
        forceRefresh = true;
        super.thingUpdated(thing);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        logger.debug("MAX! Device {}: Configuration update received", getThing().getUID().toString());
        boolean temperaturePropertyUpdateNeeded = false;
        HashMap<String, Object> deviceProperties = new HashMap<String, Object>();
        try {
            Device device = getMaxCubeBridgeHandler().getDevice(maxDeviceSerial);
            deviceProperties = new HashMap<String, Object>(device.getProperties());
        } catch (Error e) {
            // ignore
        }
        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            logger.debug("MAX! Device {}: Configuration update {} to {}", getThing().getUID().toString(),
                    configurationParameter.getKey(), configurationParameter.getValue());

            // Test if it is a part of the configuration properties.
            // As paperUI sends all parameters as changed, we need to determine which ones really changed.
            if (deviceProperties.containsKey(configurationParameter.getKey())) {
                if (deviceProperties.get(configurationParameter.getKey()).equals(configurationParameter.getValue())) {
                    logger.trace("Device {} Property {} value {} unchanged.", getThing().getUID().toString(),
                            configurationParameter.getKey(), configurationParameter.getValue());
                } else if (configurationParameter.getValue().getClass() == BigDecimal.class
                        && ((BigDecimal) deviceProperties.get(configurationParameter.getKey()))
                                .compareTo((BigDecimal) configurationParameter.getValue()) == 0) {
                    logger.trace("Device {} Property {} value {} unchanged.", getThing().getUID().toString(),
                            configurationParameter.getKey(), configurationParameter.getValue());
                } else {
                    logger.debug("Device {} Property {} value {} -> {} changed.", getThing().getUID().toString(),
                            configurationParameter.getKey(), deviceProperties.get(configurationParameter.getKey()),
                            configurationParameter.getValue());
                    temperaturePropertyUpdateNeeded = true;
                }
            }
            if (configurationParameter.getKey().equals(PROPERTY_DEVICENAME)
                    || configurationParameter.getKey().equals(PROPERTY_ROOMID)) {
                updateDeviceName(configurationParameter);
            }
            if (configurationParameter.getKey().startsWith("action-")) {
                if (configurationParameter.getValue().toString().equals(BUTTON_ACTION_VALUE)) {
                    configurationParameter.setValue(BigDecimal.valueOf(BUTTON_NOACTION_VALUE));
                    if (configurationParameter.getKey().equals(ACTION_DEVICE_DELETE)) {
                        deviceDelete();
                    }
                }
            }
            configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
        }
        // Persist changes and restart with new parameters
        updateConfiguration(configuration);
        if (temperaturePropertyUpdateNeeded) {
            sendPropertyUpdate(configurationParameters, deviceProperties);
        }
    }

    private void sendPropertyUpdate(Map<String, Object> configurationParameters,
            HashMap<String, Object> deviceProperties) {
        try {
            Device device = getMaxCubeBridgeHandler().getDevice(maxDeviceSerial);
            rfAddress = device.getRFAddress();
            int roomId = device.getRoomId();
            BigDecimal tempComfort = (BigDecimal) configurationParameters.getOrDefault(PROPERTY_THERMO_COMFORT_TEMP,
                    deviceProperties.get(PROPERTY_THERMO_COMFORT_TEMP));
            BigDecimal tempEco = (BigDecimal) configurationParameters.getOrDefault(PROPERTY_THERMO_ECO_TEMP,
                    deviceProperties.get(PROPERTY_THERMO_ECO_TEMP));
            BigDecimal tempSetpointMax = (BigDecimal) configurationParameters.getOrDefault(
                    PROPERTY_THERMO_MAX_TEMP_SETPOINT, deviceProperties.get(PROPERTY_THERMO_MAX_TEMP_SETPOINT));
            BigDecimal tempSetpointMin = (BigDecimal) configurationParameters.getOrDefault(
                    PROPERTY_THERMO_MIN_TEMP_SETPOINT, deviceProperties.get(PROPERTY_THERMO_MIN_TEMP_SETPOINT));
            BigDecimal tempOffset = (BigDecimal) configurationParameters.getOrDefault(PROPERTY_THERMO_OFFSET_TEMP,
                    deviceProperties.get(PROPERTY_THERMO_OFFSET_TEMP));
            BigDecimal tempOpenWindow = (BigDecimal) configurationParameters.getOrDefault(
                    PROPERTY_THERMO_WINDOW_OPEN_TEMP, deviceProperties.get(PROPERTY_THERMO_WINDOW_OPEN_TEMP));
            BigDecimal durationOpenWindow = (BigDecimal) configurationParameters.getOrDefault(
                    PROPERTY_THERMO_WINDOW_OPEN_DURATION, deviceProperties.get(PROPERTY_THERMO_WINDOW_OPEN_DURATION));
            S_ConfigCommand cmd = new S_ConfigCommand(rfAddress, roomId, tempComfort.doubleValue(),
                    tempEco.doubleValue(), tempSetpointMax.doubleValue(), tempSetpointMin.doubleValue(),
                    tempOffset.doubleValue(), tempOpenWindow.doubleValue(), durationOpenWindow.intValue());
            bridgeHandler.queueCommand(new SendCommand(maxDeviceSerial, cmd, "Update Thermostat Properties"));
            sendCCommand();
        } catch (NullPointerException e) {
            logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle update without bridge.");
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
        }
    }

    /**
     * Trigger update by sending C command.
     * This command is delayed as it takes time to have the updates back from the thermostat
     */
    private void sendCCommand() {
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                C_Command cmd = new C_Command(rfAddress);
                bridgeHandler.queueCommand(new SendCommand(maxDeviceSerial, cmd, "Refresh Thermostat Properties"));
                configSet = false;
            }
        }, COMMUNICATION_DELAY_TIME, TimeUnit.SECONDS);
    }

    /**
     * sends the T command to the Cube to disassociate the device from the MAX! Cube.
     */
    private void deviceDelete() {
        MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
        if (maxCubeBridge != null) {
            maxCubeBridge.sendDeviceDelete(maxDeviceSerial);
            dispose();
        }
    }

    /**
     * Updates the device & roomname
     *
     * @param configurationParameter
     */
    private void updateDeviceName(Entry<String, Object> configurationParameter) {
        try {
            Device device = getMaxCubeBridgeHandler().getDevice(maxDeviceSerial);
            String name = configurationParameter.getValue().toString();
            if (configurationParameter.getKey().equals(PROPERTY_DEVICENAME) && !(name.equals(device.getName()))) {
                logger.info("Updating device name for {} to {}", getThing().getUID().toString(), name);
                device.setName(name);
                bridgeHandler.sendDeviceAndRoomNameUpdate(name);
                SendCommand sendCommand = new SendCommand(maxDeviceSerial, new Q_Command(), "Reload Data");
                bridgeHandler.queueCommand(sendCommand);
            }
            if (configurationParameter.getKey().equals(PROPERTY_ROOMID)
                    || configurationParameter.getKey().equals(PROPERTY_ROOMNAME)) {
                int roomId = ((BigDecimal) configurationParameter.getValue()).intValue();
                if (roomId != device.getRoomId()) {
                    logger.info("Updating room for {} to {}", getThing().getUID().getAsString(), roomId);
                    device.setRoomId(roomId);
                    // TODO: handle if a room has no more devices, probably should be deleted. Also handle if room rfId
                    // is no longer valid as the related device is movd to another room
                    bridgeHandler.sendDeviceAndRoomNameUpdate(name);
                    SendCommand sendCommand = new SendCommand(maxDeviceSerial,
                            Z_Command.wakeupDevice(device.getRFAddress()),
                            "WakeUp device" + getThing().getUID().getAsString());
                    bridgeHandler.queueCommand(sendCommand);
                    sendCommand = new SendCommand(maxDeviceSerial,
                            new S_ConfigCommand(device.getRFAddress(), roomId, ConfigCommandType.SetRoom), "Set Room");
                    bridgeHandler.queueCommand(sendCommand);

                    sendCommand = new SendCommand(maxDeviceSerial, new Q_Command(), "Reload Data");
                    bridgeHandler.queueCommand(sendCommand);
                    sendCCommand();
                }

            }
        } catch (NullPointerException e) {
            logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle update without bridge.");
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
        }
    }

    private synchronized MaxCubeBridgeHandler getMaxCubeBridgeHandler() {

        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.debug("Required bridge not defined for device {}.", maxDeviceSerial);
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MaxCubeBridgeHandler) {
                this.bridgeHandler = (MaxCubeBridgeHandler) handler;
                this.bridgeHandler.registerDeviceStatusListener(this);
                forceRefresh = true;
            } else {
                logger.debug("No available bridge handler found for {} bridge {} .", maxDeviceSerial, bridge.getUID());
                return null;
            }
        }
        return this.bridgeHandler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        MaxCubeBridgeHandler maxCubeBridge = getMaxCubeBridgeHandler();
        if (maxCubeBridge == null) {
            logger.warn("MAX! Cube LAN gateway bridge handler not found. Cannot handle command without bridge.");
            return;
        }
        if (command instanceof RefreshType) {
            forceRefresh = true;
            maxCubeBridge.handleCommand(channelUID, command);
            return;
        }
        if (maxDeviceSerial == null) {
            logger.warn("Serial number missing. Can't send command to device '{}'", getThing());
            return;
        }

        if (channelUID.getId().equals(CHANNEL_SETTEMP) || channelUID.getId().equals(CHANNEL_MODE)) {
            if (refreshingActuals) {
                refreshActualsRestore();
            }
            SendCommand sendCommand = new SendCommand(maxDeviceSerial, channelUID, command);
            maxCubeBridge.queueCommand(sendCommand);
        } else {
            logger.warn("Setting of channel {} not possible. Read-only", channelUID);
        }
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, Device device) {
        if (device.getSerialNumber().equals(maxDeviceSerial)) {
            if (!device.isLinkStatusError()) {
                if (!refreshingActuals) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Updating Actual Temperature");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
            }
            if (!propertiesSet) {
                setProperties(device);
            }
            if (!configSet) {
                setDeviceConfiguration(device);
            }
            if (refreshActualRate >= REFRESH_ACTUAL_MIN_RATE && (device.getType() == DeviceType.HeatingThermostat
                    || device.getType() == DeviceType.HeatingThermostatPlus)) {
                refreshActualCheck((HeatingThermostat) device);
            }
            if (device.isUpdated() || forceRefresh) {
                logger.debug("Updating states of {} {} ({}) id: {}", device.getType(), device.getName(),
                        device.getSerialNumber(), getThing().getUID());
                switch (device.getType()) {
                    case WallMountedThermostat:
                    case HeatingThermostat:
                    case HeatingThermostatPlus:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_LOCKED),
                                ((HeatingThermostat) device).isPanelLocked() ? OpenClosedType.CLOSED
                                        : OpenClosedType.OPEN);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP),
                                ((HeatingThermostat) device).getTemperatureSetpoint());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MODE),
                                ((HeatingThermostat) device).getModeString());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
                                ((HeatingThermostat) device).getBatteryLow());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_VALVE),
                                ((HeatingThermostat) device).getValvePosition());
                        DecimalType actualTemp = ((HeatingThermostat) device).getTemperatureActual();
                        if (!actualTemp.equals(DecimalType.ZERO)) {
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_ACTUALTEMP), actualTemp);
                        }
                        break;
                    case ShutterContact:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONTACT_STATE),
                                ((ShutterContact) device).getShutterState());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
                                ((ShutterContact) device).getBatteryLow());
                        break;
                    case EcoSwitch:
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_BATTERY),
                                ((EcoSwitch) device).getBatteryLow());
                        break;
                    default:
                        logger.debug("Unhandled Device {}.", device.getType());
                        break;
                }
                forceRefresh = false;
                device.setUpdated(false);
            } else {
                logger.debug("No changes for {} {} ({}) id: {}", device.getType(), device.getName(),
                        device.getSerialNumber(), getThing().getUID());
            }
        }
    }

    private void refreshActualCheck(HeatingThermostat device) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        if (device.getActualTempLastUpdated() == null) {
            Calendar t = Calendar.getInstance();
            t.add(Calendar.MINUTE, REFRESH_ACTUAL_MIN_RATE * -1);
            device.setActualTempLastUpdated(t.getTime());
            logger.info("Actual date reset for {} {} ({}) id: {}", device.getType(), device.getName(),
                    device.getSerialNumber(), getThing().getUID());
        }
        long timediff = Calendar.getInstance().getTime().getTime() - device.getActualTempLastUpdated().getTime();
        if (timediff > (refreshActualRate * 1000 * 60)) {
            if (!refreshingActuals) {

                logger.debug("Actual needs updating for {} {} ({}) id: {}", device.getType(), device.getName(),
                        device.getSerialNumber(), getThing().getUID());

                originalSetTemp = device.getTemperatureSetpoint();
                originalMode = device.getMode();

                if (originalMode == ThermostatModeType.MANUAL || originalMode == ThermostatModeType.AUTOMATIC) {
                    BigDecimal temporaryTemp = originalSetTemp.toBigDecimal().add(BigDecimal.valueOf(0.5));
                    logger.debug("Actuals Refresh: Setting Temp {}", temporaryTemp);
                    handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP), new DecimalType(temporaryTemp));
                    refreshingActuals = true;
                } else {
                    logger.debug("Defer Actuals refresh. Only manual refresh for mode AUTOMATIC & MANUAL");
                    device.setActualTempLastUpdated(Calendar.getInstance().getTime());
                }

                if (refreshingActuals) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Updating Actual Temperature");

                    if (refreshActualsJob == null || refreshActualsJob.isCancelled()) {
                        refreshActualsJob = scheduler.schedule(refreshActualsRestoreRunnable, REFRESH_ACTUAL_DURATION,
                                TimeUnit.SECONDS);
                    }

                    device.setActualTempLastUpdated(Calendar.getInstance().getTime());
                }
            }
            logger.debug("Actual Refresh in progress for {} {} ({}) id: {}", device.getType(), device.getName(),
                    device.getSerialNumber(), getThing().getUID());
        } else {
            logger.trace("Actual date for {} {} ({}) : {}", device.getType(), device.getName(),
                    device.getSerialNumber(), dateFormat.format(device.getActualTempLastUpdated().getTime()));
        }

    }

    /**
     * Send the commands to restore the original settings for mode & temperature
     * to end the automatic update cycle
     */
    private synchronized void refreshActualsRestore() {
        try {
            refreshingActuals = false;
            if (originalMode == ThermostatModeType.AUTOMATIC || originalMode == ThermostatModeType.MANUAL) {
                logger.debug("Finished Actuals Refresh: Restoring Temp {}", originalSetTemp);
                handleCommand(new ChannelUID(getThing().getUID(), CHANNEL_SETTEMP), originalSetTemp);
            }

            if (refreshActualsJob != null && !refreshActualsJob.isCancelled()) {
                refreshActualsJob.cancel(true);
                refreshActualsJob = null;
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during Actuals Refresh : {}", e.getMessage(), e);
        }
    }

    @Override
    public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device) {
        if (device.getSerialNumber().equals(maxDeviceSerial)) {
            bridgeHandler.unregisterDeviceStatusListener(this);
            bridgeHandler = null;
            forceRefresh = true;
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        forceRefresh = true;
    }

    /**
     * Set the forceRefresh flag to ensure update when next data is coming
     */
    public void setForceRefresh() {
        forceRefresh = true;
    }

    /**
     * Set the properties for this device
     *
     * @param device
     */
    private void setProperties(Device device) {
        try {
            logger.debug("MAX! {} {} properties update", device.getType().toString(), device.getSerialNumber());
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_MODEL_ID, device.getType().toString());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber());
            properties.put(Thing.PROPERTY_VENDOR, MaxBinding.PROPERTY_VENDOR_NAME);
            updateProperties(properties);
            logger.debug("properties updated");
            propertiesSet = true;
        } catch (Exception e) {
            logger.debug("Exception occurred during property edit: {}", e.getMessage(), e);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge Status updated to {} for device: {}", bridgeStatusInfo.getStatus().toString(),
                getThing().getUID().toString());
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            // No action
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            forceRefresh = true;
        }
    }

    /**
     * Set the Configurable properties for this device
     *
     * @param device
     */
    private void setDeviceConfiguration(Device device) {
        try {
            logger.debug("MAX! {} {} configuration update", device.getType().toString(), device.getSerialNumber());
            Configuration configuration = editConfiguration();
            configuration.put(MaxBinding.PROPERTY_ROOMNAME, device.getRoomName());
            configuration.put(MaxBinding.PROPERTY_ROOMID, new BigDecimal(device.getRoomId()));
            configuration.put(MaxBinding.PROPERTY_DEVICENAME, device.getName());
            configuration.put(MaxBinding.PROPERTY_RFADDRESS, device.getRFAddress());
            // Add additional device config entries
            for (Map.Entry<String, Object> entry : device.getProperties().entrySet()) {
                configuration.put(entry.getKey(), entry.getValue());
            }
            updateConfiguration(configuration);
            logger.debug("Config updated: {}", configuration.getProperties());
            configSet = true;
        } catch (Exception e) {
            logger.debug("Exception occurred during configuration edit: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onDeviceConfigUpdate(Bridge bridge, Device device) {
        if (device.getSerialNumber().equals(maxDeviceSerial)) {
            setDeviceConfiguration(device);
        }
    }
}
