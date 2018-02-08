/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
public class OpenWebNetDevice {
    private final Integer macAddress;
    private boolean notified;
    HashMap<Integer, OpenWebNetChannel> channels;
    String firmwareVersion;
    String hardwareVersion;

    public OpenWebNetDevice(int macAddress) {
        this.macAddress = new Integer(macAddress);
        channels = new HashMap<Integer, OpenWebNetChannel>();
        notified = false;
        firmwareVersion = "Unknown";
        hardwareVersion = "Unknown";
    }

    public void setFirmwareVersion(String version) {
        firmwareVersion = version;
    }

    public void setHardwareVersion(String version) {
        hardwareVersion = version;
    }

    public boolean isNotified() {
        return notified;
    }

    public void notified() {
        notified = true;
    }

    public Integer getMacAddress() {
        return macAddress;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void addChannel(int number, int type) {
        if (!channels.containsKey(number)) {
            OpenWebNetChannel channel = new OpenWebNetChannel(number, type);
            channels.put(channel.getId(), channel);
        }
    }

    public Map<Integer, OpenWebNetChannel> getChannels() {
        return channels;
    }

    public OpenWebNetChannel getChannel(int number) {
        return this.channels.get(number);
    }

    @Override
    public @Nullable String toString() {
        StringBuilder text = new StringBuilder();
        text.append("Thing [mac = " + this.macAddress + ", HW = " + this.hardwareVersion + ", FW = "
                + this.firmwareVersion);

        channels.forEach((pos, channel) -> {
            text.append(", [channel " + channel.toString() + "] ");
        });
        return text.toString();
    }

    public boolean hasChannel(int number) {
        return channels.containsKey(number);
    }

}
