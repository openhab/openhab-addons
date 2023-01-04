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
package org.openhab.binding.wled.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wled.internal.handlers.WLedBridgeHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WledApiFactory} is responsible for creating an instance of the API that is optimized for different
 * firmware versions.
 *
 * @author Matthew Skinner - Initial contribution
 */
@Component(service = WledApiFactory.class)
@NonNullByDefault
public class WledApiFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpClient httpClient;

    @Activate
    public WledApiFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    public WledApi getApi(WLedBridgeHandler handler) throws ApiException {
        WledApi lowestSupportedApi = new WledApiV084(handler, httpClient);
        int version = lowestSupportedApi.getFirmwareVersion();
        logger.debug("Treating firmware as int:{}", version);
        if (version >= 130) {
            return new WledApiV0130(handler, httpClient);
        } else if (version >= 110) {
            return new WledApiV0110(handler, httpClient);
        } else if (version >= 100) {
            return new WledApiV084(handler, httpClient);
        }
        logger.warn("Your WLED firmware is very old, upgrade to at least 0.10.0");
        return lowestSupportedApi;
    }
}
