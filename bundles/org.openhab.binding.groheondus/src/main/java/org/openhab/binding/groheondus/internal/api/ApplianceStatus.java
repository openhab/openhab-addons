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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Status reported by the cloud for an appliance.
 *
 * @param batteryLevel battery level in percent, if reported
 * @param connected whether the cloud considers the device connected, if reported
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public record ApplianceStatus(@Nullable Integer batteryLevel, @Nullable Boolean connected) {
}
