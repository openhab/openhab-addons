/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.yeelight.YeelightBindingConstants;

import com.yeelight.sdk.device.ConnectState;
import com.yeelight.sdk.device.DeviceStatus;
import com.yeelight.sdk.enums.DeviceAction;
import com.yeelight.sdk.enums.DeviceMode;
import com.yeelight.sdk.services.DeviceManager;

/**
 * The {@link YeelightStripeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li (lixin@yeelink.net) - Initial contribution
 */
public class YeelightStripeHandler extends YeelightHandlerBase {

    private static final String TAG = YeelightStripeHandler.class.getSimpleName();

    public YeelightStripeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        mLogger.info(TAG + ": command: " + command);

        // if device is disconnect, start discover to reconnect.
        if (mDevice.isAutoConnect() && mDevice.getConnectionState() != ConnectState.CONNECTED) {
            DeviceManager.getInstance().startDiscovery(5 * 1000);
            return;
        }

        switch (channelUID.getId()) {
            case YeelightBindingConstants.CHANNEL_BRIGHTNESS:

                if (command instanceof PercentType) {
                    PercentType brightness = (PercentType) command;
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
                } else if (command instanceof OnOffType) {
                    OnOffType onoff = (OnOffType) command;
                    DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(),
                            onoff == OnOffType.ON ? DeviceAction.open : DeviceAction.close);
                } else if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType increaseDecrease = (IncreaseDecreaseType) command;
                    DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(),
                            increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_bright
                                    : DeviceAction.decrease_bright);
                }
                break;

            case YeelightBindingConstants.CHANNEL_COLOR:
                if (command instanceof HSBType) {
                    HSBType color = (HSBType) command;
                    DeviceAction caction = DeviceAction.color;
                    caction.putValue(color.getRGB() & 0xFFFFFF);
                    DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), caction);
                }
                break;
            case YeelightBindingConstants.CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof PercentType) {
                    PercentType ct = (PercentType) command;
                    DeviceAction ctaction = DeviceAction.colortemperature;
                    ctaction.putValue(48 * ct.intValue() + 1700);
                    DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(), ctaction);
                } else if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType increaseDecrease = (IncreaseDecreaseType) command;
                    DeviceManager.getInstance().doAction(channelUID.getThingUID().getId(),
                            increaseDecrease == IncreaseDecreaseType.INCREASE ? DeviceAction.increase_ct
                                    : DeviceAction.decrease_ct);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void initialize() {
        mLogger.info(TAG + ": initialize");
        super.initialize();
    }

    @Override
    protected void updateUI(DeviceStatus status) {
        super.updateUI(status);
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
                    new PercentType((status.getCt() - 1700) / 48));
        }

    }
}
