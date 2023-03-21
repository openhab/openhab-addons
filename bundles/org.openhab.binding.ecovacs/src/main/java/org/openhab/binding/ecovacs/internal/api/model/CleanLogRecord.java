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
package org.openhab.binding.ecovacs.internal.api.model;

import java.util.Date;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class CleanLogRecord {
    public final Date timestamp;
    public final long cleaningDuration;
    public final int cleanedArea;
    public final Optional<String> mapImageUrl;
    public final CleanMode mode;

    public CleanLogRecord(long timestamp, long duration, int area, Optional<String> mapImageUrl, CleanMode mode) {
        this.timestamp = new Date(timestamp * 1000);
        this.cleaningDuration = duration;
        this.cleanedArea = area;
        this.mapImageUrl = mapImageUrl;
        this.mode = mode;
    }
}
