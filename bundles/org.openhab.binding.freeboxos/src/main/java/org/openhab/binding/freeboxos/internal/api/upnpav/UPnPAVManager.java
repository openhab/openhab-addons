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
package org.openhab.binding.freeboxos.internal.api.upnpav;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.upnpav.UPnPAVConfig.UPnPAVConfigResponse;

/**
 * The {@link UPnPAVManager} is the Java class used to handle api requests
 * related to UPnP AV
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class UPnPAVManager extends ActivableRest<UPnPAVConfig, UPnPAVConfigResponse> {
    private static final String UPNPAV_URL = "upnpav";

    public UPnPAVManager(FreeboxOsSession session) {
        super(session, UPnPAVConfigResponse.class, UPNPAV_URL, CONFIG_SUB_PATH);
    }
}
