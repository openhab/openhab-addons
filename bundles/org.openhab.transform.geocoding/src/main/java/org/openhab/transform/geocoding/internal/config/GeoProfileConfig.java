/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.geocoding.internal.config;

import static org.openhab.transform.geocoding.internal.GeoProfileConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GeoProfileConfig} class contains the configuration parameters for the profile
 *
 * @author Bernd Weymann - initial contribution
 *
 */
@NonNullByDefault
public class GeoProfileConfig {
    public String provider = PROVIDER_NOMINATIM_OPENSTREETMAP;
    public String format = ROW_ADDRESS_FORMAT;
    public String resolveInterval = "5m";
    public String language = "";
}
