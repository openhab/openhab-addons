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
package org.openhab.binding.yeelight.internal.handler;

import static org.openhab.binding.yeelight.internal.YeelightBindingConstants.*;

import java.util.concurrent.TimeUnit;

import org.openhab.binding.yeelight.internal.lib.device.ConnectState;
import org.openhab.binding.yeelight.internal.lib.device.DeviceBase;
import org.openhab.binding.yeelight.internal.lib.device.DeviceFactory;
import org.openhab.binding.yeelight.internal.lib.device.DeviceStatus;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceAction;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceMode;
import org.openhab.binding.yeelight.internal.lib.enums.DeviceType;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceConnectionStateListener;
import org.openhab.binding.yeelight.internal.lib.listeners.DeviceStatusChangeListener;
import org.openhab.binding.yeelight.internal.lib.services.DeviceManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YeelightHandlerBase} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 * @author Joe Ho - Added Duration Thing parameter
 * @author Nikita Pogudalov - Added DeviceType for ceiling 1
 */
public abstract class YeelightHandlerBase extends BaseThingHandler
        implements DeviceConnectionStateListener, DeviceStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(YeelightHandlerBase.class);
    protected DeviceBase mDevice;

    // Reading the deviceId from the properties map.
    private String deviceId = getThing().getConfiguration().get(PARAMETER_DEVICE_ID).toString();

    public YeelightHandlerBase(Thing thing) {
        super(thing);
    }

    protected void updateUI(DeviceStatus status) {
    }

    @Override
    public void initialize() {
        logger.debug("Initializing, Device ID: {}", deviceId);
        mDevice = DeviceFactory.build(getDeviceModel(getThing().getThingTypeUID()).name(), deviceId);
        mDevice.setDeviceName(getThing().getLabel());
        mDevice.setAutoConnect(true);
        DeviceManager.getInstance().addDevice(mDevice);
        mDevice.registerConnectStateListener(this);
        mDevice.registerStatusChangedListener(this);
        updateStatusHelper(mDevice.getConnectionState());
        DeviceManager.getInstance().startDiscovery(15 * 1000);
    }

    @Override
    public void dispose() {
        mDevice.disconnect();
    }

    private DeviceType getDeviceModel(ThingTypeUID typeUID) {
        if (typeUID.equals(THING_TYPE_CEILING)) {
            return DeviceType.ceiling;
        } else if (typeUID.equals(THING_TYPE_CEILING1)) {
            return DeviceType.ceiling1;
        } else if (typeUID.equals(THING_TYPE_CEILING3)) {
            return DeviceType.ceiling3;
        } else if (typeUID.equals(THING_TYPE_CEILING4)) {
            return DeviceType.ceiling4;
        } else if (typeUID.equals(THING_TYPE_WONDER)) {
            return DeviceType.color;
        } else if (typeUID.equals(THING_TYPE_DOLPHIN)) {
            return DeviceType.mono;
        } else if (typeUID.equals(THING_TYPE_CTBULB)) {
            return DeviceType.ct_bulb;
        } else if (typeUID.equals(THING_TYPE_STRIPE)) {
            return DeviceType.stripe;
        } else if (typeUID.equals(THING_TYPE_DESKLAMP)) {
            return DeviceType.desklamp;
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionStateChanged(ConnectState connectState) {
        logger.debug("onConnectionStateChanged -> {}", connectState.name());
        updateStatusHelper(connectState);
    }

    public void updateStatusHelper(ConnectState connectState) {
        switch (connectState) {
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device is offline!");
                if (mDevice.isAutoConnect()) {
                    DeviceManager.sInstance.startDiscovery(5 * 1000);
                    logger.debug("Thing OFFLINE. Initiated discovery");
                }
                break;
            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                mDevice.queryStatus();
                break;
            default:
                updateStatus(ThingStatus.UNKNOWN);
                break;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("ChannelLinked -> {}", channelUID.getId());
        super.channelLinked(channelUID);

        Runnable task = () -> {
            mDevice.queryStatus();
        };
        scheduler.schedule(task, 500, TimeUnit.MILLISECONDS);
    }

    public void handleCommandHelper(ChannelUID channelUID, Command command, String logInfo) {
        logger.debug("{}: {}", logInfo, command);

        // If device is disconnected, start discovery to reconnect.
        if (mDevice.isAutoConnect() && mDevice.getConnectionState() != ConnectState.CONNECTED) {
            DeviceManager.getInstance().startDiscovery(5 * 1000);
        }
        if (command instanceof RefreshType) {
            logger.debug("Refresh channel: {} Command: {}", channelUID, command);

            DeviceManager.getInstance().startDiscovery(5 * 1000);
            DeviceStatus s = mDevice.getDeviceStatus();

            switch (channelUID.getId()) {
                case CHANNEL_BRIGHTNESS:
                    updateState(channelUID, new PercentType(s.getBrightness()));
                    break;
                case CHANNEL_COLOR:
                    updateState(channelUID, HSBType.fromRGB(s.getR(), s.getG(), s.getB()));
                    break;
                case CHANNEL_COLOR_TEMPERATURE:
                    updateState(channelUID, new PercentType(s.getCt()));
                    break;
                case CHANNEL_BACKGROUND_COLOR:
                    final HSBType hsbType = new HSBType(new DecimalType(s.getHue()), new PercentType(s.getSat()),
                            new PercentType(s.getBackgroundBrightness()));
                    updateState(channelUID, hsbType);
                    break;
                default:
                    break;
            }
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_BRIGHTNESS:
                if (command instanceof PercentType percentCommand) {
                    handlePercentMessage(percentCommand);
                } else if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand);
                } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    handleIncreaseDecreaseBrightnessCommand(increaseDecreaseCommand);
                }
                break;
            case CHANNEL_COLOR:
                if (command instanceof HSBType hsbCommand) {
                    if (hsbCommand.getBrightness().intValue() == 0) {
                        handleOnOffCommand(OnOffType.OFF);
                    } else {
                        handleHSBCommand(hsbCommand);
                    }
                } else if (command instanceof PercentType percentCommand) {
                    handlePercentMessage(percentCommand);
                } else if (command instanceof OnOffType onOffCommand) {
                    handleOnOffCommand(onOffCommand);
                } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    handleIncreaseDecreaseBrightnessCommand(increaseDecreaseCommand);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType percentCommand) {
                    handleColorTemperatureCommand(percentCommand);
                } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
                    handleIncreaseDecreaseBrightnessCommand(increaseDecreaseCommand);
                }
                break;

            case CHANNEL_BACKGROUND_COLOR:
                if (command instanceof HSBType hsbCommand) {
                    handleBackgroundHSBCommand(hsbCommand);
                } else if (command instanceof PercentType percentCommand) {
                    handleBackgroundBrightnessPercentMessage(percentCommand);
                } else if (command instanceof OnOffType onOffCommand) {
                    handleBackgroundOnOffCommand(onOffCommand);
                }
                break;
            case CHANNEL_NIGHTLIGHT:
                if (command instanceof OnOffType) {
                    DeviceAction pAction = command == OnOffType.ON ? DeviceAction.nightlight_on
                            : DeviceAction.nightlight_off;
                    pAction.putDuration(getDuration());
                    DeviceManager.getInstance().doAction(deviceId, pAction);
                }
                break;
            case CHANNEL_COMMAND:
                if (!command.toString().isEmpty()) {
                    String[] tokens = command.toString().split(";");
                    String methodAction = tokens[0];
                    String methodParams = "";
                    if (tokens.length > 1) {
                        methodParams = tokens[1];
                    }
                    logger.debug("{}: {} {}", logInfo, methodAction, methodParams);
                    handleCustomCommand(methodAction, methodParams);
                    updateState(channelUID, new StringType(""));
                }
                break;
            default:
                break;
        }
    }

    void handlePercentMessage(PercentType brightness) {
        DeviceAction pAction;
        if (brightness.intValue() == 0) {
            pAction = DeviceAction.close;
            pAction.putDuration(getDuration());
            DeviceManager.getInstance().doAction(deviceId, pAction);
        } else {
            if (mDevice.getDeviceStatus().isPowerOff()) {
                pAction = DeviceAction.open;
                // hard coded to fast open, the duration should apply to brightness increase only
                pAction.putDuration(0);
                DeviceManager.getInstance().doAction(deviceId, pAction);
            }
            pAction = DeviceAction.brightness;
            pAction.putValue(brightness.intValue());
            pAction.putDuration(getDuration());
            DeviceManager.getInstance().doAction(deviceId, pAction);
        }
    }

    void handleIncreaseDecreaseBrightnessCommand(IncreaseDecreaseType increaseDecrease) {
        DeviceAction idbAcation = increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_bright
                : DeviceAction.decrease_bright;
        idbAcation.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, idbAcation);
    }

    void handleIncreaseDecreaseColorTemperatureCommand(IncreaseDecreaseType increaseDecrease) {
        DeviceAction idctAcation = increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_ct
                : DeviceAction.decrease_ct;
        idctAcation.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, idctAcation);
    }

    void handleOnOffCommand(OnOffType onoff) {
        DeviceAction ofAction = onoff == OnOffType.ON ? DeviceAction.open : DeviceAction.close;
        ofAction.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, ofAction);
    }

    void handleHSBCommand(HSBType color) {
        DeviceAction cAction = DeviceAction.color;
        cAction.putValue(color.getRGB() & 0xFFFFFF);
        cAction.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, cAction);
    }

    void handleBackgroundHSBCommand(HSBType color) {
        DeviceAction cAction = DeviceAction.background_color;

        // TODO: actions seem to be an insufficiant abstraction.
        cAction.putValue(color.getHue() + "," + color.getSaturation());
        cAction.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, cAction);
    }

    void handleBackgroundBrightnessPercentMessage(PercentType brightness) {
        DeviceAction pAction;

        pAction = DeviceAction.background_brightness;
        pAction.putValue(brightness.intValue());
        pAction.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, pAction);
    }

    private void handleBackgroundOnOffCommand(OnOffType command) {
        DeviceAction pAction = command == OnOffType.ON ? DeviceAction.background_on : DeviceAction.background_off;
        pAction.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, pAction);
    }

    void handleColorTemperatureCommand(PercentType ct) {
        DeviceAction ctAction = DeviceAction.colortemperature;
        ctAction.putValue(COLOR_TEMPERATURE_STEP * ct.intValue() + COLOR_TEMPERATURE_MINIMUM);
        ctAction.putDuration(getDuration());
        DeviceManager.getInstance().doAction(deviceId, ctAction);
    }

    void handleCustomCommand(String action, String params) {
        DeviceManager.getInstance().doCustomAction(deviceId, action, params);
    }

    @Override
    public void onStatusChanged(DeviceStatus status) {
        logger.debug("UpdateState->{}", status);
        updateUI(status);
    }

    void updateBrightnessAndColorUI(DeviceStatus status) {
        PercentType brightness = status.isPowerOff() ? PercentType.ZERO : new PercentType(status.getBrightness());

        HSBType tempHsbType = HSBType.fromRGB(status.getR(), status.getG(), status.getB());
        HSBType hsbType = status.getMode() == DeviceMode.MODE_HSV
                ? new HSBType(new DecimalType(status.getHue()), new PercentType(status.getSat()), brightness)
                : new HSBType(tempHsbType.getHue(), tempHsbType.getSaturation(), brightness);

        logger.debug("Update Color->{}", hsbType);
        updateState(CHANNEL_COLOR, hsbType);

        logger.debug("Update CT->{}", status.getCt());
        updateState(CHANNEL_COLOR_TEMPERATURE,
                new PercentType((status.getCt() - COLOR_TEMPERATURE_MINIMUM) / COLOR_TEMPERATURE_STEP));
    }

    int getDuration() {
        // Duration should not be null, but just in case do a null check.
        return getThing().getConfiguration().get(PARAMETER_DURATION) == null ? 500
                : ((Number) getThing().getConfiguration().get(PARAMETER_DURATION)).intValue();
    }
}
