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
package org.openhab.binding.tibber.internal.history;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link TibberHistoryListener} is notified when history data has been fetched from Tibber API.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Bernd Weymann - Add history channel group
 */
@NonNullByDefault
public interface TibberHistoryListener {
    /**
     * Called when history data for a given time window has been updated.
     *
     * @param request the history request (time window + full/partial flag)
     * @param series the updated series, or null if the fetch is still in progress
     */
    void historyUpdated(TibberHistory.HistoryRequest request, @Nullable TibberHistorySeries series);
}
