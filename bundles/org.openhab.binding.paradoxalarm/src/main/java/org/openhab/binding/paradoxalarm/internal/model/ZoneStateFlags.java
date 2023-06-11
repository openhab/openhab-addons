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
 * The {@link ZoneStateFlags} Paradox zone state flags. Retrieved and parsed from RAM memory responses.
 *
 * @author Konstantin Polihronov - Initial contribution
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
