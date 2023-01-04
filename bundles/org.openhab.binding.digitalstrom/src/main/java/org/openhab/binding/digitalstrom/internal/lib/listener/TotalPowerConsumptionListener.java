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
package org.openhab.binding.digitalstrom.internal.lib.listener;

/**
 * The {@link TotalPowerConsumptionListener} is notified, if the total power consumption or the total electric meter
 * value has changed.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 *
 */
public interface TotalPowerConsumptionListener {

    /**
     * This method is called whenever the total power consumption of the digitalSTROM-System has changed.
     *
     * @param newPowerConsumption of the digitalSTROM-System
     */
    void onTotalPowerConsumptionChanged(int newPowerConsumption);

    /**
     * This method is called whenever the total energy meter value in Wh of the digitalSTROM-System has changed.
     *
     * @param newEnergyMeterValue of the digitalSTROM-System
     */
    void onEnergyMeterValueChanged(int newEnergyMeterValue);

    /**
     * This method is called whenever the total energy meter value in Ws of the digitalSTROM-System has changed.
     *
     * @param newEnergyMeterValue of the digitalSTROM-System
     */
    void onEnergyMeterWsValueChanged(int newEnergyMeterValue);
}
