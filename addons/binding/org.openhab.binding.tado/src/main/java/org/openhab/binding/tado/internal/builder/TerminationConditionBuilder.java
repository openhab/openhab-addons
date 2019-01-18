/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
public class TerminationConditionBuilder {
    private TadoZoneHandler zoneHandler;

    private OverlayTerminationConditionType terminationType = null;
    private Integer timerDurationInSeconds = null;

    protected TerminationConditionBuilder(TadoZoneHandler zoneHandler) {
        this.zoneHandler = zoneHandler;
    }

    public static TerminationConditionBuilder of(TadoZoneHandler zoneHandler) {
        return new TerminationConditionBuilder(zoneHandler);
    }

    public TerminationConditionBuilder withTerminationType(OverlayTerminationConditionType terminationType) {
        this.terminationType = terminationType;
        if (terminationType != OverlayTerminationConditionType.TIMER) {
            timerDurationInSeconds = null;
        }

        return this;
    }

    public TerminationConditionBuilder withTimerDurationInSeconds(Integer timerDurationInSeconds) {
        this.terminationType = OverlayTerminationConditionType.TIMER;
        this.timerDurationInSeconds = timerDurationInSeconds;
        return this;
    }

    public OverlayTerminationCondition build(ZoneStateProvider zoneStateProvider)
            throws IOException, ApiException {
        OverlayTerminationCondition terminationCondition = null;

        if (terminationType != null) {
            if (terminationType != OverlayTerminationConditionType.TIMER || timerDurationInSeconds != null) {
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
        OverlayTerminationCondition defaultTerminationCondition = zoneHandler.getDefaultTerminationCondition();
        return defaultTerminationCondition != null ? defaultTerminationCondition : manualTermination();
    }

    private TimerTerminationCondition getCurrentOrDefaultTimerTermination(ZoneStateProvider zoneStateProvider)
            throws IOException, ApiException {
        // Timer without duration
        int duration = zoneHandler.getFallbackTimerDuration() * 60;

        ZoneState zoneState = zoneStateProvider.getZoneState();

        // If timer is currently running, use its time
        if (zoneState.getOverlay() != null
                && zoneState.getOverlay().getTermination().getType() == OverlayTerminationConditionType.TIMER) {
            duration = ((TimerTerminationCondition) zoneState.getOverlay().getTermination()).getDurationInSeconds();
        }

        return timerTermination(duration);
    }
}
