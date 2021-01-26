/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;

/**
 * The {@link OpenSprinklerHttpApiV219} class is used for communicating with
 * the OpenSprinkler API for firmware versions 2.1.9 and up.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerHttpApiV219 extends OpenSprinklerHttpApiV217 {

    OpenSprinklerHttpApiV219(final HttpClient httpClient, final OpenSprinklerHttpInterfaceConfig config)
            throws GeneralApiException, CommunicationApiException {
        super(httpClient, config);
    }

    @Override
    public boolean isRainDetected() {
        JcResponse localReply = jcReply;
        if (localReply != null && localReply.sn1 == 1) {
            return true;
        }
        return false;
    }
}
