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
package org.openhab.binding.solax.internal.connectivity;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CloudHttpConnector} class uses HttpUtil to retrieve the raw JSON data from Inverter's Wi-Fi module.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class CloudHttpConnector implements SolaxConnector {

    private static final int HTTP_REQUEST_TIME_OUT = 5000;

    private static final String CONTENT_TYPE = "application/json; charset=utf-8";

    private final Logger logger = LoggerFactory.getLogger(CloudHttpConnector.class);

    private static final String URI = """
            https://www.solaxcloud.com/proxyApp/proxy/api/getRealtimeInfo.do?tokenId={tokenId}&sn={serialNumber}
            """;

    private String uri;

    public CloudHttpConnector(String tokenId, String serialNumber) {
        this(URI, tokenId, serialNumber);
    }

    public CloudHttpConnector(String uri, String tokenId, String serialNumber) {
        this.uri = uri.replace("{tokenId}", tokenId).replace("{serialNumber}", serialNumber).trim();
    }

    @Override
    public @Nullable String retrieveData() throws IOException {
        logger.debug("About to retrieve data from Uri: {}", uri);
        String result = HttpUtil.executeUrl(HttpMethod.GET.name(), uri, null, CONTENT_TYPE, HTTP_REQUEST_TIME_OUT);
        logger.trace("Retrieved content = {}", result);
        return result;
    }
}
