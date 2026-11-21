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
package org.openhab.binding.knx.internal.monitor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A value as the monitor shows it: the number or text, and the unit apart.
 *
 * @param value the value without its unit
 * @param unit the unit, null if the value has none
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record ValueText(String value, @Nullable String unit) {
}
