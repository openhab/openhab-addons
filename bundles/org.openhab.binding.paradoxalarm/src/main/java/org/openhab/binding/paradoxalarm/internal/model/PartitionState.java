/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link Partition} Paradox partition states. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class PartitionState {

    private static final String ARMED = "Armed";
    private static final String DISARMED = "Disarmed";
    private static final String IN_ALARM = "InAlarm";

    private static final String ARMED_IN_NO_ENTRY = "NoEntry Armed";
    private static final String ARMED_IN_STAY = "Stay Armed";
    private static final String ARMED_IN_AWAY = "Away Armed";
    private static final String FIRE_ALARM = "Fire Alarm";
    private static final String AUDIBLE_ALARM = "Audible Alarm";
    private static final String SILENT_ALARM = "Silent Alarm";
    private boolean isArmed;
    private boolean isArmedInAway;
    private boolean isArmedInStay;
    private boolean isArmedInNoEntry;
    private boolean isInAlarm;
    private boolean isInSilentAlarm;
    private boolean isInAudibleAlarm;
    private boolean isInFireAlarm;

    private boolean isReadyToArm;
    private boolean isInExitDelay;
    private boolean isInEntryDelay;
    private boolean isInTrouble;
    private boolean hasAlarmInMemory;
    private boolean isInZoneBypass;

    private boolean hasZoneInTamperTrouble;
    private boolean hasZoneInLowBatteryTrouble;
    private boolean hasZoneInFireLoopTrouble;
    private boolean hasZoneInSupervisionTrouble;

    private boolean isStayInstantReady;
    private boolean isForceReady;
    private boolean isBypassReady;
    private boolean isInhibitReady;
    private boolean areAllZoneclosed;

    public String getMainState() {
        if (isInAlarm || isInSilentAlarm || isInAudibleAlarm || isInFireAlarm) {
            return IN_ALARM;
        } else {
            return isArmed || isArmedInAway || isArmedInStay || isArmedInNoEntry ? ARMED : DISARMED;
        }
    }

    public String getDetailedState() {
        if (isInAlarm) {
            if (isInSilentAlarm) {
                return SILENT_ALARM;
            } else if (isInAudibleAlarm) {
                return AUDIBLE_ALARM;
            } else if (isInFireAlarm) {
                return FIRE_ALARM;
            }
            return IN_ALARM;
        } else if (isArmed) {
            if (isArmedInAway) {
                return ARMED_IN_AWAY;
            } else if (isArmedInStay) {
                return ARMED_IN_STAY;
            } else if (isArmedInNoEntry) {
                return ARMED_IN_NO_ENTRY;
            }
            return ARMED;
        }

        return DISARMED;
    }

    @Override
    public String toString() {
        return "PartitionState [isArmed=" + isArmed + ", isArmedInAway=" + isArmedInAway + ", isArmedInStay="
                + isArmedInStay + ", isArmedInNoEntry=" + isArmedInNoEntry + ", isInAlarm=" + isInAlarm
                + ", isInSilentAlarm=" + isInSilentAlarm + ", isInAudibleAlarm=" + isInAudibleAlarm + ", isInFireAlarm="
                + isInFireAlarm + ", isReadyToArm=" + isReadyToArm + ", isInExitDelay=" + isInExitDelay
                + ", isInEntryDelay=" + isInEntryDelay + ", isInTrouble=" + isInTrouble + ", hasAlarmInMemory="
                + hasAlarmInMemory + ", isInZoneBypass=" + isInZoneBypass + ", hasZoneInTamperTrouble="
                + hasZoneInTamperTrouble + ", hasZoneInLowBatteryTrouble=" + hasZoneInLowBatteryTrouble
                + ", hasZoneInFireLoopTrouble=" + hasZoneInFireLoopTrouble + ", hasZoneInSupervisionTrouble="
                + hasZoneInSupervisionTrouble + ", isStayInstantReady=" + isStayInstantReady + ", isForceReady="
                + isForceReady + ", isBypassReady=" + isBypassReady + ", isInhibitReady=" + isInhibitReady
                + ", areAllZoneclosed=" + areAllZoneclosed + "]";
    }

    public boolean isArmed() {
        return isArmed;
    }

    public void setArmed(boolean isArmed) {
        this.isArmed = isArmed;
    }

    public boolean isArmedInAway() {
        return isArmedInAway;
    }

    public void setArmedInAway(boolean isArmedInAway) {
        this.isArmedInAway = isArmedInAway;
    }

    public boolean isArmedInStay() {
        return isArmedInStay;
    }

    public void setArmedInStay(boolean isArmedInStay) {
        this.isArmedInStay = isArmedInStay;
    }

    public boolean isArmedInNoEntry() {
        return isArmedInNoEntry;
    }

    public void setArmedInNoEntry(boolean isArmedInNoEntry) {
        this.isArmedInNoEntry = isArmedInNoEntry;
    }

    public boolean isInAlarm() {
        return isInAlarm;
    }

    public void setInAlarm(boolean isInAlarm) {
        this.isInAlarm = isInAlarm;
    }

    public boolean isInSilentAlarm() {
        return isInSilentAlarm;
    }

    public void setInSilentAlarm(boolean isInSilentAlarm) {
        this.isInSilentAlarm = isInSilentAlarm;
    }

    public boolean isInAudibleAlarm() {
        return isInAudibleAlarm;
    }

    public void setInAudibleAlarm(boolean isInAudibleAlarm) {
        this.isInAudibleAlarm = isInAudibleAlarm;
    }

    public boolean isInFireAlarm() {
        return isInFireAlarm;
    }

    public void setInFireAlarm(boolean isInFireAlarm) {
        this.isInFireAlarm = isInFireAlarm;
    }

    public boolean isReadyToArm() {
        return isReadyToArm;
    }

    public void setReadyToArm(boolean isReadyToArm) {
        this.isReadyToArm = isReadyToArm;
    }

    public boolean isInExitDelay() {
        return isInExitDelay;
    }

    public void setInExitDelay(boolean isInExitDelay) {
        this.isInExitDelay = isInExitDelay;
    }

    public boolean isInEntryDelay() {
        return isInEntryDelay;
    }

    public void setInEntryDelay(boolean isInEntryDelay) {
        this.isInEntryDelay = isInEntryDelay;
    }

    public boolean isInTrouble() {
        return isInTrouble;
    }

    public void setInTrouble(boolean isInTrouble) {
        this.isInTrouble = isInTrouble;
    }

    public boolean isHasAarmInMemory() {
        return hasAlarmInMemory;
    }

    public void setHasAarmInMemory(boolean hasAarmInMemory) {
        this.hasAlarmInMemory = hasAarmInMemory;
    }

    public boolean isInZoneBypass() {
        return isInZoneBypass;
    }

    public void setInZoneBypass(boolean isInZoneBypass) {
        this.isInZoneBypass = isInZoneBypass;
    }

    public boolean isHasZoneInTamperTrouble() {
        return hasZoneInTamperTrouble;
    }

    public void setHasZoneInTamperTrouble(boolean hasZoneInTamperTrouble) {
        this.hasZoneInTamperTrouble = hasZoneInTamperTrouble;
    }

    public boolean isHasZoneInLowBatteryTrouble() {
        return hasZoneInLowBatteryTrouble;
    }

    public void setHasZoneInLowBatteryTrouble(boolean hasZoneInLowBatteryTrouble) {
        this.hasZoneInLowBatteryTrouble = hasZoneInLowBatteryTrouble;
    }

    public boolean isHasZoneInFireLoopTrouble() {
        return hasZoneInFireLoopTrouble;
    }

    public void setHasZoneInFireLoopTrouble(boolean hasZoneInFireLoopTrouble) {
        this.hasZoneInFireLoopTrouble = hasZoneInFireLoopTrouble;
    }

    public boolean isHasZoneInSupervisionTrouble() {
        return hasZoneInSupervisionTrouble;
    }

    public void setHasZoneInSupervisionTrouble(boolean hasZoneInSupervisionTrouble) {
        this.hasZoneInSupervisionTrouble = hasZoneInSupervisionTrouble;
    }

    public boolean isStayInstantReady() {
        return isStayInstantReady;
    }

    public void setStayInstantReady(boolean isStayInstantReady) {
        this.isStayInstantReady = isStayInstantReady;
    }

    public boolean isForceReady() {
        return isForceReady;
    }

    public void setForceReady(boolean isForceReady) {
        this.isForceReady = isForceReady;
    }

    public boolean isBypassReady() {
        return isBypassReady;
    }

    public void setBypassReady(boolean isBypassReady) {
        this.isBypassReady = isBypassReady;
    }

    public boolean isInhibitReady() {
        return isInhibitReady;
    }

    public void setInhibitReady(boolean isInhibitReady) {
        this.isInhibitReady = isInhibitReady;
    }

    public boolean isAreAllZoneclosed() {
        return areAllZoneclosed;
    }

    public void setAllZoneClosed(boolean areAllZoneclosed) {
        this.areAllZoneclosed = areAllZoneclosed;
    }

    public boolean isHasAlarmInMemory() {
        return hasAlarmInMemory;
    }

    public void setHasAlarmInMemory(boolean hasAlarmInMemory) {
        this.hasAlarmInMemory = hasAlarmInMemory;
    }
}
