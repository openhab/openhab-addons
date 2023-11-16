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
package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.TimerTerminationCondition;
import org.openhab.binding.tado.internal.api.model.ZoneState;
import org.openhab.binding.tado.internal.handler.TadoZoneHandler;

/**
 * Builder for creation of overlay termination conditions.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class TerminationConditionBuilder {

    private final TadoZoneHandler zoneHandler;

    private @Nullable OverlayTerminationConditionType terminationType;
    private int timerDurationInSeconds = 0;

    protected TerminationConditionBuilder(TadoZoneHandler zoneHandler) {
        this.zoneHandler = zoneHandler;
    }

    public static TerminationConditionBuilder of(TadoZoneHandler zoneHandler) {
        return new TerminationConditionBuilder(zoneHandler);
    }

    public TerminationConditionBuilder withTerminationType(OverlayTerminationConditionType terminationType) {
        this.terminationType = terminationType;
        if (terminationType != OverlayTerminationConditionType.TIMER) {
            timerDurationInSeconds = 0;
        }
        return this;
    }

    public TerminationConditionBuilder withTimerDurationInSeconds(int timerDurationInSeconds) {
        this.terminationType = OverlayTerminationConditionType.TIMER;
        this.timerDurationInSeconds = timerDurationInSeconds;
        return this;
    }

    public OverlayTerminationCondition build(ZoneStateProvider zoneStateProvider) throws IOException, ApiException {
        OverlayTerminationCondition terminationCondition;

        OverlayTerminationConditionType terminationType = this.terminationType;
        if (terminationType != null) {
            if (terminationType != OverlayTerminationConditionType.TIMER || timerDurationInSeconds > 0) {
                terminationCondition = getTerminationCondition(terminationType, timerDurationInSeconds);
            } else {
                terminationCondition = getCurrentOrDefaultTimerTermination(zoneStateProvider);
            }
        } else {
            ZoneState zoneState = zoneStateProvider.getZoneState();
            if (zoneState.getOverlay() != null) {
                terminationCondition = cleanTerminationCondition(zoneState.getOverlay().getTermination());
            } else {
                // Default zone termination condition
                terminationCondition = getDefaultTerminationCondition();
            }
        }

        return terminationCondition;
    }

    private OverlayTerminationCondition getDefaultTerminationCondition() throws IOException, ApiException {
        return zoneHandler.getDefaultTerminationCondition();
    }

    private TimerTerminationCondition getCurrentOrDefaultTimerTermination(ZoneStateProvider zoneStateProvider)
            throws IOException, ApiException {
        // Timer without duration
        Integer duration = zoneHandler.getFallbackTimerDuration() * 60;

        ZoneState zoneState = zoneStateProvider.getZoneState();

        // If timer is currently running, use its time
        if (zoneState.getOverlay() != null
                && zoneState.getOverlay().getTermination().getType() == OverlayTerminationConditionType.TIMER) {
            duration = ((TimerTerminationCondition) zoneState.getOverlay().getTermination()).getDurationInSeconds();
        }

        return timerTermination(duration);
    }
}
