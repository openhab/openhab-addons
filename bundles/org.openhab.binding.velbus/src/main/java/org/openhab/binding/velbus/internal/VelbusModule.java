/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.velbus.internal;

import static org.openhab.binding.velbus.internal.VelbusBindingConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velbus.internal.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.internal.packets.VelbusChannelNameRequestPacket;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link VelbusModule} represents a generic module and its basic properties
 * in the Velbus system.
 *
 * @author Cedric Boon - Initial contribution
 */
@NonNullByDefault
public class VelbusModule {
    private final HashMap<Integer, String[]> channelNames = new HashMap<>();

    private static final Set<ThingTypeUID> CHANNEL_NAME_IDX_FROM_BYTE_THING_TYPES = new HashSet<>(Arrays.asList(
            THING_TYPE_VMB4AN, THING_TYPE_VMB4LEDPWM_20, THING_TYPE_VMB4PB, THING_TYPE_VMB6PB_20, THING_TYPE_VMB8DC_20,
            THING_TYPE_VMB8IN_20, THING_TYPE_VMBEL1, THING_TYPE_VMBEL1_20, THING_TYPE_VMBEL2, THING_TYPE_VMBEL2_20,
            THING_TYPE_VMBEL4, THING_TYPE_VMBEL4_20, THING_TYPE_VMBEL4PIR_20, THING_TYPE_VMBELO, THING_TYPE_VMBELO_20,
            THING_TYPE_VMBELPIR, THING_TYPE_VMBGP1, THING_TYPE_VMBGP1_2, THING_TYPE_VMBGP1_20, THING_TYPE_VMBGP2,
            THING_TYPE_VMBGP2_2, THING_TYPE_VMBGP2_20, THING_TYPE_VMBGP4, THING_TYPE_VMBGP4_2, THING_TYPE_VMBGP4_20,
            THING_TYPE_VMBGP4PIR, THING_TYPE_VMBGP4PIR_2, THING_TYPE_VMBGP4PIR_20, THING_TYPE_VMBGPO,
            THING_TYPE_VMBGPOD, THING_TYPE_VMBGPOD_2, THING_TYPE_VMBGPO_20, THING_TYPE_VMBIN, THING_TYPE_VMBKP));

    private static final Set<ThingTypeUID> PIR_WITH_SENSOR_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_VMBPIRO, THING_TYPE_VMBPIRO_10));

    private VelbusModuleAddress velbusModuleAddress;
    private byte highByteOfSerialNumber;
    private byte lowByteOfSerialNumber;
    private byte memoryMapVersion;
    private byte buildYear;
    private byte buildWeek;
    private int numberOfChannels;

    private ThingTypeUID thingTypeUID;

    public VelbusModule(VelbusModuleAddress velbusModuleAddress, byte moduleType, byte highByteOfSerialNumber,
            byte lowByteOfSerialNumber, byte memoryMapVersion, byte buildYear, byte buildWeek,
            ThingTypeUID thingTypeUID, int numberOfChannels) {
        this.velbusModuleAddress = velbusModuleAddress;
        this.highByteOfSerialNumber = highByteOfSerialNumber;
        this.lowByteOfSerialNumber = lowByteOfSerialNumber;
        this.memoryMapVersion = memoryMapVersion;
        this.buildYear = buildYear;
        this.buildWeek = buildWeek;
        this.thingTypeUID = thingTypeUID;
        this.numberOfChannels = numberOfChannels;
    }

    public VelbusModuleAddress getModuleAddress() {
        return velbusModuleAddress;
    }

    public String getAddress() {
        return String.format("%02X", velbusModuleAddress.getAddress());
    }

    public String getModuleSerialNumber() {
        return String.format("%02X", highByteOfSerialNumber) + String.format("%02X", lowByteOfSerialNumber);
    }

    public String getMemoryMapVersion() {
        return String.format("%02X", memoryMapVersion);
    }

    public String getModuleBuild() {
        return String.format("%02X", buildYear) + String.format("%02X", buildWeek);
    }

    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public ThingUID getThingUID(ThingUID bridgeUID) {
        return new ThingUID(getThingTypeUID(), bridgeUID, getAddress());
    }

    public String getLabel() {
        return getThingTypeUID() + " (Address " + getAddress() + ")";
    }

    protected String getChannelName(int channelIndex) {
        String channelName = "";

        Integer key = channelIndex;
        if (channelNames.containsKey(key)) {
            for (int i = 0; i < 3; i++) {
                String channelNamePart = channelNames.get(key)[i];
                if (channelNamePart != null) {
                    channelName = channelName + channelNamePart;
                }
            }
        }

        return channelName;
    }

    public void sendChannelNameRequests(@Nullable VelbusBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            VelbusChannelNameRequestPacket channelNameRequest = new VelbusChannelNameRequestPacket(
                    velbusModuleAddress.getAddress());
            bridgeHandler.sendPacket(channelNameRequest.getBytes());
        }
    }

    public void setChannelName(VelbusChannelIdentifier channelIdentifier, int namePartNumber, byte[] namePart) {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < namePart.length; i++) {
            byte currentChar = namePart[i];
            if (currentChar != (byte) 0xFF) {
                contents.append((char) currentChar);
            }
        }

        Integer key = CHANNEL_NAME_IDX_FROM_BYTE_THING_TYPES.contains(thingTypeUID)
                ? channelIdentifier.getChannelByte() - 1
                : velbusModuleAddress.getChannelIndex(channelIdentifier);
        if (key == 0 && PIR_WITH_SENSOR_THING_TYPES.contains(thingTypeUID)) {
            key = key + 8;
        }

        if (!channelNames.containsKey(key)) {
            channelNames.put(key, new String[3]);
        }

        channelNames.get(key)[namePartNumber - 1] = contents.toString();
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new TreeMap<>();

        properties.put(ADDRESS, getAddress());
        properties.put(MODULE_SERIAL_NUMBER, getModuleSerialNumber());
        properties.put(MODULE_MEMORY_MAP_VERSION, getMemoryMapVersion());
        properties.put(MODULE_BUILD, getModuleBuild());

        Integer[] keys = channelNames.keySet().toArray(new Integer[0]);
        Arrays.sort(keys);

        for (Integer key : keys) {
            String channelName = getChannelName(key);
            if (channelName.length() > 0) {
                properties.put(CHANNEL + (key + 1), channelName);
            }
        }

        byte[] subAddresses = velbusModuleAddress.getSubAddresses();
        for (int i = 1; i <= subAddresses.length; i++) {
            properties.put(SUB_ADDRESS + i, String.format("%02X", subAddresses[i - 1]));
        }

        return properties;
    }
}
