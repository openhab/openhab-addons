import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.daikinmadoka.DaikinMadokaBindingConstants;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OPERATION_MODE;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetIndoorOutoorTemperatures;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetSetpointCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetVersionCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetPowerstateCommand;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;

import io.reactivex.annotations.NonNull;




// ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Refresh Current Mode (0x30 / 48)
    // */
    // private void refreshCurrentMode() {
    // logger.debug("[{}] refreshCurrentMode()", super.thing.getUID().getId());
    //
    // byte[] msg = new byte[] { 0x00, 0x06, 0x00, 0x00, 0x30, 0x00, 0x00 };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return;
    // }
    //
    // MadokaMessage mm = MadokaMessage.parse(resp);
    // logger.debug("[{}] MadokaMessage: {}", super.thing.getUID().getId(), mm);
    // if (mm == null || mm.getValues() == null || mm.getValues().get(0x20) == null) {
    // logger.error("[{}] Message null (not normal)", super.thing.getUID().getId());
    // return;
    // }
    //
    // logger.debug("[{}] Operation Mode Int Value: {}", super.thing.getUID().getId(),
    // mm.getValues().get(0x20).getRawValue()[0]);
    // OPERATION_MODE operationMode = OPERATION_MODE.valueOf(mm.getValues().get(0x20).getRawValue()[0]);
    //
    // this.brc1h_currentMode = operationMode;
    //
    // logger.debug("[{}] operationMode: {}", super.thing.getUID().getId(), operationMode);
    //
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE),
    // new StringType(operationMode.name()));
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Refresh Cooling / Heating Setpoints (0x40)
    // */
    // @SuppressWarnings("null")
    // private void refreshSetpoints() {
    // logger.debug("[{}] refreshSetpoints()", super.thing.getUID().getId());
    //
    // byte[] msg = new byte[] { 0x00, 0x06, 0x00, 0x00, 0x40, 0x00, 0x00 };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return;
    // }
    //
    // MadokaMessage mm = MadokaMessage.parse(resp);
    //
    // if (mm == null || mm.getValues() == null || mm.getValues().get(0x21) == null
    // || mm.getValues().get(0x20) == null) {
    // logger.debug("[{}] The message did not arrive well formed (not normal), exiting function",
    // super.thing.getUID().getId());
    // return;
    // }
    //
    // this.brc1h_heatingSetpoint = mm.getValues().get(0x21).getComputedValue() / 128.;
    // this.brc1h_coolingSetpoint = mm.getValues().get(0x20).getComputedValue() / 128.;
    //
    // int setpointMode = (int) mm.getValues().get(0x31).getComputedValue();
    //
    // logger.debug("[{}] heatingSetpoint: {}", super.thing.getUID().getId(), this.brc1h_heatingSetpoint);
    // logger.debug("[{}] coolingSetpoint: {}", super.thing.getUID().getId(), this.brc1h_coolingSetpoint);
    // logger.debug("[{}] setpointMode: {}", super.thing.getUID().getId(), setpointMode);
    //
    // if (this.brc1h_heatingSetpoint != null) {
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_HEATING_SETPOINT),
    // new DecimalType(this.brc1h_heatingSetpoint));
    // }
    //
    // if (this.brc1h_coolingSetpoint != null) {
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_COOLING_SETPOINT),
    // new DecimalType(this.brc1h_coolingSetpoint));
    // }
    //
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Query 80 (0x0050)
    // */
    // @SuppressWarnings("null")
    // private void refreshFanSpeed() {
    // logger.debug("[{}] refreshFanSpeed()", super.thing.getUID().getId());
    //
    // byte[] msg = new byte[] { 0x00, 0x06, 0x00, 0x00, 0x50, 0x00, 0x00 };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return;
    // }
    //
    // MadokaMessage mm = MadokaMessage.parse(resp);
    //
    // this.brc1h_coolingFanSpeed = FAN_SPEED.valueOf(mm.getValues().get(0x20).getRawValue()[0]);
    // this.brc1h_heatingFanSpeed = FAN_SPEED.valueOf(mm.getValues().get(0x21).getRawValue()[0]);
    //
    // logger.debug("[{}] Cooling Fan Speed : {}", super.thing.getUID().getId(), this.brc1h_coolingFanSpeed);
    // logger.debug("[{}] Heating Fan Speed : {}", super.thing.getUID().getId(), this.brc1h_heatingFanSpeed);
    //
    // if (this.brc1h_coolingFanSpeed != null) {
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_COOLING_FAN_SPEED),
    // new DecimalType(this.brc1h_coolingFanSpeed.value()));
    // }
    //
    // if (this.brc1h_heatingFanSpeed != null) {
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_HEATING_FAN_SPEED),
    // new DecimalType(this.brc1h_heatingFanSpeed.value()));
    // }
    //
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // @SuppressWarnings("null")
    // private void refreshIndoorOutdoorTemperature() {
    // logger.debug("[{}] refreshIndoorOutdoorTemperature()", super.thing.getUID().getId());
    //
    // byte[] msg = new byte[] { 0x00, 0x06, 0x00, 0x01, 0x10, 0x00, 0x00 };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return;
    // }
    //
    // MadokaMessage mm = MadokaMessage.parse(resp);
    //
    // this.brc1h_indoorTemperature = Integer.valueOf(mm.getValues().get(0x40).getRawValue()[0]);
    // this.brc1h_outdoorTemperature = Integer.valueOf(mm.getValues().get(0x41).getRawValue()[0]);
    //
    // if (this.brc1h_outdoorTemperature == -1) {
    // this.brc1h_outdoorTemperature = null;
    // } else {
    // if (this.brc1h_outdoorTemperature < 0) {
    // this.brc1h_outdoorTemperature = ((this.brc1h_outdoorTemperature + 256) - 128) * -1;
    // }
    // }
    //
    // logger.debug("[{}] Indoor Temp: {}", super.thing.getUID().getId(), this.brc1h_indoorTemperature);
    // logger.debug("[{}] Outdoor Temp: {}", super.thing.getUID().getId(), this.brc1h_outdoorTemperature);
    //
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_TEMPERATURE),
    // new DecimalType(this.brc1h_indoorTemperature));
    //
    // logger.debug("Channel: {}",
    // new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_TEMPERATURE));
    //
    // if (this.brc1h_outdoorTemperature != null) {
    // updateState(
    // new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OUTDOOR_TEMPERATURE),
    // new DecimalType(this.brc1h_outdoorTemperature));
    // } else {
    // // Outdoor temperature is not supported by all devices
    // updateState(
    // new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OUTDOOR_TEMPERATURE),
    // UnDefType.UNDEF);
    // }
    //
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Function ID 304 (0x0130)
    // */
    // private void refreshVersion() {
    // logger.debug("[{}] refreshVersion()", super.thing.getUID().getId());
    //
    // byte[] msg = new byte[] { 0x00, 0x0c, 0x00, 0x01, 0x30, 0x30, 0x00, 0x31, 0x00, 0x45, 0x00, 0x46, 0x00 };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null || resp.length == 0) {
    // return;
    // }
    //
    // int remoteController_major = resp[27];
    // int remoteController_minor = resp[28];
    // int remoteController_revision = resp[29];
    //
    // this.brc1h_remoteControllerVersion = remoteController_major + "." + remoteController_minor + "."
    // + remoteController_revision;
    //
    // int commController_major = resp[32];
    // int commController_minor = resp[33];
    //
    // this.brc1h_communicationControllerVersion = commController_major + "." + commController_minor;
    //
    // logger.debug("[{}] RemoteController Version: {}", super.thing.getUID().getId(),
    // this.brc1h_remoteControllerVersion);
    // logger.debug("[{}] Communication Controller Version: {}", super.thing.getUID().getId(),
    // this.brc1h_communicationControllerVersion);
    //
    // updateState(
    // new ChannelUID(getThing().getUID(),
    // DaikinMadokaBindingConstants.CHANNEL_ID_COMMUNICATION_CONTROLLER_VERSION),
    // new StringType(this.brc1h_communicationControllerVersion));
    //
    // updateState(
    // new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_REMOTE_CONTROLLER_VERSION),
    // new StringType(this.brc1h_remoteControllerVersion));
    //
    // logger.debug("[{}] Channel: {}", super.thing.getUID().getId(),
    // new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_REMOTE_CONTROLLER_VERSION));
    //
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////////////////////////

    // /**
    // * Power state (AC ON or OFF)
    // * Query ID 32 (0x20)
    // */
    // private void refreshPowerState() {
    // logger.debug("[{}] refreshPowerState()", super.thing.getUID().getId());
    //
    // byte[] msg = new byte[] { 0x00, 0x06, 0x00, 0x00, 0x20, 0x00, 0x00 };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return;
    // }
    //
    // MadokaMessage mm = MadokaMessage.parse(resp);
    //
    // if (mm == null || mm.getValues() == null || mm.getValues().get(0x20) == null) {
    // return;
    // }
    //
    // this.brc1h_poweredOn = (mm.getValues().get(0x20).getRawValue()[0] == 1 ? Boolean.TRUE : Boolean.FALSE);
    //
    // logger.debug("[{}] AC Module On: {}", super.thing.getUID().getId(), this.brc1h_poweredOn);
    //
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS),
    // (this.brc1h_poweredOn == true ? OnOffType.ON : OnOffType.OFF));
    //
    // }

    ///////////////////////////////////////////////////////////////////////////////////////////////



    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Set FAN speed (1/3/5). If Value is out of this range, it will be ignored
    // *
    // * @param intValue
    // */
    // @SuppressWarnings("null")
    // private boolean setFanSpeed(int temperature) {
    //
    // FAN_SPEED fs = FAN_SPEED.valueOf(temperature);
    // if (fs == null) {
    // return false;
    // }
    //
    // if (this.brc1h_currentMode == null) {
    // return false;
    // }
    //
    // switch (this.brc1h_currentMode) {
    // case COOL:
    // return setCoolingFanSpeed(fs.value());
    // case HEAT:
    // return setHeatingFanSpeed(fs.value());
    // default:
    // logger.debug("[{}] Unsupported operation (yet!)", super.thing.getUID().getId());
    // return false;
    // }
    //
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // @SuppressWarnings("null")
    // private boolean setHeatingFanSpeed(int value) {
    //
    // logger.debug("[{}] Will change the Heating Fan Speed to {}", super.thing.getUID().getId(), value);
    //
    // byte[] msg = new byte[] { 0x00, 0x07, 0x00, 0x40, 0x50, 0x21, 0x01, (byte) value };
    //
    // logger.debug("[{}] SetHeatingSetpoint Message: {}", super.thing.getUID().getId(), msg);
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return false;
    // }
    //
    // // MadokaMessage mm = MadokaMessage.parse(resp);
    // this.brc1h_heatingFanSpeed = FAN_SPEED.valueOf(value);
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_HEATING_FAN_SPEED),
    // new DecimalType(this.brc1h_heatingFanSpeed.value()));
    //
    // return true;
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // @SuppressWarnings("null")
    // private boolean setCoolingFanSpeed(int value) {
    //
    // logger.debug("[{}] Will change the Cooling Fan Speed to {}", super.thing.getUID().getId(), value);
    //
    // byte[] msg = new byte[] { 0x00, 0x07, 0x00, 0x40, 0x50, 0x20, 0x01, (byte) value };
    //
    // // logger.debug("setCoolingFanSpeed Message: {}", msg);
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return false;
    // }
    //
    // // MadokaMessage mm = MadokaMessage.parse(resp);
    // this.brc1h_coolingFanSpeed = FAN_SPEED.valueOf(value);
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_COOLING_FAN_SPEED),
    // new DecimalType(this.brc1h_coolingFanSpeed.value()));
    //
    // return true;
    //
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Will change the AC operating mode (COOL/HEAT...)
    // *
    // * @param mode
    // * @return
    // */
    // private boolean setOperatingMode(OPERATION_MODE mode) {
    // logger.debug("[{}] Will change the Operating mode to {} ({})", super.thing.getUID().getId(), mode,
    // mode.value());
    //
    // byte[] msg = new byte[] { 0x00, 0x07, 0x00, 0x40, 0x30, 0x20, 0x01, (byte) mode.value() };
    //
    // // logger.debug("setOperatingMode Message: {}", msg);
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // return false;
    // }
    //
    // this.brc1h_currentMode = mode;
    //
    // // MadokaMessage mm = MadokaMessage.parse(resp);
    //
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE),
    // new StringType(mode.name()));
    //
    // return true;
    // }
    //
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    //
    // /**
    // * Turn ON or OFF the unit. We don't expect answer
    // *
    // * @param on True = turn on device
    // */
    // @SuppressWarnings("null")
    // private boolean setPowerState(boolean on) {
    // logger.debug("[{}] setPowerState({})", super.thing.getUID().getId(), on);
    //
    // byte[] msg = new byte[] { 0x00, 0x07, 0x00, 0x40, 0x20, 0x20, 0x01, (byte) (on == true ? 0x01 : 0x00) };
    //
    // byte[] resp = writeMessageAndGetResponse(msg);
    //
    // if (resp == null) {
    // logger.debug("[{}] Got an empty response to setPowerState!", super.thing.getUID().getId());
    // return false;
    // }
    //
    // // MadokaMessage mm = MadokaMessage.parse(resp);
    // this.brc1h_poweredOn = on;
    // updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS),
    // (this.brc1h_poweredOn == true ? OnOffType.ON : OnOffType.OFF));
    //
    // return true;
    // }



    @SuppressWarnings("null")
    private boolean setHeatingSetpoint(Double setpoint) {

        logger.debug("[{}] Will change the Heating Setpoint to {}", super.thing.getUID().getId(), setpoint);

        byte[] heatingSetpoint_bytes = ByteBuffer.allocate(2).putShort((short) (128. * setpoint)).array();

        byte[] msg = new byte[] { 0x00, 0x08, 0x00, 0x40, 0x40, 0x21, 0x02, heatingSetpoint_bytes[0],
                heatingSetpoint_bytes[1] };

        logger.debug("[{}] SetHeatingSetpoint Message: {}", super.thing.getUID().getId(), msg);

        byte[] resp = writeMessageAndGetResponse(msg);

        if (resp == null) {
            return false;
        }

        // MadokaMessage mm = MadokaMessage.parse(resp);
        this.brc1h_heatingSetpoint = setpoint;
        updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_HEATING_SETPOINT),
                new DecimalType(this.brc1h_heatingSetpoint));

        return true;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("null")
    private boolean setCoolingSetpoint(Double setpoint) {

        logger.debug("[{}] Will change the Cooling Setpoint to {}", super.thing.getUID().getId(), setpoint);

        byte[] heatingSetpoint_bytes = ByteBuffer.allocate(2).putShort((short) (128. * setpoint)).array();

        byte[] msg = new byte[] { 0x00, 0x08, 0x00, 0x40, 0x40, 0x20, 0x02, heatingSetpoint_bytes[0],
                heatingSetpoint_bytes[1] };

        logger.debug("[{}] SetHeatingSetpoint Message: {}", super.thing.getUID().getId(), msg);

        byte[] resp = writeMessageAndGetResponse(msg);

        if (resp == null) {
            return false;
        }

        // MadokaMessage mm = MadokaMessage.parse(resp);
        this.brc1h_coolingSetpoint = setpoint;
        updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_COOLING_SETPOINT),
                new DecimalType(this.brc1h_coolingSetpoint));

        return true;
    }
    

    @SuppressWarnings("null")
    private boolean setSetpointTemperature(Double setpoint) {

        if (this.brc1h_currentMode == null) {
            // Cannot determine the current mode so return
            return false;
        }

        switch (this.brc1h_currentMode) {
            case COOL:
                if (!setCoolingSetpoint(setpoint)) {
                    return false;
                }
                break;
            case HEAT:
                if (!setHeatingSetpoint(setpoint)) {
                    return false;
                }
                break;
            default:
                logger.debug("[{}] Unsupported operation (yet!)", super.thing.getUID().getId());
                return false;
        }

        computeVirtualChannels();
        return true;
    }


    

    // @SuppressWarnings("null")
    // private byte @Nullable [] writeMessageAndGetResponse(byte[] message) {
    // logger.debug("[{}] writeMessageAndGetResponse()", super.thing.getUID().getId());
    //
    // if (charWrite == null) {
    // return null;
    // }
    //
    // @NonNull
    // BluetoothCharacteristic c = charWrite;
    //
    // synchronized (lockInUse) {
    //
    // // // clear queue
    // uartMessages.clear();
    //
    // try {
    // c.setValue(message);
    // this.device.writeCharacteristic(c);
    // } catch (Exception e) {
    // logger.error("Error while writing characterstics", e);
    // return null;
    // }
    //
    // // NEW METHOD
    // try {
    //
    // synchronized (lock) {
    // lock.wait(3 * 1000);
    // }
    //
    // } catch (InterruptedException e) {
    // // Timeout
    // logger.info("[{}] Reply not received. Cleaning queue.", super.thing.getUID().getId());
    // uartMessages.clear();
    // return null;
    // }
    //
    // // Beyond this point, full message received
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //
    // for (byte[] msg : uartMessages.toArray(new byte[][] {})) {
    // try {
    // bos.write(Arrays.copyOfRange(msg, 1, msg.length));
    // } catch (Exception e) {
    // // should never happen.
    // logger.error("Unexpected error", e);
    // }
    // }
    //
    // return bos.toByteArray();
    //
    // }
    // }

    

    /**
     * Refresh the virtual channels : setpoint, fanspeed, homekit current + target modes
     */
    @SuppressWarnings("null")
    private void computeVirtualChannels() {
        logger.debug("[{}] computeVirtualChannels()", super.thing.getUID().getId());

        logger.debug("[{}] brc1h_currentMode: {}", super.thing.getUID().getId(), brc1h_currentMode);

        if (brc1h_currentMode == null) {
            return;
        }

        switch (brc1h_currentMode) {
            case COOL:
                this.brc1h_setpoint = this.brc1h_coolingSetpoint;
                this.brc1h_fanSpeed = this.brc1h_coolingFanSpeed;
                break;
            case HEAT:
                this.brc1h_setpoint = this.brc1h_heatingSetpoint;
                this.brc1h_fanSpeed = this.brc1h_heatingFanSpeed;
                break;
            default:
                break;
        }

        if (this.brc1h_fanSpeed != null) {
            updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED),
                    new DecimalType(this.brc1h_fanSpeed.value()));
        }
        if (this.brc1h_setpoint != null) {
            updateState(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT),
                    new DecimalType(this.brc1h_setpoint));
        }

        refreshHomeKitStatus();

    }
    
    
    

    private void refreshChannels() {
        logger.debug("[{}] refreshChannels()", super.thing.getUID().getId());

        // The firmware cannot change itself ! So no need to refresh it everytime...
        if (this.brc1h_remoteControllerVersion == null || this.brc1h_communicationControllerVersion == null) {
            // refreshVersion();
            submitCommand(new GetVersionCommand());
        }

        // refreshPowerState();
        submitCommand(new GetPowerstateCommand());

        // refreshIndoorOutdoorTemperature();
        submitCommand(new GetIndoorOutoorTemperatures());

        // refreshFanSpeed();
        submitCommand(new GetFanspeedCommand());

        // refreshSetpoints();
        submitCommand(new GetSetpointCommand());

        // refreshCurrentMode();
        submitCommand(new GetOperationmodeCommand());

        /**
         * There are virtual channels to compute values not issues
         * from device, but depending on actual modes
         */
        computeVirtualChannels();

    }
    
    
    

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {

        logger.debug("[{}] onConnectionStateChange({})", super.thing.getUID().getId(),
                connectionNotification.getConnectionState());

        if (connectionNotification.getConnectionState() == ConnectionState.CONNECTED) {
            updateStatus(ThingStatus.ONLINE);

            device.discoverServices();

            // We are now connected!!! Time for fun
            this.charNotif = this.device
                    .getCharacteristic(UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID));
            this.charWrite = this.device
                    .getCharacteristic(UUID.fromString(DaikinMadokaBindingConstants.CHAR_WRITE_WITHOUT_RESPONSE_UUID));

            logger.debug("[{}] CharNotif: {}", super.thing.getUID().getId(), charNotif);
            logger.debug("[{}] CharWrite: {}", super.thing.getUID().getId(), charWrite);

            if (charNotif != null) {
                @NonNull
                BluetoothCharacteristic c = charNotif;
                if (this.device.enableNotifications(c)) {
                    logger.debug("[{}] Enabled notifications successfully!", super.thing.getUID().getId());

                    // Refresh Channels
                    refreshChannels();
                } else {
                    logger.debug("[{}] Could not enable notifications... Disconnecting", super.thing.getUID().getId());
                    device.disconnect();
                    return;
                }
            } else {
                logger.debug("[{}] Appropriate characteristic could not be found on BLE dev",
                        super.thing.getUID().getId());
                device.disconnect();
                return;
            }

        } else if (connectionNotification.getConnectionState() == ConnectionState.DISCONNECTED) {
            updateStatus(ThingStatus.OFFLINE);

        }

    }
    
    
    
    

    @SuppressWarnings("null")
    private void refreshHomeKitStatus() {

        // Compute HomeKit Channels
        // Values must be:
        // - Cooling
        // - Heating
        // - Off

        if (this.brc1h_poweredOn == false) {
            this.brc1h_homekitCurrentHeatingCoolingMode = "Off";
            this.brc1h_homekitTargetHeatingCoolingMode = "Off";
        } else {
            switch (this.brc1h_currentMode) {
                case COOL:
                    this.brc1h_homekitCurrentHeatingCoolingMode = "Cooling";
                    this.brc1h_homekitTargetHeatingCoolingMode = "CoolOn";
                    break;
                case HEAT:
                    this.brc1h_homekitCurrentHeatingCoolingMode = "Heating";
                    this.brc1h_homekitTargetHeatingCoolingMode = "HeatOn";
                    break;
                default:
                    break;
            }
        }

        updateState(
                new ChannelUID(getThing().getUID(),
                        DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                new StringType(this.brc1h_homekitCurrentHeatingCoolingMode));

        // if (lockHomekitTargetMode.tryLock()) {

        updateState(
                new ChannelUID(getThing().getUID(),
                        DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE),
                new StringType(this.brc1h_homekitTargetHeatingCoolingMode));

        // lockHomekitTargetMode.unlock();
        // }

    }
    
    

    /**
     * Change Operation Mode through HomeKit
     *
     * @param fullString
     */
    @SuppressWarnings({ "null", "unused" })
    private void processHomeKitTargetMode(String homekitTargetMode) {

        try {

            // Supported modes:
            // - CoolOn
            // - HeatOn
            // - Auto
            // - Off

            if (homekitTargetMode == null) {
                return;
            }

            switch (homekitTargetMode) {
                case "CoolOn":
                    // Change Mode to Cool first
                    submitCommand(new SetOperationmodeCommand(OPERATION_MODE.COOL));
                    // setOperatingMode(OPERATION_MODE.COOL);
                    if (this.brc1h_poweredOn == false) {
                        // Turn On Unit
                        // setPowerState(true);
                        submitCommand(new SetPowerstateCommand(OnOffType.ON));
                    }
                    break;
                case "HeatOn":
                    // Change Mode to Cool first
                    // setOperatingMode(OPERATION_MODE.HEAT);
                    submitCommand(new SetOperationmodeCommand(OPERATION_MODE.HEAT));

                    if (this.brc1h_poweredOn == false) {
                        // Turn On Unit
                        // setPowerState(true);
                        submitCommand(new SetPowerstateCommand(OnOffType.ON));
                    }
                    break;
                case "Auto":
                    // Change Mode to Auto first
                    // setOperatingMode(OPERATION_MODE.AUTO);
                    submitCommand(new SetOperationmodeCommand(OPERATION_MODE.AUTO));

                    if (this.brc1h_poweredOn == false) {
                        // Turn On Unit
                        // setPowerState(true);
                        submitCommand(new SetPowerstateCommand(OnOffType.ON));

                    }
                    break;
                case "Off":
                    if (this.brc1h_poweredOn == true) {
                        // Turn Off Unit
                        // setPowerState(false);
                        submitCommand(new SetPowerstateCommand(OnOffType.OFF));
                    }
                    break;
            }

        } catch (Exception e) {
            logger.error("Unexpected error in processHomeKitTargetMode", e);
        } finally {

        }
    }
    