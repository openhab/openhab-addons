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
package org.openhab.binding.smartthings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration data for Smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsBridgeConfig {

    public String clientId = "f9fd90b7-c4bb-49c1-974f-e80b89a7327f";
    public String clientSecret = "ec3d221b-72f6-4612-bb92-58ae5ebfe562";

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("clientId = ").append(clientId);
        sb.append("clientSecret = ").append(clientSecret);
        return sb.toString();
    }
}
