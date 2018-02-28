/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal;

import java.io.IOException;

import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.tado.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.TadoBindingConstants.OperationMode;
import org.openhab.binding.tado.handler.TadoZoneHandler;
import org.openhab.binding.tado.internal.api.TadoClientException;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.Overlay;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.openhab.binding.tado.internal.builder.TerminationConditionBuilder;
import org.openhab.binding.tado.internal.builder.ZoneSettingsBuilder;

/**
 * Builder for incremental creation of zone overlays.
 *
 * @author Dennis Frommknecht - Iniital contribution
 */
public class TadoHvacChange {
    private TadoZoneHandler zoneHandler;

    private boolean followSchedule = false;
    private TerminationConditionBuilder terminationConditionBuilder;
    private ZoneSettingsBuilder settingsBuilder;

    public TadoHvacChange(Thing zoneThing) {
        if (!(zoneThing.getHandler() instanceof TadoZoneHandler)) {
            throw new IllegalArgumentException("TadoZoneThing expected, but instead got " + zoneThing);
        }

        this.zoneHandler = (TadoZoneHandler) zoneThing.getHandler();
        this.terminationConditionBuilder = TerminationConditionBuilder.of(zoneHandler);
        this.settingsBuilder = ZoneSettingsBuilder.of(zoneHandler);
    }

    public TadoHvacChange withOperationMode(OperationMode operationMode) {
        switch (operationMode) {
            case SCHEDULE:
                return followSchedule();
            case MANUAL:
                return activeForever();
            case TIMER:
                return activeFor(null);
            case UNTIL_CHANGE:
                return activeUntilChange();
            default:
                return this;
        }
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

    public TadoHvacChange activeFor(Integer minutes) {
        terminationConditionBuilder.withTerminationType(OverlayTerminationConditionType.TIMER);
        terminationConditionBuilder.withTimerDurationInSeconds(minutes != null ? minutes * 60 : null);
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

    public void apply() throws IOException, TadoClientException {
        if (followSchedule) {
            zoneHandler.removeOverlay();
        } else {
            Overlay overlay = buildOverlay();
            zoneHandler.setOverlay(overlay);
        }
    }

    private Overlay buildOverlay() throws IOException, TadoClientException {
        ZoneState zoneState = getZoneState(); // TODO: encapsulate in Provider class to get only if required and cache
        OverlayTerminationCondition terminationCondition = terminationConditionBuilder.build(zoneState);
        GenericZoneSetting setting = settingsBuilder.build(zoneState, zoneHandler.getZoneCapabilities());

        Overlay overlay = new Overlay();
        overlay.setTermination(terminationCondition);
        overlay.setSetting(setting);
        overlay.setType(terminationCondition.getType());

        return overlay;
    }

    private ZoneState getZoneState() throws IOException, TadoClientException {
        ZoneState zoneState = zoneHandler.getZoneState();
        // empty zone state behaves like a NULL object
        return zoneState != null ? zoneState : new ZoneState();
    }
}
