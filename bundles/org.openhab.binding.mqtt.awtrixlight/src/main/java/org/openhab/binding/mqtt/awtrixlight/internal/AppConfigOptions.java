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
package org.openhab.binding.mqtt.awtrixlight.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AppConfigOptions} Holds the config for the app settings.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class AppConfigOptions {
    public String appname = "";
    public boolean useButtons = false;
}
