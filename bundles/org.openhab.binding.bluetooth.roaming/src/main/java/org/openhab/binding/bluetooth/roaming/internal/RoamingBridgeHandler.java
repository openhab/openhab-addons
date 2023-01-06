/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.roaming.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothDiscoveryListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * The {@link RoamingBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class RoamingBridgeHandler extends BaseBridgeHandler implements RoamingBluetoothAdapter {

    private final Set<BluetoothAdapter> adapters = new CopyOnWriteArraySet<>();

    /*
     * Note: this will only populate from handlers calling getDevice(BluetoothAddress), so we don't need
     * to do periodic cleanup.
     */
    private Map<BluetoothAddress, RoamingBluetoothDevice> devices = new HashMap<>();
    private ThingUID[] groupUIDs = new ThingUID[0];

    public RoamingBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        Object value = getConfig().get(RoamingBindingConstants.CONFIGURATION_GROUP_ADAPTER_UIDS);
        if (value == null || !(value instanceof String) || "".equals(value)) {
            groupUIDs = new ThingUID[0];
        } else {
            String groupIds = (String) value;
            groupUIDs = Stream.of(groupIds.split(",")).map(ThingUID::new).toArray(ThingUID[]::new);
        }

        if (adapters.stream().map(BluetoothAdapter::getUID).anyMatch(this::isGroupMember)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No Physical Bluetooth adapters found");
        }
    }

    private void updateStatus() {
        if (adapters.stream().anyMatch(this::isRoamingMember)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No Physical Bluetooth adapters found");
        }
    }

    @Override
    public void dispose() {
        // nothing that needs to be done here.
        // Listener cleanup will be performed by the discovery participant anyway.
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public @Nullable String getLocation() {
        return getThing().getLocation();
    }

    @Override
    public @Nullable String getLabel() {
        return getThing().getLabel();
    }

    private boolean isRoamingMember(BluetoothAdapter adapter) {
        return isRoamingMember(adapter.getUID());
    }

    @Override
    public boolean isRoamingMember(ThingUID adapterUID) {
        if (!isInitialized()) {
            // an unitialized roaming adapter has no members
            return false;
        }
        return isGroupMember(adapterUID);
    }

    private boolean isGroupMember(ThingUID adapterUID) {
        if (groupUIDs.length == 0) {
            // if there are no members of the group then it is treated as all adapters are members.
            return true;
        }
        for (ThingUID uid : groupUIDs) {
            if (adapterUID.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDiscoveryEnabled() {
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            return false;
        }
        Object discovery = getConfig().get(BluetoothBindingConstants.CONFIGURATION_DISCOVERY);
        return !(discovery != null && discovery.toString().equalsIgnoreCase("false"));
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void addBluetoothAdapter(BluetoothAdapter adapter) {
        if (adapter == this) {
            return;
        }

        this.adapters.add(adapter);

        if (isRoamingMember(adapter)) {
            synchronized (devices) {
                for (RoamingBluetoothDevice roamingDevice : devices.values()) {
                    roamingDevice.addBluetoothDevice(adapter.getDevice(roamingDevice.getAddress()));
                }
            }
        }

        if (getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus();
        }
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void removeBluetoothAdapter(BluetoothAdapter adapter) {
        if (adapter == this) {
            return;
        }
        this.adapters.remove(adapter);

        if (isRoamingMember(adapter)) {
            synchronized (devices) {
                for (RoamingBluetoothDevice roamingDevice : devices.values()) {
                    roamingDevice.removeBluetoothDevice(adapter.getDevice(roamingDevice.getAddress()));
                }
            }
        }

        if (getThing().getStatus() == ThingStatus.ONLINE) {
            updateStatus();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void addDiscoveryListener(BluetoothDiscoveryListener listener) {
        // we don't use this
    }

    @Override
    public void removeDiscoveryListener(@Nullable BluetoothDiscoveryListener listener) {
        // we don't use this
    }

    @Override
    public void scanStart() {
        // does nothing
    }

    @Override
    public void scanStop() {
        // does nothing
    }

    @Override
    public @Nullable BluetoothAddress getAddress() {
        // roaming adapters don't have bluetooth addresses
        return null;
    }

    @Override
    public RoamingBluetoothDevice getDevice(BluetoothAddress address) {
        // this will only get called by a bluetooth device handler
        synchronized (devices) {
            RoamingBluetoothDevice roamingDevice = Objects
                    .requireNonNull(devices.computeIfAbsent(address, addr -> new RoamingBluetoothDevice(this, addr)));

            adapters.stream().filter(this::isRoamingMember)
                    .forEach(adapter -> roamingDevice.addBluetoothDevice(adapter.getDevice(address)));

            return roamingDevice;
        }
    }

    @Override
    public boolean hasHandlerForDevice(BluetoothAddress address) {
        String addrStr = address.toString();
        /*
         * This type of search is inefficient and won't scale as the number of bluetooth Thing children increases on
         * this bridge. But implementing a more efficient search would require a bit more overhead.
         * Luckily though, it is reasonable to assume that the number of Thing children will remain small.
         */
        for (Thing childThing : getThing().getThings()) {
            Object childAddr = childThing.getConfiguration().get(BluetoothBindingConstants.CONFIGURATION_ADDRESS);
            if (addrStr.equals(childAddr)) {
                return childThing.getHandler() != null;
            }
        }
        return false;
    }
}
