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
package org.openhab.binding.freeboxos.internal.api.airmedia;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.airmedia.AirMediaConfig.AirMediaConfigResponse;
import org.openhab.binding.freeboxos.internal.api.rest.ActivableRest;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;

/**
 * The {@link AirMediaManager} is the Java class used to handle api requests
 * related to air media
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaManager extends ActivableRest<AirMediaConfig, AirMediaConfigResponse> {
    public static final String AIR_MEDIA_PATH = "airmedia";

    public AirMediaManager(FreeboxOsSession session) {
        super(session, AirMediaConfigResponse.class, AIR_MEDIA_PATH, CONFIG_SUB_PATH);
        session.addManager(MediaReceiverManager.class, new MediaReceiverManager(session, getUriBuilder()));
    }
}
