/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

import org.openhab.binding.paradoxalarm.internal.util.ParadoxUtil;

/**
 * The {@link Partition} Paradox partition states. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class PartitionState {

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

    public void updateStates(byte[] partitionFlags) {
        byte firstByte = partitionFlags[0];
        this.isArmed = ParadoxUtil.isBitSet(firstByte, 0) ? true : false;
        this.isArmedInAway = ParadoxUtil.isBitSet(firstByte, 1) ? true : false;
        this.isArmedInStay = ParadoxUtil.isBitSet(firstByte, 2) ? true : false;
        this.isArmedInNoEntry = ParadoxUtil.isBitSet(firstByte, 3) ? true : false;

        this.isInAlarm = ParadoxUtil.isBitSet(firstByte, 4) ? true : false;
        this.isInSilentAlarm = ParadoxUtil.isBitSet(firstByte, 5) ? true : false;
        this.isInAudibleAlarm = ParadoxUtil.isBitSet(firstByte, 6) ? true : false;
        this.isInFireAlarm = ParadoxUtil.isBitSet(firstByte, 7) ? true : false;

        byte secondByte = partitionFlags[1];
        this.isReadyToArm = ParadoxUtil.isBitSet(secondByte, 0) ? true : false;
        this.isInExitDelay = ParadoxUtil.isBitSet(secondByte, 1) ? true : false;
        this.isInEntryDelay = ParadoxUtil.isBitSet(secondByte, 2) ? true : false;
        this.isInTrouble = ParadoxUtil.isBitSet(secondByte, 3) ? true : false;
        this.hasAlarmInMemory = ParadoxUtil.isBitSet(secondByte, 4) ? true : false;
        this.isInZoneBypass = ParadoxUtil.isBitSet(secondByte, 5) ? true : false;

        byte thirdByte = partitionFlags[2];
        this.hasZoneInTamperTrouble = ParadoxUtil.isBitSet(thirdByte, 4) ? true : false;
        this.hasZoneInLowBatteryTrouble = ParadoxUtil.isBitSet(thirdByte, 5) ? true : false;
        this.hasZoneInFireLoopTrouble = ParadoxUtil.isBitSet(thirdByte, 6) ? true : false;
        this.hasZoneInSupervisionTrouble = ParadoxUtil.isBitSet(thirdByte, 7) ? true : false;

        byte sixthByte = partitionFlags[5];
        this.isStayInstantReady = ParadoxUtil.isBitSet(sixthByte, 0) ? true : false;
        this.isForceReady = ParadoxUtil.isBitSet(sixthByte, 1) ? true : false;
        this.isBypassReady = ParadoxUtil.isBitSet(sixthByte, 2) ? true : false;
        this.isInhibitReady = ParadoxUtil.isBitSet(sixthByte, 3) ? true : false;
        this.areAllZoneclosed = ParadoxUtil.isBitSet(sixthByte, 4) ? true : false;
    }

    public String calculatedState() {
        String state = isArmed | isArmedInAway | isArmedInNoEntry | isArmedInStay ? "Armed" : "Disarmed";
        // TODO check if isInAlarm also includes the other three if yes -> check only the other three
        if (isInAlarm) {
            state += "\tIn alarm";
        } else if (isInSilentAlarm) {
            state += "\tIn Silent alarm";
        } else if (isInAudibleAlarm) {
            state += "\tIn Audible alarm";
        } else if (isInFireAlarm) {
            state += "\tIn Fire alarm";
        }

        if (areAllZoneclosed) {
            state += "\tAll zones closed";
        }
        return state;
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

    public void setAreAllZoneclosed(boolean areAllZoneclosed) {
        this.areAllZoneclosed = areAllZoneclosed;
    }
}
