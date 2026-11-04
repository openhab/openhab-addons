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
package org.openhab.binding.solarman.internal.sunsynk;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;

/**
 * Shared connection settings and the device clock time zone.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public class SunsynkConfiguration extends SolarmanLoggerConfiguration {
    public String timeZone = ZoneId.systemDefault().getId();
}
