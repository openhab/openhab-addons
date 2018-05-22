/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.handler;

import static org.openhab.binding.yeelight.YeelightBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yeelight.YeelightBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeelight.sdk.device.ConnectState;
import com.yeelight.sdk.device.DeviceBase;
import com.yeelight.sdk.device.DeviceFactory;
import com.yeelight.sdk.device.DeviceStatus;
import com.yeelight.sdk.enums.DeviceAction;
import com.yeelight.sdk.enums.DeviceMode;
import com.yeelight.sdk.enums.DeviceType;
import com.yeelight.sdk.listeners.DeviceConnectionStateListener;
import com.yeelight.sdk.listeners.DeviceStatusChangeListener;
import com.yeelight.sdk.services.DeviceManager;

/**
 * The {@link YeelightHandlerBase} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li - Initial contribution
 */
public abstract class YeelightHandlerBase extends BaseThingHandler
        implements DeviceConnectionStateListener, DeviceStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(YeelightHandlerBase.class);
    protected DeviceBase mDevice;

    public YeelightHandlerBase(Thing thing) {
        super(thing);
    }

    protected void updateUI(DeviceStatus status) {
    }

    @Override
    public void initialize() {
        mDevice = DeviceFactory.build(getDeviceModel(getThing().getThingTypeUID()).name(), getThing().getUID().getId());
        mDevice.setDeviceName(getThing().getLabel());
        mDevice.setAutoConnect(true);
        DeviceManager.getInstance().addDevice(mDevice);
        mDevice.registerConnectStateListener(this);
        mDevice.registerStatusChangedListener(this);
        updateStatus(ThingStatus.UNKNOWN);
        DeviceManager.getInstance().startDiscovery(15 * 1000);
    }

    private DeviceType getDeviceModel(ThingTypeUID typeUID) {
        if (typeUID.equals(YeelightBindingConstants.THING_TYPE_CEILING)) {
            return DeviceType.ceiling;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_WONDER)) {
            return DeviceType.color;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_DOLPHIN)) {
            return DeviceType.mono;
        } else if (typeUID.equals(YeelightBindingConstants.THING_TYPE_STRIPE)) {
            return DeviceType.stripe;
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionStateChanged(ConnectState connectState) {
        logger.debug("onConnectionStateChanged -> {}", connectState.name());
        switch (connectState) {
            case DISCONNECTED:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Device is offline !");
                if (mDevice.isAutoConnect()) {
                    DeviceManager.sInstance.startDiscovery(5 * 1000);
                }
                break;

            case CONNECTED:
                updateStatus(ThingStatus.ONLINE);
                mDevice.queryStatus();
                break;

            default:
                break;
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("ChannelLinked -> {}", channelUID.getId());
        super.channelLinked(channelUID);

        if (channelUID.getId().equals(YeelightBindingConstants.CHANNEL_BRIGHTNESS)) {

            Runnable task = () -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.debug("Sleep interrupted setting channel brightness", e);
                }
                mDevice.queryStatus();
            };

            new Thread(task).start();
        }
    }

    public void handleCommandHelper(ChannelUID channelUID, Command command, String logInfo) {
        logger.debug(logInfo, command);

        // If device is disconnected, start discover to reconnect.
        if (mDevice.isAutoConnect() && mDevice.getConnectionState() != ConnectState.CONNECTED) {
            DeviceManager.getInstance().startDiscovery(5 * 1000);
            return;
        }

        if (command instanceof RefreshType){
            DeviceManager.getInstance().startDiscovery(15 * 1000);
        }

        switch (channelUID.getId()) {
            case YeelightBindingConstants.CHANNEL_BRIGHTNESS:
                handleBrightnessChannelCommand(channelUID, command);
                break;
            case YeelightBindingConstants.CHANNEL_COLOR:
                handleColorChannelCommand(channelUID, command);
                break;
            case YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE:
                handleColorTemperatureChannelCommand(channelUID, command);
                break;
            default:
                break;
        }
    }

    /**
     * Channel Commands
     */

    void handleBrightnessChannelCommand(ChannelUID channelUID, Command command) {
        if (command instanceof PercentType) {
            handlePercentMessage(channelUID, (PercentType) command);
        } else if (command instanceof OnOffType) {
            handleOnOffCommand(channelUID, (OnOffType) command);
        } else if (command instanceof IncreaseDecreaseType) {
            handleIncreaseDecreaseBrightnessCommand(channelUID, (IncreaseDecreaseType) command);
        }
    }

    void handleColorChannelCommand(ChannelUID channelUID, Command command) {
        if (command instanceof HSBType) {
            handleHSBCommand(channelUID, (HSBType) command);
        }
    }

    void handleColorTemperatureChannelCommand(ChannelUID channelUID, Command command) {
        if (command instanceof PercentType) {
            handleColorTemperatureCommand(channelUID, (PercentType) command);
        } else if (command instanceof IncreaseDecreaseType) {
            handleIncreaseDecreaseBrightnessCommand(channelUID, (IncreaseDecreaseType) command);
        }
    }

    /**
     * Individual Messages
     */

    void handleIncreaseDecreaseBrightnessCommand(ChannelUID channelUID, IncreaseDecreaseType increaseDecrease) {
        DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(),
                increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_bright
                        : DeviceAction.decrease_bright);
    }

    void handleIncreaseDecreaseColorTemperatureCommand(ChannelUID channelUID, IncreaseDecreaseType increaseDecrease) {
        DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(),
                increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_ct
                        : DeviceAction.decrease_ct);
    }

    void handleOnOffCommand(ChannelUID channelUID, OnOffType onoff) {
        DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(),
                onoff == OnOffType.ON ? DeviceAction.open : DeviceAction.close);
    }

    void handleHSBCommand(ChannelUID channelUID, HSBType color) {
        DeviceAction caction = DeviceAction.color;
        caction.putValue(color.getRGB() & 0xFFFFFF);
        DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), caction);
    }

    void handleColorTemperatureCommand(ChannelUID channelUID, PercentType ct) {
        DeviceAction ctaction = DeviceAction.colortemperature;
        ctaction.putValue(COLOR_TEMPERATURE_STEP * ct.intValue() + COLOR_TEMPERATURE_MINIMUM);
        DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), ctaction);
    }

    void handlePercentMessage(ChannelUID channelUID, PercentType brightness) {
        if (brightness.intValue() == 0) {
            DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), DeviceAction.close);
        } else {
            if (mDevice.getDeviceStatus().isPowerOff()) {
                DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), DeviceAction.open);
            }
            DeviceAction baction = DeviceAction.brightness;
            baction.putValue(brightness.intValue());
            DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), baction);
        }
    }

    @Override
    public void onStatusChanged(String prop, DeviceStatus status) {
        logger.debug("UpdateState->{}", status);
        updateUI(status);
    }

    void updateBrightnessAndColorUI(DeviceStatus status) {
        if (status.isPowerOff()) {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(0));
        } else {
            updateState(YeelightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(status.getBrightness()));
            HSBType hsbType = null;
            if (status.getMode() == DeviceMode.MODE_COLOR) {
                hsbType = HSBType.fromRGB(status.getR(), status.getG(), status.getB());
            } else if (status.getMode() == DeviceMode.MODE_HSV) {
                hsbType = new HSBType(new DecimalType(status.getHue()), new PercentType(status.getSat()),
                        new PercentType(1));
            }
            if (hsbType != null) {
                updateState(YeelightBindingConstants.CHANNEL_COLOR, hsbType);
            }
            updateState(YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE,
                    new PercentType((status.getCt() - COLOR_TEMPERATURE_MINIMUM) / COLOR_TEMPERATURE_STEP));
        }
    }
}
