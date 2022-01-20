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
package org.openhab.binding.opensprinkler.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.config.OpenSprinklerHttpInterfaceConfig;

/**
 * The {@link OpenSprinklerHttpApiV219} class is used for communicating with
 * the firmware versions 2.1.9 and up.
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
    public void ignoreRain(int station, boolean command) throws CommunicationApiException, UnauthorizedApiException {
        int arrayIndex = station / 8;
        int bit = station % 8;
        logger.debug("Ignore Rain for Station:{} is being looked in index: {} and bit:{}", station, arrayIndex, bit);
        byte status = state.jnReply.ignoreRain[arrayIndex];
        if (command) {
            status |= 1 << bit;
        } else {
            status &= ~(1 << bit);
        }
        http.sendHttpGet(getBaseUrl() + "cs", getRequestRequiredOptions() + "&i" + arrayIndex + "=" + status);
    }

    @Override
    public boolean isIgnoringRain(int station) {
        int arrayIndex = station / 8;
        int bit = station % 8;
        byte status = state.jnReply.ignoreRain[arrayIndex];
        return (status & (1 << bit)) != 0;
    }
}
