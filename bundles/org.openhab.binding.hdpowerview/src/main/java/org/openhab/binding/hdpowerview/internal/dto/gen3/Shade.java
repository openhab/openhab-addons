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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.dto.Firmware;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * DTO for a shade as returned by an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Shade {
    private @Nullable Integer id;
    private @Nullable Integer type;
    private @Nullable String name;
    private @Nullable @SuppressWarnings("unused") String ptName;
    private @Nullable Integer capabilities;
    private @Nullable Integer powerType;
    private @Nullable Integer batteryStatus;
    private @Nullable Integer signalStrength;
    private @Nullable String bleName;
    private @Nullable Firmware firmware;
    private @Nullable ShadePosition positions;

    private transient boolean partialState;

    public State getBatteryLevel() {
        Integer batteryStatus = this.batteryStatus;
        return batteryStatus == null ? UnDefType.UNDEF
                : new DecimalType(Math.max(0, Math.min(100, (100 * batteryStatus) / 3)));
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
        Integer id = this.id;
        return id != null ? id.intValue() : 0;
    }

    public State getLowBattery() {
        Integer batteryStatus = this.batteryStatus;
        return batteryStatus == null ? UnDefType.UNDEF : OnOffType.from(batteryStatus == 0);
    }

    public String getName() {
        return new String(Base64.getDecoder().decode(name), StandardCharsets.UTF_8);
    }

    public State getPosition(CoordinateSystem posKindCoords) {
        ShadePosition positions = this.positions;
        return positions == null ? UnDefType.UNDEF : positions.getState(posKindCoords);
    }

    public PowerType getPowerType() {
        Integer powerType = this.powerType;
        return PowerType.values()[powerType != null ? powerType.intValue() : 0];
    }

    public @Nullable ShadePosition getShadePositions() {
        return positions;
    }

    public State getSignalStrength() {
        Integer signalStrength = this.signalStrength;
        return signalStrength != null ? new QuantityType<>(signalStrength, Units.DECIBEL_MILLIWATTS) : UnDefType.UNDEF;
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
        return getPowerType() == PowerType.HARDWIRED;
    }

    public Shade setCapabilities(int capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    public Shade setId(int id) {
        this.id = id;
        return this;
    }

    public Shade setPartialState() {
        this.partialState = true;
        return this;
    }

    public Shade setPosition(CoordinateSystem coordinates, PercentType percent) {
        ShadePosition positions = this.positions;
        if (positions == null) {
            positions = new ShadePosition();
            this.positions = positions;
        }
        positions.setPosition(coordinates, percent);
        return this;
    }

    public Shade setShadePosition(ShadePosition position) {
        this.positions = position;
        return this;
    }

    public Shade setType(int type) {
        this.type = type;
        return this;
    }
}
