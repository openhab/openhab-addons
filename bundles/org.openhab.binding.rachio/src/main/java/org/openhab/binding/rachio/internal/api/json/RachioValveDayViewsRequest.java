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

import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Smart Hose day-view request.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioValveDayViewsRequest {
    public RachioDateRequest start;
    public RachioDateRequest end;
    public RachioResourceId resourceId;

    public RachioValveDayViewsRequest(LocalDate start, LocalDate end, String valveId) {
        this.start = new RachioDateRequest(start);
        this.end = new RachioDateRequest(end);
        this.resourceId = new RachioResourceId();
        this.resourceId.valveId = valveId;
    }
}
