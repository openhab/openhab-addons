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
package org.openhab.binding.freeboxos.internal.api.upnpav;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.upnpav.UPnPAVResponses.ConfigResponse;
import org.openhab.binding.freeboxos.internal.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;

/**
 * The {@link UPnPAVManager} is the Java class used to handle api requests related to UPnP AV
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class UPnPAVManager extends ActivableRest<UPnPAVConfig, ConfigResponse> {

    public UPnPAVManager(FreeboxOsSession session) {
        super(session, ConfigResponse.class, UPNPAV_URL, CONFIG_SUB_PATH);
    }
}
