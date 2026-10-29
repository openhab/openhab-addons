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
package org.openhab.binding.eyeonwater.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EyeOnWaterMeterConfiguration} class represents the configuration for a physical meter.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
public class EyeOnWaterMeterConfiguration {
    public String meterUuid = "";
    public String meterId = "";
}
