/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Mapping object for data returned from smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsDiscoveryData {
    // private Logger logger = LoggerFactory.getLogger(SmartthingsDiscoveryData.class);
    public long openHabStartTime;
    public long hubTime;
    // @Nullable
    // public String @Nullable [] data;
    public String[] data;

    public SmartthingsDiscoveryData() {
        data = new String[0];
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("openHabStartTime :").append(openHabStartTime);
        sb.append(", hubTime :").append(hubTime);
        sb.append(", data: ");
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]).append(" ");
        }
        return sb.toString();
    }
}
