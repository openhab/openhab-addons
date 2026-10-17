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
package org.openhab.binding.melcloud.internal.home.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * One {@code {x, y}} data point in a {@code report/v1/trendsummary} dataset (see
 * {@link MelCloudHomeTrendSummaryReport}). {@code x} is an ISO-8601-like timestamp, {@code y} the measured value.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MelCloudHomeTrendDataPoint {

    public String x = "";
    public double y;
}
