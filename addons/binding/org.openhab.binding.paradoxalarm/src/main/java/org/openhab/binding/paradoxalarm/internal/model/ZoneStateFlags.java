/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal.model;

/**
 * The {@link ZoneStateFlags} Paradox zone state flags. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ZoneStateFlags {
    private byte[] zonesOpened;
    private byte[] zonesTampered;
    private byte[] zonesLowBattery;

    public byte[] getZonesOpened() {
        return zonesOpened;
    }

    public void setZonesOpened(byte[] zonesOpened) {
        this.zonesOpened = zonesOpened;
    }

    public byte[] getZonesTampered() {
        return zonesTampered;
    }

    public void setZonesTampered(byte[] zonesTampered) {
        this.zonesTampered = zonesTampered;
    }

    public byte[] getZonesLowBattery() {
        return zonesLowBattery;
    }

    public void setZonesLowBattery(byte[] zonesLowBattery) {
        this.zonesLowBattery = zonesLowBattery;
    }

}
