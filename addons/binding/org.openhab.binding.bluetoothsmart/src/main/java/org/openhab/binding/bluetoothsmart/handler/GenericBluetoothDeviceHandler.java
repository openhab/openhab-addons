/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bluetoothsmart.handler;

import java.util.Arrays;
import java.util.Date;

import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.openhab.binding.bluetoothsmart.BluetoothSmartBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sputnikdev.bluetooth.gattparser.BluetoothGattParser;
import org.sputnikdev.bluetooth.manager.BluetoothManager;
import org.sputnikdev.bluetooth.manager.DeviceGovernor;
import org.sputnikdev.bluetooth.manager.GenericBluetoothDeviceListener;
import org.sputnikdev.bluetooth.manager.GovernorListener;

/**
 *
 * 
 * @author Vlad Kolotov - Initial contribution
 */
public class GenericBluetoothDeviceHandler extends BluetoothSmartHandler<DeviceGovernor>
        implements GenericBluetoothDeviceListener, GovernorListener {

    private Logger logger = LoggerFactory.getLogger(GenericBluetoothDeviceHandler.class);
    private int initialOnlineTimeout;

    private final SingleChannelHandler<Boolean, OnOffType> readyHandler = new BooleanTypeChannelHandler(
            GenericBluetoothDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_READY) {
        @Override Boolean getValue() {
            return getGovernor().isReady();
        }
    };

    private final DateTimeChannelHandler lastChangedHandler =
            new DateTimeChannelHandler(this, BluetoothSmartBindingConstants.CHANNEL_LAST_UPDATED);

    private final IntegerTypeChannelHandler rssiHandler = new IntegerTypeChannelHandler (
            GenericBluetoothDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_RSSI) {
        @Override Integer getValue() {
            return (int) getGovernor().getRSSI();
        }
    };

    private final BooleanTypeChannelHandler onlineHandler = new BooleanTypeChannelHandler (
            GenericBluetoothDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_ONLINE) {
        @Override Boolean getValue() {
            return getGovernor().isOnline();
        }
    };

    private final BooleanTypeChannelHandler blockedHandler = new BooleanTypeChannelHandler (
            GenericBluetoothDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_BLOCKED) {
        @Override Boolean getValue() {
            return getGovernor().isBlocked();
        }
    };

    private final SingleChannelHandler<Boolean, OnOffType> blockedControlHandler = new BooleanTypeChannelHandler(
            GenericBluetoothDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_BLOCKED_CONTROL, true) {
        @Override Boolean getValue() {
            return getGovernor().getBlockedControl();
        }
        @Override void updateThing(Boolean value) {
            getGovernor().setBlockedControl(value);
        }
    };

    private final IntegerTypeChannelHandler onlineTimeoutHandler = new IntegerTypeChannelHandler (
            GenericBluetoothDeviceHandler.this, BluetoothSmartBindingConstants.CHANNEL_ONLINE_TIMEOUT, true) {
        @Override Integer getValue() {
            return getGovernor().getOnlineTimeout();
        }
        @Override void updateThing(Integer value) {
            getGovernor().setOnlineTimeout(value);
        }

        @Override Integer getDefaultValue() {
            return GenericBluetoothDeviceHandler.this.initialOnlineTimeout;
        }
    };

    public GenericBluetoothDeviceHandler(Thing thing, ItemRegistry itemRegistry,
            BluetoothManager bluetoothManager, BluetoothGattParser parser) {
        super(thing, itemRegistry, bluetoothManager, parser);
        addChannelHandlers(Arrays.asList(readyHandler, lastChangedHandler, rssiHandler, onlineHandler,
                blockedHandler, blockedControlHandler, onlineTimeoutHandler));
    }

    @Override
    public void initialize() {
        super.initialize();
        getGovernor().addGenericBluetoothDeviceListener(this);
        getGovernor().addGovernorListener(this);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void online() {
        onlineHandler.updateChannel(true);
    }

    @Override
    public void offline() {
        onlineHandler.updateChannel(false);
    }

    @Override
    public void blocked(boolean blocked) {
        blockedHandler.updateChannel(blocked);
    }

    @Override
    public void rssiChanged(short rssi) {
        rssiHandler.updateChannel((int) rssi);
    }

    @Override
    public void ready(boolean ready) {
        readyHandler.updateChannel(ready);
    }

    @Override
    public void lastUpdatedChanged(Date lastActivity) {
        lastChangedHandler.updateChannel(lastActivity);
    }

    public int getInitialOnlineTimeout() {
        return initialOnlineTimeout;
    }

    public void setInitialOnlineTimeout(int initialOnlineTimeout) {
        this.initialOnlineTimeout = initialOnlineTimeout;
    }
}
