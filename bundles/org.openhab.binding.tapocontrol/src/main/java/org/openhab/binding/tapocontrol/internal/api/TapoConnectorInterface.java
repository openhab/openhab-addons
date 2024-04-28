/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tapocontrol.internal.dto.TapoResponse;
import org.openhab.binding.tapocontrol.internal.helpers.TapoErrorHandler;

/**
 * Interface for TAPO-Protocol
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public interface TapoConnectorInterface {

    /* get http-client from bridge */
    HttpClient getHttpClient();

    /* handle received taporesponse */
    public void handleResponse(TapoResponse tapoResponse, String command) throws TapoErrorHandler;

    /* handle error */
    public void handleError(TapoErrorHandler e);

    /* handle received reponse-string */
    public void responsePasstrough(String response, String command);

    /* get base url of device */
    public String getBaseUrl();

    /* geth ThingUID of device */
    public String getThingUID();
}
