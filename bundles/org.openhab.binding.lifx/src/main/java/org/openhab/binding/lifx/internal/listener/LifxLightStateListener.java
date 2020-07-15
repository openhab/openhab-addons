/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lifx.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.lifx.internal.LifxLightState;
import org.openhab.binding.lifx.internal.fields.HSBK;
import org.openhab.binding.lifx.internal.protocol.Effect;
import org.openhab.binding.lifx.internal.protocol.PowerState;
import org.openhab.binding.lifx.internal.protocol.SignalStrength;

/**
 * The {@link LifxLightStateListener} is notified when the properties of a {@link LifxLightState} change.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface LifxLightStateListener {

    /**
     * Called when the colors property changes.
     *
     * @param oldColors the old colors value
     * @param newColors the new colors value
     */
    void handleColorsChange(HSBK[] oldColors, HSBK[] newColors);

    /**
     * Called when the power state property changes.
     *
     * @param oldPowerState the old power state value
     * @param newPowerState the new power state value
     */
    void handlePowerStateChange(@Nullable PowerState oldPowerState, PowerState newPowerState);

    /**
     * Called when the infrared property changes.
     *
     * @param oldInfrared the old infrared value
     * @param newInfrared the new infrared value
     */
    void handleInfraredChange(@Nullable PercentType oldInfrared, PercentType newInfrared);

    /**
     * Called when the signal strength property changes.
     *
     * @param oldSignalStrength the old signal strength value
     * @param newSignalStrength the new signal strength value
     */
    void handleSignalStrengthChange(@Nullable SignalStrength oldSignalStrength, SignalStrength newSignalStrength);

    /**
     * Called when the tile effect changes.
     *
     * @param oldEffect the old tile effect value
     * @param newEffect new tile effectvalue
     */
    void handleTileEffectChange(@Nullable Effect oldEffect, Effect newEffect);
}
