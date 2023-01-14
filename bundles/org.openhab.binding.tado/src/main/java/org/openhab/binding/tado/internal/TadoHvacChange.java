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
package org.openhab.binding.tado.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanLevel;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HorizontalSwing;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.OperationMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.VerticalSwing;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.builder.TerminationConditionBuilder;
import org.openhab.binding.tado.internal.builder.ZoneSettingsBuilder;
import org.openhab.binding.tado.internal.builder.ZoneStateProvider;
import org.openhab.binding.tado.internal.handler.TadoZoneHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Builder for incremental creation of zone overlays.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class TadoHvacChange {

    private final TadoZoneHandler zoneHandler;
    private final TerminationConditionBuilder terminationConditionBuilder;
    private final ZoneSettingsBuilder settingsBuilder;

    private boolean followSchedule = false;

    public TadoHvacChange(Thing zoneThing) {
        ThingHandler handler = zoneThing.getHandler();
        if (!(handler instanceof TadoZoneHandler)) {
            throw new IllegalArgumentException("TadoZoneThing expected, but instead got " + zoneThing);
        }
        zoneHandler = (TadoZoneHandler) handler;
        terminationConditionBuilder = TerminationConditionBuilder.of(zoneHandler);
        settingsBuilder = ZoneSettingsBuilder.of(zoneHandler);
    }

    public TadoHvacChange withOperationMode(OperationMode operationMode) {
        switch (operationMode) {
            case SCHEDULE:
                return followSchedule();
            case MANUAL:
                return activeForever();
            case TIMER:
                return activeForMinutes(0);
            case UNTIL_CHANGE:
                return activeUntilChange();
        }
        return this;
    }

    public TadoHvacChange followSchedule() {
        followSchedule = true;
        return this;
    }

    public TadoHvacChange activeForever() {
        terminationConditionBuilder.withTerminationType(OverlayTerminationConditionType.MANUAL);
        return this;
    }

    public TadoHvacChange activeUntilChange() {
        terminationConditionBuilder.withTerminationType(OverlayTerminationConditionType.TADO_MODE);
        return this;
    }

    public TadoHvacChange activeForMinutes(int minutes) {
        terminationConditionBuilder.withTerminationType(OverlayTerminationConditionType.TIMER);
        terminationConditionBuilder.withTimerDurationInSeconds(minutes * 60);
        return this;
    }

    public TadoHvacChange withTemperature(float temperatureValue) {
        settingsBuilder.withTemperature(temperatureValue, zoneHandler.getTemperatureUnit());
        return this;
    }

    public TadoHvacChange withHvacMode(HvacMode mode) {
        settingsBuilder.withMode(mode);
        return this;
    }

    public TadoHvacChange withHvacMode(String mode) {
        return withHvacMode(HvacMode.valueOf(mode.toUpperCase()));
    }

    public TadoHvacChange withSwing(boolean swingOn) {
        settingsBuilder.withSwing(swingOn);
        return this;
    }

    public TadoHvacChange withFanSpeed(FanSpeed fanSpeed) {
        settingsBuilder.withFanSpeed(fanSpeed);
        return this;
    }

    public TadoHvacChange withFanSpeed(String fanSpeed) {
        withFanSpeed(FanSpeed.valueOf(fanSpeed.toUpperCase()));
        return this;
    }

    public TadoHvacChange withFanLevel(FanLevel fanLevel) {
        settingsBuilder.withFanLevel(fanLevel);
        return this;
    }

    public TadoHvacChange withHorizontalSwing(HorizontalSwing horizontalSwing) {
        settingsBuilder.withHorizontalSwing(horizontalSwing);
        return this;
    }

    public TadoHvacChange withVerticalSwing(VerticalSwing verticalSwing) {
        settingsBuilder.withVerticalSwing(verticalSwing);
        return this;
    }

    public void apply() throws IOException, ApiException {
        if (followSchedule) {
            zoneHandler.removeOverlay();
        } else {
            Overlay overlay = buildOverlay();
            zoneHandler.setOverlay(overlay);
        }
    }

    private Overlay buildOverlay() throws IOException, ApiException {
        ZoneStateProvider zoneStateProvider = new ZoneStateProvider(zoneHandler);
        OverlayTerminationCondition terminationCondition = terminationConditionBuilder.build(zoneStateProvider);
        GenericZoneSetting setting = settingsBuilder.build(zoneStateProvider, zoneHandler.getZoneCapabilities());

        Overlay overlay = new Overlay();
        overlay.setTermination(terminationCondition);
        overlay.setSetting(setting);

        return overlay;
    }

    public TadoHvacChange withLight(boolean lightOn) {
        settingsBuilder.withLight(lightOn);
        return this;
    }
}
