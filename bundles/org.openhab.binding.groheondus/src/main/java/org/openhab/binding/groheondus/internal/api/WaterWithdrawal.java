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
package org.openhab.binding.groheondus.internal.api;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Water consumption in litres within the aggregation period starting at {@code timestamp}.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public record WaterWithdrawal(Instant timestamp, double waterConsumption) {
}
