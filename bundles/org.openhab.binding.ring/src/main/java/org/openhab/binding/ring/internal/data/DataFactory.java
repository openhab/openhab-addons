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
package org.openhab.binding.ring.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.ApiConstants;

/**
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class DataFactory {
    public static String getOauthData(String username, String password) {
        return "";
    }

    /**
     * Get GET parameters for the session API resource.
     *
     * @return
     */
    public static String getSessionParams(String hardwareId) {
        ParamBuilder pb = new ParamBuilder(false);
        pb.add("device[os]", "android");
        pb.add("device[hardware_id]", hardwareId);
        pb.add("device[app_brand]", "ring");
        pb.add("device[metadata][device_model]", "VirtualBox");
        pb.add("device[metadata][resolution]", "600x800");
        pb.add("device[metadata][app_version]", "1.7.29");
        pb.add("device[metadata][app_installation_date]", "");
        pb.add("device[metadata][os_version]", "4.4.4");
        pb.add("device[metadata][manufacturer]", "innotek GmbH");
        pb.add("device[metadata][is_tablet]", "true");
        pb.add("device[metadata][linphone_initialized]", "true");
        pb.add("device[metadata][language]", "en");
        pb.add("api_version", "" + ApiConstants.API_VERSION);
        return pb.toString();
    }
}
