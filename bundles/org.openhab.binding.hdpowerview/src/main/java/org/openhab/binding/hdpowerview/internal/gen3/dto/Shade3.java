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
package org.openhab.binding.hdpowerview.internal.gen3.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * State of a Shade as returned by an HD PowerView hub of Generation 3.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Shade3 {
    private int id;
    private @Nullable Integer type;
    private @Nullable String name;
    private @Nullable String ptName;
    private @Nullable Integer capabilities;
    private @Nullable String powerType; // TODO unclear if this is String or Integer
    private @Nullable Integer batteryStatus;
    // private @Nullable Integer roomId;
    private @Nullable Integer signalStrength;
    private @Nullable String bleName;
    private @Nullable Firmware firmware;
    private @Nullable ShadePosition3 positions;

    private transient boolean partialState;

    public State getBatteryLevel() {
        Integer batteryStatus = this.batteryStatus;
        return batteryStatus == null ? UnDefType.UNDEF
                : new PercentType(Math.max(0, Math.min(100, (100 * batteryStatus) / 3)));
    }

    public @Nullable String getBleName() {
        return bleName;
    }

    public @Nullable Integer getCapabilities() {
        return capabilities;
    }

    public @Nullable String getCapabilitieString() {
        Integer capabilities = this.capabilities;
        return capabilities == null ? null : Integer.toString(capabilities);
    }

    public @Nullable String getFirmware() {
        Firmware firmware = this.firmware;
        return firmware == null ? null
                : String.format("%d.%d.%d", firmware.revision, firmware.subRevision, firmware.build);
    }

    public int getId() {
        return id;
    }

    public State getLowBattery() {
        Integer batteryStatus = this.batteryStatus;
        return batteryStatus == null ? UnDefType.UNDEF : OnOffType.from(batteryStatus == 0);
    }

    public String getName() {
        return String.join(" ", new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8), ptName);
    }

    public State getPosition(CoordinateSystem posKindCoords) {
        ShadePosition3 positions = this.positions;
        return positions == null ? UnDefType.UNDEF : positions.getState(posKindCoords);
    }

    public @Nullable String getPowerType() {
        return powerType;
    }

    public @Nullable ShadePosition3 getShadePositions() {
        return positions;
    }

    public State getSignalStrength() {
        Integer signalStrength = this.signalStrength;
        return signalStrength == null ? UnDefType.UNDEF : new DecimalType(signalStrength);
    }

    public @Nullable Integer getType() {
        return type;
    }

    public @Nullable String getTypeString() {
        Integer type = this.type;
        return type == null ? null : Integer.toString(type);
    }

    public boolean hasFullState() {
        return !partialState;
    }

    public boolean isMainsPowered() {
        // check powerType and return true or false
        return false;
    }

    public Shade3 setCapabilities(int capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public Shade3 setId(int id) {
        this.id = id;
        return this;
    }

    public Shade3 setPartialState() {
        this.partialState = true;
        return this;
    }

    public Shade3 setPosition(CoordinateSystem coordinates, int percent) {
        ShadePosition3 positions = this.positions;
        if (positions == null) {
            positions = new ShadePosition3();
            this.positions = positions;
        }
        positions.setPosition(coordinates, percent);
        return this;
    }

    public Shade3 setShadePosition(ShadePosition3 position) {
        this.positions = position;
        return this;
    }

    public Shade3 setType(int type) {
        this.type = type;
        return this;
    }
}
