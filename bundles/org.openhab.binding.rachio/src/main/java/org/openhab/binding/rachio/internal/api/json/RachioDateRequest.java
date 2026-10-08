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
 * Smart Hose date request.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
public class RachioDateRequest {
    public String date;

    public RachioDateRequest(LocalDate date) {
        this.date = date.toString();
    }
}
