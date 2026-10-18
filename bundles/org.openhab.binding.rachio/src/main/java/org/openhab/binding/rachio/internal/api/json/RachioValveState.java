/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal.api.json;

import static org.openhab.binding.rachio.internal.RachioUtils.firstNonBlank;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Smart Hose valve synchronization state.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveState {
    public @Nullable Boolean matches;
    public @Nullable Boolean online;
    public @Nullable Boolean connected;
    public @Nullable Boolean flowDetected;
    public String flowDetectedText = "";
    public @Nullable Integer defaultRuntimeSeconds;

    public boolean getFlowDetected() {
        Boolean flowDetected = this.flowDetected;
        return flowDetected != null ? flowDetected.booleanValue()
                : Boolean.parseBoolean(firstNonBlank(flowDetectedText));
    }

    public boolean hasFlowDetected() {
        return flowDetected != null || !firstNonBlank(flowDetectedText).isBlank();
    }
}
