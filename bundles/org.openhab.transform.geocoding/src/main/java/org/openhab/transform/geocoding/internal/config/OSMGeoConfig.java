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

import static org.openhab.transform.geocoding.internal.OSMGeoConstants.ROW_ADDRESS_FORMAT;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OSMGeoConfig} class contains the configuration parameters for OSM geocoding/reverse geocoding
 *
 * @author Bernd Weymann - initial contribution
 *
 */
@NonNullByDefault
public class OSMGeoConfig {
    public String format = ROW_ADDRESS_FORMAT;
    public String resolveDuration = "5m";
    public String language = "";
}
