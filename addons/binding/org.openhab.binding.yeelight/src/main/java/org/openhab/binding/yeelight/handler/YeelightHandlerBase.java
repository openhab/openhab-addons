/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yeelight.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.yeelight.YeelightBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeelight.sdk.device.ConnectState;
import com.yeelight.sdk.device.DeviceBase;
import com.yeelight.sdk.device.DeviceFactory;
import com.yeelight.sdk.device.DeviceStatus;
import com.yeelight.sdk.enums.DeviceType;
import com.yeelight.sdk.listeners.DeviceConnectionStateListener;
import com.yeelight.sdk.listeners.DeviceStatusChangeListener;
import com.yeelight.sdk.services.DeviceManager;

/**
 * The {@link YeelightHandlerBase} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Coaster Li (lixin@yeelink.net) - Initial contribution
 */
public abstract class YeelightHandlerBase extends BaseThingHandler
        implements DeviceConnectionStateListener, DeviceStatusChangeListener {

    private static final String TAG = YeelightHandlerBase.class.getSimpleName();

    protected Logger mLogger = LoggerFactory.getLogger(YeelightHandlerBase.class);
    protected DeviceBase mDevice;

    public YeelightHandlerBase(Thing thing) {
        super(thing);
    }

    protected void updateUI(DeviceStatus status) {
        thing.setLabel(status.getName().isEmpty() ? DeviceManager.getDefaultName(mDevice) : status.getName());
    }

    @Override
    public void initialize() {
        mDevice = DeviceFactory.build(getDeviceModel(getThing().getThingTypeUID()).name(), getThing().getUID().getId());
        mDevice.setDeviceName(getThing().getLabel());
        mDevice.setAutoConnect(true);
        DeviceManager.getInstance().addDevice(mDevice);
        mDevice.registerConnectStateListener(this);
        mDevice.registerStatusChangedListener(this);
        updateStatus(ThingStatus.OFFLINE);
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
        mLogger.debug(TAG + ": onConnectionStateChanged -> " + connectState.name());
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
        mLogger.debug(TAG + ": channelLinked -> " + channelUID.getId());
        super.channelLinked(channelUID);

        if (channelUID.getId().equals(YeelightBindingConstants.CHANNEL_BRIGHTNESS)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mDevice.queryStatus();
                }
            }).start();

        }
    }

    @Override
    public void onStatusChanged(String prop, DeviceStatus status) {
        mLogger.debug(TAG + ": updateState->" + status);
        updateUI(status);
    }
}
