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
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsBridgeConfig {

    public String appName = "";
    public String clientId = "";
    public String clientSecret = "";
    public int pollingTime = -1;
    public boolean useCloudWebhook = false;
    public boolean useDynamicThings = false;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("appName = ").append(appName);
        sb.append("clientId = ").append(clientId);
        sb.append("clientSecret = ").append(clientSecret.isBlank() ? "" : "<redacted>");
        sb.append("pollingTime = ").append(pollingTime);
        sb.append("useCloudWebhook = ").append(useCloudWebhook);
        sb.append("useDynamicThings = ").append(useDynamicThings);
        return sb.toString();
    }
}
