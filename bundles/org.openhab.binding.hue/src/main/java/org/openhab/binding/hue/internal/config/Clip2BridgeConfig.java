/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for the Clip2BridgeHandler.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Clip2BridgeConfig {
    public static final String APPLICATION_KEY = "applicationKey";

    public String ipAddress = "";
    public String applicationKey = "";
    public int checkMinutes = 60;
    public boolean useSelfSignedCertificate = true;
}
