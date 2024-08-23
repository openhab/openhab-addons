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
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link ZoneStateFlags} Paradox zone state flags. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
public class ZoneState {
    // Regular states
    private boolean isOpened;
    private boolean isTampered;
    private boolean hasLowBattery;

    // Special flag states
    private boolean supervisionTrouble;
    private boolean inTxDelay;
    private boolean shuttedDown;
    private boolean bypassed;
    private boolean hasActivatedIntellizoneDelay;
    private boolean hasActivatedEntryDelay;
    private boolean presentlyInAlarm;
    private boolean generatedAlarm;

    public ZoneState(boolean isOpened, boolean isTampered, boolean hasLowBattery) {
        this.isOpened = isOpened;
        this.isTampered = isTampered;
        this.hasLowBattery = hasLowBattery;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean isOpened) {
        this.isOpened = isOpened;
    }

    public boolean isTampered() {
        return isTampered;
    }

    public void setTampered(boolean isTampered) {
        this.isTampered = isTampered;
    }

    public boolean hasLowBattery() {
        return hasLowBattery;
    }

    public void setHasLowBattery(boolean hasLowBattery) {
        this.hasLowBattery = hasLowBattery;
    }

    public void setSupervisionTrouble(boolean supervisionTrouble) {
        this.supervisionTrouble = supervisionTrouble;
    }

    public boolean isSupervisionTrouble() {
        return supervisionTrouble;
    }

    public boolean isInTxDelay() {
        return inTxDelay;
    }

    public void setInTxDelay(boolean inTxDelay) {
        this.inTxDelay = inTxDelay;
    }

    public boolean isShutdown() {
        return shuttedDown;
    }

    public void setShuttedDown(boolean shuttedDown) {
        this.shuttedDown = shuttedDown;
    }

    public boolean isBypassed() {
        return bypassed;
    }

    public void setBypassed(boolean bypassed) {
        this.bypassed = bypassed;
    }

    public boolean isHasActivatedIntellizoneDelay() {
        return hasActivatedIntellizoneDelay;
    }

    public void setHasActivatedIntellizoneDelay(boolean hasActivatedIntellizoneDelay) {
        this.hasActivatedIntellizoneDelay = hasActivatedIntellizoneDelay;
    }

    public boolean isHasActivatedEntryDelay() {
        return hasActivatedEntryDelay;
    }

    public void setHasActivatedEntryDelay(boolean hasActivatedEntryDelay) {
        this.hasActivatedEntryDelay = hasActivatedEntryDelay;
    }

    public boolean isPresentlyInAlarm() {
        return presentlyInAlarm;
    }

    public void setPresentlyInAlarm(boolean presentlyInAlarm) {
        this.presentlyInAlarm = presentlyInAlarm;
    }

    public boolean isGeneratedAlarm() {
        return generatedAlarm;
    }

    public void setGeneratedAlarm(boolean generatedAlarm) {
        this.generatedAlarm = generatedAlarm;
    }

    @Override
    public String toString() {
        return "ZoneState [isOpened=" + isOpened + ", isTampered=" + isTampered + ", hasLowBattery=" + hasLowBattery
                + ", supervisionTrouble=" + supervisionTrouble + ", inTxDelay=" + inTxDelay + ", shuttedDown="
                + shuttedDown + ", bypassed=" + bypassed + ", hasActivatedIntellizoneDelay="
                + hasActivatedIntellizoneDelay + ", hasActivatedEntryDelay=" + hasActivatedEntryDelay
                + ", presentlyInAlarm=" + presentlyInAlarm + ", generatedAlarm=" + generatedAlarm + "]";
    }
}
