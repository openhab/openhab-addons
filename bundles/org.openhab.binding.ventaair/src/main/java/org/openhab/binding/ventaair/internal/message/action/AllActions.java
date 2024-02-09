/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.ventaair.internal.message.action;

/**
 * Actions send by the device, containing information about the current device settings
 *
 * @author Stefan Triller - Initial contribution
 *
 */
public class AllActions implements Action {
    private boolean Power = true;
    private int FanSpeed;
    private int TargetHum = 65;
    private int Timer;
    private boolean Boost;
    private boolean SleepMode;
    private boolean ChildLock;
    private boolean Automatic;
    private int SysLanguage; // 3?
    private int CleanLanguage; // 0?
    private int TempUnit; // 0=Celsius, 1=Fahrenheit?
    private int DisplayLeft;
    private int DisplayRight;
    private int Reset;
    private int ConINet;
    private boolean DelUser; // default false

    public boolean isPower() {
        return Power;
    }

    public int getFanSpeed() {
        return FanSpeed;
    }

    public int getTargetHum() {
        return TargetHum;
    }

    public int getTimer() {
        return Timer;
    }

    public boolean isBoost() {
        return Boost;
    }

    public boolean isSleepMode() {
        return SleepMode;
    }

    public boolean isChildLock() {
        return ChildLock;
    }

    public boolean isAutomatic() {
        return Automatic;
    }

    public int getSysLanguage() {
        return SysLanguage;
    }

    public int getCleanLanguage() {
        return CleanLanguage;
    }

    public int getTempUnit() {
        return TempUnit;
    }

    public int getDisplayLeft() {
        return DisplayLeft;
    }

    public int getDisplayRight() {
        return DisplayRight;
    }

    public int getReset() {
        return Reset;
    }

    public int getConINet() {
        return ConINet;
    }

    public boolean isDelUser() {
        return DelUser;
    }
}
