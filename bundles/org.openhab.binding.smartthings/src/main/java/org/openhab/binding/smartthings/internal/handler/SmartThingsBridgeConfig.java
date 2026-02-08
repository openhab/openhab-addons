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
package org.openhab.binding.smartthings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration data for SmartThings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsBridgeConfig {

    public String clientId = "e91265c7-42b1-4928-ab52-67ff1f80cc5c";
    public String clientSecret = "85697243-75e2-4e2b-ac4e-d29e784b2116";

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("clientId = ").append(clientId);
        sb.append("clientSecret = ").append(clientSecret);
        return sb.toString();
    }
}
