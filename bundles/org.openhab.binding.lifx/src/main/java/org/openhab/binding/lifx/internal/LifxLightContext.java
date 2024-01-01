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

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lifx.internal.LifxProduct.Features;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler;
import org.openhab.binding.lifx.internal.handler.LifxLightHandler.CurrentLightState;

/**
 * The {@link LifxLightContext} shares the context of a light with {@link LifxLightHandler} helper objects.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class LifxLightContext {

    private final String logId;
    private final LifxLightConfig configuration;
    private final CurrentLightState currentLightState;
    private final LifxLightState pendingLightState;
    private final Features features;
    private final ScheduledExecutorService scheduler;

    public LifxLightContext(String logId, Features features, LifxLightConfig configuration,
            CurrentLightState currentLightState, LifxLightState pendingLightState, ScheduledExecutorService scheduler) {
        this.logId = logId;
        this.configuration = configuration;
        this.features = features;
        this.currentLightState = currentLightState;
        this.pendingLightState = pendingLightState;
        this.scheduler = scheduler;
    }

    public String getLogId() {
        return logId;
    }

    public LifxLightConfig getConfiguration() {
        return configuration;
    }

    public Features getFeatures() {
        return features;
    }

    public CurrentLightState getCurrentLightState() {
        return currentLightState;
    }

    public LifxLightState getPendingLightState() {
        return pendingLightState;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
