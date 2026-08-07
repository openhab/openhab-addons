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
package org.openhab.binding.threedprinter.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for a Klipper printer accessed via the Moonraker API.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class KlipperConfiguration {
    public String hostname = "";
    public int port = 7125;
    public String apiKey = "";
    public int refreshInterval = 30;
}
