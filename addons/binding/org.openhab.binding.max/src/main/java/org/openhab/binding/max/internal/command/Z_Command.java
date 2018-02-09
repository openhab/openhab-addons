/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.command;

import org.openhab.binding.max.internal.Utils;

/**
 * The {@link Z_Command} send a wakeup request to MAX! devices.
 *
 * @author Marcel Verpaalen - Initial Contribution
 * @since 2.0
 */

public class Z_Command extends CubeCommand {

    public enum WakeUpType {
        ALL,
        ROOM,
        DEVICE
    }

    private static final int DEFAULT_WAKETIME = 30;
    private String Address;
    private WakeUpType wakeUpType;
    private int wakeUpTime;

    public Z_Command(WakeUpType wakeUpType, String address, int wakeupTime) {
        this.Address = address;
        this.wakeUpType = wakeUpType;
        this.wakeUpTime = wakeupTime;
    }

    public static Z_Command wakeupRoom(int roomId) {
        return new Z_Command(WakeUpType.ROOM, String.format("%02d", roomId), DEFAULT_WAKETIME);
    }

    public static Z_Command wakeupRoom(int roomId, int wakeupTime) {
        return new Z_Command(WakeUpType.ROOM, String.format("%02d", roomId), wakeupTime);
    }

    public static Z_Command wakeupDevice(String rfAddress) {
        return new Z_Command(WakeUpType.DEVICE, rfAddress, DEFAULT_WAKETIME);
    }

    public static Z_Command wakeupDevice(String rfAddress, int wakeupTime) {
        return new Z_Command(WakeUpType.DEVICE, rfAddress, wakeupTime);
    }

    public static Z_Command wakeupAllDevices() {
        return new Z_Command(WakeUpType.ALL, "0", DEFAULT_WAKETIME);
    }

    public static Z_Command wakeupAllDevices(int wakeupTime) {
        return new Z_Command(WakeUpType.ALL, "0", wakeupTime);
    }

    @Override
    public String getCommandString() {

        String commandString = "";
        if (wakeUpType.equals(WakeUpType.ALL)) {
            commandString = "A";
        }
        if (wakeUpType.equals(WakeUpType.ROOM)) {
            commandString = "G," + Address;
        }
        if (wakeUpType.equals(WakeUpType.DEVICE)) {
            commandString = "D," + Address;
        }

        String cmd = "z:" + Utils.toHex(wakeUpTime) + "," + commandString + '\r' + '\n';
        return cmd;
    }

    @Override
    public String getReturnStrings() {
        return "A:";
    }

}
