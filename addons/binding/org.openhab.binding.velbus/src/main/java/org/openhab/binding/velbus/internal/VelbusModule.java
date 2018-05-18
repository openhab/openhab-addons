/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.velbus.internal;

import static org.openhab.binding.velbus.VelbusBindingConstants.*;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.velbus.handler.VelbusBridgeHandler;
import org.openhab.binding.velbus.internal.packets.VelbusChannelNameRequestPacket;

/**
 * The {@link VelbusModule} represents a generic module and its basic properties
 * in the Velbus system.
 *
 * @author Cedric Boon - Initial contribution
 */
public class VelbusModule {
    private VelbusModuleAddress velbusModuleAddress;
    private byte highByteOfSerialNumber;
    private byte lowByteOfSerialNumber;
    private byte memoryMapVersion;
    private byte buildYear;
    private byte buildWeek;

    private ThingTypeUID thingTypeUID;
    private String[][] channelNames;

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
        this.channelNames = new String[numberOfChannels][3];
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

    public ThingUID getThingUID(ThingUID bridgeUID) {
        return new ThingUID(getThingTypeUID(), bridgeUID, getAddress());
    }

    public String getLabel() {
        return getThingTypeUID() + " (Address " + getAddress() + ")";
    }

    protected String getChannelName(int channelIndex) {
        String channelName = "";

        for (int i = 0; i < 3; i++) {
            String channelNamePart = channelNames[channelIndex - 1][i];
            if (channelNamePart != null) {
                channelName = channelName + channelNamePart;
            }
        }

        return channelName.length() > 0 ? channelName : null;
    }

    public void sendChannelNameRequests(VelbusBridgeHandler bridgeHandler) {
        VelbusChannelNameRequestPacket channelNameRequest = new VelbusChannelNameRequestPacket(
                velbusModuleAddress.getAddress());
        bridgeHandler.sendPacket(channelNameRequest.getBytes());
    }

    public void setChannelName(VelbusChannelIdentifier channelIdentifier, int namePartNumber, byte[] namePart) {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < namePart.length; i++) {
            byte currentChar = namePart[i];
            if (currentChar != (byte) 0xFF) {
                contents.append((char) currentChar);
            }
        }

        if (channelNames.length <= 8) {
            channelNames[channelIdentifier.getChannelNumberFromBitNumber() - 1][namePartNumber - 1] = contents
                    .toString();
        } else {
            channelNames[channelIdentifier.getChannelByte() - 1][namePartNumber - 1] = contents.toString();
        }
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new TreeMap<String, Object>();

        properties.put(MODULE_ADDRESS, getAddress());
        properties.put(MODULE_SERIAL_NUMBER, getModuleSerialNumber());
        properties.put(MODULE_MEMORY_MAP_VERSION, getMemoryMapVersion());
        properties.put(MODULE_BUILD, getModuleBuild());

        for (int i = 1; i <= channelNames.length; i++) {
            String channelName = getChannelName(i);
            if (channelName != null) {
                properties.put(CHANNEL + i, channelName);
            }
        }

        byte[] subAddresses = velbusModuleAddress.getSubAddresses();
        for (int i = 1; i <= subAddresses.length; i++) {
            properties.put(SUB_ADDRESS + i, String.format("%02X", subAddresses[i - 1]));
        }

        return properties;
    }
}
