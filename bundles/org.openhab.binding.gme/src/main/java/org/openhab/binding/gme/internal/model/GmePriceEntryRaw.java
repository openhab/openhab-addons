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
package org.openhab.binding.gme.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Raw price entry as represented by the GME API.
 *
 * Numeric values are intentionally kept as strings because the GME JSON
 * response encodes fields such as Hour, Price and Period as strings.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public record GmePriceEntryRaw(String flowDate, String hour, String market, String zone, String price, String period,
        @Nullable String notes) {
}
