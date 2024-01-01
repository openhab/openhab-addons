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
package org.openhab.binding.lifx.internal;

import static org.openhab.binding.lifx.internal.LifxBindingConstants.DEFAULT_COLOR;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lifx.internal.dto.Effect;
import org.openhab.binding.lifx.internal.dto.HevCycleState;
import org.openhab.binding.lifx.internal.dto.PowerState;
import org.openhab.binding.lifx.internal.dto.SignalStrength;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.listener.LifxLightStateListener;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link LifxLightState} stores the properties that represent the state of a light.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightState {

    private HSBK[] colors = new HSBK[] { new HSBK(DEFAULT_COLOR) };
    private @Nullable HevCycleState hevCycleState;
    private @Nullable PercentType infrared;
    private @Nullable PowerState powerState;
    private @Nullable SignalStrength signalStrength;
    private @Nullable Effect tileEffect;

    private LocalDateTime lastChange = LocalDateTime.MIN;
    private List<LifxLightStateListener> listeners = new CopyOnWriteArrayList<>();

    public void copy(LifxLightState other) {
        this.powerState = other.getPowerState();
        this.colors = other.getColors();
        this.hevCycleState = other.getHevCycleState();
        this.infrared = other.getInfrared();
        this.signalStrength = other.getSignalStrength();
        this.tileEffect = other.getTileEffect();
    }

    public @Nullable PowerState getPowerState() {
        return powerState;
    }

    public HSBK getColor() {
        return colors.length > 0 ? new HSBK(colors[0]) : new HSBK(DEFAULT_COLOR);
    }

    public HSBK getColor(int zoneIndex) {
        return zoneIndex < colors.length ? new HSBK(colors[zoneIndex]) : new HSBK(DEFAULT_COLOR);
    }

    public HSBK[] getColors() {
        HSBK[] colorsCopy = new HSBK[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colorsCopy[i] = colors[i] != null ? new HSBK(colors[i]) : null;
        }
        return colorsCopy;
    }

    public @Nullable HevCycleState getHevCycleState() {
        return hevCycleState;
    }

    public @Nullable PercentType getInfrared() {
        return infrared;
    }

    public @Nullable SignalStrength getSignalStrength() {
        return signalStrength;
    }

    public @Nullable Effect getTileEffect() {
        return tileEffect;
    }

    public void setColor(HSBType newHSB) {
        HSBK newColor = getColor();
        newColor.setHSB(newHSB);
        setColor(newColor);
    }

    public void setColor(HSBType newHSB, int zoneIndex) {
        HSBK newColor = getColor(zoneIndex);
        newColor.setHSB(newHSB);
        setColor(newColor, zoneIndex);
    }

    public void setBrightness(PercentType brightness) {
        HSBK[] newColors = getColors();
        for (HSBK newColor : newColors) {
            newColor.setBrightness(brightness);
        }
        setColors(newColors);
    }

    public void setBrightness(PercentType brightness, int zoneIndex) {
        HSBK newColor = getColor(zoneIndex);
        newColor.setBrightness(brightness);
        setColor(newColor, zoneIndex);
    }

    public void setColor(HSBK newColor) {
        HSBK[] newColors = getColors();
        Arrays.fill(newColors, newColor);
        setColors(newColors);
    }

    public void setColor(HSBK newColor, int zoneIndex) {
        HSBK[] newColors = getColors();
        newColors[zoneIndex] = newColor;
        setColors(newColors);
    }

    public void setColors(HSBK[] newColors) {
        HSBK[] oldColors = this.colors;
        this.colors = newColors;
        updateLastChange();
        listeners.forEach(listener -> listener.handleColorsChange(oldColors, newColors));
    }

    public void setPowerState(OnOffType newOnOff) {
        setPowerState(PowerState.fromOnOffType(newOnOff));
    }

    public void setPowerState(PowerState newPowerState) {
        PowerState oldPowerState = this.powerState;
        this.powerState = newPowerState;
        updateLastChange();
        listeners.forEach(listener -> listener.handlePowerStateChange(oldPowerState, newPowerState));
    }

    public void setTemperature(int kelvin) {
        HSBK[] newColors = getColors();
        for (HSBK newColor : newColors) {
            newColor.setKelvin(kelvin);
        }
        setColors(newColors);
    }

    public void setTemperature(int kelvin, int zoneIndex) {
        HSBK newColor = getColor(zoneIndex);
        newColor.setKelvin(kelvin);
        setColor(newColor, zoneIndex);
    }

    public void setHevCycleState(HevCycleState newHevCycleState) {
        HevCycleState oldHevCycleState = this.hevCycleState;
        this.hevCycleState = newHevCycleState;
        updateLastChange();
        listeners.forEach(listener -> listener.handleHevCycleStateChange(oldHevCycleState, newHevCycleState));
    }

    public void setInfrared(PercentType newInfrared) {
        PercentType oldInfrared = this.infrared;
        this.infrared = newInfrared;
        updateLastChange();
        listeners.forEach(listener -> listener.handleInfraredChange(oldInfrared, newInfrared));
    }

    public void setSignalStrength(SignalStrength newSignalStrength) {
        SignalStrength oldSignalStrength = this.signalStrength;
        this.signalStrength = newSignalStrength;
        updateLastChange();
        listeners.forEach(listener -> listener.handleSignalStrengthChange(oldSignalStrength, newSignalStrength));
    }

    public void setTileEffect(Effect newEffect) {
        // Caller has to take care that newEffect is another object
        Effect oldEffect = tileEffect;
        tileEffect = newEffect;
        updateLastChange();
        listeners.forEach(listener -> listener.handleTileEffectChange(oldEffect, newEffect));
    }

    private void updateLastChange() {
        lastChange = LocalDateTime.now();
    }

    public Duration getDurationSinceLastChange() {
        return Duration.between(lastChange, LocalDateTime.now());
    }

    public void addListener(LifxLightStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LifxLightStateListener listener) {
        listeners.remove(listener);
    }
}
