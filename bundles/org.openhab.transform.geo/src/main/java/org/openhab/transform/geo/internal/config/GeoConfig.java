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
package org.openhab.transform.geo.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GeoConfig} class contains the parameters to configure geo encoding and decoding
 *
 * @author Bernd Weymann - initial contribution
 *
 */
@NonNullByDefault
public class GeoConfig {
    public String language = "";
    public String resolveDuration = "5m";
}
