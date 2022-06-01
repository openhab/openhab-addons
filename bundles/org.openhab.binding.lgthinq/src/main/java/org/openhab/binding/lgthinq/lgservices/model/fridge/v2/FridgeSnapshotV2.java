/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model.fridge.v2;

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.binding.lgthinq.lgservices.model.Snapshot;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link FridgeSnapshotV2}
 * This map the snapshot result from Washing Machine devices
 * This json payload come with path: snapshot->fridge, but this POJO expects
 * to map field below washerDryer
 * 
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
public class FridgeSnapshotV2 implements Snapshot, org.openhab.binding.lgthinq.lgservices.model.fridge.FridgeSnapshot {

    private boolean online;
    private String fridgeTemp = FRIDGE_TEMPERATURE_IGNORE_VALUE;
    private String freezerTemp = FREEZER_TEMPERATURE_IGNORE_VALUE;
    private String tempUnit = TEMP_UNIT_CELSIUS; // celsius as default

    @Override
    @JsonAlias({ "TempUnit" })
    @JsonProperty("tempUnit")
    public String getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    @Override
    @JsonAlias({ "TempRefrigerator" })
    @JsonProperty("fridgeTemp")
    public String getFridgeTemp() {
        return fridgeTemp;
    }

    public void setFridgeTemp(String fridgeTemp) {
        this.fridgeTemp = fridgeTemp;
    }

    @Override
    @JsonAlias({ "TempFreezer" })
    @JsonProperty("freezerTemp")
    public String getFreezerTemp() {
        return freezerTemp;
    }

    public void setFreezerTemp(String freezerTemp) {
        this.freezerTemp = freezerTemp;
    }

    @Override
    public void loadSnapshot(Object veryRootNode) {
    }

    @Override
    public DevicePowerState getPowerStatus() {
        throw new IllegalStateException("Fridge has no Power state.");
    }

    @Override
    public void setPowerStatus(DevicePowerState value) {
        throw new IllegalStateException("Fridge has no Power state.");
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public void setOnline(boolean online) {
        this.online = online;
    }
}
