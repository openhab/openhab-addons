/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.api.wifi;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.wifi.WifiConfig.WifiConfigResponse;

/**
 * The {@link WifiManager} is the Java class used to handle api requests
 * related to wifi
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiManager extends ActivableRest<WifiConfig, WifiConfigResponse> {
    public static final String WIFI_SUB_PATH = "wifi";

    public WifiManager(FreeboxOsSession session) {
        super(session, WifiConfigResponse.class, WIFI_SUB_PATH, CONFIG_SUB_PATH);
        session.addManager(APManager.class, new APManager(session, getUriBuilder()));
    }
}
